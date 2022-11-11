// pages/functionalExperience/voice-notice/index.ts
import { sendRequest } from '../../../utils/util'
import { baseDomain } from '../../../config/index'
// @ts-ignore
const app = getApp()
Page({
  /**
   * 页面的初始数据
   */
  data: {
    callNumber: '', // 被通知手机号
    callContent: '', // 语音内容
  },

  // 输入手机号
  handleMobileChange(e: any) {
    this.setData({
      callNumber: e.detail.value,
    })
  },

  // 输入内容
  handleContentChange(e: any) {
    this.setData({
      callContent: e.detail.value,
    })
  },

  clearCallContent() {
    this.setData({
      callContent: '',
    })
  },

  clearPhoneNumber() {
    this.setData({
      callNumber: '',
    })
  },

  resetUI() {
    this.setData({
      callNumber: '',
      callContent: '',
    })
  },

  // 校验手机号
  onVerify() {
    const reg =
      /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
    return this.data.callNumber.length == 11 && reg.test(this.data.callNumber)
  },

  onSend() {
    if (!app.globalData.isConnected) {
      wx.showToast({
        title: '网络开小差了，请稍后再试',
        icon: 'none',
      })
      return
    }
    if (!this.onVerify()) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none',
      })
      return
    }
    if (!this.data.callContent) {
      wx.showToast({
        title: '请输入通知内容',
        icon: 'none',
      })
      return
    }
    sendRequest({
      url: `${baseDomain}/voip/v1/voice/notice/txt`,
      method: 'POST',
      data: {
        mobile: this.data.callNumber,
        voiceTxt: this.data.callContent,
      },
    })
      .then((res: any) => {
        console.log(res, 'onCall res')
        this.resetUI()
      })
      .catch((err: any) => {
        console.error(err, 'onCall error')
        wx.showToast({
          title: err.msg,
          icon: 'none',
        })
        this.resetUI()
      })
  },
})
