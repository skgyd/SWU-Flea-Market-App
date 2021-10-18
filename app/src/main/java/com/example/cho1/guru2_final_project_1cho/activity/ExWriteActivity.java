package com.example.cho1.guru2_final_project_1cho.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
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
import com.example.cho1.guru2_final_project_1cho.bean.ExBean;
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

public class ExWriteActivity extends AppCompatActivity {

    public static final String STORAGE_DB_URL = "gs://guru2-final-project-1cho.appspot.com/";

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance(STORAGE_DB_URL);
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private ImageView mImgItem;
    private Button mBtnImgEx;
    private Button mBtnGalleryEx;
    private EditText mEdtTitle;
    private  EditText mEdtItem;
    private  EditText mEdtPrice;
    private EditText mEdtBuyDate;
    private EditText mEdtExpDate;
    private EditText mEdtFault;
    private EditText mEdtSize;
    private Spinner mSprState;

    private File tempFile;



    // 사진이 저장되는 경로
    private Uri mCaptureUri;
    //사진이 저장된 단말기상의 실제 경로
    public String mPhotoPath;
    //startActivityForResult() 에 넘겨주는 값, 이 값이 나중에 onActivityResult()로 돌아와서
    //내가 던진값인지를 구별할 때 사용하는 상수.
    public static final int REQUEST_IMAGE_CAPTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_write);

        //카메라를 사용하기 위한 퍼미션을 요청한다.
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);

        mImgItem = findViewById(R.id.imgItem);
        mBtnImgEx = findViewById(R.id.btnImgEx);
        mBtnGalleryEx = findViewById(R.id.btnGalleryEx);
        mEdtTitle = findViewById(R.id.edtTitle);
        mEdtItem = findViewById(R.id.edtItem);
        mEdtPrice = findViewById(R.id.edtPrice);
        mEdtBuyDate = findViewById(R.id.edtBuyDate);
        mEdtExpDate = findViewById(R.id.edtExpDate);
        mEdtFault = findViewById(R.id.edtFault);
        mEdtSize = findViewById(R.id.edtSize);
        mSprState = findViewById(R.id.sprState);

        mBtnImgEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        mBtnGalleryEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAlbum();
            }
        });

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DB 업로드
                if ( mEdtTitle.getText().toString().length() == 0) {
                    Toast.makeText(ExWriteActivity.this, "슈니의 물건명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    mEdtTitle.requestFocus();
                    return;
                }
                if ( mEdtItem.getText().toString().length() == 0) {
                    Toast.makeText(ExWriteActivity.this, "무엇으로 교환하고 싶은지 입력하세요.", Toast.LENGTH_SHORT).show();
                    mEdtItem.requestFocus();
                    return;
                }

                if ( mEdtFault.getText().toString().length() == 0) {
                    Toast.makeText(ExWriteActivity.this, "물건의 하자유무를 입력하세요.", Toast.LENGTH_SHORT).show();
                    mEdtFault.requestFocus();
                    return;
                }

                upload();
            }
        });

        //제품상태 드롭다운 스피너 추가
        Spinner dropdown = (Spinner)findViewById(R.id.sprState);
        String[] states = new String[]{"상", "중", "하"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, states);
        dropdown.setAdapter(adapter);


    } // onCreate()

    // 새 게시글 작성
    private void upload() {

        if(mPhotoPath == null) {
            Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        //사진부터 Storage 에 업로드한다.
        StorageReference storageRef = mFirebaseStorage.getReference();
        final StorageReference imagesRef = storageRef.child("images/" + mCaptureUri.getLastPathSegment()); //images/파일날짜.jpg

        UploadTask uploadTask = imagesRef.putFile(mCaptureUri);
        //파일 업로드 실패에 따른 콜백 처리를 한다.
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
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
        ExBean exBean = new ExBean();
        exBean.id = id;
        exBean.userId = mFirebaseAuth.getCurrentUser().getEmail(); // email
        exBean.imgUrl = imgUrl;
        exBean.imgName = imgName;
        exBean.mine = mEdtTitle.getText().toString(); // 내물건 이름
        exBean.want = mEdtItem.getText().toString(); // 교환하고 싶은 물건
        exBean.price = mEdtPrice.getText().toString(); // 내 물건 원가
        exBean.state = mSprState.getSelectedItem().toString(); // 물건 상태
        exBean.fault = mEdtFault.getText().toString(); // 하자
        exBean.expire = mEdtExpDate.getText().toString(); // 유통기한
        exBean.buyDate = mEdtBuyDate.getText().toString(); // 구매날짜
        exBean.size = mEdtSize.getText().toString(); // 사이즈

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        exBean.date = sdf.format(new Date()); // 게시글 올린 날짜

        //고유번호를 생성한다
        //String guid = getUserIdFromUUID(exBean.userId);
        //dbRef.child("ex").child( guid ).child( exBean.id ).setValue(exBean);
        dbRef.child("ex").child( exBean.id ).setValue(exBean);
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

            if(tempFile != null) {
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
                String[] proj = { MediaStore.Images.Media.DATA };

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

        } else if(requestCode == REQUEST_IMAGE_CAPTURE) { //카메라로부터 오는 데이터를 취득한다.
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

        mImgItem.setImageBitmap(resizedBmp);
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
        Bitmap rotatedBmp = roate(resizedBmp, 90);
        mImgItem.setImageBitmap( rotatedBmp );
        //줄어든 이미지를 다시 저장한다
        saveBitmapToFileCache(rotatedBmp, mPhotoPath);

        Toast.makeText(this, "사진경로: " + mPhotoPath, Toast.LENGTH_LONG).show();
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

}