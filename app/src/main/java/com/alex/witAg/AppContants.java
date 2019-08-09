package com.alex.witAg;

import com.catchbest.KSJ_BAYERMODE;
import com.catchbest.KSJ_TRIGGRMODE;
import com.catchbest.KSJ_WB_MODE;

/**
 * Created by dth
 * Des:
 * Date: 2018-01-23.
 */

public interface AppContants {

    String API_BASE_URL     = "http://pk.meishifulu.cn";   //线上
    String MQTT_BASE_URL     = "tcp://pk.meishifulu.cn:1883";   //mqtt
    //String API_BASE_URL     = "http://172.168.21.33:9007";   //本地zy
    //String API_BASE_URL     = "http://172.16.23.179:9007";   //本地xxl
    String CHECK_VERSION_URL = API_BASE_URL+"update/index";
    int    CONNECT_TIME_OUT = 15;
    int    WRITE_TIME_OUT   = 15;
    int    READ_TIME_OUT    = 15;
    String APP_TAG          = "ParkYun";
    int LOGIN_INTERCEPTOR = 401;//通过ARouter拦截登录常量
    String HOME_CURRENT_TAB_POSITION = "home_current_tab_position";
    int TASK_DEFAULT_TIME = 24*60*60*1000; //定时任务默认时间ms
    String START_TASK_DEFAULT_TIME = "12:00"; //定时任务默认开始时间
    int PASS_CHECK_DEFAULT_TIME =  10*60*1000; //默认密码验证超时时间
    int POST_MESSAGE_TIME = 10*60*1000; //定时上传设备信息
    String SHOW_PIC_URL_KEY = "SHOW_PIC_URL_KEY";

    String strIP = "192.168.0.64";//默认IP地址
    int nPort = 8000;//默认端口号
   /* String strUser = "admin";//默认用户名
    String strPsd = "1234qazz";//默认密码*/
    String strUser = "admin";//默认用户名
    String strPsd = "1234QAZZ";//默认密码

    String TOKEN_TRANS_KEY = "token_trans";

    String UMENG_APP_KEY = "5ad0192eb27b0a744a0000a3";  //umeng appkey
    String UMENG_SECERT = "1fe6a20054bcef865eeb0991ee84525b";  //Push推送业务的secret

    //屏幕隔多长时间不操作后休眠
    long screenSleepTime =  5*60*1000;

    //0  0  2592  1944  500w相机设置最大分辨率
    int width = 2592;
    int height = 1944;

    //相机参数
    int red = 10;
    int green = 10;
    int blue = 10;


    int targetExposure = 128;
    /*0,1,2分别对应 enum KSJ_COLOR_TEMPRATURE
    {
        KSJ_CT_5000K,
        KSJ_CT_6500K,
        KSJ_CT_2800K
    };*/
    int WhiteBalancePresettingSetMode = 0;

    //11
    int defaultWb = KSJ_WB_MODE.KSJ_HWB_AUTO_ONCE.ordinal();
    //int defaultWb = KSJ_WB_MODE.KSJ_HWB_AUTO_CONITNUOUS.ordinal();
    //int defaultWb = KSJ_WB_MODE.KSJ_HWB_MANUAL.ordinal();

    int defaultTriggerMode = KSJ_TRIGGRMODE.KSJ_TRIGGER_SOFTWARE.ordinal();
    int defaultBayer = KSJ_BAYERMODE.KSJ_GRBG_BGR32_FLIP.ordinal();


