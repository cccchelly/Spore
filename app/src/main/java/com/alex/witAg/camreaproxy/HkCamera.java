package com.alex.witAg.camreaproxy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.ui.test.PlaySurfaceView;
import com.alex.witAg.ui.test.jna.HCNetSDKJNAInstance;
import com.alex.witAg.utils.CaptureTaskUtil;
import com.alex.witAg.utils.FileUtils;
import com.alex.witAg.utils.PostTaskMsgUtil;
import com.alex.witAg.utils.SerialInforStrUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TimeUtils;
import com.alex.witAg.utils.ToastUtils;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.INT_PTR;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_JPEGPARA;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.RealDataCallBack;
import com.hikvision.netsdk.StdDataCallBack;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by apple on 2019/6/18.
 */

public class HkCamera implements ICamera {
    OnCaptureListener onCaptureListener;

    @Override
    public void initCamera() {
       //不需要初始化  直接登录
    }

    @Override
    public void connectCamera() {
        login();
    }

    @Override
    public void captureExecute(String from) {
        capture(from);
    }


    @Override
    public void unInitCamera() {

    }

    @Override
    public boolean isCameraOpen() {
        boolean isOpen = false;
        for (int i=1; i<5*60;i++){    //查询状态是否改变  若状态未改变休眠一秒继续查询
            if (TextUtils.equals(ShareUtil.getDeviceStatue(), SerialInforStrUtil.STA_CLOSE_RESET)){ //sta=0表示复位关机状态
                //Log.i("==isCapOpen==",i+"");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                isOpen = true;
                break;
            }
        }
        return isOpen;
    }

    @Override
    public boolean isCameraConnect() {
        boolean isLogined = false;
        for (int i = 1;i<60;i++){
            int errCode = login();
            if (errCode!=0){
                if (errCode==1){  //用户名密码错误 退出循环登录请求
                    break;
                }else {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                isLogined = true;
                break;
            }
        }
        return isLogined;
    }

    @Override
    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        this.onCaptureListener = onCaptureListener;
    }


    private String TAG = HkCamera.class.getName();
    public static  String from = "fromtask";

