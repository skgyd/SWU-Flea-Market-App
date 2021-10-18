package com.example.cho1.guru2_final_project_1cho.bean;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//회원정보 Bean
public class MemberBean implements Serializable {

    public String memId;
    public String memName;
    public String memPw;
    public String imgUrl;
    public String imgName;
    public String date;

    public List<ExBean> exList;
    public transient Bitmap bmpTitle;

    public List<CommentBean> commentList = new ArrayList<>();
}
