# # -*- coding:utf-8 -*-
# # author: @sqingX
# # ctime: 2019/8/14 0:54
# # name: test-mqtt.py
# import paho.mqtt.client as mqtt
# from utils.constants import const
# import threading
# def connect_mos_broker(ip,port,timeout,i):
#     global client
#     client = mqtt.Client("cloud_server_{}".format(i))
#     client.connect(ip
#     ,port,timeout)
#     client.on_connect = on_connect
#     client.on_message = on_message
#     client.loop_forever()
#
#
# def on_connect(client,userdata,flags,rc):
#     print(threading.current_thread().name+"连接成功，代码："+str(rc))           # 测试用 连接成功时订阅：断开连接会从新订阅
#
# def on_message(client,userdata,msg):
#     pass
#
# IP = const.IP
# PORT = const.PORT
# TIMEOUT = const.TIMEOUT
# for i in range(10):
#     R = threading.Thread(target=connect_mos_broker,args=(IP,PORT,TIMEOUT,i))
#     R.start()

