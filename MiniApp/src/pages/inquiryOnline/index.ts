// pages/inquiryOnline/index.ts
Page({
  /**
   * 页面的初始数据
   */
  data: {
    currentId: '1',
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    wx.setNavigationBarTitle({
      title: '在线问诊',
    })
  },

  clickBtn(event: WechatMiniprogram.BaseEvent) {
    const {
      currentTarget: { dataset },
    } = event
    if (dataset?.id !== 1) {
      wx.showToast({
        title: '仅展示，无实际功能',
        icon: 'none',
      })
      return
    }
    this.setData({
      currentId: dataset?.id,
    })
  },
})
