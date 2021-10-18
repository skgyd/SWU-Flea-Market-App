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

public class SellWriteActivity extends AppCompatActivity {

    public static final String STORAGE_DB_URL = "gs://guru2-final-project-1cho.appspot.com/";

    private ImageView mImgSellWrite;
    private EditText mEdtTitle, mEdtWishPrice, mEdtWishOption;
    private Spinner mspinner1;  //카테고리
    private int itemNum = 0; //스피너 선택값 불러와 저장할 임시변수
    private String mCategory;

    private File tempFile;

    //Firebase DB 저장 변수
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance(STORAGE_DB_URL);
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    //사진 찍기 변수
    private Uri mCaptureUri;
    public String mPhotoPath;
    public static final int REQUEST_IMAGE_CAPTURE = 200;

    private FleaBean mFleaBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_write);

        //카테고리 드롭다운 스피너 추가
        Spinner dropdown = (Spinner) findViewById(R.id.spinSellWriteCategory);
        String[] items = new String[]{"옷", "책", "생활용품", "기프티콘", "데이터", "대리예매", "전자기기", "화장품", "기타"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        //카메라를 사용하기 위한 퍼미션을 요청한다.
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);

        mImgSellWrite = findViewById(R.id.imgSellWrite);
        mEdtTitle = findViewById(R.id.edtSellWriteTitle);
        mEdtWishPrice = findViewById(R.id.edtSellWriteWishPrice);
        mEdtWishOption = findViewById(R.id.edtSellWriteWishOption);
        mspinner1 = findViewById(R.id.spinSellWriteCategory);
        Button mBtnImgReg = findViewById(R.id.btnSellWriteImgReg);
        Button mBtnGalleryReg = findViewById(R.id.btnSellWriteGalleryReg);
        Button mBtnSellReg = findViewById(R.id.btnSellWriteReg);

        mBtnImgReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mBtnGalleryReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });

        mBtnSellReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFleaBean == null) {
                    //신규 등록
                    upload();
                } else {
                    //수정 업데이트
                    //update();
                }
            }
        });

        mFleaBean = (FleaBean) getIntent().getSerializableExtra(FleaBean.class.getName());
        if (mFleaBean != null) {
            getIntent().getParcelableArrayExtra("titleBitmap");
            if (mFleaBean.bmpTitle != null) {
                mImgSellWrite.setImageBitmap(mFleaBean.bmpTitle);
            }
            mEdtTitle.setText(mFleaBean.selltitle);
            mEdtWishPrice.setText(mFleaBean.wishprice);
            mEdtWishOption.setText(mFleaBean.wishoption);
        }
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

    //새 글 등록
    private void upload() {
        if (mPhotoPath == null) {
            Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mEdtTitle.length() == 0) {
            mEdtTitle.requestFocus();
            Toast.makeText(this, "제목을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mEdtWishPrice.length() == 0) {
            mEdtWishPrice.requestFocus();
            Toast.makeText(this, "희망가를 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mEdtWishOption.length() == 0) {
            mEdtWishOption.requestFocus();
            Toast.makeText(this, "희망옵션을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        //사진 업로드
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
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                //database upload 를 호출한다.
                uploadDB(task.getResult().toString(), mCaptureUri.getLastPathSegment());
            }
        });
    }

    private void uploadDB(String imgUrl, String imgName) {
        //Firebase 데이터베이스에 글 등록한다.
        DatabaseReference dbRef = mFirebaseDatabase.getReference();
        String id = dbRef.push().getKey(); // key 를 메모의 고유 ID 로 사용한다.

        //데이터베이스에 저장한다.
        FleaBean fleaBean = new FleaBean();
        fleaBean.id = id;
        fleaBean.userId = mFirebaseAuth.getCurrentUser().getEmail();
        fleaBean.selltitle = mEdtTitle.getText().toString();
        fleaBean.wishprice = mEdtWishPrice.getText().toString();
        fleaBean.wishoption = mEdtWishOption.getText().toString();
        fleaBean.category = mspinner1.getSelectedItem().toString(); //카테고리
        fleaBean.imgUrl = imgUrl;
        fleaBean.imgName = imgName;
        fleaBean.date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

        //고유번호를 생성한다
        //String guid = getUserIdFromUUID(fleaBean.userId);
        //dbRef.child("sell").child( guid ).child( fleaBean.id ).setValue(fleaBean);
        dbRef.child("sell").child(fleaBean.id).setValue(fleaBean);
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

        mImgSellWrite.setImageBitmap(resizedBmp);
    }

    //사진 찍기
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
        mImgSellWrite.setImageBitmap(rotatedBmp);
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

    //사진찍기완료
}
