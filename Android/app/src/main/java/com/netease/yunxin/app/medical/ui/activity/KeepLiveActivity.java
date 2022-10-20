// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.view.View;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.databinding.ActivityKeepLiveBinding;

public class KeepLiveActivity extends BaseActivity {

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_keep_live_title);
  }

  @Override
  protected View getContentView() {
    ActivityKeepLiveBinding binding = ActivityKeepLiveBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void initView() {}

  @Override
  protected void setEvent() {}
}
