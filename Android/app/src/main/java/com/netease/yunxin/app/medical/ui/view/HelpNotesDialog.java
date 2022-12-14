// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.view;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.databinding.DialogHelpNotesBinding;

public class HelpNotesDialog extends DialogFragment {
  private DialogHelpNotesBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = DialogHelpNotesBinding.inflate(inflater, container, false);
    initEvent();
    return binding.getRoot();
  }

  private void initEvent() {
    binding.ivClose.setOnClickListener(view -> dismiss());
  }

  @Override
  public void onStart() {
    super.onStart();
    initParams();
  }

  private void initParams() {
    Window window = getDialog().getWindow();
    if (window != null) {
      window.setBackgroundDrawableResource(R.drawable.bg_white_round);

      WindowManager.LayoutParams params = window.getAttributes();
      params.gravity = Gravity.CENTER;
      params.width = ViewGroup.LayoutParams.WRAP_CONTENT;

      params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      window.setAttributes(params);
    }
    setCancelable(true); //设置点击外部是否消失
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
