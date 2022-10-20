// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.manager;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.constant.ErrorCode;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.model.PersonModel;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.app.medical.utils.MocDataUtil;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.app.medical.utils.SpUtils;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.UserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class UserInfoManager {

  public static final String TAG = "UserInfoManager";

  public static void updateUserInfo(PersonModel person, int role, NECallback callback) {
    HashMap<UserInfoFieldEnum, Object> map = new HashMap<>();
    JSONObject json = new JSONObject();
    try {
      json.put(AppConstants.ROLE, role);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    map.put(UserInfoFieldEnum.EXTEND, json.toString());
    if (person != null) {
      map.put(UserInfoFieldEnum.Name, person.name);
      map.put(UserInfoFieldEnum.GENDER, person.sex);
    }
    map.put(UserInfoFieldEnum.AVATAR, AuthorManager.INSTANCE.getUserInfo().getAvatar());
    NIMClient.getService(UserService.class)
        .updateUserInfo(map)
        .setCallback(
            new RequestCallback<Void>() {
              @Override
              public void onSuccess(Void param) {
                LogUtil.d(TAG, "updateUserInfo success");
                SpUtils.getInstance().saveInt(AppConstants.ROLE, role);
                if (person != null) {
                  SpUtils.getInstance().saveString(AppConstants.USER_INFO, person.toJson());
                }
                if (callback != null) {
                  callback.onSuccess(param);
                }
              }

              @Override
              public void onFailed(int code) {
                LogUtil.d(TAG, "updateUserInfo onFailed:" + code);
                if (callback != null) {
                  callback.onError(code, "更新角色出错了");
                }
              }

              @Override
              public void onException(Throwable exception) {
                LogUtil.d(TAG, "updateUserInfo onException:" + exception.getMessage());
                if (callback != null) {
                  callback.onError(ErrorCode.ERROR, exception.getMessage());
                }
              }
            });
  }

  public static void getUserRole(String userImUid, @NonNull NECallback<Integer> callback) {
    List<String> uids = new ArrayList<>();
    uids.add(userImUid);
    NIMClient.getService(UserService.class)
        .fetchUserInfo(uids)
        .setCallback(
            new RequestCallback<List<NimUserInfo>>() {
              @Override
              public void onSuccess(List<NimUserInfo> result) {
                LogUtil.d(TAG, "fetchUserInfo success：");
                if (result != null && result.size() > 0) {
                  String roleJson = result.get(0).getExtension();
                  if (TextUtils.isEmpty(roleJson)) {
                    callback.onError(ErrorCode.NO_ROLE, "");
                    return;
                  }

                  try {
                    JSONObject jsonObject = new JSONObject(roleJson);
                    int role = jsonObject.optInt(AppConstants.ROLE, -1);
                    callback.onSuccess(role);
                  } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onError(ErrorCode.ERROR, e.getMessage());
                  }
                }
              }

              @Override
              public void onFailed(int code) {
                LogUtil.d(TAG, "fetchUserInfo onFailed code：" + code);
                callback.onError(code, "");
              }

              @Override
              public void onException(Throwable exception) {
                LogUtil.d(TAG, "fetchUserInfo onFailed code：" + exception.getMessage());
                callback.onError(ErrorCode.ERROR, exception.getMessage());
              }
            });
  }

  // 登录成功后设置默认用户信息
  public static void setDefaultUser() {
    List<String> uids = new ArrayList<>();
    uids.add(getSelfImAccid());
    NIMClient.getService(UserService.class)
        .fetchUserInfo(uids)
        .setCallback(
            new RequestCallback<List<NimUserInfo>>() {
              @Override
              public void onSuccess(List<NimUserInfo> result) {
                LogUtil.d(TAG, "fetchUserInfo success：");
                if (result != null && result.size() > 0) {
                  String roleJson = result.get(0).getExtension();
                  if (TextUtils.isEmpty(roleJson)) {
                    updateUserInfo(MocDataUtil.getRandomPerson(), Role.DOCTOR, null);
                  }
                }
              }

              @Override
              public void onFailed(int code) {
                LogUtil.d(TAG, "fetchUserInfo onFailed code：" + code);
              }

              @Override
              public void onException(Throwable exception) {
                LogUtil.d(TAG, "fetchUserInfo onFailed code：" + exception.getMessage());
              }
            });
  }

  public static UserInfo getSelfUserInfo() {
    return AuthorManager.INSTANCE.getUserInfo();
  }

  public static String getSelfImAccid() {
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d(TAG, "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getImAccid();
  }

  public static String getSelfAccessToken() {
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d(TAG, "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getAccessToken();
  }

  public static String getSelfNickname() {
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d(TAG, "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().toJson().toString();
  }
}
