// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.view.View;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.databinding.ActivityConsultationOnlineBinding;
import com.netease.yunxin.app.medical.model.DoctorModel;
import com.netease.yunxin.app.medical.ui.fragment.DoctorListAdapter;
import com.netease.yunxin.app.medical.ui.view.StatusBarConfig;
import com.netease.yunxin.app.medical.utils.MocDataUtil;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import java.util.List;

public class ConsultationOnlineActivity extends BaseActivity {

  private ActivityConsultationOnlineBinding binding;

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_consultation_online);
  }

  @Override
  protected View getContentView() {
    binding = ActivityConsultationOnlineBinding.inflate(getLayoutInflater());
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

  protected void initView() {
    setHeaderBg(R.color.color_EFF1F4);
    setHeaderDividerVisibility(View.GONE);
    List<DoctorModel> models = MocDataUtil.mockDoctorData();
    DoctorListAdapter adapter = new DoctorListAdapter(this, models);
    binding.rvDoctorList.setLayoutManager(new LinearLayoutManager(this));
    DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    divider.setDrawable(getResources().getDrawable(R.drawable.shape_divider));
    binding.rvDoctorList.addItemDecoration(divider);
    binding.rvDoctorList.setAdapter(adapter);
  }

  @Override
  protected void setEvent() {
    binding.tvDepartmentMiddle.setOnClickListener(
        v -> {
          ToastX.showShortToast(getString(R.string.medical_feature_tip));
        });
    binding.tvDepartmentRight.setOnClickListener(
        v -> {
          ToastX.showShortToast(getString(R.string.medical_feature_tip));
        });
  }
}
