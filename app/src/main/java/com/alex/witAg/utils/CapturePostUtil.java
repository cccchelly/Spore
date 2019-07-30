package com.alex.witAg.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.base.BaseObserver;
import com.alex.witAg.base.BaseResponse;
import com.alex.witAg.base.BaseResponseObserver;
import com.alex.witAg.bean.GetTokenBean;
import com.alex.witAg.bean.PicMessageBean;
import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.bean.QiNiuTokenBean;
import com.alex.witAg.http.AppDataManager;
import com.alex.witAg.http.network.Net;
import com.qiniu.android.common.FixedZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * Created by Administrator on 2018/3/31.
 */
public class CapturePostUtil {

    public static void findLocalPic(){
        List<PicPathsBean> picPaths = DataSupport.findAll(PicPathsBean.class);
        Log.i(TAG,"图片数="+picPaths.size());

        PostTaskMsgUtil.instance().postMsg(3,"本地图片数="+picPaths.size());

        if (picPaths.size()>0){
            String path = picPaths.get(0).getPath();
            try{
                File file = FileUtils.getFileFromSdcard(path);
                Login(file,path);   //重新获取token防止token失效
            }catch (NullPointerException e){
                //未找到图片（如人为删除了图片），从数据库清除图片地址
                //数据库删除文件名
                DataSupport.deleteAll(PicPathsBean.class,"path = ?",path);
                findLocalPic();
            }
        }
    }
    private static String TAG =CapturePostUtil.class.getName();
    //七牛初始化
    static Configuration config =
            new Configuration.Builder()
                    /*.chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
                    .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
                    .connectTimeout(10)           // 链接超时。默认10秒
                    .useHttps(true)               // 是否使用https上传域名
                    .responseTimeout(60)          // 服务器响应超时。默认60秒
                    .recorder(recorder)           // recorder分片上传时，已上传片记录器。默认null
                    .recorder(recorder, keyGen)   // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录*/
                    .zone(FixedZone.zone2)        // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
                    .build();

    // 初始化、执行上传
    private volatile boolean isCancelled = false;    //要取消上传时置为true

    public static void postPic(File file, String picName){
        //getView().showLoadingView("图片上传中...");

        AppDataManager.getInstence(Net.URL_KIND_COMPANY)
                .getQiNiuToken(ShareUtil.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<QiNiuTokenBean>>() {
                    @Override
                    public void onSuccess(BaseResponse<QiNiuTokenBean> response) {
                        QiNiuTokenBean qiNiuTokenBean = response.getData();
                        Log.i(TAG,"获取七牛token："+qiNiuTokenBean.toString());
                        //postToQiNiu(file,picName,qiNiuTokenBean.getToken());
                        postToQiniuCompress(file,picName,qiNiuTokenBean.getToken());   //先压缩图片再上传七牛

                    }
                });
    }

