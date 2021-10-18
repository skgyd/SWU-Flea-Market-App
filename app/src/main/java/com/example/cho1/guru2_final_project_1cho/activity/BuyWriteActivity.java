package com.example.cho1.guru2_final_project_1cho.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BuyWriteActivity extends AppCompatActivity {

    public static final String STORAGE_DB_URL = "gs://guru2-final-project-1cho.appspot.com/";

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance(STORAGE_DB_URL);
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private int itemNum = 0; //스피너 선택값 불러와 저장할 임시변수
    private String mCategory;

    private ImageView mimgBuyWrite;  //사진
    private EditText medtTitle;  //제목
    private EditText medtExplain;  //설명

    private EditText medtPrice;  //정가
    private EditText medtSalePrice;  //판매가
    private EditText medtBuyDay;  //구매일
    private EditText medtExprieDate;  //유통기한
    private EditText medtDefect;  //하자 유무
    private EditText medtSize;  //실제 측정 사이즈
    private Spinner mspinner1;  //카테고리
    private Spinner mspinner2;  //제품 상태

    private File tempFile;

    private FleaBean mFleaBean;

    //사진이 저장되는 경로
    private Uri mCaptureUri;
    //사진이 저장된 단말기상의 실제 경로
    public String mPhotoPath;
    //startActivityForResult() 에 넘겨주는 값, 이 값이 나중에 onActivityResult()로 돌아와서
    //내가 던진값인지를 구별할 때 사용하는 상수이다.
    public static final int REQUEST_IMAGE_CAPTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_write);


        //카메라를 사용하기 위한 퍼미션을 요청한다.
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);


        mimgBuyWrite = findViewById(R.id.imgBuyWrite);
        GradientDrawable drawable =
                (GradientDrawable) this.getDrawable(R.drawable.background_rounding);
        mimgBuyWrite.setBackground(drawable);
        mimgBuyWrite.setClipToOutline(true);

        Button mbtnImgReg = findViewById(R.id.btnBuyWriteImgReg);
        Button mbtnGalleryReg = findViewById(R.id.btnBuyWriteGalleryReg);

        //사진찍기
        mbtnImgReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mbtnGalleryReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });

        medtTitle = findViewById(R.id.edtBuyWriteTitle);
        medtExplain = findViewById(R.id.edtBuyWriteExplain);
        medtPrice = findViewById(R.id.edtBuyWritePrice);
        medtSalePrice = findViewById(R.id.edtBuyWriteSalePrice);
        medtBuyDay = findViewById(R.id.edtBuyWriteDay);
        medtExprieDate = findViewById(R.id.edtBuyWriteExprieDate);
        medtDefect = findViewById(R.id.edtBuyWriteDefect);
        medtSize = findViewById(R.id.edtBuyWriteSize);
        mspinner1 = findViewById(R.id.spinBuyWriteCategory);
        mspinner2 = findViewById(R.id.spinBuyWriteState);

        findViewById(R.id.btnBuyWriteOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //신규 등록
                upload();
            }
        });

        mFleaBean = (FleaBean) getIntent().getSerializableExtra(FleaBean.class.getName());
        if (mFleaBean != null) {
            getIntent().getParcelableArrayExtra("titleBitmap");
            if (mFleaBean.bmpTitle != null) {
                mimgBuyWrite.setImageBitmap(mFleaBean.bmpTitle);
            }
            medtTitle.setText(mFleaBean.title);
            medtExplain.setText(mFleaBean.subtitle);
            medtPrice.setText(mFleaBean.price);
            medtSalePrice.setText(mFleaBean.saleprice);
            medtBuyDay.setText(mFleaBean.buyday);
            medtExprieDate.setText(mFleaBean.expire);
            medtDefect.setText(mFleaBean.fault);
            medtSize.setText(mFleaBean.size);
        }

        //카테고리 드롭다운 스피너 추가
        Spinner dropdown = (Spinner) findViewById(R.id.spinBuyWriteCategory);
        String[] items = new String[]{"옷", "책", "생활용품", "기프티콘", "데이터", "대리예매", "전자기기", "화장품", "기타"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        //제품상태 드롭다운 스피너 추가
        Spinner dropdown2 = (Spinner) findViewById(R.id.spinBuyWriteState);
        String[] items2 = new String[]{"상", "중", "하"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items2);
        dropdown2.setAdapter(adapter2);

        mCategory = getIntent().getStringExtra("CATEGORY");

        //들어온 카테고리 항목이 기존 배열(items)의 몇 번째에 위치하고 있는지 알아냄
        for (int i = 0; i < items.length; i++) {
            if (TextUtils.equals(items[i], mCategory)) {
                itemNum = i;
                break;
            }
        }
        //카테고리 스피너 기본값 지정
        mspinner1.setSelection(itemNum);


    }  //end onCreate()


    // 새 게시글 작성
    private void upload() {

//        if (mPhotoPath == null) {
//            Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
//            return;
//        }


//        if(mFleaBean.category == "옷"){
//            if(medtPrice == null || medtTitle == null || medtSalePrice == null || medtBuyDay == null || medtSize == null){
//                Toast.makeText(this, "필수 항목을 채워주세요", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }


//        if (mCategory.equals("옷")) {
//            if (mPhotoPath == null) {
//                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (medtTitle.length() == 0 || medtPrice.length() == 0 || medtSalePrice.length() == 0 || medtBuyDay.length() == 0 || medtDefect.length() == 0 || medtSize.length() == 0 || medtExplain.length() == 0) {
//                medtTitle.requestFocus();
//                medtTitle.setCursorVisible(true);
////                medtPrice.requestFocus();
////                medtSalePrice.requestFocus();
////                medtBuyDay.requestFocus();
////                medtDefect.requestFocus();
////                medtSize.requestFocus();  // > 제일 마지막 항목 하나만 빨간색으로 뜨는 문제 ㅇㅇ
//                Toast.makeText(this, "필수 항목을 채워주세요", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }

        //필수 항목 받기
        if (mCategory.equals("옷")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtPrice.length() == 0) {
                medtPrice.requestFocus();
                Toast.makeText(this, "정가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtBuyDay.length() == 0) {
                medtBuyDay.requestFocus();
                Toast.makeText(this, "구매일을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtDefect.length() == 0) {
                medtDefect.requestFocus();
                Toast.makeText(this, "하자 유무를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSize.length() == 0) {
                medtSize.requestFocus();
                Toast.makeText(this, "측정 사이즈를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (mCategory.equals("책")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtPrice.length() == 0) {
                medtPrice.requestFocus();
                Toast.makeText(this, "정가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtDefect.length() == 0) {
                medtDefect.requestFocus();
                Toast.makeText(this, "하자 유무를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (mCategory.equals("생활용품")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtPrice.length() == 0) {
                medtPrice.requestFocus();
                Toast.makeText(this, "정가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtBuyDay.length() == 0) {
                medtBuyDay.requestFocus();
                Toast.makeText(this, "구매일을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExprieDate.length() == 0) {
                medtExprieDate.requestFocus();
                Toast.makeText(this, "유통기한을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtDefect.length() == 0) {
                medtDefect.requestFocus();
                Toast.makeText(this, "하자 유무를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        if (mCategory.equals("기프티콘")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtPrice.length() == 0) {
                medtPrice.requestFocus();
                Toast.makeText(this, "정가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExprieDate.length() == 0) {
                medtExprieDate.requestFocus();
                Toast.makeText(this, "유통기한을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        if (mCategory.equals("데이터")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (mCategory.equals("대리예매")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExprieDate.length() == 0) {
                medtExprieDate.requestFocus();
                Toast.makeText(this, "유통기한을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        if (mCategory.equals("전자기기")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtPrice.length() == 0) {
                medtPrice.requestFocus();
                Toast.makeText(this, "정가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtBuyDay.length() == 0) {
                medtBuyDay.requestFocus();
                Toast.makeText(this, "구매일을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtDefect.length() == 0) {
                medtDefect.requestFocus();
                Toast.makeText(this, "하자 유무를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSize.length() == 0) {
                medtSize.requestFocus();
                Toast.makeText(this, "측정 사이즈를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        if (mCategory.equals("화장품")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtPrice.length() == 0) {
                medtPrice.requestFocus();
                Toast.makeText(this, "정가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtBuyDay.length() == 0) {
                medtBuyDay.requestFocus();
                Toast.makeText(this, "구매일을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExprieDate.length() == 0) {
                medtExprieDate.requestFocus();
                Toast.makeText(this, "유통기한을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtDefect.length() == 0) {
                medtDefect.requestFocus();
                Toast.makeText(this, "하자 유무를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        if (mCategory.equals("기타")) {
            if (mPhotoPath == null) {
                Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtTitle.length() == 0) {
                medtTitle.requestFocus();
                Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtPrice.length() == 0) {
                medtPrice.requestFocus();
                Toast.makeText(this, "정가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSalePrice.length() == 0) {
                medtSalePrice.requestFocus();
                Toast.makeText(this, "판매가를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtBuyDay.length() == 0) {
                medtBuyDay.requestFocus();
                Toast.makeText(this, "구매일을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExprieDate.length() == 0) {
                medtExprieDate.requestFocus();
                Toast.makeText(this, "유통기한을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtDefect.length() == 0) {
                medtDefect.requestFocus();
                Toast.makeText(this, "하자 유무를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtSize.length() == 0) {
                medtSize.requestFocus();
                Toast.makeText(this, "측정 사이즈를 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medtExplain.length() == 0) {
                medtExplain.requestFocus();
                Toast.makeText(this, "부가적인 설명을 적어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        //사진부터 Storage 에 업로드한다.
        StorageReference storageRef = mFirebaseStorage.getReference();
        final StorageReference imagesRef = storageRef.child("images/" + mCaptureUri.getLastPathSegment()); //images/파일날짜.jpg

        UploadTask uploadTask = imagesRef.putFile(mCaptureUri);
        //파일 업로드 실패에 따른 콜백 처리를 한다.
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagesRef.getDownloadUrl();
            }
        }).

                addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        //database upload 를 호출한다.
                        uploadDB(task.getResult().toString(), mCaptureUri.getLastPathSegment());
                    }
                });
    }


    private void uploadDB(String imgUrl, String imgName) {
        //Firebase 데이터베이스에 메모를 등록한다.
        DatabaseReference dbRef = mFirebaseDatabase.getReference();
        String id = dbRef.push().getKey(); // key 를 게시글의 고유 ID 로 사용한다.

        //데이터베이스에 저장한다.
        FleaBean fleaBean = new FleaBean();
        fleaBean.id = id;
        fleaBean.userId = mFirebaseAuth.getCurrentUser().getEmail(); // email
        fleaBean.imgUrl = imgUrl;
        fleaBean.imgName = imgName;
        fleaBean.category = mspinner1.getSelectedItem().toString(); //카테고리
        fleaBean.title = medtTitle.getText().toString(); // 타이틀
        fleaBean.subtitle = medtExplain.getText().toString(); // 서브 타이틀(설명)
        fleaBean.price = medtPrice.getText().toString(); // 정가
        fleaBean.saleprice = medtSalePrice.getText().toString(); // 판매가
        fleaBean.state = mspinner2.getSelectedItem().toString(); // 물건 상태
        fleaBean.fault = medtDefect.getText().toString(); // 하자
        fleaBean.buyday = medtBuyDay.getText().toString(); // 구매일
        fleaBean.expire = medtExprieDate.getText().toString(); // 유통기한
        fleaBean.size = medtSize.getText().toString(); // 실측 사이즈

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        fleaBean.date = sdf.format(new Date()); // 게시글 올린 날짜

        //고유번호를 생성한다
        //String guid = getUserIdFromUUID(fleaBean.userId);
        //dbRef.child("buy").child( guid ).child( fleaBean.id ).setValue(fleaBean);
        dbRef.child("buy").child(fleaBean.id).setValue(fleaBean);
        Toast.makeText(this, "게시물이 등록 되었습니다.", Toast.LENGTH_LONG).show();

        finish();
    }

    public static String getUserIdFromUUID(String userEmail) {
        long val = UUID.nameUUIDFromBytes(userEmail.getBytes()).getMostSignificantBits();
        return String.valueOf(val);
    }

    //갤러리에서 이미지 가져오기
    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private static final int PICK_FROM_ALBUM = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {

            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e("test", tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }

            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {

            Uri photoUri = data.getData();

            Cursor cursor = null;

            try {

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = {MediaStore.Images.Media.DATA};

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImage();

        } else if (requestCode == REQUEST_IMAGE_CAPTURE) { //카메라로부터 오는 데이터를 취득한다.
            sendPicture();
        }
    }

    //갤러리에서 받아온 이미지 넣기
    private void setImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        Bitmap resizedBmp = getResizedBitmap(originalBm, 4, 100, 100);

        //줄어든 이미지를 다시 저장한다
        mPhotoPath = tempFile.getAbsolutePath();
        mCaptureUri = Uri.fromFile(tempFile);
        saveBitmapToFileCache(resizedBmp, mPhotoPath);

        mimgBuyWrite.setImageBitmap(resizedBmp);
    }

    //사진
    private void takePicture() {

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mCaptureUri = Uri.fromFile(getOutPutMediaFile());
        } else {
            mCaptureUri = FileProvider.getUriForFile(this,
                    "com.example.cho1.guru2_final_project_1cho", getOutPutMediaFile());
        }

        i.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureUri);

        //내가 원하는 액티비티로 이동하고, 그 액티비티가 종료되면 (finish되면)
        //다시금 나의 액티비티의 onActivityResult() 메서드가 호출되는 구조이다.
        //내가 어떤 데이터를 받고 싶을때 상대 액티비티를 호출해주고 그 액티비티에서
        //호출한 나의 액티비티로 데이터를 넘겨주는 구조이다. 이때 호출되는 메서드가
        //onActivityResult() 메서드 이다.
        startActivityForResult(i, REQUEST_IMAGE_CAPTURE);

    }

    private File getOutPutMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "cameraDemo");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        mPhotoPath = file.getAbsolutePath();

        return file;
    }

    private void sendPicture() {
        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath);
        Bitmap resizedBmp = getResizedBitmap(bitmap, 4, 100, 100);  //이미지 사이즈를 줄여줌 > size를 1로 하면 원본 크기로 나옴 > 4는 1/4사이즈

        bitmap.recycle();

        //사진이 캡쳐되서 들어오면 뒤집어져 있다. 이 애를 다시 원상복구 시킨다.
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(mPhotoPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;
        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientToDegree(exifOrientation);
        } else {
            exifDegree = 0;
        }
        Bitmap rotatedBmp = rotate(resizedBmp, 90);
        mimgBuyWrite.setImageBitmap(rotatedBmp);
        //줄어든 이미지를 다시 저장한다
        saveBitmapToFileCache(rotatedBmp, mPhotoPath);

        //사진이 저장된 경로 보여주기
        Toast.makeText(this, "사진 경로 : " + mPhotoPath, Toast.LENGTH_SHORT).show();
    }

    private void saveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int exifOrientToDegree(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bmp, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
                matrix, true);
    }

    //비트맵의 사이즈를 줄여준다.
    public static Bitmap getResizedBitmap(Bitmap srcBmp, int size, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = size;
        Bitmap resized = Bitmap.createScaledBitmap(srcBmp, width, height, true);
        return resized;
    }

    public static Bitmap getResizedBitmap(Resources resources, int id, int size, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = size;
        Bitmap src = BitmapFactory.decodeResource(resources, id, options);
        Bitmap resized = Bitmap.createScaledBitmap(src, width, height, true);
        return resized;
    }

}
