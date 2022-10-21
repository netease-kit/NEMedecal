// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.utils;

import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.model.DoctorModel;
import com.netease.yunxin.app.medical.model.PersonModel;
import com.netease.yunxin.app.medical.model.SuffererCommentModel;
import com.netease.yunxin.app.medical.model.SuffererModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MocDataUtil {

  public static List<DoctorModel> mockDoctorData() {
    List<DoctorModel> models = new ArrayList<>();
    DoctorModel doctorOne =
        new DoctorModel(
            "王伟",
            R.drawable.avatar_doctor_one,
            "主治医师",
            "第一人民医院  呼吸内科",
            "近七日接诊  30    好评率 100%",
            "擅长：肺炎、慢阻肺、睡眠呼吸障碍等常见病及疑难病诊治");

    DoctorModel doctorTwo =
        new DoctorModel(
            "李秀琴",
            R.drawable.avatar_doctor_two,
            "主任医师",
            "第一人民医院  呼吸内科",
            "近七日接诊  88    好评率 100%",
            "擅长：呼吸系统常见病及疑难病诊治");
    DoctorModel doctorThree =
        new DoctorModel(
            "林自在",
            R.drawable.avatar_doctor_three,
            "主治医师",
            "第一人民医院  呼吸内科",
            "近七日接诊  34    好评率 99%",
            "擅长：各种老年呼吸疾病和危重病，尤其是老年肺癌、老年综合征等的治疗");
    models.add(doctorOne);
    models.add(doctorTwo);
    models.add(doctorThree);
    return models;
  }

  public static List<SuffererCommentModel> mockSuffererComment() {
    List<SuffererCommentModel> models = new ArrayList<>();
    SuffererCommentModel commentOne =
        new SuffererCommentModel(
            "张*伟",
            R.drawable.sufferer_avatar_one,
            "2022.05.21",
            "大夫耐心专业，讲解了我的每一个疑问，给的建议也非常能帮助我进一步了解家人病情，真希望这样的医生多一些");
    SuffererCommentModel commentTwo =
        new SuffererCommentModel(
            "钱*多", R.drawable.sufferer_avatar_two, "2022.03.19", "医生特别耐心，回复很及时，开的药楼下药店都买的到。");
    SuffererCommentModel commentThree =
        new SuffererCommentModel(
            "刘**",
            R.drawable.sufferer_avatar_three,
            "2022.02.28",
            "在手机上提问很方便，直接把病历、报告拍给医生看就行，感谢医生耐心的回复");
    models.add(commentOne);
    models.add(commentTwo);
    models.add(commentThree);
    return models;
  }

  public static List<SuffererModel> mockSuffererData() {
    List<SuffererModel> models = new ArrayList<>();
    SuffererModel suffererOne =
        new SuffererModel("张三", R.drawable.icon_male, "男", "30岁", R.drawable.sufferer_avatar_one);
    SuffererModel suffererTwo =
        new SuffererModel("李四", R.drawable.icon_female, "女", "18岁", R.drawable.sufferer_avatar_two);
    SuffererModel suffererThree =
        new SuffererModel("陈仁", R.drawable.icon_male, "男", "28岁", R.drawable.sufferer_avatar_three);
    models.add(suffererOne);
    models.add(suffererTwo);
    models.add(suffererThree);
    return models;
  }

  public static List<PersonModel> mockPersonData() {
    List<PersonModel> models = new ArrayList<>();
    models.add(new PersonModel("冯华锋", 1));
    models.add(new PersonModel("郑建宝", 1));
    models.add(new PersonModel("白庚", 1));
    models.add(new PersonModel("张雪丹", 2));
    models.add(new PersonModel("祖己", 1));
    models.add(new PersonModel("朱志瑞", 1));
    models.add(new PersonModel("袁婷", 2));
    models.add(new PersonModel("易建辉", 1));
    models.add(new PersonModel("张宣", 2));
    models.add(new PersonModel("余新东", 1));
    models.add(new PersonModel("郁斐", 2));
    models.add(new PersonModel("郑道宏", 1));
    models.add(new PersonModel("赵贵辰", 1));
    models.add(new PersonModel("周学奎", 1));
    models.add(new PersonModel("孙力军", 1));
    models.add(new PersonModel("江永成", 1));
    models.add(new PersonModel("周瑞佳", 2));
    models.add(new PersonModel("张石明", 1));
    models.add(new PersonModel("刘晶", 2));
    models.add(new PersonModel("玉隐", 2));
    models.add(new PersonModel("裴宇", 1));
    models.add(new PersonModel("赵志斐", 1));
    models.add(new PersonModel("尹晨", 1));
    models.add(new PersonModel("张风江", 1));
    models.add(new PersonModel("邢建飞", 1));
    models.add(new PersonModel("柏鹏飞", 1));
    models.add(new PersonModel("朱继国", 1));
    models.add(new PersonModel("赵东宁", 1));
    models.add(new PersonModel("周愈", 1));
    models.add(new PersonModel("马雪晴", 2));
    return models;
  }

  public static PersonModel getRandomPerson() {
    List<PersonModel> models = mockPersonData();
    Random random = new Random();
    int index = random.nextInt(models.size());
    return models.get(index);
  }
}
