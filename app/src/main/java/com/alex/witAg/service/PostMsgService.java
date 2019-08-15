package com.alex.witAg.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.base.BaseObserver;
import com.alex.witAg.base.BaseResponse;
import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.bean.PostMsgBean;
import com.alex.witAg.bean.PostMsgResultBean;
import com.alex.witAg.http.AppDataManager;
import com.alex.witAg.http.network.Net;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.ActivityBrightnessManager;
import com.alex.witAg.utils.AppMsgUtil;
import com.alex.witAg.utils.MyLifecycleHandler;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TimeUtils;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018-07-03.
 */

public class PostMsgService extends Service {
    TaskQueue taskQueue;

    public static long lastClickTime;
    private String TAG = PostMsgService.class.getName();
    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //查询是否有可更新补丁
            //SophixManager.getInstance().queryAndLoadNewPatch();

            handler.postDelayed(runnable, ShareUtil.getPostTaskTime());

            taskQueue.add(new SeralTask(AppContants.commands.qingqiuxinxi));

            //把当前的时间传给硬件
            String timeStr = "BJ"+
                    TimeUtils.millis2String(System.currentTimeMillis(),new SimpleDateFormat("yyyyMMddHHmmss"))+getWeekday();
            taskQueue.add(new SeralTask(timeStr));

            Log.i("==post_msg===","post_msg.");
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
    };

    private String getWeekday(){
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-1;
        if(dayOfWeek <0)dayOfWeek=0;
        if (dayOfWeek ==0){
            return "7";
        }else {
            return dayOfWeek+"";
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        taskQueue = TaskQueue.getInstance();

        lastClickTime = System.currentTimeMillis();

        handler.post(runnable);

        new Thread(() -> location()).start();

        new Thread(() -> {    //循环查询是否有操作   一段时间无操作则降低屏幕亮度
            while (true){
                if (System.currentTimeMillis()-lastClickTime>AppContants.screenSleepTime){
                    if (MyLifecycleHandler.isApplicationInForeground()) {
                        ActivityBrightnessManager.setScreenBrightness(0);
                    }else {
                        ActivityBrightnessManager.setScreenBrightness(255);
                    }
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


    private void location() {
        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                        amapLocation.getLatitude();//获取纬度
                        amapLocation.getLongitude();//获取经度
                        amapLocation.getAccuracy();//获取精度信息
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date(amapLocation.getTime());
                        df.format(date);//定位时间
                        ShareUtil.saveLatitude(amapLocation.getLatitude());
                        ShareUtil.saveLongitude(amapLocation.getLongitude());
                        Log.i(TAG,"Longitude="+amapLocation.getLongitude()+",Latitude="+amapLocation.getLatitude());
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.i("AmapError","location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                    }
                }
            }
        });
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(5*60*1000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
