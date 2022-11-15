import {
  DoctorList,
  tabbarList,
  patientPage,
  inquisitionPage,
  appKey,
  randomNameList,
} from './config/index'
import { IAppOption, USER_INFO } from '../typings/index'
import { ROLE_TYPE, LOGIN_STATUS } from './type/index'
// @ts-ignore
import NIM from './resources/sdk/NIM_Web_NIM_miniapp_v9.5.0.js'
import NECall from '@xkit-yx/call-kit'
import { logger } from './utils/logger'
import { dealMsg } from './utils/util'
// @ts-ignore
import WeAppRedux from './redux/index.js'
import createStore from './redux/createStore.js'
import reducer from './store/reducer.js'
import { mobxStore } from './store/mobx-store'
const { Provider } = WeAppRedux
const store = createStore(reducer) // redux store
// app.ts
App<IAppOption>(
  Provider(store)({
    globalData: {
      nim: null,
      neCall: null,
      hasAccepted: false, // 是否点击过接听，处理断网中点击后网络重连自动接听
      isConnected: true, // 网络状态
      currentRole: ROLE_TYPE.DOCTOR, // 当前体验角色
      loginStatus: LOGIN_STATUS.UNLOGIN, // 当前登录状态
      DoctorList: DoctorList,
      sessionList: [], // 会话列表
      userInfo: {
        accessToken: '',
        accountId: '',
        imAccid: '',
        imToken: '',
        avatar: '',
        nickname: '',
        mobile: '',
        gender: '',
      },
    },
    onLaunch() {
      this.initNIM()
    },

    onShow() {
      wx.onNetworkStatusChange((res: { isConnected: boolean }) => {
        if (this.globalData.isConnected && res.isConnected) {
          // 已经是联网状态且最新状态也是联网不操作
          return
        }
        // 网络重连成功
        if (res.isConnected) {
          this.globalData.nim.connect()
          // 断网中接听过，重连后需要自动接听
          logger.reportLog(this.globalData.hasAccepted, '首次断网点击接听')
          if (this.globalData.hasAccepted && this.globalData.neCall) {
            this.globalData.neCall
              .accept()
              .catch((err: any) => {
                logger.reportError(err, 'app 自动接听失败')
              })
              .then((res: any) => {
                logger.reportLog(res, 'app 自动接听成功')
                res && (this.globalData.hasAccepted = false)
              })
          }
        }
        this.globalData.isConnected = res.isConnected
        wx.showToast({
          title: res.isConnected ? '当前网络已恢复' : '当前网络已断开',
          icon: 'none',
          duration: 1500,
        })
      })
    },

    initNIM: function (_data?: USER_INFO) {
      this.resetStore()
      const userInfo = _data || wx.getStorageSync('yx-medicine-login-userInfo')
      logger.reportLog(_data, userInfo, 'app storage userInfo')
      // 已登录
      if (userInfo && userInfo.imAccid) {
        if (!_data) {
          this.globalData.loginStatus = LOGIN_STATUS.LOGINED
          this.globalData.userInfo = userInfo
          this.globalData.currentRole = userInfo.role || ROLE_TYPE.DOCTOR
        }
        store.dispatch({
          type: 'IM_OnMyInfo',
          payload: userInfo,
        })
        // @ts-ignore
        this.globalData.nim = NIM.getInstance({
          appKey,
          token: userInfo.imToken,
          account: userInfo.imAccid,
          syncRoamingMsgs: true,
          debugLevel: 'debug',
          promise: true,
          transports: ['websocket'],
          syncSessionUnread: true, // 同步未读数
          onconnect: () => {
            logger.reportLog(this.globalData.nim, 'onconnect success')
            this.globalData.loginStatus = LOGIN_STATUS.LOGINED
            if (this.globalData.neCall) {
              this.globalData.neCall.reconnect()
              // 断网中接听过，重连后需要自动接听
              logger.reportLog(this.globalData.hasAccepted, '断网点击接听')
              if (this.globalData.hasAccepted && this.globalData.neCall) {
                this.globalData.neCall
                  .accept()
                  .catch((err: any) => {
                    logger.reportError(err, 'IM重连后自动接听失败')
                    // wx.navigateBack()
                  })
                  .then((res: any) => {
                    logger.reportLog(res, 'IM重连后自动接听成功')
                    res && (this.globalData.hasAccepted = false)
                  })
              }
              return
            }
            // @ts-ignore
            this.globalData.neCall = new NECall({
              nim: this.globalData.nim,
              currentUserInfo: { accId: userInfo.imAccid },
              appKey,
              debug: true,
            })
            // rejectTimeout-超时挂断时间, callTimeout-超时取消
            this.globalData.neCall.setTimeout({
              callTimeout: 30000,
              rejectTimeout: 30000,
            })
            this.globalData.neCall.on('onReceiveInvited', (data: any) => {
              logger.reportLog('收到来电', data)
              const extraData = data.exraInfo && JSON.parse(data.exraInfo)
              wx.navigateTo({
                url: `/pages/call/index?enableCamera=${extraData.openVideo}&enableMic=${extraData.openAudio}`,
              })
            })
            // 随机生成姓名和性别
            if (_data) {
              const randomNameIndex = Math.ceil(Math.random() * 20)
              const randomNameInfo = randomNameList[randomNameIndex]
              logger.reportLog('本次随机生成的姓名：', randomNameInfo)
              userInfo.gender = randomNameInfo && randomNameInfo.gender
              userInfo.nickname = randomNameInfo && randomNameInfo.nickname
              this.globalData.nim.updateMyInfo({
                nick: userInfo.nickname,
                gender: userInfo.gender,
                avatar: userInfo.avatar,
              })
              // console.log(userInfo, 'selectRole userInfo')
              this.globalData.userInfo = userInfo
            }
            wx.setStorageSync('yx-medicine-login-userInfo', userInfo)
            // 往服务器存储角色信息，在发起呼叫前可根据此字段判断呼叫用户的角色
            this.globalData.nim.updateMyInfo({
              custom: JSON.stringify({
                role: this.globalData.currentRole,
              }),
            })
            store.dispatch({
              type: 'IM_OnMyInfo',
              payload: userInfo,
            })
          },
          onerror: (err: any) => {
            this.globalData.loginStatus = LOGIN_STATUS.UNLOGIN
            logger.reportError(err, 'app onerror')
          },
          onwillreconnect: (data: any) => {
            logger.reportLog(data, 'app onwillreconnect')
          },
          ondisconnect: (err: any) => {
            this.globalData.loginStatus = LOGIN_STATUS.UNLOGIN
            if (err.code === 'kicked') {
              setTimeout(() => {
                wx.showToast({
                  title: '不允许同一个帐号在多个地方同时登录',
                  icon: 'none',
                })
              }, 2000)
            }
            logger.reportLog(err, 'app ondisconnect')
            wx.removeStorageSync('yx-medicine-login-userInfo')
            wx.removeStorageSync('yx-medicine-callNumberHistory')
            wx.removeStorageSync('yx-medicine-chat-userInfoObj')
            this.globalData.neCall && this.globalData.neCall.destroy()
            this.globalData.neCall = null
            this.globalData.userInfo = {
              accessToken: '',
              accountId: '',
              imAccid: '',
              imToken: '',
              avatar: '',
              nickname: '',
              mobile: '',
              gender: '',
            }
            this.globalData.nim = null
            wx.reLaunch({
              url: '/pages/launch/index',
            })
          },
          onmsg: (msg: any) => {
            logger.reportLog('onMsg: 收到消息', msg)
            try {
              this.setStorageChatUserInfo(msg.from, msg.sessionId, () => {
                store.dispatch({
                  type: 'RawMessageList_Add_Msg',
                  payload: { msg, nim: this.globalData.nim },
                })
              })
            } catch (error) {
              console.error(error, 'error')
            }
          },
          // 会话
          onsessions: (sessions: any) => {
            console.log(sessions, '收到会话列表')
            // 获取会话对象的信息，用来展示头像昵称
            sessions.forEach((session: any) => {
              this.setStorageChatUserInfo(session.to, session.id, () => {
                store.dispatch({
                  type: 'SessionUnreadInfo_update',
                  payload: sessions,
                })
              })
            })
          },
          /**
           * 会话更新：收到消息、发送消息、设置当前会话、重置会话未读数 触发
           * {id:'p2p-zys2',lastMsg:{},scene,to,unread,updateTime}
           * {id:'team-1389946935',lastMsg:{attach:{accounts,team},type,users},scene,to,from,type,unread,updateTime}
           */
          onupdatesession: (session: any) => {
            console.log('onUpdateSession: ', session)
            try {
              store.dispatch({
                type: 'UnreadInfo_update',
                payload: session,
              })
              this.getUnreadCount()
            } catch (error: any) {
              // 上一次dispatch正在执行，稍等10ms后再更新
              if (error.message === 'Reducers may not dispatch actions.') {
                setTimeout(() => {
                  this.onUpdateSession(session)
                }, 10)
              }
            }
          },
          /**
           * 是否要忽略某消息，默认false
           * @returns true - 那么 SDK 将忽略此条消息（不计未读数，不当lastMsg，不存数据库，不触发onmsg通知）
           */
          shouldIgnoreMsg: (data: any) => {
            // console.log(data, 'shouldIgnoreMsg')
            // 过滤来自g2的消息，使得会话列表的未读数不展示此类消息
            return data.type === 'g2'
          },
          /**
           * 漫游消息：会多次收到，每次只会收到指定人的漫游消息
           */
          onroamingmsgs: (data: any) => {
            console.log(data, '漫游消息')
            this.setStorageChatUserInfo(data.to, data.sessionId, () => {
              this.setRoamingMsgList(data)
              // 获取未读消息数量
              setTimeout(() => {
                this.getUnreadCount()
              }, 500)
            })
          },
          onofflinemsgs: (msg: any) => {
            console.log(msg, 'onOfflineMsgs')
            store.dispatch({
              type: 'RawMessageList_Add_OfflineMessage',
              payload: msg,
            })
          },
          /**
           * 操作主体为对方
           * 收到系统通知，例如 被对方删除好友、被对方添加好友、被对方撤回消息
           * {type,to,time,deletedMsgTime,deletedMsgFromNick,deletedIdServer,deletedIdClient,status,scene,opeAccount,msg:{flow,from,fromNick,idClient,scene,sessionId,target,time,to,opeAccount},idServer,from}
           * time:为删除消息时间，deletedMsgTime为删除的消息发送时间
           */
          onsysmsg(msg: any) {
            console.log('onSysMsg: ', msg)
            dealMsg(msg, store, this)
          },
        })
        // 发送消息开始登陆
        store.dispatch({
          type: 'Login_StartLogin',
        })
      } else {
        // 未登录跳转到启动页
        //   wx.reLaunch({
        //     url: '/pages/launch/index',
        //   })
      }
    },

    // 计算消息未读数
    getUnreadCount() {
      const unreadInfo = store.getState().unreadInfo
      logger.reportLog('getUnreadCount', unreadInfo)
      const unreadArr = Object.keys(unreadInfo)
      let unreadCount = 0
      unreadArr.forEach((item: string) => {
        unreadCount += unreadInfo[item]
      })
      mobxStore.updateUnreadCount(unreadCount)
    },

    /**
     * 重置store数据
     */
    resetStore: function () {
      store.dispatch({
        type: 'Reset_All_State',
      })
    },

    // 设置本地会话列表
    setRoamingMsgList(data: any) {
      if (!data) return
      store.dispatch({
        type: 'RawMessageList_Add_RoamingMsgList',
        payload: data,
      })
    },

    setStorageChatUserInfo(
      account: string,
      sessionId: string,
      callback?: () => void
    ) {
      // 获取会话对象的信息，用来展示头像昵称
      this.globalData.nim.getUser({
        account,
        done: (error: any, user: any) => {
          const userInfoObj =
            wx.getStorageSync('yx-medicine-chat-userInfoObj') || {}
          userInfoObj[sessionId] = user
          wx.setStorageSync('yx-medicine-chat-userInfoObj', userInfoObj)
          callback?.()
        },
      })
    },

    // 返回当前tabbar应该渲染的菜单（不同页面不共享tabbar实例，需要实时更新）
    getTabbarList: function () {
      const isDoctor = this.globalData.currentRole === ROLE_TYPE.DOCTOR
      tabbarList.shift()
      if (isDoctor) {
        tabbarList.unshift(patientPage)
      } else {
        tabbarList.unshift(inquisitionPage)
      }
      return tabbarList
    },
  })
)
