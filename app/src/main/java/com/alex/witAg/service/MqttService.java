package com.alex.witAg.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.base.BaseObserver;
import com.alex.witAg.base.BaseResponse;
import com.alex.witAg.bean.MqttMsgBean;
import com.alex.witAg.bean.PhotoSetResponseBean;
import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.bean.TaskTimeBean;
import com.alex.witAg.camreaproxy.CameraManager;
import com.alex.witAg.http.AppDataManager;
import com.alex.witAg.http.network.Net;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskExecutor;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.AppMsgUtil;
import com.alex.witAg.utils.AppUpdateUtil;
import com.alex.witAg.utils.CapturePostUtil;
import com.alex.witAg.utils.CaptureTaskUtil;
import com.alex.witAg.utils.FileUtils;
import com.alex.witAg.utils.LogUtil;
import com.alex.witAg.utils.SerialInforStrUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TaskServiceUtil;
import com.alex.witAg.utils.TaskTimeUtil;
import com.alex.witAg.utils.TimeUtils;
import com.alex.witAg.utils.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018-04-27.
 */

public class MqttService extends Service {

    public static final String TAG = MqttService.class.getSimpleName();

    private TaskQueue taskQueue;

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;

    //mosquitto_sub -t HelloWord -h 59.110.240.44

