package cn.com.pyc.bean.event;

import cn.com.pyc.bean.SmInfo;

/**
 * Created by hudaqiang on 2017/10/24.
 */

public class SeriesPbbFileEvent extends BaseEvent {

    private SmInfo mSmInfo;

    public SeriesPbbFileEvent(SmInfo smInfo) {
        mSmInfo = smInfo;
    }

    public SmInfo getSmInfo() {
        return mSmInfo;
    }
}
