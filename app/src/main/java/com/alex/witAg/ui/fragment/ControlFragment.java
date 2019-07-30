package com.alex.witAg.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.adapter.DeviceAdapter;
import com.alex.witAg.base.BaseFragment;
import com.alex.witAg.camreaproxy.CameraManager;
import com.alex.witAg.camreaproxy.HkCamera;
import com.alex.witAg.camreaproxy.KsjCamera;
import com.alex.witAg.camreaproxy.OnCaptureListener;
import com.alex.witAg.presenter.ControlPresenter;
import com.alex.witAg.presenter.viewImpl.IControlView;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.CaptureTaskUtil;
import com.alex.witAg.utils.DensityUtil;
import com.alex.witAg.utils.LocalPicCleanUtil;
import com.alex.witAg.utils.LogUtil;
import com.alex.witAg.utils.MyAnimUtil;
import com.alex.witAg.utils.SerialInforStrUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.ToastUtils;
import com.alex.witAg.view.EaseSwitchButton;
import com.alibaba.android.arouter.launcher.ARouter;
import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.ViewHolder;

import org.angmarch.views.NiceSpinner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by dth
 * Des:
 * Date: 2018-03-08.
 */

public class ControlFragment extends BaseFragment<ControlPresenter, IControlView> implements IControlView {
    @BindView(R.id.tv_decline)
    LinearLayout mTvDecline;
    @BindView(R.id.tv_take_photo)
    LinearLayout mTvTakePhoto;
    @BindView(R.id.ic_reset)
    LinearLayout mIcReset;
    @BindView(R.id.tv_Rest)
    TextView mTvSearal;
    @BindView(R.id.ic_open)
    LinearLayout mTvOpen;
    @BindView(R.id.control_spinner)
    NiceSpinner spinner;
    @BindView(R.id.control_swtbtn)
    EaseSwitchButton mSeraSwtBtn;
    @BindView(R.id.control_tv_sera_statues)
    TextView tvSeraStu;
    @BindView(R.id.control_spinner_camera)
    NiceSpinner mSpinnerCamera;
    Unbinder unbinder;
    @BindView(R.id.control_tv_preview)
    TextView mTvPreview;
    @BindView(R.id.control_spinner_sera)
    NiceSpinner mSpinnerSera;
    private CaptureTaskUtil captureTaskUtil;

    private CameraManager cameraManager;

    TaskQueue taskQueue;

    PowerManager pm;
    PowerManager.WakeLock wakeLock;


