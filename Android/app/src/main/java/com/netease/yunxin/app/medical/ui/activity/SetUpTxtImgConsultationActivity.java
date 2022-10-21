// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import com.netease.yunxin.app.medical.model.UserModel;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;

/** 发起图文问诊 */
public class SetUpTxtImgConsultationActivity extends SetUpConsultationActivity {

  @Override
  protected int getConsultationType() {
    return TXT_IMG_TYPE;
  }

  @Override
  protected void startConsultation(UserModel userModel) {
    UserInfo userInfo = new UserInfo(userModel.imAccid, userModel.nickname, userModel.avatar);
    NavUtils.toTxtImgPage(SetUpTxtImgConsultationActivity.this, userInfo);
  }
}
