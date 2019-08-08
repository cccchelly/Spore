package com.alex.witAg.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.adapter.DeviceAdapter;
import com.alex.witAg.camreaproxy.CameraManager;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortFinder;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;
import com.orhanobut.logger.Logger;

import org.litepal.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Administrator on 2018-03-28.
 */

public class CaptureTaskUtil implements
        OnOpenSerialPortListener {
    private String TAG = CaptureTaskUtil.class.getName();
    private SerialPortManager mSerialPortManager;
    private ArrayList<Device> mDevices;
    private Device mDevice;
    private DeviceAdapter mDeviceAdapter;
    private boolean isDeviceOpenFlag = false;
    StringBuilder deviceStrBuilder = new StringBuilder(); //电机返回参数处理
    StringBuilder captureStrBuilder = new StringBuilder();//摄像头返回的参数处理
    StringBuilder newDeviceStrBuilder = new StringBuilder();//设备信息返回的参数处理(新定义包含温湿度等的)

    Handler toastHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ToastUtils.showToast(msg.obj.toString());
        }
    };

    private static CaptureTaskUtil captureTaskUtil = null;

    private CaptureTaskUtil() {
    }

    public static CaptureTaskUtil instance() {
        if (captureTaskUtil == null) {
            synchronized (CaptureTaskUtil.class) {
                if (captureTaskUtil == null) {
                    captureTaskUtil = new CaptureTaskUtil();
                }
            }
        }
        return captureTaskUtil;

    }

    public void initDevice(Context context) {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        mDevices = serialPortFinder.getDevices();
        if (mDevices == null || mDevices.size() == 0) return;
        mDevice = mDevices.get(0);
        mSerialPortManager = new SerialPortManager();
        Logger.d("device: ", mDevices);

        mDeviceAdapter = new DeviceAdapter(context, mDevices);
        /*这里要找到对应的串口，看接的是哪个口子，连接相应的端口*/
        //mSerialPortManager.closeSerialPort();

        Device device;
        int index = ShareUtil.getSeraIndex();
        if (index == -1) { //默认未设置是打开倒数第二个串口
            device = mDeviceAdapter.getItem(mDeviceAdapter.getCount() - 2);
        } else {
            device = mDeviceAdapter.getItem(index);
        }

        com.alex.witAg.utils.LogUtil.i("串口:" + device.getName());

        //Device mDevice = mDeviceAdapter.getItem(6);
        openDevice(device);

    }



    /*  */

    /**
     * @fn initeSdk
     *//*
    private boolean initeSdk() {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.e(TAG, "HCNetSDK init is failed!");
            return false;
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/",
                true);
        return true;
    }*/
    public boolean openDevice(Device device) {
        // 打开串口
        boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener((OnOpenSerialPortListener) this)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        String str = TextChangeUtil.ByteToString(bytes);
                        Log.i(TAG, "==onDataReceived [ byte[] ]: " + Arrays.toString(bytes));
                        Log.i(TAG, "==onDataReceived [ String ]: " + str);
                        handleCallbackMsg(str);
                    }

                    @Override
                    public void onDataSent(byte[] bytes) {
                        Log.i(TAG, "==onDataSent [ byte[] ]: " + Arrays.toString(bytes));
                        Log.i(TAG, "==onDataSent [ String ]: " + new String(bytes));
                        final byte[] finalBytes = bytes;
                        Log.i(TAG, String.format("发送\n%s", new String(finalBytes)));
                    }
                })
                .openSerialPort(device.getFile(), 9600);

        Log.i(TAG, "onCreate: openSerialPort == " + openSerialPort);
        return openSerialPort;
    }

    /*处理指令反馈和设备信息返回参数*/
    private void handleCallbackMsg(String str) {
        if (str.contains("cmd:1")) {
            App.setIsWaitTaskFinish(false);
            saveDeviceMsg(str);
        } else if (str.contains("cmd:2")) {
            //根据取到的值判断是否到目标状态  因为硬件可能发送很多中间状态回来
            saveCommandMsg(str);
        } else if (str.contains("cmd:3")) {
            App.setIsWaitTaskFinish(false);
            toastOnMain("设备忙");
        }
    }

    private void toastOnMain(String content) {
        Message msg = Message.obtain();
        msg.what = 0;
        msg.obj = content;
        toastHandle.sendMessage(msg);
    }

    //保存命令返回参数 (设备状态码和错误码)
    private void saveCommandMsg(String string) {
        PostTaskMsgUtil.instance().postMsg(2, string);
        Log.i("==设备码：==", string);

        String camSta = CommandBackStrUtil.getInstance().getCapValue(string, 1);
        String error = CommandBackStrUtil.getInstance().getCapValue(string, 2);

        if (TextUtils.equals(camSta,App.getToDeviceSta())){
            com.alex.witAg.utils.LogUtil.i("==到达任务目标状态=="+App.getToDeviceSta());
            App.setIsWaitTaskFinish(false);
        }
        if (!TextUtils.equals(error,"00")){
            App.setIsWaitTaskFinish(false);
        }

        if (TextUtils.equals(camSta,"27")){
            takePhoto();
        }

        Log.i("==","状态码："+camSta+"，错误码"+error);
        ShareUtil.saveDeviceStatue(camSta);
        ShareUtil.saveDeviceError(error);
    }

    /*保存查询到的设备参数*/
    private void saveDeviceMsg(String str) {
        LogUtil.d("==设备参数==", str);
        String volBat = DeviceInfoStrUtil.getValue(str, 1);
        String volSun = DeviceInfoStrUtil.getValue(str, 2);
        String msta = DeviceInfoStrUtil.getValue(str, 3);
        String temp = DeviceInfoStrUtil.getValue(str, 4);
        String hum = DeviceInfoStrUtil.getValue(str, 5);

        Log.i("==","电池电压："+volBat+"，太阳能电压："+volSun+",雨水："+msta+"，温度："+temp+"，湿度："+hum);

        ShareUtil.saveDeviceBatvol(volBat);
        ShareUtil.saveDeviceSunvol(volSun);
        ShareUtil.saveRain(msta);
        ShareUtil.saveTemp(temp);
        ShareUtil.saveHum(hum);

    }

    private void takePhoto() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CameraManager cameraManager = CameraManager.getInstance();
                    cameraManager.initCamera();
                    cameraManager.connectCamera();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cameraManager.captureExecute(AppContants.CaptureFrom.FROM_TASK);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    /**
     * 发送数据
     */
    public boolean send(String data) {
        Log.i(TAG, "发送给串口-->" + data);
        if (!isDeviceOpenFlag) {
            Log.i(TAG, "请先打开串口");
            return false;
        }
        if (null == data) {
            return false;
        }
        if (TextUtils.isEmpty(data)) {
            // getView().showOpenMsg("请输入要发送的信息");
            Log.i(TAG, "onSend: 发送内容为 null");
            return false;
        }
        return sendSure(data);
    }

    private boolean sendSure(String data) {
        if (TextUtils.equals(ShareUtil.getDeviceError(), "1")) {
            return false;
        } else {
            byte[] sendContentBytes = data.getBytes();
            boolean sendBytes = mSerialPortManager.sendBytes(sendContentBytes);
            Log.i(TAG, "onSend: sendBytes = " + sendBytes);
            return sendBytes;
        }
    }


