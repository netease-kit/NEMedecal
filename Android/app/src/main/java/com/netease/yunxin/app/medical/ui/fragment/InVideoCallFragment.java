// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.content.Context;
import android.graphics.Outline;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.constant.AppParams;
import com.netease.yunxin.app.medical.constant.CallConfig;
import com.netease.yunxin.app.medical.databinding.InVideoCallFragmentBinding;
import com.netease.yunxin.app.medical.manager.CallViewModel;
import com.netease.yunxin.app.medical.ui.activity.CallActivity;
import com.netease.yunxin.app.medical.ui.view.CommonDialog;
import com.netease.yunxin.app.medical.ui.view.InTheVideoCallBottomBar;
import com.netease.yunxin.app.medical.utils.DateFormatUtils;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.ui.base.CallParam;
import org.json.JSONException;
import org.json.JSONObject;

public class InVideoCallFragment extends BaseFragment {

  private static final String TAG = "InVideoCallFragment";
  private static final long SURPLUS_TIME = 60; // 剩余60s 提示
  private InVideoCallFragmentBinding binding;
  private CallViewModel callViewModel;
  private CallActivity activity;
  private NERTCVideoCall rtcCall;
  private CallParam callParam;
  private boolean isLocalMuteAudio = false;
  private boolean isLocalMuteVideo = false;
  private String callUserId; // 对端通话用户的id
  private boolean isSelfInSmall = true; // 自己是否在小屏
  private boolean isTip = false; // 体验时间 提示
  private boolean isRemoteMuteAudio = false;
  private boolean isRemoteMuteVideo = false;

