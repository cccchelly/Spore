package com.alex.witAg.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.base.BaseFragment;
import com.alex.witAg.presenter.SControlPresenter;
import com.alex.witAg.presenter.viewImpl.ISControlView;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class SControlFragment extends BaseFragment<SControlPresenter, ISControlView> implements ISControlView {
    @BindView(R.id.scontrol_tv_yucang_kai)
    TextView mTvYucangKai;
    @BindView(R.id.scontrol_tv_yucang_guan)
    TextView mTvYucangGuan;
    @BindView(R.id.scontrol_tv_jiare_kai)
    TextView mTvJiareKai;
    @BindView(R.id.scontrol_tv_jiare_guan)
    TextView mTvJiareGuan;
    @BindView(R.id.scontrol_tv_chongcang_2kai)
    TextView mTvChongcangKai;
    @BindView(R.id.scontrol_tv_chongcang_2guan)
    TextView mTvChongcangGuan;
    @BindView(R.id.scontrol_tv_fuweiqingchong)
    TextView mTvFuweiqingchong;
    @BindView(R.id.scontrol_tv_zhengzaiqingchong)
    TextView mTvZhengzaiqingchong;
    @BindView(R.id.scontrol_tv_csd_jiechong)
    TextView mTvScdJiechong;
    @BindView(R.id.scontrol_tv_csd_paizhao)
    TextView mTvCsdPaizhao;
    @BindView(R.id.scontrol_tv_csd_qingli)
    TextView mTvCsdQingli;
    @BindView(R.id.scontrol_tv_buguangdeng_kai)
    TextView mTvBuguangdengKai;
    @BindView(R.id.scontrol_tv_buguangdeng_guan)
    TextView mTvBuguangdengGuan;
    @BindView(R.id.scontrol_tv_dengguan_kai)
    TextView mTvDengguanKai;
    @BindView(R.id.scontrol_tv_dengguan_guan)
    TextView mTvDengguanGuan;
    @BindView(R.id.scontrol_tv_fuweizhengji)
    TextView mTvFuweizhengji;
    @BindView(R.id.scontrol_tv_shebei_ting)
    TextView mTvShebeiTing;
    @BindView(R.id.scontrol_tv_shebei_huifu)
    TextView mTvShebeiHuifu;
    @BindView(R.id.scontrol_tv_xzchuancang)
    TextView mTvHuancang;
    @BindView(R.id.scontrol_tv_fuweixzc)
    TextView mTvFuweixzc;
    @BindView(R.id.scontrol_tv_qingqiuxinxi)
    TextView mTvQingqiuxinxi;
    Unbinder unbinder;

    TaskQueue taskQueue;

    @Override
    protected void fetchData() {

    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {
        taskQueue = TaskQueue.getInstance();
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_scontrol;
    }

    @Override
    protected SControlPresenter initPresenter() {
        return new SControlPresenter();
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

    @OnClick({R.id.scontrol_tv_yucang_kai, R.id.scontrol_tv_yucang_guan, R.id.scontrol_tv_jiare_kai, R.id.scontrol_tv_jiare_guan,
            R.id.scontrol_tv_chongcang_2kai, R.id.scontrol_tv_chongcang_2guan,
            R.id.scontrol_tv_chongcang_1kai, R.id.scontrol_tv_chongcang_1guan,
            R.id.scontrol_tv_fuweiqingchong, R.id.scontrol_tv_zhengzaiqingchong,
            R.id.scontrol_tv_csd_jiechong, R.id.scontrol_tv_csd_paizhao, R.id.scontrol_tv_csd_qingli,
            R.id.scontrol_tv_buguangdeng_kai, R.id.scontrol_tv_buguangdeng_guan, R.id.scontrol_tv_dengguan_kai,
            R.id.scontrol_tv_dengguan_guan, R.id.scontrol_tv_fuweizhengji, R.id.scontrol_tv_shebei_ting,
            R.id.scontrol_tv_shebei_huifu, R.id.scontrol_tv_xzchuancang, R.id.scontrol_tv_fuweixzc, R.id.scontrol_tv_qingqiuxinxi})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scontrol_tv_yucang_kai:
                taskQueue.add(new SeralTask(AppContants.commands.yucangkai));
                break;
            case R.id.scontrol_tv_yucang_guan:
                taskQueue.add(new SeralTask(AppContants.commands.yucangguan));
                break;
            case R.id.scontrol_tv_jiare_kai:
                taskQueue.add(new SeralTask(AppContants.commands.jiarekai));
                break;
            case R.id.scontrol_tv_jiare_guan:
                taskQueue.add(new SeralTask(AppContants.commands.jiareguan));
                break;
            case R.id.scontrol_tv_chongcang_2kai:
                taskQueue.add(new SeralTask(AppContants.commands.chongcang2kai));
                break;
            case R.id.scontrol_tv_chongcang_2guan:
                taskQueue.add(new SeralTask(AppContants.commands.chongcang2guan));
                break;
            case R.id.scontrol_tv_chongcang_1kai:
                taskQueue.add(new SeralTask(AppContants.commands.chongcang1kai));
                break;
            case R.id.scontrol_tv_chongcang_1guan:
                taskQueue.add(new SeralTask(AppContants.commands.chongcang1guan));
                break;
            case R.id.scontrol_tv_fuweiqingchong:
                taskQueue.add(new SeralTask(AppContants.commands.fuweiqingchong));
                break;
            case R.id.scontrol_tv_zhengzaiqingchong:
                taskQueue.add(new SeralTask(AppContants.commands.zhengzaiqingchong));
                break;
            case R.id.scontrol_tv_csd_jiechong:
                taskQueue.add(new SeralTask(AppContants.commands.csd_jiechong));
                break;
            case R.id.scontrol_tv_csd_paizhao:
                taskQueue.add(new SeralTask(AppContants.commands.csd_paizhao));
                break;
            case R.id.scontrol_tv_csd_qingli:
                taskQueue.add(new SeralTask(AppContants.commands.csd_qingli));
                break;
            case R.id.scontrol_tv_buguangdeng_kai:
                taskQueue.add(new SeralTask(AppContants.commands.buguangdeng_kai));
                break;
            case R.id.scontrol_tv_buguangdeng_guan:
                taskQueue.add(new SeralTask(AppContants.commands.buguangdeng_guan));
                break;
            case R.id.scontrol_tv_dengguan_kai:
                taskQueue.add(new SeralTask(AppContants.commands.dengguan_kai));
                break;
            case R.id.scontrol_tv_dengguan_guan:
                taskQueue.add(new SeralTask(AppContants.commands.dengguan_guan));
                break;
            case R.id.scontrol_tv_fuweizhengji:
                taskQueue.add(new SeralTask(AppContants.commands.fuweizhengji));
                break;
            case R.id.scontrol_tv_shebei_ting:
                taskQueue.add(new SeralTask(AppContants.commands.shebei_ting));
                break;
            case R.id.scontrol_tv_shebei_huifu:
                taskQueue.add(new SeralTask(AppContants.commands.shebei_huifu));
                break;
            case R.id.scontrol_tv_xzchuancang:
                taskQueue.add(new SeralTask(AppContants.commands.xzchuancang));
                break;
            case R.id.scontrol_tv_fuweixzc:
                taskQueue.add(new SeralTask(AppContants.commands.fuweixzc));
                break;
            case R.id.scontrol_tv_qingqiuxinxi:
                taskQueue.add(new SeralTask(AppContants.commands.qingqiuxinxi));
                break;
        }
    }
}
