package com.alex.witAg.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-08-27.
 */

public class DeviceInfoStrUtil {
    public static final String GET_DEVICE_MSG = "[REQEST_INFOR:1]";


    /*{cmd:1,data:{sunVol:00.1,batvol:00.2,temp:000.1,humidity:00.2,RAIN:0}}*/
    /*原本是返回上面的数据  因为串口长度限制问题给简写方式
     硬件返回的数据前面可能有OK  需要判断如果有的话需要去掉
    * OK{cmd:1,d:{s:00.1,b:00.2,t:000.1,h:00.2,r:0}}
    * */
    public static Map<String,String> getBackMapInfo(String info){  //将返回的信息串拆分为map形式
        Map<String,String> map = new HashMap();
        if (TextUtils.isEmpty(info)){
            return map;
        }
        try {
            String[] strings;
            if (info.startsWith("OK")) {
                strings = info.substring(12, info.length() - 4).split(",");
            }else {
                strings = info.substring(10, info.length() - 4).split(",");
            }
            for (String str:strings) {
                String[] keyVal = str.split(":");
                map.put(keyVal[0],keyVal[1]);
            }
        }catch (Exception e){

        }
        return map;
    }

    /*
    * {cmd:1,d:{s:00.1,b:00.2,t:000.1,h:00.2,r:0}}
    *sunVol:00.1--太阳板电压
    batvol:00.2--蓄电池电压temp:000.1--当前温度
    humidity:00.2--当前湿度
    RAIN:0--雨控（1-有雨，0-无雨）
    * */
    public static String getValue(String backInfo,int flag ){  //返回值value
        String info = "";
        for (Map.Entry<String, String> entry :getBackMapInfo(backInfo) .entrySet()) {
            switch (flag){
                case 1:
                    if (TextUtils.equals(entry.getKey(),"b")){
                        info = entry.getValue();
                    }
                    break;
                case 2:
                    if (TextUtils.equals(entry.getKey(),"s")){
                        info = entry.getValue();
                    }
                    break;
                case 3:
                    if (TextUtils.equals(entry.getKey(),"r")){
                        info = entry.getValue();
                    }
                    break;
                case 4:
                    if (TextUtils.equals(entry.getKey(),"t")){
                        info = entry.getValue();
                    }
                    break;
                case 5:
                    if (TextUtils.equals(entry.getKey(),"h")){
                        info = entry.getValue();
                    }
                    break;
            }
        }
        return info;
    }
}
