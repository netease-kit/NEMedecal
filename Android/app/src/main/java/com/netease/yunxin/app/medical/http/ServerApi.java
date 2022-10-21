// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.http;

import com.netease.yunxin.app.medical.model.UserModel;
import com.netease.yunxin.kit.common.network.Response;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServerApi {

  /**
   * 获取Rtc Token
   */
  @POST("/demo/v2/getCheckSum.action")
  Call<Response<String>> requestRtcToken(@Body Map<String, Object> body);

  /** 根据手机号查询用户信息 */
  @POST("/p2pVideoCall/caller/v2/searchSubscriber")
  Call<Response<UserModel>> searchUserWithPhoneNumber(@Body Map<String, Object> body);

  /** 发送普通短信通知 */
  @POST("/sms/sendMedicalSms")
  Call<Response<Void>> sendSms(@Body Map<String, Object> body);

  /** 语音通知 */
  @POST("/voip/v1/voice/notice/txt")
  Call<Response<Void>> sendAudioMessage(@Body Map<String, Object> body);
}
