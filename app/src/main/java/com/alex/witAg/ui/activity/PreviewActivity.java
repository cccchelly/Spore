package com.alex.witAg.ui.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.base.BaseActivity;
import com.alex.witAg.presenter.PreviewPresenter;
import com.alex.witAg.presenter.viewImpl.IPreviewView;
import com.alex.witAg.utils.LogUtil;
import com.alibaba.android.arouter.facade.annotation.Route;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Route(path = AppContants.ARouterUrl.PreviewActivity)
public class PreviewActivity extends BaseActivity<PreviewPresenter, IPreviewView> implements IPreviewView, SurfaceHolder.Callback {
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.preview_progressbar)
    SeekBar seekBar; // 曝光值

    cam ksjcam;
    Lock m_Lock;
    @BindView(R.id.preview_tv_curr_mode)
    TextView mTvCurrMode;
    @BindView(R.id.preview_tv_switch_mode)
    TextView mTvSwitchMode;
    @BindView(R.id.preview_lin_handle)
    LinearLayout mLinHandle;
    private String json;
    private CameraBean surfaceCamera;
    private SurfaceHolder m_PreviewHolder;
    BroadcastReceiver mUsbReceiver;

    Thread captureThread;

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

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        init();
        initProgressbar();
        setMode();
    }

    private void setMode() {
        if (surfaceCamera.isAutoExposure()){
            mTvCurrMode.setText("当前曝光模式：自动曝光");
            mTvSwitchMode.setText("切换到手动曝光");
            mLinHandle.setVisibility(View.GONE);
        }else {
            mTvCurrMode.setText("当前曝光模式：手动曝光");
            mTvSwitchMode.setText("切换到自动曝光");
            mLinHandle.setVisibility(View.VISIBLE);
        }
        m_Lock.lock();

        int exTime = surfaceCamera.getExposureTime();
        if (exTime == 0) {
            exTime = 200;
        }
        for (int i = 0; i < ksjcam.m_devicecount; i++) {
            if (!surfaceCamera.isAutoExposure()) {
                ksjcam.AEStart(i, 0, -1, AppContants.targetExposure);
                ksjcam.ExposureTimeSet(i, exTime);
            } else {
                ksjcam.AESetRegion(i,0,0,5472,3648);
                ksjcam.AESetExposureTimeRange(i,0,2000);
                //自动曝光开启
                //int nStart ： 1 开始自动曝光      int nMaxCount ：运算次数，一般单次写个20，如果是连续就-1，  int nTarget阈值(128-255)，一般128
                ksjcam.AEStart(i, 1, -1, AppContants.targetExposure);
            }
        }
        m_Lock.unlock();
    }

    private void initProgressbar() {
        seekBar.setMax(500);
        seekBar.setProgress(surfaceCamera.getExposureTime());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                LogUtil.i("进度" + progress);
                if (ksjcam == null)
                    return;
                changeExposureTime(progress);
            }
        });
    }

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int tellMeLayout() {
        return R.layout.activity_preview;
    }

    @Override
    protected PreviewPresenter initPresenter() {
        return new PreviewPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }


    public void init() {
        if (!checkAccess()) {
            if (upgradeRootPermission("/dev/bus/usb/")) {
                //root模式
                initData();
                if (ksjcam.m_devicecount <= 0) {
                    return;
                }
                //checkPermission();
            } else {
                // sb_switch.setStatus(false);    1
                ConfirmDialog dialog = new ConfirmDialog(App.getAppContext(), new Callback() {
                    @Override
                    public void callback(DialogButtonEnum position) {

                    }
                });
                dialog.setContent("请Root您的系统再进行测试。");
                dialog.show();
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
        //bayer   RGGB      白平衡  HWB_PRESETTINGS
        int exTime = surfaceCamera.getExposureTime();
        if (exTime == 0) {
            exTime = 200;
        }
        m_Lock.lock();
        for (int i = 0; i < ksjcam.m_devicecount; i++) {
            ksjcam.CaptureSetFieldOfView(i,0,0,AppContants.width,AppContants.height);
            ksjcam.SetParam(i,KSJ_PARAM.KSJ_RED.ordinal(),AppContants.red);
            ksjcam.SetParam(i,KSJ_PARAM.KSJ_GREEN.ordinal(),AppContants.green);
            ksjcam.SetParam(i,KSJ_PARAM.KSJ_BLUE.ordinal(),AppContants.blue);

            ksjcam.SetBayerMode(i, AppContants.defaultBayer);
            ksjcam.WhiteBalancePresettingSet(i,AppContants.WhiteBalancePresettingSetMode);
            ksjcam.WhiteBalanceSet(i, AppContants.defaultWb);
            ksjcam.SetTriggerMode(i,AppContants.defaultTriggerMode);
            ksjcam.CaptureSetFieldOfView(i,0,0,AppContants.width,AppContants.height);
        }
        m_Lock.unlock();
        surfaceCamera.setBayerMode(AppContants.defaultBayer);
        surfaceCamera.setExposureTime(exTime);
        surfaceCamera.setWhiteBalance(AppContants.defaultWb);
        surfaceCamera.setTriggerMode(AppContants.defaultTriggerMode);
        json = new Gson().toJson(surfaceCamera);
        SharedPreferencesManager.setParam(App.getAppContext(), "CAMERA_SURFACE", json);

        setMode();
        initPreview();
    }

    private void changeExposureTime(int value) {
        m_Lock.lock();
        for (int i = 0; i < ksjcam.m_devicecount; i++) {
            ksjcam.ExposureTimeSet(i, value);
        }
        m_Lock.unlock();
        surfaceCamera.setExposureTime(value);
        json = new Gson().toJson(surfaceCamera);
        SharedPreferencesManager.setParam(App.getAppContext(), "CAMERA_SURFACE", json);
    }

    public void unInit() {
        if (ksjcam != null) {
            ksjcam.UnInit();
        }
      /*  hd.removeCallbacksAndMessages(null);
        //非root模式
        if (!checkSuFile() && checkAccess()) {
           unregisterReceiver(mUsbReceiver);
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        captureThreadGo = false;
        unInit();
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

    public boolean upgradeRootPermission(String pkgCodePath) {
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
        LogUtil.i("==bayer==RGGB:" + KSJ_BAYERMODE.KSJ_RGGB_BGR32_FLIP.ordinal() + "--BGGR:" + KSJ_BAYERMODE.KSJ_BGGR_BGR32_FLIP.ordinal());

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
            LogUtil.i("==camraData==" + json);
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
            Log.e("TAG", "ksjcam.QueryFunction(i,0)==" + ksjcam.QueryFunction(i, 0));
            if (ksjcam.QueryFunction(i, 0) == 1) { //QueryFunction(i,0)  0代表查询是否是黑白相机   返回值是1说明是黑白相机 0是彩色相机
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

    private void initPreview() {
        /*if (captureThreadGo == true) {
            captureThreadGo = false;
            return;
        }*/
        ;

        Log.e("zhanwei", "UsbManager openDevice start");
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        int fd = -1;
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            Log.i("zhanwei", device.getDeviceName() + " " + Integer.toHexString(device.getVendorId()) +
                    " " + Integer.toHexString(device.getProductId()));

            manager.requestPermission(device, mPermissionIntent);
            UsbDeviceConnection connection = manager.openDevice(device);
            if (connection != null) {
                fd = connection.getFileDescriptor();
            } else
                Log.e("zhanwei", "UsbManager openDevice failed");
            break;
        }
        if (fd <= 0) return;
        ksjcam.PreInit(fd);
        captureThreadGo = true;


        m_PreviewHolder = surfaceView.getHolder();
        m_PreviewHolder.addCallback(this);

    }


    public void startCaptureThread(int camnum, final int width, final int height) {
        if (0 == camnum) return;

        Toast debugToast = Toast.makeText(getApplicationContext(), "预览成像开始", Toast.LENGTH_LONG);
        debugToast.show();
        captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (captureThreadGo) {
//                    captureAndshow_TwoStep(0,width,height);
                    Log.d("zhanwei", "before captureAndshow_Capture");

                    boolean isValid = m_PreviewHolder.getSurface().isValid();
                    LogUtil.i("==isav=="+isValid);
                    if (m_PreviewHolder.getSurface().isValid()) {
                        m_Lock.lock();

                        int code = ksjcam.CaptureBySurface(0, m_PreviewHolder.getSurface(), 0);// 0 no save 1 save bmp at /sdcard/
                        LogUtil.i("==capCode=="+code);
//                    ksjcam.CaptureBySurfaceSave(0,m_PreviewHolder.getSurface(),1,"/dev/capture.bmp");

                        m_Lock.unlock();
//                    captureAndshow_Capture(0, width, height);
                    }
                }

            }

        });
        Log.d("zhanwei", "startCaptureThread 0");
        captureThread.start();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCaptureThread(ksjcam.m_devicecount, m_nwidth, m_nheight); //130W
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        captureThreadGo = false;
        unInit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.preview_tv_switch_mode)
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.preview_tv_switch_mode:
                switchMode();
                break;
        }
    }

    private void switchMode() {
        boolean isAuto = surfaceCamera.isAutoExposure();
        surfaceCamera.setAutoExposure(!isAuto);
        json = new Gson().toJson(surfaceCamera);
        SharedPreferencesManager.setParam(App.getAppContext(), "CAMERA_SURFACE", json);
        setMode();
    }
}
