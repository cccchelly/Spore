package com.alex.witAg.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2019/8/8.
 */

public class DeleteUtil {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     * 删除指定目录指定前后缀的文件
     *
     * @param dirPath
     * @param isPrefix
     * @param regEx
     */
    public static void delete(String dirPath, boolean isPrefix, String regEx) {
        executor.execute(new DeleteRunnable(dirPath, isPrefix, regEx));
    }
}
