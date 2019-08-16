package com.example.lockapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BindLockActivity extends AppCompatActivity {

    private String TAG = "show detail";

    //控件
    private EditText et_lockid_for_bindlock;
    private Button bt_bind_lock;
    private Button bt_giveup_bindlock;
    private Button bt_scan_get_lockid;

    //socket变量
    private Socket socket = null;

    //线程池
    private ExecutorService bindLockThreadPool = null;

    //接收消息线程
    private Handler recBindLockHandler = null;

    //接收到的反馈消息
    private String response = null;

    //返回token
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_lock);

        //初始化控件
        et_lockid_for_bindlock = (EditText)findViewById(R.id.et_lockid_for_bindlock);
        bt_bind_lock = (Button)findViewById(R.id.bt_bind_lock);
        bt_giveup_bindlock = (Button)findViewById(R.id.bt_giveup_bindlock);
        bt_scan_get_lockid = (Button)findViewById(R.id.bt_scan_get_lockid);


        //界面说明
        Log.i(TAG, "来到绑定设备界面");

        //初始化线程池
        bindLockThreadPool = Executors.newCachedThreadPool();

        //通过线程池开启线程，建立socket连接
        bindLockThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "准备建立socket连接");
                    socket = new Socket(IpAndPort.ip(),IpAndPort.port());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket.isConnected())
                {
                    Log.i(TAG, "socket连接成功" );
                }
            }
        });

        //实例化接收线程，用于更新并显示接收服务器的消息
        recBindLockHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 2:
                        response = msg.obj.toString();
                        Log.i(TAG + " response", response);
                        operateResponse(response);
                        break;
                }
            }
        };

        //接收服务器发送的数据
        receiveMessage();

    }

    /*绑定设备按钮，向服务器发送消息--绑定请求*/
    public void bindLockFunction(View view) {
        //通过线程池开启线程
        bindLockThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                //从输入框读取lockid
                String lockid = et_lockid_for_bindlock.getText().toString();

                //读取token
                SharedPreferences sharedPreferences = getSharedPreferences("tokeninfo", MODE_PRIVATE);
                token = sharedPreferences.getString("token", null);

                if (lockid != null && token != null) {
                    //生成要发送的JSON数据
                    JSONObject bind_lock_request = new UserStatus().sendMessageByJson("bind_lock", "", "", "",
                            "", lockid, "", token);
                    Log.i(TAG + " 绑锁申请发送的数据", bind_lock_request.toString());
                    //发送给服务器
                    if (socket != null) {
                        OutputStream outputStream = null;
                        try {
                            outputStream = socket.getOutputStream();
                            outputStream.write(bind_lock_request.toString().getBytes());
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /*从服务器接收消息*/
    private void receiveMessage(){
        //通过线程池开启线程
        bindLockThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                DataInputStream dataInputStream = null;
                try {
                    while (true){
                        if (socket != null) {
                            //创建输入流对象
                            inputStream = socket.getInputStream();
                            //长江见输入流读取器，并传入输入流对象
                            dataInputStream = new DataInputStream(inputStream);
                            //接收服务器发送过来的消息
                            byte[] b = new byte[256];
                            //int length = dataInputStream.read(b);
                            int length = -1;
                            while ((length = dataInputStream.read(b)) != -1) {
                                String back_message = new String(b, 0, length, "utf-8");
                                Log.i(TAG + " 从服务器收到的数据是", back_message);
                                //通知接收线程，将接收到的消息显示到界面
                                Message message = new Message();
                                message.what = 2;
                                message.obj = back_message;
                                recBindLockHandler.sendMessage(message);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*放弃绑定设备按钮，跳转返回主界面*/
    public void giveupBindLock(View view) {
        turnToMain();
    }

    /*处理服务器返回信息*/
    private void operateResponse(String response){
        //response      {"status": true, "msg": "\u8bbe\u5907\u7ed1\u5b9a\u6210\u529f", "user": "13051490833"}
        if (response.contains("true")){
            Toast.makeText(this, "绑定成功", Toast.LENGTH_LONG).show();

            //使用SharedPreferences存储lockid
            String lockid = et_lockid_for_bindlock.getText().toString();
            SharedPreferences sharedPreferences = getSharedPreferences("lockinfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lockid", lockid);
            editor.commit();

            //返回主界面
            turnToMain();

        }else if (response.contains("false")){
            Toast.makeText(this, "绑定失败", Toast.LENGTH_LONG).show();
        }
    }

    /*跳转回主界面*/
    private void turnToMain(){
        //断开连接
        if (socket != null) {
            stopSocketConnect(socket);
        }
        //跳转回主界面
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        BindLockActivity.this.finish();
    }

    /*断开socket连接*/
    private void stopSocketConnect(final Socket socket){
        //通过线程池开启线程，发送断开socket连接请求
        bindLockThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //生成断开请求
                JSONObject exit_request = new UserStatus().sendMessageByJson("", "", "",
                        "", "exit", "", "", "");
                Log.i(TAG + "断开连接时向服务器发送的请求是", exit_request.toString());
                OutputStream outputStream = null;
                try {
                    outputStream = socket.getOutputStream();
                    outputStream.write(exit_request.toString().getBytes());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*扫码获取设备lockid*/
    public void getLockidByScan(View view) {
        Intent intent = new Intent(BindLockActivity.this, CaptureActivity.class);
        startActivityForResult(intent, 666);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 666){
            if (data != null){
                String content = null;
                String lockID = null;
                content = data.getStringExtra(Constant.CODED_CONTENT);
                if (content != null){
                    lockID = OperateScanQRCodeinfo.getLock_uuid(content);
                }
                if (lockID != null){
                    et_lockid_for_bindlock.setText(lockID);
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (socket != null){
            //stopSocketConnect(socket);
        }
    }
}











