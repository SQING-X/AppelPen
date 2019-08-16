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

import org.json.JSONObject;

import java.io.FileNotFoundException;

public class OpenLockByCameraActivity extends AppCompatActivity {

    private String TAG = "show detail";

    //控件
    private Button bt_take_photo_for_recognition;
    private ImageView img_show_photo;
    private TextView tv_show_detail;
    private Button bt_setMqtt;

    //拍照请求码
    private static int CAMERA_CODE = 101;

    //照片存储URi，路径,base64
    Uri take_photo_uri = null;
    String path = null;
    String photo_to_base64 = null;

    MqttManager mqttManager = null;

    int qos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo_for_recognition);

        //初始化控件
        bt_take_photo_for_recognition = (Button)findViewById(R.id.bt_take_photo_for_recognition);
        img_show_photo = (ImageView)findViewById(R.id.img_show_photo);
        tv_show_detail = (TextView)findViewById(R.id.tv_show_detail);
        bt_setMqtt = (Button)findViewById(R.id.bt_setMqtt);

        //界面介绍
        Log.i(TAG, "来到拍照界面");
    }

    /*拍照按钮*/
    public void takePhototFunction(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        take_photo_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, take_photo_uri);
        startActivityForResult(intent, CAMERA_CODE);
    }

    /*拍照完成后会执行onActivityResult方法*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == CAMERA_CODE){
                Bitmap bitmap = null;
                try {
                    //读取图像文件内容，转换成bitmap对象
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(take_photo_uri));
                    //通过Bitmap生成base64编码
                    photo_to_base64 = PictureOperate.bitmapToBase64(bitmap);
                    //Log.i(TAG + " 拍照生成图片的base64编码", photo_to_base64);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //将bitmap对象显示在ImageView中
                img_show_photo.setImageBitmap(bitmap);
                Log.i(TAG + " 拍照的URi", take_photo_uri.getPath());
            }
        }
    }

    /*解锁按钮，建立MQTT,订阅发布信息*/
    public void openLockFunc(View view) {
        if (take_photo_uri != null) {

            //将URi转换成路径
            path = PictureOperate.getRealFilePath(this, take_photo_uri);

            //读取账号token
            SharedPreferences sharedPreferences_token = getSharedPreferences("tokeninfo", MODE_PRIVATE);
            String token = sharedPreferences_token.getString("token", null);
            Log.i(TAG + " token", token);

            //读取账号绑定照片的base64编码
            SharedPreferences sharedPreferences_bind_photo_base64 = getSharedPreferences("bind_photo_base64", MODE_PRIVATE);
            String bind_photo_base64 = sharedPreferences_bind_photo_base64.getString("base64", null);
//            Log.i(TAG + " 绑定照片的base64编码", bind_photo_base64);

            //读取绑定锁的lockid
            SharedPreferences sharedPreferences_lockid = getSharedPreferences("lockinfo", MODE_PRIVATE);
            String lock_uuid = sharedPreferences_lockid.getString("lockid", null);
            Log.i(TAG + " lock_uuid", lock_uuid);

            //生成要发送的数据
            JSONObject open_lock_request = new UserStatus().sendMqttMessage("face_compare", "phone", bind_photo_base64, photo_to_base64, lock_uuid, token );

            if (bind_photo_base64 != null && photo_to_base64 != null && lock_uuid != null && token != null) {

                //Mqtt操作，建立接连，发布消息，订阅消息
                mqttManager = new MqttManager(OpenLockByCameraActivity.this);

                //建立连接
                mqttManager.connect();

                //订阅消息
                mqttManager.subscribe("your_token", qos);
                mqttManager.subscribe("binded_lock_uuid", qos);


                //发布消息
                mqttManager.publish("UnLock", open_lock_request, false, qos);
                Log.i(TAG + " 要发布的主题为UnLock的消息", open_lock_request.toString());

            }

            if (mqttManager != null) {
                if (mqttManager.isMqttConnect()) {
                    //连接成功
                    Toast.makeText(OpenLockByCameraActivity.this, "消息发送成功，4秒后返回主界面", Toast.LENGTH_LONG).show();
                    mqttManager.disconnect();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(OpenLockByCameraActivity.this, MainActivity.class);
                            startActivity(intent);
                            OpenLockByCameraActivity.this.finish();
                        }
                    }, 4000);
                }
            }
        }
    }


}
