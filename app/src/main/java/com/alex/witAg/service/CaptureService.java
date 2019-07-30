package com.alex.witAg.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.camreaproxy.CameraManager;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.CaptureTaskUtil;
import com.alex.witAg.utils.KsjCaptureUtil;
import com.alex.witAg.utils.LocalPicCleanUtil;
import com.alex.witAg.utils.LogUtil;
import com.alex.witAg.utils.SerialInforStrUtil;
import com.alex.witAg.utils.ShareUtil;
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

    Handler mHandler = new Handler();
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

    public void startCaptureTask(){
        if (App.getIsTaskRun()){
            return;
        }
        Log.i("--capture--","capture_start");
       new Thread(new Runnable() {
           @Override
           public void run() {
               App.setIsTaskRun(true);
               //执行任务前检查内存，如果有必要则清理内存
               LocalPicCleanUtil.doCleanIfNecessary();

        Log.i(TAG, App.getIsTaskRun() + " ----startCaptureTask");
        toastOnMain("定时任务开始执行");

        if (!TextUtils.equals(ShareUtil.getDeviceError(),"1")) {
            //每隔一段时间循环执行run方法
            CaptureTaskUtil captureTaskUtil = CaptureTaskUtil.instance();
            CameraManager cameraManager = CameraManager.getInstance();
            TaskQueue taskQueue = TaskQueue.getInstance();

            Log.i(TAG, "==检查是否处于复位状态==");
            String statue = ShareUtil.getDeviceStatue();
            if (!TextUtils.equals(statue, SerialInforStrUtil.STA_CLOSE_RESET)) {  //设备不在复位状态则强制复位
                Log.i(TAG, "==执行复位指令==");
                toastOnMain("准备复位");
                taskQueue.add(new SeralTask(SerialInforStrUtil.getForceRestartStr()));
                isStatueChanged(SerialInforStrUtil.STA_CLOSE_RESET);    //持续检查是否到达复位状态，到达则继续执行
            }

            Log.i(TAG, "==打开相机并翻转到正面==");
            toastOnMain("准备打开相机并翻转到正面");
            captureTaskUtil.openCaptureTurnPositive();  //打开摄像头并翻转到正面
            cameraManager.isCameraOpen();   //持续查询是否收到相机已打开命令
            //sleepMills(60 * 1000);
            Log.i(TAG, "==持续登录摄像头==");
            toastOnMain("登录摄像头");
            cameraManager.initCamera();
            cameraManager.connectCamera();
            cameraManager.isCameraConnect();  //登录摄像头(若登录失败则重新继续登录,若账号密码错误则放弃登录)
            if (isStatueChanged(SerialInforStrUtil.STA_OPEN_POSITIVE)) { //1
                sleepMills(20* 1000);    //摄像头能正常登陆到可以拍照有一个延迟时间，这里给个对应的延迟保证拍照能成功
                Log.i(TAG, "==拍摄正面==");
                toastOnMain("拍摄正面照片");
                cameraManager.captureExecute(AppContants.CaptureFrom.FROM_TASK);
                sleepMills(5 * 1000);
            }
            Log.i(TAG, "==翻转到反面==");
            toastOnMain("翻转到反面");
            taskQueue.add(new SeralTask(SerialInforStrUtil.getDeclineStr())); //翻转到反面  2
            if (isStatueChanged(SerialInforStrUtil.STA_OPEN_OPPOSITE)) {
                Log.i(TAG, "==拍摄反面==");
                toastOnMain("拍摄反面照片");
                cameraManager.captureExecute(AppContants.CaptureFrom.FROM_TASK);
                sleepMills(5 * 1000);
            }
            Log.i(TAG, "==复位关机==");
            toastOnMain("复位并关机");
            taskQueue.add(new SeralTask(SerialInforStrUtil.getForceRestartStr()));  //复位关机
            sleepMills(2 * 60 * 1000);
        }else {
            toastOnMain("机器故障！忽略本次定时任务！");
        }
               App.setIsTaskRun(false);
               //toastOnMain("定时任务执行完毕");
           }
       }).start();
        Log.i(TAG, App.getIsTaskRun() + " ----completeCaptureTask");
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

    private boolean isStatueChanged(String nextStatus) {
        boolean isChange = false;
        String next = "";
        for (int i = 1; i < 5 * 60; i++) {    //查询状态是否改变  若状态未改变休眠一秒继续查询
            //Log.i(TAG, "sta=" + ShareUtil.getDeviceStatue() + ",s=" + nextStatus);
            if (!TextUtils.equals(nextStatus, ShareUtil.getDeviceStatue())) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                isChange = true;
            }
        }
        return isChange;
    }

    private void toastOnMain(String content) {
        Message msg = Message.obtain();
        msg.what = 0;
        msg.obj = content;
        toastHandle.sendMessage(msg);
    }

    void sleepMills(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
