import { LOGIN_STATUS } from '../../type/index'
import { loginUrl, appKey } from '../../config/index'
const app = getApp()

Page({
  /**
   * 页面的初始数据
   */
  data: {
    LOGIN_STATUS: LOGIN_STATUS,
    id: '',
    login_status: LOGIN_STATUS.UNLOGIN,
  },

  login() {
    if (!appKey) {
      wx.showToast({
        title: '请配置您的appKey',
        icon: 'none',
        duration: 1500,
      })
      return
    }
    wx.navigateTo({
      url: '/pages/webview/index?url=' + loginUrl,
    })
  },
  onShow() {
    const userInfo = wx.getStorageSync('yx-medicine-login-userInfo') || {}
    console.log(userInfo, 'index-page onShow userInfo')
    // 已登录身份返回到首页，需要退出登录
    if (userInfo && userInfo.imAccid) {
      app.globalData.nim && app.globalData.nim.destroy()
    }
  },
})
