package com.alex.witAg.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.alex.witAg.R;
import com.alex.witAg.base.BaseFragment;
import com.alex.witAg.presenter.DataPresenter;
import com.alex.witAg.presenter.viewImpl.IDataView;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TimeUtils;
import com.alex.witAg.view.EaseSwitchButton;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by dth
 * Des:
 * Date: 2018-01-25.
 */

public class DataFragment extends BaseFragment<DataPresenter, IDataView> implements IDataView {
    private final String TAG = DataFragment.class.getName();
    @BindView(R.id.data_tv_dark_capture_status)
    TextView tvCaptureStatus;
    @BindView(R.id.sb_light)
    EaseSwitchButton sbCapture;
    @BindView(R.id.data_tv_batvol_status)
    TextView tvBatvolStatus;
    @BindView(R.id.data_tv_sunvvol_status)
    TextView tvSunvvolStatus;
    @BindView(R.id.data_tv_dianji_status)
    TextView tvDianjiStatus;
    @BindView(R.id.data_tv_position)
    TextView tvPosition;
    Unbinder unbinder;
    @BindView(R.id.main_data_tv_refresh)
    TextView mTvRefresh;
    @BindView(R.id.main_data_tv_last_time)
    TextView mTvLastTime;
    @BindView(R.id.data_tv_msta)
    TextView mTvMsta;
    @BindView(R.id.data_tv_temp)
    TextView mTvTemp;
    @BindView(R.id.data_tv_hum)
    TextView mTvHum;

    @Override
    protected void fetchData() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {
        initSwitchListener();
        setStatues();
    }

    private void setStatues() {
        tvCaptureStatus.setText("");
        tvBatvolStatus.setText(ShareUtil.getDeviceBatvol());
        tvSunvvolStatus.setText(ShareUtil.getDeviceSunvol());
        tvDianjiStatus.setText(ShareUtil.getDeviceError());
        if (TextUtils.equals(ShareUtil.getMsta(),"1")){
            mTvMsta.setText("有雨水");
        }else if (TextUtils.equals(ShareUtil.getMsta(),"0")){
            mTvMsta.setText("无雨水");
        }
        mTvTemp.setText(ShareUtil.getTemp()+"℃");
        mTvHum.setText(ShareUtil.getHum()+"RH");
        tvPosition.setText("经度\n" + dealLocation(ShareUtil.getLongitude()) + "\n纬度\n" + dealLocation(ShareUtil.getLatitude()));
        mTvLastTime.setText("最后刷新时间:" + TimeUtils.millis2String(System.currentTimeMillis()));
    }

    private String dealLocation(double d) {
        DecimalFormat df = new DecimalFormat("#.00");
        String str = df.format(d);
        return str;
    }

    private void initSwitchListener() {
        sbCapture.setOnSwitchListener(isOpen -> {
            if (sbCapture.isSwitchOpen()) { //打开

            } else { //关闭

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setStatues();
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_data;
    }

    @Override
    protected DataPresenter initPresenter() {
        return new DataPresenter();
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
        // TODO: inflate a fragment view
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
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.activity_anim_in);
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

    @OnClick({R.id.main_data_tv_refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.main_data_tv_refresh:
                setStatues();
                break;
        }
    }
}
