// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.kit.alog.ALog;

public class CommonDialog extends Dialog {
  protected Context context;
  protected View rootView;

  protected String titleStr;
  protected String contentStr;
  protected String positiveStr;
  protected String negativeStr;
  protected boolean isCancelOutSide = true;

  protected View.OnClickListener positiveListener;
  protected View.OnClickListener negativeListener;
  private TextView tvNegative;

  public CommonDialog(@NonNull Context context) {
    super(context, R.style.CommonDialog);
    this.context = context;
    rootView = LayoutInflater.from(getContext()).inflate(contentLayoutId(), null);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(rootView);
    //fix one plus not show when resume from background
    getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  protected @LayoutRes int contentLayoutId() {
    return R.layout.view_dialog_common_layout;
  }

  /** 页面渲染 */
  protected void renderRootView(View rootView) {
    if (rootView == null) {
      return;
    }
    TextView tvTitle = rootView.findViewById(R.id.tv_dialog_title);
    tvTitle.setText(titleStr);
    tvTitle.setVisibility(!TextUtils.isEmpty(titleStr) ? View.VISIBLE : View.GONE);

    TextView tvContent = rootView.findViewById(R.id.tv_dialog_content);
    tvContent.setText(contentStr);

    TextView tvPositive = rootView.findViewById(R.id.tv_dialog_positive);
    if (!TextUtils.isEmpty(positiveStr)) {
      tvPositive.setText(positiveStr);
    }
    tvPositive.setOnClickListener(
        v -> {
          dismiss();
          if (positiveListener != null) {
            positiveListener.onClick(v);
          }
        });

    tvNegative = rootView.findViewById(R.id.tv_dialog_negative);
    if (!TextUtils.isEmpty(negativeStr)) {
      tvNegative.setText(negativeStr);
    }
    tvNegative.setOnClickListener(
        v -> {
          dismiss();
          if (negativeListener != null) {
            negativeListener.onClick(v);
          }
        });
    setCanceledOnTouchOutside(isCancelOutSide);
  }

  public CommonDialog setCanceledOutside(boolean isCancelOutSide) {
    this.isCancelOutSide = isCancelOutSide;
    return this;
  }

  public CommonDialog setTitle(String title) {
    this.titleStr = title;
    return this;
  }

  public CommonDialog setContent(String content) {
    this.contentStr = content;
    return this;
  }

  public CommonDialog setPositiveBtnName(String positive) {
    this.positiveStr = positive;
    return this;
  }

  public CommonDialog setNegativeBtnName(String negative) {
    this.negativeStr = negative;
    return this;
  }

  public CommonDialog setPositiveOnClickListener(View.OnClickListener listener) {
    this.positiveListener = listener;
    return this;
  }

  public CommonDialog setNegativeOnClickListener(View.OnClickListener listener) {
    this.negativeListener = listener;
    return this;
  }

  public void updateNegativeContent(String contentStr) {
    if (tvNegative == null) {
      tvNegative = rootView.findViewById(R.id.tv_dialog_negative);
    }
    tvNegative.setText(contentStr);
  }

  @Override
  public void show() {
    if (isShowing()) {
      return;
    }
    renderRootView(rootView);
    try {
      super.show();
    } catch (WindowManager.BadTokenException e) {
      ALog.e("ChoiceDialog", "error message is :" + e.getMessage());
    }
  }
}
