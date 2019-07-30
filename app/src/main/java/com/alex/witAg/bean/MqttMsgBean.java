package com.alex.witAg.bean;

/**
 * Created by Administrator on 2018-05-03.
 */

public class MqttMsgBean {

    /**
     * o : task_setting
     * d : {"cmd":"high2","start_time":1111111111111111111,"delay":200}
     */

    private String o;
    private DBean d;

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    public DBean getD() {
        return d;
    }

    public void setD(DBean d) {
        this.d = d;
    }

    public static class DBean {
        /**
         * cmd : high2
         * start_time : 1111111111111111111
         * delay : 200
         */

        private String cmd;
        private long start_time;
        private long end_time;
        private int delay;

        public long getEnd_time() {
            return end_time;
        }

        public void setEnd_time(long end_time) {
            this.end_time = end_time;
        }

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public long getStart_time() {
            return start_time;
        }

        public void setStart_time(long start_time) {
            this.start_time = start_time;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }
}
