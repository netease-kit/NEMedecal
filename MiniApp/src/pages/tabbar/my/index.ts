// pages/my/index.ts
// @ts-ignore
const app = getApp()

Page({
  /**
   * 生命周期函数--监听页面加载
   */
  onShow() {
    const userInfo = app.globalData.userInfo
    this.setData({ userInfo })
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 3,
        list: getApp().getTabbarList(),
      })
    }
    this.setData({
      role: app.globalData.currentRole,
    })
  },

  data: {
    role: 1, // 1：医生 2：患者
  },

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '你确认要退出登录？',
      success(res) {
        if (res.confirm) {
          app.globalData.nim.destroy()
        }
      },
    })
  },

  handleClick(event: any) {
    const { key } = event.target.dataset
    if (key === 'test') {
      wx.previewImage({
        current:
          'https://yiyong.netease.im/yiyong-static/statics/medicine-demo/wx-qrcode.png',
        urls: [
          'https://yiyong.netease.im/yiyong-static/statics/medicine-demo/wx-qrcode.png',
        ], // 需要预览的图片 http 链接列表
      })
    } else if (key === 'setting') {
      wx.navigateTo({
        url: '/pages/setting/index',
      })
    } else {
      wx.showToast({
        title: '仅展示，无实际功能',
        icon: 'none',
      })
    }
  },

  handleSwitched() {
    this.onShow()
  },
})
