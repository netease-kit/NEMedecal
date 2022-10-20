// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.constant;

/// 通用常量
public class AppConstants {
  public static final String APP_KEY = "your app key";

  public static final int PARENT_SCOPE = 6;
  public static final int SCOPE = 2;
  public static String BASE_URL = "http://yiyong.netease.im";

  public static final String MAIN_PAGE_ACTION = "https://netease.yunxin.medical.selectRole";
  public static final String PRIVACY_POLICY = "http://yunxin.163.com/clauses?serviceType=3";
  public static final String USER_AGREEMENT = "http://yunxin.163.com/clauses";
  public static final String REGISTER_PAGE =
      "https://id.grow.163.com/register?h=media&t=media&from=nim%7Chttps%3A%2F%2Fnetease.im%2F&clueFrom=nim&referrer=https%3A%2F%2Fapp.yunxin.163.com";

  public static final String ROLE = "role";
  public static final String DOCTOR_DETAIL = "doctorDetail";
  public static final String CALL_RECORD = "callRecord";
  public static final String USER_INFO = "user_info";

  public static boolean isOnLine = true;

  public static String getBasUrl() {
      return BASE_URL;
  }

  public static String getAppKey() {
      return APP_KEY;
  }
}
