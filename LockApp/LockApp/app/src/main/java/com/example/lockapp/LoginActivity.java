package com.example.lockapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "show detail";

    //控件
    private ImageView img_logo;
    private EditText et_userid_for_login;
    private EditText et_password_for_login;
    private Button bt_login;
    private Button bt_register_without_account;

    //socket变量
    private Socket socket = null;

    //线程池
    private ExecutorService loginThreadPool = null;

    //接收消息线程
    private Handler recLoginHandler = null;

    //接收到的反馈消息
    private String response = "";

    //返回token
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //初始化控件
        img_logo = (ImageView)findViewById(R.id.img_logo);
        et_userid_for_login = (EditText)findViewById(R.id.et_userid_for_login);
        et_password_for_login = (EditText)findViewById(R.id.et_password_for_login);
        bt_login = (Button)findViewById(R.id.bt_login);
        bt_register_without_account = (Button)findViewById(R.id.bt_register_without_account);

        //界面说明
        Log.i(TAG,"来到登陆界面");

        //账号和密码框，自动读取文件中的内容，若内容未空，则提示注册
        SharedPreferences sharedPreferences = getSharedPreferences("basicinfo", MODE_PRIVATE);
        String userid = sharedPreferences.getString("userid", null);
        String password = sharedPreferences.getString("password", null);
        if (userid != null && password != null){
            et_userid_for_login.setText(userid);
            et_password_for_login.setText(password);
        }else {
            Toast.makeText(this, "未找到您的账号和密码", Toast.LENGTH_LONG).show();
        }

//        Map<String, String> maps = new UserData().read_basicinfo();
//        if (maps != null){
//            String userid = maps.get("userid");
//            String password = maps.get("password");
//            et_userid_for_login.setText(userid);
//            et_password_for_login.setText(password);
//        }else {
//            Toast.makeText(this, "账号和密码为空，请注册", Toast.LENGTH_LONG).show();
//        }

        //初始化线程池
        loginThreadPool = Executors.newCachedThreadPool();

        //利用线程池开启线程，建立socket连接
        if (socket == null) {
            loginThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "准备建立socket连接");
                        socket = new Socket(IpAndPort.ip(), IpAndPort.port());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (socket != null)
                        Log.i(TAG, "连接成功");
                    else
                        Log.i(TAG, "连接失败");
                }
            });
        }

        //实例化接收线程，用于更新并显示接收过来的消息
        recLoginHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 99:
                        response = msg.obj.toString();
                        Log.i(TAG + " response", response);
                        operateBackMessage(response);
                        break;
                }
            }
        };

        //接收服务器发送的数据
        receiceInfo();

    }

    private void startsocket(Socket socket){

    }

    /*登陆按钮，向服务器发送消息*/
    public void loginFunction(View view) {
        //利用线程池开启线程
        loginThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //读取用户信息：userid, password
                String userid = et_userid_for_login.getText().toString();
                String password = et_password_for_login.getText().toString();

                //读取token信息
                SharedPreferences sharedPreferences = getSharedPreferences("tokeninfo", MODE_PRIVATE);
                String token = sharedPreferences.getString("token", null);

                if (userid != null && password != null && token != null) {
                    //获得要发送的数据
                    JSONObject login_request = new UserStatus().sendMessageByJson("sign_in", userid, "", password,
                            "", "", "", token);
                    Log.i(TAG + " 申请登陆要发送的数据", login_request.toString());
                    //发送消息给服务器
                    if (socket != null) {
                        OutputStream outputStream = null;
                        try {
                            outputStream = socket.getOutputStream();
                            outputStream.write(login_request.toString().getBytes());
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
//                            try {
//                                if (outputStream != null) {
//                                    outputStream.close();
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                }else {
                    Looper.prepare();
                    Toast.makeText(LoginActivity.this, "登录信息缺失！", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    /*从服务器接收消息*/
    private void receiceInfo(){
        //利用线程池开启线程
        loginThreadPool.execute(new Runnable() {
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
                            byte[] b = new byte[128];
                            //int length = dataInputStream.read(b);
                            int length = -1;
                            while ((length = dataInputStream.read(b)) != -1) {
                                String back_message = new String(b, 0, length);
                                //通知接收线程，将接收到的消息显示到界面
                                Message message = new Message();
                                message.what = 99;
                                message.obj = back_message;
                                recLoginHandler.sendMessage(message);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
//                    try {
//                        if (dataInputStream != null && inputStream != null) {
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

    /*注册按钮*/
    public void gotoRegister(View view) {
        //跳转到注册界面
        if (socket != null) {
            stopSocketConnect(socket);

        }
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    /*处理返回信息*/
    private void operateBackMessage(String response){
        if (response.contains("token")){
            if (response.contains("true")) {

                Log.i(TAG, "登录成功");
                Toast.makeText(this, "登陆成功", Toast.LENGTH_LONG).show();

                //从response中提取token
                String[] resp_cut1 = response.split("\"token\"");
                String[] resp_cut2 = resp_cut1[1].split("\"");
                token = resp_cut2[1];
                Log.i(TAG + " 接收到新的token，token更新为", token);

                //保存token
                SharedPreferences sharedPreferences = getSharedPreferences("tokeninfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("token", token);
                editor.commit();

                //将token保存到tokeninfo.txt中
                new UserData().save_token_info(token);

                //跳转页面
                turnToMain();

            }else {
                Toast.makeText(this, "登陆失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*断开socket连接*/
    private void stopSocketConnect(final Socket socket){
        //利用线程池开启线程，发送断开连接请求
        loginThreadPool.execute(new Runnable() {
            @Override
            public void run() {
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
                } finally {
//                    try {
//                        if (outputStream != null) {
//                            outputStream.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });
    }

    //跳转到主页面
    private void turnToMain(){
        if (socket != null) {
            stopSocketConnect(socket);
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (socket != null){
            stopSocketConnect(socket);
        }
    }
}
