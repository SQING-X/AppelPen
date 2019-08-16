package com.example.lockapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class BindPictureForRegistActivity extends AppCompatActivity {

    private String TAG = "show detail";

    //控件
    private Button bt_take_photo;
    private ImageView img_show_picture;
    private TextView tv_show_photo_path;

    //拍照请求码
    private static int REQUEST_CAMERA_CODE = 100;

    //照片存储URi
    Uri bind_picture_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_picture_for_regist);

        //初始化控件
        bt_take_photo = (Button)findViewById(R.id.bt_take_photo_for_bind);
        img_show_picture = (ImageView)findViewById(R.id.img_show_picture);
        tv_show_photo_path = (TextView)findViewById(R.id.tv_show_photo_path);

        //界面声明
        Log.i(TAG, "来到拍照绑定人脸照片界面");

    }

    /*拍照按钮*/
    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        bind_picture_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, bind_picture_uri);
        startActivityForResult(intent, REQUEST_CAMERA_CODE);
    }

    /*拍照完成后会执行onActivityResult()方法*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_CAMERA_CODE){
                Bitmap bitmap = null;
                try {
                    //读取图像文件内容，转换成bitmap对象
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(bind_picture_uri));
                    String bind_photo_base64 = PictureOperate.bitmapToBase64(new PictureOperate().compressImage(bitmap));
//                    Log.i(TAG + " 拍照成功，图片的base64编码是", bind_photo_base64);

                    //通过SharedPreferences存储base64编码
                    SharedPreferences sharedPreferences_base64 = getSharedPreferences("bind_photo_base64", MODE_PRIVATE);
                    SharedPreferences.Editor editor_base64 = sharedPreferences_base64.edit();
                    editor_base64.putString("base64", bind_photo_base64);
                    editor_base64.commit();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //将bitmap对象显示在ImageView中
                img_show_picture.setImageBitmap(bitmap);
                //获取照片路径，并转换成base64编码存储
                if(bitmap != null){
                    //Uri转换成路径
                    Log.i(TAG + " 绑定照片的URi", bind_picture_uri.getPath());
                    String path = PictureOperate.getRealFilePath(this, bind_picture_uri);
                    Log.i(TAG + " 绑定照片的存储路径", path);

                    //通过SharedPreferences存储path     /storage/emulated/0/Pictures/1565230517460.jpg
                    SharedPreferences sharedPreferences_path = getSharedPreferences("bind_photo", MODE_PRIVATE);
                    SharedPreferences.Editor editor_path = sharedPreferences_path.edit();
                    editor_path.putString("path", path);
                    editor_path.commit();

                    //tv_show_photo_path.setText("URi:" + bind_picture_uri.toString() + "\t" + "path:" + path);
                    Toast.makeText(this, "绑定人脸成功，3秒后自动返回主界面", Toast.LENGTH_LONG).show();
                    //延迟5s执行
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            turnToLogin();
                        }
                    }, 3000);
                }
            }
        }
    }

    /*拍照绑定成功，返回登陆界面*/
    private void turnToLogin(){
        Log.i(TAG, "绑定人脸成功，返回主界面");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        BindPictureForRegistActivity.this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
