// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.model;

import org.json.JSONException;
import org.json.JSONObject;

public class PersonModel {
  public String name;
  public int sex; // 1.男 2.女

  public PersonModel(String name, int sex) {
    this.name = name;
    this.sex = sex;
  }

  public String toJson() {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("name", name);
      jsonObject.put("sex", sex);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return jsonObject.toString();
  }

  public static PersonModel createModelFromJson(String json) {
    PersonModel model = null;
    try {
      JSONObject jsonObject = new JSONObject(json);
      String name = jsonObject.getString("name");
      int sex = jsonObject.getInt("sex");
      model = new PersonModel(name, sex);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return model;
  }
}
