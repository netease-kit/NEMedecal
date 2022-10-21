// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical;

import android.app.Application;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.constant.NimSDKOptionConfig;
import com.netease.yunxin.app.medical.manager.UserInfoManager;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.common.network.ContextRegistry;
import com.netease.yunxin.kit.common.network.ServiceCreator;
import com.netease.yunxin.kit.corekit.XKit;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.IMKitUtils;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.AuthorConfig;
import com.netease.yunxin.kit.login.model.EventType;
import com.netease.yunxin.kit.login.model.LoginEvent;
import com.netease.yunxin.kit.login.model.LoginObserver;
import com.netease.yunxin.kit.login.model.LoginType;
import java.util.HashMap;

public class MedicalApplication extends Application {
  private static Application application;

  @Override
  public void onCreate() {
    super.onCreate();
    application = this;
    initIm();
    initCommon();
    initLogin();
    registerLoginObserver();
  }

  public static Application getApplication() {
    return application;
  }

  // 初始化IM
  private void initIm() {
    SDKOptions options = NimSDKOptionConfig.getSDKOptions(this, AppConstants.getAppKey());
    IMKitClient.init(this, null, options);
    if (IMKitUtils.isMainProcess(this)) {
      ChatKitClient.init(this);
    }
  }

  // 初始化登录
  private void initLogin() {
    //初始化登录模块
    AuthorConfig authorConfig =
        new AuthorConfig(
            AppConstants.getAppKey(),
            AppConstants.PARENT_SCOPE,
            AppConstants.SCOPE,
            !AppConstants.isOnLine);
    authorConfig.setLoginType(LoginType.LANGUAGE_SWITCH);
    AuthorManager.INSTANCE.initAuthor(getApplicationContext(), authorConfig);
  }

  // 注册登录状态监听
  private void registerLoginObserver() {
    LoginObserver<LoginEvent> loginObserver =
        loginEvent -> {
          if (loginEvent.getEventType() == EventType.TYPE_LOGIN) {
            ServiceCreator.user = UserInfoManager.getSelfUserInfo().getUser();
            ServiceCreator.token = UserInfoManager.getSelfAccessToken();
            HashMap<String, String> header = new HashMap<>();
            header.put("appKey", AppConstants.getAppKey());
            header.put("accessToken", ServiceCreator.token);
            ServiceCreator.collectHeaders = header;
            ServiceCreator.INSTANCE.init(
                this, AppConstants.getBasUrl(), ServiceCreator.LOG_LEVEL_BODY);
            UserInfoManager.setDefaultUser();
          }
        };
    AuthorManager.INSTANCE.registerLoginObserver(loginObserver);
  }

  /// 初始化 网络 commonUI
  private void initCommon() {
    ContextRegistry.context = this;
    XKit.Companion.initialize(this, null);
  }
}
