/* eslint-disable @typescript-eslint/no-this-alias */
// @ts-ignore
import { connect } from '../../redux/index.js'
import {
  generateBigEmojiImageFile,
  generateImageNode,
  calcTimeHeader,
} from '../../utils/util.js'
import { showToast, generateRichTextNode } from '../../utils/util.js'
import * as iconBase64Map from '../../utils/imageBase64.js'

const app = getApp()
const store = app.store
const pageConfig = {
  data: {
    videoContext: null, // 视频操纵对象
    isVideoFullScreen: false, // 视频全屏控制标准
    videoSrc: '', // 视频源
    recorderManager: null, // 微信录音管理对象
    recordClicked: false, // 判断手指是否触摸录音按钮
    iconBase64Map: {}, //发送栏base64图标集合
    isLongPress: false, // 录音按钮是否正在长按
    chatWrapperMaxHeight: 0, // 聊天界面最大高度
    chatTo: '', //聊天对象account
    chatType: '', //聊天类型 advanced 高级群聊 normal 讨论组群聊 p2p 点对点聊天
    loginAccountLogo: '', // 登录账户对象头像
    focusFlag: false, //控制输入框失去焦点与否
    emojiFlag: false, //emoji键盘标志位
    moreFlag: false, // 更多功能标志
    tipFlag: false, // tip消息标志
    tipInputValue: '', // tip消息文本框内容
    sendType: 0, //发送消息类型，0 文本 1 语音
    messageArr: [], //[{text, time, sendOrReceive: 'send', displayTimeHeader, nodes: []},{type: 'geo',geo: {lat,lng,title}}]
    inputValue: '', //文本框输入内容
    from: '',
    accountId: '', // 私聊对象
    dialogShow: false,
    replayNick: '', // 回复的昵称
    replyMsg: {}, // 回复的消息对象
    fromAvatar: '', //对方头像
    fromNick: '', //对方昵称
    buttons: [
      {
        text: '关闭',
      },
    ],
    role: 2,
  },
  onUnload() {
    // 更新当前会话对象账户
    store.dispatch({
      type: 'CurrentChatTo_Change',
      payload: '',
    })
  },
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options: {
    toAccid: any
    chatTo: any
    type: string
    from: string
    fromAvatar: string
    chatNick: string
  }) {
    console.log(options, 'options')
    const chatWrapperMaxHeight = wx.getSystemInfoSync().windowHeight - 52 - 35
    const accountId = options.toAccid
    const chatTo = options.toAccid
    const chatNick = options.chatNick
    const chatType = options.type || 'p2p'
    const fromAvatar = options.fromAvatar || ''
    // @ts-ignore
    const loginAccountLogo = this.data.userInfo.avatar
    // 设置顶部标题
    // @ts-ignore
    if (chatTo === this.data.userInfo.accountId) {
      wx.setNavigationBarTitle({
        title: '李诞',
      })
      // } else if (chatType === 'advanced' || chatType === 'normal') {
      // if (this.data.currentGroup.teamId === chatTo && this.data.currentGroup.isCurrentNotIn) {
      //   showToast('error', '您已离开该群组')
      // }
      // let card = this.data.currentGroup || this.data.groupList[chatTo] || {}
      // let memberNum = card.memberNum || 0
      // let title = card.name || chatTo
      // wx.setNavigationBarTitle({
      //   title: (title.length > 8 ? title.slice(0, 8) + '…' : title) + '（' + memberNum + '）',
      // })
      // if (!this.data.groupMemberList[chatTo] || !this.data.groupMemberList[chatTo].allMembers) { // 当前群组的成员不全时获取成员列表 并 更新当前成员是否在群聊的标志
      //   this.getMemberList(chatTo)
      // }
    } else {
      // p2p
      // @ts-ignore
      const card = this.data.friendCard[chatTo] || {}
      wx.setNavigationBarTitle({
        title: chatNick || card.nick || chatTo || accountId,
      })
    }
    // @ts-ignore
    this.setData({
      chatTo,
      chatType,
      accountId,
      fromAvatar,
      fromNick: chatNick,
      loginAccountLogo,
      iconBase64Map: iconBase64Map,
      chatWrapperMaxHeight,
      role: app.globalData.currentRole,
    })

    // 重新计算所有时间
    this.reCalcAllMessageTime()
    // 滚动到底部
    this.scrollToBottom()
    // app.globalData.emitter.on('sendNetcallTipMsg', (text) => {
    //   this.tipInputConfirm(text, true)
    // })
  },
  /**
   * 生命周期函数--监听页面展示
   */
  onShow: function () {
    const chatType = this.data.chatType
    if (chatType === 'advanced' || chatType === 'normal') {
      // @ts-ignore
      const card = this.data.currentGroup
      const memberNum = card.memberNum || 0
      const title = card.name
      wx.setNavigationBarTitle({
        title:
          (title.length > 8 ? title.slice(0, 8) + '…' : title) +
          '（' +
          memberNum +
          '）',
      })
    }
  },
  /**
   * 文本框输入事件
   */
  inputChange(e: { detail: { value: any } }) {
    // @ts-ignore
    this.setData({
      inputValue: e.detail.value,
    })
  },
  /**
   * 键盘单击发送，发送文本
   */
  inputSend(e: { detail: { value: any } }) {
    this.sendRequest(e.detail.value)
  },
  /**
   * emoji组件回调
   */
  emojiCLick(e: { detail: any }) {
    const val = e.detail
    // 单击删除按钮，，删除emoji
    if (val == '[删除]') {
      const lastIndex = this.data.inputValue.lastIndexOf('[')
      if (lastIndex != -1) {
        // @ts-ignore
        this.setData({
          inputValue: this.data.inputValue.slice(0, lastIndex),
        })
      }
      return
    }
    if (val[0] == '[') {
      // emoji
      // @ts-ignore
      this.setData({
        inputValue: this.data.inputValue + val,
      })
    } else {
      //大图
      this.sendBigEmoji(val)
    }
  },
  /**
   * emoji点击发送
   */
  emojiSend(e: any) {
    const val = this.data.inputValue
    this.sendRequest(val)
    // @ts-ignore
    this.setData({
      emojiFlag: false,
    })
  },
  /**
   * 发送网络请求：发送文本消息(包括emoji)
   */
  sendRequest(text: string, isLocal?: true) {
    if ((text && !text.trim()) || !text) {
      wx.showToast({
        title: '不能发送空消息',
        icon: 'none',
      })
      return
    }
    app.globalData.nim.sendText({
      scene: this.data.chatType === 'p2p' ? 'p2p' : 'team',
      to: this.data.accountId,
      text,
      isLocal,
      replyMsg: this.data.replyMsg,
      done: (err: any, msg: any) => {
        // 判断错误类型，并做相应处理
        if (this.handleErrorAfterSend(err)) {
          return
        }
        // 存储数据到store
        this.saveChatMessageListToStore(msg)
        // @ts-ignore
        this.setData({
          inputValue: '',
          focusFlag: false,
          replyMsg: {},
          replayNick: '',
        })
        // 滚动到底部
        this.scrollToBottom()
      },
    })
  },
  /**
   * 发送大的emoji:实际上是type=3的自定义消息
   * {"type":3,"data":{"catalog":"ajmd","chartlet":"ajmd010"}}
   */
  sendBigEmoji(val: string[]) {
    wx.showLoading({
      title: '发送中...',
    })
    const self = this
    let catalog = ''
    if (val[0] === 'a') {
      catalog = 'ajmd'
    } else if (val[0] === 'x') {
      catalog = 'xxy'
    } else if (val[0] === 'l') {
      catalog = 'lt'
    }
    const content = {
      type: 3,
      data: {
        catalog,
        chartlet: val,
      },
    }
    app.globalData.nim.sendCustomMsg({
      scene: self.data.chatType === 'p2p' ? 'p2p' : 'team',
      to: self.data.chatTo,
      content: JSON.stringify(content),
      done: function (err: any, msg: any) {
        wx.hideLoading()
        // 判断错误类型，并做相应处理
        if (self.handleErrorAfterSend(err)) {
          return
        }
        // 存储数据到store
        self.saveChatMessageListToStore(msg)

        // 隐藏发送栏
        // @ts-ignore
        self.setData({
          focusFlag: false,
          emojiFlag: false,
          tipFlag: false,
          moreFlag: false,
          replyMsg: {},
          replayNick: '',
        })
        // 滚动到底部
        self.scrollToBottom()
      },
    })
  },
  /**
   * 发送自定义消息-猜拳
   */
  sendFingerGuess() {
    // @ts-ignore
    this.setData({
      moreFlag: false,
    })
    const content = {
      type: 1,
      data: {
        value: Math.ceil(Math.random() * 3),
      },
    }
    app.globalData.nim.sendCustomMsg({
      scene: this.data.chatType === 'p2p' ? 'p2p' : 'team',
      to: this.data.chatTo,
      content: JSON.stringify(content),
      done: (err: any, msg: any) => {
        // 判断错误类型，并做相应处理
        if (this.handleErrorAfterSend(err)) {
          return
        }
        // 存储数据到store
        this.saveChatMessageListToStore(msg)

        // 滚动到底部
        this.scrollToBottom()
      },
    })
  },
  /**
   * 点击发送tip按钮
   */
  // tipInputConfirm(text: string, isLocal: boolean) {
  //   text = text || this.data.tipInputValue
  //   if (text && text.length) {
  //     app.globalData.nim.sendTipMsg({
  //       scene: this.data.chatType === 'p2p' ? 'p2p' : 'team',
  //       to: this.data.chatTo,
  //       tip: text,
  //       isLocal,
  //       done: (err: any, msg: any) => {
  //         // 判断错误类型，并做相应处理
  //         if (this.handleErrorAfterSend(err)) {
  //           return
  //         }
  //         // 存储数据到store
  //         this.saveChatMessageListToStore(msg)
  //         // @ts-ignore
  //         this.setData({
  //           tipInputValue: '',
  //           tipFlag: false,
  //           replyMsg: {},
  //           replayNick: ''
  //         })

  //         // 滚动到底部
  //         this.scrollToBottom()
  //       },
  //     })
  //   } else {
  //     showToast('text', '请输入内容')
  //   }
  // },
  /**
   * 发送语音消息
   */
  sendAudioMsg(res: WechatMiniprogram.OnStopCallbackResult) {
    wx.showLoading({
      title: '发送中...',
    })
    const tempFilePath = res.tempFilePath
    const self = this
    app.globalData.nim.sendFile({
      scene: self.data.chatType === 'p2p' ? 'p2p' : 'team',
      to: self.data.chatTo,
      type: 'audio',
      wxFilePath: tempFilePath,
      done: function (err: any, msg: any) {
        wx.hideLoading()
        // 判断错误类型，并做相应处理
        if (self.handleErrorAfterSend(err)) {
          return
        }
        // @ts-ignore
        self.setData({
          replyMsg: {},
          replayNick: '',
        })
        // 存储数据到store
        self.saveChatMessageListToStore(msg)

        // 滚动到底部
        self.scrollToBottom()
      },
    })
  },
  /**
   * 发送位置消息
   */
  // sendPositionMsg(res: WechatMiniprogram.ChooseLocationSuccessCallbackResult) {
  //   const self = this
  //   const { address, latitude, longitude } = res
  //   app.globalData.nim.sendGeo({
  //     scene: self.data.chatType === 'p2p' ? 'p2p' : 'team',
  //     to: self.data.chatTo,
  //     geo: {
  //       lng: +longitude,
  //       lat: +latitude,
  //       title: address,
  //     },
  //     done: function (err: any, msg: any) {
  //       // 判断错误类型，并做相应处理
  //       if (self.handleErrorAfterSend(err)) {
  //         return
  //       }
  //       // 存储数据到store
  //       self.saveChatMessageListToStore(msg)

  //       // 滚动到底部
  //       self.scrollToBottom()
  //     },
  //   })
  // },
  /**
   * 发送视频文件到nos
   */
  sendVideoToNos(res: WechatMiniprogram.ChooseVideoSuccessCallbackResult) {
    wx.showLoading({
      title: '发送中...',
    })
    // {duration,errMsg,height,size,tempFilePath,width}
    const self = this
    const tempFilePath = res.tempFilePath
    // 上传文件到nos
    app.globalData.nim.sendFile({
      type: 'video',
      scene: self.data.chatType === 'p2p' ? 'p2p' : 'team',
      to: self.data.chatTo,
      wxFilePath: tempFilePath,
      done: function (err: any, msg: any) {
        wx.hideLoading()
        // file: {dur, ext,h,md5,name,size,url,w}
        // 判断错误类型，并做相应处理
        if (self.handleErrorAfterSend(err)) {
          return
        }
        // @ts-ignore
        self.setData({
          replyMsg: {},
          replayNick: '',
        })
        // 存储数据到store
        self.saveChatMessageListToStore(msg)

        // 滚动到底部
        self.scrollToBottom()
      },
    })
  },
  /**
   * 发送图片到nos
   */
  sendImageToNOS(res: WechatMiniprogram.ChooseImageSuccessCallbackResult) {
    wx.showLoading({
      title: '发送中...',
    })
    const self = this
    const tempFilePaths = res.tempFiles
    for (let i = 0; i < tempFilePaths.length; i++) {
      // 上传文件到nos
      app.globalData.nim.sendFile({
        // app.globalData.nim.previewFile({
        type: 'image',
        scene: self.data.chatType === 'p2p' ? 'p2p' : 'team',
        to: self.data.chatTo,
        // @ts-ignore
        wxFilePath: tempFilePaths[i].tempFilePath,
        done: function (err: any, msg: any) {
          wx.hideLoading()
          // 判断错误类型，并做相应处理
          if (self.handleErrorAfterSend(err)) {
            return
          }
          // @ts-ignore
          self.setData({
            replyMsg: {},
            replayNick: '',
          })
          // 存储数据到store
          self.saveChatMessageListToStore(msg)

          // 滚动到底部
          self.scrollToBottom()
        },
      })
    }
  },
  /**
   * 统一发送消息后打回的错误信息
   * 返回true表示有错误，false表示没错误
   */
  handleErrorAfterSend(err: { code: number }) {
    if (err) {
      if (err.code == 7101) {
        showToast('text', '你已被对方拉黑')
      }
      console.log(err)
      return true
    }
    return false
  },
  /**
   * 滚动页面到底部
   */
  scrollToBottom() {
    wx.pageScrollTo({
      scrollTop: 999999,
      duration: 0,
    })
  },
  /**
   * 保存数据到store
   */
  saveChatMessageListToStore(rawMsg: any, handledMsg?: undefined) {
    console.log(rawMsg, 'rawMsg')
    store.dispatch({
      type: 'RawMessageList_Add_Msg',
      payload: { msg: rawMsg },
    })
  },
  /**
   * 收起所有输入框
   */
  chatingWrapperClick(e: any) {
    this.foldInputArea()
  },
  /**
   * 收起键盘
   */
  foldInputArea() {
    // @ts-ignore
    this.setData({
      focusFlag: false,
      emojiFlag: false,
      tipFlag: false,
      moreFlag: false,
    })
  },
  /**
   * 全屏播放视频
   */
  requestFullScreenVideo(e: { currentTarget: { dataset: { video: any } } }) {
    const video = e.currentTarget.dataset.video
    wx.previewMedia({
      sources: [
        {
          url: video.url,
          type: 'video',
        },
      ],
      url: video.url,
      success: (res) => {
        console.log(res)
      },
      fail: (err) => {
        console.error(err)
      },
    })
    // const videoContext = wx.createVideoContext('videoEle')
    // // @ts-ignore
    // this.setData({
    //   isVideoFullScreen: true,
    //   videoSrc: video.url,
    //   videoContext,
    // })
    // // @ts-ignore
    // videoContext.requestFullScreen({
    //   direction: 0
    // })
    // videoContext.play()
  },
  /**
   * 视频播放结束钩子
   */
  videoEnded() {
    // @ts-ignore
    this.setData({
      isVideoFullScreen: false,
      videoSrc: '',
    })
  },
  /**
   * 播放音频
   */
  playAudio(e: { currentTarget: { dataset: { audio: any } } }) {
    showToast('text', '播放中', {
      duration: 120 * 1000,
      isMask: true,
    })
    const audio = e.currentTarget.dataset.audio
    const audioContext = wx.createInnerAudioContext()
    if (audio.ext === 'mp3') {
      // 小程序发送的
      audioContext.src = audio.url
    } else {
      audioContext.src = audio.mp3Url
    }
    audioContext.play()
    // audioContext.onPlay(() => {})
    audioContext.onEnded(() => {
      wx.hideToast()
    })
    audioContext.onError((res: any) => {
      showToast('text', res.errCode)
    })
  },
  /**
   * 重新计算时间头
   */
  reCalcAllMessageTime() {
    const tempArr = [...this.data.messageArr]
    if (tempArr.length == 0) return
    // 计算时差
    tempArr.map((msg: any, index) => {
      if (index === 0) {
        msg['displayTimeHeader'] = calcTimeHeader(msg.time)
      } else {
        // @ts-ignore
        const delta = (msg.time - tempArr[index - 1].time) / (120 * 1000)
        if (delta > 1) {
          // 距离上一条，超过两分钟重新计算头部
          msg['displayTimeHeader'] = calcTimeHeader(msg.time)
        }
      }
    })
    // @ts-ignore
    this.setData({
      messageArr: tempArr,
    })
  },
  /**
   * 切换发送文本类型
   */
  switchSendType() {
    // @ts-ignore
    this.setData({
      sendType: this.data.sendType == 0 ? 1 : 0,
      focusFlag: false,
      emojiFlag: false,
    })
  },
  /**
   * 获取焦点
   */
  inputFocus(e: any) {
    // @ts-ignore
    this.setData({
      emojiFlag: false,
      focusFlag: true,
    })
  },
  /**
   * 失去焦点
   */
  inputBlur() {
    // @ts-ignore
    this.setData({
      focusFlag: false,
    })
  },
  /**
   * tip输入
   */
  tipInputChange(e: { detail: { value: any } }) {
    // @ts-ignore
    this.setData({
      tipInputValue: e.detail.value,
    })
  },
  /**
   * 组件按钮回调
   */
  // tipClickHandler(e: { detail: { data: any } }) {
  //   const data = e.detail.data
  //   if (data === 'confirm') {
  //     if (this.data.tipInputValue.length === 0) {
  //       showToast('text', '请输入内容')
  //     } else {
  //       // @ts-ignore
  //       this.tipInputConfirm()
  //       // @ts-ignore
  //       this.setData({
  //         tipFlag: false,
  //       })
  //     }
  //   } else if (data === 'cancel') {
  //     // @ts-ignore
  //     this.setData({
  //       tipFlag: false,
  //     })
  //   }
  // },
  /**
   * 切换出emoji键盘
   */
  toggleEmoji() {
    // @ts-ignore
    this.setData({
      sendType: 0,
      // focusFlag: this.data.emojiFlag ? true : false,
      emojiFlag: !this.data.emojiFlag,
      moreFlag: false,
    })
  },
  /**
   * 切出更多
   */
  toggleMore() {
    // @ts-ignore
    this.setData({
      moreFlag: !this.data.moreFlag,
      emojiFlag: false,
      focusFlag: false,
    })
  },
  /**
   * 调出tip发送面板
   */
  // showTipMessagePanel() {
  //   // @ts-ignore
  //   this.setData({
  //     tipFlag: true,
  //     moreFlag: false,
  //   })
  // },
  /**
   * 微信按钮长按，有bug，有时候不触发
   */
  voiceBtnLongTap(e: any) {
    const self = this
    // @ts-ignore
    self.setData({
      isLongPress: true,
    })
    wx.getSetting({
      success: (res) => {
        const recordAuth = res.authSetting['scope.record']
        if (recordAuth == false) {
          //已申请过授权，但是用户拒绝
          wx.openSetting({
            success: function (res) {
              const recordAuth = res.authSetting['scope.record']
              if (recordAuth == true) {
                showToast('success', '授权成功')
              } else {
                showToast('text', '请授权录音')
              }
              // @ts-ignore
              self.setData({
                isLongPress: false,
              })
            },
          })
        } else if (recordAuth == true) {
          // 用户已经同意授权
          self.startRecord()
        } else {
          // 第一次进来，未发起授权
          wx.authorize({
            scope: 'scope.record',
            success: () => {
              //授权成功
              showToast('success', '授权成功')
            },
          })
        }
      },
      fail: function () {
        showToast('error', '鉴权失败，请重试')
      },
    })
  },
  /**
   * 手动模拟按钮长按，
   */
  longPressStart() {
    const self = this
    // @ts-ignore
    self.setData({
      recordClicked: true,
    })
    setTimeout(() => {
      if (self.data.recordClicked == true) {
        self.executeRecord()
      }
    }, 350)
  },
  /**
   * 语音按钮长按结束
   */
  longPressEnd() {
    // @ts-ignore
    this.setData({
      recordClicked: false,
    })
    // 第一次授权，
    if (!this.data.recorderManager) {
      // @ts-ignore
      this.setData({
        isLongPress: false,
      })
      return
    }
    if (this.data.isLongPress === true) {
      // @ts-ignore
      this.setData({
        isLongPress: false,
      })
      wx.hideToast()
      // @ts-ignore
      this.data.recorderManager.stop()
    }
  },
  /**
   * 执行录音逻辑
   */
  executeRecord() {
    const self = this
    // @ts-ignore
    self.setData({
      isLongPress: true,
    })
    wx.getSetting({
      success: (res) => {
        const recordAuth = res.authSetting['scope.record']
        if (recordAuth == false) {
          //已申请过授权，但是用户拒绝
          wx.openSetting({
            success: function (res) {
              const recordAuth = res.authSetting['scope.record']
              if (recordAuth == true) {
                showToast('success', '授权成功')
              } else {
                showToast('text', '请授权录音')
              }
              // @ts-ignore
              self.setData({
                isLongPress: false,
              })
            },
          })
        } else if (recordAuth == true) {
          // 用户已经同意授权
          self.startRecord()
        } else {
          // 第一次进来，未发起授权
          wx.authorize({
            scope: 'scope.record',
            success: () => {
              //授权成功
              showToast('success', '授权成功')
            },
          })
        }
      },
      fail: function () {
        showToast('error', '鉴权失败，请重试')
      },
    })
  },
  /**
   * 开始录音
   */
  startRecord() {
    const self = this
    showToast('text', '开始录音', { duration: 120000 })
    const recorderManager = self.data.recorderManager || wx.getRecorderManager()
    const options = {
      duration: 120 * 1000,
      format: 'mp3',
    }
    // @ts-ignore
    recorderManager.start(options)
    // @ts-ignore
    self.setData({
      recorderManager,
    })
    recorderManager.onStop((res) => {
      if (res.duration < 2000) {
        showToast('text', '录音时间太短')
      } else {
        self.sendAudioMsg(res)
      }
    })
  },
  /**
   * 选择相册图片
   */
  chooseImageToSend(e: { currentTarget: { dataset: { type: any } } }) {
    const self = this
    // @ts-ignore
    self.setData({
      moreFlag: false,
    })
    wx.chooseMedia({
      sourceType: ['album'],
      mediaType: ['image'],
      success: function (res: any) {
        self.sendImageToNOS(res)
      },
    })
  },
  /**
   * 选择拍摄视频或者照片
   */
  chooseImageOrVideo() {
    const self = this
    // @ts-ignore
    self.setData({
      moreFlag: false,
    })
    wx.showActionSheet({
      itemList: ['照相', '视频'],
      success: function (res) {
        if (res.tapIndex === 0) {
          // 相片
          wx.chooseMedia({
            sourceType: ['camera'],
            mediaType: ['image'],
            success: function (res: any) {
              self.sendImageToNOS(res)
            },
          })
        } else if (res.tapIndex === 1) {
          // 视频
          wx.chooseVideo({
            sourceType: ['camera', 'album'],
            success: function (res) {
              if (res.duration > 60) {
                showToast('text', '视频时长超过60s，请重新选择')
                return
              }
              console.log(res)
              // {duration,errMsg,height,size,tempFilePath,width}
              self.sendVideoToNos(res)
            },
          })
        }
      },
    })
  },
  /**
   * 选取位置
   */
  // choosePosition() {
  //   const self = this
  //   self.setData({
  //     moreFlag: false,
  //   })
  //   wx.getSetting({
  //     success: (res) => {
  //       const auth = res.authSetting['scope.userLocation']
  //       if (auth == false) {
  //         //已申请过授权，但是用户拒绝
  //         wx.openSetting({
  //           success: function (res) {
  //             if (res.authSetting['scope.userLocation'] == true) {
  //               showToast('success', '授权成功')
  //             } else {
  //               showToast('text', '请授权地理位置')
  //             }
  //           },
  //         })
  //       } else if (auth == true) {
  //         // 用户已经同意授权
  //         self.callSysMap()
  //       } else {
  //         // 第一次进来，未发起授权
  //         wx.authorize({
  //           scope: 'scope.userLocation',
  //           success: () => {
  //             //授权成功
  //             self.callSysMap()
  //           },
  //         })
  //       }
  //     },
  //     fail: (res) => {
  //       showToast('error', '鉴权失败，请重试')
  //     },
  //   })
  // },
  /**
   * 视频通话
   */
  // videoCall() {
  //   if (app.globalData.waitingUseVideoCall) {
  //     showToast('text', '请勿频繁操作', { duration: 2000 })
  //     return
  //   }
  //   // 挂断的时候开启定时器，以免对方尚未状态结束
  //   // clearTimeout(app.globalData.videoCallTimer)
  //   // app.globalData.videoCallTimer = setTimeout(() => {
  //   //   app.globalData.waitingUseVideoCall = false
  //   // }, 5000)
  //   // app.globalData.waitingUseVideoCall = true
  //   if (this.data.chatType === 'advanced' || this.data.chatType === 'normal') {
  //     // 群组
  //     // if (this.data.currentGroup.memberNum.length < 2) {
  //     //   showToast('text', '无法发起，人数少于2人')
  //     // } else {
  //     //   wx.navigateTo({
  //     //     url: `../forwardMultiContact/forwardMultiContact?teamId=${this.data.currentGroup.teamId}`,
  //     //   })
  //     // }
  //   } else {
  //     // p2p
  //     console.log(`正在发起对${this.data.chatTo}的视频通话`)
  //     wx.navigateTo({
  //       url: `../videoCall/videoCall?callee=${this.data.chatTo}`,
  //     })
  //   }
  // },
  /**
   * 调用系统地图界面
   */
  // callSysMap() {
  //   const self = this
  //   wx.chooseLocation({
  //     success: function (res) {
  //       const { address, latitude, longitude } = res
  //       self.sendPositionMsg(res)
  //     },
  //   })
  // },
  /**
   * 查看全屏地图
   */
  // fullScreenMap(e: { currentTarget: { dataset: { geo: any } } }) {
  //   const geo = e.currentTarget.dataset.geo
  //   wx.openLocation({
  //     latitude: geo.lat,
  //     longitude: geo.lng,
  //   })
  // },
  /**
   * 切换到个人介绍页
   */
  // switchToMyTab() {
  //   wx.switchTab({
  //     url: '../../pages/setting/setting',
  //   })
  // },
  /**
   * 切换到对方介绍页
   */
  // switchPersonCard(data: { target: { dataset: { account: string } } }) {
  //   if (this.data.chatType === 'p2p') {
  //     if (
  //       this.data.chatTo === this.data.userInfo.accountId ||
  //       this.data.chatTo === 'ai-assistant'
  //     ) {
  //       return
  //     }
  //     // 重定向进入account介绍页
  //     // clickLogoJumpToCard(this.data.friendCard, this.data.chatTo, false)
  //   } else if (this.data.chatType === 'advanced') {
  //     wx.navigateTo({
  //       url:
  //         '../../partials/advancedGroupMemberCard/advancedGroupMemberCard?account=' +
  //         data.target.dataset.account +
  //         '&teamId=' +
  //         this.data.chatTo,
  //     })
  //   }
  // },
  /**
   * 查看云端历史消息、查看群信息、查看讨论组信息
   */
  // lookMessage() {
  //   const self = this
  //   const actionArr = ['清空本地聊天记录', '查看云消息记录']
  //   const actionFn = [self.sureToClearAllMessage, self.lookAllMessage]
  // if (this.data.currentGroup.isCurrentNotIn) {
  //   actionArr.pop()
  // }
  // if (self.data.chatType === 'advanced') {
  //   actionArr.unshift('群信息')
  //   actionFn.unshift(self.lookAdvancedGroupInfo)
  // } else if (self.data.chatType === 'normal') {
  //   actionArr.unshift('讨论组信息')
  //   actionFn.unshift(self.lookNormalGroupInfo)
  // }
  //   wx.showActionSheet({
  //     itemList: actionArr,
  //     success: (res) => {
  //       actionFn[res.tapIndex]()
  //     },
  //   })
  // },
  /**
   * 查看群信息
  //  */
  // lookAdvancedGroupInfo() {
  //   store.dispatch({
  //     type: 'Set_Current_Group_And_Members',
  //     payload: this.data.chatTo,
  //   })
  //   wx.navigateTo({
  //     url: `../advancedGroupCard/advancedGroupCard?teamId=${this.data.chatTo}&from=${this.data.from}`,
  //   })
  // },
  /**
   * 查看讨论组信息
   */
  // lookNormalGroupInfo() {
  //   store.dispatch({
  //     type: 'Set_Current_Group_And_Members',
  //     payload: this.data.chatTo,
  //   })
  //   wx.navigateTo({
  //     url: `../normalGroupCard/normalGroupCard?teamId=${this.data.chatTo}&from=${this.data.from}`,
  //   })
  // },
  /**
   * 弹框 确认 清除本地记录
   */
  // sureToClearAllMessage() {
  //   const self = this
  //   wx.showActionSheet({
  //     //二次确认
  //     itemList: ['清空'],
  //     itemColor: '#f00',
  //     success: (res) => {
  //       if (res.tapIndex == 0) {
  //         self.clearAllMessage()
  //       }
  //     },
  //   })
  // },
  /**
   * 查看云消息记录
   */
  // lookAllMessage() {
  //   wx.navigateTo({
  //     url: `../historyFromCloud/historyFromCloud?account=${this.data.chatTo}&chatType=${this.data.chatType}`,
  //   })
  // },
  /**
   * 清除本地记录
   */
  // clearAllMessage() {
  //   // 刷新本地视图
  //   // @ts-ignore
  //   this.setData({
  //     messageArr: [],
  //   })
  //   store.dispatch({
  //     type: 'Delete_All_MessageByAccount',
  //     payload:
  //       (this.data.chatType === 'p2p' ? 'p2p-' : 'team-') + this.data.chatTo,
  //   })
  // },
  /**
   * 展示编辑菜单
   */
  showEditorMenu(e: { currentTarget: { dataset: { message: any } } }) {
    const message = e.currentTarget.dataset.message
    if (message.type === 'tip') {
      return
    }
    console.log(message, 'showEditorMenu message')
    if (message.type === 'image') {
      message.replayText = '[图片]'
    } else if (message.type === 'video') {
      message.replayText = '[视频]'
    } else if (message.type === 'audio') {
      message.replayText = '[语音]'
    } else {
      message.replayText = message.text
    }
    const self = this
    if (message.sendOrReceive === 'send') {
      // 自己消息
      wx.showActionSheet({
        itemList:
          message.type === 'text'
            ? ['回复', '删除', '撤回', '复制']
            : ['回复', '删除', '撤回'],
        success: function (res) {
          switch (res.tapIndex) {
            case 0:
              // @ts-ignore
              self.setData({
                replyMsg: message,
                replayNick: app.globalData.userInfo.nickname,
              })
              break
            case 1:
              wx.showActionSheet({
                itemList: ['确定删除'],
                itemColor: '#ff0000',
                success: function (res) {
                  if (res.tapIndex === 0) {
                    self.deleteMessageRecord(message)
                  }
                },
              })
              break
            case 2:
              wx.showActionSheet({
                itemList: ['确定'],
                itemColor: '#ff0000',
                success: function (res) {
                  if (res.tapIndex === 0) {
                    self.recallMessage(message)
                  }
                },
              })
              break
            case 3:
              wx.setClipboardData({
                data: message.text || '',
              })
              break
            default:
              break
          }
        },
      })
    } else {
      // 对方消息
      wx.showActionSheet({
        itemList: ['复制', '回复', '删除'],
        success: function (res) {
          switch (res.tapIndex) {
            case 0:
              wx.setClipboardData({
                data: message.text || '',
              })
              break
            case 1:
              // @ts-ignore
              self.setData({
                replyMsg: message,
                replayNick: self.data.fromNick,
              })
              break
            case 2:
              wx.showActionSheet({
                itemList: ['确定删除'],
                itemColor: '#ff0000',
                success: function (res) {
                  if (res.tapIndex === 0) {
                    self.deleteMessageRecord(message)
                  }
                },
              })
              break
            default:
              break
          }
        },
      })
    }
  },
  /**
   * 转发消息
   */
  // forwardMessage(paramObj: { time: any; chatTo: string; chatType: string }) {
  //   const str = encodeURIComponent(JSON.stringify(paramObj))
  //   wx.redirectTo({
  //     url: '../forwardContact/forwardContact?data=' + str,
  //   })
  // },
  /**
   * 撤回消息
   */
  recallMessage(message: { time: string | number }) {
    const self = this
    const sessionId =
      (self.data.chatType === 'p2p' ? 'p2p-' : 'team-') + self.data.chatTo
    // @ts-ignore
    const rawMessage = self.data.rawMessageList[sessionId][message.time]

    app.globalData.nim.deleteMsg({
      msg: rawMessage,
      done: function (err: any, { msg }: any) {
        if (err) {
          // 撤回失败
          console.log(err)
          showToast('text', '消息已超过2分钟，不能撤回')
          return
        } else {
          // 撤回成功
          store.dispatch({
            type: 'RawMessageList_Recall_Msg',
            payload: msg,
          })
          // 滚动到底部
          self.scrollToBottom()
        }
      },
    })
  },
  // 重新编辑撤回的信息
  handleReEditMsg(e: any) {
    const text = e.currentTarget.dataset.msg || ''
    // @ts-ignore
    this.setData({
      inputValue: text,
    })
  },
  // 取消回复
  handleCancelReplay() {
    // @ts-ignore
    this.setData({
      replyMsg: {},
    })
  },
  // 跳转到引用消息
  handleReachToReaply(e: any) {
    const selector = '#' + e.currentTarget.dataset.id
    wx.createSelectorQuery()
      .select(selector)
      .boundingClientRect(function (rect) {
        console.log(selector, 'selector')
        wx.pageScrollTo({
          selector: selector,
        })
      })
      .exec()
  },
  /**
   * 删除消息
   * {displayTimeHeader,nodes,sendOrReceive,text,time,type}
   */
  deleteMessageRecord(msg: { time: any }) {
    const sessionId =
      (this.data.chatType === 'p2p' ? 'p2p-' : 'team-') + this.data.chatTo
    // 从全局记录中删除(本地删除)
    store.dispatch({
      type: 'Delete_Single_MessageByAccount',
      payload: { sessionId: sessionId, time: msg.time },
    })
    // 需要后台开通删除漫游会话的权限
    // app.globalData.nim.deleteMsgSelf({
    //   msg: msg,
    //   done: (error: any, obj: any) => {
    //     console.log('删除会话' + (!error ? '成功' : '失败'), error, obj)
    //   },
    // })
  },
  /**
   * 距离上一条消息是否超过两分钟
   */
  judgeOverTwoMinute: (time: any, messageArr: string | any[]) => {
    let displayTimeHeader = ''
    const lastMessage = messageArr[messageArr.length - 1]
    if (lastMessage) {
      //拥有上一条消息
      const delta = time - lastMessage.time
      if (delta > 2 * 60 * 1000) {
        //两分钟以上
        displayTimeHeader = calcTimeHeader(time)
      }
    } else {
      //没有上一条消息
      displayTimeHeader = calcTimeHeader(time)
    }
    return displayTimeHeader
  },
  handleCloseDialog() {
    // @ts-ignore
    this.setData({
      dialogShow: false,
    })
  },
  handleShowPatientInfo() {
    // @ts-ignore
    this.setData({
      dialogShow: true,
    })
  },
  handleShowToast() {
    showToast('text', '仅展示，无实际功能')
  },
  // 图片预览
  handlePreviewImg(event: any) {
    const nodes = event.currentTarget.dataset.nodes
    const url = nodes[0] && nodes[0].attrs && nodes[0].attrs.src
    console.log(nodes, 'handlePreviewImg')
    if (!url) {
      wx.showToast({
        title: '图片加载失败，请重试',
        icon: 'none',
      })
    }
    wx.previewImage({
      current: url,
      urls: [url], // 需要预览的图片 http 链接列表
    })
  },
  onImageError(e: any) {
    console.error('previewImage error', e.detail.errMsg)
  },
  /**
   * 原始消息列表转化为适用于渲染的消息列表
   * {unixtime1: {flow,from,fromNick,idServer,scene,sessionId,text,target,to,time...}, unixtime2: {}}
   * =>
   * [{text, time, sendOrReceive: 'send', displayTimeHeader, nodes: []},{type: 'geo',geo: {lat,lng,title}}]
   */
  convertRawMessageListToRenderMessageArr(rawMsgList: any) {
    const messageArr = []
    for (const time in rawMsgList) {
      const rawMsg = rawMsgList[time] || {}
      let msgType = ''
      if (
        rawMsg.type === 'custom' &&
        JSON.parse(rawMsg['content'])['type'] === 1
      ) {
        msgType = '猜拳'
      } else if (
        rawMsg.type === 'custom' &&
        JSON.parse(rawMsg['content'])['type'] === 3
      ) {
        msgType = '贴图表情'
      } else {
        msgType = rawMsg.type
      }
      const displayTimeHeader = this.judgeOverTwoMinute(rawMsg.time, messageArr)
      const sendOrReceive = rawMsg.flow === 'in' ? 'receive' : 'send'
      let specifiedObject = {}
      switch (msgType) {
        case 'text': {
          specifiedObject = {
            nodes: generateRichTextNode(rawMsg.text),
          }
          break
        }
        case 'image': {
          specifiedObject = {
            nodes: generateImageNode(rawMsg.file),
          }
          break
        }
        case 'geo': {
          specifiedObject = {
            geo: rawMsg.geo,
          }
          break
        }
        case 'audio': {
          specifiedObject = {
            audio: rawMsg.file,
            duration: rawMsg.file.dur && Math.round(rawMsg.file.dur / 1000),
          }
          break
        }
        case 'video': {
          specifiedObject = {
            video: rawMsg.file,
            duration: rawMsg.file.dur && Math.round(rawMsg.file.dur / 1000),
          }
          break
        }
        case '贴图表情': {
          const content = JSON.parse(rawMsg['content'])
          specifiedObject = {
            nodes: generateImageNode(generateBigEmojiImageFile(content)),
          }
          break
        }
        case 'tip': {
          specifiedObject = {
            text: rawMsg.tip,
            nodes: [
              {
                type: 'text',
                text: rawMsg.tip,
              },
            ],
          }
          break
        }
        case '白板消息':
        case '阅后即焚': {
          specifiedObject = {
            nodes: [
              {
                type: 'text',
                text: `[${msgType}],请到手机或电脑客户端查看`,
              },
            ],
          }
          break
        }
        case 'file':
        case 'robot': {
          const text = msgType === 'file' ? '文件消息' : '机器人消息'
          specifiedObject = {
            nodes: [
              {
                type: 'text',
                text: `[${text}],请到手机或电脑客户端查看`,
              },
            ],
          }
          break
        }
        case 'custom':
          specifiedObject = {
            nodes: [
              {
                type: 'text',
                text: '自定义消息',
              },
            ],
          }
          break
        case 'notification':
          specifiedObject = {
            // netbill的text为空
            text:
              rawMsg.groupNotification ||
              (rawMsg.text.length == 0 ? '通知' : rawMsg.text),
            nodes: [
              {
                type: 'text',
                text:
                  rawMsg.groupNotification ||
                  (rawMsg.text.length == 0 ? '通知' : rawMsg.text),
              },
            ],
          }
          break
        default: {
          break
        }
      }
      const msgItem = Object.assign(
        {},
        {
          from: rawMsg.from,
          type: msgType,
          text: rawMsg.text || '',
          time,
          sendOrReceive,
          displayTimeHeader,
          ...rawMsg,
        },
        specifiedObject
      )
      // 撤回的消息，需要携带已撤回文案，方便重新编辑
      if (msgType === 'tip') {
        // @ts-ignore
        msgItem.recallText = rawMsg.recallText
      }

      // 是回复消息需要带上回复信息体
      if (rawMsg.isReplyMsg && rawMsg.replyMsgTime && rawMsg.replyMsgIdClient) {
        const replayMsg = rawMsgList[rawMsg.replyMsgTime]
        if (!replayMsg) return
        // @ts-ignore
        msgItem.replyMsg = replayMsg || {}
        if (replayMsg.type === 'image') {
          msgItem.replyMsg.replayText = '[图片]'
        } else if (replayMsg.type === 'video') {
          msgItem.replyMsg.replayText = '[视频]'
        } else if (replayMsg.type === 'audio') {
          msgItem.replyMsg.replayText = '[语音]'
        } else {
          msgItem.replyMsg.replayText = rawMsgList[rawMsg.replyMsgTime].text
        }
      }
      // 过滤rtc相关的通知消息
      if (rawMsg.type !== 'g2') {
        messageArr.push(msgItem)
      }
      // 标记已读
      app.globalData.nim.sendMsgReceipt({
        msg: rawMsg,
      })
    }
    messageArr.sort((a, b) => {
      // @ts-ignore
      return parseInt(a.time) - parseInt(b.time)
    })

    // 滚动到底部
    this.scrollToBottom()
    return messageArr
  },
}

const mapStateToData = (state: any) => {
  const sessionId = state.currentChatTo
  const messageArr = pageConfig.convertRawMessageListToRenderMessageArr(
    state.rawMessageList[sessionId]
  )
  // console.log(messageArr, 'messageArr')
  return {
    friendCard: state.friendCard,
    personList: state.personList,
    userInfo: state.userInfo,
    currentGroup: state.currentGroup,
    groupList: state.groupList,
    groupMemberList: state.groupMemberList,
    rawMessageList: state.rawMessageList,
    messageArr: messageArr || [],
  }
}
// eslint-disable-next-line @typescript-eslint/no-empty-function
const mapDispatchToPage = (dispatch: any) => {}
const connectedPageConfig = connect(
  mapStateToData,
  mapDispatchToPage
)(pageConfig)
Page(connectedPageConfig)
