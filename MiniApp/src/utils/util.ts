/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { appKey } from '../config/index'
import emojimap from './emojimap.js'
const emoji: any = emojimap.emojiList.emoji

/**
 * @description: 防抖
 * @param {*} fn
 * @param {*} wait
 * @return {*}
 */
export function debounce(
  fn: (...args: any) => void,
  wait: any = 300
): () => void {
  let timer: any = null
  return function (...args) {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }

    timer = setTimeout(() => {
      // @ts-ignore
      fn.apply(this, args)
    }, wait)
  }
}

export const formatTime = (date: Date) => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return (
    [year, month, day].map(formatNumber).join('/') +
    ' ' +
    [hour, minute, second].map(formatNumber).join(':')
  )
}

const formatNumber = (n: number) => {
  const s = n.toString()
  return s[1] ? s : '0' + s
}
/**
 * 生成富文本节点
 */
export const generateRichTextNode = (text: string) => {
  let tempStr = text
  const richTextNode = []
  let leftBracketIndex = tempStr.indexOf('[')
  let rightBracketIndex = tempStr.indexOf(']')
  let countOfWord = 0
  Array.from(tempStr).map((item) => {
    if (item != '[' && item != ']') {
      countOfWord++
    }
  })
  if (leftBracketIndex == -1 || rightBracketIndex == -1 || countOfWord == 0) {
    //没有emoji
    richTextNode.push({
      type: 'text',
      text: tempStr,
    })
    return richTextNode
  }
  while (tempStr.length != 0) {
    leftBracketIndex = tempStr.indexOf('[')
    rightBracketIndex = tempStr.indexOf(']')
    if (leftBracketIndex == 0) {
      // 开头是[
      rightBracketIndex = tempStr.indexOf(']')
      if (rightBracketIndex == -1) {
        richTextNode.push({
          type: 'text',
          text: tempStr,
        })
        tempStr = ''
      } else {
        const emojiName = tempStr.slice(0, rightBracketIndex + 1)
        if (emoji[emojiName]) {
          // 有效emoji
          richTextNode.push({
            name: 'img',
            attrs: {
              width: '30rpx',
              height: '30rpx',
              src: emoji[emojiName].img,
            },
          })
        } else {
          //无效emoji
          richTextNode.push({
            type: 'text',
            text: emojiName,
          })
        }
        tempStr = tempStr.substring(rightBracketIndex + 1, tempStr.length)
      }
    } else {
      // 开头不是[
      if (leftBracketIndex == -1) {
        // 最后全是文字
        richTextNode.push({
          type: 'text',
          text: tempStr.slice(0, tempStr.length),
        })
        tempStr = ''
      } else {
        richTextNode.push({
          type: 'text',
          text: tempStr.slice(0, leftBracketIndex),
        })
        tempStr = tempStr.substring(leftBracketIndex, tempStr.length + 1)
      }
    }
  }
  return richTextNode
}
/**
 * 输入Unix时间戳，返回指定时间格式
 */
export const calcTimeHeader = (time: string) => {
  // 格式化传入时间
  const date = new Date(parseInt(time)),
    year = date.getUTCFullYear(),
    month = date.getUTCMonth(),
    day = date.getDate(),
    hour = date.getHours(),
    minute = date.getUTCMinutes()
  // 获取当前时间
  const currentDate = new Date(),
    currentYear = date.getUTCFullYear(),
    currentMonth = date.getUTCMonth(),
    currentDay = currentDate.getDate()
  // 计算是否是同一天
  if (currentYear == year && currentMonth == month && currentDay == day) {
    //同一天直接返回
    if (hour > 12) {
      return `下午 ${hour}:${minute < 10 ? '0' + minute : minute}`
    } else {
      return `上午 ${hour}:${minute < 10 ? '0' + minute : minute}`
    }
  }
  // 计算是否是昨天
  // @ts-ignore
  const yesterday = new Date(currentDate - 24 * 3600 * 1000)
  if (
    year == yesterday.getUTCFullYear() &&
    // @ts-ignore
    month == yesterday.getUTCMonth &&
    day == yesterday.getDate()
  ) {
    //昨天
    return `昨天 ${hour}:${minute < 10 ? '0' + minute : minute}`
  } else {
    return `${year}-${month + 1}-${day} ${hour}:${
      minute < 10 ? '0' + minute : minute
    }`
  }
}

/**
 * 封装toast
 */
