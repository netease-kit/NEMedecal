// pages/call/index.ts
// @ts-ignore
const app = getApp()

Page({
  onUnload() {
    app.globalData.neCall && app.globalData.neCall.hangup()
  },
  onLoad(options: any) {
    Object.keys(options).length &&
      this.setData({
        localOptions: {
          enableCamera: options.enableCamera === 'true' ? true : false,
          enableMic: options.enableMic === 'true' ? true : false,
          enableVirtualBg: options.enableVirtualBg === 'true' ? true : false,
        },
      })
  },
  data: {
    localOptions: {
      enableCamera: true,
      enableMic: true,
      enableVirtualBg: true,
    },
  },
})
