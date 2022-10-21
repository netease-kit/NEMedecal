// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.view.View;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.databinding.ActivitySettingBinding;
import com.netease.yunxin.app.medical.utils.AppUtils;
import com.netease.yunxin.app.medical.utils.NavUtils;

public class SettingActivity extends BaseActivity {

  private ActivitySettingBinding binding;

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_mine_setting);
  }

  @Override
  protected void initView() {
    binding.tvVersion.setText("V" + AppUtils.getVersionName(this));
  }

  @Override
  protected View getContentView() {
    binding = ActivitySettingBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void setEvent() {
    binding.llPrivacyPolicy.setOnClickListener(
        view ->
            NavUtils.toBrowsePage(
                SettingActivity.this,
                getResources().getString(R.string.medical_privacy_policy),
                AppConstants.PRIVACY_POLICY));
    binding.llUserProtocol.setOnClickListener(
        view ->
            NavUtils.toBrowsePage(
                SettingActivity.this,
                getResources().getString(R.string.medical_user_protocol),
                AppConstants.USER_AGREEMENT));
  }
}
