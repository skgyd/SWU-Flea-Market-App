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
import com.example.cho1.guru2_final_project_1cho.activity.BuyDetailActivity;
import com.example.cho1.guru2_final_project_1cho.activity.FreeDetailActivity;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.FreeBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;

import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;

public class MyFreeAdapter extends BaseAdapter {

    private Context mContext;
    private List<FreeBean> mFreeList;
    private MemberBean mLoginMember;

    public MyFreeAdapter(Context context, List<FreeBean> freeList) {
        mContext = context;
        mFreeList = freeList;
    }

    public void setList(List<FreeBean> freeList) {
        mFreeList = freeList;
    }

    @Override
    public int getCount() {
        return mFreeList.size();
    }

    @Override
    public Object getItem(int i) {
        return mFreeList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_free_item, null);

        mLoginMember = FileDB.getLoginMember(mContext);
        final FreeBean freeBean = mFreeList.get(i);

        //if(TextUtils.equals(mLoginMember.memId, fleaBean.userId)) {
            ImageView imgFree = view.findViewById(R.id.imgFree);
            GradientDrawable drawable =
                    (GradientDrawable) mContext.getDrawable(R.drawable.background_rounding);
            imgFree.setBackground(drawable);
            imgFree.setClipToOutline(true);

            TextView txtFreeTitle = view.findViewById(R.id.txtFreeTitle);
            TextView txtFreeExplain = view.findViewById(R.id.txtFreeExplain);
            TextView txtFreeId = view.findViewById(R.id.txtFreeId);
            TextView txtFreePlace = view.findViewById(R.id.txtFreePlace);
            TextView txtFreeDate = view.findViewById(R.id.txtFreeDate);


            // imgTitle ???????????? ????????? ?????? ?????? ????????? ?????? ??????????????????, ???????????? ????????????.
            try {
                if (freeBean.bmpTitle == null) {
                    new DownloadImgTaskFree(mContext, imgFree, mFreeList, i).execute(new URL(freeBean.imgUrl));
                } else {
                    imgFree.setImageBitmap(freeBean.bmpTitle);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //ui??? ?????? ????????? ??????
            txtFreeTitle.setText(freeBean.title);
            txtFreeExplain.setText(freeBean.explain);
            txtFreeDate.setText(freeBean.date);
            txtFreePlace.setText(freeBean.place);
            StringTokenizer tokens = new StringTokenizer(freeBean.userId);
            String userId = tokens.nextToken("@");
            txtFreeId.setText(userId);

            //????????? ?????? ????????? ????????? ????????????
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, FreeDetailActivity.class);
                    //intent.putExtra("INDEX", i); //?????????????????? ??????
                    intent.putExtra("FREEITEM", freeBean); //??????????????? ?????? ?????????
                    intent.putExtra("TITLE", freeBean.title);
                    mContext.startActivity(intent);
                }
            });

            return view;
        //}

        //return null;
    }

}
