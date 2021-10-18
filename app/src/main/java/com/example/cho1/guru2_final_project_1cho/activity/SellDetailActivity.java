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
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.CommentAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.DownloadImgTaskFlea;
import com.example.cho1.guru2_final_project_1cho.firebase.SellAdapter;
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


public class SellDetailActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    private TextView txtSellDetailId, txtSellDetailDate, txtSellTitle, txtSellDetailPrice, txtSellDetailOption;
    private ImageView imgSellDetail, imgStar;
    //private LinearLayout layoutSellVisibility;

    private FleaBean mFleaBean;
    private List<FleaBean> mFleaList = new ArrayList<>();
    private SellAdapter mSellAdapter;

    private Context mContext;

    private List<CommentBean> mCommentList = new ArrayList<>();
    private CommentAdapter mCommentAdapter;
    private ListView lstSellComment;
    private Button btnSellComment, btnSellWriter;
    private ImageButton btnSellModify, btnSellDel;
    private EditText edtSellComment;

    private MemberBean mLoginMember;

    private List<String> mScrapList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_detail);

        mFleaBean = (FleaBean) getIntent().getSerializableExtra("SELLITEM");
        mLoginMember = FileDB.getLoginMember(this);
        CommentAdapter.setFleaBean(mFleaBean);

        lstSellComment = findViewById(R.id.lstSellComment);
        // Header, Footer 생성 및 등록
        View header = getLayoutInflater().inflate(R.layout.activity_sell_detail_header, null, false);
        //View footer = getLayoutInflater().inflate(R.layout.activity_sell_detail_footer, null, false);

        lstSellComment.addHeaderView(header);
        // lstSellComment.addFooterView(footer);

        imgStar = header.findViewById(R.id.imgSellStar); //스크랩버튼

        txtSellDetailId = header.findViewById(R.id.txtSellDetailId); //아이디
        txtSellDetailDate = header.findViewById(R.id.txtSellDetailDate); //날짜
        imgSellDetail = header.findViewById(R.id.imgSellDetail);
        GradientDrawable drawable = (GradientDrawable) this.getDrawable(R.drawable.background_rounding);
        imgSellDetail.setBackground(drawable);
        imgSellDetail.setClipToOutline(true);

        txtSellTitle = header.findViewById(R.id.txtSellTitle);
        txtSellDetailPrice = header.findViewById(R.id.txtSellDetailPrice); //희망가
        txtSellDetailOption = header.findViewById(R.id.txtSellDetailOption); //희망 옵션

        //layoutSellVisibility = footer.findViewById(R.id.layoutSellVisibility); //수정, 삭제 버튼 감싼 레이아웃
        btnSellComment = findViewById(R.id.btnSellComment);
        edtSellComment = findViewById(R.id.edtSellComment);
        btnSellWriter = findViewById(R.id.btnSellWriter);
        btnSellModify = findViewById(R.id.btnSellModify);
        btnSellDel = findViewById(R.id.btnSellDel);

        mCommentAdapter = new CommentAdapter(this, mCommentList);
        lstSellComment.setAdapter(mCommentAdapter);

        //수정, 삭제 버튼에 클릭리스너 달아주기
        header.findViewById(R.id.btnSellModify).setOnClickListener(BtnClick);
        header.findViewById(R.id.btnSellDel).setOnClickListener(BtnClick);
        header.findViewById(R.id.btnSellWriter).setOnClickListener(BtnClick);
        header.findViewById(R.id.imgSellStar).setOnClickListener(BtnClick);
        //footer.findViewById(R.id.btnSellModify).setOnClickListener(BtnClick);
        // footer.findViewById(R.id.btnSellDel).setOnClickListener(BtnClick);

        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = JoinActivity.getUserIdFromUUID(userEmail);
        mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("sell").addValueEventListener(new ValueEventListener() {
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


        //상단 아이디 바 글쓴이 아이디, 올린 날짜 출력
        mFirebaseDB.getReference().child("sell").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //데이터를 받아와서 List에 저장.
                mFleaList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FleaBean bean = snapshot.getValue(FleaBean.class);
                    if (bean != null) {
                        if (TextUtils.equals(bean.id, mFleaBean.id)) {
                            // imgTitle 이미지를 표시할 때는 원격 서버에 있는 이미지이므로, 비동기로 표시한다.
                            try {
                                if (bean.bmpTitle == null) {
                                    new DownloadImgTaskFlea(mContext, imgSellDetail, mFleaList, 0).execute(new URL(bean.imgUrl));
                                } else {
                                    imgSellDetail.setImageBitmap(bean.bmpTitle);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            txtSellTitle.setText(bean.selltitle);
                            txtSellDetailOption.setText(bean.wishoption);
                            txtSellDetailPrice.setText(bean.wishprice);
                            StringTokenizer tokens = new StringTokenizer(bean.userId);
                            String userId = tokens.nextToken("@");
                            txtSellDetailId.setText(userId);
                            txtSellDetailDate.setText(bean.date);

                            LinearLayout layoutSellVisibility = findViewById(R.id.layoutSellVisibility); //수정, 삭제 버튼 감싼 레이아웃

                            //상단 아이디(글쓴이 아이디)와 로그인 아이디가 같으면 수정, 삭제버튼 visibility 풀기
                            if (TextUtils.equals(mFleaBean.userId, mFirebaseAuth.getCurrentUser().getEmail())) {
                                btnSellModify.setVisibility(View.VISIBLE);
                                btnSellDel.setVisibility(View.VISIBLE);
                            }
                            //상단 아이디(글쓴이 아이디)와 로그인 아이디가 다르면 작성자 페이지 가는 버튼 visibility 풀기
                            if(!TextUtils.equals(mFleaBean.userId, mFirebaseAuth.getCurrentUser().getEmail())){
                                btnSellWriter.setVisibility(View.VISIBLE);
                                imgStar.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                if (mSellAdapter != null) {
                    mSellAdapter.setList(mFleaList);
                    mSellAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        btnSellComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(edtSellComment.getText().toString())) {
                    DatabaseReference dbRef = mFirebaseDB.getReference();
                    String id = dbRef.push().getKey(); // key 를 메모의 고유 ID 로 사용한다.

                    CommentBean commentBean = new CommentBean();
                    commentBean.comment = edtSellComment.getText().toString();
                    commentBean.date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                    commentBean.userId = mFirebaseAuth.getCurrentUser().getEmail();
                    commentBean.id = id;
                    commentBean.flag = 2;

                    //고유번호를 생성한다
                    dbRef.child("sell").child(mFleaBean.id).child("comments").child(id).setValue(commentBean);
                    Toast.makeText(SellDetailActivity.this, "댓글이 등록 되었습니다", Toast.LENGTH_LONG).show();
                    edtSellComment.setText(null);
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    lstSellComment.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                    lstSellComment.setSelection(mCommentAdapter.getCount() - 1);
                    lstSellComment.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);

                    dbRef.child("sell").child(mFleaBean.id).child("comments").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //데이터를 받아와서 List에 저장.
                            mCommentList.clear();
                        /*mFleaBean.commentList.clear();
                        mLoginMember.commentList.clear();*/

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                CommentBean bean = snapshot.getValue(CommentBean.class);
                                mCommentList.add(bean);
                            /*mFleaBean.commentList.add(bean);
                            mLoginMember.commentList.add(bean);*/
                            }
                            //바뀐 데이터로 Refresh 한다.
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
                    Toast.makeText(SellDetailActivity.this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //유저 아이디 클릭 시 해당 유저의 글 목록 보여줌
        findViewById(R.id.txtSellDetailId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellDetailActivity.this, UserBoardActivity.class);
                intent.putExtra("ID", txtSellDetailId.getText().toString());
                startActivity(intent);
            }
        });

    }

    View.OnClickListener BtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnSellModify:
                    modify();
                    break;
                case R.id.btnSellDel:
                    delete();
                    break;
                case R.id.btnSellWriter:
                    writerPage();
                    break;
                case R.id.imgSellStar:
                    scrap();
                    break;
            }
        }
    };

    private void scrap() {
        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = JoinActivity.getUserIdFromUUID(userEmail);

        mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("sell").addListenerForSingleValueEvent(new ValueEventListener() {
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(SellDetailActivity.this);
                    builder.setTitle("스크랩");
                    builder.setMessage("스크랩하시겠습니까?");
                    builder.setNegativeButton("아니오", null);
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            String uuid = JoinActivity.getUserIdFromUUID(userEmail);
                            mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("sell").child(mFleaBean.id).setValue(mFleaBean.id);
                            Toast.makeText(SellDetailActivity.this, "스크랩 되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SellDetailActivity.this);
                    builder.setTitle("스크랩");
                    builder.setMessage("스크랩 취소하시겠습니까?");
                    builder.setNegativeButton("아니오", null);
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            String uuid = JoinActivity.getUserIdFromUUID(userEmail);
                            mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("sell").child(mFleaBean.id).removeValue();
                            Toast.makeText(SellDetailActivity.this, "스크랩 취소되었습니다.", Toast.LENGTH_SHORT).show();
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

    //수정
    private void modify() {
        //처리
        Intent intent = new Intent(SellDetailActivity.this, SellModifyActivity.class);
        intent.putExtra("SELLITEM", mFleaBean);
        startActivity(intent);
    }

    //삭제
    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("삭제");
        builder.setMessage("삭제하시겠습니까?");
        builder.setNegativeButton("아니오", null);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                String uuid = SellWriteActivity.getUserIdFromUUID(email);

                //DB에서 삭제처리
                FirebaseDatabase.getInstance().getReference().child("sell").child(mFleaBean.id).removeValue();
                //Storage 삭제처리
                if (mFleaBean.imgName != null) {
                    try {
                        FirebaseStorage.getInstance().getReference().child("images").child(mFleaBean.imgName).delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getApplicationContext(), "삭제 되었습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.create().show();
    }

    //작성자 페이지
    private void writerPage(){
        //처리
        Intent intent = new Intent(SellDetailActivity.this, UserBoardActivity.class);
        intent.putExtra("ID", txtSellDetailId.getText().toString());
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //데이터 취득
        DatabaseReference dbRef = mFirebaseDB.getReference();
        dbRef.child("sell").child(mFleaBean.id).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //데이터를 받아와서 List에 저장.
                mCommentList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CommentBean bean = snapshot.getValue(CommentBean.class);
                    mCommentList.add(bean);
                }
                //바뀐 데이터로 Refresh 한다.
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

    /* 3. 댓글 구현 (db를 더 만들어야 하는가??, 뿌린다면 리스트로?)
     * */

}
