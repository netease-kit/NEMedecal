// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.app.medical.databinding.DoctorItemBinding;
import com.netease.yunxin.app.medical.model.DoctorModel;
import com.netease.yunxin.app.medical.utils.NavUtils;
import java.util.List;

public class DoctorListAdapter extends RecyclerView.Adapter<DoctorListAdapter.DoctorHolder> {

  private Context context;
  private List<DoctorModel> list;

  public DoctorListAdapter(Context context, @NonNull List<DoctorModel> list) {
    this.context = context;
    this.list = list;
  }

  @NonNull
  @Override
  public DoctorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    DoctorItemBinding binding =
        DoctorItemBinding.inflate(LayoutInflater.from(context), parent, false);
    return new DoctorHolder(context, binding);
  }

  @Override
  public void onBindViewHolder(@NonNull DoctorHolder holder, int position) {
    DoctorModel model = list.get(position);
    DoctorHolder doctorHolder = (DoctorHolder) holder;
    doctorHolder.bindData(model, position);
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  public static class DoctorHolder extends RecyclerView.ViewHolder {
    private DoctorItemBinding binding;
    private Context context;

    public DoctorHolder(Context context, @NonNull DoctorItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
      this.context = context;
    }

    public void bindData(DoctorModel model, int position) {
      binding.ivAvatar.setImageResource(model.doctorAvatar);
      binding.tvDoctorName.setText(model.doctorName);
      binding.tvPosition.setText(model.doctorPosition);
      binding.tvDepartment.setText(model.doctorDepartment);
      binding.tvGoodAt.setText(model.goodAt);
      binding.tvSuffererCount.setText(model.suffererCount);
      binding
          .getRoot()
          .setOnClickListener(
              v -> {
                NavUtils.toDoctorDetail(context, model);
              });
    }
  }
}
