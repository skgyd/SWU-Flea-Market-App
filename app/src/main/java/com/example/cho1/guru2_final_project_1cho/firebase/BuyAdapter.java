package com.example.cho1.guru2_final_project_1cho.firebase;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.activity.BuyDetailActivity;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class BuyAdapter extends BaseAdapter {

    private Context mContext;
    private List<FleaBean> mFleaList;


//    /****/
//    private LayoutInflater inflate;
//    private RecyclerView.ViewHolder viewHolder;
//
//
//    ArrayList<BuyAdapter> buyAdapter = new ArrayList<>();
//    BuyAdapter mBuyAdapter;
//    ArrayList<BuyAdapter> arrayList = new ArrayList<BuyAdapter>();
//
//    public void filter(String charText){
//        charText = charText.toLowerCase(Locale.getDefault());
//        buyAdapter.clear();
//
//        Log.d("SIZE", "" + arrayList.size());
//
//        if(charText.length() == 0){
//            buyAdapter.addAll(arrayList);
//        }
//        else {
//            for(BuyAdapter potion : arrayList){
//                String name = potion.name;
//            }
//        }
//    }

    public BuyAdapter(Context context, List<FleaBean> fleaList) {
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
        view = inflater.inflate(R.layout.view_buy_item, null);

        ImageView imgBuy = view.findViewById(R.id.imgBuy);
        GradientDrawable drawable=
                (GradientDrawable) mContext.getDrawable(R.drawable.background_rounding);
        imgBuy.setBackground(drawable);
        imgBuy.setClipToOutline(true);

        TextView txtBuyTitle = view.findViewById(R.id.txtBuyTitle);
        TextView txtBuySubTitle = view.findViewById(R.id.txtBuyExplain);
        TextView txtBuyPrice = view.findViewById(R.id.txtBuyPrice);
        TextView txtBuyId = view.findViewById(R.id.txtBuyId);
        TextView txtBuyDate = view.findViewById(R.id.txtBuyDate);

        final FleaBean fleaBean = mFleaList.get(i);

        // imgTitle 이미지를 표시할 때는 원격 서버에 있는 이미지이므로, 비동기로 표시한다.
        try{
            if(fleaBean.bmpTitle == null) {
                new DownloadImgTaskFlea(mContext, imgBuy, mFleaList, i).execute(new URL(fleaBean.imgUrl));
            } else {
                imgBuy.setImageBitmap(fleaBean.bmpTitle);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        //ui에 원본 데이터 적용
        txtBuyTitle.setText(fleaBean.title);
        txtBuySubTitle.setText(fleaBean.subtitle);
        txtBuyPrice.setText(fleaBean.saleprice);
        txtBuyDate.setText(fleaBean.date);
        StringTokenizer tokens = new StringTokenizer(fleaBean.userId);
        String userId = tokens.nextToken("@");
        txtBuyId.setText(userId);

        //리스트 항목 누르면 디테일 페이지로
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, BuyDetailActivity.class);
                intent.putExtra("INDEX", i); //원본데이터의 순번
                intent.putExtra("BUYITEM", fleaBean); //상세표시할 원본 데이터
                intent.putExtra("TITLE", fleaBean.title);
                mContext.startActivity(intent);
            }
        });

        return view;
    }

}