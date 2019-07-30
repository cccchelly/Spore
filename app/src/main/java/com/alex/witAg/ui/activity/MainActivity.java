package com.alex.witAg.ui.activity;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.base.BaseActivity;
import com.alex.witAg.base.BaseObserver;
import com.alex.witAg.base.BaseResponse;
import com.alex.witAg.bean.HomeBean;
import com.alex.witAg.bean.PostMsgBean;
import com.alex.witAg.bean.PostMsgResultBean;
import com.alex.witAg.bean.TaskTimeBean;
import com.alex.witAg.http.AppDataManager;
import com.alex.witAg.http.network.Net;
import com.alex.witAg.presenter.MainPresenter;
import com.alex.witAg.presenter.viewImpl.IMainView;
import com.alex.witAg.ui.fragment.AboutFragment;
import com.alex.witAg.ui.fragment.ControlFragment;
import com.alex.witAg.ui.fragment.DataFragment;
import com.alex.witAg.ui.fragment.HomeFragment;
import com.alex.witAg.ui.fragment.SettingFragment;
import com.alex.witAg.utils.AppMsgUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TaskServiceUtil;
import com.alex.witAg.utils.TaskTimeUtil;
import com.alex.witAg.utils.TimeUtils;
import com.alex.witAg.utils.ToastUtils;
import com.alex.witAg.view.LeftTabView;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import org.litepal.tablemanager.Connector;
import org.litepal.util.LogUtil;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Route(path = AppContants.ARouterUrl.MAIN_ACTIVITY/*, extras = AppContants.LOGIN_INTERCEPTOR*/)
public class MainActivity extends BaseActivity<MainPresenter, IMainView> implements IMainView {


    @BindView(R.id.left_tab_view)
    LeftTabView mLeftTabView;
    @BindView(R.id.fl_container)
    FrameLayout mFlContainer;
    @BindView(R.id.main_tv_dark)
    TextView mTvDark;
    @BindView(R.id.main_top_view)
    View mTopView;
    @BindView(R.id.main_rl_topbar)
    RelativeLayout mRlTopbar;
    private HomeFragment mHomeFragment;
    private SettingFragment mSettingFragment;
    private DataFragment mDataFragment;
    private ControlFragment mControlFragment;
    private AboutFragment mAboutFragment;
    SQLiteDatabase db = Connector.getDatabase();

    final  int COUNTS = 5;//点击次数
    final  long DURATION = 3 * 1000;//规定有效时间
    long[] mHits = new long[COUNTS];

