// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.view.View;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.databinding.ActivityLoginHomeBinding;
import com.netease.yunxin.app.medical.utils.NavUtils;

public class LoginHomeActivity extends BaseActivity {
  private ActivityLoginHomeBinding binding;
  private static final String TAG = "SplashActivity";
  public static LoginHomeActivity activity;

  @Override
  protected boolean hasTitle() {
    return false;
  }

  @Override
  protected View getContentView() {
    activity = this;
    binding = ActivityLoginHomeBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  protected void initView() {}

  @Override
  protected void setEvent() {
    binding.verificationCode.setOnClickListener(
        view -> {
          NavUtils.toLoginPage(LoginHomeActivity.this);
        });
  }
}
