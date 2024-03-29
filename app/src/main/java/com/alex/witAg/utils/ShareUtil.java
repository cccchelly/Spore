package com.alex.witAg.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;

import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2018-03-28.
 */

public class ShareUtil {
    private static final String SHARE_NAME = "app_share_name";
    private static final String COMPANY_BASE_URL = "company_base_url";
    private static final String TOKEN = "user_token";
    private static final String BindCompany = "bind_company";

    private static final String LOGIN_ID ="login_id";
    private static final String CHANNEL ="channel";
    private static  final String TIME_TASK = "time_task";  //定时任务执行时间
    private static  final String START_TIME_TASK = "start_time_task";  //定时任务开始时间
    private static  final String CAPTURE_QUALITY = "capture_quality";  //拍照质量

    private static  final String STR_IP = "device_ip";//IP地址
    private static  final String PORT = "device_port";//端口号
    private static  final String USER = "device_username";//用户名
    private static  final String PASSWORD = "device_password";//密码

    private static final String ANDROID_PASS = "android_password"; //设备密码

    private static final String ANDROID_PASS_CHECK_TIME = "android_password_check_time"; //平板密码校验时间
    private static final String CaptureCamSta = "CaptureCamSta"; //相机状态  指令改变已废弃，通过设备状态判断
    private static final String CaptureHignSta = "CaptureHignSta"; //板子高度
    private static final String CaptureErrorSta = "CaptureErrorSta"; //相机错误码
    private static final String DeviceStatue = "DeviceStatue"; //设备状态   1-摄像机打开并翻转到正面，2-摄像机打开并翻转到反面，0-摄像机关闭并复位
    private static final String DeviceError = "DeviceError"; //设备错误码
    private static final String DeviceBatvol = "DeviceBatvol"; //电池电压
    private static final String DeviceSunvol = "DeviceSunvol"; //太阳能电压
    private static final String Msta = "Msta"; //雨水状态
    private static final String TEMP = "TEMP"; //温度
    private static final String hum = "hum"; //湿度

    private static final String LocationLongitude = "LocationLongitude"; //定位Longitude
    private static final String LocationLatitude = "LocationLatitude"; //定位Latitude

    private static  final String postTime = "PostTime";
    private static  final String screenBright = "screenBright";
    private static  final String mqttMsgId = "mqttMsgId";

    private static final String taskTimeAreas = "taskTimeAreas";  //定时任务时间区域的列表
    private static final String taskTimeMins = "taskTimeMins";  //定时任务全部时间点的列表

    private static final String lampTimeAreas = "lampTimeAreas";  //灯管开关任务时间区域的列表
    private static final String lampTimeopen = "lampTimeopen";  //执行灯管开的时间点
    private static final String lampTimeclose = "lampTimeclose";  //执行灯管关的时间点

    private static final String CameraType = "cameraType";  //相机种类
    private static final String seraIndex = "seraIndex";  //串口下标

