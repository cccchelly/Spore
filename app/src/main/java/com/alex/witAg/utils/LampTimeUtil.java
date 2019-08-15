package com.alex.witAg.utils;

import android.util.Log;

import com.alex.witAg.bean.TaskTimeBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
* 控制灯管时间的工具类
* 灯管时间规则：设置时间区间，在每个时间区间开始时发送打开命令，时间结束时发送关闭命令。可以设置多个区域
* 这里采取的方案是：将时间区域列表拆开成开始时间组成一个列表，结束时间组成一个列表。
* 定时任务实时判断当前时间是否在开始时间列表中或者在结束时间列表中
* */
public class LampTimeUtil {
    private static volatile LampTimeUtil lampTimeUtil;

    private LampTimeUtil() {
    }

    public static LampTimeUtil getInstance(){
        if (null == lampTimeUtil){
            synchronized (LampTimeUtil.class){
                if (null == lampTimeUtil){
                    lampTimeUtil = new LampTimeUtil();
                }
            }
        }
        return lampTimeUtil;
    }


    public  boolean addTime(TaskTimeBean taskTimeBean){
        if (isNewTimeExist(taskTimeBean)){
            Log.i("==time","时间段"+taskTimeBean.toString()+",与已有时间重叠，不再添加时间");
            return false;
        }
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(getTimeAreaList());

        //把时间只剩下时分再保存
        TaskTimeBean temp = new TaskTimeBean();
        temp.setStartTime(timeGetHM(taskTimeBean.getStartTime()));
        temp.setEndTime(timeGetHM(taskTimeBean.getEndTime()));
        taskTimeBeans.add(temp);

        //保存添加后的时间区域列表
        setTimeAreaList(taskTimeBeans);
        //刷新本地时间点列表数据  改成只配置了
        //setMinList();
        return true;
    }

    public void deleteTime(TaskTimeBean taskTimeBean){
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(getTimeAreaList());
        Log.i("==timeBeforDelete",getTimeAreaList().toString());

        Iterator<TaskTimeBean> it = taskTimeBeans.iterator();
        while(it.hasNext()){
            TaskTimeBean temp = it.next();
            if (temp.getStartTime() == taskTimeBean.getStartTime()
                    && temp.getEndTime() == taskTimeBean.getEndTime()){
                it.remove();
            }
        }

        Log.i("==timeaferdelete",taskTimeBeans.toString());
        //保存删除后的时间区域列表
        setTimeAreaList(taskTimeBeans);
        //刷新本地时间点列表数据   改成只配置了
        //setMinList();
    }

    private void setMinList() {
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(getTimeAreaList());

        List<Long> timeOpenMin = new ArrayList<>();
        List<Long> timeCloseMin = new ArrayList<>();
        //分开出列表中的所有时间点
        for (TaskTimeBean tempTime : taskTimeBeans){
            timeOpenMin.add(tempTime.getStartTime());
            timeCloseMin.add(tempTime.getEndTime());
        }

        Log.i("==timeOpenPoint==",timeOpenMin.toString());
        Log.i("==timeClosePoint==",timeCloseMin.toString());
        for (long t : timeOpenMin){
            Log.i("==timeOpenPoint2==",TimeUtils.millis2String(t,new SimpleDateFormat("HH:mm")));
        }
        for (long t : timeCloseMin){
            Log.i("==timeclosePoint2==",TimeUtils.millis2String(t,new SimpleDateFormat("HH:mm")));
        }
        //保存所有开始和结束时间点列表
        setStartTimeMinList(timeOpenMin);
        setEndTimeMinList(timeCloseMin);
    }

    //是否存在开启时间
    public boolean isHaveOpenTimePoint(long time){
        //本地保存的时间点的时间戳是从小时开始的，先把要比较的时间去掉天及以上的单位，再去掉秒数,再比较是否存在相应时间戳
        long timeNoMin = timeGetHM(time);
        if (getStartTimeMinList().contains(timeNoMin)){
            return true;
        }else {
            return false;
        }
    }
    //是否存在关闭时间
    public boolean isHaveCloseTimePoint(long time){
        //本地保存的时间点的时间戳是从小时开始的，先把要比较的时间去掉天及以上的单位，再去掉秒数,再比较是否存在相应时间戳
        long timeNoMin = timeGetHM(time);
        if (getEndTimeMinList().contains(timeNoMin)){
            return true;
        }else {
            return false;
        }
    }