/*    public void capture(String from) {
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

    */

    /**
     * 截图
     *
     * @param
     * @param
     *//*
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

            if (null!=onCaptureFinishListener){
                if (TextUtils.equals(from,FROM_Hand)) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    Bitmap bitmap = FileUtils.getPicFromBytes(sbuffer, options);
                    onCaptureFinishListener.finish(bitmap, file, picName);
                }
            }
            Log.i(TAG, "NET_DVR_CaptureJPEGPicture_NEW size!" + bytesRerned.iValue);
        }
    }*/
    public void destoryDevice() {
        if (mSerialPortManager != null) {
            mSerialPortManager.closeSerialPort();
            mSerialPortManager = null;
        }
    }

    @Override
    public void onSuccess(File device) {
        Log.i(TAG, String.format("串口 [%s] 打开成功", device.getPath()));
        isDeviceOpenFlag = true;
    }

    @Override
    public void onFail(File device, Status status) {
        isDeviceOpenFlag = false;
        switch (status) {
            case NO_READ_WRITE_PERMISSION:
                Log.i(TAG, device.getPath() + "--- 没有读写权限");
                break;
            case OPEN_FAIL:
            default:
                Log.i(TAG, device.getPath() + "--- 串口打开失败");
                break;
        }
    }

   /* //相机是否已打开
    public boolean isCaptureOpen() {
        boolean isOpen = false;
        if (!TextUtils.equals(ShareUtil.getDeviceStatue(),SerialInforStrUtil.STA_CLOSE_RESET)){
            isOpen = true;
        }
        return isOpen;
    }
    //持续查询相机是否已打开
    public boolean isCaptureOpenLong() {
        boolean isOpen = false;
        for (int i=1; i<5*60;i++){    //查询状态是否改变  若状态未改变休眠一秒继续查询
            if (TextUtils.equals(ShareUtil.getDeviceStatue(),SerialInforStrUtil.STA_CLOSE_RESET)){ //sta=0表示复位关机状态
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
    //持续查询相机是否登录成功（登录未成功则继续登录）
    public boolean loginCaptureLong(){
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
    //尝试登录摄像头
    public int loginCapture(){
       return login();
    }

    //相机是否已关闭
    public boolean isCaptureClose(){
        boolean isOpen = false;
            if (TextUtils.equals(ShareUtil.getDeviceStatue(),SerialInforStrUtil.STA_CLOSE_RESET)) {
                isOpen = true;
            }
        return isOpen;
    }
    //持续查询相机是否已关闭
    public boolean isCaptureCloseLong(){
        boolean isOpen = false;
        for (int i=1; i<5*60;i++){    //查询状态是否改变  若状态未改变休眠一秒继续查询
            if (!TextUtils.equals(ShareUtil.getDeviceStatue(),SerialInforStrUtil.STA_CLOSE_RESET)){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                isOpen = true;
            }
        }
        return isOpen;
    }
    */


    //发送命令打开相机
    public boolean openCaptureTurnPositive() {
        sendSure(SerialInforStrUtil.openCamTurnPositive());   //开启摄像头并翻转到正面
        return true;
    }

    //调节高度，必须在复位状态，若不在先强制复位
    public void setHighAfterReset(TaskQueue taskQueue, String highString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.equals(ShareUtil.getDeviceStatue(), SerialInforStrUtil.STA_CLOSE_RESET)) { //如果不在复位状态
                    taskQueue.add(new SeralTask(SerialInforStrUtil.getForceRestartStr())); //强制复位
                }
                for (int i = 0; i < 5 * 60; i++) {  //持续查询是否完成复位
                    if (TextUtils.equals(ShareUtil.getDeviceStatue(), SerialInforStrUtil.STA_CLOSE_RESET)) {
                        break;
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                taskQueue.add(new SeralTask(highString));   //发送高度调节指令
            }
        }).start();
    }

   /* //登录设备
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

    private OnCaptureFinishListener onCaptureFinishListener;
    public void setOnCaptureFinishListener(OnCaptureFinishListener onCaptureFinishListener){
        this.onCaptureFinishListener = onCaptureFinishListener;
    }
    public interface  OnCaptureFinishListener{
        void finish(Bitmap bitmap,File file,String name);
    }*/
}
