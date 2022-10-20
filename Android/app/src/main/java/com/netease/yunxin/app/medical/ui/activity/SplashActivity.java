// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.app.medical.utils.SpUtils;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.kit.login.model.UserInfo;

public class SplashActivity extends BaseActivity {

  @Override
  protected void setEvent() {
    AuthorManager.INSTANCE.autoLogin(
        true,
        new LoginCallback<UserInfo>() {
          @Override
          public void onSuccess(@Nullable UserInfo userInfo) {
            int role = SpUtils.getInstance().getInt(AppConstants.ROLE, -1);
            if (role != -1) {
              NavUtils.toMainPage(SplashActivity.this, role);
            } else {
              NavUtils.toSelectRolePage(SplashActivity.this);
            }
            finish();
          }

          @Override
          public void onError(int i, @NonNull String s) {
            NavUtils.toLoginHomePage(SplashActivity.this);
            finish();
          }
        });
  }

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected boolean hasTitle() {
    return false;
  }

  @Override
  protected View getContentView() {
    return null;
  }

  @Override
  protected void initView() {}
}
