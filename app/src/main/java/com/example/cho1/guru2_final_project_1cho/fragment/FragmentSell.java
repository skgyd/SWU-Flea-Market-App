package com.example.cho1.guru2_final_project_1cho.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.activity.SellWriteActivity;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.firebase.SellAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentSell extends Fragment {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    private ListView mLstSell;
    private List<FleaBean> mFleaList = new ArrayList<>();
    private SellAdapter mSellAdapter;

    private String mCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell, container, false);

        mCategory = getActivity().getIntent().getStringExtra("CATEGORY");


        mLstSell = view.findViewById(R.id.lstSell);

        mSellAdapter = new SellAdapter(getActivity(), mFleaList);
        mLstSell.setAdapter(mSellAdapter);

        view.findViewById(R.id.btnSellWrite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SellWriteActivity.class);
                intent.putExtra("CATEGORY", mCategory);
                startActivity(intent);
            }
        });
        return view;
    }//end onCreate()

    @Override
    public void onResume() {
        super.onResume();

        //데이터 취득
        mFirebaseDB.getReference().child("sell").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //data가 바뀔 때마다 이벤트가 들어옴
                //data를 받아와서 List에 저장
                mFleaList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    FleaBean bean = snapshot.getValue(FleaBean.class);
                    if(TextUtils.equals(bean.category, mCategory)) {
                        mFleaList.add(0, bean);
                    }
                }
                //바뀐 데이터로 Refresh 한다
                if(mSellAdapter != null){
                    mSellAdapter.setList(mFleaList);
                    mSellAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //어댑터 생성
    }
}
