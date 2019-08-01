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
    int POST_MESSAGE_TIME = 5*60*1000; //定时上传设备信息
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
    long screenSleepTime =  60*1000;

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
        public static  String from = "fromtask";
        public static String FROM_TASK = "fromtask";
        public static String FROM_Hand = "fromhand";
    }

    interface commands{
        //雨仓开
        String yucangkai = "";
        //雨仓关
        String yucangguan = "";
        //加热烘烤开
        String jiarekai = "";
        //加热烘烤关
        String jiareguan = "";
        //虫仓漏虫开
        String chongcangkai = "";
        //虫仓漏虫关
        String chongcangguan = "";
        //清虫位置置顶-复位清虫
        String fuweiqingchong = "";
        //清虫位置置底-正在清虫
        String zhengzaiqingchong = "";
        //传送带位于接虫位置
        String csd_jiechong = "";
        //传送带位于拍照位置
        String csd_paizhao = "";
        //传送带完成一个周期的清理
        String csd_qingli = "";
        //相机补光灯打开
        String buguangdeng_kai = "";
        //相机补光灯关闭
        String buguangdeng_guan = "";
        //诱虫灯管开
        String dengguan_kai = "";
        //诱虫灯管关
        String dengguan_guan = "";
        //复位整机
        String fuweizhengji = "";
        //设备紧急停止
        String shebei_ting = "";
        //设备从紧急停止状态恢复正常工作
        String shebei_huifu = "";
        //旋转仓换仓
        String huancang = "";
        //旋转仓复位
        String fuweixzc = "";
        //请求本机信息
        String qingqiuxinxi = "";
    }

}
