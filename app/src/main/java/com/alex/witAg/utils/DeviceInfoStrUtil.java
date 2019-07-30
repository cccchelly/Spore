package com.alex.witAg.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-08-27.
 */

public class DeviceInfoStrUtil {
    public static final String GET_DEVICE_MSG = "[REQEST_INFOR:1]";


    public static Map<String,String> getBackMapInfo(String info){  //将返回的信息串拆分为map形式
        Map<String,String> map = new HashMap();
        if (TextUtils.isEmpty(info)||!info.startsWith("[")||!info.endsWith("]")){
            return map;
        }
        String[] strings = info.substring(1, info.length() - 1).split(",");
        for (String str:strings) {
            String[] keyVal = str.split(":");
            map.put(keyVal[0],keyVal[1]);
        }
        return map;
    }

    /*
    * flag   [VOLBAT:12.1v,VOLSUN:21.1v,Msta:0,TEMP:21.0,hum:60.0%]       说明：
                      * 1.VOLBAT----蓄电池电压
                      * 2.VOLSUN----太阳能板子电压
                      * 3.Msta----雨水状态 其中：0---有雨水状态   1---无雨水状态
                      * 4.TEMP---温度，单位：摄氏度
                      * 5.hum-----湿度，单位RH
    **/
    public static String getValue(String backInfo,int flag ){  //返回值value
        String info = "";
        for (Map.Entry<String, String> entry :getBackMapInfo(backInfo) .entrySet()) {
            switch (flag){
                case 1:
                    if (TextUtils.equals(entry.getKey(),"VOLBAT")){
                        info = entry.getValue();
                    }
                    break;
                case 2:
                    if (TextUtils.equals(entry.getKey(),"VOLSUN")){
                        info = entry.getValue();
                    }
                    break;
                case 3:
                    if (TextUtils.equals(entry.getKey(),"Msta")){
                        info = entry.getValue();
                    }
                    break;
                case 4:
                    if (TextUtils.equals(entry.getKey(),"TEMP")){
                        info = entry.getValue();
                    }
                    break;
                case 5:
                    if (TextUtils.equals(entry.getKey(),"hum")){
                        info = entry.getValue();
                    }
                    break;
            }
        }
        return info;
    }
}
