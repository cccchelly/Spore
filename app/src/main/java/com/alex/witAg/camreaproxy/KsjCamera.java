package com.alex.witAg.camreaproxy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.utils.LogUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TimeUtils;
import com.alex.witAg.utils.ToastUtils;
import com.catchbest.Callback;
import com.catchbest.CameraBean;
import com.catchbest.ConfirmDialog;
import com.catchbest.DialogButtonEnum;
import com.catchbest.KSJ_BAYERMODE;
import com.catchbest.KSJ_PARAM;
import com.catchbest.KSJ_TRIGGRMODE;
import com.catchbest.KSJ_WB_MODE;
import com.catchbest.SharedPreferencesManager;
import com.catchbest.cam;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by apple on 2019/6/18.
 */

public class KsjCamera implements ICamera {
    OnCaptureListener onCaptureListener;

    @Override
    public void initCamera() {
        init();
    }

    @Override
    public void connectCamera() {
        //凯视佳相机不需要实现连接登录功能
    }

    @Override
    public void captureExecute(String from) {
        capture(from);
    }


    @Override
    public void unInitCamera() {
        unInit();
    }

    @Override
    public boolean isCameraOpen() {
        //硬件未支持凯视佳相机打开标志 默认相机已打开
        return true;
    }

    @Override
    public boolean isCameraConnect() {
        //凯视佳相机没有连接验证 默认已连接
        return true;
    }

