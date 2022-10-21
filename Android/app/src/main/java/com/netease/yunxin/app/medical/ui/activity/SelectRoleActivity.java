// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.databinding.ActivitySelectRoleBinding;
import com.netease.yunxin.app.medical.manager.UserInfoManager;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.app.medical.utils.MocDataUtil;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.app.medical.utils.SpUtils;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;

/** 选择角色页面 */
public class SelectRoleActivity extends BaseActivity {
  private ActivitySelectRoleBinding binding;
  public static final String TAG = "SelectRoleActivity";

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_select_role_title);
  }

  @Override
  protected View getContentView() {
    binding = ActivitySelectRoleBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    binding.llDoctor.setSelected(true);
  }

  @Override
  protected void setEvent() {
    binding.llDoctor.setOnClickListener(
        v -> {
          binding.llSufferer.setSelected(false);
          binding.llDoctor.setSelected(true);
        });
    binding.llSufferer.setOnClickListener(
        v -> {
          binding.llSufferer.setSelected(true);
          binding.llDoctor.setSelected(false);
        });
    binding.selectSure.setOnClickListener(
        v -> {
          updateRole(binding.llDoctor.isSelected());
        });
  }

  private void updateRole(boolean isDoctor) {
    int role = Role.DOCTOR;
    if (!isDoctor) {
      role = Role.SUFFERER;
    }
    int finalRole = role;
    UserInfoManager.updateUserInfo(
        MocDataUtil.getRandomPerson(),
        role,
        new NECallback() {
          @Override
          public void onSuccess(Object o) {
            NavUtils.toMainPage(SelectRoleActivity.this, finalRole);
            finish();
          }

          @Override
          public void onError(int code, String errorMsg) {
            LogUtil.e(TAG, "code:" + code + ",msg:" + errorMsg);
          }
        });
  }

  @Override
  protected void onKickOut() {
    super.onKickOut();
    NavUtils.toLoginHomePage(this);
    finish();
  }

  @Override
  public void onBackPressed() {
    AuthorManager.INSTANCE.logout(
        new LoginCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void unused) {
            SpUtils.getInstance().clear();
          }

          @Override
          public void onError(int i, @NonNull String s) {
            LogUtil.e(TAG, "logout onError code:" + i);
          }
        });
    super.onBackPressed();
  }
}
