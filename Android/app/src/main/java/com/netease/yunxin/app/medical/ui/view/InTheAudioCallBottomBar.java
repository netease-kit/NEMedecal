// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import com.netease.yunxin.app.medical.databinding.ViewInThAudioCallBottomBarBinding;

public class InTheAudioCallBottomBar extends RelativeLayout {
  private ViewInThAudioCallBottomBarBinding binding;

  public InTheAudioCallBottomBar(Context context) {
    super(context);
    init(context);
  }

  public InTheAudioCallBottomBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public InTheAudioCallBottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    binding = ViewInThAudioCallBottomBarBinding.inflate(LayoutInflater.from(context), this, true);
    binding.ivMicrophone.setOnClickListener(
        view -> {
          if (onItemClickListener != null) {
            onItemClickListener.onMicroPhoneButtonClick();
          }
        });
    binding.ivHangup.setOnClickListener(
        view -> {
          if (onItemClickListener != null) {
            onItemClickListener.onHangupButtonClick();
          }
        });
  }

  public ViewInThAudioCallBottomBarBinding getViewBinding() {
    return binding;
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  private OnItemClickListener onItemClickListener;

  public interface OnItemClickListener {
    void onMicroPhoneButtonClick();

    void onHangupButtonClick();
  }
}
