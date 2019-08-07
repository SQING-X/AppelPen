import qrcode
import base64
from PIL import Image
from io import BytesIO
import re

def get_qr_image(qr_lock_uuid,qr_token):
    # 生成的图片大小为106 X 106
    qr_data_added = {"lock_uuid":qr_lock_uuid,"token":qr_token}
    qr = qrcode.QRCode(
      version=7,
      error_correction=qrcode.constants.ERROR_CORRECT_L,
      box_size=2.5,
      border=4
    )
    qr.add_data(qr_data_added)
    qr.make(fit=True)
    img = qr.make_image()
    img_base64 = img_to_base64(img)
    return img_base64

def img_to_base64(img):
    output_buffer = BytesIO()
    img.save(output_buffer, format='png')
    byte_data = output_buffer.getvalue()
    base64_str = base64.b64encode(byte_data)
    return base64_str


# 这个需要写在树莓派的扩展包里面，待移植
def base64_to_image(base64_data,name):
    byte_data = base64.b64decode(base64_data)
    image_data = BytesIO(byte_data)
    img = Image.open(image_data)
    img.resize((106, 106), Image.ANTIALIAS)
    img.save("./pi_ui/picture/{}.png".format(name))
    print("二维码收到并保存完成")
