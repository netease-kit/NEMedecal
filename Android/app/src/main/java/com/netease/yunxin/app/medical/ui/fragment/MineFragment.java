// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.databinding.MineFragmentBinding;
import com.netease.yunxin.app.medical.model.PersonModel;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.app.medical.utils.SpUtils;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.nertc.nertcvideocall.utils.IMHelpers;

public class MineFragment extends HomeFragment {

  private MineFragmentBinding binding;
  private int role;

  @Override
  protected View providerView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = MineFragmentBinding.inflate(getLayoutInflater(), container, false);
    role = SpUtils.getInstance().getInt(AppConstants.ROLE, -1);
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    if (role == Role.DOCTOR) {
      showDoctor();
    } else {
      showSufferer();
    }
  }

  @Override
  protected void initEvent() {
    binding.llMineSufferer.setOnClickListener(
        v -> {
          ToastX.showShortToast(getString(R.string.medical_feature_tip));
        });
    binding.llMineTask.setOnClickListener(
        v -> ToastX.showShortToast(getString(R.string.medical_feature_tip)));
    binding.llMineApply.setOnClickListener(
        v ->
            NavUtils.toBrowsePage(
                requireActivity(),
                getString(R.string.medical_mine_apply),
                AppConstants.REGISTER_PAGE));
    binding.llMineSetting.setOnClickListener(v -> NavUtils.toSettingPage(requireActivity()));
    binding.ivChangeRole.setOnClickListener(view -> showChangeRoleDialog());
    binding.btnLogout.setOnClickListener(view -> logout());
  }

  private void logout() {
    AuthorManager.INSTANCE.logout(
        new LoginCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void unused) {
            IMHelpers.logout();
            SpUtils.getInstance().clear();
          }

          @Override
          public void onError(int i, @NonNull String s) {}
        });
  }

  private void showDoctor() {
    ImageLoader.with(requireActivity())
        .circleLoad(AuthorManager.INSTANCE.getUserInfo().getAvatar(), binding.ivMineAvatar);
    String personDataStr = SpUtils.getInstance().getString(AppConstants.USER_INFO, null);
    if (TextUtils.isEmpty(personDataStr)) {
      ToastX.showShortToast(getString(R.string.medical_data_error));
      logout();
      return;
    }
    PersonModel model =
        PersonModel.createModelFromJson(
            SpUtils.getInstance().getString(AppConstants.USER_INFO, null));
    binding.tvMineName.setText(model.name);
    binding.tvMineDepartment.setText(getString(R.string.medical_mine_department));
    binding.tvMineDepartment.setVisibility(View.VISIBLE);
    binding.ivMineSufferer.setImageResource(R.drawable.icon_mine_sufferer);
    binding.tvMineSufferer.setText(getString(R.string.medical_mine_sufferer));
    binding.ivMineTask.setImageResource(R.drawable.icon_mine_task);
    binding.tvMineTask.setText(getString(R.string.medical_mine_task));
    binding.tvMineSuffer.setVisibility(View.GONE);
    binding.llSuffererSex.setVisibility(View.GONE);
  }

  private void showSufferer() {
    ImageLoader.with(requireActivity())
        .circleLoad(AuthorManager.INSTANCE.getUserInfo().getAvatar(), binding.ivMineAvatar);
    String personDataStr = SpUtils.getInstance().getString(AppConstants.USER_INFO, null);
    if (TextUtils.isEmpty(personDataStr)) {
      ToastX.showShortToast(getString(R.string.medical_data_error));
      logout();
      return;
    }
    PersonModel model =
        PersonModel.createModelFromJson(
            SpUtils.getInstance().getString(AppConstants.USER_INFO, null));
    binding.tvMineName.setText(model.name);
    if (model.sex == 1) {
      binding.ivSuffererSex.setImageResource(R.drawable.icon_male);
      binding.tvSuffererSex.setText(getResources().getString(R.string.medical_mine_male));
    } else {
      binding.ivSuffererSex.setImageResource(R.drawable.icon_female);
      binding.tvSuffererSex.setText(getResources().getString(R.string.medical_mine_female));
    }
    binding.ivMineSufferer.setImageResource(R.drawable.icon_mine_doctor);
    binding.tvMineSufferer.setText(getString(R.string.medical_mine_doctor));
    binding.ivMineTask.setImageResource(R.drawable.icon_mine_record);
    binding.tvMineTask.setText(getString(R.string.medical_mine_record));
    binding.tvMineDepartment.setVisibility(View.GONE);
    binding.tvMineSuffer.setVisibility(View.VISIBLE);
    binding.llSuffererSex.setVisibility(View.VISIBLE);
  }

  @Override
  protected void roleChanged(int role) {
    this.role = role;
    initView();
  }
}
