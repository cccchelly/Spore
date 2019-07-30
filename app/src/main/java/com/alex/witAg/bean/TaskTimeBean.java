package com.alex.witAg.bean;

import com.alex.witAg.utils.TimeUtils;

/**
 * Created by Administrator on 2018-11-05.
 */

public class TaskTimeBean {
    private Long startTime;
    private Long endTime;
    private Long delay;

    @Override
    public String toString() {
        return "TaskTimeBean{" +
                "startTime=" + TimeUtils.millis2String(startTime) +
                ", endTime=" + TimeUtils.millis2String(endTime) +
                ", delay=" + delay +
                '}';
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }
}