  @Override
  protected View providerView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = InVideoCallFragmentBinding.inflate(getLayoutInflater(), container, false);
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    callViewModel = new ViewModelProvider(requireActivity()).get(CallViewModel.class);
    rtcCall = activity.getRtcCall();
    callParam = activity.getCallParams();
    initAudioVideo();
    subscribeUI();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (CallActivity) context;
    handleBackPressed();
  }

  private void subscribeUI() {
    callViewModel.refresh(callParam);
    LifecycleOwner viewLifecycleOwner = getViewLifecycleOwner();
    callViewModel
        .getCallUserInfo()
        .observe(
            viewLifecycleOwner,
            callUserInfo -> {
              callUserId = callUserInfo.accId;
              rtcCall.setupRemoteView(binding.bigVideo, callUserId);
              rtcCall.setupLocalView(binding.smallVideo);
              binding.tvNickname.setText(callUserInfo.nickname);
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

    callViewModel
        .getRemoteVideoMute()
        .observe(
            viewLifecycleOwner,
            aBoolean -> {
              isRemoteMuteVideo = aBoolean;
              handleUI();
            });
  }

  @Override
  protected void initEvent() {
    binding.flSmallVideo.setOnClickListener(
        view -> {
          isSelfInSmall = !isSelfInSmall;
          switchVideosCanvas(isSelfInSmall);
          handleUI();
        });
    binding.bottomBar.setOnItemClickListener(
        new InTheVideoCallBottomBar.OnItemClickListener() {
          @Override
          public void onMicroPhoneButtonClick() {
            isLocalMuteAudio = !isLocalMuteAudio;
            rtcCall.muteLocalAudio(isLocalMuteAudio);
            handleAudioIconState();
            handleUI();
          }

          @Override
          public void onVideoButtonClick() {
            isLocalMuteVideo = !isLocalMuteVideo;
            rtcCall.muteLocalVideo(isLocalMuteVideo);
            handleVideoIconState();
            handleUI();
          }

          @Override
          public void onSwitchCameraButtonClick() {
            rtcCall.switchCamera();
          }

          @Override
          public void onVirtualButtonClick() {
            ToastX.showShortToast(getString(R.string.medical_virtual_bg_tips));
          }

          @Override
          public void onHangupButtonClick() {
            handleHangupEvent();
          }
        });
  }

  /// 初始化音视频初始状态
  private void initAudioVideo() {
    try {
      JSONObject jsonObject = new JSONObject(callParam.getCallExtraInfo());
      isLocalMuteAudio = !jsonObject.getBoolean(AppParams.OPEN_AUDIO);
      isLocalMuteVideo = !jsonObject.getBoolean(AppParams.OPEN_VIDEO);
      rtcCall.muteLocalAudio(isLocalMuteAudio);
      rtcCall.muteLocalVideo(isLocalMuteVideo);
      handleVideoIconState();
      handleAudioIconState();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void handleAudioIconState() {
    if (isLocalMuteAudio) {
      binding
          .bottomBar
          .getViewBinding()
          .ivMicrophone
          .setImageResource(R.drawable.icon_close_microphone);
    } else {
      binding.bottomBar.getViewBinding().ivMicrophone.setImageResource(R.drawable.icon_microphone);
    }
  }

  private void handleVideoIconState() {
    if (isLocalMuteVideo) {
      binding.bottomBar.getViewBinding().ivVideo.setImageResource(R.drawable.icon_close_call_video);
    } else {
      binding.bottomBar.getViewBinding().ivVideo.setImageResource(R.drawable.icon_call_video);
    }
  }

  private void handleUI() {
    handleSmallVideoUi();
    handleBigVideoUi();
  }

  private void handleBigVideoUi() {
    LogUtil.i(
        TAG,
        "handleBigVideoUi isSelfInSmall:"
            + isSelfInSmall
            + ",isLocalMuteVideo:"
            + isLocalMuteVideo
            + ",isRemoteMuteVideo:"
            + isRemoteMuteVideo);
    if (!isSelfInSmall) {
      //大屏是自己
      showBigVideoView(!isLocalMuteVideo, true);
    } else {
      //大屏是对方
      showBigVideoView(!isRemoteMuteVideo, false);
    }
  }

  private void showBigVideoView(boolean show, boolean isSelf) {
    if (show) {
      binding.bigVideo.setVisibility(View.VISIBLE);
      binding.tvBigVideoDesc.setVisibility(View.GONE);
    } else {
      binding.bigVideo.setVisibility(View.GONE);
      binding.tvBigVideoDesc.setVisibility(View.VISIBLE);
      if (isSelf) {
        binding.tvBigVideoDesc.setText(R.string.medical_already_close_camera);
      } else {
        binding.tvBigVideoDesc.setText(R.string.medical_other_already_close_camera);
      }
    }
  }

  private void handleSmallVideoUi() {
    LogUtil.i(
        TAG,
        "handleSmallVideoUi localVideoIsSmall:"
            + isSelfInSmall
            + ",isLocalMuteVideo:"
            + isLocalMuteVideo
            + ",isRemoteMuteVideo:"
            + isRemoteMuteVideo);
    if (isSelfInSmall) {
      //小屏是自己
      if (!isLocalMuteVideo) {
        // 摄像头打开
        binding.smallVideo.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setVisibility(View.GONE);
      } else {
        // 摄像头关闭
        binding.smallVideo.setVisibility(View.GONE);
        binding.tvSmallVideoDesc.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setText(R.string.medical_already_close_camera);
      }
    } else {
      //小屏是对方
      if (!isRemoteMuteVideo) {
        // 摄像头打开
        binding.smallVideo.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setVisibility(View.GONE);
      } else {
        // 摄像头关闭
        binding.smallVideo.setVisibility(View.GONE);
        binding.tvSmallVideoDesc.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setText(R.string.medical_other_already_close_camera);
      }
    }
    binding.smallVideo.setClipToOutline(true);
    binding.smallVideo.setOutlineProvider(
        new ViewOutlineProvider() {
          @Override
          public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 20f);
          }
        });
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

  // 处理挂断
  private void handleHangupEvent() {
    activity.rtcHangup(
        new NECallback<Integer>() {
          @Override
          public void onSuccess(Integer integer) {}

          @Override
          public void onError(int code, String errorMsg) {}
        });
    finishActivity();
  }

  // 大小video 视图切换
  private void switchVideosCanvas(boolean isSelfInSmallUi) {
    if (isSelfInSmallUi) {
      rtcCall.setupLocalView(binding.smallVideo);
      rtcCall.setupRemoteView(binding.bigVideo, callUserId);
    } else {
      rtcCall.setupLocalView(binding.bigVideo);
      rtcCall.setupRemoteView(binding.smallVideo, callUserId);
    }
  }

  @Override
  protected void roleChanged(int role) {}
}
