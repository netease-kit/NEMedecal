// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.databinding.ActivityAuthSuccessBinding;
import com.netease.yunxin.app.medical.utils.NavUtils;

public class AuthSuccessActivity extends BaseActivity {

  private ActivityAuthSuccessBinding binding;

  private CountDownTimer timer;
  private static final int DURATION = 3000;
  private static final int INTERVAL = 1000;

  @Override
  protected View getContentView() {
    binding = ActivityAuthSuccessBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_auth_name_title);
  }

  @Override
  protected void initView() {}

  @Override
  protected void setEvent() {
    binding.authSuccessSkip.setOnClickListener(
        v -> {
          handleSkip();
        });

    timer =
        new CountDownTimer(DURATION, INTERVAL) {
          @Override
          public void onTick(long millisUntilFinished) {
            SpannableStringBuilder spannableString = new SpannableStringBuilder();
            ForegroundColorSpan colorSpan =
                new ForegroundColorSpan(getResources().getColor(R.color.color_1975FE));
            int sec = (int) (millisUntilFinished / 1000);
            spannableString.append(String.format(getString(R.string.medical_skip_to), sec));
            spannableString.setSpan(colorSpan, 3, 4, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            binding.authSuccessCountdown.setText(spannableString);
          }

          @Override
          public void onFinish() {
            handleSkip();
          }
        };
  }

  private void handleSkip() {
    if (AuthNameActivity.fromPage == AuthNameActivity.FROM_VIDEO) {
      finish();
      if (AuthNameActivity.authNameActivity != null
          && !AuthNameActivity.authNameActivity.isFinishing()) {
        AuthNameActivity.authNameActivity.finish();
      }
      NavUtils.toVideoConsultation(AuthSuccessActivity.this);
    } else {
      finish();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (timer != null) {
      timer.start();
    }
  }

  @Override
  protected void onDestroy() {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    super.onDestroy();
  }
}
