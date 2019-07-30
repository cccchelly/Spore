package com.alex.witAg.bean;

/**
 * Created by Administrator on 2018-07-16.
 */

public class PostTaskMsgBean {
    //type   1.发送串口指令   2.接收到串口回调   3.上传照片   4.拍照
    private String message;
    private int types;
    private String imei;
    private long  times;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTypes() {
        return types;
    }

    public void setTypes(int types) {
        this.types = types;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }
}