    public boolean isNewTimeExist(TaskTimeBean newTimeBean){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        boolean isExist = false;
        long newStartTime = timeGetHM(newTimeBean.getStartTime());
        long newEndTime = timeGetHM(newTimeBean.getEndTime());

        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(getTimeAreaList());
        for (TaskTimeBean t : taskTimeBeans){
            try {
                TimeBucket[] buckets = {
                        new TimeBucket(TimeUtils.millis2String(t.getStartTime(),simpleDateFormat), TimeUtils.millis2String(t.getEndTime(),simpleDateFormat)),
                        new TimeBucket(TimeUtils.millis2String(newTimeBean.getStartTime(),simpleDateFormat), TimeUtils.millis2String(newTimeBean.getEndTime(),simpleDateFormat))
                };
                TimeBucket union = TimeBucket.union(buckets);
                if (null != union) {
                    Log.i("==time_is_exist==","存在重叠区域,重叠时间段:" + union.toString());
                    isExist = true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            /*if ((newStartTime < t.getStartTime() && newEndTime > t.getEndTime()) ||
                    newStartTime > t.getStartTime() && newEndTime < t.getEndTime() ||
                    newStartTime > t.getStartTime() && newEndTime > t.getEndTime() && newStartTime < t.getEndTime() ||
                    newStartTime < t.getStartTime() && newEndTime < t.getEndTime() && newEndTime > t.getStartTime() ||
                    newStartTime == t.getStartTime() && newEndTime == t.getEndTime()){
                isExist = true;*/
        }
        return isExist;
    }

    public  boolean isTimeRight(TaskTimeBean taskTimeBean){ //检验时间是否是由小到大
        Long startTime = taskTimeBean.getStartTime();
        Long endTime = taskTimeBean.getEndTime();
        Log.i("==timeprase","开始时间="+startTime+",结束时间="+endTime);
        if (startTime < endTime){
            return true;
        }else {
            return false;
        }
    }


    private long timeGetHM(long timeMills){ //时间戳只保留小时和分钟部分，去掉其他的
        long timeHMS = timeMills%(1000 * (60 * 60 * 24));
        long timeHM = (timeHMS / (60 * 1000)) * (60 * 1000);

        return timeHM;
    }

    /**
     * 保存时间区域List
     * @param datalist
     */
    private void setTimeAreaList(List<TaskTimeBean> datalist) {
        if (null == datalist)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        ShareUtil.setLampTimeAreaStr(strJson);
    }

    /**
     * 获取时间区域List
     * @return
     */
    public    List<TaskTimeBean> getTimeAreaList() {
        List<TaskTimeBean> datalist = new ArrayList<TaskTimeBean>();
        String strJson = ShareUtil.getLampTimeAreaStr();
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<TaskTimeBean>>() {
        }.getType());
        return datalist;
    }



    /**
     * 保存开启时间点List
     * @param datalist
     */
    private void setStartTimeMinList(List<Long> datalist) {
        if (null == datalist)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        ShareUtil.setOpenTimeMinStr(strJson);
    }
    /**
     * 保存关闭时间点List
     * @param datalist
     */
    private void setEndTimeMinList(List<Long> datalist) {
        if (null == datalist)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        ShareUtil.setCloseTimeMinStr(strJson);
    }

    /**
     * 获取开启时间点List
     * @return
     */
    private   List<Long> getStartTimeMinList() {
        List<Long> datalist = new ArrayList<Long>();
        String strJson = ShareUtil.getOpenTimeMinStr();
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<Long>>() {
        }.getType());
        return datalist;
    }

    /**
     * 获取关闭时间点List
     * @return
     */
    private   List<Long> getEndTimeMinList() {
        List<Long> datalist = new ArrayList<Long>();
        String strJson = ShareUtil.getCloseTimeMinStr();
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<Long>>() {
        }.getType());
        return datalist;
    }


}