    @Override
    protected void fetchData() {

    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {
        pm = (PowerManager) (getActivity().getSystemService(Context.POWER_SERVICE));
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TAG");

        taskQueue = TaskQueue.getInstance();

        captureTaskUtil = CaptureTaskUtil.instance();
        cameraManager = CameraManager.getInstance();
        cameraManager.initCamera();

        initSpinner();
        initCameraSpinner();
        getPresenter().getDevices(getACtivity());  //获取串口设备列表
        initSwtBtn();
        setCaptureListener();

    }

    private void setCaptureListener() {
        cameraManager.setOnCaptureListener(new OnCaptureListener() {
            @Override
            public void finish(Bitmap bitmap, File file, String name) {
                //截图成功回调
                LogUtil.i("截图成功回调");
                showCapture(bitmap);
                //showAnotherPage(name);
            }
        });
    }

    private void initCameraSpinner() {
        setPreviewFun();
        mSpinnerCamera.attachDataSource(new LinkedList<>(Arrays.asList("凯视佳", "海康威视")));
        mSpinnerCamera.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {//切换后先设置相机类型，再存储类型到本地
                    case 0:
                        CameraManager.getInstance().setCamera(new KsjCamera());
                        ShareUtil.saveCameraType(AppContants.CameraType.KSJ);
                        break;
                    case 1:
                        CameraManager.getInstance().setCamera(new HkCamera());
                        ShareUtil.saveCameraType(AppContants.CameraType.HKVision);
                        break;
                }
                setPreviewFun();
                setCaptureListener(); //策略对象改变 对象引用改变 所以要重新设置拍照监听器
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        switch (ShareUtil.getCameraType()) { //与上面所给类型对应
            case AppContants.CameraType.KSJ:
                mSpinnerCamera.setSelectedIndex(0);
                break;
            case AppContants.CameraType.HKVision:
                mSpinnerCamera.setSelectedIndex(1);
                break;
        }

    }

    private void setPreviewFun() {
        if (TextUtils.equals(ShareUtil.getCameraType(), AppContants.CameraType.KSJ)) {
            mTvPreview.setVisibility(View.VISIBLE);
        } else {
            mTvPreview.setVisibility(View.GONE);
        }
    }

    private void initSeraSpinner(List<Device> devices) {
        List<String> deviceStrs = new ArrayList<>();

        for (Device device: devices){
            deviceStrs.add(device.getName());
        }
        mSpinnerSera.attachDataSource(deviceStrs);
        mSpinnerSera.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ShareUtil.saveSeraIndex(position);
                captureTaskUtil.initDevice(getACtivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        int currentIndex = ShareUtil.getSeraIndex();
        if (currentIndex == -1){
            mSpinnerSera.setSelectedIndex(devices.size()-2);
        }else {
            mSpinnerSera.setSelectedIndex(currentIndex);
        }
        captureTaskUtil.initDevice(getActivity());
    }

    //到另一个页面展示图片并可选择是否上传
    private void showAnotherPage(String name) {
        ARouter.getInstance()
                .build(AppContants.ARouterUrl.ShowCaptureActivity)
                .withString(AppContants.SHOW_PIC_URL_KEY, name)
                .navigation();
    }

    private void initSwtBtn() {
        mSeraSwtBtn.closeSwitch();
        mSeraSwtBtn.setOnSwitchListener(new EaseSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitchChange(boolean isOpen) {
                if (isOpen) {
                    captureTaskUtil.initDevice(getActivity());
                } else {
                    captureTaskUtil.destoryDevice();
                }
            }
        });
    }

    private void initSpinner() {
        spinner.attachDataSource(new LinkedList<>(Arrays.asList("待调节高度", "高度1", "高度2", "高度3", "高度4")));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (App.getIsTaskRun()) {
                    ToastUtils.showToast("定时任务执行中，请稍后再试");
                    spinner.setSelectedIndex(0);
                } else {
                    if (getPresenter().isRun || !TextUtils.equals(ShareUtil.getDeviceStatue(), "0")) {
                        ToastUtils.showToast("请在复位状态操作");
                        spinner.setSelectedIndex(0);
                        return;
                    }
                    switch (position) {
                        case 0:
                            break;
                        case 1:
                            taskQueue.add(new SeralTask(SerialInforStrUtil.getHighStr1()));
                            break;
                        case 2:
                            taskQueue.add(new SeralTask(SerialInforStrUtil.getHighStr2()));
                            break;
                        case 3:
                            taskQueue.add(new SeralTask(SerialInforStrUtil.getHighStr3()));
                            break;
                        case 4:
                            taskQueue.add(new SeralTask(SerialInforStrUtil.getHighStr4()));
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_control;
    }

    @Override
    protected ControlPresenter initPresenter() {
        return new ControlPresenter();
    }

    @Override
    protected void onRetryListener() {
    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

    @Override
    public void onDestroy() {
        //getPresenter().destoryDevice();
        super.onDestroy();
    }


    @OnClick({R.id.tv_decline, R.id.tv_take_photo, R.id.ic_reset, R.id.tv_Rest, R.id.ic_open,
            R.id.control_open, R.id.control_close, R.id.control_tv_preview})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.tv_decline:
                decline();
                break;
            case R.id.tv_take_photo:
                LocalPicCleanUtil.doCleanIfNecessary();
                /* //临时跳过检测直接拍照，测试用
               captureTaskUtil.loginCapture();  //登录摄像机
                captureTaskUtil.capture(CaptureTaskUtil.FROM_Hand); //执行拍照任务*/
                //正常操作
                takePhoto();
                break;
            case R.id.ic_reset:
                toReset();
                break;
            case R.id.ic_open:
                toOpen();
                break;
            case R.id.tv_Rest:
                resetLocal();
                break;
            case R.id.control_open:

                break;
            case R.id.control_close:
                Log.i("screen", "close");
                wakeLock.release();
                break;
            case R.id.control_tv_preview:
                ARouter.getInstance()
                        .build(AppContants.ARouterUrl.PreviewActivity)
                        .navigation();
                break;
        }
    }


    private boolean isDeviceRun() {
        if (getPresenter().isRun) {
            ToastUtils.showToast("机器运行中，请稍后操作");
            return true;
        } else {
            return false;
        }
    }

    private boolean isTaskRun() {
        if (App.getIsTaskRun()) {
            ToastUtils.showToast("定时任务执行中，请稍后再试");
            return true;
        } else {
            return false;
        }
    }


    private void resetLocal() {
        mTvSearal.startAnimation(MyAnimUtil.alphHalf2All());
        getPresenter().restLocalMsg();
        //captureTaskUtil.sendSure(SerialInforStrUtil.getRestartStr());
        //getPresenter().getDeviceList();
    }

    private void toOpen() {
        mTvOpen.startAnimation(MyAnimUtil.alphHalf2All());
        if (!isTaskRun()) {
            new Thread(() -> {
                getActivity().runOnUiThread(() -> ToastUtils.showToast("准备打开摄像机并将粘虫板翻转至正面"));
                captureTaskUtil.openCaptureTurnPositive();
            }).start();
        }
    }

    private void toReset() {
        mIcReset.startAnimation(MyAnimUtil.alphHalf2All());
        if (!isTaskRun() && !isDeviceRun()) {
            new Thread(() -> {
                getActivity().runOnUiThread(() -> ToastUtils.showToast("准备将粘虫板复位并关闭相机"));
                taskQueue.add(new SeralTask(SerialInforStrUtil.getForceRestartStr()));
            }).start();
        }
    }

/*    private void takePhoto() {
        mTvTakePhoto.startAnimation(MyAnimUtil.alphHalf2All());
        //拍照
        if (!isTaskRun()) {
                new Thread(() -> {
                    int errCode = captureTaskUtil.loginCapture();  //登录摄像机
                    if (errCode == 0) {  //没有错误
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        captureTaskUtil.capture(CaptureTaskUtil.FROM_Hand); //执行拍照任务
                    } else if (errCode == 1) {
                        getActivity().runOnUiThread(() -> ToastUtils.showToast("账号密码错误！"));
                    } else {
                        getActivity().runOnUiThread(() -> ToastUtils.showToast("连接摄像机失败！"));
                    }
                }).start();

                    *//*captureTaskUtil.login();
                    captureTaskUtil.capture(CaptureTaskUtil.FROM_Hand);*//*
        }
    }*/

    private void takePhoto() {
        mTvTakePhoto.startAnimation(MyAnimUtil.alphHalf2All());
        if (!isTaskRun()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cameraManager.initCamera();
                    cameraManager.connectCamera();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cameraManager.captureExecute(AppContants.CaptureFrom.FROM_Hand);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            ToastUtils.showToast("定时任务执行中，请稍后再试");
        }
    }

    private void decline() {
        mTvDecline.startAnimation(MyAnimUtil.alphHalf2All());
        if (!isTaskRun() && !isDeviceRun()) {
            new Thread(() ->
            {
                /*if (captureTaskUtil.isCaptureOpen()) {*/
                getActivity().runOnUiThread(() -> ToastUtils.showToast("准备将粘虫板翻转至反面"));
                taskQueue.add(new SeralTask(SerialInforStrUtil.getDeclineStr()));
                /*} else {
                    getActivity().runOnUiThread(() -> ToastUtils.showToast("请先启动摄像机"));
                }*/
            }).start();
        }
    }

    @Override
    public Activity getACtivity() {
        return getActivity();
    }

    @Override
    public void showDialog(DeviceAdapter mDeviceAdapter, SerialPortManager mSerialPortManager) {
        DialogPlus.newDialog(getContext())
                .setContentHolder(new ListHolder())
                .setAdapter(mDeviceAdapter)
                .setCancelable(true)
                .setGravity(Gravity.CENTER)
                .setOverlayBackgroundResource(Color.TRANSPARENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentWidth(DensityUtil.dip2px(600))
                .setOnItemClickListener((dialog, item, view1, position) -> {
                    mSerialPortManager.closeSerialPort();
                    Device mDevice = mDeviceAdapter.getItem(position);
                    getPresenter().openDevice(mDevice);
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    @Override
    public void showOpenMsg(String msg) {
        ToastUtils.showToast(msg);
    }

    @Override
    public void showCapture(Bitmap bitmap) { //直接显示照片
        DialogPlus dialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(R.layout.show_pic_imageview))
                .setCancelable(true)
                .setGravity(Gravity.CENTER)
                .setOverlayBackgroundResource(Color.TRANSPARENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .create();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogPlus.show();
                ImageView imageView = (ImageView) dialogPlus.getHolderView().findViewById(R.id.show_pic_imageview);
                imageView.setOnClickListener(v -> dialogPlus.dismiss());
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public void showSeraStatus(String sta) {
        tvSeraStu.setText(sta);
    }

    @Override
    public void setSwtBtnChecked(boolean checked) {
        if (checked) {
            mSeraSwtBtn.openSwitch();
        } else {
            mSeraSwtBtn.closeSwitch();
        }
    }


    @Override
    public void showDevices(ArrayList<Device> mDevices) {
        initSeraSpinner(mDevices);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.activity_anim_in);
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
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
}
