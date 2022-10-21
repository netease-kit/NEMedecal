// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.model;

import androidx.annotation.DrawableRes;

public class SuffererModel {

  public String name;
  public int sexDrawable;
  public String sexString;
  public String age;
  public int avatar;

  public SuffererModel(
      String name,
      @DrawableRes int sexDrawable,
      String sexString,
      String age,
      @DrawableRes int avatar) {
    this.name = name;
    this.sexDrawable = sexDrawable;
    this.sexString = sexString;
    this.age = age;
    this.avatar = avatar;
  }
}
