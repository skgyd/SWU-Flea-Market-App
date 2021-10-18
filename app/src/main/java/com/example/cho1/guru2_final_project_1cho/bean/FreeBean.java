package com.example.cho1.guru2_final_project_1cho.bean;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class FreeBean implements Serializable {

    public String id; // 게시글 id
    public String userId; // 이메일

    public String imgUrl;
    public String imgName;

    public String title;
    public String explain;

    public String place;
    public String detailPlace;

    public transient Bitmap bmpTitle;

    public String date; // 게시물 올린 날짜

    public Boolean isCompleted;

    /* 위치 추가 */
}
