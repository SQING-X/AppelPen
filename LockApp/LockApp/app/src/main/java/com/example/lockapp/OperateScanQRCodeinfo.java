package com.example.lockapp;

public class OperateScanQRCodeinfo {

    //back_info = "{'lock_uuid':'d0131804-6b84-4425-9f87-3c0347f38a82','token':[('a97e81409438e575fe029b4c42f8c950'),('0ce81cdcc2baabb311811e9a0de8d74d',)],'time':1565765165.9758651}";

    public static String getLock_uuid(String back_message){
        String lock_uuid = null;

        int lock_uuid_index  = back_message.indexOf("lock_uuid");
        int token_index = back_message.indexOf("token");
        String lock_uuid_substring = back_message.substring(lock_uuid_index, token_index);
        lock_uuid = lock_uuid_substring.split("'")[2];

        return lock_uuid;
    }

    public static String getTokenGather(String back_message){
        String token_substring = null;

        int token_index = back_message.indexOf("token");
        int time_index = back_message.indexOf("time");
        token_substring = back_message.substring(token_index, time_index);

        return token_substring;
    }

    public static int getTime(String back_message){
        int time = 0;

        int time_index = back_message.indexOf("time");
        String time_substring = back_message.substring(time_index);
        String[] cut = time_substring.split(":");
        String time_str = cut[1].split("\\}")[0];
        time = (int) Double.parseDouble(time_str);

        return time;
    }


}
