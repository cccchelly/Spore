package com.alex.witAg.camreaproxy;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.lgh.uvccamera.IUVCCamera;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PhotographCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.lgh.uvccamera.config.CameraConfig;
import com.lgh.uvccamera.usb.UsbMonitor;
import com.lgh.uvccamera.utils.FileUtil;
import com.lgh.uvccamera.utils.LogUtil;
import com.lgh.uvccamera.utils.RxUtil;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/*重新定义了Camera类
添加了不预览直接拍照的方法。
将该类设置为单例，保证唯一并且防止预览或者拍照后没有正常解注册造成的连接错误。如果单例对象不为空则先解注册，再重新赋值变量。*/
public class MyUvcCameraProxy implements IUVCCamera {
    private static int PICTURE_WIDTH = 640;
    private static int PICTURE_HEIGHT = 480;
    private static Context mContext;
    private static UsbMonitor mUsbMonitor;
    protected static UVCCamera mUVCCamera;
    private View mPreviewView;
    private Surface mSurface;
    private PictureCallback mPictureCallback;
    private PhotographCallback mPhotographCallback;
    private PreviewCallback mPreviewCallback;
    private ConnectCallback mConnectCallback;
    protected static CompositeSubscription mSubscriptions;
    private static CameraConfig mConfig;
    protected float mPreviewRotation;
    protected boolean isTakePhoto;
    private String mPictureName;

    private  static volatile MyUvcCameraProxy myUvcCameraProxy;

    public MyUvcCameraProxy() {
    }

    private MyUvcCameraProxy(Context context) {
        this.mContext = context;
        this.mConfig = new CameraConfig();
        this.mUsbMonitor = new UsbMonitor(context, this.mConfig);
        this.mSubscriptions = new CompositeSubscription();
    }

    public static MyUvcCameraProxy getInstance(Context context){
        if (myUvcCameraProxy == null){
            synchronized (MyUvcCameraProxy.class){
                if (myUvcCameraProxy == null){
                    myUvcCameraProxy = new MyUvcCameraProxy(context);
                }
            }
        }else {
            closeAll();

            mContext = context;
            mConfig = new CameraConfig();
            mUsbMonitor = new UsbMonitor(context, mConfig);
            mSubscriptions = new CompositeSubscription();
        }

        return myUvcCameraProxy;
    }


    public void registerReceiver() {
        this.mUsbMonitor.registerReceiver();
    }

    public void unregisterReceiver() {
        this.mUsbMonitor.unregisterReceiver();
    }

    public void checkDevice() {
        this.mUsbMonitor.checkDevice();
    }

    public void requestPermission(UsbDevice usbDevice) {
        this.mUsbMonitor.requestPermission(usbDevice);
    }

    public void connectDevice(UsbDevice usbDevice) {
        this.mUsbMonitor.connectDevice(usbDevice);
    }

    public void closeDevice() {
        this.mUsbMonitor.closeDevice();
    }

    public void openCamera() {
        try {
            this.mUVCCamera = new UVCCamera();
            this.mUVCCamera.open(this.mUsbMonitor.getUsbController());
            LogUtil.i("openCamera");
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        if (this.mUVCCamera != null && this.mConnectCallback != null) {
            this.mConnectCallback.onCameraOpened();
        }

    }

    public  void closeCamera() {
        try {
            if ( mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }

            mUsbMonitor.closeDevice();
            LogUtil.i("closeCamera");
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        mSubscriptions.clear();
    }

    private static void closeAll() {
        try {
            if ( mUVCCamera != null) {
                mUVCCamera.stopPreview();
                mUVCCamera.destroy();
                mUVCCamera = null;
            }

            mUsbMonitor.closeDevice();
            LogUtil.i("closeCamera");
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        mSubscriptions.clear();
    }



    public void setPreviewSurface(SurfaceView surfaceView) {
        this.mPreviewView = surfaceView;
        if (surfaceView != null && surfaceView.getHolder() != null) {
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                public void surfaceCreated(SurfaceHolder holder) {
                    LogUtil.i("surfaceCreated");
                    MyUvcCameraProxy.this.mSurface = holder.getSurface();
                    MyUvcCameraProxy.this.checkDevice();
                    MyUvcCameraProxy.this.registerReceiver();
                }

                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    LogUtil.i("surfaceChanged");
                }

                public void surfaceDestroyed(SurfaceHolder holder) {
                    LogUtil.i("surfaceDestroyed");
                    MyUvcCameraProxy.this.mSurface = null;
                    MyUvcCameraProxy.this.unregisterReceiver();
                    MyUvcCameraProxy.this.closeCamera();
                }
            });
        }

    }

