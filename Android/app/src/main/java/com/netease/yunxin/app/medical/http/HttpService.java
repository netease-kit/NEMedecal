// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.http;

import com.netease.yunxin.app.medical.constant.ErrorCode;
import com.netease.yunxin.app.medical.model.UserModel;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.kit.common.network.Response;
import com.netease.yunxin.kit.common.network.ServiceCreator;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;

public class HttpService {
  public static void requestRtcToken(long uid, NECallback<String> callback) {
    ServerApi serverApi = ServiceCreator.INSTANCE.create(ServerApi.class);
    Map<String, Object> map = new HashMap<>();
    map.put("roomCname", "");
    map.put("uid", uid);
    serverApi
        .requestRtcToken(map)
        .enqueue(
            new Callback<Response<String>>() {
              @Override
              public void onResponse(
                  Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                if (response.isSuccessful()) {
                  if (response.body().getCode() == ErrorCode.SUCCESS) {
                    callback.onSuccess(response.body().getData());
                  } else {
                    callback.onError(response.body().getCode(), response.body().getMsg());
                  }
                } else {
                  callback.onError(response.code(), response.message());
                }
              }

              @Override
              public void onFailure(Call<Response<String>> call, Throwable t) {
                callback.onError(ErrorCode.ERROR, t.getMessage());
              }
            });
  }

  public static void searchUserInfoWithPhoneNumber(
      String phoneNumber, NECallback<UserModel> callback) {
    ServerApi serverApi = ServiceCreator.INSTANCE.create(ServerApi.class);
    Map<String, Object> map = new HashMap<>();
    map.put("mobile", phoneNumber);
    serverApi
        .searchUserWithPhoneNumber(map)
        .enqueue(
            new Callback<Response<UserModel>>() {
              @Override
              public void onResponse(
                  Call<Response<UserModel>> call,
                  retrofit2.Response<Response<UserModel>> response) {
                if (response.isSuccessful()) {
                  if (response.body().getCode() == ErrorCode.SUCCESS) {
                    callback.onSuccess(response.body().getData());
                  } else {
                    callback.onError(response.body().getCode(), response.body().getMsg());
                  }
                } else {
                  callback.onError(response.code(), response.message());
                }
              }

              @Override
              public void onFailure(Call<Response<UserModel>> call, Throwable t) {
                callback.onError(ErrorCode.ERROR, t.getMessage());
              }
            });
  }

  public static void sendSms(String phoneNumber, String noticeUser, NECallback<Void> callback) {
    ServerApi serverApi = ServiceCreator.INSTANCE.create(ServerApi.class);
    Map<String, Object> map = new HashMap<>();
    map.put("mobile", phoneNumber);
    map.put("noticeUser", noticeUser);
    serverApi
        .sendSms(map)
        .enqueue(
            new Callback<Response<Void>>() {
              @Override
              public void onResponse(
                  Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                  if (response.body().getCode() == ErrorCode.SUCCESS) {
                    callback.onSuccess(response.body().getData());
                  } else {
                    callback.onError(response.body().getCode(), response.body().getMsg());
                  }
                } else {
                  callback.onError(response.code(), response.message());
                }
              }

              @Override
              public void onFailure(Call<Response<Void>> call, Throwable t) {
                callback.onError(ErrorCode.ERROR, t.getMessage());
              }
            });
  }

  public static void sendAudioMessage(String mobile, String voiceTxt, NECallback<Void> callback) {
    ServerApi serverApi = ServiceCreator.INSTANCE.create(ServerApi.class);
    Map<String, Object> map = new HashMap<>();
    map.put("mobile", mobile);
    map.put("voiceTxt", voiceTxt);
    serverApi
        .sendAudioMessage(map)
        .enqueue(
            new Callback<Response<Void>>() {
              @Override
              public void onResponse(
                  Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                  if (response.body().getCode() == ErrorCode.SUCCESS) {
                    callback.onSuccess(response.body().getData());
                  } else {
                    callback.onError(response.body().getCode(), response.body().getMsg());
                  }
                } else {
                  callback.onError(response.code(), response.message());
                }
              }

              @Override
              public void onFailure(Call<Response<Void>> call, Throwable t) {
                callback.onError(ErrorCode.ERROR, t.getMessage());
              }
            });
  }
}
