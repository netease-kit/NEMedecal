// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.databinding.HomeSuffererFragmentBinding;
import com.netease.yunxin.app.medical.model.DoctorModel;
import com.netease.yunxin.app.medical.utils.MocDataUtil;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import java.util.List;

public class HomeSuffererFragment extends HomeFragment {

  private HomeSuffererFragmentBinding binding;

  @Override
  protected View providerView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = HomeSuffererFragmentBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    binding.tvRole.setText(
        String.format(getString(R.string.medical_role_desc), getString(R.string.medical_sufferer)));
    initDoctorList();
  }

  @Override
  protected void initEvent() {
    binding.ivChangeRole.setOnClickListener(
        v -> {
          showChangeRoleDialog();
        });
    binding.ivDemoTip.setOnClickListener(v -> showDemoDescDialog());
    binding.llOnline.setOnClickListener(
        v -> {
          NavUtils.toConsultationPage(getContext());
        });
    binding.llAppointment.setOnClickListener(
        v -> {
          ToastX.showShortToast(getString(R.string.medical_feature_tip));
        });
  }

  private void initDoctorList() {
    List<DoctorModel> models = MocDataUtil.mockDoctorData();
    DoctorListAdapter adapter = new DoctorListAdapter(getContext(), models);
    binding.rvDoctorList.setLayoutManager(new LinearLayoutManager(getContext()));
    DividerItemDecoration divider =
        new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
    divider.setDrawable(getResources().getDrawable(R.drawable.shape_divider));
    binding.rvDoctorList.addItemDecoration(divider);
    binding.rvDoctorList.setAdapter(adapter);
  }
}