    interface ARouterUrl{
        String SPLASH_ACTIVITY = "/foundation/splash";
        String MAIN_ACTIVITY = "/foundation/main";
        String LOGIN_ACTIVITY = "/foundation/login";
        String SHOW_PIC = "/foundation/showpic";
        String TASK_SETTING_ACTIVITY = "/foundation/tasksettting";
        String TASK_SETTING_NEW_ACTIVITY = "/foundation/tasksetttingnew";
        String SET_ACCOUNT_ACTIVITY = "/foundation/setaccount";
        String BIND_PHONE_ACTIVITY = "/foundation/bindphone";
        String RESET_ACTIVITY = "/foundation/reset";
        String SETIP_ACTIVITY = "/foundation/setip";
        String SET_COMPANY_URL_ACTIVITY = "/foundation/set_company_url";
        String BIND_COMPANY_ACTIVITY = "/foundation/bind_company";
        String DEBUG_ACTIVITY = "/foundation/debug_activity";
        String PreviewActivity = "/foundation/PreviewActivity";
        String ShowCaptureActivity = "/foundation/ShowCaptureActivity";
    }
    String KEY_PIC_BYTES_TAKE_PHOTO = "bytes_take_photo";

    interface CameraType{ //相机类型
        String KSJ = "KSJ"; //凯视佳相机
        String HKVision = "HKVision"; //海康威视

    }

    interface CaptureFrom{
        String from = "fromtask";
        String FROM_TASK = "fromtask";
        String FROM_Hand = "fromhand";
    }

    interface commands{
        //雨仓开
        String yucangkai = "STA:01,TL:000,MODE_T:0";
        //雨仓关
        String yucangguan = "STA:02,TL:000,MODE_T:0";
        //加热烘烤开
        String jiarekai = "STA:03,TL:000,MODE_T:0";
        //加热烘烤关
        String jiareguan = "STA:04,TL:000,MODE_T:0";
        //虫仓2漏虫开
        String chongcang2kai = "STA:05,TL:000,MODE_T:0";
        //虫仓2漏虫关
        String chongcang2guan = "STA:06,TL:000,MODE_T:0";
        //清虫位置置顶-复位清虫
        String fuweiqingchong = "STA:07,TL:000,MODE_T:0";
        //清虫位置置底-正在清虫
        String zhengzaiqingchong = "STA:08,TL:000,MODE_T:0";
        //传送带位于接虫位置
        String csd_jiechong = "STA:09,TL:000,MODE_T:0";
        //传送带位于拍照位置
        String csd_paizhao = "STA:10,TL:000,MODE_T:0";
        //传送带完成一个周期的清理
        String csd_qingli = "STA:11,TL:000,MODE_T:0";
        //相机补光灯打开
        String buguangdeng_kai = "STA:12,TL:000,MODE_T:0";
        //相机补光灯关闭
        String buguangdeng_guan = "STA:13,TL:000,MODE_T:0";
        //诱虫灯管开
        String dengguan_kai = "STA:14,TL:000,MODE_T:0";
        //诱虫灯管关
        String dengguan_guan = "STA:15,TL:000,MODE_T:0";
        //复位整机
        String fuweizhengji = "STA:16,TL:000,MODE_T:0";
        //设备紧急停止
        String shebei_ting = "STA:17,TL:000,MODE_T:0";
        //设备从紧急停止状态恢复正常工作
        String shebei_huifu = "STA:18,TL:000,MODE_T:0";
        //旋转仓计数
        String xzchuancang = "STA:22,TL:000,MODE_T:0";
        //旋转仓复位
        String fuweixzc = "STA:23,TL:000,MODE_T:0";
        //请求本机信息
        String qingqiuxinxi = "STA:19,TL:000,MODE_T:0";
        //虫仓1漏虫开
        String chongcang1kai = "STA:25,TL:000,MODE_T:0";
        //虫仓1漏虫关
        String chongcang1guan = "STA:26,TL:000,MODE_T:0";


        //请求开始拍照流程
        String kaishipaizhao = "STA:24,TL:000,MODE_T:0";

        //时控 进入诱虫状态
        String shikongkai = "STA:21,TL:000,MODE_T:1";
        //时控 关闭诱虫状态
        String shikongguan = "STA:21,TL:000,MODE_T:0";

    }

}
