package com.alex.witAg.ui.activity;

import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.camreaproxy.MyUvcCameraProxy;
import com.alex.witAg.utils.LogUtil;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PictureCallback;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Route(path = AppContants.ARouterUrl.SporeShowActivity)
public class SporeShowActivity extends AppCompatActivity {
    MyUvcCameraProxy mUVCCamera;
    @BindView(R.id.spore_show_surface)
    SurfaceView mSurface;
    @BindView(R.id.spore_capture_test)
    Button mBtnCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spore_show);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        if (mUVCCamera == null) {
            mUVCCamera =  MyUvcCameraProxy.getInstance(this);
        }

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

                /*if (null!=onCaptureListener){
                    if (TextUtils.equals(from, AppContants.CaptureFrom.FROM_Hand)) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
                        if (bitmap!=null) {
                            onCaptureListener.finish(bitmap, file, path);
                        }
                    }
                }*/
            }
        });

        mUVCCamera.getConfig()
                .isDebug(true) // 是否调试
                .setPicturePath(PicturePath.SDCARD) // 图片保存路径，保存在app缓存还是sd卡
                .setDirName("") // 图片保存目录名称
                //.setProductId(0) // 产品id，用于过滤设备，不需要可不设置
                .setVendorId(0);

        mUVCCamera.setPreviewSurface(mSurface);

        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {
                LogUtil.i("====设备授权");
                mUVCCamera.requestPermission(usbDevice); // USB设备授权
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                if (granted) {
                    LogUtil.i("====连接设备");
                    mUVCCamera.connectDevice(usbDevice); // 连接USB设备
                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                LogUtil.i("====打开相机");
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

        /*mUVCCamera.checkDevice();
        mUVCCamera.registerReceiver();*/


    }


    @OnClick(R.id.spore_capture_test)
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.spore_capture_test:
                mUVCCamera.takePictureWithoutPreview("test.jpg");
                mUVCCamera.takePicture("test.jpg");

                break;
        }
    }
}
