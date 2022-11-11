// pages/diagnose/diagnose.ts
import { sendRequest } from '../../utils/util'
import { baseDomain } from '../../config/index'
import { ROLE_TYPE, LOGIN_STATUS } from '../../type/index'
// @ts-ignore
const app = getApp()

Page({
  /**
   * 页面的初始数据
   */
  data: {
    callNumber: '',
    currentRole: app.globalData.currentRole,
    showErrorTips: false,
    diagnoseType: 'video', // 会诊类型 audio|video
    callNumberHistory: [] as string[],
    enableCamera: true,
    enableMic: true,
    enableVirtualBg: true,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onShow() {
    const callNumberList =
      wx.getStorageSync('yx-medicine-callNumberHistory') || []
    this.setData({
      callNumberHistory: callNumberList,
      currentRole: app.globalData.currentRole,
    })
  },
  onLoad(options) {
    this.setData({
      diagnoseType: options.diagnoseType,
    })
    wx.setNavigationBarTitle({
      title:
        options.diagnoseType === 'video'
          ? '视频问诊'
          : options.diagnoseType === 'message'
          ? '图文问诊'
          : '语音问诊',
    })
  },

  // 输入手机号
  handleMobileChange(e: any) {
    this.setData({
      callNumber: e.detail.value,
    })
  },
  changeHandler(e: any) {
    const key = e.currentTarget.dataset.key
    if (this.data.showErrorTips) {
      this.setData({
        showErrorTips: false,
      })
    }
    this.setData({
      [key]: e.detail.value,
    })
  },

  clearPhoneNumber() {
    this.setData({
      callNumber: '',
      showErrorTips: false,
    })
  },

  // 校验手机号
  onVerify() {
    const reg =
      /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
    if (this.data.callNumber.length == 11 && reg.test(this.data.callNumber)) {
      return true
    } else {
      this.setData({
        showErrorTips: true,
      })
      return false
    }
  },

  // 发起问诊
  handleDiagnose() {
    if (!app.globalData.isConnected) {
      wx.showToast({
        title: '网络开小差了，请稍后再试',
        icon: 'none',
      })
      return
    }
    if (!this.onVerify()) {
      this.setData({
        showErrorTips: true,
      })
      return
    }
    sendRequest({
      url: `${baseDomain}/p2pVideoCall/caller/v2/searchSubscriber`,
      method: 'POST',
      data: {
        mobile: this.data.callNumber,
      },
    })
      .then((res: any) => {
        console.log(res, 'handleDiagnose res')
        if (app.globalData.userInfo.accountId === res.imAccid) {
          wx.showToast({
            title:
              this.data.diagnoseType === 'message'
                ? '不能与自己聊天'
                : '不能呼叫自己',
            icon: 'none',
          })
          return
        }
        app.globalData.nim.getHistoryMsgs({
          scene: 'p2p',
          to: res.imAccid,
          done: function (error: any, obj: any) {
            console.log('获取会话' + (!error ? '成功' : '失败'), error, obj)
            obj.sessionId = 'p2p-' + obj.to
            app.setRoamingMsgList(obj)
          },
        })
        app.globalData.nim.getUser({
          account: res.imAccid,
          done: (err: any, user: any) => {
            console.log('呼叫的用户信息：', user)
            const userInfoObj =
              wx.getStorageSync('yx-medicine-chat-userInfoObj') || {}
            userInfoObj['p2p-' + user.account] = user
            wx.setStorageSync('yx-medicine-chat-userInfoObj', userInfoObj)
            if (user) {
              const role = user.custom && JSON.parse(user.custom).role
              if (role === app.globalData.currentRole) {
                wx.showToast({
                  title: `请将该手机号身份切换为${
                    role === ROLE_TYPE.DOCTOR ? '患者' : '医生'
                  }后重试`,
                  icon: 'none',
                })
                return
              } else {
                // 图文问诊
                if (this.data.diagnoseType === 'message') {
                  // 更新会话对象
                  const sessionId = 'p2p-' + res.imAccid
                  app.store.dispatch({
                    type: 'CurrentChatTo_Change',
                    payload: sessionId,
                  })
                  wx.navigateTo({
                    url:
                      '/pages/chating/chating?toAccid=' +
                      res.imAccid +
                      '&chatNick=' +
                      user.nick +
                      '&fromAvatar=' +
                      user.avatar,
                  })
                  return
                }
                // 音视频问诊
                const diagnoseType =
                  this.data.diagnoseType === 'video' ? '2' : '1'

                const extraData = {
                  caller_userName: app.globalData.userInfo.nickname,
                  caller_userRole: app.globalData.userInfo.role,
                  called_userRole: role,
                  called_userName: user.nick,
                  called_userMobile: user.mobile,
                  openAudio: this.data.enableMic,
                  openVideo: this.data.enableCamera,
                }
                console.log(extraData, 'extraData')
                app.globalData.neCall
                  .call({
                    callType: diagnoseType,
                    accId: res.imAccid,
                    extraInfo: JSON.stringify(extraData),
                  })
                  .then(() => {
                    const { enableCamera, enableMic } = this.data
                    wx.navigateTo({
                      url: `/pages/call/index?enableCamera=${enableCamera}&enableMic=${enableMic}`,
                    })
                      .catch((err) => {
                        console.error(err, 'call error')
                      })
                      .catch((err) => {
                        console.error(err, 'call error')
                      })
                  })
              }
            }
            if (err) {
              app.globalData.nim.connect()
              wx.showToast({
                title: '系统开小差了，请稍后再试',
                icon: 'none',
              })
              console.error(err, 'getUser err')
            }
          },
        })

        this.setCallHistory()
      })
      .catch((err) => {
        console.error(err, 'handleDiagnose error')
        wx.showToast({
          title: err.code === 500 ? '该手机号未注册' : err.msg,
          icon: 'none',
        })
        this.setCallHistory()
      })
  },

  setCallHistory() {
    let callNumberList = this.data.callNumberHistory
    if (
      this.data.callNumber &&
      !callNumberList.includes(this.data.callNumber)
    ) {
      callNumberList.unshift(this.data.callNumber)
      if (callNumberList.length > 2) {
        callNumberList = callNumberList.splice(0, 2)
      }
      this.setData({
        callNumberHistory: callNumberList,
      })
      wx.setStorageSync('yx-medicine-callNumberHistory', callNumberList)
    }
    this.setData({
      callNumber: '',
    })
  },

  clearHistoryItem(e: any) {
    const delNum = e.target.dataset.item
    const callNumberList = [...this.data.callNumberHistory]
    const delIndex = callNumberList.findIndex((item) => item === delNum)
    callNumberList.splice(delIndex, 1)
    this.setData({
      callNumberHistory: callNumberList,
    })
    wx.setStorageSync('yx-medicine-callNumberHistory', callNumberList)
  },

  // 历史记录填写到输入框
  historyFillCallNumber(e: any) {
    this.setData({
      callNumber: e.target.dataset?.number || '',
      showErrorTips: false,
    })
  },
})
