// pages/functions/index.ts
Page({
  /**
   * 页面的初始数据
   */
  data: {},

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 2,
        list: getApp().getTabbarList(),
      })
    }
  },

  handleClick(event: any) {
    const { key } = event.target.dataset
    if (key === 'voice-notification') {
      // 语音通知
      wx.navigateTo({
        url: '/pages/functionalExperience/voice-notice/index',
      })
    } else if (key === 'app-alive') {
      // 应用保活
      wx.navigateTo({
        url: '/pages/functionalExperience/app-alive-introduce/index',
      })
    } else if (key === 'phone-identification') {
      // 号码认证
      wx.navigateTo({
        url: '/pages/functionalExperience/phone-certification-introduce/index',
      })
    } else if (key === 'ID-certification') {
      // 实名认证
      wx.navigateTo({
        url: '/pages/functionalExperience/ID-certification/index',
      })
    } else {
      wx.showToast({
        title: '虚拟背景RTC SDK已支持，可联系商务经理获取',
        icon: 'none',
      })
    }
  },
})
