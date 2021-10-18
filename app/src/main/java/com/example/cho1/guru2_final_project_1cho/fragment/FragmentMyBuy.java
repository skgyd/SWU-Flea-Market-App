package com.example.cho1.guru2_final_project_1cho.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.activity.BuyWriteActivity;
import com.example.cho1.guru2_final_project_1cho.activity.SellWriteActivity;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.MyBuyAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentMyBuy extends Fragment {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    public ListView mLstMyBuy;
    private List<FleaBean> mBuyList = new ArrayList<>();
    private MyBuyAdapter mBuyAdapter;

    private MemberBean mLoginMember;

    private boolean flag;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_buy, container, false);

        mLstMyBuy = view.findViewById(R.id.lstMyBuy);
        mLoginMember = FileDB.getLoginMember(getActivity());

        //최초 데이터 세팅
        mBuyAdapter = new MyBuyAdapter(getActivity(), mBuyList);
        mLstMyBuy.setAdapter(mBuyAdapter);

        view.findViewById(R.id.btnDelMyBuy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });

        return view;
    }

    //삭제
    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("삭제");
        builder.setMessage("삭제하시겠습니까?");
        builder.setNegativeButton("아니오", null);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                String uuid = SellWriteActivity.getUserIdFromUUID(email);

                flag = false;

                mFirebaseDB.getReference().child("buy").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //data가 바뀔 때마다 이벤트가 들어옴
                        //data를 받아와서 List에 저장
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){  //파이어베이스가 이중 구조여서
                            FleaBean bean = snapshot.getValue(FleaBean.class);
                            if(TextUtils.equals(mFirebaseAuth.getCurrentUser().getEmail(), bean.userId)) {
                                FirebaseDatabase.getInstance().getReference().child("buy").child(bean.id).removeValue();
                                flag = true;
                            }
                        }
                        if(flag) {
                            Toast.makeText(getActivity(), "삭제 되었습니다", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "삭제할 게시물이 없습니다", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();

        //데이터 취득
        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = BuyWriteActivity.getUserIdFromUUID(userEmail);
        mFirebaseDB.getReference().child("buy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //data가 바뀔 때마다 이벤트가 들어옴
                //data를 받아와서 List에 저장
                mBuyList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){  //파이어베이스가 이중 구조여서
                    FleaBean bean = snapshot.getValue(FleaBean.class);
                    if(TextUtils.equals(mFirebaseAuth.getCurrentUser().getEmail(), bean.userId))
                        mBuyList.add(0, bean);
                }
                //바뀐 데이터로 Refresh 한다
                if(mBuyAdapter != null){
                    mBuyAdapter.setList(mBuyList);
                    mBuyAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //어댑터 생성
    }
}
