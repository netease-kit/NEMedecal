// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.medical.model.CallPageParams;
import com.netease.yunxin.app.medical.model.UserModel;
import com.netease.yunxin.app.medical.utils.NavUtils;

/** 发起视频问诊 */
public class SetUpVideoConsultationActivity extends SetUpConsultationActivity {

  @Override
  protected int getConsultationType() {
    return VIDEO_TYPE;
  }

  @Override
  protected void startConsultation(UserModel userModel) {
    NavUtils.toCallPage(
        SetUpVideoConsultationActivity.this,
        new CallPageParams(userModel, currentRole, isOpenAudio, isOpenVideo, ChannelType.VIDEO));
  }
}
