// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.model;

import androidx.annotation.DrawableRes;

/** 患者评论model */
public class SuffererCommentModel {
  public String name;
  public int avatar;
  public String date;
  public String content;

  public SuffererCommentModel(String name, @DrawableRes int avatar, String date, String content) {
    this.name = name;
    this.avatar = avatar;
    this.date = date;
    this.content = content;
  }
}
