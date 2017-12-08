package cn.com.pyc.bean.event;

import cn.com.pyc.conn.SmResult;
import cn.com.pyc.pbbonline.bean.event.BaseOnEvent;

/**
 * Created by hudq on 2017/3/10.
 */

public class SmsResultEvent extends BaseOnEvent {

    private SmResult mResult;

    public SmResult getResult() {
        return mResult;
    }

    public SmsResultEvent(SmResult result) {
        mResult = result;
    }
}
