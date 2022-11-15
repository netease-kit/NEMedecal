export function secondToDate(result) {
  let h = Math.floor(result / 3600)
  let m = Math.floor((result / 60) % 60)
  let s = Math.floor(result % 60)
  if (s < 10) {
    s = '0' + s
  }
  if (m < 10) {
    m = '0' + m
  }
  if (h === 0) {
    return m + ':' + s
  }
  if (h < 10) {
    h = '0' + h
  }
  return h + ':' + m + ':' + s
}

export const toast = (title, icon = 'none', duration, options) => {
  return new Promise((resolve, reject) => {
    wx.showToast({
      title: title || '',
      icon: icon,
      image: (options && options.image) || '',
      duration: duration || 2000,
      mask: false,
      success: function (res) {
        setTimeout(() => {
          resolve(res)
        }, 1500)
      },
      fail: function (err) {
        reject(err)
      },
    })
  })
}
