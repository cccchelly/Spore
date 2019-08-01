package com.alex.witAg.utils;

import android.content.Intent;

import com.alex.witAg.App;
import com.alex.witAg.service.CaptureService;
import com.alex.witAg.service.LampTaskService;
import com.alex.witAg.service.MqttService;
import com.alex.witAg.service.PostMsgService;
import com.alex.witAg.service.PostPicService;

/**
 * Created by Administrator on 2018-04-12.
 */

public class TaskServiceUtil {

    public static void  startTasks(){
        Intent intent = new Intent(App.getAppContext(), CaptureService.class);   //开启服务定时拍照
        App.getAppContext().startService(intent);
        Intent postIntent = new Intent(App.getAppContext(), PostPicService.class);   //开启服务定时上传
        App.getAppContext().startService(postIntent);
        /*Intent updateIntent = new Intent(App.getAppContext(), UpdateService.class);  //开启更新查询
        App.getAppContext().startService(updateIntent);*/
        /*Intent locationIntent = new Intent(App.getAppContext(), LocationService.class);
        App.getAppContext().startService(locationIntent);*/
        Intent mqttIntent = new Intent(App.getAppContext(), MqttService.class);
        App.getAppContext().startService(mqttIntent);
        Intent postMsgIntent = new Intent(App.getAppContext(), PostMsgService.class);
        App.getAppContext().startService(postMsgIntent);
        Intent lampIntent = new Intent(App.getAppContext(), LampTaskService.class);   //开启服务控制灯管
        App.getAppContext().startService(lampIntent);
    }


    public static void stopTasks(){
        Intent intent = new Intent(App.getAppContext(), CaptureService.class);   //关闭服务定时拍照
        App.getAppContext().stopService(intent);
        Intent postIntent = new Intent(App.getAppContext(), PostPicService.class);   //关闭服务定时上传
        App.getAppContext().stopService(postIntent);
       /* Intent updateIntent = new Intent(App.getAppContext(), UpdateService.class);  //关闭更新查询
        App.getAppContext().stopService(updateIntent);*/
        /*Intent locationIntent = new Intent(App.getAppContext(), LocationService.class);
        App.getAppContext().stopService(locationIntent);*/
        Intent mqttIntent = new Intent(App.getAppContext(), MqttService.class);
        App.getAppContext().stopService(mqttIntent);
        Intent postMsgIntent = new Intent(App.getAppContext(), PostMsgService.class);
        App.getAppContext().stopService(postMsgIntent);
        Intent lampIntent = new Intent(App.getAppContext(), LampTaskService.class);   //关闭服务控制灯管
        App.getAppContext().stopService(lampIntent);
    }

    public static void  startPhotoTasks(){
        Intent intent = new Intent(App.getAppContext(), CaptureService.class);   //开启服务定时拍照
        App.getAppContext().startService(intent);
        Intent postIntent = new Intent(App.getAppContext(), PostPicService.class);   //开启服务定时上传
        App.getAppContext().startService(postIntent);
    }

    public static void stopPhotoTasks(){
        Intent intent = new Intent(App.getAppContext(), CaptureService.class);   //关闭服务定时拍照
        App.getAppContext().stopService(intent);
        Intent postIntent = new Intent(App.getAppContext(), PostPicService.class);   //关闭服务定时上传
        App.getAppContext().stopService(postIntent);
    }

    public static  void resetPhotoTasks(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stopPhotoTasks();
                    Thread.sleep(10*1000);
                    startPhotoTasks();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void  startLampTasks(){
        Intent intent = new Intent(App.getAppContext(), LampTaskService.class);   //开启服务控制灯管
        App.getAppContext().startService(intent);
    }

    public static void stopLampTasks(){
        Intent intent = new Intent(App.getAppContext(), LampTaskService.class);   //关闭服务控制灯管
        App.getAppContext().stopService(intent);
    }

    public static void resetLampTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stopLampTasks();
                    Thread.sleep(10*1000);
                    startLampTasks();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static  void resetTasks(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stopTasks();
                    Thread.sleep(10*1000);
                    startTasks();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
