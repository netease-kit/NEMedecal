import version from './version'

const LogManager = wx.getRealtimeLogManager ? wx.getRealtimeLogManager() : null

const LevelMap = {
  log: 0,
  trace: 1,
  debug: 2,
  info: 3,
  warn: 4,
  error: 5,
}

class LogDebug {
  constructor({
    level = 'log',
    appName = '',
    version = '',
    debug = false,
  } = {}) {
    this.level = LevelMap[level]
    this.appName = appName
    this.version = version
    this.enable = debug
  }
  // 上报日志
  reportLog(...msgs) {
    this._print('log', ...msgs)
    if (LogManager) {
      LogManager.info.apply(LogManager, arguments) // LogManager暂不支持log用info代替
    }
  }
  reportWarn(...msgs) {
    this._print('warn', ...msgs)
    if (LogManager) {
      LogManager.warn.apply(LogManager, arguments)
    }
  }
  reportError(...msgs) {
    this._print('error', ...msgs)
    if (LogManager) {
      LogManager.error.apply(LogManager, arguments)
    }
  }
  reportDebug(...msgs) {
    this._print('debug', ...msgs)
    if (LogManager) {
      LogManager.debug.apply(LogManager, arguments)
    }
  }
  reportInfo(...msgs) {
    this._print('info', ...msgs)
    if (LogManager) {
      LogManager.info.apply(LogManager, arguments)
    }
  }
  // 普通日志
  log(...msgs) {
    this._print('log', ...msgs)
  }
  trace(...msgs) {
    this._print('trace', ...msgs)
  }
  debug(...msgs) {
    this._print('debug', ...msgs)
  }
  info(...msgs) {
    this._print('info', ...msgs)
  }
  warn(...msgs) {
    this._print('warn', ...msgs)
  }
  error(...msgs) {
    this._print('error', ...msgs)
  }
  _print(funcName, ...msgs) {
    if (LevelMap[funcName] >= this.level && console[funcName] && this.enable) {
      console[funcName](
        `[ ${this.appName} ${this.version} ${this._genTime()} ]`,
        ...msgs
      )
    }
  }
  _genTime() {
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1
    const day = now.getDate()
    const hour = now.getHours() < 10 ? `0${now.getHours()}` : now.getHours()
    const min =
      now.getMinutes() < 10 ? `0${now.getMinutes()}` : now.getMinutes()
    const s = now.getSeconds() < 10 ? `0${now.getSeconds()}` : now.getSeconds()
    const nowString = `${year}-${month}-${day} ${hour}:${min}:${s}`
    return nowString
  }
  setFilterMsg(msg) {
    // 从基础库2.7.3开始支持
    if (!LogManager || !LogManager.setFilterMsg) return
    if (typeof msg !== 'string') return
    LogManager.setFilterMsg(msg)
  }
  addFilterMsg(msg) {
    // 从基础库2.8.1开始支持
    if (!LogManager || !LogManager.addFilterMsg) return
    if (typeof msg !== 'string') return
    LogManager.addFilterMsg(msg)
  }
}

export const logger = new LogDebug({
  appName: 'App-Medicine-Miniapp',
  version,
  debug: true,
})

export default LogDebug
