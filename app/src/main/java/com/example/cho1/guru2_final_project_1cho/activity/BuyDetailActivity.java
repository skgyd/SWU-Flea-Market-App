package com.example.cho1.guru2_final_project_1cho.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.BuyAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.CommentAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.DownloadImgTaskFlea;
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

public class BuyDetailActivity extends AppCompatActivity {
    private TextView txtBuyDetailDate;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    private Uri mCaptureUri;
    private Context mContext;

    private String mCategory;
    public String mPhotoPath;

    private FleaBean mFleaBean;
    private ImageView imgBuyDetail, imgStar;
    private TextView txtBuyDetailId, txtBuyDetailProduct, txtBuyDetailPrice, txtBuyDetailFinalPrice,
            txtBuyDetailState, txtBuyDetailFault, txtBuyDetailBuyDate, txtBuyDetailExpire, txtBuyDetailSize, txtBuyDetailExplain;
    private ListView lstBuyComment;
    private Button btnBuyComment, btnBuyWriter;
    private ImageButton btnBuyModify, btnBuyDel;
    private EditText edtBuyComment;

    private List<FleaBean> mFleaList = new ArrayList<>();
    private BuyAdapter mBuyAdapter;
    private MemberBean mLoginMember;

    private List<CommentBean> mCommentList = new ArrayList<>();
    private CommentAdapter mCommentAdapter;

    private List<String> mScrapList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_detail);

        mFleaBean = (FleaBean) getIntent().getSerializableExtra("BUYITEM");
        CommentAdapter.setFleaBean(mFleaBean);
        mLoginMember = FileDB.getLoginMember(this);

        lstBuyComment = findViewById(R.id.lstBuyComment);

        // Header, Footer ?????? ??? ??????
        View header = getLayoutInflater().inflate(R.layout.activity_buy_detail_header, null, false);
        //iew footer = getLayoutInflater().inflate(R.layout.activity_buy_detail_footer, null, false);

        lstBuyComment.addHeaderView(header);
        //lstBuyComment.addFooterView(footer);

        btnBuyComment = findViewById(R.id.btnBuyComment);
        edtBuyComment = findViewById(R.id.edtBuyComment);
        btnBuyWriter = findViewById(R.id.btnBuyWriter);
        btnBuyModify = findViewById(R.id.btnBuyModify);
        btnBuyDel = findViewById(R.id.btnBuyDel);

        imgStar = header.findViewById(R.id.imgBuyStar); //???????????????

        //??????, ?????? ????????? ??????????????? ????????????
        header.findViewById(R.id.btnBuyModify).setOnClickListener(BtnClick);
        header.findViewById(R.id.btnBuyDel).setOnClickListener(BtnClick);
        header.findViewById(R.id.btnBuyWriter).setOnClickListener(BtnClick);
        header.findViewById(R.id.imgBuyStar).setOnClickListener(BtnClick);
