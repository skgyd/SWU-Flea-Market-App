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
import com.example.cho1.guru2_final_project_1cho.activity.FreeDetailActivity;
import com.example.cho1.guru2_final_project_1cho.bean.FreeBean;

import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;

public class FreeAdapter extends BaseAdapter {

    private Context mContext;
    private List<FreeBean> mFreeList;

    public FreeAdapter(Context context, List<FreeBean> freeList) {
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_free_item, null);

        ImageView imgFree = view.findViewById(R.id.imgFree);
        GradientDrawable drawable=
                (GradientDrawable) mContext.getDrawable(R.drawable.background_rounding);
        imgFree.setBackground(drawable);
        imgFree.setClipToOutline(true);

        TextView txtFreeTitle = view.findViewById(R.id.txtFreeTitle);
        TextView txtFreeExplain = view.findViewById(R.id.txtFreeExplain);
        TextView txtFreeId = view.findViewById(R.id.txtFreeId);
        TextView txtFreeDate = view.findViewById(R.id.txtFreeDate);
        TextView txtFreePlace = view.findViewById(R.id.txtFreePlace);

        final FreeBean freeBean = mFreeList.get(i);

        // imgTitle 이미지를 표시할 때는 원격 서버에 있는 이미지이므로, 비동기로 표시한다.
        try{
            if(freeBean.bmpTitle == null) {
                new DownloadImgTaskFree(mContext, imgFree, mFreeList, i).execute(new URL(freeBean.imgUrl));
            } else {
                imgFree.setImageBitmap(freeBean.bmpTitle);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        txtFreeTitle.setText(freeBean.title);
        txtFreeExplain.setText(freeBean.explain);
        txtFreeDate.setText(freeBean.date);
        txtFreePlace.setText(freeBean.place);
        StringTokenizer tokens = new StringTokenizer(freeBean.userId);
        String userId = tokens.nextToken("@") ;
        txtFreeId.setText(userId);

        //리스트 항목 누르면 디테일 페이지로
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FreeDetailActivity.class);
                intent.putExtra("INDEX", i); //원본데이터의 순번
                intent.putExtra("FREEITEM", freeBean); //상세표시할 원본 데이터
                mContext.startActivity(intent);
            }
        });

        return view;
    }
}
