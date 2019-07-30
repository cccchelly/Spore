package com.alex.witAg.presenter.viewImpl;

import android.app.Activity;

import com.alex.witAg.base.BaseMvpView;
import com.alex.witAg.base.IBaseView;

/**
 * Created by Administrator on 2018-11-06.
 */

public interface ITaskSettingNewView extends IBaseView {
    Activity getActivity();
    void finishActivity();
}
