// @ts-ignore
const app = getApp()
Page({
  /**
   * 页面的初始数据
   */
  data: {
    isDetected: false, // 是否采集过信息
    count: 4,
    timer: 0,
    fromVideo: false, // 视频问诊页过来的需要返回到此页面
  },

  onLoad(options) {
    if (options.fromVideo == 'true') {
      this.setData({
        fromVideo: true,
      })
    }
  },

  leave() {
    setTimeout(() => {
      clearInterval(this.data.timer)
      const timer = setInterval(() => {
        if (this.data.count > 0) {
          this.setData({
            isDetected: true,
            count: this.data.count - 1,
          })
        }
        if (!this.data.isDetected) {
          this.setData({
            isDetected: true,
          })
        }
        if (this.data.count === 0) {
          clearInterval(this.data.timer)
          this.handleNavgateTo()
        }
      }, 1000)
      this.setData({
        timer,
      })
    }, 3000)
  },

  handleCameraError(e: any) {
    console.log(e, 'handleCameraError')
    this.handleOpenSetting()
  },

  handleCameraDone(e: any) {
    console.log(e, 'handleCameraDone')
    this.leave()
  },

  handleOpenSetting() {
    wx.showModal({
      title: '无法使用摄像头',
      content: '该功能需要摄像头，请允许小程序访问您的摄像头权限',
      confirmText: '前往设置',
      success: (res) => {
        if (res.confirm) {
          wx.openSetting({
            success: (res) => {
              console.log('成功', res.authSetting)
              if (res.authSetting['scope.camera']) {
                wx.showToast({
                  title: '权限申请成功，请重新打开当前页面',
                  icon: 'none',
                  duration: 1500,
                })
                this.handleNavgateTo()
              } else {
                this.cancelAuth()
              }
            },
            fail: (err) => {
              console.error('失败', err)
              this.cancelAuth()
            },
          })
        } else if (res.cancel) {
          console.log('用户放弃授权')
          this.cancelAuth()
        }
      },
    })
  },

  cancelAuth() {
    wx.showToast({
      title: '权限申请失败，暂无法使用',
      icon: 'none',
      duration: 1500,
    })
    setTimeout(() => {
      this.handleNavgateTo()
    }, 3000)
  },

  handleNavgateTo() {
    // @ts-ignore
    const currentRoute = getCurrentPages().pop().route
    console.log(currentRoute, 'currentRoute')
    if (currentRoute === 'pages/functionalExperience/face-detect/index') {
      if (this.data.fromVideo) {
        wx.redirectTo({
          url: '/pages/diagnose/index?diagnoseType=video',
        })
      } else {
        wx.navigateBack()
      }
    }
  },
})
