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
        selected: 0,
        list: getApp().getTabbarList(),
      })
    }
  },
  makeAppointment() {
    wx.showToast({
      title: '仅展示，无实际功能',
      icon: 'none',
      duration: 2000,
    })
  },
  inquiryOnline() {
    wx.navigateTo({
      url: '/pages/inquiryOnline/index',
    })
  },

  handleSwitched() {
    wx.switchTab({
      url: '/pages/tabbar/doctor-index/index',
    })
  },
})
