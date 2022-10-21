// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.databinding.ActivityAudioNotifyBinding;
import com.netease.yunxin.app.medical.http.HttpService;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.login.utils.HelperUtils;

public class AudioNotifyActivity extends BaseActivity {

  private ActivityAudioNotifyBinding binding;

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_audio_notify_title);
  }

  @Override
  protected View getContentView() {
    binding = ActivityAudioNotifyBinding.inflate(LayoutInflater.from(this));
    return binding.getRoot();
  }

  protected void initView() {}

  @Override
  protected void setEvent() {
    binding.tvSendAudio.setOnClickListener(v -> sendAudioNotify());
  }

  /// 发送语音通知
  private void sendAudioNotify() {
    String mobile = binding.etNotifyMobile.getText().toString();
    String content = binding.etNotifyContent.getText().toString();
    if (!HelperUtils.INSTANCE.verifyPhoneNumber(mobile)) {
      ToastX.showShortToast(getString(R.string.medical_mobile_error));
    } else if (TextUtils.isEmpty(content)) {
      ToastX.showShortToast(getString(R.string.medical_audio_notify_empty));
    } else {
      HttpService.sendAudioMessage(
          mobile,
          content,
          new NECallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
              ToastX.showShortToast(getString(R.string.medical_send_audio_success));
            }

            @Override
            public void onError(int code, String errorMsg) {
              ToastX.showShortToast(errorMsg);
            }
          });
    }
  }
}
