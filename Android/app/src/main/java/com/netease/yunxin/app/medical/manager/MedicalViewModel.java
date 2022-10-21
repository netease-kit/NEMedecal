// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.manager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MedicalViewModel extends ViewModel {
  public MutableLiveData<Integer> roleType = new MutableLiveData();
}
