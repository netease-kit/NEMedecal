// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.manager;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.pstn.base.AbsPstnCallback;
import com.netease.yunxin.nertc.pstn.base.PstnFunctionMgr;
import com.netease.yunxin.nertc.ui.base.ResultInfo;
import com.netease.yunxin.nertc.ui.utils.SecondsTimer;

public class PstnCallViewModel extends AndroidViewModel {

  public static final String TAG = "PSTNCallViewModel";
  private SecondsTimer inThePstnCallSecondTimer;
  private final MutableLiveData<Long> pstnCallUsedDuration = new MutableLiveData<>(); // 通话时长
  private final MutableLiveData<Boolean> switchToInAudio = new MutableLiveData<>(); // 切换语音呼叫页面
  private final MutableLiveData<String> pstnToast = new MutableLiveData<>();
  private final MutableLiveData<Boolean> releaseAndFinish = new MutableLiveData<>();
  private final MutableLiveData<Boolean> sendSmsData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> rtcCallResult = new MutableLiveData<>();
  private static final int REJECT_CODE = 1000;

  public MutableLiveData<Long> getPstnCallUsedDuration() {
    return pstnCallUsedDuration;
  }

  public MutableLiveData<String> getPstnToast() {
    return pstnToast;
  }

  public MutableLiveData<Boolean> getReleaseAndFinish() {
    return releaseAndFinish;
  }

  public MutableLiveData<Boolean> getSendSmsData() {
    return sendSmsData;
  }

  public MutableLiveData<Boolean> getRtcCallResult() {
    return rtcCallResult;
  }

  public MutableLiveData<Boolean> getSwitchToInAudio() {
    return switchToInAudio;
  }

  private AbsPstnCallback callback =
      new AbsPstnCallback() {
        @Override
        public void onDirectCallAccept(int code) {
          super.onDirectCallAccept(code);
          startInTheCallTimer();
          switchToInAudio.postValue(true);
        }

        @Override
        public void onDirectCallDisconnectWithError(int code, @Nullable String errorMsg) {
          LogUtil.i(TAG, "onDirectCallDisconnectWithError,code:" + code + ",errorMsg:" + errorMsg);
          releaseAndFinish.postValue(true);
        }

        @Override
        public void onDirectCallHangupWithReason(
            int reason, int code, @Nullable String errorMsg, boolean isCallEstablished) {
          LogUtil.i(
              TAG,
              "onDirectCallHangupWithReason:reason:"
                  + reason
                  + ",code:"
                  + code
                  + ",errorMsg:"
                  + errorMsg
                  + ",isCallEstablished:"
                  + isCallEstablished);
          if (reason == REJECT_CODE && !isCallEstablished) {
            //对方拒接，自己挂断
            pstnToast.postValue(getApplication().getString(R.string.medical_reject_tips));
          }
          releaseAndFinish.postValue(true);
        }

        @Override
        public void onTimeOutWithPstn() {
          super.onTimeOutWithPstn();
          LogUtil.i(TAG, "onTimeOutWithPstn");
          releaseAndFinish.postValue(true);
          sendSmsData.postValue(true);
        }

        @Override
        public void onDirectCallRing(int code) {
          LogUtil.i(TAG, "onDirectCallRing,code:" + code);
        }

        @Override
        public void onDirectStartCall(int code, @Nullable String errorMsg) {
          LogUtil.i(TAG, "onDirectStartCall,code:" + code + ",errorMsg:" + errorMsg);
        }

        @Override
        public void onRtcCallResult(@NonNull ResultInfo<ChannelFullInfo> result) {
          LogUtil.i(TAG, "onRtcCallHangupResult,result:" + result);
          rtcCallResult.postValue(true);
          if (result.getSuccess()) {
            return;
          }

          if (result.getMsg() != null
              && result.getMsg().getCode() == ResponseCode.RES_PEER_NIM_OFFLINE) {
            return;
          }
          pstnToast.postValue(getApplication().getString(R.string.medical_call_failed));
          if (NERTCVideoCall.sharedInstance().getCurrentState() == CallState.STATE_IDLE) {
            releaseAndFinish.postValue(true);
          }
        }

        @Override
        public void onTransError(@NonNull ResultInfo<?> result) {
          LogUtil.i(TAG, "onTransError,result:" + result);
          releaseAndFinish.postValue(true);
        }
      };

  public PstnCallViewModel(@NonNull Application application) {
    super(application);
    PstnFunctionMgr.addCallback(callback);
  }

  public void startInTheCallTimer() {
    if (inThePstnCallSecondTimer == null) {
      inThePstnCallSecondTimer = new SecondsTimer(0, 1000);
    }
    inThePstnCallSecondTimer.start(
        aLong -> {
          LogUtil.i(TAG, "startInTheCallTimer aLong:" + aLong);
          pstnCallUsedDuration.postValue(aLong);
          return null;
        });
  }

  public void cancelInTheTimer() {
    if (inThePstnCallSecondTimer != null) {
      inThePstnCallSecondTimer.cancel();
      inThePstnCallSecondTimer = null;
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    PstnFunctionMgr.removeCallback(callback);
    cancelInTheTimer();
  }
}
