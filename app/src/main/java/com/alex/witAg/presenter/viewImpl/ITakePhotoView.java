package com.alex.witAg.presenter.viewImpl;

import com.alex.witAg.base.IBaseView;
import com.kongqw.serialportlibrary.Device;

import java.util.ArrayList;

public interface ITakePhotoView extends IBaseView {
    void showDevices(ArrayList<Device> mDevices);

}
