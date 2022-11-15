// @ts-ignore
import { connect } from '../../../redux/index.js'
import { calcTimeHeader } from '../../../utils/util.js'
const app = getApp()
const store = app.store
import { mobxStore } from '../../../store/mobx-store'

const startX = 0
const pageConfig = {
  /**
   * 页面的初始数据
   */
  data: {
    loginUserAccount: '',
    translateX: 0,
    chatList: [], // [{account,nick,lastestMsg,type,timestamp,displayTime,message,unread,status}]
    chatAccount: {}, // {accountName: accountName} 备注:消息通知key为notification
  },
  /**
   * 显示时排序
   */
  onShow() {
    // @ts-ignore
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      // @ts-ignore
      this.getTabBar().setData({
        selected: 1,
        list: getApp().getTabbarList(),
      })
    }
  },
  /**
   * 排序chatlist
   */
  sortChatList() {
    if (this.data.chatList.length !== 0) {
      const chatList = [...this.data.chatList]
      chatList.sort((a, b) => {
        // @ts-ignore
        return parseInt(b.timestamp) - parseInt(a.timestamp)
      })
      // @ts-ignore
      this.setData({
        chatList,
      })
    }
  },
  /**
   * 传递消息进来，添加至最近会话列表
   * 必须字段 {type, time, from,to}
   */
  addNotificationToChatList(msg: {
    type: string
    from: any
    content: string
    time: string
  }) {
    console.log(msg, 'addNotificationToChatList msg')
    let desc = ''
    // eslint-disable-next-line @typescript-eslint/no-this-alias
    const self = this
    switch (msg.type) {
      case 'addFriend': {
        desc = `添加好友-${msg.from}`
        break
      }
      case 'deleteFriend': {
        desc = `删除好友-${msg.from}`
        break
      }
      case 'deleteMsg':
        desc = `${msg.from}撤回了一条消息`
        break
      case 'custom':
        const data = JSON.parse(msg.content)
        const seen: any[] = []
        const str =
          data['content'] ||
          JSON.stringify(data, function (key, val) {
            if (typeof val == 'object') {
              if (seen.indexOf(val) >= 0) return
              seen.push(val)
            }
            return val
          }) // 可能没有content属性
        desc = `自定义系统通知-${str}`
        break
      default:
        desc = msg.type
        break
    }

    // @ts-ignore
    if (!self.data.chatAccount['notification']) {
      // 没有系统通知
      // @ts-ignore
      self.setData({
        chatList: [
          {
            account: '消息通知',
            timestamp: msg.time,
            displayTime: msg.time ? calcTimeHeader(msg.time) : '',
            lastestMsg: desc,
          },
          ...self.data.chatList,
        ],
        chatAccount: Object.assign({}, self.data.chatAccount, {
          notification: 'notification',
        }),
      })
    } else {
      const temp = [...self.data.chatList]
      temp.map((message, index) => {
        // @ts-ignore
        if (message.account === '消息通知') {
          // @ts-ignore
          temp[index].lastestMsg = desc
          // @ts-ignore
          temp[index].timestamp = msg.time
          // @ts-ignore
          temp[index].displayTime = msg.time ? calcTimeHeader(msg.time) : ''
          return
        }
      })
      // @ts-ignore
      temp.sort((a, b) => {
        // @ts-ignore
        return a.timestamp < b.timestamp
      })
      // @ts-ignore
      self.setData({
        chatList: temp,
      })
    }
  },
  /**
   * 捕获从滑动删除传递来的事件
   */
  catchDeleteNotification() {
    store.dispatch({
      type: 'Notification_Clear_All',
    })
  },
  /**
   * 捕获从滑动删除传递来的事件
   */
  catchDeleteTap(e: {
    currentTarget: { dataset: { session: string; account: string } }
  }) {
    console.log(e, 'catchDeleteTap event')
    wx.showActionSheet({
      itemList: ['确定删除'],
      itemColor: '#ff0000',
      success: (res) => {
        if (res.tapIndex === 0) {
          const session = e.currentTarget.dataset.session
          const chatAccount = Object.assign({}, this.data.chatAccount)
          // @ts-ignore
          delete chatAccount[session]
          const chatList = [...this.data.chatList]
          let deleteIndex = 0
          chatList.map((item: any, index) => {
            if (item.session === session) {
              deleteIndex = index
              return
            }
          })
          // 告知服务器，标记会话已读
          app.globalData.nim.resetSessionUnread(session)
          mobxStore.updateUnreadCount(0)
          chatList.splice(deleteIndex, 1)
          store.dispatch({
            type: 'Delete_All_MessageByAccount',
            payload: session,
          })
          // // 从服务器上删除漫游消息（本产品无需删除服务器数据）
          // app.globalData.nim.deleteRoamingMsgBySession({
          //   scene: 'p2p',
          //   to: account,
          //   done: (error: any, obj: any) => {
          //     console.log('删除会话' + (!error ? '成功' : '失败'), error, obj)
          //   },
          // })
          // @ts-ignore
          this.setData({
            chatList,
            chatAccount,
          })
        }
      },
    })
  },
  /**
   * 单击消息通知
   */
  switchToMessageNotification() {
    wx.navigateTo({
      url: '../../partials/messageNotification/messageNotification',
    })
  },
  /**
   * 单击进入聊天页面
   */
  switchToChating(e: {
    currentTarget: { dataset: { account: any; session: any } }
  }) {
    const account = e.currentTarget.dataset.account
    const session = e.currentTarget.dataset.session
    console.log(account, session, 'switchToChating')
    // 告知服务器，标记会话已读
    app.globalData.nim.resetSessionUnread(session)
    mobxStore.updateUnreadCount(0)
    // 更新会话对象
    app.store.dispatch({
      type: 'CurrentChatTo_Change',
      payload: session,
    })
    const userInfoObj = wx.getStorageSync('yx-medicine-chat-userInfoObj') || {}
    const fromAvatar = userInfoObj[session] && userInfoObj[session].avatar
    const fromNick = userInfoObj[session] && userInfoObj[session].nick
    app.globalData.nim.getHistoryMsgs({
      scene: 'p2p',
      to: account,
      done: function (error: any, obj: any) {
        console.log('获取会话' + (!error ? '成功' : '失败'), error, obj)
        obj.sessionId = 'p2p-' + obj.to
        app.setRoamingMsgList(obj)
        wx.navigateTo({
          url:
            '/pages/chating/chating?toAccid=' +
            account +
            '&fromAvatar=' +
            fromAvatar +
            '&chatNick=' +
            fromNick,
        })
      },
    })
  },
  /**
   * 单击进入个人区域
   */
  // switchToPersonCard(e) {
  //   let account = e.currentTarget.dataset.account
  //   console.log(account, 'account')
  //   if (account === 'ai-assistant') {
  //     return
  //   }
  //   // 重置该人的未读数
  //   // 重置某个会话的未读数,如果是已经存在的会话记录, 会将此会话未读数置为 0, 并会收到onupdatesession回调,而且此会话在收到消息之后依然会更新未读数
  //   app.globalData.nim.resetSessionUnread(`p2p-${account}`)
  // },
  /**
   * 判断消息类型，返回提示
   */
  judgeMessageType(rawMsg: { type?: any; pushContent?: any }) {
    rawMsg = rawMsg || {}
    let msgType = ''
    if (rawMsg.type === 'image') {
      msgType = '[图片]'
    } else if (rawMsg.type === 'geo') {
      msgType = '[位置]'
    } else if (rawMsg.type === 'audio') {
      msgType = '[语音]'
    } else if (rawMsg.type === 'video') {
      msgType = '[视频]'
    } else if (rawMsg.type === 'custom') {
      msgType = rawMsg.pushContent || '[自定义消息]'
    } else if (rawMsg.type === 'tip') {
      msgType = '[提醒消息]'
    } else if (rawMsg.type === 'deleteMsg') {
      //可能是他人撤回消息
      msgType = '[提醒消息]'
    } else if (rawMsg.type === 'file') {
      msgType = '[文件消息]'
    } else if (rawMsg.type === '白板消息') {
      msgType = '[白板消息]'
    } else if (rawMsg.type === '阅后即焚') {
      msgType = '[阅后即焚]'
    } else if (rawMsg.type === 'robot') {
      msgType = '[机器人消息]'
    } else if (rawMsg.type === 'notification') {
      msgType = '[通知消息]'
    }
    return msgType
  },
  /**
   * 将原生消息转化为最近会话列表渲染数据
   */
  convertRawMessageListToRenderChatList(
    // eslint-disable-next-line @typescript-eslint/ban-types
    rawMessageList: { [x: string]: { [x: string]: {} } },
    friendCard: { [x: string]: any },
    groupList: { [x: string]: any },
    unreadInfo: { [x: string]: any }
  ) {
    const chatList: {
      chatType: any
      session: string
      account: string
      status: any
      nick: any
      avatar: any
      lastestMsg: any
      type: any
      timestamp: any
      unread: any
      displayTime: string
    }[] = []
    const sessions = Object.keys(rawMessageList)
    const storageUserInfoList =
      wx.getStorageSync('yx-medicine-chat-userInfoObj') || {}
    sessions.map((session) => {
      const storageUser = storageUserInfoList[session] || {}
      const account =
        session.indexOf('team-') === 0
          ? session.slice(5, session.length)
          : session.slice(4, session.length)
      const isP2p = session.indexOf('p2p-') === 0
      const chatType = isP2p
        ? 'p2p'
        : groupList[account] && groupList[account].type
      const sessionCard =
        (isP2p ? friendCard[account] : groupList[account]) || {}
      const unixtimeList: any = Object.keys(rawMessageList[session])
      if (!unixtimeList) {
        return
      }
      const maxTime = Math.max(...unixtimeList)
      if (maxTime && account !== app.globalData.userInfo.accountId) {
        const msg: any = rawMessageList[session][maxTime + ''] || {}
        const msgType = this.judgeMessageType(msg)
        const lastestMsg = msgType
        const status = isP2p ? sessionCard.status || '离线' : ''
        const nick =
          storageUser.nick ||
          (isP2p ? sessionCard.nick || '非好友' : sessionCard.name)
        const avatar =
          storageUser.avatar ||
          (isP2p ? sessionCard.avatar : sessionCard.avatar)
        // TODO：此处存在逻辑漏洞，好友的时候发送消息，然后对方删除好友，再次登录后，会出错
        chatList.push({
          chatType,
          session,
          account,
          status,
          nick,
          avatar,
          lastestMsg: lastestMsg || msg.text,
          type: msgType || msg.type,
          timestamp: msg.time,
          unread: unreadInfo[session] || 0,
          displayTime: msg.time ? calcTimeHeader(msg.time) : '',
        })
      } else {
        console.log(
          account,
          app.globalData.userInfo.accountId,
          'app.globalData.userInfo.accountId'
        )
      }
    })
    // 排序
    chatList.sort((a, b) => {
      return b.timestamp - a.timestamp
    })
    return chatList
  },
  /**
   * 计算最近一条发送的通知消息列表
   */
  caculateLastestNotification(notificationList: {
    system: any[]
    custom: any[]
  }) {
    const temp = Object.assign({}, notificationList)
    let lastestDesc = ''
    const systemMaxIndex = null
    const customMaxIndex = null
    // 从大到小
    const system = notificationList.system.sort(
      (a: { msg: { time: number } }, b: { msg: { time: number } }) => {
        return b.msg.time - a.msg.time
      }
    )
    const custom = notificationList.custom.sort(
      (a: { msg: { time: number } }, b: { msg: { time: number } }) => {
        return b.msg.time - a.msg.time
      }
    )
    if (system[0]) {
      if (custom[0]) {
        lastestDesc =
          system[0].msg.time - custom[0].msg.time
            ? system[0].desc
            : custom[0].desc
      } else {
        lastestDesc = system[0].desc
      }
    } else {
      if (custom[0]) {
        lastestDesc = custom[0].desc
      }
    }
    return lastestDesc
  },
}
const mapStateToData = (state: any) => {
  const chatList = pageConfig.convertRawMessageListToRenderChatList(
    state.rawMessageList,
    state.friendCard,
    state.groupList,
    state.unreadInfo
  )
  const latestNotification = pageConfig.caculateLastestNotification(
    state.notificationList
  )
  console.log(chatList, 'chatList')
  return {
    rawMessageList: state.rawMessageList,
    userInfo: state.userInfo,
    friendCard: state.friendCard,
    groupList: state.groupList,
    unreadInfo: state.unreadInfo,
    chatList: chatList,
    latestNotification,
  }
}
const mapDispatchToPage = (dispatch: any) => ({})
const connectedPageConfig = connect(
  mapStateToData,
  mapDispatchToPage
)(pageConfig)
Page(connectedPageConfig)