    public static SharedPreferences getShare(){
        SharedPreferences sharedPreferences = App.getAppContext().getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    //存储经纬度
    public static  void saveLatitude(double la){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putFloat(LocationLatitude, (float) la);
        editor.commit();
    }
    public static double getLatitude(){
        float la = getShare().getFloat(LocationLatitude, 0f);
        return la;
    }

    public static  void saveLongitude(double lo){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putFloat(LocationLongitude, (float) lo);
        editor.commit();
    }
    public static double getLongitude(){
        float lo = getShare().getFloat(LocationLongitude, 0f);
        return lo;
    }

    // 相机状态
    public static void saveCaptureCamSta(String sta){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(CaptureCamSta,sta);
        editor.apply();
    }
    public static String getCaptureCamSta(){
        String sta = getShare().getString(CaptureCamSta,"0");
        return sta;
    }

    // 板子高度
    public static void saveCaptureHignSta(String sta){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(CaptureHignSta,sta);
        editor.apply();
    }
    public static String getCaptureHignSta(){
        String sta = getShare().getString(CaptureHignSta,"1");
        return sta;
    }

    // 相机错误码
    public static void saveCaptureErrorSta(String sta){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(CaptureErrorSta,sta);
        editor.apply();
    }
    public static String getCaptureErrorSta(){
        String sta = getShare().getString(CaptureErrorSta,"0");
        return sta;
    }

    // 设备状态
    public static void saveDeviceStatue(String sta){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(DeviceStatue,sta);
        editor.apply();
    }
    public static String getDeviceStatue(){
        String sta = getShare().getString(DeviceStatue,"0");
        return sta;
    }

    // 设备错误码
    public static void saveDeviceError(String sta){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(DeviceError,sta);
        editor.apply();
    }
    public static String getDeviceError(){
        String sta = getShare().getString(DeviceError,"0");
        return sta;
    }

    // 电池电压
    public static void saveDeviceBatvol(String sta){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(DeviceBatvol,sta);
        editor.apply();
    }
    public static String getDeviceBatvol(){
        String sta = getShare().getString(DeviceBatvol,"0");
        return sta;
    }

    // 太阳能电压
    public static void saveDeviceSunvol(String sta){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(DeviceSunvol,sta);
        editor.apply();
    }
    public static String getDeviceSunvol(){
        String sta = getShare().getString(DeviceSunvol,"0");
        return sta;
    }

    // 雨水状态
    public static void saveRain(String msta){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(Msta,msta);
        editor.apply();
    }
    public static String getRain(){
        String sta = getShare().getString(Msta,"0");
        return sta;
    }

    // 温度
    public static void saveTemp(String temp){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(TEMP,temp);
        editor.apply();
    }
    public static String getTemp(){
        String sta = getShare().getString(TEMP,"0");
        return sta;
    }

    // 湿度
    public static void saveHum(String temp){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(hum,temp);
        editor.apply();
    }
    public static String getHum(){
        String sta = getShare().getString(hum,"0");
        return sta;
    }

    // 湿度
    public static void saveCameraType(String cameraType){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(CameraType,cameraType);
        editor.apply();
    }
    public static String getCameraType(){
        String sta = getShare().getString(CameraType,"");
        return sta;
    }
    // 串口下标
    public static void saveSeraIndex(int index){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putInt(seraIndex,index);
        editor.apply();
    }

    public static int getSeraIndex(){
        int sta = getShare().getInt(seraIndex,-1);
        return sta;
    }




    // 企业baseurl
    public static void saveCompanyBaseUrl(String url){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(COMPANY_BASE_URL,url);
        editor.apply();
    }
    public static String getCompanyBaseUrl(){
        String url = getShare().getString(COMPANY_BASE_URL,"-1");
        return TextUtils.equals(url,"-1")? AppContants.API_BASE_URL:url;
    }
    public static boolean isCompanyBaseUrlSetting(){
        String url = getShare().getString(COMPANY_BASE_URL,"-1");
        return TextUtils.equals(url,"-1")? false:true;
    }

    // 绑定公司账号
    public static void saveCompanyUser(String user){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(BindCompany,user);
        editor.apply();
    }
    public static String getCompanyUser(){
        String url = getShare().getString(BindCompany,"-1");
        return TextUtils.equals(url,"-1")? AppContants.API_BASE_URL:url;
    }
    public static boolean isCompanyUserBind(){
        String url = getShare().getString(BindCompany,"-1");
        return TextUtils.equals(url,"-1")? false:true;
    }
    public static void cleanBindComMsg(){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(BindCompany,"-1");
        editor.apply();
    }

    // token
    public static void saveToken(String token){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(TOKEN,token);
        editor.apply();
    }
    public static String getToken(){
        String token = getShare().getString(TOKEN,"");
        return token;
    }
    public static boolean hasToken(){
        String token = getShare().getString(TOKEN,"");
        return TextUtils.equals(token,"")?false:true;
    }
    public static void cleanToken(){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(TOKEN,"");
        editor.apply();
    }

    //设备loginId
    public static void saveLoginId(int loginId){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putInt(LOGIN_ID,loginId);
        editor.apply();
    }
    public static int getLoginId(){
       return getShare().getInt(LOGIN_ID,-1);
    }

    //      设备通道号
    public static void saveChannel(int channel){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putInt(CHANNEL,channel);
        editor.apply();
    }
    public static int getChannel(){
        return getShare().getInt(CHANNEL,-1);
    }

    // 定时任务时间间隔
    public static void saveTaskTime(int time){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putInt(TIME_TASK,time);
        editor.apply();
    }
    public static int getTaskTime(){
        int time = getShare().getInt(TIME_TASK,-1);
        return time==-1? AppContants.TASK_DEFAULT_TIME:time;
    }

    // 定时任务开始时间
    public static void saveStartTaskTime(long time){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putLong(START_TIME_TASK,time);
        editor.apply();
    }
    public static String getStartTaskTime(){
        long time = getShare().getLong(START_TIME_TASK,-1);
        return time==-1? AppContants.START_TASK_DEFAULT_TIME:TimeUtils.millis2String(time,new SimpleDateFormat("HH:mm"));
    }

    //拍照质量
    public static void saveCaptureQuality(int quality){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putInt(CAPTURE_QUALITY,quality);
        editor.apply();
    }
    public static int getCaptureQuality(){
        int quality = getShare().getInt(CAPTURE_QUALITY,0);
        return quality;
    }

    // IP地址
    public static void saveIp(String ip){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(STR_IP,ip);
        editor.apply();
    }
    public static String getIp(){
        String ip = getShare().getString(STR_IP,"-1");
        return TextUtils.equals(ip,"-1")? AppContants.strIP:ip;
    }

    // 端口号
    public static void savePort(int port){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putInt(PORT,port);
        editor.apply();
    }
    public static int getPort(){
        int port = getShare().getInt(PORT,-1);
        return port==-1? AppContants.nPort:port;
    }

    // 账户名
    public static void saveUser(String user){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(USER,user);
        editor.apply();
    }
    public static String getUser(){
        String user = getShare().getString(USER,"-1");
        return TextUtils.equals(user,"-1")? AppContants.strUser:user;
    }

    // 密码
    public static void savePass(String pass){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(PASSWORD,pass);
        editor.apply();
    }
    public static String getPass(){
        String pass = getShare().getString(PASSWORD,"-1");
        return TextUtils.equals(pass,"-1")? AppContants.strPsd:pass;
    }

    // 平板密码(有无绑定基础设置依据)
    public static void saveAndroidPass(String pass){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(ANDROID_PASS,pass);
        editor.apply();
    }
    public static String getAndroidPass(){
        String pass = getShare().getString(ANDROID_PASS,"-1");
        return TextUtils.equals(pass,"-1")? AppContants.strIP:pass;
    }
    public static void cleanAndroidPass(){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(ANDROID_PASS,"-1");
        editor.apply();
    }
    public static boolean isSetAndroidPass(){
        String pass = getShare().getString(ANDROID_PASS,"-1");
        return TextUtils.equals(pass,"-1")? false:true;
    }

    //密码校验时间
    public static void savePassCheckTime(long time){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putLong(ANDROID_PASS_CHECK_TIME,time);
        editor.apply();
    }

    public static boolean getIsPassChecked(){
        long time = getShare().getLong(ANDROID_PASS_CHECK_TIME,-1);
        long currentTime = System.currentTimeMillis();
        if (currentTime-time<=AppContants.PASS_CHECK_DEFAULT_TIME){
            return true;
        }else {
            return false;
        }
    }

    //是否登录
    public static boolean isLogin(){
        return getLoginId()==-1?false:true;
    }


    // 上传信息任务时间间隔
    public static void setPostTaskTime(int time){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putInt(postTime,time);
        editor.apply();
    }

    public static int getPostTaskTime(){
        int time = getShare().getInt(postTime,-1);
        return time==-1? AppContants.POST_MESSAGE_TIME:time;
    }

    // 屏幕亮度值
    public static void setScreenBright(float time){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putFloat(screenBright,time);
        editor.apply();
    }

    public static float getScreenBright(){
        float time = getShare().getFloat(screenBright,-1.0f);
        return time;
    }

    // mqtt最后一次消息id
    public static void setMqttMsgId(int id){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putInt(mqttMsgId,id);
        editor.apply();
    }

    public static int getMqttMsgId(){
        int id = getShare().getInt(mqttMsgId,-1);
        return id;
    }

    //定时任务区域列表
    public static void  setTimeAreaStr(String timeAreaStr){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(taskTimeAreas,timeAreaStr);
        editor.apply();
    }

    public static String getTimeAreaStr(){
        return getShare().getString(taskTimeAreas,null);
    }

    //定时任务计算出每个时间点的列表
    public static void  setTimeMinStr(String timeMinStr){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(taskTimeMins,timeMinStr);
        editor.apply();
    }

    public static String getTimeMinStr(){
        return getShare().getString(taskTimeMins,null);
    }

    /*灯管时间控制*/
    //灯管定时任务区域列表
    public static void  setLampTimeAreaStr(String timeAreaStr){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(lampTimeAreas,timeAreaStr);
        editor.apply();
    }

    public static String getLampTimeAreaStr(){
        return getShare().getString(lampTimeAreas,null);
    }


    //灯管开关任务所有开始时间点的列表
    public static void  setOpenTimeMinStr(String timeMinStr){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(lampTimeopen,timeMinStr);
        editor.apply();
    }

    public static String getOpenTimeMinStr(){
        return getShare().getString(lampTimeopen,null);
    }


    //灯管开关任务所有关闭时间点的列表
    public static void  setCloseTimeMinStr(String timeMinStr){
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(lampTimeclose,timeMinStr);
        editor.apply();
    }

    public static String getCloseTimeMinStr(){
        return getShare().getString(lampTimeclose,null);
    }



}
