// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.app.medical.databinding.SufferCommentItemBinding;
import com.netease.yunxin.app.medical.model.SuffererCommentModel;
import java.util.List;

public class SuffererCommentAdapter
    extends RecyclerView.Adapter<SuffererCommentAdapter.CommentViewHolder> {

  private Context context;
  private List<SuffererCommentModel> list;

  public SuffererCommentAdapter(Context context, List<SuffererCommentModel> list) {
    this.context = context;
    this.list = list;
  }

  @NonNull
  @Override
  public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    SufferCommentItemBinding binding =
        SufferCommentItemBinding.inflate(LayoutInflater.from(context), parent, false);
    CommentViewHolder holder = new CommentViewHolder(binding, context);
    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
    holder.bindData(list.get(position));
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  public static class CommentViewHolder extends RecyclerView.ViewHolder {
    private SufferCommentItemBinding binding;
    private Context context;

    public CommentViewHolder(SufferCommentItemBinding binding, Context context) {
      super(binding.getRoot());
      this.binding = binding;
      this.context = context;
    }

    public void bindData(SuffererCommentModel model) {
      binding.tvDoctorName.setText(model.name);
      binding.tvDate.setText(model.date);
      binding.tvComment.setText(model.content);
      binding.ivSuffererAvatar.setImageResource(model.avatar);
    }
  }
}
