import { ROLE_TYPE } from '../../type/index'
const app = getApp()

Page({
  /**
   * 页面的初始数据
   */
  data: {
    ROLE_TYPE: ROLE_TYPE,
    currentRole: ROLE_TYPE.DOCTOR,
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 已登录直接跳转
    if (app.globalData.userInfo.accountId && app.globalData.userInfo.role) {
      this.setData({
        currentRole: app.globalData.userInfo.role,
      })
      this.handleToSwitchPage(app.globalData.userInfo.role)
      return
    }
    app.globalData.currentRole = this.data.currentRole
  },

  onLoad(options) {
    console.log(options, '登录页参数')
    if (options.accountId && options.accessToken) {
      const {
        avatar,
        accountId,
        accessToken,
        imAccid,
        imToken,
        nickname,
        mobile,
      } = options
      const userInfo = {
        accountId,
        accessToken: decodeURIComponent(accessToken),
        avatar,
        imAccid,
        imToken,
        nickname,
        gender: '1',
        mobile,
      }
      app.initNIM(userInfo)
    }
  },

  selectRole(event: WechatMiniprogram.BaseEvent) {
    const {
      currentTarget: { dataset },
    } = event
    this.setData({
      currentRole: Number(dataset?.role),
    })
    app.globalData.currentRole = dataset?.role
  },

  confirmRole() {
    this.setData({
      'userInfo.role': this.data.currentRole,
    })
    this.handleToSwitchPage(this.data.currentRole)
  },

  handleToSwitchPage(role: number) {
    // todo: 向im发送请求修改当前用户的角色信息

    if (!app.globalData.nim) {
      console.warn('nim 未初始化')
      return
    }
    const userInfo = app.globalData.userInfo
    userInfo.role = role
    wx.setStorageSync('yx-medicine-login-userInfo', userInfo)
    app.globalData.userInfo.role = role
    app.globalData.currentRole = role
    app.globalData.nim &&
      app.globalData.nim.updateMyInfo({
        custom: JSON.stringify({
          role: role,
        }),
      })
    if (role === ROLE_TYPE.DOCTOR) {
      wx.switchTab({
        url: '/pages/tabbar/doctor-index/index',
      })
    } else {
      wx.switchTab({
        url: '/pages/tabbar/patient-index/index',
      })
    }
  },
})
