package com.alex.witAg.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.base.BaseFragment;
import com.alex.witAg.presenter.BaseMsgPresenter;
import com.alex.witAg.presenter.viewImpl.IBaseMsgView;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.AppMsgUtil;
import com.alex.witAg.utils.AppUpdateUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TimeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseMsgFragment extends BaseFragment<BaseMsgPresenter, IBaseMsgView> implements IBaseMsgView {


    @BindView(R.id.base_msg_tv_refresh)
    TextView mTvRefresh;
    @BindView(R.id.base_msg_tv_time)
    TextView mTvTime;
    @BindView(R.id.base_msg_tv_bat_power)
    TextView mTvBatPower;
    @BindView(R.id.base_msg_tv_sun_power)
    TextView mTvSunPower;
    @BindView(R.id.base_msg_tv_temp)
    TextView mTvTemp;
    @BindView(R.id.base_msg_tv_hum)
    TextView mTvHum;
    @BindView(R.id.base_msg_tv_rain)
    TextView mTvRain;
    @BindView(R.id.base_msg_tv_error)
    TextView mTvError;
    Unbinder unbinder;
    @BindView(R.id.base_msg_tv_version)
    TextView mTvVersion;
    @BindView(R.id.base_msg_tv_update)
    TextView mTvUpdate;

    @Override
    protected void fetchData() {

    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_base_msg;
    }

    private void setStatues() {
        mTvVersion.setText("当前版本："+ AppMsgUtil.getVerName(getActivity()));

        mTvBatPower.setText(ShareUtil.getDeviceBatvol());
        mTvSunPower.setText(ShareUtil.getDeviceSunvol());
        mTvError.setText("当前状态码："+ShareUtil.getDeviceStatue()+",错误码：" + ShareUtil.getDeviceError());
        /*RAIN:0--雨控（1-有雨，0-无雨）*/
        if (TextUtils.equals(ShareUtil.getRain(), "1")) {
            mTvRain.setText("有雨水");
        } else if (TextUtils.equals(ShareUtil.getRain(), "0")) {
            mTvRain.setText("无雨水");
        }
        mTvTemp.setText(ShareUtil.getTemp() + "℃");
        mTvHum.setText(ShareUtil.getHum() + "RH");
    }

    @Override
    protected BaseMsgPresenter initPresenter() {
        return new BaseMsgPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    //刷新数据
    public void refreshData() {
        mTvTime.setText(TimeUtils.millis2String(System.currentTimeMillis()));
        TaskQueue.getInstance().add(new SeralTask(AppContants.commands.qingqiuxinxi));
        setStatues();
    }

    @OnClick({R.id.base_msg_tv_version, R.id.base_msg_tv_update,R.id.base_msg_tv_refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.base_msg_tv_refresh:
                refreshData();
                break;
            case R.id.base_msg_tv_version:
                mTvVersion.setText("当前版本："+ AppMsgUtil.getVerName(getActivity()));
                break;
            case R.id.base_msg_tv_update:
                AppUpdateUtil.check(true, true, false, true, true, 998, App.getAppContext(),true);    //检查新版本
                break;
        }
    }
}
