// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.medical.databinding.ImCustomBottomLayoutBinding;

public class IMBottomView extends LinearLayout {

  private ImCustomBottomLayoutBinding binding;
  private IBottomViewClick clickListener;

  public IMBottomView(Context context) {
    this(context, null);
  }

  public IMBottomView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    binding = ImCustomBottomLayoutBinding.inflate(LayoutInflater.from(context));
    this.addView(binding.getRoot());
    binding.suffererInfo.setOnClickListener(
        v -> {
          if (clickListener != null) {
            clickListener.clickSuffererInfo();
          }
        });

    binding.history.setOnClickListener(
        v -> {
          if (clickListener != null) {
            clickListener.clickHistory();
          }
        });

    binding.end.setOnClickListener(
        v -> {
          if (clickListener != null) {
            clickListener.clickEndConsultation();
          }
        });
  }

  public void setClickListener(IBottomViewClick clickListener) {
    this.clickListener = clickListener;
  }

  public interface IBottomViewClick {
    void clickSuffererInfo();

    void clickHistory();

    void clickEndConsultation();
  }
}
