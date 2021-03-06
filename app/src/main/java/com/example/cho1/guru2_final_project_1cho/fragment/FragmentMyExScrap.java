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
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.BuyAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.ExAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentMyExScrap extends Fragment {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    public ListView mLstMyExScrap;
    private List<ExBean> mExList = new ArrayList<>();
    private ExAdapter mExAdapter;

    private MemberBean mLoginMember;

    private Boolean flag;

    private String userEmail, uuid;

    private List<String> mScrapList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_ex_scrap, container, false);

        mLstMyExScrap = view.findViewById(R.id.lstMyExScrap);
        mLoginMember = FileDB.getLoginMember(getActivity());

        //?????? ????????? ??????
        mExAdapter = new ExAdapter(getActivity(), mExList);
        mLstMyExScrap.setAdapter(mExAdapter);


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        //????????? ??????
        userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        uuid = BuyWriteActivity.getUserIdFromUUID(userEmail);

        mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("ex").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mScrapList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String str = snapshot.getValue(String.class);
                    if(!TextUtils.isEmpty(str)) {
                        mScrapList.add(0, str);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFirebaseDB.getReference().child("ex").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //data??? ?????? ????????? ???????????? ?????????
                //data??? ???????????? List??? ??????
                mExList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){  //????????????????????? ?????? ????????????
                    ExBean bean = snapshot.getValue(ExBean.class);
                    for(int i=0; i<mScrapList.size(); i++) {
                        if (TextUtils.equals(mScrapList.get(i), bean.id))
                            mExList.add(0, bean);
                    }
                }
                //?????? ???????????? Refresh ??????
                if(mExAdapter != null){
                    mExAdapter.setList(mExList);
                    mExAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //????????? ??????
    }
}
