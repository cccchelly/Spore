package com.alex.witAg.utils;

import com.google.common.base.MoreObjects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018-11-07.
 */

public class TimeBucket {

    private  static SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("HH:mm");
    //private static final ThreadLocal<DateFormat> FORMATS = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    private final Date start;

    private final Date end;

    public TimeBucket(Date start, Date end) {
        if (start.after(end)) {
            throw new IllegalArgumentException("时间段无效(开始日期需要小于结束日期)");
        }
        this.start = start;
        this.end = end;
    }

    public TimeBucket(String start, String end) throws ParseException {
        this(parse(start), parse(end));
    }

    public TimeBucket(long startTime, long endTime) {
        this(new Date(startTime), new Date(endTime));
    }

    /**
     * TimeBucket会返回重叠的时间段
     * 若返回null说明没有重叠的时间段
     *
     * @param buckets 时间段
     * @return
     */
    public static TimeBucket union(TimeBucket... buckets) {
        //长度为1无需判断
        if (buckets == null || buckets.length <= 1) {
            return null;
        }
        for (int i = 0; i < buckets.length - 1; i++) {
            long start = buckets[i].getStartTime();
            long end = buckets[i].getEndTime();
            for (int j = i + 1; j < buckets.length; j++) {
                if (buckets[j].getStartTime() > start) {
                    start = buckets[j].getStartTime();
                }
                if (buckets[j].getEndTime() < end) {
                    end = buckets[j].getEndTime();
                }
                if (start < end) {
                    return new TimeBucket(start, end);
                }
            }
        }
        return null;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public long getStartTime() {
        return start.getTime();
    }

    public long getEndTime() {
        return end.getTime();
    }

    private static Date parse(String str) throws ParseException {
        //return FORMATS.get().parse(str);
        return TimeUtils.string2Date(str,simpleDateFormat);
    }

    private static String format(Date str) {
        //return FORMATS.get().format(str);
        return TimeUtils.date2String(str,simpleDateFormat);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("start", format(start))
                .add("end", format(end))
                .toString();
    }

}
