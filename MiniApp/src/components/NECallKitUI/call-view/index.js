import { secondToDate, toast } from '../utils/index'
import { logger } from '../utils/logger'
const app = getApp()

Component({
  properties: {
    userAccId: String,
    options: {
      type: Object,
    },
  },
  data: {
    callStatus: 0, // 状态 0：闲置 1：正在呼叫 2：正在被呼叫 3：通话中 4: 已接通但对方可能未加入
    callType: '1', // 通话类型 1：语音通话 2：视频通话
    durationText: '00:00', // 通话时长
    userInfo: {},
    microphoneImg: {
      open: '../assets/microphone-open.png',
      close: '../assets/microphone-close.png',
    },
    videoImg: {
      open: '../assets/video-open.png',
      close: '../assets/video-close.png',
    },
    acceptImg: {
      audio: '../assets/audio-accept.png',
      video: '../assets/video-accept.png',
    },
    switchImg: {
      audio: '../assets/switch-audio.png',
      video: '../assets/switch-video.png',
    },
    hangupImg: '../assets/hangup.png',
    cameraRevertImg: '../assets/camera-revert.png',
    virtualBgImg: '../assets/virtual-bg.png',
    pusher: {
      mode: 'RTC',
      autopush: true,
      enableCamera: true,
      enableMic: true,
      enableVirtualBg: false, // 虚拟背景
      isFullScreen: false, // 全屏
      waitingImage:
        'https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Fgroupcall%2FLark20210401-161321.jpeg',
    },
    player: {
      mode: 'RTC',
      autoplay: true,
      objectFit: 'fillCrop',
      videoMute: false,
      isFullScreen: true, // 全屏
      playerContext: {},
    },
    roleAvatarImg: '',
    roleAvatarImgList: {
      doctor: '../assets/doctor-avatar.png',
      patient: '../assets/patient-avatar.png',
    },
  },
  lifetimes: {
    ready: function () {
      // 在组件实例刚刚被创建时执行
      logger.reportLog(this.properties.options, 'options')
      const { enableCamera, enableMic, enableVirtualBg } =
        this.properties.options
      this.setData({
        'pusher.enableCamera': enableCamera,
        'pusher.enableMic': enableMic,
        'pusher.enableVirtualBg': enableVirtualBg,
        'player.enableCamera': enableCamera,
        'player.enableMic': enableMic,
      })
      setInterval(() => {
        wx.setKeepScreenOn({
          keepScreenOn: true,
        })
      }, 20000)
    },
    attached() {
      const neCall = app.globalData.neCall
      const nim = app.globalData.nim
      const roleAvatarImgList = this.data.roleAvatarImgList
      this.setData({
        roleAvatarImg:
          roleAvatarImgList[
            app.globalData.currentRole === 1 ? 'patient' : 'doctor'
          ],
      })
      if (!neCall || !nim) {
        throw new Error(
          '需要在全局实例化 neCall 和 nim，绑定到 app.globalData 上'
        )
      }
      const callStatus = neCall.signalController.callStatus
      let userAccId =
        callStatus === 1
          ? neCall.signalController._channelInfo &&
            neCall.signalController._channelInfo.calleeId
          : neCall.signalController._channelInfo &&
            neCall.signalController._channelInfo.callerId
      console.log(callStatus, userAccId, 'callStatus')

      app.globalData.nim.getUser({
        account: userAccId,
        done: (error, user) => {
          logger.reportLog(user, 'getUser')
          user.role = user.custom && JSON.parse(user.custom).role
          if (!error && user) {
            this.setData({
              userInfo: user,
              'userInfo.nick':
                user.role === 1 ? user.nick.substr(0, 1) + '医生' : user.nick,
            })
          }
        },
      })
      this.setData({
        callStatus: neCall.signalController.callStatus,
        callType: neCall._callType,
      })
      this.durationTimer = null
      let duration = 0
      neCall.on('onCallConnected', () => {
        console.warn('onCallConnected')
        this.setData({
          callStatus: 3,
        })
        this.durationTimer && clearInterval(this.durationTimer)
        this.durationTimer = setInterval(() => {
          duration += 1
          this.setData({
            durationText: secondToDate(duration),
          })
          // 通话四分钟时提醒用户
          if (duration === 240) {
            wx.showToast({
              title: '本次询问还剩一分钟',
              icon: 'none',
              duration: 5000, // 微信bug，设置5s不会消失
            })
          }
          if (duration === 300) {
            this.onHangup()
          }
        }, 1000)
      })
      neCall.on('onSwtichCallType', (value) => {
        const callType = value.callType
        if (value.state === 1) {
          const content =
            callType === '1'
              ? '对方请求将视频转为音频，将直接关闭您的摄像头'
              : '对方请求将转音频为视频，需要打开您的摄像头'
          wx.showModal({
            title: '权限请求',
            content,
            cancelText: '拒绝',
            confirmText: '同意',
            success(res) {
              if (res.confirm) {
                neCall.switchCallType({ callType, state: 2 })
              } else if (res.cancel) {
                neCall.switchCallType({ callType, state: 3 })
              }
            },
          })
        }
        if (value.state === 2) {
          this.setData({
            callType: value.callType,
            pusher: {
              ...this.data.pusher,
              enableCamera: value.callType === '2',
            },
            pusher: { ...this.data.pusher, enableMic: true },
          })
        }
        if (value.state === 3) {
        }
      })
      neCall.on('onVideoMuteOrUnmute', (mute) => {
        this.setData({
          player: { ...this.data.player, videoMute: mute },
        })
      })
      neCall.on('onStreamPublish', (url) => {
        this.setData({
          pusher: { ...this.data.pusher, url },
        })
      })
      neCall.on('onStreamSubscribed', (url) => {
        logger.reportLog('onStreamSubscribed', url)
        this.setData({
          player: { ...this.data.player, url },
        })
      })
      /**
       * reason - 0：正常流程 | 1：token请求失败 | 2：呼叫超时 | 3：用户占线 | 4：nertc 初始化失败 | 5：加入 rtc 房间失败 | 6：cancel 取消参数错误 | 7：发起呼叫失败 | 8：对方rtc失败 | 9：其他端接受邀请 | 10：其他端拒绝邀请 | 11：通话中rtc 异常
       * exReason?: number 附加原因 0：取消 1：被取消 2：拒绝 3：被拒绝 4：挂断 5：被挂断
       **/
      neCall.on('onCallEnd', (data) => {
        logger.reportLog('onCallEnd', data)
        duration = 0
        this.durationTimer && clearInterval(this.durationTimer)
        this.setData({
          player: this.data.player,
          pusher: this.data.pusher,
        })
        switch (data.reason) {
          case 0:
            let endMessage = '通话已结束'
            if (data.exReason === 1) {
              endMessage = '对方已取消连接'
            } else if (data.exReason === 3) {
              endMessage = '对方拒绝了你的请求'
            } else if (data.exReason === 5) {
              endMessage = '对方已结束通话'
            }
            toast(endMessage)
            break
          case 1:
            toast('token请求失败')
            break
          case 2:
            toast('呼叫超时')
            break
          case 3:
            toast('用户占线')
            break
          case 4:
            toast('初始化失败')
            break
          case 5:
            toast('加入 rtc 房间失败')
            break
          case 6:
            toast('取消参数错误')
            break
          case 7:
            toast('发起呼叫失败')
            break
          case 8:
            toast('对方加入 rtc 房间失败')
            break
          case 9:
            toast('已在其他端接受邀请')
            break
          case 10:
            toast('已在其他端拒绝邀请')
            break
          case 11:
            toast('通话中rtc 异常')
            break
          default:
            toast('通话已结束')
            break
        }
        setTimeout(() => {
          // @ts-ignore
          const currentRoute = getCurrentPages().pop().route
          console.log(currentRoute, 'currentRoute')
          if (currentRoute === 'pages/call/index') {
            wx.navigateBack()
          }
        }, 1500)
      })
    },
    detached() {
      const neCall = app.globalData.neCall
      neCall.off('onCallConnected')
      neCall.off('onSwtichCallType')
      neCall.off('onStreamPublish')
      neCall.off('onStreamSubscribed')
      neCall.off('onCallEnd')
      app.globalData.hasAccepted = false
      this.durationTimer && clearInterval(this.durationTimer)
      this.setData({
        callStatus: 0,
        durationText: '00:00', // 通话时长
        userInfo: {},
      })
    },
  },
  methods: {
    onHangup() {
      logger.reportLog('onHangup')
      const neCall = app.globalData.neCall
      neCall.hangup()
      // 断网挂断需要马上回退
      // @ts-ignore
      const currentRoute = getCurrentPages().pop().route
      console.log(currentRoute, 'currentRoute')
      if (currentRoute === 'pages/call/index') {
        wx.navigateBack()
      }
    },
    onAccept() {
      if (!app.globalData.isConnected) {
        app.globalData.hasAccepted = true
        wx.showToast({
          title: '网络开小差了，请稍后再试',
          icon: 'none',
        })
        return
      }
      logger.reportLog('onAccept', app.globalData.hasAccepted)
      const neCall = app.globalData.neCall
      neCall
        .accept()
        .catch((err) => {
          logger.reportError(err, 'onAccept err')
          wx.showToast({
            title: '系统开小差了，请稍后再试',
            icon: 'none',
          })
        })
        .then((res) => {
          // 主叫断网时，可以成功接听加入rtc房间
          logger.reportLog(res, 'onAccept success')
          if (res && this.data.callStatus !== 3) {
            this.setData({
              callStatus: 4,
            })
            app.globalData.hasAccepted = false
          }
        })
    },
    handleEnableLocalAudio() {
      const neCall = app.globalData.neCall
      const enable = this.data.pusher.enableMic
      neCall.enableLocalAudio(!enable)
      this.setData({
        pusher: { ...this.data.pusher, enableMic: !enable },
      })
    },
    handleEnableLocalVideo() {
      const neCall = app.globalData.neCall
      const enable = this.data.pusher.enableCamera
      neCall.enableLocalVideo(!enable)
      this.setData({
        pusher: { ...this.data.pusher, enableCamera: !enable },
      })
    },
    handleSwitchCallType() {
      const neCall = app.globalData.neCall
      const callType = this.data.callType === '1' ? '2' : '1'
      neCall.switchCallType({ callType, state: 1 })
    },
    handleCameraRevert() {
      const livePusherContext = wx.createLivePusherContext()
      livePusherContext.switchCamera()
    },

    // 虚拟背景
    handleShowVirtualBg() {
      wx.showToast({
        icon: 'none',
        title: '虚拟背景RTC SDK已支持，可联系商务经理获取',
      })
    },

    _toggleFullScreen() {
      if (this.data.pusher.isFullScreen) {
        // 本端缩放时，对端要放大
        this.setData({
          'pusher.isFullScreen': false,
          'player.isFullScreen': true,
        })
      } else {
        this.setData({
          'pusher.isFullScreen': true,
          'player.isFullScreen': false,
        })
      }
    },
    _pusherErrorHandler(event) {
      logger.reportLog('_pusherErrorHandler', event)
      // 未开启摄像头或者麦克风权限
      if (event.detail.errCode === 10001 || event.detail.errCode === 10002) {
        // 后于温馨提示弹窗显示
        setTimeout(() => {
          this.handleOpenSetting()
        }, 2000)
      }
    },
    _playerStateChange(event) {
      const { code, message } = event.detail
      switch (code) {
        case -2301:
          logger.reportError('获取拉流失败', message, code)
          const playerContext = wx.createLivePlayerContext('player', this)
          playerContext.stop({
            success: () => {
              playerContext.play({
                success: (res) => {
                  console.warn(res, '重新播放流 success')
                },
                fail: (err) => {
                  console.error(err, '重新播放流 fail')
                },
              })
            },
            fail: (err) => {
              console.error(err, 'stop fail')
            },
          })
          this.setData({
            'player.playerContext': playerContext,
          })
          break
        default:
          logger.reportLog('_playerStateChange', message, code)
          break
      }
    },
    handleOpenSetting() {
      wx.showModal({
        title: '无法使用摄像头和麦克风',
        content: '该功能需要摄像头，请允许小程序访问您的摄像头和麦克风权限',
        confirmText: '前往设置',
        success(res) {
          if (res.confirm) {
            wx.openSetting({
              success(res) {
                logger.reportLog('成功', res.authSetting)
                wx.navigateBack()
              },
              fail(err) {
                logger.reportError('失败', err)
              },
            })
          } else if (res.cancel) {
            logger.reportLog('用户放弃授权')
          }
        },
      })
    },
  },
})
