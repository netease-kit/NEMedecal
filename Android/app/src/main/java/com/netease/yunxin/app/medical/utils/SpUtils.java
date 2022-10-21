// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.netease.yunxin.app.medical.MedicalApplication;

public class SpUtils {

  private SharedPreferences sharedPreferences;
  private Context context;

  public static SpUtils getInstance() {
    return InstanceHolder.INSTANCE;
  }

  private SpUtils() {
    context = MedicalApplication.getApplication();
  }

  private void checkPreferences() {
    if (this.sharedPreferences == null) {
      this.sharedPreferences = context.getSharedPreferences("com_netease_yunxin_app_medical", 0);
    }
  }

  public String getString(String key, String defValue) {
    this.checkPreferences();
    return this.sharedPreferences.getString(key, defValue);
  }

  public void saveString(String key, String value) {
    this.checkPreferences();
    this.sharedPreferences.edit().putString(key, value).apply();
  }

  public int getInt(String key, int defValue) {
    this.checkPreferences();
    return this.sharedPreferences.getInt(key, defValue);
  }

  public void saveInt(String key, int value) {
    this.checkPreferences();
    this.sharedPreferences.edit().putInt(key, value).apply();
  }

  public void clear() {
    this.checkPreferences();
    this.sharedPreferences.edit().clear().apply();
  }

  private static class InstanceHolder {
    private static SpUtils INSTANCE = new SpUtils();

    private InstanceHolder() {}
  }
}
