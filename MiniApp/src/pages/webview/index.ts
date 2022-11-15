import { appKey } from '../../config/index'

Page({
  onLoad(options) {
    this.setData({
      url: options.url + '?appKey=' + appKey,
    })
  },
})
