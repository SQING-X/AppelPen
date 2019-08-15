# -*- coding:utf-8 -*-
# author: @sqingX

import socket
import threading
import time
# import simplejson
from utils.session import Session
import json

# 接受客户端登陆或注册数据形式json:
# {"action":"sign_in/sign_up/bind_lock/bind_face","userid":"","username":"","passwd":"","hold":"null/exit","lock_uuid":"","face_data":"","token":""}
def tcplink(sock, addr):
    print('Accept new connection from %s:%s...' % addr)
    sock.send(json.dumps({"msg":"welcome"}).encode("utf-8"))
    while True:
        # 一次最大接受字符个数，如果大于这个个数需要分多次传输合并成一个消息在进行处理，否则造成消息接受不完整
        data_raw = sock.recv(102400)
        if data_raw:
            print(data_raw)
            data = json.loads(data_raw.decode("utf-8"))
            print(data)
            if data["hold"] == 'exit':
                break
            elif data["action"] == 'sign_up':
                payloads = Session(userid=data["userid"],passwd=data["passwd"],username=data["username"]).sign_up()
                sock.send(json.dumps(payloads).encode("utf-8"))
            elif data["action"] == 'sign_in':
                print("登录")
                payloads = Session(userid=data["userid"],passwd=data["passwd"],token=data["token"]).sign_in()
                sock.send(json.dumps(payloads).encode("utf-8"))
            elif data["action"] == 'bind_lock':
                payloads = Session(token=data["token"],lock_uuid=data["lock_uuid"]).bind_lock()
                sock.send(json.dumps(payloads).encode("utf-8"))
            elif data["action"] == 'bind_face':
                print("绑脸")
                payloads = Session(token=data["token"],face_data = data["face_data"]).bind_face()
                sock.send(json.dumps(payloads).encode("utf-8"))

        else:
            pass

    sock.close()
    print('Connection from %s:%s closed.' % addr)


s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(('', 9999))
s.listen(5) # 可同时监听的客户端数量
print('Waiting for connection...')
while True:
    # 接受一个新连接:
    sock, addr = s.accept()
    # 创建新线程来处理TCP连接:
    t = threading.Thread(target=tcplink, args=(sock, addr))
    t.start()
    t.join()


