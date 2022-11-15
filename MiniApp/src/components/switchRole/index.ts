import { ROLE_TYPE } from '../../type/index'
import { debounce } from '../../utils/util'
const app = getApp()
Component({
  /**
   * 组件的属性列表
   */
  properties: {
    onlyShowIcon: {
      type: Boolean,
    },
  },

  /**
   * 组件的初始数据
   */
  data: {
    roleName: '',
    buttons: [
      {
        text: '确定',
      },
    ],
    dialogShow: false,
    onlyShowIcon: false,
  },

  lifetimes: {
    // 在组件实例进入页面节点树时执行
    attached() {
      this.updateRoleName()
    },
  },

  /**
   * 组件的方法列表
   */
  methods: {
    updateRoleName() {
      const roleName =
        app?.globalData?.currentRole === ROLE_TYPE.DOCTOR ? '医生' : '患者'
      this.setData({
        roleName: '',
      })
      this.setData({
        roleName: roleName,
      })
    },
    switchRole: debounce(function () {
      const isDoctor = app?.globalData?.currentRole === ROLE_TYPE.DOCTOR
      wx.showModal({
        title: '身份切换',
        content: `是否将身份切换为${isDoctor ? '患者' : '医生'}？`,
        success: (res) => {
          if (res.confirm) {
            const userInfo =
              wx.getStorageSync('yx-medicine-login-userInfo') || {}
            if (isDoctor) {
              app.globalData.currentRole = ROLE_TYPE.PATIENT
              app.globalData.userInfo.role = ROLE_TYPE.PATIENT
              userInfo.role = ROLE_TYPE.PATIENT
            } else {
              app.globalData.currentRole = ROLE_TYPE.DOCTOR
              app.globalData.userInfo.role = ROLE_TYPE.DOCTOR
              userInfo.role = ROLE_TYPE.DOCTOR
            }
            console.log(app.globalData.nim, 'app.globalData.nim.')
            app.globalData.nim.updateMyInfo({
              custom: JSON.stringify({
                role: isDoctor ? ROLE_TYPE.PATIENT : ROLE_TYPE.DOCTOR,
              }),
            })
            console.log(
              '切换成功',
              isDoctor ? ROLE_TYPE.PATIENT : ROLE_TYPE.DOCTOR
            )
            wx.setStorageSync('yx-medicine-login-userInfo', userInfo)
            // @ts-ignore
            this.triggerEvent('hasSwitched', true)
          } else if (res.cancel) {
            console.log('取消切换角色')
          }
        },
      })
    }),
    handleShowTips() {
      this.setData({
        dialogShow: true,
      })
    },
    handleCloseDialog() {
      this.setData({
        dialogShow: false,
      })
    },
  },
})
