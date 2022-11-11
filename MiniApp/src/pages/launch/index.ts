// pages/launch/index.ts
// @ts-ignore
const app = getApp()

Page({
  /**
   * 页面的初始数据
   */
  data: {
    timer: 0,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad() {
    // @ts-ignore
    const currentRoute = getCurrentPages().pop().route
    console.log(currentRoute, 'currentRoute')
    clearTimeout(this.data.timer)
    this.data.timer = setTimeout(() => {
      if (
        currentRoute === 'pages/launch/index' &&
        !app.globalData.userInfo.imToken
      ) {
        wx.redirectTo({
          url: '/pages/index/index',
        })
      } else {
        // 已登录跳转对应的主页
        if (app.globalData.userInfo.role === 2) {
          wx.switchTab({
            url: '/pages/tabbar/patient-index/index',
          })
        } else {
          wx.switchTab({
            url: '/pages/tabbar/doctor-index/index',
          })
        }
      }
    }, 1500)
  },
})