export const showToast = (
  type: string,
  text: string,
  obj?: { duration?: number; isMask?: boolean }
) => {
  const param = {
    title: '',
    icon: 'none',
    duration: (obj && obj.duration) || 1500,
    mask: obj && obj.isMask,
  }
  switch (type) {
    case 'text': {
      param['title'] = text || ''
      param['icon'] = 'none'
      break
    }
    case 'loading': {
      param['title'] = text || ''
      param['icon'] = 'loading'
      break
    }
    case 'success': {
      param['title'] = text || ''
      param['icon'] = 'success'
      break
    }
    case 'error': {
      param['title'] = text || ''
      param['icon'] = 'error'
      break
    }
    default: {
      break
    }
  }
  // @ts-ignore
  wx.showToast(param)
}

/**
 * 输出贴图表情对象，用于生成富文本图片节点
 * content:"{"type":3,"data":{"catalog":"ajmd","chartlet":"ajmd010"}}"
 */
export const generateBigEmojiImageFile = (content: {
  data: { catalog: ''; chartlet: '' }
}) => {
  const prefix = 'http://yx-web.nosdn.127.net/webdoc/h5/emoji/'
  const file = { w: 100, h: 100, url: '' }
  file.url = `${prefix}${content.data.catalog}/${content.data.chartlet}.png`
  return file
}
/**
 * 处理图片富文本节点
 */
export const generateImageNode = (file: { w: number; h: number; url: any }) => {
  // console.log(file)
  let width = 0,
    height = 0
  if (file.w > 250) {
    width = 200
    height = file.h / (file.w / 200)
  } else {
    width = file.w
    height = file.h
  }
  const richTextNode = []
  richTextNode.push({
    name: 'img',
    attrs: {
      width: `${width}rpx`,
      height: `${height}rpx`,
      src: file.url,
    },
  })
  return richTextNode
}
export const dealMsg = (msg: any, store: any, app: any) => {
  const account = msg.from
  if (msg.type === 'deleteMsg') {
    store.dispatch({
      type: 'RawMessageList_OppositeRecall_Msg',
      payload: msg,
    })
  } else if (msg.type === 'addFriend') {
    //第三方将自己加到好友列表
    app.globalData.nim.subscribeEvent({
      type: 1, // 订阅用户登录状态事件
      accounts: [account],
      sync: true,
      done: function (err: any, obj: any) {
        console.log(err, obj)
      },
    })
    app.globalData.nim.getUser({
      account: account,
      done: function (err: any, user: any) {
        if (err) {
          console.log('onSysMsg: getUser: ', err)
          return
        }
        store.dispatch({
          type: 'Notification_Opposite_AddFriend',
          payload: {
            msg,
            desc: `添加好友-${msg.from}添加你为好友`,
          },
        })
        store.dispatch({
          type: 'FriendCard_Add_Friend',
          payload: user,
        })
      },
    })
  } else if (msg.type === 'deleteFriend') {
    store.dispatch({
      type: 'Notification_Opposite_DeleteFriend',
      payload: {
        msg,
        desc: `删除好友-${msg.from}已将你从他的好友列表中移除`,
      },
    })
    store.dispatch({
      type: 'FriendCard_Delete_By_Account',
      payload: account,
    })
  } else if (msg.type === 'teamInvite') {
    // category:"team"
    store.dispatch({
      type: 'Notification_Team_Invite',
      payload: {
        msg,
        desc: `${msg.from}邀请你入群“${msg.attach.team.name}”`,
      },
    })
  } else if (msg.type === 'applyTeam') {
    // category:"team"
    store.dispatch({
      type: 'Notification_Team_Apply',
      payload: {
        msg,
        desc: `${msg.from}申请加入`,
      },
    })
  }
}

// 发请求
export const sendRequest = (params: {
  url: string
  data?: any
  header?: any
  method?: string
}) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: params.url,
      data: params.data,
      header: {
        clientType: 'miniApp',
        versionCode: '1.1.0',
        'content-type': 'application/json;charset=utf-8',
        appKey,
        accessToken: wx.getStorageSync('yx-medicine-login-userInfo')
          .accessToken,
        ...params.header,
      },
      // @ts-ignore
      method: params.method || 'POST',
      // @ts-ignore
      success: (res: { code: number; requestId: number; data: any }) => {
        if (res.data.code !== 200) {
          return reject(res.data)
        }
        resolve(res.data.data)
      },
      fail: (err) => {
        reject(err)
      },
    })
  })
}
