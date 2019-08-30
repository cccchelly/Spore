package com.alex.witAg.bean;

import java.util.List;

/**
 * Created by Administrator on 2018-05-03.
 */

public class MqttMsgBean {

    /**
     * type : android_control
     * cmd : time_control
     * control : [{"startTime":"2333333333333","endTime":"5555555555555","delay":"20"},{"startTime":"2333333333333","endTime":"5555555555555","delay":"140"}]
     */

    private String type;
    private String cmd;
    private List<TaskTimeBean> control;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public List<TaskTimeBean> getControl() {
        return control;
    }

    public void setControl(List<TaskTimeBean> control) {
        this.control = control;
    }

}
