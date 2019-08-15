package com.alex.witAg.ui.activity;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
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
import com.alex.witAg.http.AppDataManager;
import com.alex.witAg.http.network.Net;
import com.alex.witAg.presenter.MainPresenter;
import com.alex.witAg.presenter.viewImpl.IMainView;
import com.alex.witAg.ui.fragment.AboutFragment;
import com.alex.witAg.ui.fragment.BaseMsgFragment;
import com.alex.witAg.ui.fragment.LampControlNewFragment;
import com.alex.witAg.ui.fragment.SControlFragment;
import com.alex.witAg.ui.fragment.TakePhotoFragment;
import com.alex.witAg.utils.AppMsgUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TaskServiceUtil;
import com.alex.witAg.view.LeftTabView;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import org.litepal.tablemanager.Connector;

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
    private BaseMsgFragment mBaseMsgFragment;
    private LampControlNewFragment mLampConFragment;
    private SControlFragment mSControlFragment;
    private TakePhotoFragment mTakePhotoFragment;
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
                //基础数据
                case 0:
                    transaction.hide(mSControlFragment)
                            .hide(mLampConFragment)
                            .hide(mTakePhotoFragment)
                            .hide(mAboutFragment)
                            .show(mBaseMsgFragment)
                            .commitAllowingStateLoss();
                    break;
                //灯头控制
                case 1:
                    transaction.hide(mBaseMsgFragment)
                            .hide(mSControlFragment)
                            .hide(mTakePhotoFragment)
                            .hide(mAboutFragment)
                            .show(mLampConFragment)
                            .commitAllowingStateLoss();

                    break;
                //单步控制
                case 2:
                    transaction.hide(mBaseMsgFragment)
                            .hide(mLampConFragment)
                            .hide(mTakePhotoFragment)
                            .hide(mAboutFragment)
                            .show(mSControlFragment)
                            .commitAllowingStateLoss();
                    break;
                //拍照
                case 3:
                    transaction.hide(mBaseMsgFragment)
                            .hide(mSControlFragment)
                            .hide(mLampConFragment)
                            .hide(mAboutFragment)
                            .show(mTakePhotoFragment)
                            .commitAllowingStateLoss();
                    break;
                //关于我们
                case 4:
                    transaction.hide(mBaseMsgFragment)
                            .hide(mSControlFragment)
                            .hide(mLampConFragment)
                            .hide(mTakePhotoFragment)
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
            mBaseMsgFragment = (BaseMsgFragment) getSupportFragmentManager().findFragmentByTag("HomeFragment");
            mLampConFragment = (LampControlNewFragment) getSupportFragmentManager().findFragmentByTag("SettingFragment");
            mSControlFragment = (SControlFragment) getSupportFragmentManager().findFragmentByTag("DataFragment");
            mTakePhotoFragment = (TakePhotoFragment) getSupportFragmentManager().findFragmentByTag("ControlFragment");
            mAboutFragment = (AboutFragment) getSupportFragmentManager().findFragmentByTag("AboutFragment");
            currentTabPosition = savedInstanceState.getInt(AppContants.HOME_CURRENT_TAB_POSITION);
        } else {
            mBaseMsgFragment = new BaseMsgFragment();
            mLampConFragment = new LampControlNewFragment();
            mSControlFragment = new SControlFragment();
            mTakePhotoFragment = new TakePhotoFragment();
            mAboutFragment = new AboutFragment();
            transaction.add(R.id.fl_container, mBaseMsgFragment, "HomeFragment");
            transaction.add(R.id.fl_container, mLampConFragment, "SettingFragment");
            transaction.add(R.id.fl_container, mSControlFragment, "DataFragment");
            transaction.add(R.id.fl_container, mTakePhotoFragment, "ControlFragment");
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
