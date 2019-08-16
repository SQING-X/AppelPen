package com.example.lockapp;

import org.json.JSONException;
import org.json.JSONObject;

public class UserStatus {

    protected JSONObject sendMessageByJson(String action, String userid, String username, String password,
                                 String hold, String lock_uuid, String face_data, String token){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action",action);
            jsonObject.put("userid", userid);
            jsonObject.put("username", username);
            jsonObject.put("passwd", password);
            jsonObject.put("hold", hold);
            jsonObject.put("lock_uuid", lock_uuid);
            jsonObject.put("face_data", face_data);
            jsonObject.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /*人脸解锁请求，topic：UnLock*/
    protected JSONObject sendMqttMessage(String action, String from, String face_data_1, String face_data_2,
                                         String lock_uuid, String token){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", action);
            jsonObject.put("from", from);
            jsonObject.put("lock_uuid", lock_uuid);
            jsonObject.put("token", token);
            jsonObject.put("face_data_1", face_data_1);
            jsonObject.put("face_data_2", face_data_2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /*扫码解锁请求    topiv:UnLock*/
    protected JSONObject sendMqttMessageByScanQRcode(String from, String action, String lock_uuid, String token, String bind_face_base64){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from", from);
            jsonObject.put("action", action);
            jsonObject.put("lock_uuid", lock_uuid);
            jsonObject.put("token", token);
            jsonObject.put("face_data_1", bind_face_base64);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
