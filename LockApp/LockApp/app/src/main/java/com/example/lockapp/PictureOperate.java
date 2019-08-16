package com.example.lockapp;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PictureOperate {

    /*用URi得到真实路径*/
    public static String getRealFilePath(Context context, Uri uri ) {
        if ( uri == null )
            return null;
        String scheme = uri.getScheme();
        String path = null;
        if ( scheme == null )
            path = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {//以file://形式开头
            path = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( cursor != null ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        path = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return path;
    }

    /*方法一：通过路径对图片进行base64编码*/
    public static String imgToBase64(String path){
        String base64str = null;
        InputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(path);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //进行base64编码
        base64str = Base64.encodeToString(data, Base64.NO_WRAP);
        return base64str;
    }

    /*方法二：将bitmap转为base64*/
    public static String bitmapToBase64(Bitmap bitmap){
        String result = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            if (bitmap != null) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                //压缩图片
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                byte[] b = byteArrayOutputStream.toByteArray();
                result = Base64.encodeToString(b, Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /*压缩图片*/
    public Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) { //循环判断如果压缩后图片是否大于50kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 20;//每次都减少20
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}















