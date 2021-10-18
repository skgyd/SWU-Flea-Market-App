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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.cho1.guru2_final_project_1cho.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 4.4 kitkat 까지 지원하는 카메라 예제.
 */
public class CameraCaptureActivity extends AppCompatActivity {

    private ImageView mImgProfile;
    //사진이 저장도니 경로 - onActivityResult()로부터 받는 데이터
    private Uri mCaptureUri;
    //사진이 저장된 단말기상의 실제 경로
    public String mPhotoPath;
    //startActivityForResult() 에 넘겨주는 값, 이 값이 나중에 onActivityResult()로 돌아와서
    //내가 던진값인지를 구별할 때 사용하는 상수이다.
    public static final int REQUEST_IMAGE_CAPTURE = 200;

    //멤버변수
    private EditText mEdtId, mEdtName;


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

        mImgProfile = findViewById(R.id.imgProfile);
        Button btnCamera = findViewById(R.id.btnCamera);
        //사진찍기 버튼
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mEdtId = findViewById(R.id.edtId);
        mEdtName = findViewById(R.id.edtName);

        //회원가입 버튼
        findViewById(R.id.btnJoin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinProcess();
            }
        });

    }//end onCreate()

    //회원가입 처리
    private void joinProcess() {
/*
        MemberBean memberBean = new MemberBean();
        memberBean.memId = mEdtId.getText().toString();
        memberBean.memName = mEdtName.getText().toString();
        memberBean.memKakaoId = mEdtKakaoId.getText().toString();
        memberBean.photoPath = mPhotoPath;

        String pw1 = mEdtPw1.getText().toString();
        String pw2 = mEdtPw2.getText().toString();
        //서로 패스워드가 일치하는지 확인한다.
        if( !TextUtils.equals(pw1, pw2) ) {
            //일치하지 않을경우는
            Toast.makeText(this, "패스워드가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        //아이디가 공백인지 체크한다.
        if( TextUtils.isEmpty(memberBean.memId) ) {
            Toast.makeText(this, "회원 아이디를 입력하세요", Toast.LENGTH_LONG).show();
            return;
        }
        //이미 존재하는 회원 아이디를 찾는다.
        MemberBean findMemBean = FileDB.getFindMember(this, memberBean.memId);
        if( findMemBean != null ) {
            //해당되는 아이디를 찾았다.
            Toast.makeText(this, "입력하신 회원 아이디를 이미 존재 합니다.", Toast.LENGTH_LONG).show();
            return;
        }

        memberBean.memPw = pw1;

        //memberBean 을 파일로 저장한다. ==> JSON 변환후
        FileDB.addMember(this, memberBean);

        //회원가입 완료
        Toast.makeText(this, "회원가입이 완료 되었습니다.", Toast.LENGTH_LONG).show();
        finish();*/
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

        Toast.makeText(this, "사진경로: " + mPhotoPath, Toast.LENGTH_LONG).show();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //카메라로부터 오는 데이터를 취득한다.
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_IMAGE_CAPTURE) {
                sendPicture();
            }
        }
    }

}
