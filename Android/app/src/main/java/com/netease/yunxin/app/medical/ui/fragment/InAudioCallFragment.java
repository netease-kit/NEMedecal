// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.constant.AppParams;
import com.netease.yunxin.app.medical.constant.CallConfig;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.databinding.InAudioCallFragmentBinding;
import com.netease.yunxin.app.medical.manager.CallViewModel;
import com.netease.yunxin.app.medical.manager.PstnCallViewModel;
import com.netease.yunxin.app.medical.ui.activity.CallActivity;
import com.netease.yunxin.app.medical.ui.view.CommonDialog;
import com.netease.yunxin.app.medical.ui.view.InTheAudioCallBottomBar;
import com.netease.yunxin.app.medical.utils.DateFormatUtils;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.pstn.base.PstnFunctionMgr;
import com.netease.yunxin.nertc.ui.base.CallParam;
import org.json.JSONException;
import org.json.JSONObject;

public class InAudioCallFragment extends BaseFragment {

  private static final String TAG = "InAudioCallFragment";
  private InAudioCallFragmentBinding binding;
  private CallViewModel callViewModel;
  private PstnCallViewModel pstnCallViewModel;
  private CallActivity activity;
  private boolean isAudioMute = false;
  private boolean isTip = false; // 体验时间 提示
  private static final long SURPLUS_TIME = 60; // 剩余60s 提示
  private CallParam callParam;
  private NERTCVideoCall rtcCall;

  @Override
  protected View providerView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = InAudioCallFragmentBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    callViewModel = new ViewModelProvider(requireActivity()).get(CallViewModel.class);
    pstnCallViewModel = new ViewModelProvider(requireActivity()).get(PstnCallViewModel.class);
    callParam = activity.getCallParams();
    rtcCall = activity.getRtcCall();
    initAudio();
    subscribeUi();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (CallActivity) context;
    handleBackPressed();
  }

  private void subscribeUi() {
    callViewModel.refresh(activity.getCallParams());
    LifecycleOwner viewLifecycleOwner = getViewLifecycleOwner();
    callViewModel
        .getCallUserInfo()
        .observe(
            viewLifecycleOwner,
            callUserInfo -> {
              binding.tvNick.setText(callUserInfo.nickname);
              if (callUserInfo.role == Role.DOCTOR) {
                binding.ivAvatar.setImageResource(R.drawable.avatar_doctor_four);
                binding.clRoot.setBackgroundResource(R.drawable.bg_call_doctor);
              } else {
                binding.ivAvatar.setImageResource(R.drawable.avatar_sufferer_four);
                binding.clRoot.setBackgroundResource(R.drawable.bg_call_sufferer);
              }
            });
    callViewModel
        .getInTheCallDuration()
        .observe(
            viewLifecycleOwner,
            aLong -> {
              binding.tvDuration.setText(DateFormatUtils.long2StrHS(aLong * 1000));
              if (!isTip && (CallConfig.CALL_TOTAL_SECOND_DURATION - aLong <= SURPLUS_TIME)) {
                isTip = true;
                Toast toast =
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.medical_surplus_time_tip),
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
              } else if (aLong - CallConfig.CALL_TOTAL_SECOND_DURATION >= 0) {
                handleHangupEvent();
              }
            });

    callViewModel
        .getCallFinished()
        .observe(
            viewLifecycleOwner,
            aBoolean -> {
              ToastX.showShortToast(getString(R.string.medical_other_end_call));
              finishActivity();
            });

    pstnCallViewModel
        .getPstnCallUsedDuration()
        .observe(
            viewLifecycleOwner,
            aLong -> {
              binding.tvDuration.setText(DateFormatUtils.long2StrHS(aLong * 1000));
              if (!isTip && (CallConfig.CALL_TOTAL_SECOND_DURATION - aLong <= SURPLUS_TIME)) {
                isTip = true;
                ToastX.showShortToast(getString(R.string.medical_surplus_time_tip));
              } else if (aLong - CallConfig.CALL_TOTAL_SECOND_DURATION >= 0) {
                handleHangupEvent();
              }
            });
    pstnCallViewModel.getPstnToast().observe(viewLifecycleOwner, s -> ToastX.showShortToast(s));
    pstnCallViewModel
        .getReleaseAndFinish()
        .observe(viewLifecycleOwner, aBoolean -> finishActivity());
  }

  @Override
  protected void initEvent() {
    binding.bottomBar.setOnItemClickListener(
        new InTheAudioCallBottomBar.OnItemClickListener() {
          @Override
          public void onMicroPhoneButtonClick() {
            isAudioMute = !isAudioMute;
            activity.getRtcCall().muteLocalAudio(isAudioMute);
            handleAudioState();
          }

          @Override
          public void onHangupButtonClick() {
            handleHangupEvent();
          }
        });
  }

  private void handleAudioState() {
    if (isAudioMute) {
      binding
          .bottomBar
          .getViewBinding()
          .ivMicrophone
          .setImageResource(R.drawable.icon_close_microphone);
    } else {
      binding.bottomBar.getViewBinding().ivMicrophone.setImageResource(R.drawable.icon_microphone);
    }
  }

  /// 初始化音视频初始状态
  private void initAudio() {
    try {
      JSONObject jsonObject = new JSONObject(callParam.getCallExtraInfo());
      isAudioMute = !jsonObject.getBoolean(AppParams.OPEN_AUDIO);
      rtcCall.muteLocalAudio(isAudioMute);
      handleAudioState();
    } catch (JSONException e) {
      e.printStackTrace();
    }
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

  /// 挂断处理
  private void handleHangupEvent() {
    CallParam callParams = activity.getCallParams();
    if (callParams.isCalled()) {
      activity.rtcHangup(
          new NECallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}

            @Override
            public void onError(int code, String errorMsg) {}
          });
    } else {
      try {
        JSONObject jsonObject = new JSONObject(callParams.getCallExtraInfo());
        if (jsonObject.getBoolean(AppParams.NEED_PSTN_CALL)) {
          PstnFunctionMgr.hangup();
        } else {
          activity.rtcHangup(
              new NECallback<Integer>() {
                @Override
                public void onSuccess(Integer integer) {}

                @Override
                public void onError(int code, String errorMsg) {}
              });
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    finishActivity();
  }

  @Override
  protected void roleChanged(int role) {}
}
