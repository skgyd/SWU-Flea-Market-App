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
import com.example.cho1.guru2_final_project_1cho.bean.ExBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.ExAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentUserEx extends Fragment {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    public ListView mLstMyEx;
    private List<ExBean> mExList = new ArrayList<>();
    private ExAdapter mExAdapter;

    private MemberBean mLoginMember;
    private String mWriterID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_ex, container, false);

        mLstMyEx = view.findViewById(R.id.lstMyEx);
        mWriterID = getActivity().getIntent().getStringExtra("ID") + "@gmail.com";

        //최초 데이터 세팅
        mExAdapter = new ExAdapter(getActivity(), mExList);
        mLstMyEx.setAdapter(mExAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //데이터 취득
        String userEmail = mWriterID;
        String uuid = BuyWriteActivity.getUserIdFromUUID(userEmail);
        mFirebaseDB.getReference().child("ex").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //data가 바뀔 때마다 이벤트가 들어옴
                //data를 받아와서 List에 저장
                mExList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){  //파이어베이스가 이중 구조여서
                    ExBean bean = snapshot.getValue(ExBean.class);
                    if(TextUtils.equals(mWriterID, bean.userId))
                        mExList.add(0, bean);
                }
                //바뀐 데이터로 Refresh 한다
                if(mExAdapter != null){
                    mExAdapter.setList(mExList);
                    mExAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //어댑터 생성
    }
}
