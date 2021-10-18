package com.example.cho1.guru2_final_project_1cho.bean;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ExBean implements Serializable {

    public String id; // 게시글 id
    public String userId; // 이메일
    public String imgUrl;
    public String imgName;
    public String mine; // 내 물건
    public String want; // 교환하고 싶은 물건
    public String price; // 내 물건 원가
    public String state; // 물건 상태
    public String fault; // 하자
    public String expire; // 유통기한
    public String size; // 사이즈

    public transient Bitmap bmpTitle;

    public String date; // 게시물 올린 날짜
    public String buyDate; // 물건을 구매한 날짜

    public List<CommentBean> commentList = new ArrayList<>();
}
