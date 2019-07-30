package com.alex.witAg.camreaproxy;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by apple on 2019/6/18.
 */

public interface OnCaptureListener {
    void finish(Bitmap bitmap, File file, String name);
}
