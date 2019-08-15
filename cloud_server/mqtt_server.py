# -*- coding:utf-8 -*-
# author: @sqingX

import json
import paho.mqtt.client as mqtt
import ctypes
import requests
import sqlite3
import datetime
import threading
import time
import os
from utils import qr
from queue import Queue
from utils.constants import const
# 手机人脸解锁申请表
msg_list =Queue(maxsize=1000)


def connect_mos_broker(ip,port,timeout):
    global client
    client = mqtt.Client("cloud_server——222")
    client.connect(ip,port,timeout)
    client.on_connect = on_connect
    client.on_message = on_message
    client.loop_forever()

def on_connect(client,userdata,flags,rc):
    '''
    连接成功的回调程序
    userdata：Client（）或userdata_set（）中设置的私有用户数据
    rc的值表示成功与否：
            0：连接成功
            1：连接被拒绝 - 协议版本不正确
            2：连接被拒绝 - 客户端标识符无效
            3：连接被拒绝 - 服务器不可用
            4：连接被拒绝 - 用户名或密码错误
            5：连接被拒绝 - 未经授权
    '''
    print("连接成功，代码："+str(rc))           # 测试用 连接成功时订阅：断开连接会从新订阅
    client.subscribe("UnLock")
def on_message(client,userdata,msg):
    ''' 消息推送的回调程序 '''
    ## 不加client,userdata,接受不到消息，为啥嘞
    if msg.topic == "UnLock":
        json_datas = json.loads(msg.payload.decode("gbk"))
        msg_list.put(json_datas)
        #
        # # evt.set()
        # print("发送数据到另一个线程")

# {"from":"pi","lock_uuid":"afba5de8-94e9-11e9-8ccd-d510df66620f"}

# 线程创建器
def make_thread_for_msg(msg_list):

    global handle_msg
    i = 0
    while True:
        if msg_list:
            json_data = msg_list.get()
            if json_data["from"] == "pi":
                handle_msg = threading.Thread(target=make_qr,args=(json_data,))
            else:
                handle_msg = threading.Thread(target=face_unlock, args=(json_data,))
            handle_msg.start()
            print(i)
            i += 1

def face_unlock(json_data):
    '''
    unlock msg format from phone
    :param json_data:
    :return:
    '''
    lock_uuid = json_data["lock_uuid"]
    print("uuid"+lock_uuid)
    if json_data["action"] == "yes_unlock":
        # 扫描二维码以后收到的消息
        face1 = json_data["face_data_1"]
        qr_unlock_msg = {"action":"yes_unlock","user_img":face1,"simla":1}
        client.publish(lock_uuid, json.dumps(qr_unlock_msg).encode("utf-8"), 2)
    else:
        # 手机解锁收到的消息
        print('开始人脸比较')
        face1 = json_data["face_data_1"]
        face2 = json_data["face_data_2"]
        token = json_data["token"]
        isUnlock, simlarity = compareface(face1, face2)
        if isUnlock:
            print("解锁成功:" + str(simlarity))
            UNLOCKSUCCESS = {"action":"yes_unlock","user_img":face1,"simla":simlarity}
            client.publish(lock_uuid, json.dumps(UNLOCKSUCCESS).encode("utf-8"), 2)
            client.publish(token,json.dumps(UNLOCKSUCCESS).encode("utf-8"),2)
            log_lock_to_base(lock_uuid)
            print("执行发送")
        else:
            UNLOCKFAIL = {"action":"no_unlock","simla":simlarity}
            client.publish(token, json.dumps(UNLOCKFAIL).encode("gbk"), 2)
            print("解锁失败")

def make_qr(json_data):
    print("制作二维码")
    # 本地不做存储，只在前端做验证，符合条件给予放行
    lock_uuid = json_data["lock_uuid"]

    conn = sqlite3.connect(const.DBNAME)
    cusor = conn.cursor()
    sql = "select token from user where lock_uuid = '{}'".format(lock_uuid)
    temp = cusor.execute(sql).fetchall()
    conn.close()
    if len(temp) != 0:
        qr_token = temp
        # 生成base64编码的pillow对象，虽然现在这个功能有和没有一样
        qr_img = qr.get_qr_image(lock_uuid,qr_token) # token有很多，看是谁的手机在解锁
        client.publish(lock_uuid,
                       json.dumps({"action": "show-qr", "img_data": qr_img}).encode("gbk"),
                       2)
        # log_lock_to_base(lock_uuid)
    else:
        client.publish(lock_uuid,
                       json.dumps({"action": "show-qr", "img_data": None}).encode("gbk"),
                       2)


# 解锁信息写入数据库
def log_lock_to_base(lock_uuid):
    conn = sqlite3.connect(const.DBNAME)
    cusor = conn.cursor()
    sql = "select unlocktimes from user where lock_uuid = '{}'".format(lock_uuid)
    unlocktimes = cusor.execute(sql).fetchone()[0] + 1
    time_now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    sql = "update  user set lastunlock = '{}',unlocktimes = {} where lock_uuid =='{}'".format(time_now, unlocktimes,
                                                                                              lock_uuid)
    print(unlocktimes)
    cusor.execute(sql)
    conn.commit()
    conn.close()

def compareface (face1, face2):
    url = "https://face.cn-north-1.myhuaweicloud.com/v1/"+const.PROJ_ID+"/face-compare"  # 在怎么来？分析Ajax请求
    hd = {"Content-Type": 'application/json',
          "X-Auth-Token": const.X_AUTH_TOKEN}  # 模仿浏览器
    Request_Body = {
        "image1_base64": face1,
        "image2_base64": face2
    }
    response = requests.post(url=url, headers=hd, json=Request_Body).json()
    print(response)
    if response['similarity']>=0.9:
        return True,response['similarity']
    return False


if __name__ == "__main__":
    # evt = threading.Event()
    IP = const.IP
    PORT = const.PORT
    TIMEOUT = const.TIMEOUT
    R = threading.Thread(target=connect_mos_broker,args=(IP,PORT,TIMEOUT,))
    P = threading.Thread(target=make_thread_for_msg,args=(msg_list,))
    R.start()
    P.start()
    R.join()
    P.join()



