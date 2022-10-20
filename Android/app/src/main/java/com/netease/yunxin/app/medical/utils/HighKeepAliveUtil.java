// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.utils;

import android.content.Context;
import com.netease.lava.nertc.foreground.ForegroundKit;
import com.netease.yunxin.app.medical.constant.AppConstants;

public class HighKeepAliveUtil {
  private static final int BACKGROUND_TIME = 10000;

  public static int openHighKeepAlive(Context context) {
    Context appContext = context.getApplicationContext();
    if (ForegroundKit.getInstance(appContext).checkNotifySetting()) {
      ForegroundKit instance = ForegroundKit.getInstance(appContext);
      int result = instance.init(AppConstants.getAppKey(), BACKGROUND_TIME);
      return result;
    }
    return -1;
  }

  public static void closeHighKeepAlive(Context context) {
    ForegroundKit.getInstance(context.getApplicationContext()).release();
  }

  public static void requestNotifyPermission(Context context) {
    ForegroundKit.getInstance(context.getApplicationContext()).requestNotifyPermission();
  }
}