    private static final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventBus.getDefault().register(MainActivity.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //EventBus.getDefault().unregister(MainActivity.this);
    }

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        initFragment(savedInstanceState);
        //每次重启上传信息，然后再重置本地状态
        postMsg();
        restLocalMsg();
        TaskServiceUtil.resetTasks();

    }


    private void postMsg() {
        PostMsgBean postMsgBean = new PostMsgBean();
        postMsgBean.setSunvol(ShareUtil.getDeviceSunvol());
        postMsgBean.setBatvol(ShareUtil.getDeviceBatvol());
        postMsgBean.setHighsta(ShareUtil.getCaptureHignSta());
        postMsgBean.setSta(ShareUtil.getDeviceStatue());
        postMsgBean.setError(ShareUtil.getDeviceError());
        postMsgBean.setImei(AppMsgUtil.getIMEI(App.getAppContext()));
        postMsgBean.setLatitude(ShareUtil.getLatitude() + "");
        postMsgBean.setLongitude(ShareUtil.getLongitude() + "");
        postMsgBean.setFirstStart(true);

        AppDataManager.getInstence(Net.URL_KIND_BASE)
                .postDeviceMsg(postMsgBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<PostMsgResultBean>>() {
                    @Override
                    public void onSuccess(BaseResponse<PostMsgResultBean> response) {

                    }
                });
    }

    public void restLocalMsg() {
        ShareUtil.saveCaptureCamSta("0");
        ShareUtil.saveCaptureErrorSta("0");
        ShareUtil.saveDeviceStatue("0");
        ShareUtil.saveDeviceError("0");
    }


    private void initListener() {
        mLeftTabView.setOnSelectedChangeListener((view, position) -> {
            Logger.d("position:  " + position);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            switch (position) {
                //首页
                case 0:
                    transaction.hide(mDataFragment)
                            .hide(mSettingFragment)
                            .hide(mControlFragment)
                            .hide(mAboutFragment)
                            .show(mHomeFragment)
                            .commitAllowingStateLoss();
                    break;
                //实时数据
                case 1:

                    transaction.hide(mHomeFragment)
                            .hide(mSettingFragment)
                            .hide(mControlFragment)
                            .hide(mAboutFragment)
                            .show(mDataFragment)
                            .commitAllowingStateLoss();
                    break;
                //系统设置
                case 2:
                    transaction.hide(mHomeFragment)
                            .hide(mDataFragment)
                            .hide(mControlFragment)
                            .hide(mAboutFragment)
                            .show(mSettingFragment)
                            .commitAllowingStateLoss();
                    break;
                //手动控制
                case 3:
                    transaction.hide(mHomeFragment)
                            .hide(mDataFragment)
                            .hide(mSettingFragment)
                            .hide(mAboutFragment)
                            .show(mControlFragment)
                            .commitAllowingStateLoss();
                    break;
                //关于我们
                case 4:
                    transaction.hide(mHomeFragment)
                            .hide(mDataFragment)
                            .hide(mSettingFragment)
                            .hide(mControlFragment)
                            .show(mAboutFragment)
                            .commitAllowingStateLoss();
                    break;
                default:
            }
        });
    }

    /**
     * 初始化碎片
     */
    private void initFragment(Bundle savedInstanceState) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        int currentTabPosition = 0;
        if (savedInstanceState != null) {//内存重启
            mHomeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HomeFragment");
            mSettingFragment = (SettingFragment) getSupportFragmentManager().findFragmentByTag("SettingFragment");
            mDataFragment = (DataFragment) getSupportFragmentManager().findFragmentByTag("DataFragment");
            mControlFragment = (ControlFragment) getSupportFragmentManager().findFragmentByTag("ControlFragment");
            mAboutFragment = (AboutFragment) getSupportFragmentManager().findFragmentByTag("AboutFragment");
            currentTabPosition = savedInstanceState.getInt(AppContants.HOME_CURRENT_TAB_POSITION);
        } else {
            mHomeFragment = new HomeFragment();
            mSettingFragment = new SettingFragment();
            mDataFragment = new DataFragment();
            mControlFragment = new ControlFragment();
            mAboutFragment = new AboutFragment();
            transaction.add(R.id.fl_container, mHomeFragment, "HomeFragment");
            transaction.add(R.id.fl_container, mSettingFragment, "SettingFragment");
            transaction.add(R.id.fl_container, mDataFragment, "DataFragment");
            transaction.add(R.id.fl_container, mControlFragment, "ControlFragment");
            transaction.add(R.id.fl_container, mAboutFragment, "AboutFragment");
        }
        transaction.commit();

        initListener();
        mLeftTabView.setSelectPosition(currentTabPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //内存重启前保存位置
        Logger.e("onSaveInstanceState进来了1");
        if (mLeftTabView != null) {
            Logger.e("onSaveInstanceState进来了2");
            outState.putInt(AppContants.HOME_CURRENT_TAB_POSITION, mLeftTabView.getSelectPosition());
        }
    }

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int tellMeLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected MainPresenter initPresenter() {
        return new MainPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }


    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onSuccess(HomeBean homeBean) {

    }

    @OnClick({R.id.main_tv_dark, R.id.main_top_view,R.id.main_rl_topbar})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.main_tv_dark:
                /*ActivityBrightnessManager.setActivityBrightness(0.0f, getActivity());
                ShareUtil.setScreenBright(0.0f);
                mTopView.setVisibility(View.VISIBLE);*/
                break;
            case R.id.main_top_view:
                /*ActivityBrightnessManager.setActivityBrightness(1.0f,getActivity());
                ShareUtil.setScreenBright(1.0f);
                mTopView.setVisibility(View.GONE);*/
                break;
            case R.id.main_rl_topbar:
                /**
                 * 实现双击方法
                 * src 拷贝的源数组
                 * srcPos 从源数组的那个位置开始拷贝.
                 * dst 目标数组
                 * dstPos 从目标数组的那个位子开始写数据
                 * length 拷贝的元素的个数
                 */
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    String tips = "您已在[" + DURATION + "]ms内连续点击【" + mHits.length + "】次了！！！";

                    ARouter.getInstance().build(AppContants.ARouterUrl.DEBUG_ACTIVITY)
                            .navigation();
                    //ToastUtils.showToast(tips);
                }
                break;
        }
    }
}
