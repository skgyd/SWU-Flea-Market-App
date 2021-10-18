package com.example.cho1.guru2_final_project_1cho.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import com.example.cho1.guru2_final_project_1cho.bean.ExBean;
import com.example.cho1.guru2_final_project_1cho.firebase.DownloadImgTaskEx;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExModifyActivity extends AppCompatActivity {

   public static final String STORAGE_DB_URL = "gs://guru2-final-project-1cho.appspot.com/";

    private ImageView mImgItem; // 내 물건
    private EditText mEdtTitle; // 내 물건명
    private Button mbtnModifyImgEx; // 사진찍기 버튼
    private Button mbtnModifyGalleryEx; // 갤러리 버튼
    private  EditText mEdtItem; // 교환하고자 하는 물건명
    private  EditText mEdtPrice; // 원가
    private EditText mEdtBuyDate; // 구매한 날짜
    private EditText mEdtExpDate; // 유통기한
    private EditText mEdtFault; // 결함
    private EditText mEdtSize; // 사이즈
    private Spinner mSprState; // 상태

    private File tempFile;

    private ExBean mExBean;
    private List<ExBean> mExList = new ArrayList<>();
    private Context mContext;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance(STORAGE_DB_URL);
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private int itemNum = 0;

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
        setContentView(R.layout.activity_ex_modify);

        //카메라를 사용하기 위한 퍼미션을 요청한다.
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);

        mExBean = (ExBean) getIntent().getSerializableExtra("EXITEM");

        mImgItem = findViewById(R.id.imgItem);
        mbtnModifyImgEx = findViewById(R.id.btnModifyImgEx);
        mbtnModifyGalleryEx = findViewById(R.id.btnModifyGalleryEx);
        mEdtTitle = findViewById(R.id.edtTitle);
        mEdtItem = findViewById(R.id.edtItem);
        mEdtPrice = findViewById(R.id.edtPrice);
        mEdtBuyDate = findViewById(R.id.edtBuyDate);
        mEdtExpDate = findViewById(R.id.edtExpDate);
        mEdtFault = findViewById(R.id.edtFault);
        mEdtSize = findViewById(R.id.edtSize);
        mSprState = findViewById(R.id.sprState);


        //기존 데이터 가져와 뿌려주기
        mFirebaseDatabase.getReference().child("ex").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //데이터를 받아와서 List에 저장.
                //mExAdapter.clear();
                mExList.clear();
                mExList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    /*for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                        ExBean bean = snapshot2.getValue(ExBean.class); //파이어베이스 이중구조 처리방법*/
                    ExBean bean = snapshot.getValue(ExBean.class);
                    if(TextUtils.equals(mExBean.id, bean.id)) {
                        // imgTitle 이미지를 표시할 때는 원격 서버에 있는 이미지이므로, 비동기로 표시한다.
                        try {
                            if (bean.bmpTitle == null) {
                                new DownloadImgTaskEx(mContext, mImgItem, mExList, 0).execute(new URL(bean.imgUrl));
                            } else {
                                mImgItem.setImageBitmap(bean.bmpTitle);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mEdtTitle.setText(bean.mine); // 내 물건
                        mEdtItem.setText(bean.want); // 상대방 물건
                        mExBean.state = mSprState.getSelectedItem().toString(); // 물건 상태
                        mEdtPrice.setText(bean.price); // 원가
                        mEdtFault.setText(bean.fault); // 하자
                        mEdtExpDate.setText(bean.expire); // 유통기한
                        mEdtBuyDate.setText(bean.buyDate); // 구매한 날짜
                        mEdtSize.setText(bean.size); // 사이즈

                        //제품상태 드롭다운 스피너 추가
                        Spinner dropdown = (Spinner) findViewById(R.id.sprState);
                        String[] items = new String[]{"상", "중", "하"};
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ExModifyActivity.this, android.R.layout.simple_spinner_dropdown_item, items);
                        dropdown.setAdapter(adapter);

                        //bean.category에 저장된항목이 기존 배열(items)의 몇 번째에 위치하고 있는지 알아냄
                        for(int i=0; i<items.length; i++) {
                            if(items[i] == bean.state) {
                                itemNum = i;
                                break;
                            }
                        }
                        mSprState.setSelection(itemNum);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        mbtnModifyImgEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        mbtnModifyGalleryEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAlbum();
            }
        });

        findViewById(R.id.btnModify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ( mEdtTitle.getText().toString().length() == 0) {
                    Toast.makeText(ExModifyActivity.this, "슈니의 물건명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    mEdtTitle.requestFocus();
                    return;
                }
                if ( mEdtItem.getText().toString().length() == 0) {
                    Toast.makeText(ExModifyActivity.this, "무엇으로 교환하고 싶은지 입력하세요.", Toast.LENGTH_SHORT).show();
                    mEdtItem.requestFocus();
                    return;
                }

                if ( mEdtFault.getText().toString().length() == 0) {
                    Toast.makeText(ExModifyActivity.this, "물건의 하자유무를 입력하세요.", Toast.LENGTH_SHORT).show();
                    mEdtFault.requestFocus();
                    return;
                }

                // DB 업로드
                update();
            }
        });

        //제품상태 드롭다운 스피너 추가
        Spinner dropdown = (Spinner)findViewById(R.id.sprState);
        String[] states = new String[]{"상", "중", "하"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, states);
        dropdown.setAdapter(adapter);


    } // onCreate()

    // 게시물 수정
    private void update() {
        // 사진을 찍었을 경우, 안 찍었을 경우
        if(mPhotoPath == null) {
            // 안 찍었을 경우, DB만 업데이트 시켜준다.
            mExBean.mine = mEdtTitle.getText().toString(); // 내물건 이름
            mExBean.want = mEdtItem.getText().toString(); // 교환하고 싶은 물건
            mExBean.state = mSprState.getSelectedItem().toString(); // 물건 상태

            mExBean.price = mEdtPrice.getText().toString(); // 원가
            mExBean.fault = mEdtFault.getText().toString(); // 하자
            mExBean.expire = mEdtExpDate.getText().toString(); // 유통기한
            mExBean.buyDate = mEdtBuyDate.getText().toString(); // 구매날짜
            mExBean.size = mEdtSize.getText().toString(); // 실측사이즈
            // DB 업로드
            DatabaseReference dbRef = mFirebaseDatabase.getReference();
            String uuid = getUserIdFromUUID(mExBean.userId);
            // 동일 ID로 데이터 수정
            dbRef.child("ex").child(mExBean.id).setValue(mExBean);
            Toast.makeText(this, "수정되었습니다.", Toast.LENGTH_LONG).show();

            finish();
            return;

        }

        // 사진을 찍었을 경우, 사진부터 업로드하고 DB 업데이트한다.
        StorageReference storageRef = mFirebaseStorage.getReference();
        final StorageReference imagesRef = storageRef.child("images/" + mCaptureUri.getLastPathSegment());
        UploadTask uploadTask = imagesRef.putFile(mCaptureUri);
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
                // 파일 업로드 완료후 호출된다.
                // 기존 이미지 파일을 삭제한다.
                if (mExBean.imgName != null) {
                    try {
                        mFirebaseStorage.getReference().child("images").child(mExBean.imgName).delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mExBean.imgUrl = task.getResult().toString();
                mExBean.imgName = mCaptureUri.getLastPathSegment();
                mExBean.mine = mEdtTitle.getText().toString(); // 내물건 이름
                mExBean.want = mEdtItem.getText().toString(); // 교환하고 싶은 물건
                mExBean.state = mSprState.getSelectedItem().toString(); // 물건 상태
                mExBean.price = mEdtPrice.getText().toString(); // 원가
                mExBean.fault = mEdtFault.getText().toString(); // 하자
                mExBean.expire = mEdtExpDate.getText().toString(); // 유통기한
                mExBean.buyDate = mEdtBuyDate.getText().toString(); // 구매날짜
                mExBean.size = mEdtSize.getText().toString(); // 실측사이즈

                // 수정된 날짜로
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                mExBean.date = sdf.format(new Date());

                String uuid = getUserIdFromUUID(mExBean.userId);
                mFirebaseDatabase.getReference().child("ex").child(mExBean.id).setValue(mExBean);

                Toast.makeText(getBaseContext(), "수정되었습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
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
        Bitmap rotatedBmp = roate(resizedBmp, 90); // 돌아감 수정
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
