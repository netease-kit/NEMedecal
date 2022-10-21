// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.constant.AppConstants;
import com.netease.yunxin.app.medical.constant.Role;
import com.netease.yunxin.app.medical.utils.SpUtils;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenuAction;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenu;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import java.util.ArrayList;
import java.util.List;

// im ui相关配置
public class IMCustomConfig {

  // 聊天页面ui 配置
  public static void configChatKit(Context context) {
    ChatUIConfig uiConfig = new ChatUIConfig();
    uiConfig.messageProperties = new MessageProperties();
    uiConfig.messageProperties.showTitleBarRightIcon = false; // 标题栏 右侧icon不展示
    uiConfig.chatViewCustom =
        layout -> { // 底部输入 布局自定义
          int role = SpUtils.getInstance().getInt(AppConstants.ROLE, -1);
          if (role == Role.SUFFERER) {
            return;
          }
          FrameLayout frameLayout = layout.getChatBodyBottomLayout();
          IMBottomView imBottomView = new IMBottomView(context);
          imBottomView.setClickListener(
              new IMBottomView.IBottomViewClick() {
                @Override
                public void clickSuffererInfo() {
                  new SuffererInfoDialog(layout.getContext()).show();
                }

                @Override
                public void clickHistory() {
                  ToastX.showShortToast(context.getString(R.string.medical_feature_tip));
                }

                @Override
                public void clickEndConsultation() {
                  ToastX.showShortToast(context.getString(R.string.medical_feature_tip));
                }
              });
          FrameLayout.LayoutParams layoutParams =
              new FrameLayout.LayoutParams(
                  ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
          layoutParams.leftMargin = context.getResources().getDimensionPixelSize(R.dimen.dp_16);
          frameLayout.addView(imBottomView, layoutParams);
        };
    uiConfig.chatPopMenu =
        new IChatPopMenu() { // 长按消息 消息操作菜单自定义
          @NonNull
          @Override
          public List<ChatPopMenuAction> customizePopMenu(
              List<ChatPopMenuAction> menuList, ChatMessageBean messageBean) {
            List<ChatPopMenuAction> customMenuList = new ArrayList<>();
            for (int i = 0; i < menuList.size(); i++) {
              ChatPopMenuAction menuAction = menuList.get(i);
              if (!TextUtils.equals(menuAction.getAction(), ActionConstants.POP_ACTION_TRANSMIT)
                  && !TextUtils.equals(menuAction.getAction(), ActionConstants.POP_ACTION_PIN)) {
                customMenuList.add(menuAction);
              }
            }
            menuList.clear();
            menuList.addAll(customMenuList);
            return menuList;
          }

          @Override
          public boolean showDefaultPopMenu() {
            return true;
          }
        };
    ChatKitClient.setChatUIConfig(uiConfig);
  }

  // 会话页面配置
  public static void configConversationUI() {
    ConversationUIConfig conversationUIConfig = new ConversationUIConfig();
    conversationUIConfig.showTitleBar = false; // 设置会话页面不展示title
    ConversationKitClient.setConversationUIConfig(conversationUIConfig);
  }
}
