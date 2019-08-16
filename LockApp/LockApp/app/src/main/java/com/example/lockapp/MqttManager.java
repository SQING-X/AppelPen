package com.example.lockapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class MqttManager {

    private String TAG = "show detail";

    private String host = "tcp://114.116.49.97:1883";
    private String clientId = "";

    private MqttManager mqttManager = null;
    private MqttClient client;
    private MqttConnectOptions connectOptions;

    private String payload = null;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    //生成clientId
    public MqttManager(Context context){
        clientId = MqttClient.generateClientId();
    }

    //实例化MqttManager
    public MqttManager getInstance(Context context){
        if (mqttManager == null){
            mqttManager = new MqttManager(context);
        }else {
            return mqttManager;
        }
        return null;
    }

    //建立MQTT连接
    public void connect(){
        Log.i(TAG, "准备建立Mqtt连接");
        try {
            client = new MqttClient(host, clientId, new MemoryPersistence());
            //MQTT的连接设置
            connectOptions = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            connectOptions.setCleanSession(true);
            // 设置超时时间 单位为秒
            connectOptions.setConnectionTimeout(30);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            connectOptions.setKeepAliveInterval(30);
            //设置回调
            client.setCallback(mqttCallback);
            client.connect(connectOptions);
            if (client.isConnected()){
                Log.i(TAG, "连接成功");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅消息
     * @param topic     订阅的主题
     * @param qos       服务质量，0：至多一次    1：至少一次   2：只有一次
     */
    public void subscribe(String topic, int qos){
        if (client != null){
            int[] Qos = {qos};
            String[] topoc_1 = {topic};
            try {
                client.subscribe(topoc_1, Qos);
                Log.i(TAG, "订阅的主题：" + topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发布消息
     * @param topic         发布消息的主题
     * @param msg           消息体
     * @param isRetained    是否为保留信息
     * @param qos
     */
    public void publish(String topic, JSONObject msg, boolean isRetained, int qos){
        try {
            if (client != null) {
                MqttMessage message = new MqttMessage();
                message.setQos(qos);
                message.setRetained(isRetained);
                message.setPayload(msg.toString().getBytes());
                client.publish(topic, message);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /*发布和订阅消息的回调*/
    int count = 0;
    protected MqttCallback mqttCallback = new MqttCallback() {
        /*失去连接，重新连接*/
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "connectionLost:" + cause);
            if (count < 5){
                count++;
                Log.i(TAG, "断开连接，重新连接" + count + "次" + cause);
                try {
                    client.close();
                    connect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }

        /*接收消息的回调方法*/
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            //subscribe 后得到的消息会执行到这里
            payload = new String(message.getPayload());
            Log.i(TAG + " 接收到的主题为" + topic + "的消息", payload);
        }

        /*发布消息的回调方法*/
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //publish后会执行到这里
            Log.i(TAG, "发布消息成功后的回调:" + token.isComplete());
        }
    };


    public void disconnect(){
        if (client.isConnected()){
            try {
                Log.i(TAG, "即将关闭Mqtt连接");
                client.disconnect();
                if (!client.isConnected()){
                    Log.i(TAG, "关闭成功");
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public  boolean isMqttConnect(){
        return client.isConnected();
    }
}












