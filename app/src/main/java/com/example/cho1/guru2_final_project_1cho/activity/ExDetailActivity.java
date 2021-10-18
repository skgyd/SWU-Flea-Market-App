package com.example.cho1.guru2_final_project_1cho.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
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
import com.example.cho1.guru2_final_project_1cho.bean.ExBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.CommentAdapter;
import com.example.cho1.guru2_final_project_1cho.firebase.DownloadImgTaskEx;
import com.example.cho1.guru2_final_project_1cho.firebase.ExAdapter;
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

public class ExDetailActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    private TextView txtExDetailId, txtExDetailDate, txtExDetailTitle, txtExDetailWant, txtExDetailPrice, txtExDetailState, txtExDetailFault, txtExDetailBuyDate, txtExDetailExpire, txtExDetailSize;
    private ListView lstExComment;
    private Button btnExComment, btnExWriter;
    private EditText edtExComment;
    private ImageButton btnExModify, btnExDel;
    private LinearLayout layoutExVisibility;
    private ImageView imgExDetail, imgStar;

    private Context mContext;
    private ExBean mExBean;
    private List<ExBean> mExList = new ArrayList<>();
    private ExAdapter mExAdapter;
    private MemberBean mLoginMember;

    private List<CommentBean> mCommentList = new ArrayList<>();
    private CommentAdapter mCommentAdapter;

    private List<String> mScrapList = new ArrayList<>();

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_detail);

        mLoginMember = FileDB.getLoginMember(this);
        mExBean = (ExBean) getIntent().getSerializableExtra("EXITEM");
        CommentAdapter.setExBean(mExBean);

        lstExComment = findViewById(R.id.lstExComment);

        // Header, Footer 생성 및 등록
        View header = getLayoutInflater().inflate(R.layout.activity_ex_detail_header, null, false);
        //View footer = getLayoutInflater().inflate(R.layout.activity_ex_detail_footer, null, false);

        lstExComment.addHeaderView(header);
        //lstExComment.addFooterView(footer);


        imgExDetail = header.findViewById(R.id.imgExDetail); // 내 물건 이미지
        GradientDrawable drawable = (GradientDrawable) this.getDrawable(R.drawable.background_rounding);
        imgExDetail.setBackground(drawable);
        imgExDetail.setClipToOutline(true);

        imgStar = findViewById(R.id.imgExStar); //스크랩버튼

        txtExDetailId = header.findViewById(R.id.txtExDetailId); //아이디
        txtExDetailDate = header.findViewById(R.id.txtExDetailDate); //날짜
        txtExDetailTitle = header.findViewById(R.id.txtExDetailTitle); // 내 물건
        txtExDetailWant = header.findViewById(R.id.txtExDetailWant); // 상대방 물건
        txtExDetailPrice = header.findViewById(R.id.txtExDetailPrice); // 원가
        txtExDetailState = header.findViewById(R.id.txtExDetailState); // 상태
        txtExDetailFault = header.findViewById(R.id.txtExDetailFault); // 하자
        txtExDetailBuyDate = header.findViewById(R.id.txtExDetailBuyDate); // 구매날짜
        txtExDetailExpire = header.findViewById(R.id.txtExDetailExpire); // 유통기한
        txtExDetailSize = header.findViewById(R.id.txtExDetailSize); // 사이즈

        btnExComment = findViewById(R.id.btnExComment);
        edtExComment = findViewById(R.id.edtExComment);
        btnExWriter = findViewById(R.id.btnExWriter);
        btnExModify = findViewById(R.id.btnExModify);
        btnExDel = findViewById(R.id.btnExDel);

        //layoutExVisibility = footer.findViewById(R.id.layoutExVisibility);

        mCommentAdapter = new CommentAdapter(this, mCommentList);
        lstExComment.setAdapter(mCommentAdapter);


        header.findViewById(R.id.btnExModify).setOnClickListener(mBtnClick);
        header.findViewById(R.id.btnExDel).setOnClickListener(mBtnClick);
        header.findViewById(R.id.btnExWriter).setOnClickListener(mBtnClick);
        header.findViewById(R.id.imgExStar).setOnClickListener(mBtnClick);

