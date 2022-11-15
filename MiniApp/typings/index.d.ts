import { LOGIN_STATUS, ROLE_TYPE } from '../src/type/index'

interface IAppOption {
  globalData: {
    nim: any
    neCall: any
    hasAccepted?: boolean
    isConnected: boolean
    userInfo?: USER_INFO
    currentRole?: ROLE_TYPE
    loginStatus?: LOGIN_STATUS
    DoctorList?: any
    sessionList: []
  }
  userInfoReadyCallback?: WechatMiniprogram.GetUserInfoSuccessCallback
  initNIM: (data?: USER_INFO) => void
  getTabbarList?: () => void
  onUpdateSession: (data: any) => void
}

interface USER_INFO {
  accountId: string
  accessToken: string
  role?: ROLE_TYPE
  imAccid: string
  imToken: string
  avatar?: string
  nickname: string
  gender?: string
  mobile?: string
}
