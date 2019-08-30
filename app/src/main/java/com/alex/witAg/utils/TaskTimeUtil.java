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
 * 定时拍照任务工具类
 * 时间规则：设置时间段和间隔时间，在这个时间段内每隔间隔时间执行一次任务
 * 方案：根据设置的时间段个间隔，把每个要执行的时间点算出来存到本地，任务实时判断当前时间是否在本地时间点列表里存在
 */

public class TaskTimeUtil {
    private static volatile TaskTimeUtil taskTimeUtil = null;

    private TaskTimeUtil() {
    }

    public static TaskTimeUtil getInstance(){
        if (null == taskTimeUtil){
            synchronized (TaskTimeUtil.class){
                if (null == taskTimeUtil){
                    taskTimeUtil = new TaskTimeUtil();
                }
            }
        }
        return taskTimeUtil;
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
        temp.setDelay(taskTimeBean.getDelay());
        taskTimeBeans.add(temp);

        //保存添加后的时间区域列表
        setTimeAreaList(taskTimeBeans);
        //刷新本地时间点列表数据
        setMinList();
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
                    && temp.getEndTime() == taskTimeBean.getEndTime()
                    && temp.getDelay() == taskTimeBean.getDelay()){
                it.remove();
            }
        }

        Log.i("==timeaferdelete",taskTimeBeans.toString());
        //保存删除后的时间区域列表
        setTimeAreaList(taskTimeBeans);
        //刷新本地时间点列表数据
        setMinList();
    }

    public void clearTimes(){ //清除本地所有时间
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        setTimeAreaList(taskTimeBeans);
        setMinList();
    }

    private void setMinList() {
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(getTimeAreaList());
        List<Long> timeMin = new ArrayList<>();
        //计算出列表中的所有时间点
        for (TaskTimeBean tempTime : taskTimeBeans){
            long startTimeHM = tempTime.getStartTime();
            long endTimeHM = tempTime.getEndTime();
            long delayMils = tempTime.getDelay()*1000*60;

            if (endTimeHM <= startTimeHM){//结束时间的时分秒数小于开始时间，则时间跨过了8点，8点时间戳为0
                while (startTimeHM <= endTimeHM + 86400000){ //每次循环累加开始时间一个时间间隔，直到下一个时间点大于结束时间点加一天为止
                    timeMin.add(startTimeHM % 86400000);
                    startTimeHM += delayMils;
                }
            }else { //结束时间的时分秒数大于开始时间（没跨过8点）
                while (startTimeHM <= endTimeHM){ //每次循环累加开始时间一个时间间隔，直到下一个时间点大于结束时间点为止
                    timeMin.add(startTimeHM);
                    startTimeHM += delayMils;
                }
            }
        }

        Log.i("==timePoint==",timeMin.toString());
       for (long t : timeMin){
           Log.i("==timePoint2==",TimeUtils.millis2String(t,new SimpleDateFormat("HH:mm")));
       }
        //保存所有时间点列表
        setTimeMinList(timeMin);
    }

    public boolean isHaveTimePoint(long time){
        //本地保存的时间点的时间戳是从小时开始的，先把要比较的时间去掉天及以上的单位，再去掉秒数,再比较是否存在相应时间戳
        long timeNoMin = timeGetHM(time);
        if (getTimeMinList().contains(timeNoMin)){
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
        ShareUtil.setTimeAreaStr(strJson);
    }

    /**
     * 获取时间区域List
     * @return
     */
    public    List<TaskTimeBean> getTimeAreaList() {
        List<TaskTimeBean> datalist = new ArrayList<TaskTimeBean>();
        String strJson = ShareUtil.getTimeAreaStr();
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<TaskTimeBean>>() {
        }.getType());
        return datalist;
    }

    /**
     * 保存时间点List
     * @param datalist
     */
    private void setTimeMinList(List<Long> datalist) {
        if (null == datalist)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        ShareUtil.setTimeMinStr(strJson);
    }

    /**
     * 获取时间点List
     * @return
     */
    private   List<Long> getTimeMinList() {
        List<Long> datalist = new ArrayList<Long>();
        String strJson = ShareUtil.getTimeMinStr();
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<Long>>() {
        }.getType());
        return datalist;
    }


}
