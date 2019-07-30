/**
 * Title: CrashUtil.java
 * Package: com.hikvision.ivms4510hd.utils
 * Copyright: Hikvision Digital Technology Co., Ltd. All Right Reserved.
 * Address: http://www.hikvision.com
 * Description: 鏈唴瀹逛粎闄愪簬鏉窞娴峰悍濞佽鏁板瓧鎶�鏈偂浠芥湁闄愬叕鍙稿唴閮ㄤ娇鐢紝绂佹杞彂銆�
 * Author: chenhao17
 * Date: 2016-5-06-006
 * Version: 1.0
 */
package com.alex.witAg.ui.test;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.base.BaseObserver;
import com.alex.witAg.base.BaseResponse;
import com.alex.witAg.bean.PostMsgBean;
import com.alex.witAg.bean.PostMsgResultBean;
import com.alex.witAg.http.AppDataManager;
import com.alex.witAg.http.network.Net;
import com.alex.witAg.ui.activity.SplashActivity;
import com.alex.witAg.utils.AppMsgUtil;
import com.alex.witAg.utils.ShareUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Class: CrashUtil
 * Description:
 * Author: chenhao17
 * Time: 2016-5-06-006 19:36:15
 */
public class CrashUtil implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashUtil";

    private static final String SD_CARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SimpleDemo/crash";
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
  
    private final Map<String, String> infos = new HashMap<String, String>();
  
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
    
    private static CrashUtil mInstance = null;

    private Context mContext;

    public static CrashUtil getInstance()
    {
        if (null == mInstance)
        {
            synchronized (CrashUtil.class)
            {
                if (null == mInstance)
                {
                    mInstance = new CrashUtil();
                }
            }
        }
        return mInstance;
    }

    private CrashUtil ()
    {

    }

    public void init(Context context)
    {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context;
    }

    @Override
    public void uncaughtException (Thread thread, Throwable ex)
    {
        postError(ex);
        handleException(ex);
        restartApp(thread,ex);   //crash重启
        if (mDefaultCrashHandler != null)
        {
            SystemClock.sleep(500);
            mDefaultCrashHandler.uncaughtException(thread, ex);
        }
    }

    private void postError(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key);
            sb.append(" : ");
            sb.append(value);
            sb.append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null)
        {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();

        String result = writer.toString();
        sb.append("\n");
        sb.append(result);

        Log.i("==errorstr==",sb.toString());

        PostMsgBean postMsgBean = new PostMsgBean();
        postMsgBean.setSunvol(ShareUtil.getDeviceSunvol());
        postMsgBean.setBatvol(ShareUtil.getDeviceBatvol());
        postMsgBean.setHighsta(ShareUtil.getCaptureHignSta());
        postMsgBean.setSta(ShareUtil.getDeviceStatue());
        postMsgBean.setError(ShareUtil.getDeviceError());
        postMsgBean.setImei(AppMsgUtil.getIMEI(App.getAppContext()));
        postMsgBean.setLatitude(ShareUtil.getLatitude()+"");
        postMsgBean.setLongitude(ShareUtil.getLongitude()+"");
        postMsgBean.setFirstStart(false);
        postMsgBean.setErrorInfo(sb.toString());

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

    private boolean handleException (Throwable ex)
    {
        if (ex == null)
        {
            return false;
        }

        collectDeviceInfo();
        saveCrashInfoToFile(ex);
        return true;
    }

    private void restartApp(Thread thread,Throwable ex) {
        if (!handleException(ex) && mDefaultCrashHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            ex.printStackTrace();
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(mContext, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("crash", true);
            PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            System.gc();
        }
    }

    private void collectDeviceInfo ()
    {
        try
        {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null)
            {
                infos.put("App Version", pi.versionName + '_' + pi.versionCode + "\n");
                infos.put("OS Version", Build.VERSION.RELEASE + '_' + Build.VERSION.SDK_INT + "\n");
                infos.put("Device ID", Build.ID + "\n");
                infos.put("Device Serial", Build.SERIAL + "\n");
                infos.put("Manufacturer", Build.MANUFACTURER + "\n");
                infos.put("Model", Build.MODEL + "\n");
                infos.put("CPU ABI", Build.CPU_ABI + "\n");
                infos.put("Brand", Build.BRAND + "\n");
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e(TAG, "an error occurred when collect package info");
            e.printStackTrace();
        }

//        Field[] fields = Build.class.getDeclaredFields();
//        for (Field field : fields)
//        {
//            try
//            {
//                field.setAccessible(true);
//                infos.put(field.getName(), field.get(null).toString());
//                LogUtil.d(field.getName() + " : " + field.get(null));
//            }
//            catch (IllegalAccessException e)
//            {
//                LogUtil.e("an error occured when collect crash info");
//                e.printStackTrace();
//            }
//        }
    }

    private String saveCrashInfoToFile (Throwable ex)
    {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key);
            sb.append(" : ");
            sb.append(value);
            sb.append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null)
        {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();

        String result = writer.toString();
        sb.append("\n");
        sb.append(result);

        try
        {
            long currentTime = System.currentTimeMillis();
            String time = formatter.format(new Date(currentTime));
            String fileName = "crash_" + time + "_" + currentTime + ".log";

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                File dir = new File(SD_CARD_PATH);
                if (!dir.exists())
                {
                    boolean s = dir.mkdirs();
                    System.out.println(s);
                }
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(SD_CARD_PATH + "/" + fileName);
                    fileOutputStream.write(sb.toString().getBytes());
                    fileOutputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            return fileName;
        }
        catch (Exception e)
        {
            Log.e(TAG, "an error occurred while writing file...");
            e.printStackTrace();
        }

        return "";
    }
}
