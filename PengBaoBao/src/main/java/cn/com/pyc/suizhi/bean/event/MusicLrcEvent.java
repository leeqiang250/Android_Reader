package cn.com.pyc.suizhi.bean.event;

import cn.com.pyc.bean.event.BaseEvent;

/**
 * 处理歌词通知
 */

public class MusicLrcEvent extends BaseEvent {

    public enum Way {
        //加载，更新，切换
        LRC_LOAD, LRC_UPDATE, LRC_CHANGE
    }

    private Object obj;
    private Way way = Way.LRC_LOAD;

    public MusicLrcEvent(Object obj, Way way) {
        super();
        this.obj = obj;
        this.way = way;
    }

    public Object getObj() {
        return obj;
    }

    public Way getWay() {
        return way;
    }
}
