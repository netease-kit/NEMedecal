// pages/patients/index.ts
import { PatientList } from '../../../config/index'

Page({
  /**
   * 页面的初始数据
   */
  data: {
    patientList: PatientList,
  },

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

  handleToDiagnose(e: any) {
    const type = e.currentTarget.dataset.type
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

  handleSwitched() {
    wx.switchTab({
      url: '/pages/tabbar/patient-index/index',
    })
  },
})
