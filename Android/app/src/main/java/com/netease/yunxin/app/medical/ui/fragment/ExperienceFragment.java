// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.databinding.ExperienceFragmentBinding;
import com.netease.yunxin.app.medical.ui.activity.AuthNameActivity;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;

public class ExperienceFragment extends BaseFragment {
  private ExperienceFragmentBinding binding;

  @Override
  protected View providerView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = ExperienceFragmentBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  protected void initEvent() {
    binding.ivAudioNotify.setOnClickListener(v -> NavUtils.toAudioNotifyPage(requireActivity()));

    binding.ivKeepLive.setOnClickListener(v -> NavUtils.toKeepLivePage(requireActivity()));

    binding.ivPhoneAuth.setOnClickListener(v -> NavUtils.toAuthMobilePage(requireActivity()));

    binding.ivRealNameAuth.setOnClickListener(
        v -> {
          NavUtils.toAuthNamePage(requireActivity());
          AuthNameActivity.fromPage = AuthNameActivity.EXPERIENCE;
        });

    binding.ivVirtualBg.setOnClickListener(
        v -> {
          ToastX.showShortToast(getString(R.string.medical_virtual_bg_tips));
        });
  }

  @Override
  protected void initView() {}

  @Override
  protected void roleChanged(int role) {}
}
