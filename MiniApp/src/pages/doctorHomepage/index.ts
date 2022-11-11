const patientsEvaluation = [
  {
    name: '张*伟',
    photo: 'zhangsan.png',
    comment_date: '2022.05.21',
    comment:
      '大夫耐心专业，讲解了我的每一个疑问，给的建议也非常能帮助我进一步了解家人病情，真希望这样的医生多一些。',
  },
  {
    name: '钱*多',
    photo: 'lisi.png',
    comment_date: '2022.03.19',
    comment: '医生特别耐心，回复很及时，开的药楼下药店都买的到。',
  },
  {
    name: '刘**',
    photo: 'chenren.png',
    comment_date: '2022.02.08',
    comment:
      '在手机上提问很方便，直接把病历、报告拍给医生看就行，感谢医生耐心的回复。',
  },
]
Page({
  /**
   * 页面的初始数据
   */
  data: {
    doctorId: 0,
    doctorInfo: {},
    patientsEvaluation: patientsEvaluation,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(option) {
    this.data.doctorId = Number(option.id || 1)
    const doctorInfo = getApp().globalData.DoctorList.find(
      (item: any) => item.id == option.id
    )
    this.setData({
      doctorInfo: doctorInfo,
    })
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    wx.setNavigationBarTitle({
      title: '医生主页',
    })
  },

  // 发起问诊
  handleToDiagnose(event: WechatMiniprogram.BaseEvent) {
    const type = event.currentTarget.dataset.type
    if (type === 'video') {
      wx.showModal({
        title: '实名认证',
        content: '线上问诊要求实名认证，网易提供认证能力，是否体验',
        confirmText: '体验',
        cancelText: '暂不体验',
        success: (res) => {
          if (res.confirm) {
            // 确定
            wx.navigateTo({
              url: '/pages/functionalExperience/ID-certification/index?fromVideo=true',
            })
          } else {
            wx.navigateTo({
              url: '/pages/diagnose/index?diagnoseType=video',
            })
          }
        },
      })
    } else {
      wx.navigateTo({
        url: '/pages/diagnose/index?diagnoseType=' + type,
      })
    }
  },
})
