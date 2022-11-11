Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 是否展示评分
    ifShowStar: {
      type: Boolean,
      default: false,
    },
  },

  /**
   * 组件的初始数据
   */
  data: {
    doctorList: [],
  },

  attached() {
    this.setData({
      doctorList: getApp().globalData.DoctorList,
    })
  },

  /**
   * 组件的方法列表
   */
  methods: {
    turnToDoctorHomepage(event: WechatMiniprogram.BaseEvent) {
      const {
        currentTarget: { dataset },
      } = event
      const url = `/pages/doctorHomepage/index?id=${dataset?.id}`
      wx.navigateTo({
        url: url,
      })
    },
  },
})
