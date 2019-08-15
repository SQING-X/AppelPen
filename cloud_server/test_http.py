# -*- coding:utf-8 -*- 
# author: @sqingX
# ctime: 2019/8/12 17:14
# name: test_http.py
import json
import socket# 客户端 发送一个数据，再接收一个数据
client = socket.socket(socket.AF_INET,socket.SOCK_STREAM) #声明socket类型，同时生成链接对象
client.connect(('114.116.49.97',8080))

msg = {"action":"sign_up","userid":"123456778","username":"sqing","passwd":"123123","hold":""}
client.send(json.dumps(msg).encode("utf-8"))
msg = {"action":"sign_up","userid":"123456778","username":"sqing","passwd":"123123","hold":""}
data = client.recv(1024)
print('recv:',data.decode())
client.send(json.dumps(msg).encode("utf-8"))
data = client.recv(1024)
print('recv:',data.decode())
