package com.alex.witAg.ui.fragment;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alex.witAg.App;
import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.base.BaseFragment;
import com.alex.witAg.bean.TaskTimeBean;
import com.alex.witAg.camreaproxy.CameraManager;
import com.alex.witAg.camreaproxy.HkCamera;
import com.alex.witAg.camreaproxy.KsjCamera;
import com.alex.witAg.camreaproxy.OnCaptureListener;
import com.alex.witAg.camreaproxy.SporeCamera;
import com.alex.witAg.presenter.TakePhotoPresenter;
import com.alex.witAg.presenter.viewImpl.ITakePhotoView;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.CapturePostUtil;
import com.alex.witAg.utils.CaptureTaskUtil;
import com.alex.witAg.utils.DialogDelete;
import com.alex.witAg.utils.LocalPicCleanUtil;
import com.alex.witAg.utils.LogUtil;
import com.alex.witAg.utils.MyAnimUtil;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TaskServiceUtil;
import com.alex.witAg.utils.TaskTimeUtil;
import com.alex.witAg.utils.TimeUtils;
import com.alex.witAg.utils.ToastUtils;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.kongqw.serialportlibrary.Device;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.angmarch.views.NiceSpinner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class TakePhotoFragment extends BaseFragment<TakePhotoPresenter, ITakePhotoView> implements ITakePhotoView {

    @BindView(R.id.set_capture_tv_choose_time)
    TextView mTvChooseTime;
    @BindView(R.id.set_capture_edt_time)
    EditText mEdtTime;
    @BindView(R.id.set_capture_tv_sure)
    TextView mTvSure;
    @BindView(R.id.set_capture_tv_cancle)
    TextView mTvCancle;
    @BindView(R.id.task_setting_recyclerview)
    RecyclerView mRecyclerview;
    @BindView(R.id.set_capture_tv_choose_end_time)
    TextView mEndTime;
    @BindView(R.id.takephoto_tv_capture)
    TextView mTvCapture;
    @BindView(R.id.takephoto_tv_post)
    TextView mTvPost;
    Unbinder unbinder;
    private TimePickerView timePickerView;
    private long startTime = 0;
    private long endTime = 0;

    private TimeTaskListAdapter mAdapter;

    private boolean isStartTime = false;

    private CaptureTaskUtil captureTaskUtil;

    private CameraManager cameraManager;

    TaskQueue taskQueue;


    @BindView(R.id.control_tv_preview)
    TextView mTvPreview;
    @BindView(R.id.control_spinner_camera)
    NiceSpinner mSpinnerCamera;
    @BindView(R.id.control_spinner_sera)
    NiceSpinner mSpinnerSera;

    @Override
    protected void fetchData() {

    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {

        taskQueue = TaskQueue.getInstance();

        captureTaskUtil = CaptureTaskUtil.instance();
        cameraManager = CameraManager.getInstance();
        cameraManager.initCamera();

        initCameraSpinner();
        getPresenter().getDevices();  //获取串口设备列表
        setCaptureListener();

        initTimeDialog();
        initRecyclerview();
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
        mSpinnerCamera.attachDataSource(new LinkedList<>(Arrays.asList("凯视佳", "海康威视","孢子捕捉仪")));
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
                    case 2:
                        CameraManager.getInstance().setCamera(new SporeCamera());
                        ShareUtil.saveCameraType(AppContants.CameraType.USB);
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
            case AppContants.CameraType.USB:
                mSpinnerCamera.setSelectedIndex(2);
                break;
        }

    }

    private void setPreviewFun() {
        if (TextUtils.equals(ShareUtil.getCameraType(), AppContants.CameraType.KSJ) || TextUtils.equals(ShareUtil.getCameraType(), AppContants.CameraType.USB)) {
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
                captureTaskUtil.initDevice(getActivity());
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
    public void showDevices(ArrayList<Device> mDevices) {
        initSeraSpinner(mDevices);
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_take_photo;
    }

    private void initRecyclerview() {
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(TaskTimeUtil.getInstance().getTimeAreaList());
        mAdapter = new TimeTaskListAdapter(taskTimeBeans);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerview.setAdapter(mAdapter);
    }


    private void initTimeDialog() {
        timePickerView = new TimePickerBuilder(getActivity(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                if (isStartTime) {
                    mTvChooseTime.setText(TimeUtils.date2String(date, new SimpleDateFormat("HH:mm")));
                    startTime = TimeUtils.date2Millis(date);
                } else {
                    mEndTime.setText(TimeUtils.date2String(date, new SimpleDateFormat("HH:mm")));
                    endTime = TimeUtils.date2Millis(date);
                }
                timePickerView.dismiss();
            }
        }).setType(new boolean[]{false, false, false, true, true, false})// 显示时分
                .build();
    }


    @OnClick({R.id.set_capture_tv_choose_time, R.id.set_capture_tv_choose_end_time, R.id.set_capture_tv_sure, R.id.set_capture_tv_cancle,
            R.id.takephoto_tv_post, R.id.control_tv_preview,R.id.takephoto_tv_capture,R.id.takephoto_tv_capture_other})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case  R.id.control_tv_preview:
                if (TextUtils.equals(ShareUtil.getCameraType(), AppContants.CameraType.USB)) {
                ARouter.getInstance()
                        .build(AppContants.ARouterUrl.SporeShowActivity)
                        .navigation();
            }else {

                ARouter.getInstance()
                        .build(AppContants.ARouterUrl.PreviewActivity)
                        .navigation();
                }
                break;
            case R.id.takephoto_tv_post: //上传本地照片
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CapturePostUtil.findLocalPic();
                    }
                }).start();
                break;
            case R.id.takephoto_tv_capture_other: //请求硬件运行到相应位置并且拍照
                taskQueue.add(new SeralTask(AppContants.commands.kaishipaizhao));
                break;
            case R.id.takephoto_tv_capture: //执行直接拍照
                LocalPicCleanUtil.doCleanIfNecessary();
                takePhoto();
                break;
            case R.id.set_capture_tv_choose_time: //开始时间
                isStartTime = true;
                timePickerView.show();
                break;
            case R.id.set_capture_tv_choose_end_time: //结束时间
                isStartTime = false;
                timePickerView.show();
                break;
            case R.id.set_capture_tv_sure:
                String delay = mEdtTime.getText().toString();
                if (TextUtils.isEmpty(delay) || startTime == 0 || endTime == 0) {
                    ToastUtils.showToast("请填写完整配置");
                    return;
                }
                if (Integer.parseInt(delay) < 10) {
                    ToastUtils.showToast("时间间隔应至少十分钟");
                    return;
                }
                TaskTimeBean taskTimeBean = new TaskTimeBean();
                taskTimeBean.setStartTime(startTime);
                taskTimeBean.setEndTime(endTime);
                taskTimeBean.setDelay(Long.valueOf(delay));
                if (TaskTimeUtil.getInstance().isTimeRight(taskTimeBean)) {
                    if (TaskTimeUtil.getInstance().addTime(taskTimeBean)) {
                        ToastUtils.showToast("添加成功");
                        refreshRvData();
                    } else {
                        ToastUtils.showToast("该时间段与已有时间段冲突");
                    }
                } else {
                    ToastUtils.showToast("结束时间应在开始时间后");
                }
                break;
            case R.id.set_capture_tv_cancle:
                TaskServiceUtil.resetPhotoTasks();
                ToastUtils.showToast("拍照定时任务重启");
                break;
        }
    }

    private void refreshRvData() {
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(TaskTimeUtil.getInstance().getTimeAreaList());
        mAdapter.setNewData(taskTimeBeans);
        mAdapter.notifyDataSetChanged();
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


    public class TimeTaskListAdapter extends BaseQuickAdapter<TaskTimeBean, BaseViewHolder> {

        public TimeTaskListAdapter(@Nullable List<TaskTimeBean> data) {
            super(R.layout.time_task_rv_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, TaskTimeBean item) {
            helper.setText(R.id.task_setting_rv_item_tv_time, TimeUtils.millis2String(item.getStartTime(), new SimpleDateFormat("HH:mm")) + "-" + TimeUtils.millis2String(item.getEndTime(), new SimpleDateFormat("HH:mm")));
            helper.setText(R.id.task_setting_rv_item_delay, "间隔:" + item.getDelay() + "分");
            TextView textView = helper.getView(R.id.task_setting_rv_item_img_delete);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   DialogDelete dialogDelete =  new DialogDelete(getActivity());
                   dialogDelete.setOnSureListener(new DialogDelete.OnSureListener() {
                       @Override
                       public void onSure() {
                           TaskTimeUtil.getInstance().deleteTime(item);
                           refreshRvData();
                       }
                   });
                   dialogDelete.show();

                }
            });

        }
    }


    @Override
    protected TakePhotoPresenter initPresenter() {
        return new TakePhotoPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

    private void takePhoto() {
        mTvCapture.startAnimation(MyAnimUtil.alphHalf2All());
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

    private boolean isTaskRun() {
        if (App.getIsTaskRun()) {
            ToastUtils.showToast("定时任务执行中，请稍后再试");
            return true;
        } else {
            return false;
        }
    }

}
