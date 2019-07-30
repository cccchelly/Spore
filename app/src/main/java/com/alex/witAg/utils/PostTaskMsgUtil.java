package com.alex.witAg.utils;

import com.alex.witAg.App;
import com.alex.witAg.base.BaseObserver;
import com.alex.witAg.base.BaseResponse;
import com.alex.witAg.bean.PostMsgResultBean;
import com.alex.witAg.bean.PostTaskMsgBean;
import com.alex.witAg.http.AppDataManager;
import com.alex.witAg.http.network.Net;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018-07-16.
 */

public class PostTaskMsgUtil {
   private static PostTaskMsgUtil postTaskMsgUtil;

    private PostTaskMsgUtil(){}

    public static PostTaskMsgUtil instance(){
        if (postTaskMsgUtil ==null){
            synchronized (PostTaskMsgUtil.class){
                if (postTaskMsgUtil ==null){
                    postTaskMsgUtil = new PostTaskMsgUtil();
                }
            }
        }
        return postTaskMsgUtil;
    }

    public void postMsg(int type,String message){  //type   1.发送串口指令   2.接收到串口回调   3.上传照片   4.拍照
        PostTaskMsgBean postTaskMsgBean = new PostTaskMsgBean();
        postTaskMsgBean.setTimes(System.currentTimeMillis());
        postTaskMsgBean.setImei(AppMsgUtil.getIMEI(App.getAppContext()));
        postTaskMsgBean.setTypes(type);
        postTaskMsgBean.setMessage(message);

        AppDataManager.getInstence(Net.URL_KIND_COMPANY)
                .postTaskMsg(postTaskMsgBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<PostMsgResultBean>>() {
                    @Override
                    public void onSuccess(BaseResponse<PostMsgResultBean> response) {

                    }
                });
    }

}