    public void setPreviewTexture(TextureView textureView) {
        this.mPreviewView = textureView;
        if (textureView != null) {
            if (this.mPreviewRotation != 0.0F) {
                textureView.setRotation(this.mPreviewRotation);
            }

            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    LogUtil.i("onSurfaceTextureAvailable");
                    MyUvcCameraProxy.this.mSurface = new Surface(surface);
                    MyUvcCameraProxy.this.checkDevice();
                    MyUvcCameraProxy.this.registerReceiver();
                }

                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    LogUtil.i("onSurfaceTextureSizeChanged");
                }

                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    LogUtil.i("onSurfaceTextureDestroyed");
                    MyUvcCameraProxy.this.mSurface = null;
                    MyUvcCameraProxy.this.unregisterReceiver();
                    MyUvcCameraProxy.this.closeCamera();
                    return false;
                }

                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
        }

    }

    public void setPreviewRotation(float rotation) {
        if (this.mPreviewView != null && this.mPreviewView instanceof TextureView) {
            this.mPreviewRotation = rotation;
            this.mPreviewView.setRotation(rotation);
        }

    }

    public void setPreviewDisplay(Surface surface) {
        this.mSurface = surface;

        try {
            if (this.mUVCCamera != null && this.mSurface != null) {
                this.mUVCCamera.setPreviewDisplay(this.mSurface);
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public void setPreviewSize(int width, int height) {
        try {
            if (this.mUVCCamera != null) {
                PICTURE_WIDTH = width;
                PICTURE_HEIGHT = height;
                this.mUVCCamera.setPreviewSize(width, height);
                LogUtil.i("setPreviewSize-->" + width + " * " + height);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public Size getPreviewSize() {
        try {
            if (this.mUVCCamera != null) {
                return this.mUVCCamera.getPreviewSize();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        return null;
    }

    public List<Size> getSupportedPreviewSizes() {
        try {
            if (this.mUVCCamera != null) {
                return this.mUVCCamera.getSupportedSizeList();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        return new ArrayList();
    }

    public void startPreview() {
        try {
            if (this.mUVCCamera != null) {
                LogUtil.i("startPreview");
                this.mSubscriptions.add(Observable.create(new Observable.OnSubscribe<Integer>() {
                    public void call(final Subscriber<? super Integer> subscriber) {
                        MyUvcCameraProxy.this.mUVCCamera.setButtonCallback(new IButtonCallback() {
                            public void onButton(int button, int state) {
                                LogUtil.i("button-->" + button + " state-->" + state);
                                if (button == 1 && state == 0) {
                                    subscriber.onNext(state);
                                }

                            }
                        });
                    }
                }).compose(RxUtil.io_main()).subscribe(new Action1<Integer>() {
                    public void call(Integer integer) {
                        if (MyUvcCameraProxy.this.mPhotographCallback != null) {
                            MyUvcCameraProxy.this.mPhotographCallback.onPhotographClick();
                        }

                    }
                }));
                this.mUVCCamera.setFrameCallback(new IFrameCallback() {
                    public void onFrame(ByteBuffer frame) {
                        int lenght = frame.capacity();
                        byte[] yuv = new byte[lenght];
                        frame.get(yuv);
                        if (MyUvcCameraProxy.this.mPreviewCallback != null) {
                            MyUvcCameraProxy.this.mPreviewCallback.onPreviewFrame(yuv);
                        }

                        if (MyUvcCameraProxy.this.isTakePhoto) {
                            LogUtil.i("take picture");
                            MyUvcCameraProxy.this.isTakePhoto = false;
                            MyUvcCameraProxy.this.savePicture(yuv, MyUvcCameraProxy.PICTURE_WIDTH, MyUvcCameraProxy.PICTURE_HEIGHT, MyUvcCameraProxy.this.mPreviewRotation);
                        }

                    }
                }, 4);
                if (this.mSurface != null) {
                    this.mUVCCamera.setPreviewDisplay(this.mSurface);
                }

                this.mUVCCamera.updateCameraParams();
                this.mUVCCamera.startPreview();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public void savePicture(final byte[] yuv, final int width, final int height, final float rotation) {
        if (this.mPictureCallback != null) {
            LogUtil.i("savePicture");
            this.mSubscriptions.add(Observable.create(new Observable.OnSubscribe<String>() {
                public void call(Subscriber<? super String> subscriber) {
                    File file = MyUvcCameraProxy.this.getPictureFile(MyUvcCameraProxy.this.mPictureName);
                    String path = FileUtil.saveYuv2Jpeg(file, yuv, width, height, rotation);
                    subscriber.onNext(path);
                }
            }).compose(RxUtil.io_main()).subscribe(new Action1<String>() {
                public void call(String path) {
                    if (MyUvcCameraProxy.this.mPictureCallback != null) {
                        MyUvcCameraProxy.this.mPictureCallback.onPictureTaken(path);
                    }

                }
            }, new Action1<Throwable>() {
                public void call(Throwable throwable) {
                    if (MyUvcCameraProxy.this.mPictureCallback != null) {
                        MyUvcCameraProxy.this.mPictureCallback.onPictureTaken((String)null);
                    }

                }
            }));
        }
    }

    public void stopPreview() {
        try {
            if (this.mUVCCamera != null) {
                LogUtil.i("stopPreview");
                this.mUVCCamera.setButtonCallback((IButtonCallback)null);
                this.mUVCCamera.setFrameCallback((IFrameCallback)null, 0);
                this.mUVCCamera.stopPreview();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    //不预览  直接后台服务拍照   实际是给一个空surface预览
    public void takePictureWithoutPreview(String pictureName){
        this.isTakePhoto = true;
        this.mPictureName = pictureName;

        this.mUVCCamera.setFrameCallback(new IFrameCallback() {
            public void onFrame(ByteBuffer frame) {
                int lenght = frame.capacity();
                byte[] yuv = new byte[lenght];
                frame.get(yuv);
                if (MyUvcCameraProxy.this.mPreviewCallback != null) {
                    MyUvcCameraProxy.this.mPreviewCallback.onPreviewFrame(yuv);
                }

                if (MyUvcCameraProxy.this.isTakePhoto) {
                    LogUtil.i("take picture");
                    MyUvcCameraProxy.this.isTakePhoto = false;
                    MyUvcCameraProxy.this.savePicture(yuv, MyUvcCameraProxy.PICTURE_WIDTH, MyUvcCameraProxy.PICTURE_HEIGHT, MyUvcCameraProxy.this.mPreviewRotation);
                    stopPreview();
                }

            }
        }, 4);

        setPreviewDisplay(new Surface(new SurfaceTexture(0)));

        this.mUVCCamera.updateCameraParams();
        this.mUVCCamera.startPreview();

    }

    public void takePicture() {
        this.isTakePhoto = true;
        this.mPictureName = UUID.randomUUID().toString() + ".jpg";
    }

    public void takePicture(String pictureName) {
        this.isTakePhoto = true;
        this.mPictureName = pictureName;
    }

    public void setConnectCallback(ConnectCallback callback) {
        this.mConnectCallback = callback;
        this.mUsbMonitor.setConnectCallback(callback);
    }

    public void setPreviewCallback(PreviewCallback callback) {
        this.mPreviewCallback = callback;
    }

    public void setPhotographCallback(PhotographCallback callback) {
        this.mPhotographCallback = callback;
    }

    public void setPictureTakenCallback(PictureCallback callback) {
        this.mPictureCallback = callback;
    }

    public UVCCamera getUVCCamera() {
        return this.mUVCCamera;
    }

    public boolean isCameraOpen() {
        return this.mUVCCamera != null;
    }

    public CameraConfig getConfig() {
        return this.mConfig;
    }

    public void clearCache() {
        try {
            File cacheDir = new File(FileUtil.getDiskCacheDir(this.mContext, this.mConfig.getDirName()));
            FileUtil.deleteFile(cacheDir);
            File sdcardDir = FileUtil.getSDCardDir(this.mConfig.getDirName());
            FileUtil.deleteFile(sdcardDir);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    protected File getPictureFile(String pictureName) {
        File file = null;
        switch(this.mConfig.getPicturePath()) {
            case APPCACHE:
            default:
                file = FileUtil.getCacheFile(this.mContext, this.mConfig.getDirName(), pictureName);
                break;
            case SDCARD:
                file = FileUtil.getSDCardFile(this.mConfig.getDirName(), pictureName);
        }

        return file;
    }
}
