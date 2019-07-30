package com.alex.witAg.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.base.BaseActivity;
import com.alex.witAg.bean.TaskTimeBean;
import com.alex.witAg.presenter.TaskSettingNewPresenter;
import com.alex.witAg.presenter.viewImpl.ITaskSettingNewView;
import com.alex.witAg.utils.ShareUtil;
import com.alex.witAg.utils.TaskServiceUtil;
import com.alex.witAg.utils.TaskTimeUtil;
import com.alex.witAg.utils.TimeUtils;
import com.alex.witAg.utils.ToastUtils;
import com.alex.witAg.view.EaseSwitchButton;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.angmarch.views.NiceSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Route(path = AppContants.ARouterUrl.TASK_SETTING_NEW_ACTIVITY)
public class TaskSettingNewActivity extends BaseActivity<TaskSettingNewPresenter, ITaskSettingNewView> implements ITaskSettingNewView {

    @BindView(R.id.set_capture_tv_choose_time)
    TextView mTvChooseTime;
    @BindView(R.id.set_capture_edt_time)
    EditText mEdtTime;
    @BindView(R.id.set_capture_tv_sure)
    TextView mTvSure;
    @BindView(R.id.set_capture_tv_cancle)
    TextView mTvCancle;
    @BindView(R.id.set_capture_swt_btn_logo)
    EaseSwitchButton mSwtBtnLogo;
    @BindView(R.id.set_capture_swt_spinner)
    NiceSpinner mSwtSpinner;
    @BindView(R.id.task_setting_recyclerview)
    RecyclerView mRecyclerview;
    @BindView(R.id.set_capture_tv_choose_end_time)
    TextView mEndTime;
    private TimePickerView timePickerView;
    private long startTime = 0;
    private long endTime = 0;

    private TimeTaskListAdapter mAdapter;

    private boolean isStartTime = false;

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        initTimeDialog();
        initSpinner();
        initSwitchBtn();

        initRecyclerview();
    }

    private void initRecyclerview() {
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(TaskTimeUtil.getInstance().getTimeAreaList());
        mAdapter = new TimeTaskListAdapter(taskTimeBeans);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerview.setAdapter(mAdapter);
    }



    private void initSwitchBtn() {
        mSwtBtnLogo.setOnSwitchListener(new EaseSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitchChange(boolean isOpen) {
                //
            }
        });
    }

    private void initSpinner() {
        mSwtSpinner.attachDataSource(new LinkedList<>(Arrays.asList("高", "中", "低")));
    }

    private void initTimeDialog() {
        timePickerView = new TimePickerBuilder(this, new OnTimeSelectListener() {
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

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int tellMeLayout() {
        return R.layout.activity_task_setting_new;
    }

    @Override
    protected TaskSettingNewPresenter initPresenter() {
        return new TaskSettingNewPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }


    @OnClick({R.id.set_capture_tv_choose_time, R.id.set_capture_tv_choose_end_time, R.id.set_capture_tv_sure, R.id.set_capture_tv_cancle})
    public void onViewClicked(View view) {
        switch (view.getId()) {
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
               /* String delay = mEdtTime.getText().toString();
                boolean isLogo = mSwtBtnLogo.isSwitchOpen();
                int quality = mSwtSpinner.getSelectedIndex();
                getPresenter().setTask(date, delay, isLogo, quality);*/
               if (TextUtils.isEmpty(delay) || startTime == 0 ||endTime ==0){
                   ToastUtils.showToast("请填写完整配置");
                   return;
               }
                if (Integer.parseInt(delay)<10){
                    ToastUtils.showToast("时间间隔应至少十分钟");
                    return;
                }
               TaskTimeBean taskTimeBean = new TaskTimeBean();
               taskTimeBean.setStartTime(startTime);
               taskTimeBean.setEndTime(endTime);
               taskTimeBean.setDelay(Long.valueOf(delay));
                if (TaskTimeUtil.getInstance().isTimeRight(taskTimeBean)){
                    if (TaskTimeUtil.getInstance().addTime(taskTimeBean)){
                        ToastUtils.showToast("添加成功");
                        refreshRvData();
                    }else {
                        ToastUtils.showToast("该时间段与已有时间段冲突");
                    }
                }else {
                    ToastUtils.showToast("结束时间应在开始时间后");
                }
                break;
            case R.id.set_capture_tv_cancle:
                TaskServiceUtil.resetPhotoTasks();
                ToastUtils.showToast("定时任务重启");
                onBackPressed();
                break;
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    private void refreshRvData() {
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(TaskTimeUtil.getInstance().getTimeAreaList());
        mAdapter.setNewData(taskTimeBeans);
        mAdapter.notifyDataSetChanged();
    }


    public class TimeTaskListAdapter extends BaseQuickAdapter<TaskTimeBean, BaseViewHolder> {

        public TimeTaskListAdapter(@Nullable List<TaskTimeBean> data) {
            super(R.layout.time_task_rv_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, TaskTimeBean item) {
            helper.setText(R.id.task_setting_rv_item_tv_time, TimeUtils.millis2String(item.getStartTime(), new SimpleDateFormat("HH:mm")) + "-" + TimeUtils.millis2String(item.getEndTime(), new SimpleDateFormat("HH:mm")));
            helper.setText(R.id.task_setting_rv_item_delay, "间隔时间:" + item.getDelay() + "分");
            TextView textView = helper.getView(R.id.task_setting_rv_item_img_delete);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(TaskSettingNewActivity.this)
                            .setTitle("删除任务")
                            .setMessage("是否确认删除此任务?")
                            .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TaskTimeUtil.getInstance().deleteTime(item);
                                    dialog.dismiss();
                                    refreshRvData();
                                }
                            })
                            .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });

        }
    }


}
