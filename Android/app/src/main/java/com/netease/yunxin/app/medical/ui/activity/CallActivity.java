// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.constant.ErrorCode;
import com.netease.yunxin.app.medical.manager.CallViewModel;
import com.netease.yunxin.app.medical.manager.PstnCallViewModel;
import com.netease.yunxin.app.medical.ui.fragment.CallFragment;
import com.netease.yunxin.app.medical.ui.fragment.InAudioCallFragment;
import com.netease.yunxin.app.medical.ui.fragment.InVideoCallFragment;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.netease.yunxin.nertc.nertcvideocall.model.JoinChannelCallBack;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.base.CommonCallActivity;
import java.util.Arrays;
import java.util.List;

/** */
public class CallActivity extends CommonCallActivity {
  private static final String TAG = "CallActivity";
  private CallParam callParam;
  private String[] permissions = {
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.READ_PHONE_STATE
  };
  private CallViewModel viewModel;
  private PstnCallViewModel pstnCallViewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    adapterStatusBar();
    super.onCreate(savedInstanceState);
    callParam = getCallParam();
    applyPermission();
  }

  /** 申请权限 */
  private void applyPermission() {
    boolean isGranted = PermissionUtils.hasPermissions(this, permissions);
    if (!isGranted) {
      Permission.requirePermissions(this, permissions)
          .request(
              new Permission.PermissionCallback() {
                @Override
                public void onGranted(List<String> permissionsGranted) {
                  if (permissionsGranted.containsAll(Arrays.asList(permissions))) { // 权限通过
                    initView();
                  }
                }

                @Override
                public void onDenial(
                    List<String> permissionsDenial, List<String> permissionDenialForever) {
                  ToastX.showShortToast(getString(R.string.medical_permission_request_failed_tips));
                  releaseAndFinish(true);
                }

                @Override
                public void onException(Exception e) {
                  ToastX.showShortToast(getString(R.string.medical_permission_request_failed_tips));
                  releaseAndFinish(true);
                }
              });
    } else {
      initView();
    }
  }

  // 展示呼叫页面
  private void showCallFragment() {
    getSupportFragmentManager()
        .beginTransaction()
        .setReorderingAllowed(true)
        .add(R.id.fragment_container_view, CallFragment.class, null)
        .commit();
  }

  private void switchToInCall() {
    stopRing();
    Fragment fragment;
    if (callParam.getChannelType() == ChannelType.AUDIO.getValue()) {
      fragment = new InAudioCallFragment();
    } else {
      fragment = new InVideoCallFragment();
    }
    getSupportFragmentManager()
        .beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fragment_container_view, fragment)
        .commit();
  }

  public CallParam getCallParams() {
    return callParam;
  }

  public NERTCVideoCall getRtcCall() {
    return getVideoCall();
  }

  /** 处理获取权限之后 页面展示 */
  private void initView() {
    if (callParam.isCalled() && getVideoCall().getCurrentState() == CallState.STATE_IDLE) {
      releaseAndFinish(false);
      return;
    }
    viewModel = new ViewModelProvider(this).get(CallViewModel.class);
    pstnCallViewModel = new ViewModelProvider(this).get(PstnCallViewModel.class);
    showCallFragment();
    viewModel.getSwitchToInCall().observe(CallActivity.this, b -> switchToInCall());
    pstnCallViewModel.getSwitchToInAudio().observe(this, aBoolean -> switchToInCall());
  }

  public void rtcCall(NECallback<ChannelFullInfo> callback) {
    doCall(
        new JoinChannelCallBack() {
          @Override
          public void onJoinChannel(ChannelFullInfo channelFullInfo) {
            LogUtil.i(TAG, "rtcCall onJoinChannel");
            callback.onSuccess(channelFullInfo);
          }

          @Override
          public void onJoinFail(String msg, int code) {
            LogUtil.e(TAG, "rtcCall,onJoinFail msg:" + msg + ",code:" + code);
            callback.onError(code, msg);
          }
        });
  }

  public void rtcAccept() {
    doAccept(
        new JoinChannelCallBack() {
          @Override
          public void onJoinChannel(ChannelFullInfo channelFullInfo) {
            LogUtil.i(TAG, "rtcAccept onJoinChannel");
          }

          @Override
          public void onJoinFail(String msg, int code) {
            LogUtil.e(TAG, "rtcAccept,onJoinFail msg:" + msg + ",code:" + code);
            if (code == ErrorCode.JOIN_FAIL) {
              ToastX.showShortToast(getString(R.string.medical_accept_error));
            } else {
              ToastX.showShortToast(getString(R.string.medical_called_timeout_tips));
            }
            stopRing();
          }
        });
  }

  public void rtcHangup(NECallback<Integer> callback) {
    doHangup(
        new RequestCallbackWrapper<Void>() {
          @Override
          public void onResult(int code, Void result, Throwable exception) {
            LogUtil.i(TAG, "rtcHangup,code:" + code + ",exception:" + exception);
            callback.onSuccess(code);
          }
        });
  }

  /** 停止响铃 */
  private void stopRing() {
    AVChatSoundPlayer.Companion.instance().stop(CallActivity.this);
  }

  @Override
  public void finish() {
    super.finish();
  }

  private void adapterStatusBar() {
    // 5.0以上系统状态栏透明
    Window window = getWindow();
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    //会让应用的主体内容占用系统状态栏的空间
    window
        .getDecorView()
        .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    //将状态栏设置成透明色
    window.setStatusBarColor(Color.TRANSPARENT);
  }

  @Override
  protected int provideLayoutId() {
    return R.layout.activity_call;
  }

  @Override
  public void onBackPressed() {
    getOnBackPressedDispatcher().onBackPressed();
  }
}
