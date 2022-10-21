// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.constant.AppParams;
import com.netease.yunxin.app.medical.constant.CallConfig;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.databinding.FragmentCallBinding;
import com.netease.yunxin.app.medical.http.HttpService;
import com.netease.yunxin.app.medical.manager.CallViewModel;
import com.netease.yunxin.app.medical.ui.activity.CallActivity;
import com.netease.yunxin.app.medical.ui.view.CommonDialog;
import com.netease.yunxin.app.medical.utils.CallTimeOutHelper;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.nertc.pstn.base.PstnCallParam;
import com.netease.yunxin.nertc.pstn.base.PstnFunctionMgr;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import org.json.JSONException;
import org.json.JSONObject;

/** 呼叫 页面UI（主叫被叫） */
public class CallFragment extends BaseFragment {
  private static final String TAG = "CallFragment";
  private CallActivity activity;
  private FragmentCallBinding binding;
  private CallParam callParams;
  private boolean needPstnCall = false;
  private boolean callFinished = true;
  private String calledMobile;
  private String callerUserName;
  private CallViewModel callViewModel;

  @Override
  protected View providerView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = FragmentCallBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    callViewModel = new ViewModelProvider(requireActivity()).get(CallViewModel.class);
    handleCall();
    subscribeUi();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (CallActivity) context;
    handleBackPressed();
  }

  private void subscribeUi() {
    callViewModel.refresh(callParams);
    if (callParams.getChannelType() == ChannelType.AUDIO.getValue()) {
      binding.ivInvitedAccept.setImageResource(R.drawable.icon_call_audio_accept);
    } else {
      binding.ivInvitedAccept.setImageResource(R.drawable.icon_call_accept);
    }
    handleUserInfoUi();
    handleToastEvent();
    handleRingEvent();
    handleCallFinishedEvent();
    handleSmsEvent();
  }

  @Override
  protected void initEvent() {

    binding.ivInviteCancel.setOnClickListener(view -> handleHangupEvent());

    binding.ivInvitedAccept.setOnClickListener(view -> handleInvitedAcceptEvent());

    binding.ivInvitedReject.setOnClickListener(view -> handleHangupEvent());
  }

  private void handleHangupEvent() {
    if (needPstnCall && callParams.getChannelType() == ChannelType.AUDIO.getValue()) {
      PstnFunctionMgr.hangup();
      finishActivity();
    } else {
      activity.rtcHangup(
          new NECallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
              finishActivity();
            }

            @Override
            public void onError(int code, String errorMsg) {
              finishActivity();
            }
          });
    }
    if (callParams.isCalled()) {
      binding.ivInvitedAccept.setEnabled(false);
      binding.ivInvitedReject.setEnabled(false);
    } else {
      binding.ivInviteCancel.setEnabled(false);
    }
  }

  private void handleInvitedAcceptEvent() {
    activity.rtcAccept();
    binding.ivInvitedAccept.setEnabled(false);
    binding.ivInvitedReject.setEnabled(false);
  }

  private void handleUserInfoUi() {
    callViewModel
        .getCallUserInfo()
        .observe(
            getViewLifecycleOwner(),
            callUserInfo -> {
              binding.tvNick.setText(callUserInfo.nickname);
              binding.tvTitle.setText(callUserInfo.title);
              binding.tvSubtitle.setText(callUserInfo.subtitle);
              if (callUserInfo.isCalled) {
                binding.llInvite.setVisibility(View.GONE);
                binding.rlInvited.setVisibility(View.VISIBLE);
              } else {
                binding.llInvite.setVisibility(View.VISIBLE);
                binding.rlInvited.setVisibility(View.GONE);
                binding.tvSubtitle.setTextColor(Color.WHITE);
              }
              if (callUserInfo.role == Role.DOCTOR) {
                binding.ivAvatar.setImageResource(R.drawable.avatar_doctor_four);
                binding.clRoot.setBackgroundResource(R.drawable.bg_call_doctor);
              } else {
                binding.ivAvatar.setImageResource(R.drawable.avatar_sufferer_four);
                binding.clRoot.setBackgroundResource(R.drawable.bg_call_sufferer);
              }
            });
  }

  private void handleCall() {
    callParams = activity.getCallParams();
    if (callParams.getCallExtraInfo() == null) {
      return;
    }
    JSONObject callParamExtraInfo;
    try {
      callParamExtraInfo = new JSONObject(callParams.getCallExtraInfo());
      if (!callParams.isCalled() && callFinished) {
        callFinished = false;
        needPstnCall = callParamExtraInfo.getBoolean(AppParams.NEED_PSTN_CALL);
        calledMobile = callParamExtraInfo.getString(AppParams.CALLED_USER_MOBILE);
        callerUserName = callParamExtraInfo.getString(AppParams.CALLER_USER_NAME);
        if (callParams.getChannelType() == ChannelType.AUDIO.getValue()) {
          if (needPstnCall) {
            pstnCall();
          } else {
            call();
          }
        } else {
          call();
        }
      } else {
        playRing(AVChatSoundPlayer.RingerTypeEnum.RING);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  /** 高接通处理 */
  private void pstnCall() {
    CallTimeOutHelper.configTimeOut(
        CallConfig.CALL_TOTAL_WAIT_TIMEOUT, CallConfig.CALL_PSTN_WAIT_MILLISECONDS);
    PstnCallParam pstnCallParam = new PstnCallParam(callParams, calledMobile, null);
    PstnFunctionMgr.callWithCor(pstnCallParam);
  }

  private void call() {
    CallTimeOutHelper.configTimeOut(
        CallConfig.CALL_TOTAL_WAIT_TIMEOUT, CallConfig.CALL_TOTAL_WAIT_TIMEOUT);
    activity.rtcCall(
        new NECallback<ChannelFullInfo>() {
          @Override
          public void onSuccess(ChannelFullInfo channelFullInfo) {
            callFinished = true;
          }

          @Override
          public void onError(int code, String errorMsg) {
            ToastX.showShortToast(getString(R.string.medical_call_failed));
          }
        });
    LogUtil.i(TAG, "handleCall->rtcCall");
  }

  private void playRing(AVChatSoundPlayer.RingerTypeEnum ring) {
    AVChatSoundPlayer.Companion.instance().play(activity, ring);
  }

  private void handleToastEvent() {
    callViewModel.getToastData().observe(getViewLifecycleOwner(), s -> ToastX.showShortToast(s));
  }

  private void handleRingEvent() {
    callViewModel
        .getPlayRing()
        .observe(getViewLifecycleOwner(), ringerTypeEnum -> playRing(ringerTypeEnum));
  }

  private void handleCallFinishedEvent() {
    callViewModel.getCallFinished().observe(getViewLifecycleOwner(), aBoolean -> finishActivity());
  }

  private void handleSmsEvent() {
    callViewModel.getSendSmsData().observe(getViewLifecycleOwner(), aBoolean -> sendSms());
  }

  private void sendSms() {
    HttpService.sendSms(
        calledMobile,
        callerUserName,
        new NECallback<Void>() {
          @Override
          public void onSuccess(Void unused) {
            LogUtil.i(
                TAG, "sendSms,calledMobile:" + calledMobile + ",callerUserName:" + callerUserName);
          }

          @Override
          public void onError(int code, String errorMsg) {
            LogUtil.e(TAG, "sendSms onError code:" + code);
          }
        });
  }

  @Override
  protected void finishActivity() {
    AVChatSoundPlayer.Companion.instance().stop(getContext());
    super.finishActivity();
  }

  /// 通话中返回处理
  private void handleBackPressed() {
    OnBackPressedCallback callback =
        new OnBackPressedCallback(true) {
          @Override
          public void handleOnBackPressed() {

            CommonDialog dialog = new CommonDialog(getContext());
            dialog
                .setTitle(activity.getString(R.string.medical_end_call))
                .setContent(activity.getString(R.string.medical_sure_end_call))
                .setPositiveOnClickListener(
                    v -> {
                      handleHangupEvent();
                    })
                .show();
          }
        };
    activity
        .getOnBackPressedDispatcher()
        .addCallback(
            this, // LifecycleOwner
            callback);
  }

  @Override
  protected void roleChanged(int role) {}
}
