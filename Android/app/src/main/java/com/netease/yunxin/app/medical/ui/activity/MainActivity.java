// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.constant.AppRtcConfig;
import com.netease.yunxin.app.medical.constant.CallConfig;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.databinding.ActivityMainBinding;
import com.netease.yunxin.app.medical.http.HttpService;
import com.netease.yunxin.app.medical.manager.MedicalViewModel;
import com.netease.yunxin.app.medical.manager.UserInfoManager;
import com.netease.yunxin.app.medical.ui.fragment.ExperienceFragment;
import com.netease.yunxin.app.medical.ui.fragment.HomeDoctorFragment;
import com.netease.yunxin.app.medical.ui.fragment.HomeSuffererFragment;
import com.netease.yunxin.app.medical.ui.fragment.MessageFragment;
import com.netease.yunxin.app.medical.ui.fragment.MineFragment;
import com.netease.yunxin.app.medical.ui.view.IMCustomConfig;
import com.netease.yunxin.app.medical.ui.view.StatusBarConfig;
import com.netease.yunxin.app.medical.utils.HighKeepAliveUtil;
import com.netease.yunxin.app.medical.utils.LogUtil;
import com.netease.yunxin.app.medical.utils.NECallback;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.app.medical.utils.RtcUtil;
import com.netease.yunxin.app.medical.utils.SpUtils;
import com.netease.yunxin.kit.conversationkit.repo.ConversationRepo;
import com.netease.yunxin.nertc.pstn.PstnUIHelper;
import com.netease.yunxin.nertc.pstn.base.PstnCallKitOptions;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;
import com.netease.yunxin.nertc.ui.extension.SelfConfigExtension;
import java.util.List;

/// 主页面
public class MainActivity extends BaseActivity {

  private ActivityMainBinding binding;
  private static final String TAG = "MainActivity";
  private static final int TAB_HOME_DOCTOR = 0;
  private static final int TAB_HOME_SUFFERER = 1;
  private static final int TAB_MSG = 2;
  private static final int TAB_EXPERIENCE = 3;
  private static final int TAB_MINE = 4;
  private HomeSuffererFragment homeSuffererFragment;
  private HomeDoctorFragment homeDoctorFragment;
  private MessageFragment messageFragment;
  private ExperienceFragment experienceFragment;
  private MineFragment mineFragment;
  public int curTabIndex = -1;
  private MedicalViewModel viewModel;
  private Observer<List<IMMessage>> incomingMessageObserver;

  @Override
  protected boolean hasTitle() {
    return false;
  }

  @Override
  protected View getContentView() {
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    loadConfig();
    viewModel =
        new ViewModelProvider(
                this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory())
            .get(MedicalViewModel.class);
    switchToHome();
    initCallKit();
    isShowDot(ConversationRepo.getMsgUnreadCount() > 0);
    HighKeepAliveUtil.openHighKeepAlive(this);
  }

  @Override
  protected StatusBarConfig provideStatusBarConfig() {
    StatusBarConfig config =
        new StatusBarConfig.Builder()
            .statusBarDarkFont(true)
            .statusBarColor(R.color.color_EFF1F4)
            .build();
    return config;
  }

  protected void setEvent() {
    binding.tvHome.setOnClickListener(
        view -> {
          switchToHome();
        });
    binding.tvMsg.setOnClickListener(view -> selectFragment(TAB_MSG));
    binding.tvExperience.setOnClickListener(view -> selectFragment(TAB_EXPERIENCE));
    binding.tvMine.setOnClickListener(view -> selectFragment(TAB_MINE));
    viewModel.roleType.observe(
        this,
        role -> {
          handleRoleFragment(role);
          if (curTabIndex == TAB_HOME_DOCTOR || curTabIndex == TAB_HOME_SUFFERER) {
            if (role == Role.DOCTOR) {
              selectFragment(TAB_HOME_DOCTOR);
            } else {
              selectFragment(TAB_HOME_SUFFERER);
            }
          }
        });

    incomingMessageObserver = imMessages -> isShowDot(true);
    NIMClient.getService(MsgServiceObserve.class)
        .observeReceiveMessage(incomingMessageObserver, true);
  }

  // im ui kit 配置
  private void loadConfig() {
    IMCustomConfig.configConversationUI();
    IMCustomConfig.configChatKit(this);
  }

  public void isShowDot(boolean isShow) {
    if (isShow) {
      binding.conversationDot.setVisibility(View.VISIBLE);
    } else {
      binding.conversationDot.setVisibility(View.GONE);
    }
  }

  private void switchToHome() {
    int role = SpUtils.getInstance().getInt(AppConstants.ROLE, Role.SUFFERER);
    if (role == Role.DOCTOR) {
      selectFragment(TAB_HOME_DOCTOR);
    } else {
      selectFragment(TAB_HOME_SUFFERER);
    }
  }

  private void handleRoleFragment(Integer role) {
    if (role == Role.SUFFERER) {
      binding.tvHome.setText(getString(R.string.medical_home_page));
      Drawable drawable = getResources().getDrawable(R.drawable.tab_home_selector);
      binding.tvHome.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
    } else {
      binding.tvHome.setText(getString(R.string.medical_home_doctor_page));
      Drawable drawable = getResources().getDrawable(R.drawable.tab_home_sufferer_selector);
      binding.tvHome.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
    }
  }

