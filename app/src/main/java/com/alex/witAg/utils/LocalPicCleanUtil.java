package com.alex.witAg.utils;

import android.os.Environment;
import android.util.Log;

import com.alex.witAg.bean.PicPathsBean;
import com.alex.witAg.utils.FileUtils;
import com.alex.witAg.utils.ToastUtils;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by apple on 2019/5/29.
 */

public class LocalPicCleanUtil {

    public static void doCleanIfNecessary(){
        cleanLog();
        if (!isNeedClean())
            return;

        List<PicPathsBean> picPaths = DataSupport.findAll(PicPathsBean.class);
        try{
            for (int i=0;i<2;i++){
                //数据库删除文件名   删除文件
                DataSupport.deleteAll(PicPathsBean.class, "path = ?", picPaths.get(i).getPath());
                FileUtils.deleteFile(FileUtils.getFileFromSdcard(picPaths.get(i).getPath()).getAbsolutePath());
            }
        }catch (Exception e){

        }

    }

    private static void cleanLog(){
        DeleteUtil.delete(Environment.getExternalStorageDirectory().getAbsolutePath(), false, ".log");
    }

    private  static boolean isNeedClean(){ //可用内存小于十分之一总内存则需要清理
        boolean isNeed = false;
        long total = MemoryUtil.getRomTotalSize();
        long ava = MemoryUtil.getRomAvailableSize();
        LogUtil.i(String.valueOf(total/ava));
        if (total/ava>=10){
            isNeed = true;
        }
        return  isNeed;
    }
}
