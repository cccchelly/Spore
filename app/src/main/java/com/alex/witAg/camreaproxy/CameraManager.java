package com.alex.witAg.camreaproxy;

import com.alex.witAg.utils.LocalPicCleanUtil;

/**
 * Created by apple on 2019/6/18.
 */

public class CameraManager implements ICamera {

    private static CameraManager cameraManager;
    private  static ICamera iCamera;

    private CameraManager(){
    }


    public static CameraManager getInstance(){
        if (cameraManager == null){
            synchronized (CameraManager.class){
                if (cameraManager == null){
                    cameraManager = new CameraManager();
                }
            }
        }
        return cameraManager;
    }

    public void setCamera(ICamera iCamera){
        if (iCamera !=null) {
            this.iCamera = iCamera;
        }
    }


    @Override
    public void initCamera() {
        iCamera.initCamera();
    }

    @Override
    public void connectCamera() {
        iCamera.connectCamera();
    }

    @Override
    public void captureExecute(String from) {
        //执行任务前检查内存，如果有必要则清理内存
        LocalPicCleanUtil.doCleanIfNecessary();
        iCamera.captureExecute(from);
    }


    @Override
    public void unInitCamera() {
        iCamera.unInitCamera();
    }

    @Override
    public boolean isCameraOpen() {
        return iCamera.isCameraOpen();
    }

    @Override
    public boolean isCameraConnect() {
        return iCamera.isCameraConnect();
    }

    @Override
    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        iCamera.setOnCaptureListener(onCaptureListener);
    }

}
