// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/** 预览画面的比例固定为 4:3 的 SurfaceView。 */
public class ExSurfaceView extends SurfaceView {

  public ExSurfaceView(Context context) {
    this(context, null, 0);
  }

  public ExSurfaceView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ExSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = width / 3 * 4;
    setMeasuredDimension(width, height);
  }
}
