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
import com.netease.yunxin.app.medical.databinding.HomeDoctorFragmentBinding;
import com.netease.yunxin.app.medical.model.SuffererModel;
import com.netease.yunxin.app.medical.utils.MocDataUtil;
import java.util.List;

public class HomeDoctorFragment extends HomeFragment {

  private HomeDoctorFragmentBinding binding;

  @Override
  protected View providerView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = HomeDoctorFragmentBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    binding.tvRole.setText(
        String.format(getString(R.string.medical_role_desc), getString(R.string.medical_doctor)));
    initSuffererList();
  }

  @Override
  protected void initEvent() {
    binding.ivChangeRole.setOnClickListener(
        v -> {
          showChangeRoleDialog();
        });
    binding.ivDemoTip.setOnClickListener(v -> showDemoDescDialog());
  }

  private void initSuffererList() {
    List<SuffererModel> models = MocDataUtil.mockSuffererData();
    SuffererListAdapter adapter = new SuffererListAdapter(getContext(), models);
    binding.rvSuffererList.setLayoutManager(new LinearLayoutManager(getContext()));
    DividerItemDecoration divider =
        new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
    divider.setDrawable(getResources().getDrawable(R.drawable.shape_divider));
    binding.rvSuffererList.addItemDecoration(divider);
    binding.rvSuffererList.setAdapter(adapter);
  }
}
