package com.example.cho1.guru2_final_project_1cho.activity;


import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
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

public class JoinActivity extends AppCompatActivity {
    //멤버변수
    private ImageView mImgProfile;
    private EditText mEdtId, mEdtName;

    private String mCurrentImageFilePath = null;

    public static final String STORAGE_DB_URL = "gs://guru2-final-project-1cho.appspot.com";
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance(STORAGE_DB_URL);
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    //사진이 저장되는 경로 - onActivityResult()로부터 받는 데이터
    private Uri mCaptureUri;
    //사진이 저장된 단말기상의 실제 경로
    public String mPhotoPath;
    //startActivityForResult() 에 넘겨주는 값, 이 값이 나중에 onActivityResult()로 돌아와서
    //내가 던진값인지를 구별할 때 사용하는 상수이다.
    public static final int REQUEST_IMAGE_CAPTURE = 200;

    public String tokenId;
    private MemberBean mMemberBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //카메라를 사용하기 위한 퍼미션을 요청한다.
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);

        tokenId = getIntent().getStringExtra("tokenId");
        String email = getIntent().getStringExtra("userEmail");
        mImgProfile = findViewById(R.id.imgProfile);
        mEdtId = findViewById(R.id.edtId);
        mEdtId.setText(email);
        mEdtId.setEnabled(false);
        mEdtName = findViewById(R.id.edtName);

        mMemberBean = FileDB.getLoginMember(this);

        if(mMemberBean != null) {
            mMemberBean.bmpTitle = getIntent().getParcelableExtra("titleBitmap");
            if(mMemberBean.bmpTitle != null) {
                mImgProfile.setImageBitmap(mMemberBean.bmpTitle);
            }
        }

        //카메라 버튼
        findViewById(R.id.btnCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        //회원가입 버튼
        findViewById(R.id.btnJoin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( mEdtName.getText().toString().length() == 0) {
                    Toast.makeText(JoinActivity.this, "이름을 입력하세요..", Toast.LENGTH_SHORT).show();
                    mEdtName.requestFocus();
                    return;
                }

                joinProcess();
            }
        });
    }//end onCreate()


    //회원가입 작업시작
    private void joinProcess() {
        //사진올리고
        if (mPhotoPath == null) {
            Toast.makeText(this, "사진을 찍어 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuthWithGoogle(tokenId);

        //MemberBean 생성
        //insert
        //firebase인증

    }

    private void uploadDB(String imgUrl, String imgName) {
        //Firebase 데이터베이스에 메모를 등록한다.
        DatabaseReference dbRef = mFirebaseDatabase.getReference();
        //String id = dbRef.push().getKey(); // key를 메모의 고유 ID로 사용한다.

        //데이터베이스에 저장한다.
        MemberBean memberBean = new MemberBean();
        memberBean.memId = mFirebaseAuth.getCurrentUser().getEmail();
        memberBean.memName = mEdtName.getText().toString();
        memberBean.imgUrl = imgUrl;
        memberBean.imgName = imgName;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        memberBean.date = sdf.format(new Date());

        //고유번호를 생성한다
        String guid = getUserIdFromUUID(mFirebaseAuth.getCurrentUser().getEmail());

        dbRef.child("member").child(guid).setValue(memberBean);
        Toast.makeText(this, "멤버 저장 완료", Toast.LENGTH_SHORT).show();

        //메인 화면으로 이동
        startActivity(new Intent(JoinActivity.this, MainActivity.class));
        finish();
    }

    public static String getUserIdFromUUID(String userEmail) {
        long val = UUID.nameUUIDFromBytes(userEmail.getBytes()).getMostSignificantBits();
        return String.valueOf(val);
    }

    private void firebaseAuthWithGoogle(String tokenId) {
        //Firebase 인증
        AuthCredential credential = GoogleAuthProvider.getCredential(tokenId, null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    //Firebase 로그인 성공
                    Toast.makeText(getBaseContext(), "FireBase 로그인 성공", Toast.LENGTH_SHORT).show();
                    //메인 화면으로 이동
                    //startActivity(new Intent(JoinActivity.this, MainActivity.class));
                    //finish();

                    StorageReference stroageRef = mFirebaseStorage.getReference();
                    final StorageReference imagesRef = stroageRef.child("images/"
                            + mCaptureUri.getLastPathSegment()); // images/파일날짜.jpg 로 들어감

                    UploadTask uploadTask = imagesRef.putFile(mCaptureUri);

                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() { // Task<Uri> 입력하고 Alt+Enter
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return imagesRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            //database upload를 호출한다.
                            uploadDB(task.getResult().toString(), mCaptureUri.getLastPathSegment());
                        }
                    });


                } else {
                    // 로그인 실패
                    Toast.makeText(getBaseContext(), "FireBase 로그인 실패", Toast.LENGTH_SHORT).show();
                    Log.w("TEST", "인증실패: " + task.getException());
                }
            }
        });
    }

    private void takePicture() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mCaptureUri = Uri.fromFile( getOutPutMediaFile() );
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

    // 이미지 파일명 생성
    private File createFileName() {
        // 현재 "년월일 시분초"를 기준으로 파일명 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = timeStamp + ".jpg";

        File myDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "item");
        if (!myDir.exists()) {
            myDir.mkdir();
        }

        File imageFile = new File(myDir, fileName);
        mCurrentImageFilePath = imageFile.getAbsolutePath();

        return imageFile;
    }
/*
    private void checkPermission() {
        // Self 권한 체크
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            // 권한동의 체크
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
            ) {
                //다이얼로 출력

            } else {
                // 권한동의 팝업 표시 요청
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA}, 1111);
            }
        }
    } // End checkPermission
    */

    // 사용자 권한동의 결과 획득
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 1111:
                // 0:권한 허용  -1:권한 거부
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] < 0) {
                        Toast.makeText(this, "해당 권한을 활성화하셔야 합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
        } // End switch
    }

    private File getOutPutMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "cameraDemo");
        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {
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
        Bitmap resizedBmp = getResizedBitmap(bitmap, 4, 100, 100);

        bitmap.recycle();

        //사진이 캡쳐되서 들어오면 뒤집어져 있다. 이애를 다시 원상복구 시킨다.
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(mPhotoPath);
        } catch(Exception e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;
        if(exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientToDegree(exifOrientation);
        } else {
            exifDegree = 0;
        }
        Bitmap rotatedBmp = roate(resizedBmp, exifDegree);
        mImgProfile.setImageBitmap( rotatedBmp );

        //줄어든 이미지를 다시 저장한다
        saveBitmapToFileCache(rotatedBmp, mPhotoPath);

        Toast.makeText(this, "사진경로:"+mPhotoPath, Toast.LENGTH_SHORT).show();
    }

    private void saveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try
        {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private int exifOrientToDegree(int exifOrientation) {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap roate(Bitmap bmp, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
                matrix, true);
    }

    //비트맵의 사이즈를 줄여준다.
    public static Bitmap getResizedBitmap(Bitmap srcBmp, int size, int width, int height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = size;
        Bitmap resized = Bitmap.createScaledBitmap(srcBmp, width, height, true);
        return resized;
    }

    public static Bitmap getResizedBitmap(Resources resources, int id, int size, int width, int height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = size;
        Bitmap src = BitmapFactory.decodeResource(resources, id, options);
        Bitmap resized = Bitmap.createScaledBitmap(src, width, height, true);
        return resized;
    }

    // 카메라, 앨범등의 처리결과
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //카메라로부터 오는 데이터를 취득한다.
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_IMAGE_CAPTURE) {
                sendPicture();
            }
        }
    }
}//end class