    private static void Login(File file, String picName){
        //获取token
        AppDataManager.getInstence(Net.URL_KIND_BASE)
                .getToken(AppMsgUtil.getIMEI(App.getAppContext()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<GetTokenBean>>() {
                    @Override
                    public void onSuccess(BaseResponse<GetTokenBean> response) {
                        Log.i("==gettoken==",response.toString());
                        if (response.getCode()==BaseResponse.RESULT_CODE_SUCCESS){
                            //得到token
                            ShareUtil.saveToken(response.getData().getToken());
                            postPic(file,picName);
                        }else if (response.getCode()>0){
                            //ToastUtils.showToast("获取token错误："+response.getMsg());
                        }
                    }
                });
    }

    static void postToQiniuCompress(File data, String name, String token){
        PostTaskMsgUtil.instance().postMsg(3,"上传照片到七牛,照片名:"+name);
        Bitmap image = BitmapFactory.decodeFile(data.getAbsolutePath());

        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
                int options = 100;
                //while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
                baos.reset(); // 重置baos即清空baos
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
                //options -= 10;// 每次都减少10
                //}
                ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
                Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
                saveImg(name,bitmap,getNewPicName(name) ,App.getAppContext(),token);
            }
        }).start();

    }

    private static String getNewPicName(String oldName){
        String[] s = oldName.split("\\.");
        String name = s[0];
        String[] nameArr = name.split("-");
        StringBuffer sb = new StringBuffer();
        long mills = TimeUtils.string2Millis(nameArr[0],new SimpleDateFormat("yyyyMMddHHmmss"));
        String newTime = TimeUtils.millis2String(mills+1000,new SimpleDateFormat("yyyyMMddHHmmss"));
        sb.append(newTime);
        sb.append("-");
        sb.append(nameArr[1]+".bmp");
        return  sb.toString();
    }

    /** 保存方法 */


    public static boolean saveImg(String oldName,Bitmap bitmap, String newName, Context context,String token) {
        try {
            String dir = Environment.getExternalStorageDirectory().getAbsolutePath();                   //图片保存的文件夹名
            File file = new File(dir);                                 //已File来构建
            if (!file.exists()) {                                     //如果不存在  就mkdirs()创建此文件夹
                file.mkdirs();
            }
            Log.i("SaveImg", "file uri==>" + dir);
            File mFile = new File(dir + "/"+newName);                        //将要保存的图片文件
            if (!mFile.exists()) { //文件不存在才压缩并生成文件，已存在则表示之前已经有压缩文件，上传过程出了问题，直接取出来上传
                FileOutputStream outputStream = new FileOutputStream(mFile);     //构建输出流
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);  //compress到输出outputStream
                Uri uri = Uri.fromFile(mFile);                                  //获得图片的uri
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)); //发送广播通知更新图库，这样系统图库可以找到这张图片

            }


            UploadManager uploadManager = new UploadManager(config);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            uploadManager.put(mFile, newName, token,
                                    new UpCompletionHandler() {
                                        @Override
                                        public void complete(String key, ResponseInfo info, JSONObject res) {
                                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                                            if(info.isOK()) {
                                                Log.i(TAG, "Upload Success");
                                                PicMessageBean messageBean = new PicMessageBean();
                                                //messageBean.setDeviceId(ShareUtil.getLoginId());
                                                //messageBean.setName(key.toString());
                                                messageBean.setName(oldName);
                                                messageBean.setUrl(key.toString());
                                                postPic(messageBean,oldName,mFile);
                                                //getView().dissmissLoadingView();
                                            } else {
                                                Log.i(TAG, "Upload Fail");
                                                //getView().dissmissLoadingView();
                                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                            }
                                            Log.i(TAG, key + ",\r\n " + info + ",\r\n " + res);
                                        }
                                    },new UploadOptions(null, null, false,
                                            new UpProgressHandler(){
                                                public void progress(String key, double percent){
                                                    Log.i(TAG, key + ": " + percent);
                                                }
                                            }, null));
                        }
                    }
            ).start();

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    static void postToQiNiu(File data, String name, String token){
        PostTaskMsgUtil.instance().postMsg(3,"上传照片到七牛,照片名:"+name);

        UploadManager uploadManager = new UploadManager(config);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        uploadManager.put(data, name, token,
                                new UpCompletionHandler() {
                                    @Override
                                    public void complete(String key, ResponseInfo info, JSONObject res) {
                                        //res包含hash、key等信息，具体字段取决于上传策略的设置
                                        if(info.isOK()) {
                                            Log.i(TAG, "Upload Success");
                                            PicMessageBean messageBean = new PicMessageBean();
                                            //messageBean.setDeviceId(ShareUtil.getLoginId());
                                            messageBean.setName(key.toString());
                                            messageBean.setUrl(key.toString());
                                            postPic(messageBean,name,null);
                                            //getView().dissmissLoadingView();
                                        } else {
                                            Log.i(TAG, "Upload Fail");
                                            //getView().dissmissLoadingView();
                                            //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                        }
                                        Log.i(TAG, key + ",\r\n " + info + ",\r\n " + res);
                                    }
                                },new UploadOptions(null, null, false,
                                        new UpProgressHandler(){
                                            public void progress(String key, double percent){
                                                Log.i(TAG, key + ": " + percent);
                                            }
                                        }, null));
                    }
                }
        ).start();
    }

   public static void postPic(PicMessageBean messageBean, String picName,File comFile){
       PostTaskMsgUtil.instance().postMsg(3,"上传照片至服务器,照片名："+picName);

        AppDataManager.getInstence(Net.URL_KIND_COMPANY)
                .postDevicePic(messageBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseResponseObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody response) {
                        ToastUtils.showToast("图片:" + picName + "上传成功");
                        try {
                            Log.i("==fileName==",picName);
                            //数据库删除文件名   删除文件
                            DataSupport.deleteAll(PicPathsBean.class, "path = ?", picName);
                            FileUtils.deleteFile(FileUtils.getFileFromSdcard(picName).getAbsolutePath());
                            if (comFile != null){
                                FileUtils.deleteFile(comFile);
                            }
                            findLocalPic();   //递归上传，每次上传第一张图片，完成后删除图片继续上传直到全部上传完毕。
                            //Log.i("==fileAbsName==",FileUtils.getFileFromSdcard(picName).getAbsolutePath());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

    }

}