//        footer.findViewById(R.id.btnBuyModify).setOnClickListener(BtnClick);
//        footer.findViewById(R.id.btnBuyDel).setOnClickListener(BtnClick);

        txtBuyDetailId = header.findViewById(R.id.txtBuyDetailId); //?????????
        txtBuyDetailDate = header.findViewById(R.id.txtBuyDetailDate); //??????
        imgBuyDetail = header.findViewById(R.id.imgBuyDetail); //?????????
        GradientDrawable drawable = (GradientDrawable) this.getDrawable(R.drawable.background_rounding);
        imgBuyDetail.setBackground(drawable);
        imgBuyDetail.setClipToOutline(true);

        txtBuyDetailProduct = header.findViewById(R.id.txtBuyDetailProduct); //?????????
        txtBuyDetailExplain = header.findViewById(R.id.txtBuyDetailExplain); //??????
        txtBuyDetailPrice = header.findViewById(R.id.txtBuyDetailPrice); //??????
        txtBuyDetailFinalPrice = header.findViewById(R.id.txtBuyDetailFinalPrice); //?????????
        txtBuyDetailState = header.findViewById(R.id.txtBuyDetailState); //????????????
        txtBuyDetailFault = header.findViewById(R.id.txtBuyDetailFault); //????????????
        txtBuyDetailBuyDate = header.findViewById(R.id.txtBuyDetailBuyDate); //?????????
        txtBuyDetailExpire = header.findViewById(R.id.txtBuyDetailExpire); //????????????
        txtBuyDetailSize = header.findViewById(R.id.txtBuyDetailSize); //???????????????

        mCommentAdapter = new CommentAdapter(this, mCommentList);
        lstBuyComment.setAdapter(mCommentAdapter);


        LinearLayout layoutBuyVisibility = findViewById(R.id.layoutBuyVisibility); //??????, ?????? ?????? ?????? ????????????

        //?????? ?????????(????????? ?????????)??? ????????? ???????????? ????????? ??????, ???????????? visibility ??????
        if (TextUtils.equals(mFleaBean.userId, mFirebaseAuth.getCurrentUser().getEmail())) {
            btnBuyModify.setVisibility(View.VISIBLE);
            btnBuyDel.setVisibility(View.VISIBLE);
        }
        //?????? ?????????(????????? ?????????)??? ????????? ???????????? ????????? ????????? ????????? ?????? ?????? visibility ??????
        if(!TextUtils.equals(mFleaBean.userId, mFirebaseAuth.getCurrentUser().getEmail())){
            btnBuyWriter.setVisibility(View.VISIBLE);
            imgStar.setVisibility(View.VISIBLE);
        }


        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = JoinActivity.getUserIdFromUUID(userEmail);
        mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("buy").addValueEventListener(new ValueEventListener() {
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
                    if(TextUtils.equals(mFleaBean.id, mScrapList.get(i))) {
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

        //?????? ????????????(?????????, ??????), ??? ?????? ????????? ??????
        mCommentAdapter = new CommentAdapter(this, mCommentList);
        lstBuyComment.setAdapter(mCommentAdapter);


        mFirebaseDB.getReference().child("buy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //???????????? ???????????? List??? ??????.
                mFleaList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FleaBean bean = snapshot.getValue(FleaBean.class);
                    if (TextUtils.equals(bean.id, mFleaBean.id)) {
                        if (mFleaBean != null) {
                            // imgTitle ???????????? ????????? ?????? ?????? ????????? ?????? ??????????????????, ???????????? ????????????.
                            try {
                                if (bean.bmpTitle == null) {
                                    new DownloadImgTaskFlea(mContext, imgBuyDetail, mFleaList, 0).execute(new URL(bean.imgUrl));
                                } else {
                                    imgBuyDetail.setImageBitmap(bean.bmpTitle);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            txtBuyDetailProduct.setText(bean.title);
                            txtBuyDetailExplain.setText(bean.subtitle);
                            txtBuyDetailPrice.setText(bean.price);
                            txtBuyDetailFinalPrice.setText(bean.saleprice);
                            txtBuyDetailState.setText(bean.state);
                            txtBuyDetailFault.setText(bean.fault);
                            txtBuyDetailBuyDate.setText(bean.buyday);
                            txtBuyDetailExpire.setText(bean.expire);
                            txtBuyDetailSize.setText(bean.size);

                            txtBuyDetailDate.setText(bean.date);

                            StringTokenizer tokens = new StringTokenizer(bean.userId);
                            String userId = tokens.nextToken("@");
                            txtBuyDetailId.setText(userId);
                        }
                    }

                }

                if (mBuyAdapter != null) {
                    mBuyAdapter.setList(mFleaList);
                    mBuyAdapter.notifyDataSetChanged();
                }

                mCategory = getIntent().getStringExtra("CATEGORY");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //?????? ????????? ?????? ??? ?????? ????????? ??? ?????? ?????????
        findViewById(R.id.txtBuyDetailId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuyDetailActivity.this, UserBoardActivity.class);
                intent.putExtra("ID", txtBuyDetailId.getText().toString());
                startActivity(intent);
            }
        });

        btnBuyComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(edtBuyComment.getText().toString())) {
                    DatabaseReference dbRef = mFirebaseDB.getReference();
                    String id = dbRef.push().getKey(); // key ??? ????????? ?????? ID ??? ????????????.

                    CommentBean commentBean = new CommentBean();
                    commentBean.comment = edtBuyComment.getText().toString();
                    commentBean.date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                    commentBean.userId = mFirebaseAuth.getCurrentUser().getEmail();
                    commentBean.id = id;
                    commentBean.flag = 1;

                    //??????????????? ????????????
                    dbRef.child("buy").child(mFleaBean.id).child("comments").child(id).setValue(commentBean);
                    Toast.makeText(BuyDetailActivity.this, "????????? ?????? ???????????????", Toast.LENGTH_LONG).show();
                    edtBuyComment.setText(null);
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    lstBuyComment.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                    lstBuyComment.setSelection(mCommentAdapter.getCount() - 1);
                    lstBuyComment.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);

                    dbRef.child("buy").child(mFleaBean.id).child("comments").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //???????????? ???????????? List??? ??????.
                            mCommentList.clear();
                        /*mFleaBean.commentList.clear();
                        mLoginMember.commentList.clear();*/

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
                } else {
                    Toast.makeText(BuyDetailActivity.this, "????????? ???????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }  //end onCreate()

    View.OnClickListener BtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnBuyModify:
                    modify();
                    break;
                case R.id.btnBuyDel:
                    delete();
                    break;
                case R.id.btnBuyWriter:
                    writerPage();
                    break;
                case R.id.imgBuyStar:
                    scrap();
                    break;
            }
        }
    };

    private void scrap() {
        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = JoinActivity.getUserIdFromUUID(userEmail);

        mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("buy").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mScrapList.clear();
                Boolean flag= false;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String str = snapshot.getValue(String.class);
                    if (TextUtils.equals(mFleaBean.id, str)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BuyDetailActivity.this);
                    builder.setTitle("?????????");
                    builder.setMessage("????????????????????????????");
                    builder.setNegativeButton("?????????", null);
                    builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            String uuid = JoinActivity.getUserIdFromUUID(userEmail);
                            mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("buy").child(mFleaBean.id).setValue(mFleaBean.id);
                            Toast.makeText(BuyDetailActivity.this, "????????? ???????????????.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BuyDetailActivity.this);
                    builder.setTitle("?????????");
                    builder.setMessage("????????? ?????????????????????????");
                    builder.setNegativeButton("?????????", null);
                    builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            String uuid = JoinActivity.getUserIdFromUUID(userEmail);
                            mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("buy").child(mFleaBean.id).removeValue();
                            Toast.makeText(BuyDetailActivity.this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
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

    //??????
    private void modify() {
        //??????
        Intent intent = new Intent(BuyDetailActivity.this, BuyModifyActivity.class);
        intent.putExtra("BUYITEM", mFleaBean);
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
                FirebaseDatabase.getInstance().getReference().child("buy").child(mFleaBean.id).removeValue();
                //Storage ????????????
                if (mFleaBean.imgName != null) {
                    try {
                        FirebaseStorage.getInstance("gs://guru2-final-project-1cho.appspot.com/").getReference().child("images").child(mFleaBean.imgName).delete();
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
        Intent intent = new Intent(BuyDetailActivity.this, UserBoardActivity.class);
        intent.putExtra("ID", txtBuyDetailId.getText().toString());
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //????????? ??????
        DatabaseReference dbRef = mFirebaseDB.getReference();
        dbRef.child("buy").child(mFleaBean.id).child("comments").addValueEventListener(new ValueEventListener() {
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
