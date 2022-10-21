// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.manager;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.constant.AppParams;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.model.CallUserInfo;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.utils.SecondsTimer;
import org.json.JSONException;
import org.json.JSONObject;

public class CallViewModel extends AndroidViewModel {
  private static final String TAG = "CallViewModel";
  private final MutableLiveData<Boolean> switchToInCall = new MutableLiveData<>(); // 切换至通话状态
  private final MutableLiveData<Long> inTheCallDuration = new MutableLiveData<>(); // 通话时长
  private final MutableLiveData<Boolean> remoteVideoMute = new MutableLiveData<>(); // 远端用户关闭摄像头
  private final MutableLiveData<Boolean> remoteAudioMute = new MutableLiveData<>(); // 远端用户关闭麦克风
  private final MutableLiveData<AVChatSoundPlayer.RingerTypeEnum> playRing =
      new MutableLiveData<>();
  private final MutableLiveData<Boolean> sendSmsData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> callFinished = new MutableLiveData<>(); // 呼叫结束
  private MutableLiveData<CallUserInfo> callUserInfo = new MutableLiveData<>();
  private SecondsTimer inTheCallSecondTimer;
  private CallParam callParam;
  private final MutableLiveData<String> toastData = new MutableLiveData<>(); // 提示
  private boolean needPstnCall = false;
  private CallUserInfo userInfo;

  private final NERtcCallDelegate neRtcCallDelegate =
      new NERtcCallDelegate() {

        @Override
        public void onFirstVideoFrameDecoded(@Nullable String userId, int width, int height) {
          super.onFirstVideoFrameDecoded(userId, width, height);
        }

        @Override
        public void onVideoMuted(String userId, boolean isMuted) {
          super.onVideoMuted(userId, isMuted);
          LogUtil.i(TAG, "onVideoMuted,userId:" + userId);
          remoteVideoMute.postValue(isMuted);
        }

        @Override
        public void onAudioMuted(String userId, boolean isMuted) {
          super.onAudioMuted(userId, isMuted);
          LogUtil.i(TAG, "onAudioMuted,userId:" + userId);
          remoteAudioMute.postValue(isMuted);
        }

        @Override
        public void onUserEnter(@Nullable String userId) {
          super.onUserEnter(userId);
          LogUtil.i(TAG, "onUserEnter,userId:" + userId);
          switchToInCall.postValue(true);
          startInTheCallTimer();
        }

        @Override
        public void onCallEnd(@Nullable String userId) {
          cancelInTheCallTimer();
          super.onCallEnd(userId);
          LogUtil.i(TAG, "onCallEnd,userId:" + userId);
        }

        @Override
        public void onCallFinished(@Nullable int code, @Nullable String msg) {
          super.onCallFinished(code, msg);
          callFinished.postValue(true);
          LogUtil.i(TAG, "onCallFinished,code:" + code + ",msg:" + msg);
        }

        @Override
        public void onRejectByUserId(@Nullable String userId) {
          if (!callParam.isCalled()) { // 主叫 被拒接
            toastData.postValue(getApplication().getString(R.string.medical_reject_tips));
            playRing.postValue(AVChatSoundPlayer.RingerTypeEnum.PEER_REJECT);
          }
          super.onRejectByUserId(userId);
          LogUtil.i(TAG, "onRejectByUserId,userId:" + userId);
        }

        @Override
        public void onUserBusy(@Nullable String userId) {
          if (!callParam.isCalled()) { // 被叫忙线
            toastData.postValue(getApplication().getString(R.string.medical_user_busy));
            playRing.postValue(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);
          }
          super.onUserBusy(userId);
          LogUtil.i(TAG, "onUserBusy,userId:" + userId);
        }

        @Override
        public void onCancelByUserId(@Nullable String userId) {
          if (callParam.isCalled()) {
            toastData.postValue(getApplication().getString(R.string.medical_cancel_by_other));
          }
          super.onCancelByUserId(userId);
          LogUtil.i(TAG, "onCancelByUserId,userId:" + userId);
        }

        @Override
        public void timeOut() {
          if (needPstnCall) {
            if (callParam.isCalled()) {
              toastData.postValue(getApplication().getString(R.string.medical_called_timeout_tips));
              super.timeOut();
            }
          } else {
            if (callParam.isCalled()) {
              toastData.postValue(getApplication().getString(R.string.medical_called_timeout_tips));
            } else {
              toastData.postValue(getApplication().getString(R.string.medical_caller_timeout_tips));
              sendSmsData.postValue(true); // 搞接通开启 在接通超时时 发送短信通知给被叫
            }
            super.timeOut();
          }
          LogUtil.i(TAG, "timeOut");
        }
      };

