package com.alex.witAg.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.base.BaseActivity;
import com.alex.witAg.presenter.DebugPresenter;
import com.alex.witAg.presenter.viewImpl.IDebugView;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.DeviceInfoStrUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Route(path = AppContants.ARouterUrl.DEBUG_ACTIVITY)
public class DebugActivity extends BaseActivity<DebugPresenter, IDebugView> implements IDebugView {


    @BindView(R.id.debug_btn_send_get_msg)
    Button mBtnSendGetMsg;
    @BindView(R.id.debug_btn_refresh_device_msg)
    Button mBtnRefreshDeviceMsg;
    @BindView(R.id.debug_tv_show_device_msg)
    TextView mTvShowDeviceMsg;

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {

    }

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int tellMeLayout() {
        return R.layout.activity_debug;
    }

    @Override
    protected DebugPresenter initPresenter() {
        return new DebugPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.debug_btn_send_get_msg, R.id.debug_btn_refresh_device_msg})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.debug_btn_send_get_msg:
                TaskQueue.getInstance().add(new SeralTask(DeviceInfoStrUtil.GET_DEVICE_MSG));
                break;
            case R.id.debug_btn_refresh_device_msg:
                mTvShowDeviceMsg.setText("VOLBAT="+ ShareUtil.getDeviceBatvol()+",VOLSUN="+ShareUtil.getDeviceSunvol()+",Msta="+ShareUtil.getRain()+
                        ",TEMP="+ShareUtil.getTemp()+",hum="+ShareUtil.getHum());
                break;
        }
    }
}
