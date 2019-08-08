package com.alex.witAg.taskqueue;

import android.util.Log;

import com.alex.witAg.App;
import com.alex.witAg.utils.CaptureTaskUtil;
import com.alex.witAg.utils.LogUtil;
import com.alex.witAg.utils.PostTaskMsgUtil;


/**
 * Created by Administrator on 2018-05-07.
 */

public  class SeralTask implements ITask{
    private String send;

    public SeralTask(String send){
        this.send = send;
        LogUtil.i("==命令放入任务栈=="+send);
    }

    @Override
    public void run() {
        App.setIsWaitTaskFinish(true);
        if (send.contains("STA:")){
            try {
                String sta = send.substring(4,6);
                App.setToDeviceSta(sta);
            }catch (Exception e){}
        }
        Log.i("==命令发给硬件==",send);
        PostTaskMsgUtil.instance().postMsg(1,send); //上传消息至后台
        if (!CaptureTaskUtil.instance().send(send)){ //发送消息到串口，如果发送失败则置反标志位认为本次任务完成
            App.setIsWaitTaskFinish(false);
        }
    }


}