// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.medical.manager.MedicalViewModel;

public abstract class BaseFragment extends Fragment {

  protected MedicalViewModel viewModel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewModel =
        new ViewModelProvider(
                getActivity(),
                (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory())
            .get(MedicalViewModel.class);
    viewModel.roleType.observe(this, role -> roleChanged(role));
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = providerView(inflater, container);
    initView();
    initEvent();
    return view;
  }

  protected abstract View providerView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container);

  protected abstract void initView();

  protected abstract void initEvent();

  protected void finishActivity() {
    if (!getActivity().isFinishing()) {
      getActivity().finish();
    }
  }

  protected abstract void roleChanged(int role);
}
