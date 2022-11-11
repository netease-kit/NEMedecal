// @ts-ignore
const app = getApp()
Page({
  /**
   * 页面的初始数据
   */
  data: {
    IDNumber: '', // 身份证号码
    name: '', // 语音内容
    fromVideo: false,
  },

  onLoad(options) {
    if (options.fromVideo == 'true') {
      this.setData({
        fromVideo: true,
      })
    }
  },

  // 输入身份证
  handleIDChange(e: any) {
    this.setData({
      IDNumber: e.detail.value,
    })
  },

  // 输入姓名
  handleNameChange(e: any) {
    this.setData({
      name: e.detail.value,
    })
  },

  clearName() {
    this.setData({
      name: '',
    })
  },

  clearIDContent() {
    this.setData({
      IDNumber: '',
    })
  },

  resetUI() {
    this.setData({
      IDNumber: '',
      name: '',
    })
  },

  // 申请试用
  handleToRegist() {
    wx.previewImage({
      current:
        'https://yiyong.netease.im/yiyong-static/statics/medicine-demo/wx-qrcode.png',
      urls: [
        'https://yiyong.netease.im/yiyong-static/statics/medicine-demo/wx-qrcode.png',
      ], // 需要预览的图片 http 链接列表
    })
  },

  onSend() {
    if (!app.globalData.isConnected) {
      wx.showToast({
        title: '网络开小差了，请稍后再试',
        icon: 'none',
      })
      return
    }

    if (!this.data.name) {
      wx.showToast({
        title: '请输入姓名',
        icon: 'none',
      })
      return
    }

    if (
      !this.data.IDNumber ||
      (this.data.IDNumber && this.data.IDNumber.length !== 18)
    ) {
      wx.showToast({
        title: '请输入正确的身份证号码',
        icon: 'none',
      })
      return
    }

    const url =
      '/pages/functionalExperience/face-detect/index?fromVideo=' +
      this.data.fromVideo
    if (this.data.fromVideo) {
      wx.redirectTo({
        url,
      })
    } else {
      wx.navigateTo({
        url,
      })
    }
  },
})
