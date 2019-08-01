package com.alex.witAg.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alex.witAg.AppContants;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.LampTimeUtil;
import com.alex.witAg.utils.ToastUtils;

public class LampTaskService extends Service {

    private static final String TAG = LampTaskService.class.getName();
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
        startLampTask();
    }

    private void startLampTask() {
        ToastUtils.showToast("定时控制灯管开关任务开始执行");
        flagStop = false;  //服务启动
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        long nowTime = System.currentTimeMillis();
                        if (flagStop) {    //检测到服务销毁，跳出循环
                            Log.i(TAG, "灯管控制旧循环停止");
                            break;
                        }

                        if (LampTimeUtil.getInstance().isHaveOpenTimePoint(nowTime)) {
                            Log.i("--lamp--", "存在打开时间，开始任务");
                            openLamp();
                            Thread.sleep(60 * 1000);
                        } else {
                            Log.i("--lamp--", "不存在打开时间");
                        }

                        if (LampTimeUtil.getInstance().isHaveCloseTimePoint(nowTime)) {
                            Log.i("--lamp--", "存在关闭时间，开始任务");
                            closeLamp();
                            Thread.sleep(60 * 1000);
                        } else {
                            Log.i("--lamp--", "不存在关闭时间");
                        }

                        Thread.sleep(10000);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void openLamp() {
        toastOnMain("发送开灯命令");
        TaskQueue.getInstance().add(new SeralTask(AppContants.commands.dengguan_kai));
    }

    public void closeLamp() {
        toastOnMain("发送关灯命令");
        TaskQueue.getInstance().add(new SeralTask(AppContants.commands.dengguan_guan));
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
