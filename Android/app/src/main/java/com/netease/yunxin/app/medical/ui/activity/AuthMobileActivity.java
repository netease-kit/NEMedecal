// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.view.View;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.databinding.ActivityAuthMobileBinding;

public class AuthMobileActivity extends BaseActivity {

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_auth_mobile_title);
  }

  @Override
  protected View getContentView() {
    ActivityAuthMobileBinding binding = ActivityAuthMobileBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void initView() {}

  @Override
  protected void setEvent() {}
}
