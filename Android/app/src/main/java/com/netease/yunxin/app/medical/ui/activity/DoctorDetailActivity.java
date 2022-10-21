// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.view.View;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.databinding.ActivityDoctorDetailBinding;
import com.netease.yunxin.app.medical.model.DoctorModel;
import com.netease.yunxin.app.medical.model.SuffererCommentModel;
import com.netease.yunxin.app.medical.ui.view.CommonDialog;
import com.netease.yunxin.app.medical.ui.view.StatusBarConfig;
import com.netease.yunxin.app.medical.utils.MocDataUtil;
import com.netease.yunxin.app.medical.utils.NavUtils;
import java.util.List;

/** 医生主页 */
public class DoctorDetailActivity extends BaseActivity {

  private ActivityDoctorDetailBinding binding;

  @Override
  protected void initView() {
    setHeaderBg(R.color.color_EFF1F4);
    setHeaderDividerVisibility(View.GONE);
    initDoctorInfo();
    initComment();
  }

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_doctor_home);
  }

  @Override
  protected View getContentView() {
    binding = ActivityDoctorDetailBinding.inflate(getLayoutInflater());
    ;
    return binding.getRoot();
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

  private void initDoctorInfo() {
    DoctorModel model = (DoctorModel) getIntent().getSerializableExtra(AppConstants.DOCTOR_DETAIL);
    binding.tvDoctorName.setText(model.doctorName);
    binding.tvDoctorPosition.setText(model.doctorPosition);
    binding.tvDepartment.setText(model.doctorDepartment);
    binding.tvGoodAt.setText(model.goodAt);
    binding.ivDoctorAvatar.setImageResource(model.doctorAvatar);
  }

  private void initComment() {
    binding.rvSuffererComment.setLayoutManager(new LinearLayoutManager(this));
    List<SuffererCommentModel> models = MocDataUtil.mockSuffererComment();
    SuffererCommentAdapter adapter = new SuffererCommentAdapter(this, models);
    binding.rvSuffererComment.setAdapter(adapter);
    DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    divider.setDrawable(getResources().getDrawable(R.drawable.shape_divider));
    binding.rvSuffererComment.addItemDecoration(divider);
  }

  @Override
  protected void setEvent() {
    binding.ivImgTxtConsultation.setOnClickListener(v -> NavUtils.toTxtImgConsultation(this));
    binding.ivAudioConsultation.setOnClickListener(v -> NavUtils.toAudioConsultation(this));
    binding.ivVideoConsultation.setOnClickListener(
        v -> {
          CommonDialog dialog = new CommonDialog(DoctorDetailActivity.this);
          dialog
              .setTitle(getResources().getString(R.string.medical_auth_name_title))
              .setContent(getResources().getString(R.string.medical_auth_name_tips))
              .setPositiveBtnName(getResources().getString(R.string.medical_yes))
              .setNegativeBtnName(getResources().getString(R.string.medical_no))
              .setPositiveOnClickListener(
                  v1 -> {
                    NavUtils.toAuthNamePage(DoctorDetailActivity.this);
                    AuthNameActivity.fromPage = AuthNameActivity.FROM_VIDEO;
                  })
              .setNegativeOnClickListener(
                  v12 -> {
                    NavUtils.toVideoConsultation(DoctorDetailActivity.this);
                  })
              .show();
        });
  }
}
