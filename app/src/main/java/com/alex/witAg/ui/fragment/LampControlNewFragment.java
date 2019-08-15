package com.alex.witAg.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alex.witAg.R;
import com.alex.witAg.base.BaseFragment;
import com.alex.witAg.bean.TaskTimeBean;
import com.alex.witAg.presenter.LampControlNewPresenter;
import com.alex.witAg.presenter.viewImpl.ILampControlNewView;
import com.alex.witAg.utils.DialogDelete;
import com.alex.witAg.utils.LampTimeUtil;
import com.alex.witAg.utils.TimeUtils;
import com.alex.witAg.utils.ToastUtils;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.angmarch.views.NiceSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class LampControlNewFragment extends BaseFragment<LampControlNewPresenter, ILampControlNewView> implements ILampControlNewView {

    @BindView(R.id.set_capture_tv_choose_time)
    TextView mTvChooseTime;
    @BindView(R.id.task_setting_recyclerview)
    RecyclerView mRecyclerview;
    @BindView(R.id.set_capture_tv_choose_end_time)
    TextView mEndTime;
    @BindView(R.id.lamp_control_new_spinner)
    NiceSpinner mSpinner;
    @BindView(R.id.lamp_control_new_edt_time)
    EditText mEdtTime;
    @BindView(R.id.lamp_control_new_tv_light_sure)
    TextView mTvLightSure;
    @BindView(R.id.lamp_control_new_lin_light)
    LinearLayout mLinLight;
    @BindView(R.id.lamp_control_new_lin_time)
    LinearLayout mLinTime;
    Unbinder unbinder;

    private TimePickerView timePickerView;
    private long startTime = 0;
    private long endTime = 0;
    private TimeTaskListAdapter mAdapter;

    private boolean isStartTime = false;

    @Override
    protected void fetchData() {

    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {
        initTimeDialog();
        initRecyclerview();
        initSpinner();
    }

    private void initSpinner() {
        setTimeControl();

        List<String> datas = new ArrayList<>();
        datas.add("时控");
        datas.add("光控");
        mSpinner.attachDataSource(datas);
        mSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        setTimeControl();
                        break;
                    case 1:
                        setLightControl();
                        break;
                }
            }
        });

    }

    private void setLightControl(){
        mLinTime.setVisibility(View.GONE);
        mLinLight.setVisibility(View.VISIBLE);
    }

    private void setTimeControl(){
        mLinTime.setVisibility(View.VISIBLE);
        mLinLight.setVisibility(View.GONE);
    }

    private void initRecyclerview() {
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(LampTimeUtil.getInstance().getTimeAreaList());
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

    @OnClick({R.id.set_capture_tv_choose_time, R.id.set_capture_tv_choose_end_time, R.id.set_capture_tv_sure,
            R.id.set_capture_tv_cancle,R.id.lamp_control_new_tv_light_sure})
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

                if (startTime == 0 || endTime == 0) {
                    ToastUtils.showToast("请填写完整配置");
                    return;
                }

                TaskTimeBean taskTimeBean = new TaskTimeBean();
                taskTimeBean.setStartTime(startTime);
                taskTimeBean.setEndTime(endTime);

                if (LampTimeUtil.getInstance().getTimeAreaList().size() >= 5) {
                    ToastUtils.showToast("最多设置五个时间段");
                    return;
                }

                if (LampTimeUtil.getInstance().isTimeRight(taskTimeBean)) {
                    if (LampTimeUtil.getInstance().addTime(taskTimeBean)) {
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
                if (LampTimeUtil.getInstance().getTimeAreaList().size() >= 5) {
                    ToastUtils.showToast("最多设置五个时间段");
                    return;
                }
                if (LampTimeUtil.getInstance().getTimeAreaList().size() == 0) {
                    ToastUtils.showToast("至少设置一个时间段");
                    return;
                }
                getPresenter().setTimeControl();
                ToastUtils.showToast("时控请求已发送");
                /*TaskServiceUtil.resetLampTask();
                ToastUtils.showToast("灯管控制定时任务重启");*/
                break;
            case R.id.lamp_control_new_tv_light_sure:
                if (TextUtils.isEmpty(mEdtTime.getText().toString())){
                    ToastUtils.showToast("请输入持续时长");
                    return;
                }
                double time = Double.parseDouble(mEdtTime.getText().toString());
                if (time<1 || time>600){
                    ToastUtils.showToast("时长需在1-600分钟内");
                    return;
                }

                getPresenter().setLightControl(mEdtTime.getText().toString());
                ToastUtils.showToast("光控请求已发送");
                break;
        }
    }

    private void refreshRvData() {
        List<TaskTimeBean> taskTimeBeans = new ArrayList<>();
        taskTimeBeans.addAll(LampTimeUtil.getInstance().getTimeAreaList());
        mAdapter.setNewData(taskTimeBeans);
        mAdapter.notifyDataSetChanged();
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

    public class TimeTaskListAdapter extends BaseQuickAdapter<TaskTimeBean, BaseViewHolder> {

        public TimeTaskListAdapter(@Nullable List<TaskTimeBean> data) {
            super(R.layout.time_task_rv_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, TaskTimeBean item) {
            helper.setText(R.id.task_setting_rv_item_tv_time, TimeUtils.millis2String(item.getStartTime(), new SimpleDateFormat("HH:mm")) + "-" + TimeUtils.millis2String(item.getEndTime(), new SimpleDateFormat("HH:mm")));

            helper.getView(R.id.task_setting_rv_item_delay).setVisibility(View.GONE);

            TextView textView = helper.getView(R.id.task_setting_rv_item_img_delete);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogDelete dialogDelete = new DialogDelete(getActivity());
                    dialogDelete.setOnSureListener(new DialogDelete.OnSureListener() {
                        @Override
                        public void onSure() {
                            LampTimeUtil.getInstance().deleteTime(item);
                            refreshRvData();
                        }
                    });
                    dialogDelete.show();

                }
            });

        }
    }


    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_lamp_control_new;
    }

    @Override
    protected LampControlNewPresenter initPresenter() {
        return new LampControlNewPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

}