//        footer.findViewById(R.id.btnExModify).setOnClickListener(mBtnClick);
//        footer.findViewById(R.id.btnExDel).setOnClickListener(mBtnClick);
        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = JoinActivity.getUserIdFromUUID(userEmail);
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

                Boolean exist = false;
                for(int i=0; i<mScrapList.size(); i++) {
                    if(TextUtils.equals(mExBean.id, mScrapList.get(i))) {
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
        mFirebaseDB.getReference().child("ex").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //데이터를 받아와서 List에 저장.
                mExList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                    ExBean bean = snapshot.getValue(ExBean.class);

                    if (TextUtils.equals(bean.id, mExBean.id)) {
                        if (mExBean != null) {
                            // imgTitle 이미지를 표시할 때는 원격 서버에 있는 이미지이므로 비동기로 표시한다.
                            try {
                                if (bean.bmpTitle == null) {
                                    new DownloadImgTaskEx(mContext, imgExDetail, mExList, 0).execute(new URL(bean.imgUrl));
                                } else {

                                    imgExDetail.setImageBitmap(bean.bmpTitle);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            txtExDetailTitle.setText(bean.mine);
                            txtExDetailWant.setText(bean.want);
                            txtExDetailPrice.setText(bean.price);
                            txtExDetailState.setText(bean.state);
                            txtExDetailFault.setText(bean.fault);
                            txtExDetailBuyDate.setText(bean.buyDate);
                            txtExDetailExpire.setText(bean.expire);
                            txtExDetailSize.setText(bean.size);

                            StringTokenizer tokens = new StringTokenizer(bean.userId);
                            String userId = tokens.nextToken("@");
                            txtExDetailId.setText(userId);
                            txtExDetailDate.setText(bean.date);
                        }

                        LinearLayout layoutExVisibility = findViewById(R.id.layoutExVisibility);
                        //상단 아이디(글쓴이 아이디)와 로그인 아이디가 같으면 수정, 삭제버튼 visibility 풀기
                        if (TextUtils.equals(mExBean.userId, mFirebaseAuth.getCurrentUser().getEmail())) {
                            btnExModify.setVisibility(View.VISIBLE);
                            btnExDel.setVisibility(View.VISIBLE);
                        }
                        //상단 아이디(글쓴이 아이디)와 로그인 아이디가 다르면 작성자 페이지 가는 버튼 visibility 풀기
                        if (!TextUtils.equals(mExBean.userId, mFirebaseAuth.getCurrentUser().getEmail())) {
                            btnExWriter.setVisibility(View.VISIBLE);
                            imgStar.setVisibility(View.VISIBLE);

                        }
                        // }
                    }
                }

                if (mExAdapter != null) {
                    mExAdapter.setList(mExList);
                    mExAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        btnExComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(edtExComment.getText().toString())) {
                    DatabaseReference dbRef = mFirebaseDB.getReference();
                    String id = dbRef.push().getKey(); // key 를 메모의 고유 ID 로 사용한다.

                    CommentBean commentBean = new CommentBean();
                    commentBean.comment = edtExComment.getText().toString();
                    commentBean.date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                    commentBean.userId = mFirebaseAuth.getCurrentUser().getEmail();
                    commentBean.id = id;
                    commentBean.flag = 3;

                    //고유번호를 생성한다
                    String uuid = JoinActivity.getUserIdFromUUID(mFirebaseAuth.getCurrentUser().getEmail());
                    dbRef.child("ex").child(mExBean.id).child("comments").child(id).setValue(commentBean);
                    Toast.makeText(ExDetailActivity.this, "댓글이 등록 되었습니다", Toast.LENGTH_LONG).show();
                    edtExComment.setText(null);
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    lstExComment.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                    lstExComment.setSelection(mCommentAdapter.getCount() - 1);
                    lstExComment.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);

                    dbRef.child("ex").child(mExBean.id).child("comments").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //데이터를 받아와서 List에 저장.
                            mCommentList.clear();
                       /* mExBean.commentList.clear();
                        mLoginMember.commentList.clear();*/

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                CommentBean bean = snapshot.getValue(CommentBean.class);
                                mCommentList.add(bean);
                           /* mExBean.commentList.add(bean);
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
                    Toast.makeText(ExDetailActivity.this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //유저 아이디 클릭 시 해당 유저의 글 목록 보여줌
        findViewById(R.id.txtExDetailId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExDetailActivity.this, UserBoardActivity.class);
                intent.putExtra("ID", txtExDetailId.getText().toString());
                startActivity(intent);
            }
        });

    } // onCreate()

    public void onResume() {
        super.onResume();
        mExAdapter = new ExAdapter(this, FileDB.getExList(getApplicationContext()));

        DatabaseReference dbRef = mFirebaseDB.getReference();
        dbRef.child("ex").child(mExBean.id).child("comments").addValueEventListener(new ValueEventListener() {
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

    private View.OnClickListener mBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //어떤 버튼이 클릭 됐는지 구분한다
            switch (view.getId()) {
                case R.id.btnExModify: // 수정
                    modEx();
                    break;

                case R.id.btnExDel: // 삭제
                    //처리
                    delEx();
                    break;

                case R.id.btnExWriter:
                    writerPage();
                    break;

                case R.id.imgExStar:
                    scrap();
                    break;

            }
        }
    };

    private void scrap() {
        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = JoinActivity.getUserIdFromUUID(userEmail);

        mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("ex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mScrapList.clear();
                Boolean flag= false;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String str = snapshot.getValue(String.class);
                    if (TextUtils.equals(mExBean.id, str)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExDetailActivity.this);
                    builder.setTitle("스크랩");
                    builder.setMessage("스크랩하시겠습니까?");
                    builder.setNegativeButton("아니오", null);
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            String uuid = JoinActivity.getUserIdFromUUID(userEmail);
                            mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("ex").child(mExBean.id).setValue(mExBean.id);
                            Toast.makeText(ExDetailActivity.this, "스크랩 되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExDetailActivity.this);
                    builder.setTitle("스크랩");
                    builder.setMessage("스크랩 취소하시겠습니까?");
                    builder.setNegativeButton("아니오", null);
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            String uuid = JoinActivity.getUserIdFromUUID(userEmail);
                            mFirebaseDB.getReference().child("member").child(uuid).child("scrap").child("ex").child(mExBean.id).removeValue();
                            Toast.makeText(ExDetailActivity.this, "스크랩 취소되었습니다.", Toast.LENGTH_SHORT).show();
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

    private void modEx() { // 게시글 수정
        //처리
        Intent intent = new Intent(getApplicationContext(), ExModifyActivity.class);
        intent.putExtra("EXITEM", mExBean);
        startActivity(intent);
    }

    private void delEx() { // 게시글 삭제
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("삭제");
        builder.setMessage("삭제하시겠습니까?");
        builder.setNegativeButton("아니오", null);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                String uuid = ExWriteActivity.getUserIdFromUUID(email);

                //DB에서 삭제처리
                FirebaseDatabase.getInstance().getReference().child("ex").child(mExBean.id).removeValue();
                //Storage 삭제처리
                if (mExBean.imgName != null) {
                    try {
                        FirebaseStorage.getInstance("gs://guru2-final-project-1cho.appspot.com/").getReference().child("images").child(mExBean.imgName).delete();
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
        Intent intent = new Intent(ExDetailActivity.this, UserBoardActivity.class);
        intent.putExtra("ID", txtExDetailId.getText().toString());
        startActivity(intent);
    }
}
