package com.example.lockapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 欢迎界面，启动APP，3s后进入注册\登陆界面
 * 实现权限申请
 */
public class WelcomeActivity extends AppCompatActivity {

    private boolean ispermission = false;

    //创建Handler,是消息传递机制，可以在不同线程之间传递数据。
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //从欢迎界面跳转到注册/登陆界面
            intent_to_login();
            WelcomeActivity.this.finish();
        }
    };

    /**
     * 权限申请
     * 网络权限
     * SD卡读写权限
     * 照相机权限
     */
    public void init_permission(){
        //所有权限
        String[] permission = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
        };
        //需要动态申请的权限
        List<String> unadmit_permissionList = new ArrayList<>();
        for (int i = 0; i < permission.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permission[i])
                    != PackageManager.PERMISSION_GRANTED){
                unadmit_permissionList.add(permission[i]);
            }
        }
        if (unadmit_permissionList.isEmpty()){
            ispermission = true;
            Toast.makeText(this, "APP权限均已获取", Toast.LENGTH_LONG).show();
        }else {//存在未获取权限
            String[] unadmit_permissionStr = unadmit_permissionList.toArray(new String[unadmit_permissionList.size()]);
            //申请权限
            ActivityCompat.requestPermissions(this, unadmit_permissionStr, 666);
        }
    }

    /*重写函数，该函数是请求权限后的回调*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 666:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                            //如果不勾选不再提示，且拒绝权限授予，则无限循环弹出申请框
                            init_permission();
                            return;
                        }
                    }
                }
                break;
        }
    }

    /*跳转到注册界面*/
    private void intent_to_login() {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //1、在清单文件中，将该WelcomeActivity设置为启动界面
        //2、将背景图片设置为缩放适应屏幕：将图片属性中的scaleType设置为"centerCrop"
        //3、去掉标题栏，在res/values/styles.xml中添加“去掉Activity的ActionBar的样式”，具体代码见该页
        //4、去掉状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //权限申请
        init_permission();

        //创建存储数据文件夹
        new UserData().create_folder();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //子线程,3s后调用该run()函数
                handler.sendEmptyMessage(0);
            }
        },weltime());
    }

    private int weltime(){
        int time;
        if (ispermission = true){
            time = 1000;
        }else {
            time = 6000;
        }
        return time;
    }

}













