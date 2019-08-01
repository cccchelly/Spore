package com.alex.witAg.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.camreaproxy.CameraManager;
import com.alex.witAg.utils.LocalPicCleanUtil;
import com.alex.witAg.utils.TaskTimeUtil;
import com.alex.witAg.utils.ToastUtils;


/**
 * Created by Administrator on 2018-03-28.
 */

public class CaptureService extends Service {
    public static String action = "capture_action";
    private static final String TAG = CaptureService.class.getName();
    Handler toastHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ToastUtils.showToast(msg.obj.toString());
        }
    };

    private boolean flagStop = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startPointTask();
    }

    private void startPointTask() {
        ToastUtils.showToast("定时拍照任务开始执行");
        flagStop = false;  //服务启动
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        long nowTime = System.currentTimeMillis();
                        if (flagStop) {    //检测到服务销毁，跳出循环
                            Log.i(TAG,"拍照旧循环停止");
                            break;
                        }
                        if (!App.getIsTaskRun()) {
                            if (TaskTimeUtil.getInstance().isHaveTimePoint(nowTime)) {
                                Log.i("--capture--", "存在该时间，开始任务");
                                startCaptureOnly();
                                Thread.sleep(60*1000);
                            } else {
                                Log.i("--capture--", "不存在该时间");
                                Thread.sleep(10000);
                            }
                        }else {
                            Log.i("--capture--","定时任务运行中");
                            Thread.sleep(10000);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //简易版直接到时间只拍照
    public void startCaptureOnly(){
        if (App.getIsTaskRun()){
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                App.setIsTaskRun(true);
                //执行任务前检查内存，如果有必要则清理内存
                LocalPicCleanUtil.doCleanIfNecessary();
                Log.i(TAG, App.getIsTaskRun() + " ----startCaptureTask");
                toastOnMain("定时任务开始执行");

                CameraManager cameraManager = CameraManager.getInstance();
                cameraManager.initCamera();
                cameraManager.connectCamera();
                cameraManager.isCameraConnect();
                cameraManager.captureExecute(AppContants.CaptureFrom.FROM_TASK);

                toastOnMain("拍照完成");
                App.setIsTaskRun(false);


            }
        }).start();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "--------->onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "--------->onDestroy: ");
        flagStop = true;
        super.onDestroy();
    }



    private void toastOnMain(String content) {
        Message msg = Message.obtain();
        msg.what = 0;
        msg.obj = content;
        toastHandle.sendMessage(msg);
    }

}
