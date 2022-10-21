// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppUtils {

  public static int getVersionCode(Context context) {
    PackageManager manager = context.getPackageManager();
    int code = 0;
    try {
      PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      code = info.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return code;
  }

  public static String getVersionName(Context context) {
    PackageManager manager = context.getPackageManager();
    String name = "";
    try {
      PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      name = info.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    return name;
  }
}
