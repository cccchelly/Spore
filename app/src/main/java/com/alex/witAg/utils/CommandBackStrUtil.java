package com.alex.witAg.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-04-10.
 */

public class CommandBackStrUtil {
    static CommandBackStrUtil captureInfoStrUtil = null;

    private CommandBackStrUtil(){};

    public static CommandBackStrUtil getInstance(){
        if (captureInfoStrUtil==null){
            return  new CommandBackStrUtil();
        }else {
            return captureInfoStrUtil;
        }
    }
    /*
     硬件返回的数据前面可能有OK  需要判断如果有的话需要去掉
    OK{cmd:2,d:{STA:11,ERR:00}}*/
    public  Map<String,String> getBackMapInfo(String info){  //将返回的信息串拆分为map形式
        Map<String,String> map = new HashMap();
        if (TextUtils.isEmpty(info)){
            return map;
        }
        try {
            String[] strings;
            if (info.startsWith("OK")){
                strings = info.substring(12, info.length() - 4).split(",");
            }else {
                strings = info.substring(10, info.length() - 4).split(",");
            }

            for (String str:strings) {
                String[] keyVal = str.split(":");
                map.put(keyVal[0],keyVal[1]);
            }
        }catch (Exception e){}
        return map;
    }

    /*
    * flag    1 状态  2错误码
    **/
    public  String getCapValue(String backInfo,int flag ){  //返回值value
        String info = "";
        for (Map.Entry<String, String> entry :getBackMapInfo(backInfo) .entrySet()) {
            switch (flag){
                case 1:
                    if (TextUtils.equals(entry.getKey(),"STA")){
                        info = entry.getValue();
                    }
                    break;
                case 2:
                    if (TextUtils.equals(entry.getKey(),"ERR")){
                        info = entry.getValue();
                    }
                    break;
            }
        }
        return info;
    }

}
