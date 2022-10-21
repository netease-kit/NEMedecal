// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.constant.AppParams;
import com.netease.yunxin.app.medical.manager.UserInfoManager;
import com.netease.yunxin.app.medical.model.CallPageParams;
import com.netease.yunxin.app.medical.model.DoctorModel;
import com.netease.yunxin.app.medical.ui.activity.AudioNotifyActivity;
import com.netease.yunxin.app.medical.ui.activity.AuthMobileActivity;
import com.netease.yunxin.app.medical.ui.activity.AuthNameActivity;
import com.netease.yunxin.app.medical.ui.activity.AuthPreviewActivity;
import com.netease.yunxin.app.medical.ui.activity.AuthSuccessActivity;
import com.netease.yunxin.app.medical.ui.activity.ConsultationOnlineActivity;
import com.netease.yunxin.app.medical.ui.activity.DoctorDetailActivity;
import com.netease.yunxin.app.medical.ui.activity.KeepLiveActivity;
import com.netease.yunxin.app.medical.ui.activity.LoginHomeActivity;
import com.netease.yunxin.app.medical.ui.activity.MainActivity;
import com.netease.yunxin.app.medical.ui.activity.SelectRoleActivity;
import com.netease.yunxin.app.medical.ui.activity.SetUpAudioConsultationActivity;
import com.netease.yunxin.app.medical.ui.activity.SetUpTxtImgConsultationActivity;
import com.netease.yunxin.app.medical.ui.activity.SetUpVideoConsultationActivity;
import com.netease.yunxin.app.medical.ui.activity.SettingActivity;
import com.netease.yunxin.app.medical.ui.activity.WebViewActivity;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.base.CallParam;

/// 页面跳转
public class NavUtils {

  public static void toLoginHomePage(Context context) {
    Intent intent = new Intent(context, LoginHomeActivity.class);
    context.startActivity(intent);
  }

  public static void toLoginPage(Activity context) {
    AuthorManager.INSTANCE.launchLogin(context, AppConstants.MAIN_PAGE_ACTION, true);
  }

  public static void toSelectRolePage(Context context) {
    Intent intent = new Intent(context, SelectRoleActivity.class);
    context.startActivity(intent);
  }

  public static void toBrowsePage(Context context, String title, String url) {
    Intent intent = new Intent(context, WebViewActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(AppParams.PARAM_KEY_TITLE, title);
    intent.putExtra(AppParams.PARAM_KEY_URL, url);

    context.startActivity(intent);
  }

  public static void toMainPage(Context context, int role) {
    Intent intent = new Intent(context, MainActivity.class);
    intent.putExtra(AppConstants.ROLE, role);
    context.startActivity(intent);
  }

  public static void toConsultationPage(Context context) {
    Intent intent = new Intent(context, ConsultationOnlineActivity.class);
    context.startActivity(intent);
  }

  public static void toDoctorDetail(Context context, DoctorModel model) {
    Intent intent = new Intent(context, DoctorDetailActivity.class);
    intent.putExtra(AppConstants.DOCTOR_DETAIL, model);
    context.startActivity(intent);
  }

  public static void toVideoConsultation(Context context) {
    Intent intent = new Intent(context, SetUpVideoConsultationActivity.class);
    context.startActivity(intent);
  }

  public static void toAudioConsultation(Context context) {
    Intent intent = new Intent(context, SetUpAudioConsultationActivity.class);
    context.startActivity(intent);
  }

  public static void toTxtImgConsultation(Context context) {
    Intent intent = new Intent(context, SetUpTxtImgConsultationActivity.class);
    context.startActivity(intent);
  }

  public static void toCallPage(Context context, CallPageParams pageParams) {
    CallParam param =
        CallParam.createSingleCallParam(
            pageParams.channelType.getValue(),
            UserInfoManager.getSelfImAccid(),
            pageParams.calledUser.imAccid,
            pageParams.toJson());
    CallKitUI.startSingleCall(context, param);
  }

  public static void toTxtImgPage(Context context, UserInfo userInfo) {
    XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE)
        .withParam(RouterConstant.CHAT_KRY, userInfo)
        .withContext(context)
        .navigate();
  }

  /// 跳转语音通知页面
  public static void toAudioNotifyPage(Context context) {
    Intent intent = new Intent(context, AudioNotifyActivity.class);
    context.startActivity(intent);
  }

  public static void toAuthNamePage(Context context) {
    Intent intent = new Intent(context, AuthNameActivity.class);
    context.startActivity(intent);
  }

  public static void toSettingPage(Context context) {
    Intent intent = new Intent(context, SettingActivity.class);
    context.startActivity(intent);
  }

  public static void toKeepLivePage(Context context) {
    Intent intent = new Intent(context, KeepLiveActivity.class);
    context.startActivity(intent);
  }

  public static void toAuthMobilePage(Context context) {
    Intent intent = new Intent(context, AuthMobileActivity.class);
    context.startActivity(intent);
  }

  public static void toAuthPreviewPage(Context context) {
    Intent intent = new Intent(context, AuthPreviewActivity.class);
    context.startActivity(intent);
  }

  public static void toAuthSuccessPage(Context context) {
    Intent intent = new Intent(context, AuthSuccessActivity.class);
    context.startActivity(intent);
  }
}
