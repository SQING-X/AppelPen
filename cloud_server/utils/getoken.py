import hashlib
import time
# 创建获取时间戳的对象


class Time(object):
    def t_stamp(self):
        t = time.time()
        t_stamp = int(t)
        return t_stamp

# 创建获取token的对象


class Token(object):
    '''
    使用：
        tokenprogramer = Token('api_secret具体值', 'project_code具体值', 'account具体值')  # 对象实例化
        tokenprogramer.get_token()
    '''

    def __init__(self, userid, passwd, username):
        self.user_id = userid
        self.passwd = passwd
        self.username = username

    def get_token(self):
        strs = self.passwd + str(Time().t_stamp()) + \
            self.user_id + self.username
        hl = hashlib.md5()
        hl.update(strs.encode("utf8"))
        token = hl.hexdigest()
        return token
