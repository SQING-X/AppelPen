# -*- coding:utf-8 -*-
# author: @sqingX

import sqlite3
from utils.constants import const
from utils import getoken
from utils.qr import base64_to_image
'''
user:
    userid varchar(20) PRIMARY KEY,
    username varchar(20) NOT NULL,
    passwd varchar(20) NOT NULL,
    token varchar(100) NOT NULL,
    face_data TEXT default NULL,
    lock_uuid varchar(100) default None,
    lastunlock timestamp,
    unlocktimes int DEFAULT 0,
    admin bool default False

'''
class Session(object):

    def __init__(self,userid="",passwd="",username="",lock_uuid = "",token = "",face_data = ""):
        self.userid = userid
        self.username = username
        self.passwd = passwd
        self.lock_uuid = lock_uuid
        self.token = token
        self.face_data = face_data

    def sign_up(self):
        '''
        协议：http
        客户端构造http请求，包含请求头和json数据：
        url http://114.116.49.97/sign_up
        header content-type application/json
        body json
        还没有加数据验证
        :param userid:
        :param username:
        :param passwd:
        :param token: 由服务端生成发送过来，并保存在客户端
        :return:
        '''
        # try:
        tokenmaker = getoken.Token(userid=self.userid, passwd=self.passwd, username=self.username)
        self.token = tokenmaker.get_token()
        conn = sqlite3.connect(const.DBNAME)
        cusor = conn.cursor()
        print(type(self.token))
        sql_check = "select * from user where userid = {}".format(self.userid)
        if cusor.execute(sql_check).fetchone():
            return {"status":False,"msg":"注册失败，此手机已经被注册","token":""}
        # 记得{} 要加单引号，不然会报错，sql要求的数据格式的问题
        else:
            sql = "INSERT INTO user(userid,username,passwd,token) VALUES ('{}','{}','{}','{}')".format(self.userid,self.username,self.passwd,self.token)
            cusor.execute(sql)
            conn.commit()
            conn.close()
        return {"status":True,"msg":"注册成功","token":self.token}
        # except:
        #     return {"status":False,"msg":"程序出错，请联系管理员","token":""}

    def sign_in(self):
        # 还没有加数据验证
        # 每重新登录一次会换一个token
        # try:
        conn = sqlite3.connect(const.DBNAME)
        cusor = conn.cursor()
        sql = "SELECT * FROM user WHERE token = '{}'".format(self.token)
        result = cusor.execute(sql).fetchone()
        print(result)
        if result == None:
            return {"status": False, "msg": "用户不存在，请重试"}

        if self.passwd == result[2]:
            username = result[1]
            print(username+":"+result[2])
            tokenmaker = getoken.Token(userid=self.userid, passwd=self.passwd, username=username)
            token = tokenmaker.get_token()
            cusor.execute("UPDATE user SET token = '{}' WHERE token = '{}'".format(token,self.token))
            conn.commit()
            conn.close()
            return {"status":True,"msg":"登陆成功","token":token}
        # except:
        #     return {"status": False, "msg": "未知错误，请重试！", "token": "null"}

        return {"status":False,"msg":"密码错误或不存在，请重试"}

    def bind_lock(self):

        conn = sqlite3.connect(const.DBNAME)
        cusor = conn.cursor()
        if self.token != "":
            # 判断设备是否已经被其他用户绑定
            sql_check_is_bind = "select * from user where lock_uuid = '{}'".format(self.lock_uuid)
            num_binded = len(cusor.execute(sql_check_is_bind).fetchall())
            if  num_binded >= 5 :
                return {"status": False, "msg": "绑定失败，此设备绑定人数最多五人，当前：{}人！".format(num_binded)}

            # 判断token是否是伪造，存在用户与之对应才可以
            sql = "select * from user where token = '{}'".format(self.token)
            userinfo = cusor.execute(sql).fetchone()
            if userinfo :
                self.userid = userinfo[0]
                get_uuid = userinfo[4]
                if get_uuid == None:

                    sql = "UPDATE user SET lock_uuid = '{}' WHERE userid = '{}'".format(self.lock_uuid, self.userid)
                    cusor.execute(sql)
                    conn.commit()
                    conn.close()
                    return {"status": True, "msg": "设备绑定成功", "user": self.userid}
                else:
                    sql = "UPDATE user SET lock_uuid = '{}' WHERE userid = '{}'".format(self.lock_uuid, self.userid)
                    cusor.execute(sql)
                    conn.commit()
                    conn.close()
                    return {"status": True, "msg": "设备更换成功", "user": self.userid}
            else:
                conn.close()
                return {"status": False, "msg": "用户不存在"}
    def bind_face(self):
        base64_to_image(self.face_data,self.token)
        conn = sqlite3.connect(const.DBNAME)
        cusor = conn.cursor()
        if self.token != "":
            sql = "select * from user where token = '{}'".format(self.token)

            userinfo = cusor.execute(sql).fetchone()
            if userinfo:
                self.userid = userinfo[0]
                sql = "UPDATE user SET face_data = '{}' WHERE userid = '{}'".format("../face_data/{}.png".format(self.token), self.userid)
                cusor.execute(sql)
                conn.commit()
                conn.close()
                return {"status": True, "msg": "人脸绑定成功", "user": self.userid}
            else:
                conn.close()
                return {"status": False, "msg": "人脸绑定失败", "user": self.userid}
        else:
            conn.close()
            return {"status": False, "msg": "用户不存在"}