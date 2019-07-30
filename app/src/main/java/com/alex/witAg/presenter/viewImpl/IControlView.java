package com.alex.witAg.presenter.viewImpl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.alex.witAg.adapter.DeviceAdapter;
import com.alex.witAg.base.IBaseView;
import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dth
 * Des:
 * Date: 2018-03-08.
 */

public interface IControlView extends IBaseView{
    Activity getACtivity();
    void showDialog(DeviceAdapter mDeviceAdapter, SerialPortManager mSerialPortManager);
    void showOpenMsg(String msg);
    void showCapture(Bitmap bitmap);
    void showSeraStatus(String sta);
    void setSwtBtnChecked(boolean checked);

    void showDevices(ArrayList<Device> mDevices);
}
