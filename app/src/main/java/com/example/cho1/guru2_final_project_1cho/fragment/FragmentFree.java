package com.example.cho1.guru2_final_project_1cho.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.activity.BuyWriteActivity;
import com.example.cho1.guru2_final_project_1cho.activity.ExWriteActivity;
import com.example.cho1.guru2_final_project_1cho.activity.FreeWriteActivity;
import com.example.cho1.guru2_final_project_1cho.bean.ExBean;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.FreeBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.BuyAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.ExAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.FreeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentFree extends Fragment {
    //layout fragment
    public ListView mLstFree;
    private List<FreeBean> mFreeList = new ArrayList<>();
    private FreeAdapter mFreeAdapter;
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_free_share, container, false);

        mLstFree = view.findViewById(R.id.lstFree);

        //최초 데이터 세팅
        mFreeAdapter = new FreeAdapter(getActivity(), mFreeList);
        mLstFree.setAdapter(mFreeAdapter);

        //글등록 버튼 눌러 페이지 이동
        ImageButton mbtnOk = view.findViewById(R.id.btnFreeWrite);

        mbtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), FreeWriteActivity.class);
                startActivity(i);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //데이터 취득
        mFirebaseDB.getReference().child("free").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //data가 바뀔 때마다 이벤트가 들어옴
                //data를 받아와서 List에 저장
                mFreeList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    FreeBean bean = snapshot.getValue(FreeBean.class);
                    mFreeList.add(0, bean);
                }
                //바뀐 데이터로 Refresh 한다
                if(mFreeAdapter != null){
                    mFreeAdapter.setList(mFreeList);
                    mFreeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //어댑터 생성
    }
}