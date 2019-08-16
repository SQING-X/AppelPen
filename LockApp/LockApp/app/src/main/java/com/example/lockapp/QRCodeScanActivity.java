package com.example.lockapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.json.JSONObject;

public class QRCodeScanActivity extends AppCompatActivity {

    private String TAG = "show detail";

    private int REQUEST_CODE_SCAN = 369;

    //控件
    private Button bt_scan_qrcode;
    private TextView tv_display_lock_uuid;
    private Button bt_exit_scan_qrcode;

    //扫码结果
    private String qrcode_info = null;

    //扫码信息提取
    private String lock_uuid = null;
    private String token_gather = null;
    private int time_in_qrcode = 0;     //单位：秒

    MqttManager mqttManager = null;
    int qos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);

        //界面介绍：来到二维码扫码界面

        //控件初始化
        bt_scan_qrcode = (Button)findViewById(R.id.bt_scan_qrcode);
        tv_display_lock_uuid = (TextView)findViewById(R.id.tv_display_lock_uuid);
        bt_exit_scan_qrcode = (Button)findViewById(R.id.bt_exit_scan_qrcode);

    }

    /*扫码按钮*/
    public void scanQRCode(View view) {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描二维码
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SCAN){
            if (data != null){
                qrcode_info = data.getStringExtra(Constant.CODED_CONTENT);
                //tv_display_lock_uuid.setText("扫码结果：" + qrcode_info);
                Log.i(TAG + " 扫码结果", qrcode_info);
                if (qrcode_info != null){
                    //提取二维码有效信息     lock_uuid   token_gather    time
                    lock_uuid = OperateScanQRCodeinfo.getLock_uuid(qrcode_info);
                    token_gather = OperateScanQRCodeinfo.getTokenGather(qrcode_info);
                    time_in_qrcode = OperateScanQRCodeinfo.getTime(qrcode_info);

                    //在界面显示设备ID
                    tv_display_lock_uuid.setText(lock_uuid);

                    //处理二维码有效信息
                    if (lock_uuid != null && token_gather != null && time_in_qrcode != 0){
                        sendOpenLockMqttRequest(lock_uuid, token_gather, time_in_qrcode);
                    }
                }
            }
        }
    }

    /*处理解码信息，建立MQTT连接，发送解锁申请*/
    private void sendOpenLockMqttRequest(String lock_uuid, String token_gather, int time_in_qrcode){

        //读取账号token
        SharedPreferences sharedPreferences_token = getSharedPreferences("tokeninfo", MODE_PRIVATE);
        String token = sharedPreferences_token.getString("token", null);

        //获得当前时间、时间差
        int current_time = (int) (System.currentTimeMillis()/1000);     //单位：秒
        int time_delay = (current_time - time_in_qrcode)/60;        //单位：分钟

        //读取绑定照片的base64
        SharedPreferences sharedPreferences_bind_photo_base64 = getSharedPreferences("bind_photo_base64", MODE_PRIVATE);
        String bind_photo_base64 = sharedPreferences_bind_photo_base64.getString("base64", null);

        //条件
        if (time_delay >= 3){
            //Looper.prepare();
            Toast.makeText(QRCodeScanActivity.this, "二维码已过期，扫码解锁失败！", Toast.LENGTH_SHORT).show();
            //Looper.loop();
        }else if (!token_gather.contains(token)){
            //Looper.prepare();
            Toast.makeText(QRCodeScanActivity.this, "您没有绑定该设备，无法解锁！", Toast.LENGTH_SHORT).show();
            //Looper.loop();
        }else {
            //生成要发送的JSON格式消息体
            JSONObject open_lock_request_by_scan_qrcode = new UserStatus().sendMqttMessageByScanQRcode("phone", "yes_unlock", lock_uuid, token, bind_photo_base64);

            //TODO  建立Mqtt连接
            //Mqtt初始化
            mqttManager = new MqttManager(QRCodeScanActivity.this);

            //Mqtt连接
            mqttManager.connect();

            //订阅消息
            mqttManager.subscribe("your_token", qos);
            mqttManager.subscribe("bind_lock_uuid", qos);

            //发布消息
            mqttManager.publish("UnLock", open_lock_request_by_scan_qrcode, false, qos);
            Log.i(TAG + " 要发布的主题为UnLock的消息", open_lock_request_by_scan_qrcode.toString());

            if (mqttManager.isMqttConnect()){
                //Looper.prepare();
                Toast.makeText(QRCodeScanActivity.this, "消息发送成功，请等待处理结果", Toast.LENGTH_SHORT).show();
                //Looper.loop();
            }else {
                //Looper.prepare();
                Toast.makeText(QRCodeScanActivity.this, "解锁请求发送失败！", Toast.LENGTH_SHORT).show();
                //Looper.loop();
            }
        }

    }

    /*退出扫码*/
    public void exitScanQRCode(View view) {
        if (mqttManager != null){
            mqttManager.disconnect();
        }
        Intent intent = new Intent(QRCodeScanActivity.this, MainActivity.class);
        startActivity(intent);
        QRCodeScanActivity.this.finish();
    }


}
