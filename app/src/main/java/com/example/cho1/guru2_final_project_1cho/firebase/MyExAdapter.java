package com.example.cho1.guru2_final_project_1cho.firebase;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.activity.ExDetailActivity;
import com.example.cho1.guru2_final_project_1cho.bean.ExBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;

import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;


public class MyExAdapter extends BaseAdapter {

    private Context mContext;
    private List<ExBean> mExList;
    private MemberBean mLoginMember;

    public void setList(List<ExBean> exList) {
        mExList = exList;
    }

    public MyExAdapter(Context context, List<ExBean> exList) {
        mContext = context;
        mExList = exList;
    }
    @Override
    public int getCount() {
        return mExList.size();
    }

    @Override
    public Object getItem(int i) {
        return mExList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_ex_item, null);

        mLoginMember = FileDB.getLoginMember(mContext);
        final ExBean exBean = mExList.get(i);

        ImageView imgEx = view.findViewById(R.id.imgEx);
        GradientDrawable drawable=
                (GradientDrawable) mContext.getDrawable(R.drawable.background_rounding);
        imgEx.setBackground(drawable);
        imgEx.setClipToOutline(true);

        TextView txtExMine = view.findViewById(R.id.txtExMine);
        TextView txtExWant = view.findViewById(R.id.txtExWant);
        TextView txtExDate = view.findViewById(R.id.txtExDate);
        TextView txtExId = view.findViewById(R.id.txtExId);

        // imtTitle ???????????? ????????? ?????? ?????? ????????? ?????? ??????????????????, ???????????? ????????????.
        try{
            if(exBean.bmpTitle == null) {
                new DownloadImgTaskEx(mContext, imgEx, mExList, i).execute(new URL(exBean.imgUrl));
            } else {
                imgEx.setImageBitmap(exBean.bmpTitle);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        txtExMine.setText(exBean.mine);
        txtExWant.setText(exBean.want);
        txtExDate.setText(exBean.date);
        StringTokenizer tokens = new StringTokenizer(exBean.userId);
        String userId = tokens.nextToken("@") ;
        txtExId.setText(userId);

        //????????? ?????? ????????? ????????? ????????????
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ExDetailActivity.class);
                //intent.putExtra("INDEX", i); //?????????????????? ??????
                intent.putExtra("ITEM", exBean); //??????????????? ?????? ?????????
                mContext.startActivity(intent);
            }
        });

        return view;
    }
}
