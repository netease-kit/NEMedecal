// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.constant.ErrorCode;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.databinding.SetupConsultationBinding;
import com.netease.yunxin.app.medical.http.HttpService;
import com.netease.yunxin.app.medical.manager.UserInfoManager;
import com.netease.yunxin.app.medical.model.UserModel;
import com.netease.yunxin.app.medical.ui.view.StatusBarConfig;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.app.medical.utils.SpUtils;
import com.netease.yunxin.kit.login.utils.HelperUtils;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

/** 图文，语音，视频问诊的基类， */
public abstract class SetUpConsultationActivity extends BaseActivity {
  private SetupConsultationBinding binding;
  protected boolean isOpenVideo = true;
  protected boolean isOpenAudio = true;
  protected boolean isOpenHighOn = false;
  protected int currentRole;
  public static final int TXT_IMG_TYPE = 0; // 图文
  public static final int AUDIO_TYPE = 1; // 语音
  public static final int VIDEO_TYPE = 2; // 视频
  public static final long TIME = 500;
  private long tempTime = 0;

  @Override
  protected String getTitleContent() {
    String title = getString(R.string.medical_video_consultation);
    if (getConsultationType() == TXT_IMG_TYPE) { //图文
      title = getString(R.string.medical_text_img_consultation);
    } else if (getConsultationType() == AUDIO_TYPE) { // 语音
      title = getString(R.string.medical_audio_consultation);
    }
    return title;
  }

  @Override
  protected View getContentView() {
    binding = SetupConsultationBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    setHeaderBg(R.color.color_EFF1F4);
    setHeaderDividerVisibility(View.GONE);
    if (getConsultationType() == TXT_IMG_TYPE) {
      binding.llCallSetting.setVisibility(View.GONE);
    } else if (getConsultationType() == AUDIO_TYPE) {
      binding.llVideo.setVisibility(View.GONE);
      binding.llVideoDivider.setVisibility(View.GONE);
    } else {
      binding.llHighOn.setVisibility(View.GONE);
      binding.llAudioDivider.setVisibility(View.GONE);
    }
    setRole();
    showCallRecord();
  }

  @Override
  protected StatusBarConfig provideStatusBarConfig() {
    StatusBarConfig config =
        new StatusBarConfig.Builder()
            .statusBarDarkFont(true)
            .statusBarColor(R.color.color_EFF1F4)
            .build();
    return config;
  }

  /// 问诊类型
  protected abstract int getConsultationType();

  // 处理问诊跳转
  protected abstract void startConsultation(UserModel userModel);

  private void setRole() {
    currentRole = SpUtils.getInstance().getInt(AppConstants.ROLE, Role.SUFFERER);
    if (currentRole == Role.DOCTOR) {
      binding.tvRole.setText(getString(R.string.medical_doctor));
    } else {
      binding.tvRole.setText(getString(R.string.medical_sufferer));
    }
  }

  private void showCallRecord() {
    List<String> records = getRecordList();
    if (records.size() < 1) {
      binding.llCallRecord.setVisibility(View.GONE);
    } else {
      binding.llCallRecord.setVisibility(View.VISIBLE);
      int size = Math.min(records.size(), 2);
      for (int i = 0; i < size; i++) {
        if (i == 0) {
          binding.flMobileOne.setVisibility(View.VISIBLE);
          binding.tvRecordMobileOne.setText(records.get(i));
          binding.flMobileTwo.setVisibility(View.GONE);
        }
        if (i == 1) {
          binding.flMobileTwo.setVisibility(View.VISIBLE);
          binding.tvRecordMobileTwo.setText(records.get(i));
        }
      }
    }
  }