  private void selectFragment(int tabIndex) {
    if (tabIndex == curTabIndex) {
      LogUtil.i(TAG, "tabIndex==curTabIndex");
      return;
    }
    ImmersionBar bar;
    if (tabIndex == TAB_MINE) {
      bar = ImmersionBar.with(this).statusBarDarkFont(true).statusBarColor(R.color.white);
    } else {
      bar = ImmersionBar.with(this).statusBarDarkFont(true).statusBarColor(R.color.color_EFF1F4);
    }
    bar.init();
    FragmentManager supportFragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
    Fragment currentFragment = supportFragmentManager.findFragmentByTag(curTabIndex + "");
    if (currentFragment != null) {
      fragmentTransaction.hide(currentFragment);
      LogUtil.i(TAG, "hide:" + currentFragment);
    }
    curTabIndex = tabIndex;
    Fragment fragment = supportFragmentManager.findFragmentByTag(tabIndex + "");
    if (fragment == null) {
      switch (tabIndex) {
        case TAB_HOME_DOCTOR:
          if (homeDoctorFragment == null) {
            handleRoleFragment(SpUtils.getInstance().getInt(AppConstants.ROLE, Role.SUFFERER));
            homeDoctorFragment = new HomeDoctorFragment();
          }
          fragment = homeDoctorFragment;
          break;

        case TAB_HOME_SUFFERER:
          if (homeSuffererFragment == null) {
            handleRoleFragment(SpUtils.getInstance().getInt(AppConstants.ROLE, Role.SUFFERER));
            homeSuffererFragment = new HomeSuffererFragment();
          }
          fragment = homeSuffererFragment;
          break;

        case TAB_MSG:
          if (messageFragment == null) {
            messageFragment = new MessageFragment();
          }
          fragment = messageFragment;
          break;
        case TAB_EXPERIENCE:
          if (experienceFragment == null) {
            experienceFragment = new ExperienceFragment();
          }
          fragment = experienceFragment;
          break;
        case TAB_MINE:
          if (mineFragment == null) {
            mineFragment = new MineFragment();
          }
          fragment = mineFragment;
          break;
        default:
          break;
      }
      LogUtil.i(TAG, "add:" + fragment);
      fragmentTransaction.add(R.id.fragment_container, fragment, tabIndex + "");
    } else {
      LogUtil.i(TAG, "show:" + fragment);
      fragmentTransaction.show(fragment);
    }
    if ((fragment instanceof HomeDoctorFragment) || (fragment instanceof HomeSuffererFragment)) {
      handleBottomSelectedState(true, false, false, false);
    } else if (fragment instanceof MessageFragment) {
      handleBottomSelectedState(false, true, false, false);
    } else if (fragment instanceof MineFragment) {
      handleBottomSelectedState(false, false, false, true);
    } else if (fragment instanceof ExperienceFragment) {
      handleBottomSelectedState(false, false, true, false);
    }
    fragmentTransaction.commit();
  }

  private void handleBottomSelectedState(
      boolean homeSelected, boolean msgSelected, boolean experienceSelected, boolean mineSelected) {
    binding.tvHome.setSelected(homeSelected);
    binding.tvMsg.setSelected(msgSelected);
    binding.tvMine.setSelected(mineSelected);
    binding.tvExperience.setSelected(experienceSelected);
  }

  private void initCallKit() {
    NERtcOption neRtcOption = new NERtcOption();
    neRtcOption.logLevel = NERtcConstants.LogLevel.INFO;
    CallKitUIOptions options =
        new CallKitUIOptions.Builder()
            // 必要：音视频通话 sdk appKey，用于通话中使用
            .rtcAppKey(AppConstants.getAppKey())
            // 必要：当前用户 AccId
            .currentUserAccId(UserInfoManager.getSelfImAccid())
            // 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
            .notificationConfigFetcher(
                invitedInfo -> new CallKitNotificationConfig(R.mipmap.ic_launcher))
            // 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
            .resumeBGInvitation(true)
            .enableOrder(false)
            .rtcCallExtension(
                new SelfConfigExtension() {
                  @Override
                  public void configVideoConfig() {
                    RtcUtil.configVideoConfig(AppRtcConfig.VIDEO_WIDTH, AppRtcConfig.VIDEO_HEIGHT);
                  }
                })
            .rtcTokenService(
                (uid, channel, callback) ->
                    HttpService.requestRtcToken(
                        uid,
                        new NECallback<String>() {
                          @Override
                          public void onSuccess(String s) {
                            LogUtil.d("getToken", "response token:" + s);
                            callback.onSuccess(s);
                          }

                          @Override
                          public void onError(int code, String errorMsg) {
                            callback.onFailed(code);
                          }
                        })) // 自己实现的 token 请求方法
            .rtcSdkOption(neRtcOption)
            // 呼叫组件初始化 rtc 范围，true-全局初始化，false-每次通话进行初始化以及销毁
            // 全局初始化有助于更快进入首帧页面，当结合其他组件使用时存在rtc初始化冲突可设置false
            .rtcInitScope(true)
            .p2pAudioActivity(CallActivity.class)
            .p2pVideoActivity(CallActivity.class)
            .build();
    // 若重复初始化会销毁之前的初始化实例，重新初始化
    PstnCallKitOptions pstnCallKitOptions =
        new PstnCallKitOptions.Builder(options)
            .timeOutMillisecond(CallConfig.CALL_TOTAL_WAIT_TIMEOUT)
            .transOutMillisecond(CallConfig.CALL_PSTN_WAIT_MILLISECONDS)
            .build();
    PstnUIHelper.init(getApplicationContext(), pstnCallKitOptions);
  }

  @Override
  protected void onKickOut() {
    super.onKickOut();
    NavUtils.toLoginHomePage(this);
    if (incomingMessageObserver != null) {
      NIMClient.getService(MsgServiceObserve.class)
          .observeReceiveMessage(incomingMessageObserver, false);
    }
    finish();
  }
}