   // private String host = "tcp://59.110.240.44:1883";
    private String host = AppContants.MQTT_BASE_URL;
    private String userName = "admin";
    private String passWord = "password";
    private static String myTopic = "Device/DFS/cid" + AppMsgUtil.getIMEI(App.getAppContext());
    private String clientId = "test";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        taskQueue = TaskQueue.getInstance();
        Log.i(TAG, "mqttService---Start");
        init();
    }

    public static void publish(String msg) {
        String topic = myTopic;
        Integer qos = 2;
        Boolean retained = false;
        try {
            client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        // 服务器地址（协议+地址+端口号）
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());
        //断开后，是否自动连接
        conOpt.setAutomaticReconnect(true);

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        String topic = myTopic;
        Integer qos = 2;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            // 最后的遗嘱
            try {
                conOpt.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }

        if (doConnect) {
            doClientConnection();
        }

    }

    @Override
    public void onDestroy() {
        try {
            client.disconnect();
            client.unregisterResources();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNomarl()) {
            try {
                client.connect(conOpt, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                // 订阅myTopic话题
                client.subscribe(myTopic, 2);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            // 连接失败，重连
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            String str1 = new String(message.getPayload());
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG, "id:"+message.getId()+",messageArrived:" + str1);
            Log.i(TAG, str2);
            dealMsg(str1);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连

        }
    };

    //处理收到的消息
    private void dealMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        try {
            MqttMsgBean msgBean = new Gson().fromJson(msg, MqttMsgBean.class);
            String oprate = msgBean.getO();
            if (!TextUtils.isEmpty(oprate)) {
                switch (oprate) {   //判断操作类型
                    case "op_camera":  //相机
                        dealCmdStr(msgBean.getD().getCmd());
                        break;
                    case "op_board": // 粘虫板
                        dealCmdStr(msgBean.getD().getCmd());
                        break;
                    case "app_task":  //定时任务
                        dealCmdStr(msgBean.getD().getCmd());
                        break;

                    case "task_setting"://设置定时任务开始时间和间隔
                        setTask(msgBean.getD().getStart_time(),msgBean.getD().getEnd_time(), msgBean.getD().getDelay());
                        break;
                    case "post_time":
                        setPostTime(msgBean.getD().getDelay());
                        break;
                    case "picture":
                        dealPicture(msgBean.getD().getCmd());
                        break;
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

    }

    private void setPostTime(int delay) {
        ShareUtil.setPostTaskTime(delay*60*1000);
    }

    private void dealPicture(String cmd){
        List<PicPathsBean> picPaths = DataSupport.findAll(PicPathsBean.class);
        if (picPaths.size()<=0)
            return;
        List<String>  deletePaths = new ArrayList<>();
        try {

            if (TextUtils.equals(cmd,"all")){
                for (PicPathsBean pathsBean : picPaths){
                    deletePaths.add(pathsBean.getPath());
                }
            }else {
                if (TextUtils.isEmpty(cmd))
                    return;
                String[] split = cmd.split(",");
                for (String s :split){
                    deletePaths.add(s);
                }
            }

            for (String path :deletePaths){
                try{
                    File file = FileUtils.getFileFromSdcard(path);
                    //删除文件
                    FileUtils.deleteFile(file);
                    //数据库删除文件名
                    DataSupport.deleteAll(PicPathsBean.class,"path = ?",path);
                }catch (NullPointerException e){
                    //未找到图片（如人为删除了图片），从数据库清除图片地址
                    //数据库删除文件名
                    DataSupport.deleteAll(PicPathsBean.class,"path = ?",path);
                }
            }

        }catch (Exception e){
            ToastUtils.showToast("删除图片出错");
        }
    }

    /*根据需要发送的消息类型选择对应的消息给串口*/
    private void dealCmdStr(String cmd) {
        CaptureTaskUtil captureTaskUtil = CaptureTaskUtil.instance();
        switch (cmd) {
            case "task_rest"://重启定时任务
                TaskServiceUtil.resetPhotoTasks();
                break;
            case "app_reset"://重启app
                AppUpdateUtil.restartApp();
                break;
            case "post_pic": //上传本地图片
                new Thread(() -> CapturePostUtil.findLocalPic()).start();
                break;


            case "capture":   //执行相机拍照
                capture();
                break;
            case "positive": //打开摄像机并翻转到正面
                taskQueue.add(new SeralTask(SerialInforStrUtil.openCamTurnPositive()));
                break;
            case "opposite": //反面
                taskQueue.add(new SeralTask(SerialInforStrUtil.getDeclineStr()));
                break;
            case "reset":   //强制复位
                taskQueue.add(new SeralTask(SerialInforStrUtil.getForceRestartStr()));
                break;
            case "high1":   //调节到高度1
                captureTaskUtil.setHighAfterReset(taskQueue, SerialInforStrUtil.getHighStr1());
                break;
            case "high2":   //调节到高度2
                captureTaskUtil.setHighAfterReset(taskQueue, SerialInforStrUtil.getHighStr2());
                break;
            case "high3":   //调节到高度3
                captureTaskUtil.setHighAfterReset(taskQueue, SerialInforStrUtil.getHighStr3());
                break;
            case "high4":   //调节到高度4
                captureTaskUtil.setHighAfterReset(taskQueue, SerialInforStrUtil.getHighStr4());
                break;
            case "high5":   //调节到高度5
                captureTaskUtil.setHighAfterReset(taskQueue, SerialInforStrUtil.getHighStr5());
                break;
        }
    }

    public void setTask(long startTime,long endTime, int time) {
        int logoFlag = 1;
        if (time < 10) {
            ToastUtils.showToast("时间间隔应至少十分钟");
        } else if (endTime == 0){
            AppDataManager.getInstence(Net.URL_KIND_COMPANY)
                    .setPhotoTask(ShareUtil.getToken(), startTime + "", time, logoFlag, 0)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<BaseResponse<PhotoSetResponseBean>>() {
                        @Override
                        public void onSuccess(BaseResponse<PhotoSetResponseBean> response) {
                            Log.i("==settingPhotoTask==", response.toString());
                            if (response.getCode() == BaseResponse.RESULT_CODE_SUCCESS) {
                                ShareUtil.saveTaskTime(time * 60 * 1000);
                                ShareUtil.saveStartTaskTime(startTime);
                                ShareUtil.saveCaptureQuality(0);
                                ToastUtils.showToast("设置成功,重新启动服务");
                                //AppUpdateUtil.restartApp();
                                TaskServiceUtil.resetPhotoTasks();
                            } else if (response.getCode() == BaseResponse.RESULT_CODE_DEVICE_UNBIND) {
                                ToastUtils.showToast("token无效");
                                ShareUtil.cleanToken();
                                AppUpdateUtil.restartApp();
                            }
                        }
                    });

        }else {
            TaskTimeUtil.getInstance().getTimeAreaList().clear();
            TaskTimeBean taskTimeBean = new TaskTimeBean();
            taskTimeBean.setStartTime(startTime);
            taskTimeBean.setEndTime(endTime);
            taskTimeBean.setDelay((long) time);
            if (TaskTimeUtil.getInstance().isTimeRight(taskTimeBean)){
                if (TaskTimeUtil.getInstance().addTime(taskTimeBean)){
                    ToastUtils.showToast("添加 成功");
                }else {
                    ToastUtils.showToast("该时间段与已有时间段冲突");
                }
            }else {
                ToastUtils.showToast("结束时间应在开始时间后");
            }
        }
    }

    private void capture() {
        new Thread(() -> {
            CameraManager cameraManager = CameraManager.getInstance();
            cameraManager.initCamera();
            cameraManager.connectCamera();
            cameraManager.captureExecute(AppContants.CaptureFrom.FROM_TASK);
        }).start();
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        /*ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }*/
        //4g卡用检检测不到有网络，屏蔽网络检测直接连接
        return true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