    /**
     * @fn initeSdk
     */
    private boolean initeSdk() {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.e(TAG, "HCNetSDK init is failed!");
            return false;
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/",
                true);
        return true;
    }

    public void capture(String from) {
        this.from = from;
        if (initeSdk()) { //初始化sdk
            Log.i(TAG,"logId="+ShareUtil.getLoginId()+",chanel="+ShareUtil.getChannel());
            //testShowCapture(ShareUtil.getLoginId(),ShareUtil.getChannel());
            Test_CaptureJpegPicture_new(ShareUtil.getLoginId(), ShareUtil.getChannel());
        }
    }

    private void testShowCapture(int loginId,int channel) {

        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = channel;
        previewInfo.dwStreamType = 1; // substream
        previewInfo.bBlocked = 1;

        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(loginId,
                previewInfo, null);

        if (m_iPlayID < 0) {
            Log.e(TAG, "NET_DVR_RealPlay is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }

        boolean bRet = HCNetSDKJNAInstance.getInstance().NET_DVR_OpenSound(m_iPlayID);
        if(bRet){
            Log.e(TAG, "NET_DVR_OpenSound Succ!");
        }

        Log.i(TAG,
                "NetSdk Play sucess ***********************3***************************");

        if(cbf == null) {
            cbf = new StdDataCallBack() {
                public void fStdDataCallback(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize) {
                    // DemoActivity.this.processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_REALTIME);
                }
            };
        }

        if(!HCNetSDK.getInstance().NET_DVR_SetStandardDataCallBack(m_iPlayID, cbf)){
            Log.e(TAG, "NET_DVR_SetStandardDataCallBack is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
        }
        Log.i(TAG,
                "NET_DVR_SetStandardDataCallBack sucess ***************************************************");

        if (m_iPlayID != -1){

            String deviceStatue = "";
            if (TextUtils.equals(ShareUtil.getDeviceStatue(),"1")){
                deviceStatue = "-A";
            }else if (TextUtils.equals(ShareUtil.getDeviceStatue(),"2")){
                deviceStatue = "-B";
            }else {
                deviceStatue = "-O";
            }
            String picName = TimeUtils.millis2String(System.currentTimeMillis(),
                    new SimpleDateFormat("yyyyMMddHHmmss")) + deviceStatue + ".jpeg";
            File file = new File(Environment.getExternalStorageDirectory(), picName);
            String picPath = file.getPath();
            Log.i("==picPath==",picPath);


            if(m_iPlayID < 0){
                Log.e(TAG, "please start preview first");
                return;
            }else if(HCNetSDKJNAInstance.getInstance().NET_DVR_CapturePictureBlock(m_iPlayID, "/sdcard/capblock.jpg", 0)){
                Log.e(TAG, "NET_DVR_CapturePictureBlock Succ!");
            } else {
                Log.e(TAG, "NET_DVR_CapturePictureBlock fail! Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            }

        }

    }

    /**
     * 截图
     * @param iUserID
     * @param iChan
     */
    public  void Test_CaptureJpegPicture_new(int iUserID, int iChan) {

        PostTaskMsgUtil.instance().postMsg(4,"拍照,时间："+TimeUtils.millis2String(System.currentTimeMillis(),
                new SimpleDateFormat("yyyyMMddHHmmss")) );

        NET_DVR_JPEGPARA strJpeg = new NET_DVR_JPEGPARA();
        strJpeg.wPicQuality = ShareUtil.getCaptureQuality();   //图片质量  0-最好   1-较好   2-一般
        strJpeg.wPicSize = 0xff;
        int iBufferSize = 1*1024 * 1024;
        byte[] sbuffer = new byte[iBufferSize];
        INT_PTR bytesRerned = new INT_PTR();
        if (!HCNetSDK.getInstance().NET_DVR_CaptureJPEGPicture_NEW(iUserID, iChan, strJpeg, sbuffer, iBufferSize, bytesRerned)) {
            Log.i(TAG, "NET_DVR_CaptureJPEGPicture_NEW!" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            String deviceStatue = "";
            if (TextUtils.equals(ShareUtil.getDeviceStatue(),"1")){
                deviceStatue = "-A";
            }else if (TextUtils.equals(ShareUtil.getDeviceStatue(),"2")){
                deviceStatue = "-B";
            }else {
                deviceStatue = "-O";
            }
            String picName = TimeUtils.millis2String(System.currentTimeMillis(),
                    new SimpleDateFormat("yyyyMMddHHmmss")) + deviceStatue + ".jpeg";
            //文件保存到内存
            FileUtils.saveContentToSdcard(picName,sbuffer);
            //文件地址保存到数据库
            PicPathsBean data = new PicPathsBean();
            data.setPath(picName);
            data.save();
            File file = FileUtils.getFileFromSdcard(picName);

            if (null!=onCaptureListener){
                if (TextUtils.equals(from, AppContants.CaptureFrom.FROM_Hand)) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    Bitmap bitmap = FileUtils.getPicFromBytes(sbuffer, options);
                    onCaptureListener.finish(bitmap, file, picName);
                }
            }
            Log.i(TAG, "NET_DVR_CaptureJPEGPicture_NEW size!" + bytesRerned.iValue);
        }
    }

    //尝试登录摄像头
    public int loginCapture(){
        return login();
    }


    //登录设备
    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;
    private StdDataCallBack cbf = null;
    private RealDataCallBack rdf = null;
    private SurfaceView m_osurfaceView = null;
    private int m_iPlayID = -1;
    private static PlaySurfaceView[] playView = new PlaySurfaceView[4];

    private int login(){
        try {
            // login on the device
            int errCode = loginDevice();
            if (ShareUtil.getLoginId() < 0) {
                Log.e(TAG, "This device logins failed!");
                return errCode;
            } else {
                Log.e(TAG, "m_iLogID=" + ShareUtil.getLoginId());
            }
            // get instance of exception callback and set
            ExceptionCallBack oexceptionCbf = getExceptiongCbf();
            if (oexceptionCbf == null) {
                Log.e(TAG, "ExceptionCallBack object is failed!");
                return errCode;
            }

            if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(
                    oexceptionCbf)) {
                Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
                return errCode;
            }

            //                m_oLoginBtn.setText("Logout");
            Log.i(TAG,
                    "Login sucess ****************************1***************************");
            return errCode;

        } catch (Exception err) {
            Log.e(TAG, "error: " + err.toString());
            return -1;
        }
    }

    private int loginDevice() {
        int errCode = -1;
        if (!initeSdk()) {
            return errCode;
        }
        errCode = loginNormalDevice();
        return errCode;
    }

    private int loginNormalDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return -1;
        }
        String strIP = ShareUtil.getIp();//IP地址
        int nPort = ShareUtil.getPort();//端口号
        String strUser = ShareUtil.getUser();//用户名
        String strPsd = ShareUtil.getPass();//密码
        Log.i("==CapLogin==","IP:"+strIP+",port:"+nPort+",user:"+strUser+",pass:"+strPsd);
        // call NET_DVR_Login_v30 to login on, port 8000 as default
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort,
                strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "NET_DVR_Login is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
        }
        Log.i(TAG,(m_oNetDvrDeviceInfoV30.byChanNum)+"");
        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            //m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            ShareUtil.saveChannel(m_oNetDvrDeviceInfoV30.byChanNum);
        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            //m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            ShareUtil.saveChannel(m_oNetDvrDeviceInfoV30.byIPChanNum
                    + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256);
        }
        if (iLogID>=0) {  //登陆成功，保存登录id
            ShareUtil.saveLoginId(iLogID);
        }
        Log.i(TAG, "NET_DVR_Login is Successful!");
        App.setIsNeedReLogin(false);
        return HCNetSDK.getInstance().NET_DVR_GetLastError();   //返回错误码
    }

    private ExceptionCallBack getExceptiongCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack() {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle) {
                System.out.println("recv exception, type:" + iType);
            }
        };
        return oExceptionCbf;
    }




}
