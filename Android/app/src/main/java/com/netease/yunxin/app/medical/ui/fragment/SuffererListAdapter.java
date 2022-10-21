// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.databinding.SuffererItemBinding;
import com.netease.yunxin.app.medical.model.SuffererModel;
import com.netease.yunxin.app.medical.ui.activity.AuthNameActivity;
import com.netease.yunxin.app.medical.ui.view.CommonDialog;
import com.netease.yunxin.app.medical.utils.NavUtils;
import java.util.List;

public class SuffererListAdapter
    extends RecyclerView.Adapter<SuffererListAdapter.SuffererViewHolder> {

  private Context context;
  private List<SuffererModel> list;

  public SuffererListAdapter(Context context, List<SuffererModel> list) {
    this.context = context;
    this.list = list;
  }

  @NonNull
  @Override
  public SuffererViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    SuffererViewHolder holder =
        new SuffererViewHolder(
            SuffererItemBinding.inflate(LayoutInflater.from(context), parent, false), context);
    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull SuffererViewHolder holder, int position) {
    holder.bindData(list.get(position));
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  public static class SuffererViewHolder extends RecyclerView.ViewHolder {
    private SuffererItemBinding binding;
    private Context context;

    public SuffererViewHolder(SuffererItemBinding binding, Context context) {
      super(binding.getRoot());
      this.binding = binding;
      this.context = context;
    }

    public void bindData(SuffererModel model) {
      binding.ivSuffererAvatar.setImageResource(model.avatar);
      binding.tvSuffererName.setText(model.name);
      binding.ivSuffererSex.setImageResource(model.sexDrawable);
      binding.tvSuffererSex.setText(model.sexString);
      binding.tvSuffererAge.setText(model.age);
      binding.ivImgTxtConsultation.setOnClickListener(v -> NavUtils.toTxtImgConsultation(context));
      binding.ivVideoConsultation.setOnClickListener(
          v -> {
            CommonDialog dialog = new CommonDialog(context);
            dialog
                .setTitle(context.getResources().getString(R.string.medical_auth_name_title))
                .setContent(context.getResources().getString(R.string.medical_auth_name_tips))
                .setPositiveBtnName(context.getResources().getString(R.string.medical_yes))
                .setNegativeBtnName(context.getResources().getString(R.string.medical_no))
                .setPositiveOnClickListener(
                    v1 -> {
                      NavUtils.toAuthNamePage(context);
                      AuthNameActivity.fromPage = AuthNameActivity.FROM_VIDEO;
                    })
                .setNegativeOnClickListener(
                    v12 -> {
                      NavUtils.toVideoConsultation(context);
                    })
                .show();
          });
      binding.ivAudioConsultation.setOnClickListener(v -> NavUtils.toAudioConsultation(context));
    }
  }
}
