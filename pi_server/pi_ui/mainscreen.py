

import os
from PyQt5 import QtGui
from PyQt5.QtWidgets import *
from PyQt5.QtCore import *
from PyQt5.QtGui import *
import time
import logging
import utils.qr as qr
import json
import paho.mqtt.client as mqtt
from utils.constants import const


class MQThread(QThread):
    to_where_signal = pyqtSignal(bytes)

    def __init__(self):
        super().__init__()

    def run(self):

        client.loop_forever()

class WaittingScreen(QWidget):

    def __init__(self,):
        super().__init__()
        self.setupUI()
        self.connect_mqtt()



    def connect_mqtt(self):
        global client
        client = mqtt.Client("pi_server_stack")
        client.on_connect = self.on_connect
        client.on_message = self.on_msg
        client.on_disconnect = self.on_disconnect
        client.connect(const.IP,1883,60)

        self.qtThread = MQThread()
        self.qtThread.to_where_signal.connect(self.to_where)
        self.qtThread.start()
        client.subscribe(const.LOCK_UUID)


    def to_where(self,msg):
        msg_temp = json.loads(msg.decode("utf-8"))
        if msg_temp["action"] == "show-qr":
            qr.base64_to_image(msg_temp["img_data"],"Qr")
            self.show_qr()
        elif msg_temp['action'] == "yes_unlock":
            qr.base64_to_image(msg_temp["user_img"],"face")
            self.show_unlock()

    # mqtt
    def on_disconnect(self,client, userdata, flags, rc):
        print("断开了连接")

    def on_connect(self, client, userdata, flags, rc):
        print("连接成功：" + rc)
        client.subscribe(const.LOCK_UUID)

    def on_msg(self, client, userdata, msg):
        self.qtThread.to_where_signal.emit(msg.payload)


    def showtime(self):
        datetime = QDateTime.currentDateTime()
        text = time.strftime("%H:%M:%S")
        self.label_1.setText(text)

    # set ui
    def setupUI(self,):
        # 先初始化两个定时器
        self.timer_unlock = QTimer(self)
        self.timer_qr = QTimer(self)

        # 设置窗体为无边框
        self.setWindowFlags(Qt.FramelessWindowHint)

        # 设置窗口的位置和大小
        self.setGeometry(100, 100, 480,320)
        self.setObjectName("WaittingScreen")

        # 设置标签显示个性化信息
        self.label_2 = QLabel(self)
        self.label_2.setFixedWidth(80)
        self.label_2.move(130, 160)
        self.label_2.setStyleSheet(
            "QLabel{background:#000000;}"
            "QLabel{color:#ffffff;font-size:10px;font-weight:bold;font-family:宋体;font-size:16px;}"
        )
        self.label_2.setText("eyes on ")

        # 按钮插入gif动图
        self.gif = QMovie("./pi_ui/picture/button.gif")
        self.label_3 = QLabel(self)
        self.label_3.setFixedWidth(33)
        self.label_3.setFixedHeight(33)
        self.label_3.setMovie(self.gif)
        self.label_3.move(1,1)
        self.gif.start()

        # 动态显示时间在label上
        # 设置一个标签显示当前系统时间
        self.label_1 = QLabel(self)
        self.label_1.setFixedWidth(80)
        self.label_1.move(130, 140)
        self.label_1.setStyleSheet(
            "QLabel{background:#000000;}"
            "QLabel{color:#ffffff;font-size:10px;font-weight:bold;font-family:宋体;font-size:16px;}"
        )

        timer = QTimer(self)
        timer.timeout.connect(self.showtime)
        timer.start()


        # 创建一个Buttun,点击进入扫码解锁界面
        self.btn = QPushButton(u'    ', self)
        self.btn.setFont(QtGui.QFont('Microsoft YaHei', 10,75))
        self.btn.setGeometry(328,216,30,30)
        self.btn.setStyleSheet(
            # "QPushButton{background-image: url(./picture/button.gif)}"
            "QPushButton{color:#03899C}"  # 按键前景色
            "QPushButton{background-color:#FF0000}"  # 按键背景色
            "QPushButton:hover{color:}"  # 光标移动到上面后的前景色
            "QPushButton{border-radius:12px}"  # 圆角半径
            "QPushButton:pressed{background-color:rgb(0x00,0xff,0x00,0x00);border: 2px;}"  # 按下时的样式
        )
        self.btn.resize(self.btn.sizeHint())
        self.btn.setShortcut('L')
        self.btn.clicked.connect(self.show_loading)

        # 设置背景图片
        self.main_background_pic = "./pi_ui/picture/backG.png"
        self.qr_background_pic = "./pi_ui//picture/qrbackG.png"
        self.loading_gif_pic = "./pi_ui/picture/loading.gif"
        self.qr_imgshow_pic = "./pi_ui/picture/Qr.png"
        self.unlock_bgimg_pic = "./pi_ui/picture/unlock.png"
        self.unlock_faceimg_pic = "./pi_ui/picture/face.png"
        self.unlock_cirimg_pic = "./pi_ui/picture/circle.png"
        # "min-width:106px; min-height:106px;max-width:106px; max-height:106px;"
        self.face_SheetStyle = "border-radius:58px;border:3px solid black;background-color:red"

        self.show()


    # set bg
    def paintEvent(self, event):
        # 设置背景图片，平铺到整个窗口，随着窗口改变而改变
        painter = QPainter(self)
        pixmap = QPixmap(self.main_background_pic)
        painter.drawPixmap(self.rect(), pixmap)


    def show_loading(self):
        # 正在加载动图
        client.subscribe(const.LOCK_UUID)
        client.publish("UnLock",json.dumps({"from":"pi","lock_uuid":const.LOCK_UUID}))
        self.gif_loading = QMovie(self.loading_gif_pic)
        self.label_loading = QLabel(self)
        self.setFixedSize(480, 320)
        self.label_loading.setMovie(self.gif_loading)
        self.gif_loading.start()
        self.label_loading.show()



    def show_qr(self):
        print("show_qr")
        self.label_loading.hide()
        self.gif_loading.stop()

        # qr-qr-qr-qr
        qr_imgshow = QPixmap(self.qr_imgshow_pic)
        qr_bg = QPixmap(self.qr_background_pic)
        self.label_qrbg = QLabel(self)
        self.label_qrbg.setFixedSize(480, 320)
        self.label_qrbg.setPixmap(qr_bg)
        self.label_4 = QLabel(self)
        self.label_4.setFixedSize(106, 106)
        self.label_4.move(253, 120)
        self.label_4.setPixmap(qr_imgshow)
        self.label_qrbg.show()
        self.label_4.show()
        self.timer_qr.start(5000)
        self.timer_qr.timeout.connect(self.timeout_show_main)

    def timeout_show_main(self):
        # print(self.timer_qr.isActive())
        print(self.timer_unlock.isActive())
        if self.timer_qr.isActive():
            self.timer_qr.stop()
            self.label_4.hide()
            self.label_qrbg.hide()
            print("完成")

        if self.timer_unlock.isActive():
            self.timer_unlock.stop()
            self.label_unlockbg.hide()
            self.label_circle.hide()
            self.label_face.hide()
            self.label_unlocktext.hide()


    def show_unlock(self):
        if self.timer_qr.isActive() :
            self.timer_qr.stop()
            self.label_4.hide()
            self.label_qrbg.hide()
        unlock_bg = QPixmap(self.unlock_bgimg_pic)
        unlock_face = QPixmap(self.unlock_faceimg_pic)
        unlock_circle = QPixmap(self.unlock_cirimg_pic)
        self.label_unlockbg = QLabel(self)
        self.label_face = QLabel(self)
        self.label_circle = QLabel(self)
        self.label_circle.setFixedSize(138,165)
        self.label_circle.move(86,40)
        self.label_circle.setPixmap(unlock_circle)
        self.label_unlockbg.setFixedSize(480,320)
        self.label_unlockbg.setPixmap(unlock_bg)
        self.label_face.setStyleSheet(self.face_SheetStyle)
        self.label_face.setFixedSize(130, 130)
        self.label_face.move(95, 60)
        self.label_face.setPixmap(unlock_face)
        self.label_face.setScaledContents(True)

        # 显示解锁信息，文本数据
        self.label_unlocktext = QLabel(self)
        self.label_unlocktext.setFixedSize(127, 200)
        self.label_unlocktext.move(274, 46)
        self.label_unlocktext.setText("解锁成功！\n相似度")
        self.label_unlocktext.setStyleSheet(
            "color:white;size:16px;"
        )

        self.label_unlockbg.show()
        self.label_circle.show()
        self.label_face.show()
        self.label_unlocktext.show()

        self.timer_unlock.start(10000)
        self.timer_unlock.timeout.connect(self.timeout_show_main)








