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
import com.example.cho1.guru2_final_project_1cho.firebase.DownloadImgTaskFlea;
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

public class BuyModifyActivity extends AppCompatActivity {

    public static final String STORAGE_DB_URL = "gs://guru2-final-project-1cho.appspot.com/";

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance(STORAGE_DB_URL);
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();

    private List<FleaBean> mFleaList = new ArrayList<>();

    private int itemNum = 0; //????????? ????????? ????????? ????????? ????????????
    private int itemNum2 = 0;

    private String mCategory;

    private ImageView mimgBuyWrite;  //??????
    private EditText medtTitle;  //??????
    private EditText medtExplain;  //??????
    private EditText medtPrice;  //??????
    private EditText medtSalePrice;  //?????????
    private EditText medtBuyDay;  //?????????
    private EditText medtExprieDate;  //????????????
    private EditText medtDefect;  //?????? ??????
    private EditText medtSize;  //?????? ?????? ?????????
    private Spinner mspinner1;  //????????????
    private Spinner mspinner2;  //?????? ??????

    private File tempFile;

    private FleaBean mFleaBean;

    //??????
    private Uri mCaptureUri;
    public String mPhotoPath;
    public static final int REQUEST_IMAGE_CAPTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_modify);

        mFleaBean = (FleaBean) getIntent().getSerializableExtra("BUYITEM");
