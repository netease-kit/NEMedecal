// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.manager.UserInfoManager;
import com.netease.yunxin.app.medical.ui.view.CommonDialog;
import com.netease.yunxin.app.medical.ui.view.HelpNotesDialog;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.app.medical.utils.SpUtils;

public abstract class HomeFragment extends BaseFragment {

  /** 展示 角色切换对话框 */
  protected void showChangeRoleDialog() {
    int role = SpUtils.getInstance().getInt(AppConstants.ROLE, -1);
    String roleStr = getResources().getString(R.string.medical_doctor);
    if (role == Role.DOCTOR) {
      roleStr = getResources().getString(R.string.medical_sufferer);
    }
    CommonDialog dialog = new CommonDialog(getContext());
    dialog
        .setTitle(getResources().getString(R.string.medical_change_role_title))
        .setContent(getResources().getString(R.string.medical_change_role_content) + roleStr)
        .setPositiveOnClickListener(
            v -> {
              changeRole();
            })
        .show();
  }

  /** 切换角色 */
  private void changeRole() {
    int role = SpUtils.getInstance().getInt(AppConstants.ROLE, -1);
    int changeToRole;
    if (role == Role.DOCTOR) {
      changeToRole = Role.SUFFERER;
    } else {
      changeToRole = Role.DOCTOR;
    }
    UserInfoManager.updateUserInfo(
        null,
        changeToRole,
        new NECallback() {
          @Override
          public void onSuccess(Object o) {
            viewModel.roleType.postValue(changeToRole);
          }

          @Override
          public void onError(int code, String errorMsg) {}
        });
  }

  protected void showDemoDescDialog() {
    new HelpNotesDialog().show(getChildFragmentManager(), "");
  }

  @Override
  protected void roleChanged(int role) {}
}
