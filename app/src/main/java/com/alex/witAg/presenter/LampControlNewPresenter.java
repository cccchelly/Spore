package com.alex.witAg.presenter;

import com.alex.witAg.base.BasePresenter;
import com.alex.witAg.bean.TaskTimeBean;
import com.alex.witAg.presenter.viewImpl.ILampControlNewView;
import com.alex.witAg.taskqueue.SeralTask;
import com.alex.witAg.taskqueue.TaskQueue;
import com.alex.witAg.utils.LampTimeUtil;
import com.alex.witAg.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.List;

public class LampControlNewPresenter extends BasePresenter<ILampControlNewView> {


    public void setTimeControl(){
        List<TaskTimeBean> taskTimeBeans = LampTimeUtil.getInstance().getTimeAreaList();

        StringBuffer sb = new StringBuffer();
        sb.append("{\"S1" + "\":\"111\"");
        for (int i =0;i<taskTimeBeans.size();i++){
            TaskTimeBean timesBean = taskTimeBeans.get(i);
            sb.append(",T"+ (i+1) + "1" +
                    TimeUtils.millis2String(timesBean.getStartTime(),new SimpleDateFormat("HH")) +
                    TimeUtils.millis2String(timesBean.getStartTime(),new SimpleDateFormat("mm")) +
                    TimeUtils.millis2String(timesBean.getEndTime(),new SimpleDateFormat("HH")) +
                    TimeUtils.millis2String(timesBean.getEndTime(),new SimpleDateFormat("mm"))
            );
        }
        sb.append("}");
        String command = sb.toString();
        TaskQueue.getInstance().add(new SeralTask(command));

                    /*Command = '{"S' + controlType + '":"111",'"T' + count（时间条数）+ "1" + 00(开始时，两位数) + 00(开始分，两位数) +
                    00(结束时，两位数)+ 00(结束分，两位数) + '",'"T' + count + '011111111",'+'}'*/

    }

    public void setLightControl(String time){
        String finalTime = "";
        switch (time.length()){
            case 1:
                finalTime = "00"+time;
                break;
            case 2:
                finalTime = "0"+time;
                break;
            case 3:
                finalTime = time;
                break;
        }
        String command = "STA:20,TL:"+finalTime+",MODE_T:0";
        TaskQueue.getInstance().add(new SeralTask(command));
    }


}
