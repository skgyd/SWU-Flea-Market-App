package com.example.cho1.guru2_final_project_1cho.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.firebase.DownloadImgTaskMember;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URL;

public class ModifyMemberActivity extends AppCompatActivity {

    private MemberBean loginMember;
    private EditText mEdtDetailId, mEdtDetailName, mEdtDetailPw, mEdtDetailPw1, mEdtDetailPw2;
    private Button mBtnModify, mBtnLogout, mBtnMyBoard, mBtnSecession, mBtnMyScrap;
    private ImageView mImgDetailProfile;

    private String id, name, imgUrl, uuid;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_member);

        loginMember = FileDB.getLoginMember(this);

        mEdtDetailId = findViewById(R.id.edtDetailId);
        mEdtDetailName = findViewById(R.id.edtDetailName);
        mBtnLogout = findViewById(R.id.btnLogout);
        mBtnMyBoard = findViewById(R.id.btnMyBoard);
        mBtnSecession = findViewById(R.id.btnSecession);
        mBtnMyScrap = findViewById(R.id.btnMyScrap);
        mImgDetailProfile = findViewById(R.id.imgDetailProfile);
        GradientDrawable drawable=
                (GradientDrawable) this.getDrawable(R.drawable.background_rounding);
        mImgDetailProfile.setBackground(drawable);
        mImgDetailProfile.setClipToOutline(true);

        id = mFirebaseAuth.getCurrentUser().getEmail();
        name = loginMember.memName;
        //pw = loginMember.memPw;
        imgUrl = loginMember.imgUrl;

        // imtTitle ???????????? ????????? ?????? ?????? ????????? ?????? ??????????????????, ???????????? ????????????.
        try{
            if(loginMember.bmpTitle == null) {
                new DownloadImgTaskMember(this, mImgDetailProfile, loginMember).execute(new URL(loginMember.imgUrl));
            } else {
                mImgDetailProfile.setImageBitmap(loginMember.bmpTitle);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        mEdtDetailId.setText(id);
        mEdtDetailId.setEnabled(false);
        mEdtDetailName.setText(name);
        mEdtDetailName.setEnabled(false);

        //???????????? ??????
        mBtnLogout.setOnClickListener(mClicks);
        //?????? ??? ??? ?????? ??????
        mBtnMyBoard.setOnClickListener(mClicks);
        //?????? ?????? ??????
        mBtnSecession.setOnClickListener(mClicks);
        //??? ????????? ??????
        mBtnMyScrap.setOnClickListener(mClicks);
    }

    //???????????? ??????
    private void logout() {
        try{
            //if(googleLoginFlag) {
                mFirebaseAuth.signOut();
            //}
            Toast.makeText(this, "???????????? ???????????????.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ModifyMemberActivity.this, LoginActivity.class));
            ActivityCompat.finishAffinity(ModifyMemberActivity.this);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

   /* //???????????? ?????? ??????
    private void modify() {
        String pw1 = mEdtDetailPw1.getText().toString();
        String pw2 = mEdtDetailPw2.getText().toString();
        String pw = mEdtDetailPw.getText().toString();

        if(!TextUtils.isEmpty(pw) || !TextUtils.isEmpty(pw1) || !TextUtils.isEmpty(pw2)) { // ???????????? ?????? ????????? ????????? ??????
            if (!TextUtils.equals(pw, loginMember.memPw)) {
                Toast.makeText(ModifyMemberActivity.this, "?????? ??????????????? ???????????? ????????????", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(pw1) || TextUtils.isEmpty(pw2)) {
                Toast.makeText(ModifyMemberActivity.this, "????????? ??????????????? ????????? ?????????", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.equals(pw1, pw2)) {
                Toast.makeText(ModifyMemberActivity.this, "????????? ??????????????? ???????????? ????????????", Toast.LENGTH_SHORT).show();
                return;
            }

            loginMember.memPw = mEdtDetailPw1.getText().toString();
            uuid = JoinActivity.getUserIdFromUUID(loginMember.memId);

            mFirebaseDatabase.getReference().child("member").child(uuid).setValue(loginMember);
            Toast.makeText(ModifyMemberActivity.this, "????????? ?????????????????????", Toast.LENGTH_SHORT).show();
            FileDB.setLoginMember(this, loginMember);
            finish();
        } else {
            Toast.makeText(ModifyMemberActivity.this, "????????? ????????? ????????????", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void secession() {
        AlertDialog.Builder builer = new AlertDialog.Builder(ModifyMemberActivity.this);
        builer.setTitle("?????? ??????");
        builer.setMessage("?????? ?????????????????????????");
        builer.setNegativeButton("?????????", null);
        builer.setPositiveButton("???", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MemberBean loginMember = FileDB.getLoginMember(ModifyMemberActivity.this);

                String guid = JoinActivity.getUserIdFromUUID(mFirebaseAuth.getCurrentUser().getEmail());
                FirebaseDatabase.getInstance().getReference().child("member").child(guid).removeValue();
                mFirebaseAuth.signOut();
                Toast.makeText(ModifyMemberActivity.this, "?????? ???????????????", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ModifyMemberActivity.this, LoginActivity.class));
                ActivityCompat.finishAffinity(ModifyMemberActivity.this);
            }
        });
        builer.create().show(); // ??????????????? ?????????
    }

    //?????? ?????? ?????????
    private View.OnClickListener mClicks = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnLogout:
                    logout();
                    break;

                /*case R.id.btnModify:
                    modify();
                    break;*/

                case R.id.btnMyBoard:
                    Intent i = new Intent(ModifyMemberActivity.this, MyBoardActivity.class);
                    startActivity(i);
                    break;

                case R.id.btnSecession:
                    secession();
                    break;

                case R.id.btnMyScrap:
                    Intent ii = new Intent(ModifyMemberActivity.this, MyScrapActivity.class);
                    startActivity(ii);
                    break;
            }
        }
    };

}
