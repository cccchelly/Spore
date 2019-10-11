package com.alex.witAg.camreaproxy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.text.TextUtils;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.utils.LogUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TimeUtils;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PictureCallback;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SporeCamera implements ICamera {

    MyUvcCameraProxy mUVCCamera;
    OnCaptureListener onCaptureListener;

    @Override
    public void initCamera() {
        if (mUVCCamera == null){
            mUVCCamera =  MyUvcCameraProxy.getInstance(App.getAppContext());
        }

        mUVCCamera.stopPreview();
        mUVCCamera.closeCamera();

        //相机参数设置
        mUVCCamera.getConfig()
                .isDebug(true) // 是否调试
                .setPicturePath(PicturePath.SDCARD) // 图片保存路径，保存在app缓存还是sd卡
                .setDirName("") // 图片保存目录名称
                //.setProductId(0) // 产品id，用于过滤设备，不需要可不设置
                .setVendorId(0);

        //相机连接监听
        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {
                mUVCCamera.requestPermission(usbDevice); // USB设备授权
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                if (granted) {
                    mUVCCamera.connectDevice(usbDevice); // 连接USB设备
                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                mUVCCamera.openCamera(); // 打开相机
            }

            @Override
            public void onCameraOpened() {
                mUVCCamera.setPreviewSize(640, 480); // 设置预览尺寸
                mUVCCamera.startPreview(); // 开始预览
            }

            @Override
            public void onDetached(UsbDevice usbDevice) {
                mUVCCamera.closeCamera(); // 关闭相机
            }
        });

        mUVCCamera.checkDevice();
        mUVCCamera.registerReceiver();

        //拍照成功回调
        mUVCCamera.setPictureTakenCallback(new PictureCallback() {
            @Override
            public void onPictureTaken(String path) {
                LogUtil.i("====图片地址"+ path);
                File file = new File(path);
                Uri uri = Uri.fromFile(file);
                //文件保存到内存   截图就自动保存了 不用自己保存
                // FileUtils.saveContentToSdcard(picName,sbuffer);
                //文件地址保存到数据库
                PicPathsBean data = new PicPathsBean();
                data.setPath(file.getName());
                data.save();

                if (null!=onCaptureListener){
                    if (TextUtils.equals(from, AppContants.CaptureFrom.FROM_Hand)) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
                        if (bitmap!=null) {
                            onCaptureListener.finish(bitmap, file, path);
                        }
                    }
                }
                // Log.i(TAG, "NET_DVR_CaptureJPEGPicture_NEW size!" + bytesRerned.iValue);
                LogUtil.i("==截图成功==");
                mUVCCamera.closeCamera();
            }
        });

    }

    @Override
    public void connectCamera() {

    }

    @Override
    public void captureExecute(String from) {
        capture(from);
    }

    @Override
    public void unInitCamera() {
        mUVCCamera.closeCamera();
    }

    @Override
    public boolean isCameraOpen() {
        return true;
    }

    @Override
    public boolean isCameraConnect() {
        return true;
    }

    @Override
    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        this.onCaptureListener = onCaptureListener;
    }

    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    long timestamp;
    String time;
    public static  String from = "fromtask";

    public void capture(String from){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SporeCamera.this.from = from;

                timestamp = System.currentTimeMillis();
                time = formatter.format(new Date());

                String deviceStatue = "";
                if (TextUtils.equals(ShareUtil.getDeviceStatue(),"1")){
                    deviceStatue = "-A";
                }else if (TextUtils.equals(ShareUtil.getDeviceStatue(),"2")){
                    deviceStatue = "-B";
                }else {
                    deviceStatue = "-O";
                }
                String picName = TimeUtils.millis2String(System.currentTimeMillis(),
                        new SimpleDateFormat("yyyyMMddHHmmss")) + deviceStatue + ".jpg";

                if (mUVCCamera.isCameraOpen()){
                    LogUtil.i("==相机已连接，开始拍照");
                    mUVCCamera.takePictureWithoutPreview(picName);
                }else {
                    LogUtil.i("==相机未连接");
                }

            }
        }).start();
    }

}
