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
import com.example.cho1.guru2_final_project_1cho.activity.SellDetailActivity;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;

import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;

public class MySellAdapter extends BaseAdapter {

    private Context mContext;
    private List<FleaBean> mFleaList;
    private MemberBean mLoginMember;

    public MySellAdapter(Context context, List<FleaBean> fleaList) {
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_sell_item, null);

        mLoginMember = FileDB.getLoginMember(mContext);
        final FleaBean fleaBean = mFleaList.get(i);

        ImageView imgSell = view.findViewById(R.id.imgSell);
        GradientDrawable drawable=
                (GradientDrawable) mContext.getDrawable(R.drawable.background_rounding);
        imgSell.setBackground(drawable);
        imgSell.setClipToOutline(true);

        TextView txtSellTitle = view.findViewById(R.id.txtSellTitle);
        TextView txtSellSubTitle = view.findViewById(R.id.txtSellExplain);
        TextView txtSellPrice = view.findViewById(R.id.txtSellPrice);
        TextView txtSellId = view.findViewById(R.id.txtSellId);
        TextView txtSellDate = view.findViewById(R.id.txtSellDate);

        // imgTitle 이미지를 표시할 때는 원격 서버에 있는 이미지이므로, 비동기로 표시한다.
        try{
            if(fleaBean.bmpTitle == null) {
                new DownloadImgTaskFlea(mContext, imgSell, mFleaList, i).execute(new URL(fleaBean.imgUrl));
            } else {
                imgSell.setImageBitmap(fleaBean.bmpTitle);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        txtSellTitle.setText(fleaBean.selltitle);
        txtSellSubTitle.setText(fleaBean.wishoption);
        txtSellPrice.setText(fleaBean.wishprice);
        txtSellDate.setText(fleaBean.date);
        StringTokenizer tokens = new StringTokenizer(fleaBean.userId);
        String userId = tokens.nextToken("@") ;
        txtSellId.setText(userId);

        //리스트 항목 누르면 디테일 페이지로
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SellDetailActivity.class);
                //intent.putExtra("INDEX", i); //원본데이터의 순번
                intent.putExtra("SELLITEM", fleaBean); //상세표시할 원본 데이터
                mContext.startActivity(intent);
            }
        });

        return view;
    }
}
