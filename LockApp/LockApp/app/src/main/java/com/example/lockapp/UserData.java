package com.example.lockapp;


import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserData {

    private String TAG = "show detail";

    String data_path = "/data/data/com.example.lockapp/userdata";

    //创建用于存储数据文件夹userdata，在欢迎界面创建
    protected void create_folder(){
        File file = new File(data_path);
        if (file.exists()){
            Log.i(TAG, "userdata文件夹已存在");
//            Log.i(TAG, "名称--" + file.getName());
//            Log.i(TAG, "路径--" + file.getPath());
        }else {
            file.mkdir();
            if (file.exists()){
                Log.i(TAG, "userdata文件夹被创建");
//                Log.i(TAG, "名称--" + file.getName());
//                Log.i(TAG, "路径--" + file.getPath());
            }else {
                Log.i(TAG, "userdata文件夹未被创建");
            }
        }
    }

   //创建用于保存用户信息的txt文件，basicinfo.txt     userid, username, password,
    private File basicinfo(){
        File file = new File(data_path + "/" + "basicinfo.txt");
        createAndCheckFile(file, "basicinfo.txt");
        return file;
    }

    //创建用于保存用户token的txt文件，tokeninfo.txt     token
    private File tokeninfo(){
        File file = new File(data_path + "/" + "tokeninfo.txt");
        createAndCheckFile(file, "tokeninfo.txt");
        return file;
    }

    /*创建用于保存用户注册时绑定照片的base64编码字符串的文件    bind_photo_base64str.txt*/
    private File bind_photo_base64info(){
        File file = new File(data_path + "/" + "bind_photo_base64info.txt");
        createAndCheckFile(file, "bind_photo_base64info.txt");
        return file;
    }

    /**
     * 创建文件，并在日志中打印相关信息
     * @param file  文件
     * @param filename  文件名称，可带后缀
     */
    private void createAndCheckFile(File file, String filename){
        if (file.exists()){
            Log.i(TAG, filename + "文件已存在，更新文件内容");
//            Log.i(TAG, "名称--" + file.getName());
//            Log.i(TAG, "路径--" + file.getPath());
        }else {
            try {
                file.createNewFile();
                if (file.exists()){
                    Log.i(TAG, filename + "被创建");
//                    Log.i(TAG + "名字--", file.getName());
//                    Log.i(TAG + "路径--", file.getPath());
                }else {
                    Log.i(TAG, filename + "仍未被创建");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //向basicinfo.txt中写入用户信息，userid, username, password, token
    protected void save_base_info(String userid, String username, String password){
        //要存储的内容
        String content = userid + "##" + username + "##" + password + "##";
        File file = basicinfo();
        saveStr(file, content);
    }

    //读取basicinfo.txt中的信息   userid, username, password
    protected Map<String, String> read_basicinfo(){
        Map<String, String> map = null;
        File file = basicinfo();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            //TODO  参考注册界面，使用DataInputStream,不使用BufferedReader
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            //读取数据
            byte[] b = new byte[256];
            int length = dataInputStream.read(b);
            String content = new String(b);
            //TODO  切割字符串，封装到Map集合中
            if (content.contains("##")) {
                map = new HashMap<String, String>();
                String[] content_cut = content.split("##");
                String userid = content_cut[0];
                String username = content_cut[1];
                String password = content_cut[2];
                //放入对应的键值对中
                map.put("userid", userid);
                map.put("username", username);
                map.put("password", password);
            }
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    //向tokeninfo.txt中写入token信息，token
    protected void save_token_info(String token){
        String content = "token##" + token + "##";
        File file = tokeninfo();
        saveStr(file, content);
    }

    //读取tokeninfo.txt中的信息   token
    protected Map<String, String> read_tokeninfo(){
        Map<String, String> map = null;
        File file = tokeninfo();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            byte[] b = new byte[256];
            int length = dataInputStream.read(b);
            String content = new String(b);
            //TODO  封装到Map集合中
            if (content.contains("##")) {
                map = new HashMap<String, String>();
                String[] content_cut = content.split("##");
                map.put("token", content_cut[1]);
            }
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /*向bind_photo_base64str.txt中写入base64编码信息*/
    protected void  save_bind_photo_base64_info(String base64str){
        String content = "base64##" + base64str + "##";
        File file = bind_photo_base64info();
        saveStr(file, content);
    }

    /*读取bind_photo_base64str.txt中的信息   base64*/
    protected Map<String, String> read_bind_photo_base64(){
        Map<String, String> map = null;
        File file = bind_photo_base64info();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            byte[] b = new byte[256];
            int length = dataInputStream.read(b);
            String content = new String(b);
            //TODO  封装到Map集合中
            if (content.contains("##")){
                map = new HashMap<String, String>();
                String[] content_cut = content.split("##");
                map.put("base64", content_cut[1]);
            }
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 向目标文件中写入（保存）数据
     * @param file  目标文件
     * @param content   要写入的内容
     */
    private void saveStr(File file, String content){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

