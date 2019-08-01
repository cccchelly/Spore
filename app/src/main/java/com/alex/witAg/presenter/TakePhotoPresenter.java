package com.alex.witAg.presenter;

import com.alex.witAg.base.BasePresenter;
import com.alex.witAg.presenter.viewImpl.ITakePhotoView;
import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortFinder;

import java.util.ArrayList;

public class TakePhotoPresenter extends BasePresenter<ITakePhotoView> {
    private ArrayList<Device> mDevices;

    public void getDevices(){  //获取串口列表
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        mDevices = serialPortFinder.getDevices();
        if (mDevices == null || mDevices.size() == 0) return;
        getView().showDevices(mDevices);
    }

}
