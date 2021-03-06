package com.example.cho1.guru2_final_project_1cho.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.bean.CommentBean;
import com.example.cho1.guru2_final_project_1cho.bean.FreeBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.CommentAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.DownloadImgTaskFree;
import com.example.cho1.guru2_final_project_1cho.firebase.FreeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class FreeDetailActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    private TextView txtFreeDetailId, txtFreeDetailDate, txtFreeTitle, txtFreeDetailPlace, txtFreeDetailOption;
    private ImageView imgFreeDetail,imgStar;


    private Context mContext;

    private List<CommentBean> mCommentList = new ArrayList<>();
    private CommentAdapter mCommentAdapter;
    private ListView lstFreeComment;
    private Button btnFreeComment, btnFreeWriter;
    private ImageButton btnFreeModify, btnFreeDel;
    private EditText edtFreeComment;

    private MemberBean mLoginMember;
    private FreeBean mFreeBean;

    private List<String> mScrapList = new ArrayList<>();
    private List<FreeBean> mFreeList = new ArrayList<>();
    private FreeAdapter mFreeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_detail);

        mLoginMember = FileDB.getLoginMember(this);
        mFreeBean = (FreeBean) getIntent().getSerializableExtra("FREEITEM");
        CommentAdapter.setFreeBean(mFreeBean);

        lstFreeComment = findViewById(R.id.lstFreeComment);
        View header = getLayoutInflater().inflate(R.layout.activity_free_detail_header, null, false);
        lstFreeComment.addHeaderView(header);

        imgStar = header.findViewById(R.id.imgFreeStar); //???????????????

        header.findViewById(R.id.btnFreeModify).setOnClickListener(BtnClick);
        txtFreeDetailId = header.findViewById(R.id.txtFreeDetailId); //?????????
        txtFreeDetailDate = header.findViewById(R.id.txtFreeDetailDate); //??????
        imgFreeDetail = header.findViewById(R.id.imgFreeDetail);
        GradientDrawable drawable = (GradientDrawable) this.getDrawable(R.drawable.background_rounding);
        imgFreeDetail.setBackground(drawable);
        imgFreeDetail.setClipToOutline(true);

        txtFreeTitle = header.findViewById(R.id.txtFreeTitle);
        txtFreeDetailPlace = header.findViewById(R.id.txtFreeDetailPlace); //????????????
        txtFreeDetailOption = header.findViewById(R.id.txtFreeDetailOption); //???????????????,,

        btnFreeComment = findViewById(R.id.btnFreeComment);
        edtFreeComment = findViewById(R.id.edtFreeComment);
        btnFreeWriter = header.findViewById(R.id.btnFreeWriter);
        btnFreeModify = header.findViewById(R.id.btnFreeModify);
        btnFreeDel = header.findViewById(R.id.btnFreeDel);

        mCommentAdapter = new CommentAdapter(this, mCommentList);
        lstFreeComment.setAdapter(mCommentAdapter);

        //??????, ?????? ????????? ??????????????? ????????????
        header.findViewById(R.id.btnFreeModify).setOnClickListener(BtnClick);
        header.findViewById(R.id.btnFreeDel).setOnClickListener(BtnClick);
        header.findViewById(R.id.btnFreeWriter).setOnClickListener(BtnClick);
        header.findViewById(R.id.imgFreeStar).setOnClickListener(BtnClick);

        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = JoinActivity.getUserIdFromUUID(userEmail);
        mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("free").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mScrapList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String str = snapshot.getValue(String.class);
                    if(!TextUtils.isEmpty(str)) {
                        mScrapList.add(0, str);
                    }
                }

                Boolean exist = false;
                for(int i=0; i<mScrapList.size(); i++) {
                    if(TextUtils.equals(mFreeBean.id, mScrapList.get(i))) {
                        exist = true;
                    }
                }
                if(exist) {
                    imgStar.setImageResource(R.drawable.full_star);
                } else {
                    imgStar.setImageResource(R.drawable.empty_star);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //?????? ????????? ??? ????????? ?????????, ?????? ?????? ??????
        mFirebaseDB.getReference().child("free").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //???????????? ???????????? List??? ??????.
                mFreeList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                    FreeBean bean = snapshot.getValue(FreeBean.class);

                    if (TextUtils.equals(bean.id, mFreeBean.id)) {
                        if (mFreeBean != null) {
                            // imgTitle ???????????? ????????? ?????? ?????? ????????? ?????? ?????????????????? ???????????? ????????????.
                            try {
                                if (bean.bmpTitle == null) {
                                    new DownloadImgTaskFree(mContext, imgFreeDetail, mFreeList, 0).execute(new URL(bean.imgUrl));
                                } else {
                                    imgFreeDetail.setImageBitmap(bean.bmpTitle);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            txtFreeTitle.setText(bean.title);
                            txtFreeDetailOption.setText(bean.explain);

                            StringTokenizer tokens = new StringTokenizer(bean.userId);
                            String userId = tokens.nextToken("@");
                            txtFreeDetailId.setText(userId);
                            txtFreeDetailDate.setText(bean.date);
                            txtFreeDetailPlace.setText(bean.place);
                        }

                        LinearLayout layoutExVisibility = findViewById(R.id.layoutExVisibility);
                        //?????? ?????????(????????? ?????????)??? ????????? ???????????? ????????? ??????, ???????????? visibility ??????
                        if (TextUtils.equals(mFreeBean.userId, mFirebaseAuth.getCurrentUser().getEmail())) {
                            btnFreeModify.setVisibility(View.VISIBLE);
                            btnFreeDel.setVisibility(View.VISIBLE);
                        }
                        //?????? ?????????(????????? ?????????)??? ????????? ???????????? ????????? ????????? ????????? ?????? ?????? visibility ??????
                        if (!TextUtils.equals(mFreeBean.userId, mFirebaseAuth.getCurrentUser().getEmail())) {
                            btnFreeWriter.setVisibility(View.VISIBLE);
                            imgStar.setVisibility(View.VISIBLE);

                        }
                        // }
                    }
                }

                if (mFreeAdapter != null) {
                    mFreeAdapter.setList(mFreeList);
                    mFreeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        btnFreeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(edtFreeComment.getText().toString())) {
                    DatabaseReference dbRef = mFirebaseDB.getReference();
                    String id = dbRef.push().getKey(); // key ??? ????????? ?????? ID ??? ????????????.

                    CommentBean commentBean = new CommentBean();
                    commentBean.comment = edtFreeComment.getText().toString();
                    commentBean.date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                    commentBean.userId = mFirebaseAuth.getCurrentUser().getEmail();
                    commentBean.id = id;
                    commentBean.flag = 4;

                    //??????????????? ????????????
                    String uuid = JoinActivity.getUserIdFromUUID(mFirebaseAuth.getCurrentUser().getEmail());
                    dbRef.child("free").child(mFreeBean.id).child("comments").child(id).setValue(commentBean);
                    Toast.makeText(FreeDetailActivity.this, "????????? ?????? ???????????????", Toast.LENGTH_LONG).show();
                    edtFreeComment.setText(null);
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    lstFreeComment.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                    lstFreeComment.setSelection(mCommentAdapter.getCount() - 1);
                    lstFreeComment.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);

                    dbRef.child("free").child(mFreeBean.id).child("comments").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //???????????? ???????????? List??? ??????.
                            mCommentList.clear();
                       /* mExBean.commentList.clear();
                        mLoginMember.commentList.clear();*/

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                CommentBean bean = snapshot.getValue(CommentBean.class);
                                mCommentList.add(bean);
                           /* mExBean.commentList.add(bean);
                            mLoginMember.commentList.add(bean);*/
                            }
                            //?????? ???????????? Refresh ??????.
                            if (mCommentAdapter != null) {
                                mCommentAdapter.setList(mCommentList);
                                mCommentAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } else {
                    Toast.makeText(FreeDetailActivity.this, "????????? ???????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    View.OnClickListener BtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnFreeModify:
                    modify();
                    break;
                case R.id.btnFreeDel:
                    delete();
                    break;
                case R.id.btnFreeWriter:
                    writerPage();
                    break;
                case R.id.imgFreeStar:
                    scrap();
                    break;
            }
        }
    };

    //??????
    private void modify() {
        //??????
        Intent intent = new Intent(FreeDetailActivity.this, FreeModifyActivity.class);
        intent.putExtra("FREEITEM", mFreeBean);
        startActivity(intent);
    }

    //??????
    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????");
        builder.setMessage("?????????????????????????");
        builder.setNegativeButton("?????????", null);
        builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //DB?????? ????????????
                FirebaseDatabase.getInstance().getReference().child("free").child(mFreeBean.id).removeValue();
                //Storage ????????????
                if (mFreeBean.imgName != null) {
                    try {
                        FirebaseStorage.getInstance("gs://guru2-final-project-1cho.appspot.com/").getReference().child("images").child(mFreeBean.imgName).delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getApplicationContext(), "?????? ???????????????.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.create().show();
    }

    //????????? ?????????
    private void writerPage(){
        //??????
        Intent intent = new Intent(FreeDetailActivity.this, UserBoardActivity.class);
        intent.putExtra("ID", txtFreeDetailId.getText().toString());
        startActivity(intent);
    }

    private void scrap() {
        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = JoinActivity.getUserIdFromUUID(userEmail);

        mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("free").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mScrapList.clear();
                Boolean flag= false;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String str = snapshot.getValue(String.class);
                    if (TextUtils.equals(mFreeBean.id, str)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FreeDetailActivity.this);
                    builder.setTitle("?????????");
                    builder.setMessage("????????????????????????????");
                    builder.setNegativeButton("?????????", null);
                    builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            String uuid = JoinActivity.getUserIdFromUUID(userEmail);
                            mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("free").child(mFreeBean.id).setValue(mFreeBean.id);
                            Toast.makeText(FreeDetailActivity.this, "????????? ???????????????.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FreeDetailActivity.this);
                    builder.setTitle("?????????");
                    builder.setMessage("????????? ?????????????????????????");
                    builder.setNegativeButton("?????????", null);
                    builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            String uuid = JoinActivity.getUserIdFromUUID(userEmail);
                            mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("free").child(mFreeBean.id).removeValue();
                            Toast.makeText(FreeDetailActivity.this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //????????? ??????
        DatabaseReference dbRef = mFirebaseDB.getReference();
        dbRef.child("free").child(mFreeBean.id).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //???????????? ???????????? List??? ??????.
                mCommentList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CommentBean bean = snapshot.getValue(CommentBean.class);
                    mCommentList.add(bean);
                }
                //?????? ???????????? Refresh ??????.
                if (mCommentAdapter != null) {
                    mCommentAdapter.setList(mCommentList);
                    mCommentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}