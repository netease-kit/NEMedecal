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
   * https://nrtc.netease.im/demo/getChecksum.action?uid=30569704039&appkey=56813bdfbaa1c2a29bbea391ffbbe27a
   * { "code": 200, "checksum": "4ac9066de56ba7259dddb45a4334b8838a987dcb" }
   *
   * <p>{ "code": 200, "data": "c4a69f571a1291ebe7729a906668b62f138b1b9d", "requestId":
   * "abd1e08202204141725273050020000", "costTime": "96ms" }
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