  private List<String> getRecordList() {
    String recordsJson = SpUtils.getInstance().getString(AppConstants.CALL_RECORD, null);
    List<String> records = new ArrayList<>();
    if (!TextUtils.isEmpty(recordsJson)) {
      try {
        JSONArray jsonArray = new JSONArray(recordsJson);
        for (int i = 0; i < jsonArray.length(); i++) {
          records.add(jsonArray.getString(i));
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return records;
  }

  private void saveRecord(String mobile) {
    List<String> records = getRecordList();
    if (records.contains(mobile)) {
      records.remove(mobile);
    }
    records.add(0, mobile);
    saveToSp(records);
  }

  private void saveToSp(List<String> records) {
    JSONArray jsonArray = new JSONArray();
    for (int i = 0; i < records.size(); i++) {
      jsonArray.put(records.get(i));
    }
    SpUtils.getInstance().saveString(AppConstants.CALL_RECORD, jsonArray.toString());
  }

  @Override
  protected void setEvent() {
    binding.switchVideo.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          isOpenVideo = isChecked;
        });
    binding.switchAudio.setOnCheckedChangeListener(
        ((buttonView, isChecked) -> {
          isOpenAudio = isChecked;
        }));
    binding.switchHighOn.setOnCheckedChangeListener(
        ((buttonView, isChecked) -> {
          isOpenHighOn = isChecked;
        }));

    binding.tvSetupVideo.setOnClickListener(
        v -> {
          long currentTime = System.currentTimeMillis();
          if (currentTime - tempTime < TIME) {
            return;
          }
          tempTime = currentTime;
          String mobile = binding.etMobile.getText().toString();
          if (!HelperUtils.INSTANCE.verifyPhoneNumber(mobile)) {
            binding.tvError.setVisibility(View.VISIBLE);
            binding.tvError.setText(getString(R.string.medical_mobile_error));
          } else if (TextUtils.equals(mobile, UserInfoManager.getSelfUserInfo().getUser())) {
            binding.tvError.setVisibility(View.VISIBLE);
            binding.tvError.setText(getString(R.string.medical_call_self_tip));
          } else {
            saveRecord(mobile);
            handleSetup(mobile);
          }
        });
    binding.tvRecordMobileOne.setOnClickListener(
        v -> {
          binding.etMobile.setText(binding.tvRecordMobileOne.getText());
        });
    binding.tvRecordMobileTwo.setOnClickListener(
        v -> {
          binding.etMobile.setText(binding.tvRecordMobileTwo.getText());
        });
    binding.ivMobileClearOne.setOnClickListener(
        v -> {
          List<String> records = getRecordList();
          records.remove(binding.tvRecordMobileOne.getText().toString());
          saveToSp(records);
          showCallRecord();
        });

    binding.ivMobileClearTwo.setOnClickListener(
        v -> {
          List<String> records = getRecordList();
          records.remove(binding.tvRecordMobileTwo.getText().toString());
          saveToSp(records);
          showCallRecord();
        });
    handleMobileInput();
  }

  private void handleMobileInput() {
    binding.ivMobileClearInput.setOnClickListener(
        view -> {
          binding.etMobile.setText("");
          binding.ivMobileClearInput.setVisibility(View.GONE);
        });
    binding.etMobile.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.tvError.setVisibility(View.GONE);
          }

          @Override
          public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
              binding.ivMobileClearInput.setVisibility(View.VISIBLE);
            } else {
              binding.ivMobileClearInput.setVisibility(View.GONE);
            }
          }
        });
  }

  @Override
  protected void onResume() {
    super.onResume();
    showCallRecord();
  }

  private void handleSetup(String mobile) {
    HttpService.searchUserInfoWithPhoneNumber(
        mobile,
        new NECallback<UserModel>() {
          @Override
          public void onSuccess(UserModel userModel) {
            String userImUid = userModel.imAccid;
            UserInfoManager.getUserRole(
                userImUid,
                new NECallback<Integer>() {
                  @Override
                  public void onSuccess(Integer role) {
                    int currentRole = SpUtils.getInstance().getInt(AppConstants.ROLE, -1);
                    if (currentRole == role) {
                      if (currentRole == Role.DOCTOR) {
                        binding.tvError.setText(getString(R.string.medical_mobile_error_doctor));
                      } else {
                        binding.tvError.setText(getString(R.string.medical_mobile_error_sufferer));
                      }
                      binding.tvError.setVisibility(View.VISIBLE);
                    } else { /// 处理问诊跳转逻辑
                      startConsultation(userModel);
                    }
                  }

                  @Override
                  public void onError(int code, String errorMsg) {
                    binding.tvError.setVisibility(View.VISIBLE);
                    if (code == ErrorCode.NO_ROLE) {
                      binding.tvError.setText(getString(R.string.medical_no_role_tip));
                    } else {
                      binding.tvError.setText(errorMsg);
                    }
                  }
                });
          }

          @Override
          public void onError(int code, String errorMsg) {
            binding.tvError.setVisibility(View.VISIBLE);
            if (ErrorCode.NO_REGISTER_ERROR == code) {
              binding.tvError.setText(getString(R.string.medical_no_register));
            } else {
              binding.tvError.setText(getString(R.string.medical_exception));
            }
          }
        });
  }
}
