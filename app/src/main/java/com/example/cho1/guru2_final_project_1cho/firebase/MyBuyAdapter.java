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
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;

import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;

public class MyBuyAdapter extends BaseAdapter {

    private Context mContext;
    private List<FleaBean> mFleaList;
    private MemberBean mLoginMember;

    public MyBuyAdapter(Context context, List<FleaBean> fleaList) {
        mContext = context;
        mFleaList = fleaList;
    }

    public void setList(List<FleaBean> fleaList) {
        mFleaList = fleaList;
    }

    @Override
    public int getCount() {
        return mFleaList.size();
    }

    @Override
    public Object getItem(int i) {
        return mFleaList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_buy_item, null);

        mLoginMember = FileDB.getLoginMember(mContext);
        final FleaBean fleaBean = mFleaList.get(i);

        //if(TextUtils.equals(mLoginMember.memId, fleaBean.userId)) {
            ImageView imgBuy = view.findViewById(R.id.imgBuy);
            GradientDrawable drawable =
                    (GradientDrawable) mContext.getDrawable(R.drawable.background_rounding);
            imgBuy.setBackground(drawable);
            imgBuy.setClipToOutline(true);

            TextView txtBuyTitle = view.findViewById(R.id.txtBuyTitle);
            TextView txtBuySubTitle = view.findViewById(R.id.txtBuyExplain);
            TextView txtBuyPrice = view.findViewById(R.id.txtBuyPrice);
            TextView txtBuyId = view.findViewById(R.id.txtBuyId);
            TextView txtBuyDate = view.findViewById(R.id.txtBuyDate);


            // imgTitle ???????????? ????????? ?????? ?????? ????????? ?????? ??????????????????, ???????????? ????????????.
            try {
                if (fleaBean.bmpTitle == null) {
                    new DownloadImgTaskFlea(mContext, imgBuy, mFleaList, i).execute(new URL(fleaBean.imgUrl));
                } else {
                    imgBuy.setImageBitmap(fleaBean.bmpTitle);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //ui??? ?????? ????????? ??????
            txtBuyTitle.setText(fleaBean.title);
            txtBuySubTitle.setText(fleaBean.subtitle);
            txtBuyPrice.setText(fleaBean.saleprice);
            txtBuyDate.setText(fleaBean.date);
            StringTokenizer tokens = new StringTokenizer(fleaBean.userId);
            String userId = tokens.nextToken("@");
            txtBuyId.setText(userId);

            //????????? ?????? ????????? ????????? ????????????
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, BuyDetailActivity.class);
                    //intent.putExtra("INDEX", i); //?????????????????? ??????
                    intent.putExtra("BUYITEM", fleaBean); //??????????????? ?????? ?????????
                    intent.putExtra("TITLE", fleaBean.title);
                    mContext.startActivity(intent);
                }
            });

            return view;
        //}

        //return null;
    }

}
