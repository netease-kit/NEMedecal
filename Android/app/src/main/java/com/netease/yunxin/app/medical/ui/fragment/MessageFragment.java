// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.databinding.MessageFragmentBinding;
import com.netease.yunxin.app.medical.ui.activity.MainActivity;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationFragment;
import java.util.List;

public class MessageFragment extends BaseFragment {

  private MessageFragmentBinding binding;
  private static final String TAG = "MessageFragment";

  @Override
  protected View providerView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = MessageFragmentBinding.inflate(getLayoutInflater(), container, false);
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    NIMClient.getService(MsgService.class)
        .queryRecentContacts(null, QueryDirectionEnum.QUERY_OLD, 100)
        .setCallback(
            new RequestCallback<List<RecentContact>>() {
              @Override
              public void onSuccess(List<RecentContact> result) {
                if (result != null && result.size() > 0) {
                  FragmentManager supportFragmentManager = getChildFragmentManager();
                  FragmentTransaction fragmentTransaction =
                      supportFragmentManager.beginTransaction();
                  ConversationFragment fragment = new ConversationFragment();
                  fragmentTransaction.add(R.id.frag_container, fragment, "fragment");
                  fragmentTransaction.commit();
                  initConversationFragment(fragment);
                  binding.ivNoMessage.setVisibility(View.GONE);
                  binding.fragContainer.setVisibility(View.VISIBLE);
                } else {
                  binding.fragContainer.setVisibility(View.GONE);
                  binding.ivNoMessage.setVisibility(View.VISIBLE);
                }
              }

              @Override
              public void onFailed(int code) {
                binding.fragContainer.setVisibility(View.GONE);
                binding.ivNoMessage.setVisibility(View.VISIBLE);
              }

              @Override
              public void onException(Throwable exception) {
                binding.fragContainer.setVisibility(View.GONE);
                binding.ivNoMessage.setVisibility(View.VISIBLE);
              }
            });
  }

  private void initConversationFragment(ConversationFragment conversationFragment) {
    if (conversationFragment != null) {
      conversationFragment.setConversationCallback(
          count -> {
            ((MainActivity) requireActivity()).isShowDot(count > 0);
            NIMClient.getService(MsgService.class)
                .queryRecentContacts(null, QueryDirectionEnum.QUERY_OLD, 100)
                .setCallback(
                    new RequestCallback<List<RecentContact>>() {
                      @Override
                      public void onSuccess(List<RecentContact> result) {
                        if (result == null || result.size() == 0) {
                          binding.fragContainer.setVisibility(View.GONE);
                          binding.ivNoMessage.setVisibility(View.VISIBLE);
                        } else {
                          binding.fragContainer.setVisibility(View.VISIBLE);
                          binding.ivNoMessage.setVisibility(View.GONE);
                        }
                      }

                      @Override
                      public void onFailed(int code) {}

                      @Override
                      public void onException(Throwable exception) {}
                    });
          });
    }
  }

  @Override
  protected void initEvent() {}

  @Override
  public void onResume() {
    super.onResume();
    initView();
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    if (!hidden) {
      initView();
    }
  }

  @Override
  protected void roleChanged(int role) {}
}
