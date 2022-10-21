// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.model;

import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.medical.constant.AppParams;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.manager.UserInfoManager;
import org.json.JSONException;
import org.json.JSONObject;

public class CallPageParams {

  public UserModel calledUser;

  public int callRole; // 主叫角色

  /// 是否开启高接通
  public boolean needPSTNCall = false;

  /// 是否开启音频
  public boolean isOpenAudio = true;

  /// 是否你开启视频
  public boolean isOpenVideo = true;

  public ChannelType channelType;

  public CallPageParams(
      UserModel calledUser,
      int callRole,
      boolean isOpenAudio,
      boolean isOpenVideo,
      ChannelType channelType) {
    this.calledUser = calledUser;
    this.callRole = callRole;
    this.isOpenAudio = isOpenAudio;
    this.isOpenVideo = isOpenVideo;
    this.channelType = channelType;
  }

  public CallPageParams(UserModel calledUser, int callRole) {
    this.calledUser = calledUser;
    this.callRole = callRole;
  }

  public CallPageParams(
      UserModel calledUser,
      int callRole,
      boolean needPSTNCall,
      boolean isOpenAudio,
      boolean isOpenVideo,
      ChannelType channelType) {
    this.calledUser = calledUser;
    this.channelType = channelType;
    this.callRole = callRole;
    this.needPSTNCall = needPSTNCall;
    this.isOpenAudio = isOpenAudio;
    this.isOpenVideo = isOpenVideo;
  }

  public String toJson() {
    JSONObject extraInfo = new JSONObject();
    try {
      extraInfo.putOpt(AppParams.CALLER_USER_NAME, UserInfoManager.getSelfUserInfo().getNickname());
      extraInfo.putOpt(AppParams.CALLER_USER_MOBILE, UserInfoManager.getSelfUserInfo().getUser());
      extraInfo.putOpt(AppParams.CALLER_USER_AVATAR, UserInfoManager.getSelfUserInfo().getAvatar());
      extraInfo.putOpt(AppParams.CALLER_USER_ROLE, callRole);
      if (callRole == Role.DOCTOR) {
        extraInfo.putOpt(AppParams.CALLED_USER_ROLE, Role.SUFFERER);
      } else {
        extraInfo.putOpt(AppParams.CALLED_USER_ROLE, Role.DOCTOR);
      }
      extraInfo.putOpt(AppParams.CALLED_USER_NAME, calledUser.nickname);
      extraInfo.putOpt(AppParams.CALLED_USER_MOBILE, calledUser.mobile);
      extraInfo.putOpt(AppParams.CALLED_USER_AVATAR, calledUser.avatar);
      extraInfo.putOpt(AppParams.NEED_PSTN_CALL, needPSTNCall);
      extraInfo.putOpt(AppParams.OPEN_AUDIO, isOpenAudio);
      extraInfo.putOpt(AppParams.OPEN_VIDEO, isOpenVideo);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return extraInfo.toString();
  }
}