    @Override
    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        this.onCaptureListener = onCaptureListener;
    }


    cam ksjcam;
    Lock m_Lock;
    private String json;
    private CameraBean surfaceCamera;

    private int currentWB = KSJ_WB_MODE.KSJ_SWB_AUTO_ONCE.ordinal();
    private int currentTrigger = KSJ_TRIGGRMODE.KSJ_TRIGGER_SOFTWARE.ordinal();
    private int currentBayer = KSJ_BAYERMODE.KSJ_BGGR_BGR32_FLIP.ordinal();

    private boolean isMirror = false;
    private boolean isLut = true;

    boolean captureThreadGo = false;//相机开启状态  true：开启  false：停止

    public int m_nwidth;
    public int m_nheight;
    public int m_x;
    public int m_y;

    public void init(){
        if(!checkAccess()) {
            if(upgradeRootPermission("/dev/bus/usb/")) {
                //root模式
                initData();
                if (ksjcam.m_devicecount <= 0) {
                    return;
                }
                //checkPermission();
            }else {
                ToastUtils.showToast("设备未Root");
                // sb_switch.setStatus(false);    1
                ConfirmDialog dialog = new ConfirmDialog(App.getAppContext(), new Callback() {
                    @Override
                    public void callback(DialogButtonEnum position) {

                    }
                });
                dialog.setContent("请Root您的系统再进行测试。");
                //dialog.show();
            }
        } else {
            //root模式
            initData();
            if (ksjcam.m_devicecount <= 0) {
                return;
            }
            //checkPermission();
        }
    }




    private void changeSetting() {
        //defaultBayer   BGGR      白平衡  HWB_PRESETTINGS
        int exTime = surfaceCamera.getExposureTime();
        if (exTime == 0){
            exTime = 200;
        }
        for(int i=0;i<ksjcam.m_devicecount;i++){
            ksjcam.CaptureSetFieldOfView(i,0,0,AppContants.width,AppContants.height);
            ksjcam.SetParam(i,KSJ_PARAM.KSJ_RED.ordinal(),AppContants.red);
            ksjcam.SetParam(i,KSJ_PARAM.KSJ_GREEN.ordinal(),AppContants.green);
            ksjcam.SetParam(i,KSJ_PARAM.KSJ_BLUE.ordinal(),AppContants.blue);

            ksjcam.SetBayerMode(i, AppContants.defaultBayer);

            if (!surfaceCamera.isAutoExposure()){
                ksjcam.AEStart(i, 0, -1, AppContants.targetExposure);
                ksjcam.ExposureTimeSet(i,exTime);
            }else {
                ksjcam.AESetRegion(i,0,0,5472,3648);
                ksjcam.AESetExposureTimeRange(i,0,2000);
                //自动曝光开启
                //int nStart ： 1 开始自动曝光      int nMaxCount ：运算次数，一般单次写个20，如果是连续就-1，  int nTarget阈值(128-255)，一般128
                ksjcam.AEStart(i,1,-1,AppContants.targetExposure);
            }
            ksjcam.WhiteBalancePresettingSet(i,AppContants.WhiteBalancePresettingSetMode);
            ksjcam.WhiteBalanceSet(i, AppContants.defaultWb);
            ksjcam.SetTriggerMode(i, AppContants.defaultTriggerMode);
        }
        surfaceCamera.setBayerMode(AppContants.defaultBayer);
        surfaceCamera.setExposureTime(exTime);
        surfaceCamera.setWhiteBalance(AppContants.defaultWb);
        surfaceCamera.setTriggerMode(AppContants.defaultTriggerMode);
        json = new Gson().toJson(surfaceCamera);
        SharedPreferencesManager.setParam(App.getAppContext(), "CAMERA_SURFACE", json);

    }

    public void unInit(){
        if (ksjcam != null) {
            ksjcam.UnInit();
        }
      /*  hd.removeCallbacksAndMessages(null);
        //非root模式
        if (!checkSuFile() && checkAccess()) {
           unregisterReceiver(mUsbReceiver);
        }*/
    }

    //用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    long timestamp;
    String time;
    /*String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/capture";
    File outfile = new File(path);*/

    public static  String from = "fromtask";

    public void capture(String from){

        new Thread(new Runnable() {
            @Override
            public void run() {
                KsjCamera.this.from = from;
                m_Lock.lock();
                countExposureTime();
                //                ksjcam.SetBayerMode(0,19);
                timestamp = System.currentTimeMillis();
                time = formatter.format(new Date());
                // 如果文件不存在，则创建一个新文件
              /*  if (!outfile.isDirectory()) {
                    try {
                        outfile.mkdir();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*/

                String deviceStatue = "";
                if (TextUtils.equals(ShareUtil.getDeviceStatue(),"1")){
                    deviceStatue = "-A";
                }else if (TextUtils.equals(ShareUtil.getDeviceStatue(),"2")){
                    deviceStatue = "-B";
                }else {
                    deviceStatue = "-O";
                }
                String picName = TimeUtils.millis2String(System.currentTimeMillis(),
                        new SimpleDateFormat("yyyyMMddHHmmss")) + deviceStatue + ".bmp";
                int code = ksjcam.CaptureBitmap(0, Environment.getExternalStorageDirectory()+"/"+ picName);
                Log.i("==captureCode==",code+"");
                File file = new File(Environment.getExternalStorageDirectory(),picName);
                Uri uri = Uri.fromFile(file);
                LogUtil.i("==path1=="+picName+"--path2=="+file.getAbsolutePath());
                //文件保存到内存   新摄像机截图就自动保存了 不用自己保存
                // FileUtils.saveContentToSdcard(picName,sbuffer);
                //文件地址保存到数据库
                PicPathsBean data = new PicPathsBean();
                data.setPath(picName);
                data.save();

                if (null!=onCaptureListener){
                    if (TextUtils.equals(from,AppContants.CaptureFrom.FROM_Hand)) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
                        if (bitmap!=null) {
                            onCaptureListener.finish(bitmap, file, picName);
                        }
                    }
                }
                // Log.i(TAG, "NET_DVR_CaptureJPEGPicture_NEW size!" + bytesRerned.iValue);
                LogUtil.i("==截图成功==");
       /* LogUtil.e("====code="+code);
        Log.i("==code==","code="+code);
        Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(path + "/catchbest" + time + ".bmp");
        Uri uri = Uri.fromFile(file);
        intent1.setData(uri);*/
                //sendBroadcast(intent1);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
                //                ksjcam.SetBayerMode(0,15);
                m_Lock.unlock();

                /**final Bitmap bitmap = acquireScreenshot(this);
                 //                Log.e("TAG", "width--" + bitmap.getWidth() + ",height--" + bitmap.getHeight());
                 if (bitmap != null) {
                 new Thread() {
                 public void run() {
                 saveImage(bitmap);
                 }
                 }.start();
                 }*/
            }
        }).start();
    }

    private void  countExposureTime(){
        int delayTime;
        if (TextUtils.equals(from,AppContants.CaptureFrom.FROM_Hand)){
            delayTime = 5*1000;
        }else {
            delayTime = 60*1000;
        }

        int count = 1;
        long inTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - inTime <= delayTime){
            ksjcam.CaptureRGBdataIntArray(0,5500,3700);
            LogUtil.i("计算曝光次数="+count);
            count++;
        }

    }


    /**
     * 判断手机手否可以访问adb ls dev/bus/usb
     *
     * @return
     */
    private boolean checkAccess() {
        //这个类是一个很好用的工具，java中可以执行java命令，android中可以执行shell命令
        Runtime mRuntime = Runtime.getRuntime();
        try {
//Process中封装了返回的结果和执行错误的结果
            Process mProcess = mRuntime.exec("ls /dev/bus/usb/ -al ");
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            StringBuffer mRespBuff = new StringBuffer();
            char[] buff = new char[1024];
            int ch = 0;
            while ((ch = mReader.read(buff)) != -1) {
                mRespBuff.append(buff, 0, ch);
            }
            mReader.close();
//            Log.e("TAG", "haha:" + mRespBuff.toString());
//            Toast.makeText(MainActivity.this, "haha:" + mRespBuff.toString(), Toast.LENGTH_SHORT).show();
//            mMsgText.setText(mRespBuff.toString());
//            if(mRespBuff != null) {
//                if(mRespBuff.toString().contains("daemon not running")) {
//                    Log.e("TAG", "su失败");
//                    return false;
//                } else {
//                    Log.e("TAG", "su成功");
//                    return true;
//                }
//            } else {
//                Log.e("TAG", "失败");
            return false;
//            }
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
//            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public  boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod -R 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("setenforce 0" + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {

            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    private void initData() {
        int[] widtharray = new int[1];
        int[] heightarray = new int[1];
        int[] xarray = new int[1];
        int[] yarray = new int[1];

        int[] deviceTypeArray = new int[1];
        int[] serialsArray = new int[1];
        int[] firmwareVersionArray = new int[1];
        LogUtil.i("==马上要初始化相机了==");

//        if (checkSuFile()) {
        //root模式
        ksjcam = new cam();
        ksjcam.Init();
//        }


        m_Lock = new ReentrantLock();

        ksjcam.m_devicecount = ksjcam.DeviceGetCount();

        Log.e("zhanwei", "ksjcam.m_devicecount = " + String.valueOf(ksjcam.m_devicecount));
        json = (String) SharedPreferencesManager.getParam(App.getAppContext(), "CAMERA_SURFACE", "");
        if (json.equals("")) {
            surfaceCamera = new CameraBean();
            surfaceCamera.setStartX(0);
            surfaceCamera.setStartY(0);
            surfaceCamera.setWidth(1280);
            surfaceCamera.setHeight(960);
            surfaceCamera.setBayerMode(KSJ_BAYERMODE.KSJ_BGGR_BGR32_FLIP.ordinal());
            surfaceCamera.setRedGain(48);
            surfaceCamera.setGreenGain(48);
            surfaceCamera.setBlueGain(48);
            surfaceCamera.setTriggerMode(KSJ_TRIGGRMODE.KSJ_TRIGGER_SOFTWARE.ordinal());
            surfaceCamera.setWhiteBalance(KSJ_WB_MODE.KSJ_HWB_PRESETTINGS.ordinal());
            surfaceCamera.setExposureTime(80);
            surfaceCamera.setAutoExposure(true);
            surfaceCamera.setMirror(false);
            surfaceCamera.setLut(true);
            surfaceCamera.setSensitivity(0);
            json = new Gson().toJson(surfaceCamera);
            LogUtil.i("==camraData=="+json);
            SharedPreferencesManager.setParam(App.getAppContext(), "CAMERA_SURFACE", json);
        } else {
            surfaceCamera = new Gson().fromJson(json, CameraBean.class);
        }


        currentWB = surfaceCamera.getWhiteBalance();
        currentTrigger = surfaceCamera.getTriggerMode();
        currentBayer = surfaceCamera.getBayerMode();

        isMirror = surfaceCamera.isMirror();
        isLut = surfaceCamera.isLut();

        for (int i = 0; i < ksjcam.m_devicecount; i++) {
            Log.e("TAG", "ksjcam.QueryFunction(i,0)==" + ksjcam.QueryFunction(i,0));
            if(ksjcam.QueryFunction(i,0) == 1) { //QueryFunction(i,0)  0代表查询是否是黑白相机   返回值是1说明是黑白相机 0是彩色相机
                captureThreadGo = false;
                /*sb_switch.setStatus(false);
                tv_fps.setText("fps:0.0");
                Intent intent = new Intent(this, ImageActivity.class);
                startActivity(intent);
                finish();*/
                return;
            }

            ksjcam.CaptureSetFieldOfView(i, surfaceCamera.getStartX(), surfaceCamera.getStartY(), surfaceCamera.getWidth(), surfaceCamera.getHeight());

            ksjcam.SetParam(i, KSJ_PARAM.KSJ_RED.ordinal(), surfaceCamera.getRedGain());
            ksjcam.SetParam(i, KSJ_PARAM.KSJ_GREEN.ordinal(), surfaceCamera.getGreenGain());
            ksjcam.SetParam(i, KSJ_PARAM.KSJ_BLUE.ordinal(), surfaceCamera.getBlueGain());

//            ksjcam.SetParam(i,KSJ_GAMMA.ordinal(),1);
//            ksjcam.SetParam(i,KSJ_BIN.ordinal(),1);
//            ksjcam.SetParam(i,KSJ_CONTRAST.ordinal(),1);
//            ksjcam.SetParam(i,KSJ_BRIGHTNESS.ordinal(),1);
//            ksjcam.SetParam(i,KSJ_EXPOSURE_LINES.ordinal(),200);
//            ksjcam.SetParam(i,KSJ_BLACKLEVEL.ordinal(),0);

            ksjcam.DeviceGetInformation(i, deviceTypeArray, serialsArray, firmwareVersionArray);
            int nSerial = serialsArray[0];
//            Log.e("zhanwei", "nSerial = " + String.valueOf(nSerial) + "   index = " + String.valueOf(i));

            ksjcam.CaptureGetFieldOfView(i, xarray, yarray, widtharray, heightarray);
//            ksjcam.CaptureGetSize(i,widtharray,heightarray);

            //触发模式
            ksjcam.SetTriggerMode(i, surfaceCamera.getTriggerMode());

            /*ksjcam.WhiteBalanceSet(i, surfaceCamera.getWhiteBalance());
            ksjcam.ExposureTimeSet(i, surfaceCamera.getExposureTime());
            ksjcam.SetBayerMode(i, surfaceCamera.getBayerMode());//10,11(倒像)，12，13,14,15*/

//            ksjcam.WhiteBalanceSet(i, KSJ_WB_MODE.KSJ_HWB_MANUAL.ordinal());



            ksjcam.SensitivitySetMode(i, surfaceCamera.getSensitivity());

        }

        m_nwidth = widtharray[0];
        m_nheight = heightarray[0];
        m_x = xarray[0];
        m_y = yarray[0];

//        Log.e("zhanwei", "m_nwidth = " + String.valueOf(m_nwidth));
//        Log.e("zhanwei", "m_nheight = " + String.valueOf(m_nheight));
//        Log.e("zhanwei", "m_x = " + String.valueOf(m_x));
//        Log.e("zhanwei", "m_y = " + String.valueOf(m_y));

        changeSetting();

    }


}
