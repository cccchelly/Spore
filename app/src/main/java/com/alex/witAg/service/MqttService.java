package com.alex.witAg.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.base.BaseObserver;
import com.alex.witAg.base.BaseResponse;
import com.alex.witAg.bean.MqttMsgBean;
import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.bean.PostMsgBean;
import com.alex.witAg.bean.PostMsgResultBean;
import com.alex.witAg.bean.TaskTimeBean;
import com.alex.witAg.http.AppDataManager;
import com.alex.witAg.http.network.Net;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.AppMsgUtil;
import com.alex.witAg.utils.CapturePostUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TaskServiceUtil;
import com.alex.witAg.utils.TaskTimeUtil;
import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.litepal.crud.DataSupport;

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

    private String host = "tcp://192.168.1.33:1883";
    //private String host = AppContants.MQTT_BASE_URL;
    private String userName = "admin";
    private String passWord = "password";
    //private static String myTopic = "Device/DFS/cid" + AppMsgUtil.getIMEI(App.getAppContext());
    private static String myTopic = "MQTT/TOPIC";
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
            // 连接失败
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            client.messageArrivedComplete(message.getId(),message.getQos());

            String str1 = new String(message.getPayload());
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG, "id:"+message.getId()+",messageArrived:" + str1);
            Log.i(TAG, str2);
            postMqttMsg(str1);
            dealMsg(str1);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接
        }
    };

    private void postMqttMsg(String str){
        PostMsgBean postMsgBean = new PostMsgBean();
        postMsgBean.setSunvol(ShareUtil.getDeviceSunvol());
        postMsgBean.setBatvol(ShareUtil.getDeviceBatvol());
        postMsgBean.setHighsta(ShareUtil.getCaptureHignSta());
        postMsgBean.setSta(ShareUtil.getDeviceStatue());
        postMsgBean.setError(ShareUtil.getDeviceError());
        postMsgBean.setImei(AppMsgUtil.getIMEI(App.getAppContext()));
        postMsgBean.setLatitude(ShareUtil.getLatitude()+"");
        postMsgBean.setLongitude(ShareUtil.getLongitude()+"");
        postMsgBean.setMsta(ShareUtil.getRain());
        postMsgBean.setTemp(ShareUtil.getTemp());
        postMsgBean.setHum(ShareUtil.getHum());
        postMsgBean.setFirstStart(false);

        postMsgBean.setMqttMessage(str);

        try {
            List<PicPathsBean> picPaths = DataSupport.findAll(PicPathsBean.class);
            postMsgBean.setPics("本地图片数量："+picPaths.size()+"；名字="+picPaths.toString());
        }catch (Exception e){
        }

        AppDataManager.getInstence(Net.URL_KIND_BASE)
                .postDeviceMsg(postMsgBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<PostMsgResultBean>>() {
                    @Override
                    public void onSuccess(BaseResponse<PostMsgResultBean> response) {

                    }
                });
    }

    //处理收到的消息
    private void dealMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        try {
            MqttMsgBean msgBean = new Gson().fromJson(msg,MqttMsgBean.class);
            switch (msgBean.getType()){
                case "command": //执行串口命令
                    taskQueue.add(new SeralTask(msgBean.getCmd()));
                    break;
                case "android_control":
                    switch (msgBean.getCmd()){
                        case "post_picture": //上传本地照片
                            postLocalPicture();
                            break;
                        case "time_control"://设置拍照时间
                            setCaptureTask(msgBean.getControl());
                            break;
                    }

                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setCaptureTask(List<TaskTimeBean> times){
        new Thread(()->{
            TaskTimeUtil.getInstance().clearTimes(); //删除旧时间
            for (TaskTimeBean task : times){ //添加新时间
                TaskTimeUtil.getInstance().addTime(task);
            }
            //重启任务
            TaskServiceUtil.resetPhotoTasks();
        }).start();
    }

    private void postLocalPicture(){
        new Thread(() -> CapturePostUtil.findLocalPic()).start();
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
