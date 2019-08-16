package com.example.lockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private String TAG = "show detail";

    //控件
    private ImageView img_face;
    private TextView tv_display_username;
    private TextView tv_display_lockname;
    private Button bt_bind_lock_request;
    private ImageButton imgbt_require_open_lock;
    private ImageButton imgbt_scan_open_lock;
    private Button bt_exit_login;

    String photo_path = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化控件
        img_face = (ImageView)findViewById(R.id.img_face);
        tv_display_username = (TextView)findViewById(R.id.tv_display_username);
        tv_display_lockname = (TextView)findViewById(R.id.tv_display_lockname);
        bt_bind_lock_request = (Button)findViewById(R.id.bt_bind_lock_request);
        imgbt_require_open_lock = (ImageButton)findViewById(R.id.imgbt_require_open_lock);
        imgbt_scan_open_lock = (ImageButton)findViewById(R.id.imgbt_scan_open_lock);
        bt_exit_login = (Button)findViewById(R.id.bt_exit_login);

        //界面信息
        Log.i(TAG, "来到主页面");

        //图像框显示注册时绑定的人脸照片
        displayFace();

        //用户名框显示用户名usernam
        displayUsername();

        //绑定设备ID框显示lockid
        displayLockid();

    }

    /*申请绑锁按钮*/
    public void bindLockRequest(View view) {
        //跳转到绑锁界面
        Intent intent = new Intent(this, BindLockActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }

    /*申请开锁按钮，跳转到拍照界面，拍照，并发送对比图片的base64编码*/
    public void requestOpenLock(View view) {
        if (tv_display_lockname.getText().toString().contains("UNLOCK")){
            Toast.makeText(MainActivity.this, "请先绑定设备！", Toast.LENGTH_LONG).show();
        }else if (photo_path == null){
            Toast.makeText(MainActivity.this, "请先绑定头像！", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, OpenLockByCameraActivity.class);
            startActivity(intent);
           // MainActivity.this.finish();
        }
    }

    /*扫码开锁按钮*/
    public void scanOpenLock(View view) {
        Intent intent = new Intent(MainActivity.this, QRCodeScanActivity.class);
        startActivity(intent);
       // MainActivity.this.finish();
    }

    /*退出登陆按钮，跳转返回登陆界面*/
    public void exitLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }

    /*图片框加载用户注册时绑定的头像*/
    private void displayFace(){
        //通过SharedPreferences提取图片path
        SharedPreferences sharedPreferences = getSharedPreferences("bind_photo", MODE_PRIVATE);
        photo_path = sharedPreferences.getString("path", null);
        //将图片加载到ImageView
        Bitmap bitmap = BitmapFactory.decodeFile(photo_path);
        img_face.setImageBitmap(bitmap);
    }

    /*用户名框显示用户名usernam*/
    private void displayUsername(){
        SharedPreferences sharedPreferences = getSharedPreferences("basicinfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        tv_display_username.setText(username);
    }

    /*绑定设备ID框显示lockid*/
    private void displayLockid(){
        //通过SharedPreferences获得lockid
        SharedPreferences sharedPreferences = getSharedPreferences("lockinfo", MODE_PRIVATE);
        String lockid = sharedPreferences.getString("lockid", "UNLOCK");
        tv_display_lockname.setText(lockid);
    }

    /*绑定头像*/
    public void bindFace(View view) {
        Intent intent = new Intent(MainActivity.this, BindPictureForRegistActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
       // MainActivity.this.finish();
    }
}