  public CallViewModel(@NonNull Application application) {
    super(application);
    NERTCVideoCall.sharedInstance().addDelegate(neRtcCallDelegate);
  }

  public MutableLiveData<Boolean> getSwitchToInCall() {
    return switchToInCall;
  }

  public MutableLiveData<Long> getInTheCallDuration() {
    return inTheCallDuration;
  }

  public MutableLiveData<Boolean> getRemoteVideoMute() {
    return remoteVideoMute;
  }

  public MutableLiveData<Boolean> getRemoteAudioMute() {
    return remoteAudioMute;
  }

  public MutableLiveData<CallUserInfo> getCallUserInfo() {
    return callUserInfo;
  }

  public MutableLiveData<AVChatSoundPlayer.RingerTypeEnum> getPlayRing() {
    return playRing;
  }

  public MutableLiveData<Boolean> getSendSmsData() {
    return sendSmsData;
  }

  public MutableLiveData<Boolean> getCallFinished() {
    return callFinished;
  }

  public MutableLiveData<String> getToastData() {
    return toastData;
  }

  public void refresh(CallParam callParam) {
    try {
      LogUtil.i(TAG, "callParam:" + callParam);
      this.callParam = callParam;
      JSONObject jsonObject = new JSONObject(callParam.getCallExtraInfo());
      userInfo = new CallUserInfo();
      userInfo.isCalled = callParam.isCalled();
      userInfo.callType = callParam.getChannelType();
      needPstnCall = jsonObject.optBoolean(AppParams.NEED_PSTN_CALL, false);
      if (callParam.isCalled()) { // 当前是被叫，构建主叫信息
        userInfo.mobile = jsonObject.optString(AppParams.CALLER_USER_MOBILE, "");
        userInfo.avatar = jsonObject.optString(AppParams.CALLER_USER_AVATAR, "");
        userInfo.role = jsonObject.getInt(AppParams.CALLER_USER_ROLE);
        if (userInfo.callType == ChannelType.AUDIO.getValue()) {
          userInfo.title = getApplication().getString(R.string.medical_invited_audio_title);
        } else {
          userInfo.title = getApplication().getString(R.string.medical_invited_video_title);
        }
        if (userInfo.role == Role.DOCTOR) { /// 主叫是医生
          userInfo.subtitle = getApplication().getString(R.string.medical_invited_subtitle);
          userInfo.nickname = getApplication().getString(R.string.medical_call_doctor_name);
        } else {
          userInfo.nickname = getApplication().getString(R.string.medical_call_sufferer_name);
        }
        userInfo.accId = callParam.getCallerAccId();
      } else { // 当前是主叫，构建被叫信息
        userInfo.nickname = jsonObject.getString(AppParams.CALLED_USER_NAME);
        userInfo.mobile = jsonObject.getString(AppParams.CALLED_USER_MOBILE);
        userInfo.avatar = jsonObject.getString(AppParams.CALLED_USER_AVATAR);
        userInfo.title = getApplication().getString(R.string.medical_connecting);
        userInfo.role = jsonObject.getInt(AppParams.CALLED_USER_ROLE);
        if (userInfo.role == Role.DOCTOR) {
          userInfo.nickname = getApplication().getString(R.string.medical_call_doctor_name);
        } else {
          userInfo.nickname = getApplication().getString(R.string.medical_call_sufferer_name);
        }
        userInfo.accId = callParam.getCalledAccIdList().get(0);
      }
      callUserInfo.postValue(userInfo);
    } catch (JSONException e) {
      e.printStackTrace();
      LogUtil.e(TAG, "json parse error,e:" + e);
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    cancelInTheCallTimer();
    NERTCVideoCall.sharedInstance().removeDelegate(neRtcCallDelegate);
  }

  private void startInTheCallTimer() {
    if (inTheCallSecondTimer == null) {
      inTheCallSecondTimer = new SecondsTimer(0, 1000);
    }
    inTheCallSecondTimer.start(
        aLong -> {
          LogUtil.i(TAG, "startInTheCallTimer aLong:" + aLong);
          inTheCallDuration.postValue(aLong);
          return null;
        });
  }

  private void cancelInTheCallTimer() {
    if (inTheCallSecondTimer != null) {
      inTheCallSecondTimer.cancel();
      inTheCallSecondTimer = null;
    }
  }
}
