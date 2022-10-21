// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.utils;

import com.netease.yunxin.app.medical.MedicalApplication;

public class ScreenUtils {

  /**
   * Return the width of screen, in pixel.
   *
   * @return The absolute width of the available display size in pixels
   */
  public static int getDisplayWidth() {
    return MedicalApplication.getApplication().getResources().getDisplayMetrics().widthPixels;
  }

  /**
   * Return the height of screen, in pixel.
   *
   * @return The absolute height of the available display size in pixels
   */
  public static int getDisplayHeight() {
    return MedicalApplication.getApplication().getResources().getDisplayMetrics().heightPixels;
  }
}
