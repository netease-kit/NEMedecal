import { storeBindingsBehavior } from 'mobx-miniprogram-bindings'
import { mobxStore } from '../store/mobx-store'

Component({
  behaviors: [storeBindingsBehavior],
  data: {
    color: '#999999',
    selectedColor: '#37D6CA',
    selected: 0,
    unreadCount: 0,
    list: [], // 菜单项，在使用页面中动态设置
  },
  // @ts-ignore
  storeBindings: {
    mobxStore,
    fields: {
      unreadCount: () => mobxStore.unreadCount,
    },
  },
  methods: {
    switchTab(e: any) {
      const data = e.currentTarget.dataset
      const url = data.path
      this.setData({
        selected: data.index,
      })
      wx.switchTab({ url })
    },
  },
})
