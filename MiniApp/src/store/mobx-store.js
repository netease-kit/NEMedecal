import { observable, action } from 'mobx-miniprogram'

export const mobxStore = observable({
  // 未读消息
  unreadCount: 0,

  // 计算属性
  get getUnreadCount() {
    return this.unreadCount
  },

  // actions
  updateUnreadCount: action(function (count) {
    this.unreadCount = count
  }),
})
