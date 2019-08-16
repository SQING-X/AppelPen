package com.example.lockapp;

public class IpAndPort {

    //socket = new Socket("114.116.49.97", 8080)
    //socket = new Socket("192.168.1.2", 9999)

    public static String ip(){
        //String ip ="192.168.1.2";
        String ip = "114.116.49.97";    //真机上线使用
        return ip;
    }

    public static int port(){
        //int port = 9999;
        int port = 8080;        //真机上线使用
        return port;
    }

}
