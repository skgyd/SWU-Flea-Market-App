package com.example.cho1.guru2_final_project_1cho.fragment;

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
import com.example.cho1.guru2_final_project_1cho.activity.BuyWriteActivity;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.MyBuyAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.UserBuyAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentUserBuy extends Fragment {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    public ListView mLstUserBuy;
    private List<FleaBean> mBuyList = new ArrayList<>();
    private UserBuyAdapter mUserBuyAdapter;

    private MemberBean mLoginMember;
    private String mWriterID;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_buy, container, false);

        mLstUserBuy = view.findViewById(R.id.lstMyBuy);
        mLoginMember = FileDB.getLoginMember(getActivity());
        mWriterID = getActivity().getIntent().getStringExtra("ID") + "@gmail.com";

        //최초 데이터 세팅
        mUserBuyAdapter = new UserBuyAdapter(getActivity(), mBuyList);
        mLstUserBuy.setAdapter(mUserBuyAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //데이터 취득

        mFirebaseDB.getReference().child("buy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //data가 바뀔 때마다 이벤트가 들어옴
                //data를 받아와서 List에 저장
                mBuyList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){  //파이어베이스가 이중 구조여서
                    FleaBean bean = snapshot.getValue(FleaBean.class);
                    if(TextUtils.equals(mWriterID, bean.userId))
                        mBuyList.add(0, bean);
                }
                //바뀐 데이터로 Refresh 한다
                if(mUserBuyAdapter != null){
                    mUserBuyAdapter.setList(mBuyList);
                    mUserBuyAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //어댑터 생성
    }
}
