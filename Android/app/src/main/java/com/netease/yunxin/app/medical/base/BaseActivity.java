// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.databinding.HeaderBinding;
import com.netease.yunxin.app.medical.ui.activity.LoginHomeActivity;
import com.netease.yunxin.app.medical.ui.view.StatusBarConfig;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.EventType;
import com.netease.yunxin.kit.login.model.LoginEvent;
import com.netease.yunxin.kit.login.model.LoginObserver;

public abstract class BaseActivity extends AppCompatActivity {
  private static final String TAG = "BaseActivity";

  private LoginObserver<LoginEvent> loginObserver =
      loginEvent -> {
        if (loginEvent.getEventType() == EventType.TYPE_LOGOUT && !ignoredLoginEvent()) {
          LogUtil.i(TAG, "LoginObserver TYPE_LOGOUT ");
          finish();
          onKickOut();
        } else if (loginEvent.getEventType() == EventType.TYPE_LOGIN) {
          if (LoginHomeActivity.activity != null) {
            LoginHomeActivity.activity.finish();
          }
        }
      };
  private HeaderBinding headerBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AuthorManager.INSTANCE.registerLoginObserver(loginObserver);
    handleStatusBar();
    addContentView();
    setTopPadding();
    initView();
    setEvent();
  }

  protected boolean needTransparentStatusBar() {
    return false;
  }

  protected boolean hasTitle() {
    return true;
  }

  protected String getTitleContent() {
    return null;
  };

  protected abstract View getContentView();

  protected abstract void initView();

  protected abstract void setEvent();

  private void addContentView() {
    if (hasTitle()) {
      LinearLayout linearLayout = new LinearLayout(this);
      linearLayout.setLayoutParams(
          new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
      linearLayout.setOrientation(LinearLayout.VERTICAL);
      linearLayout.addView(handleHeader());
      linearLayout.addView(
          getContentView(),
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.MATCH_PARENT);
      super.setContentView(linearLayout);
    } else {
      if (getContentView() != null) super.setContentView(getContentView());
    }
  }

  protected void setHeaderBg(@ColorRes int color) {
    if (headerBinding != null) {
      headerBinding.getRoot().setBackgroundColor(getResources().getColor(color));
    }
  }

  protected void setHeaderDividerVisibility(int visibility) {
    if (headerBinding != null) {
      headerBinding.titleDivide.setVisibility(visibility);
    }
  }

  private View handleHeader() {
    headerBinding = HeaderBinding.inflate(getLayoutInflater());
    headerBinding.tvTitle.setText(getTitleContent());
    headerBinding.ivClose.setOnClickListener(view -> onBackPressed());
    return headerBinding.getRoot();
  }

  private void handleStatusBar() {
    if (needTransparentStatusBar()) {
      adapterStatusBar();
    } else {
      StatusBarConfig config = provideStatusBarConfig();
      if (config == null) {
        config =
            new StatusBarConfig.Builder()
                .statusBarDarkFont(true)
                .statusBarColor(R.color.white)
                .build();
      }
      ImmersionBar bar =
          ImmersionBar.with(this)
              .statusBarDarkFont(config.isDarkFont())
              .statusBarColor(config.getBarColor());
      if (config.isFits()) {
        bar.fitsSystemWindows(true);
      }
      if (config.isFullScreen()) {
        bar.fullScreen(true);
      }
      bar.init();
    }
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
  protected void onDestroy() {
    AuthorManager.INSTANCE.unregisterLoginObserver(loginObserver);
    super.onDestroy();
  }

  protected StatusBarConfig provideStatusBarConfig() {
    return null;
  }

  protected boolean ignoredLoginEvent() {
    return false;
  }

  protected void onKickOut() {}

  protected void setTopPadding() {
    ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);
    if (viewGroup.getChildCount() > 0) {
      StatusBarConfig.paddingStatusBarHeight(this, viewGroup.getChildAt(0));
    }
  }
}