//        FleaAdapter.setFleaBean(mFleaBean);

        //???????????? ???????????? ?????? ???????????? ????????????.
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);

        mimgBuyWrite = findViewById(R.id.imgBuyModify);
        Button mbtnImgReg = findViewById(R.id.btnBuyModifyImgReg);
        Button mbtnGalleryReg = findViewById(R.id.btnBuyModifyGalleryReg);
        //????????????
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

        medtTitle = findViewById(R.id.edtBuyModifyTitle);
        medtExplain = findViewById(R.id.edtBuyModifyExplain);
        medtPrice = findViewById(R.id.edtBuyModifyPrice);
        medtSalePrice = findViewById(R.id.edtBuyModifySalePrice);
        medtBuyDay = findViewById(R.id.edtBuyModifyDay);
        medtExprieDate = findViewById(R.id.edtBuyModifyExprieDate);
        medtDefect = findViewById(R.id.edtBuyModifyDefect);
        medtSize = findViewById(R.id.edtBuyModifySize);
        mspinner1 = findViewById(R.id.spinBuyModifyCategory);
        mspinner2 = findViewById(R.id.spinBuyModifyState);

        findViewById(R.id.btnBuyModifyOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //?????? ????????????
                update();
            }
        });


        //?????? ????????? ????????? ????????????
        mFirebaseDB.getReference().child("buy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FleaBean bean = snapshot.getValue(FleaBean.class);
                    if (TextUtils.equals(mFleaBean.id, bean.id)) {
                        // imgTitle ???????????? ????????? ?????? ?????? ????????? ?????? ??????????????????, ???????????? ????????????.
                        try {
                            if (bean.bmpTitle == null) {
                                new DownloadImgTaskFlea(BuyModifyActivity.this, mimgBuyWrite, mFleaList, 0).execute(new URL(bean.imgUrl));
                            } else {
                                mimgBuyWrite.setImageBitmap(bean.bmpTitle);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        medtTitle.setText(bean.title);
                        medtExplain.setText(bean.subtitle);
                        medtPrice.setText(bean.price);
                        medtSalePrice.setText(bean.saleprice);
                        medtBuyDay.setText(bean.buyday);
                        medtExprieDate.setText(bean.expire);
                        medtDefect.setText(bean.fault);
                        medtSize.setText(bean.size);

                        //???????????? ???????????? ????????? ??????
                        Spinner dropdown = (Spinner) findViewById(R.id.spinBuyModifyCategory);
                        String[] items = new String[]{"???", "???", "????????????", "????????????", "?????????", "????????????", "????????????", "?????????", "??????"};
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(BuyModifyActivity.this, android.R.layout.simple_spinner_dropdown_item, items);
                        dropdown.setAdapter(adapter);

                        //???????????? ???????????? ????????? ??????
                        Spinner dropdown2 = (Spinner) findViewById(R.id.spinBuyModifyState);
                        String[] items2 = new String[]{"???", "???", "???"};
                        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(BuyModifyActivity.this, android.R.layout.simple_spinner_dropdown_item, items2);
                        dropdown2.setAdapter(adapter2);

                        //bean.category??? ?????????????????? ?????? ??????(items)??? ??? ????????? ???????????? ????????? ?????????
                        for (int i = 0; i < items.length; i++) {
                            if (items[i] == bean.category) {
                                itemNum = i;
                                break;
                            }
                        }
                        //bean.category??? ?????????????????? ?????? ??????(items)??? ??? ????????? ???????????? ????????? ?????????
                        for (int i = 0; i < items2.length; i++) {
                            if (items2[i] == bean.state) {
                                itemNum2 = i;
                                break;
                            }
                        }
                        //????????? ????????? ?????? ????????? ??????
                        mspinner1.setSelection(itemNum);
                        mspinner2.setSelection(itemNum2);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }  //end onCreate


    //????????? ??????
    private void update() {
            //???????????? ??????, DB ??? ???????????? ????????????.
            if (mPhotoPath == null) {
                mFleaBean.title = medtTitle.getText().toString();  //??????
                mFleaBean.subtitle = medtExplain.getText().toString();  //??????
                mFleaBean.price = medtPrice.getText().toString();  //??????
                mFleaBean.saleprice = medtSalePrice.getText().toString();  //?????????
                mFleaBean.buyday = medtBuyDay.getText().toString();  //?????????
                mFleaBean.expire = medtExprieDate.getText().toString();  //????????????
                mFleaBean.fault = medtDefect.getText().toString();  //?????? ??????
                mFleaBean.size = medtSize.getText().toString();  //?????? ?????? ?????????
                mFleaBean.category = mspinner1.getSelectedItem().toString();  //????????????
                mFleaBean.state = mspinner2.getSelectedItem().toString();  //?????? ??????
                mFleaBean.date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());  //??? ?????? ??????

                //DB ?????????
                DatabaseReference dbRef = mFirebaseDB.getReference();
                //?????? ID ??? ????????? ??????
                dbRef.child("buy").child(mFleaBean.id).setValue(mFleaBean);
                Toast.makeText(this, "?????? ???????????????.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
//        //???????????????
//        mCategory = getIntent().getStringExtra("CATEGORY");
//        if (mCategory.equals("???")) {
//            if (mPhotoPath == null) {
//                if (medtTitle.length() == 0 || medtPrice.length() == 0 || medtSalePrice.length() == 0 || medtBuyDay.length() == 0 || medtDefect.length() == 0 || medtSize.length() == 0 || medtExplain.length() == 0){
//                    Toast.makeText(this, "?????? ????????? ???????????????", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                else{
//                    mFleaBean.title = medtTitle.getText().toString();  //??????
//                    mFleaBean.subtitle = medtExplain.getText().toString();  //??????
//                    mFleaBean.price = medtPrice.getText().toString();  //??????
//                    mFleaBean.saleprice = medtSalePrice.getText().toString();  //?????????
//                    mFleaBean.buyday = medtBuyDay.getText().toString();  //?????????
//                    mFleaBean.expire = medtExprieDate.getText().toString();  //????????????
//                    mFleaBean.fault = medtDefect.getText().toString();  //?????? ??????
//                    mFleaBean.size = medtSize.getText().toString();  //?????? ?????? ?????????
//                    mFleaBean.category = mspinner1.getSelectedItem().toString();  //????????????
//                    mFleaBean.state = mspinner2.getSelectedItem().toString();  //?????? ??????
//                    mFleaBean.date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());  //??? ?????? ??????
//                }
//
//                //DB ?????????
//                DatabaseReference dbRef = mFirebaseDB.getReference();
//                //?????? ID ??? ????????? ??????
//                dbRef.child("buy").child(mFleaBean.id).setValue(mFleaBean);
//                Toast.makeText(this, "?????? ???????????????.", Toast.LENGTH_LONG).show();
//                finish();
//                return;
//            }
//        }


            //????????? ????????? ??????, ???????????? ????????? ?????? DB ???????????? ??????.
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
                    //?????? ????????? ????????? ????????????.
                    //?????? ????????? ????????? ????????????.
                    if (mFleaBean.imgName != null) {
                        try {
                            mFirebaseStorage.getReference().child("images").child(mFleaBean.imgName).delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    mFleaBean.imgUrl = task.getResult().toString();
                    mFleaBean.imgName = mCaptureUri.getLastPathSegment();
                    mFleaBean.subtitle = medtExplain.getText().toString();
                    mFleaBean.title = medtTitle.getText().toString();  //??????
                    mFleaBean.subtitle = medtExplain.getText().toString();  //??????
                    mFleaBean.price = medtPrice.getText().toString();  //??????
                    mFleaBean.saleprice = medtSalePrice.getText().toString();  //?????????
                    mFleaBean.buyday = medtBuyDay.getText().toString();  //?????????
                    mFleaBean.expire = medtExprieDate.getText().toString();  //????????????
                    mFleaBean.fault = medtDefect.getText().toString();  //?????? ??????
                    mFleaBean.size = medtSize.getText().toString();  //?????? ?????? ?????????
                    mFleaBean.category = mspinner1.getSelectedItem().toString();  //????????????
                    mFleaBean.state = mspinner2.getSelectedItem().toString();  //?????? ??????

                    //????????? ?????????
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    mFleaBean.date = sdf.format(new Date());

                    mFirebaseDB.getReference().child("buy").child(mFleaBean.id).setValue(mFleaBean);

                    Toast.makeText(getBaseContext(), "?????? ???????????????.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            });
    }

    public static String getUserIdFromUUID(String userEmail) {
        long val = UUID.nameUUIDFromBytes(userEmail.getBytes()).getMostSignificantBits();
        return String.valueOf(val);
    }

    //??????????????? ????????? ????????????
    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private static final int PICK_FROM_ALBUM = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {

            Toast.makeText(this, "?????? ???????????????.", Toast.LENGTH_SHORT).show();

            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e("test", tempFile.getAbsolutePath() + " ?????? ??????");
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
                 *  Uri ????????????
                 *  content:/// ?????? file:/// ???  ????????????.
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

        } else if (requestCode == REQUEST_IMAGE_CAPTURE) { //?????????????????? ?????? ???????????? ????????????.
            sendPicture();
        }
    }

    //??????????????? ????????? ????????? ??????
    private void setImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        Bitmap resizedBmp = getResizedBitmap(originalBm, 4, 100, 100);

        //????????? ???????????? ?????? ????????????
        mPhotoPath = tempFile.getAbsolutePath();
        mCaptureUri = Uri.fromFile(tempFile);
        saveBitmapToFileCache(resizedBmp, mPhotoPath);

        mimgBuyWrite.setImageBitmap(resizedBmp);
    }

    private void takePicture() {

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mCaptureUri = Uri.fromFile(getOutPutMediaFile());
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
        Bitmap resizedBmp = getResizedBitmap(bitmap, 4, 100, 100);

        bitmap.recycle();

        //????????? ???????????? ???????????? ???????????? ??????. ????????? ?????? ???????????? ?????????.
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
        Bitmap rotatedBmp = roate(resizedBmp, 90); // ????????? ??????
        mimgBuyWrite.setImageBitmap(rotatedBmp);

        //????????? ???????????? ?????? ????????????
        saveBitmapToFileCache(rotatedBmp, mPhotoPath);

        Toast.makeText(this, "????????????:" + mPhotoPath, Toast.LENGTH_SHORT).show();
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

    private Bitmap roate(Bitmap bmp, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    //???????????? ???????????? ????????????.
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
