// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.netease.yunxin.app.medical.R;

public class SuffererInfoDialog extends Dialog {
  public SuffererInfoDialog(@NonNull Context context) {
    super(context, R.style.CommonDialog);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dialog_sufferer_info);
    initEvent();
    setCancelable(true); //设置点击外部是否消失
  }

  private void initEvent() {
    findViewById(R.id.tv_close).setOnClickListener(view -> dismiss());
  }
}
