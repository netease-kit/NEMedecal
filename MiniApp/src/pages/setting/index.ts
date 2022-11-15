// pages/setting/index.ts
Page({
  handleToAgreement() {
    wx.navigateTo({
      url: '/pages/webview/index?url=https://yiyong.netease.im/yiyong-static/statics/medicine-demo/service/index.html#/',
    })
  },

  handleToPrivacy() {
    wx.navigateTo({
      url: '/pages/webview/index?url=https://yiyong.netease.im/yiyong-static/statics/medicine-demo/privacy/index.html#/',
    })
  },
})
