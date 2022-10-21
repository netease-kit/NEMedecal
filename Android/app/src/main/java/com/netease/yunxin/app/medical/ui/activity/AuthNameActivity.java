// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.Manifest;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.databinding.ActivityAuthNameBinding;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import java.util.Arrays;
import java.util.List;

public class AuthNameActivity extends BaseActivity {

  private ActivityAuthNameBinding binding;
  public static int fromPage = 0;
  public static final int FROM_VIDEO = 1;
  public static final int EXPERIENCE = 2;
  public static AuthNameActivity authNameActivity;
  private static final String[] REQUIRED_PERMISSIONS = {
    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
  };

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_auth_name_title);
  }

  @Override
  protected View getContentView() {
    binding = ActivityAuthNameBinding.inflate(LayoutInflater.from(this));
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    authNameActivity = this;
  }

  @Override
  protected void setEvent() {
    binding.tvCommit.setOnClickListener(
        view -> {
          String name = binding.etName.getText().toString();
          String ID = binding.etId.getText().toString();
          if (TextUtils.isEmpty(name)) {
            ToastX.showShortToast(getString(R.string.medical_auth_name_tip));
          } else if (TextUtils.isEmpty(ID) || (!(ID.length() == 18 || ID.length() == 19))) {
            ToastX.showShortToast(getString(R.string.medical_auth_name_id));
          } else {
            Permission.requirePermissions(this, REQUIRED_PERMISSIONS)
                .request(
                    new Permission.PermissionCallback() {
                      @Override
                      public void onGranted(List<String> list) {
                        if (list.containsAll(Arrays.asList(REQUIRED_PERMISSIONS))) {
                          NavUtils.toAuthPreviewPage(AuthNameActivity.this);
                        }
                      }

                      @Override
                      public void onDenial(List<String> list, List<String> list1) {
                        ToastX.showShortToast(
                            getString(R.string.medical_permission_request_failed_tips));
                      }

                      @Override
                      public void onException(Exception e) {}
                    });
          }
        });
  }
}
