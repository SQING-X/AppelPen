##存储常量

class Const(object):
    class ConstError(TypeError):
        pass

    class ConstCaseError(ConstError):
        pass

    def __setattr__(self, name, value):
        if name in self.__dict__: # 判断是否已经被赋值，如果是则报错
            raise self.ConstError("Can't change const.%s" % name)
        if not name.isupper(): # 判断所赋值是否是全部大写，用来做第一次赋值的格式判断，也可以根据需要改成其他判断条件
            raise self.ConstCaseError('const name "%s" is not all supercase' % name)

        self.__dict__[name] = value

const = Const()



const.IP = "114.116.49.97"
const.PORT = 1883
const.TIMEOUT = 60

# 此区域待删除，功能上新后返回信息不固定
const.UNLOCKSUCCESS = {"action":"UnLock","img_data":"","UnLock":True}
const.UNLOCKFAIL = {"action":"UnLock","img_data":"","UnLock":False}

# 树莓派识别码，实验用，上线删除
const.LOCK_UUID = "d0131804-6b84-4425-9f87-3c0347f38a82"