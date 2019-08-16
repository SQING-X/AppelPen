package com.example.lockapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RegisterActivity extends AppCompatActivity {

    private String TAG = "show detail";

    //控件
    private EditText et_userid_for_regist;
    private EditText et_username_for_regist;
    private EditText et_password_for_regist;
    private Button bt_rigister;
    private Button bt_giveup_register;
    private TextView tv_show_response;

    //socket变量
    private Socket socket = null;

    //线程池
    private ExecutorService registerThreadPool = null;

    //接收线程
    private Handler recRegisterHandler = null;

    //接收到的反馈消息
    private String response = "";

    //返回token
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerctivity);

        //初始化控件
        et_userid_for_regist = (EditText)findViewById(R.id.et_userid_for_regist);
        et_username_for_regist = (EditText)findViewById(R.id.et_username_for_regist);
        et_password_for_regist = (EditText)findViewById(R.id.et_password_for_regist);
        bt_rigister = (Button)findViewById(R.id.bt_rigister);
        bt_giveup_register = (Button)findViewById(R.id.bt_giveup_register);
        tv_show_response = (TextView)findViewById(R.id.tv_show_response);

        //初始化线程池
        registerThreadPool = Executors.newCachedThreadPool();

        //界面说明
        Log.i(TAG, "来到注册界面");

        //删除app存储数据文件
        File file = new File("/data/data/com.example.lockapp/shared_prefs");
        if (file.exists()){
            file.delete();
        }

        //利用线程池开启线程，建立socket连接
        registerThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "准备建立socket连接");
                    socket = new Socket(IpAndPort.ip(),IpAndPort.port());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket != null){
                    if (socket.isConnected()){
                        Log.i(TAG, "连接成功");
                    }
                    else
                        Log.i(TAG, "连接失败");
                }
            }
        });

        //实例化接收线程，用于更新接收过来的消息
        recRegisterHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        response = msg.obj.toString();
                        Log.i(TAG + " response", response);
                        registerSuccess(response);
                        break;
                }
            }
        };

        //接收服务器发送的数据
        receiveMessage();

    }

    /*注册按钮，向服务器发送消息*/
    public void registerFunction(View view) {
        //利用线程池开启线程
        registerThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //获取用户注册时候输入的账号、用户名、密码
                String userid = et_userid_for_regist.getText().toString();
                String username = et_username_for_regist.getText().toString();
                String password = et_password_for_regist.getText().toString();
                //要发送的JSON数据
                JSONObject register_request = new UserStatus().sendMessageByJson("sign_up", userid, username, password, "",
                        "", "", "");
                Log.i(TAG + " 向服务器发送的消息", register_request.toString());
                if (socket != null){
                    OutputStream outputStream = null;
                    try {
                        //从socket获得输出流对象
                        outputStream = socket.getOutputStream();
                        outputStream.write(register_request.toString().getBytes());
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
//                        try {
//                            if (outputStream != null) {
//                                outputStream.close();
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                }else {
                    Looper.prepare();
                    Toast.makeText(RegisterActivity.this, "与服务器断开连接，请退出后重新注册", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    /*从服务器接收消息*/
    private void receiveMessage(){
        //利用线程池开启线程
        registerThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                DataInputStream dataInputStream = null;
                try {
                    while (true){
                        if (socket != null) {
                            //创建输入流对象
                            inputStream = socket.getInputStream();
                            //创建见输入流读取器，并传入输入流对象
                            dataInputStream = new DataInputStream(inputStream);
                            //接收服务器发送过来的消息
                            byte[] b = new byte[256];
                            //int length = dataInputStream.read(b);
                            int length = -1;
                            while ((length = dataInputStream.read(b)) != -1) {
                                String back_message = new String(b, 0, length, "utf-8");
                                //通知接收线程，将接收到的消息显示到界面
                                Message message = new Message();
                                message.what = 0;
                                message.obj = back_message;
                                recRegisterHandler.sendMessage(message);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
//                    try {//关闭数据流
//                        if (inputStream != null && dataInputStream != null) {
//                            dataInputStream.close();
//                            inputStream.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });
    }

    /*放弃注册按钮*/
    public void giveupRegister(View view) {
        if (socket != null) {
            stopSocketConnect(socket);
        }
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        RegisterActivity.this.finish();
    }

    /*判断是否注册成功（返回信息是否包含token）,然后保存数据*/
    private void registerSuccess(String response){
        if (response.contains("token")) {
            if (response.contains("true")) {
                Log.i(TAG, "注册成功");
                //获取token
                String[] strs = response.split("\"token\"")[1].split("\"");
                token = strs[1];
                Log.i(TAG + " token信息更新为", token);
                //将token信息保存到tokeninfo.txt中
                new UserData().save_token_info(token);
                //使用SharedPreferences存储
                SharedPreferences sharedPreferences_token = getSharedPreferences("tokeninfo", MODE_PRIVATE);
                SharedPreferences.Editor editor_token = sharedPreferences_token.edit();
                editor_token.putString("token", token);
                editor_token.commit();

                //获取用户注册基本信息
                String userid = et_userid_for_regist.getText().toString();
                String username = et_username_for_regist.getText().toString();
                String password = et_password_for_regist.getText().toString();
                //将用户注册基本信息保存到basicinfo.txt中
                new UserData().save_base_info(userid, username, password);
                //使用SharedPreferences方法存储
                SharedPreferences sharedPreferences_basicinfo = getSharedPreferences("basicinfo", MODE_PRIVATE);
                SharedPreferences.Editor editor_basicinfo = sharedPreferences_basicinfo.edit();
                editor_basicinfo.putString("userid", userid);
                editor_basicinfo.putString("username", username);
                editor_basicinfo.putString("password", password);
                editor_basicinfo.commit();

                Toast.makeText(this, "注册成功，将跳转到登陆界面", Toast.LENGTH_LONG).show();


                //删除旧账户信息   头像（path）,lockid
                SharedPreferences sharedPreferences_face_path = getSharedPreferences("bind_photo", MODE_PRIVATE);
                SharedPreferences.Editor editor_face_path = sharedPreferences_face_path.edit();
                editor_face_path.putString("path", null);
                editor_face_path.commit();

                SharedPreferences sharedPreferences_lock_uuid = getSharedPreferences("lockinfo", MODE_PRIVATE);
                SharedPreferences.Editor editor_lock_uuid = sharedPreferences_lock_uuid.edit();
                editor_lock_uuid.putString("lockid", "UNLOCK");
                editor_lock_uuid.commit();

                turnToBindPhoto();
            }else {
                Toast.makeText(this, "注册失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*注册成功（若返回消息中包含token），跳转到登陆的界面*/
    private void turnToBindPhoto(){
        if (socket != null) {
            stopSocketConnect(socket);
        }
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        RegisterActivity.this.finish();
    }

    /*断开socket连接*/
    private void stopSocketConnect(final Socket socket){
        registerThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject exit_request = new UserStatus().sendMessageByJson("", "", "",
                "", "exit", "", "", "");
                Log.i(TAG + " 断开连接时向服务器发送的请求是", exit_request.toString());
                OutputStream outputStream = null;
                try {
                    outputStream = socket.getOutputStream();
                    outputStream.write(exit_request.toString().getBytes());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
//                    try {
//                        outputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (socket != null){
            stopSocketConnect(socket);
        }
        RegisterActivity.this.finish();
    }
}













