package com.example.cho1.guru2_final_project_1cho.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.cho1.guru2_final_project_1cho.bean.ExBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class FileDB {

    private static final String FILE_DB = "FileDB";
    private static Gson mGson = new Gson();

    private static SharedPreferences getSP(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_DB, Context.MODE_PRIVATE);
        return sp;
    }

    public static List<MemberBean> getMemberList(Context context) {
        String listStr = getSP(context).getString("memberList", null);
        //저장된 리스트가 없을 경우에 새로운 리스트를 리턴한다.
        if(listStr == null) {
            return new ArrayList<MemberBean>();
        }
        //Gson 으로 변환한다.
        List<MemberBean> memberList =
            mGson.fromJson(listStr, new TypeToken<List<MemberBean>>(){}.getType() );
        return memberList;
    }

    public static MemberBean getFindMember(Context context, String memId) {
        //1.멤버리스트를 가져온다
        List<MemberBean> memberList = getMemberList(context);
        //2.for 문 돌면서 해당 아이디를 찾는다.
        for(MemberBean bean : memberList) {
            if(TextUtils.equals(bean.memId, memId)) { //아이디가 같다.
                //3.찾았을 경우는 해당 MemberBean 을 리턴한다.
                return bean;
            }
        }
        //3-2.못찾았을 경우는??? null 리턴
        return null;
    }

    //로그인한 MemberBean 을 저장한다.
    public static void setLoginMember(Context context, MemberBean bean) {
        if(bean != null) {
            String str = mGson.toJson(bean);
            SharedPreferences.Editor editor = getSP(context).edit();
            editor.putString("loginMemberBean", str);
            editor.commit();
        }
    }

    //기존 멤버 교체
    public static void setMember(Context context, MemberBean memberBean){
        //전체 멤버 리스트를 취득한다.
        List<MemberBean> memberList = getMemberList(context);
        if(memberList.size() == 0) return;

        for(int i=0; i<memberList.size(); i++){
            MemberBean bean = memberList.get(i);
            if(TextUtils.equals(bean.memId, memberBean.memId)){
                //같은 멤버Id를 찾았다.
                memberList.set(i, memberBean);
                break;
            }
        }
        //새롭게 update 된 리스트를 저장한다
        String jsonStr = mGson.toJson(memberList);
        //멤버 리스트를 저장한다.
        SharedPreferences.Editor editor = getSP(context).edit();
        editor.putString("memberList", jsonStr);
        editor.commit();
    }

    //로그인한 MemberBean 을 취득한다.
    public static MemberBean getLoginMember(Context context) {
        String str = getSP(context).getString("loginMemberBean", null);
        if(str == null) return null;
        MemberBean memberBean = mGson.fromJson(str, MemberBean.class);
        return memberBean;
    }

    /* 물물교환 */
    // 물물교환게시글을 추가
    public static void addEx(Context context, String memId, ExBean exBean){
        MemberBean findMember = getFindMember(context, memId);
        if(findMember == null) return; // 해당 멤버가 없다면

        List<ExBean> exList = findMember.exList;
        if(exList == null){
            exList = new ArrayList<>();
        }
        // 고유 게시글 ID를 생성해 준다
        long temp = System.currentTimeMillis();
        exBean.id = String.valueOf(temp); // 삭제하고 새로 생성됐을 때 인덱스가 겹치기 때문에 이를 방지하기 위해 게시글이 만들어지는 시간으로 고유 ID 생성
        exList.add(0, exBean);
        findMember.exList = exList;
        // 저장
        setMember(context, findMember); // 기존의 memberBean을 갈아끼우면서 저장
    }

    // 물물교환 게시글 수정
    public static void setEx(Context context, ExBean exBean){
        MemberBean memberBean = getLoginMember(context);
        if(memberBean != null || memberBean.exList == null){
            return;
        }
        List<ExBean> exList = memberBean.exList;
        for(int i=0; i<exList.size(); i++){
            ExBean eBean = exList.get(i);
            if(eBean.id == exBean.id) {
                // 찾았다.
                exList.set(i, exBean); // 교체
                break;
            }
        }
        // 업데이트된 물물교환 게시글 리스트를 저장
        memberBean.exList = exList;
        setMember(context, memberBean);
    }

    // 물물교환 게시글 삭제
    public static void delEx(Context context, String id){
        MemberBean memberBean = getLoginMember(context); // 로그인된 멤버를 memberBean에 넣어줌
        List<ExBean> exList = memberBean.exList;
        if(exList == null) return; // 아무 게시글도 없다면 리턴

        for(int i=0; i<exList.size(); i++){ // 게시글리스트의 개수만큼 반복해서 끝까지
            ExBean eBean = exList.get(i);
            if(eBean.id == id) {
                // 찾았다.
                exList.remove(i);
                break;
            }
        }
        // 저장
        memberBean.exList = exList;
        setMember(context, memberBean);

    }

    // 어떠한 물물교환 게시글을 삭제,수정하기 위해 그 게시글이 어떤 게시글인지 알기 위한 게시글 ID를 찾는다
    public static ExBean getEx(Context context, String id){
        MemberBean memberBean = getLoginMember(context);
        List<ExBean> exList = memberBean.exList;
        if(exList == null) return null;

        for(ExBean bean: exList){
            if(bean.id == id){
                // 찾았다.
                return bean;
            }
        }
        return null;
    }

    // 물물교환 리스트 취득
    public static List<ExBean> getExList(Context context) {
        MemberBean memberBean = getLoginMember(context);
        if (memberBean == null) return null; // 만약 멤버에 없다면 아무것도 하면 안됨

        if (memberBean.exList == null) {
            return new ArrayList<>();
        } else {
            return memberBean.exList;
        }

    }

    /* End 물물교환 */
}
