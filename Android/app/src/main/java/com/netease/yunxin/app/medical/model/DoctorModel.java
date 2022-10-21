// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.model;

import androidx.annotation.DrawableRes;
import java.io.Serializable;

public class DoctorModel implements Serializable {
  public String doctorName; // 张三
  public int doctorAvatar; // 头像
  public String doctorPosition; // 主治医生
  public String doctorDepartment; // 第一人民医院，呼吸内科
  public String suffererCount; // 接诊人数，好评率
  public String goodAt; //擅长

  public DoctorModel(
      String doctorName,
      @DrawableRes int doctorAvatar,
      String doctorPosition,
      String doctorDepartment,
      String suffererCount,
      String goodAt) {
    this.doctorName = doctorName;
    this.doctorAvatar = doctorAvatar;
    this.doctorPosition = doctorPosition;
    this.doctorDepartment = doctorDepartment;
    this.suffererCount = suffererCount;
    this.goodAt = goodAt;
  }
}
