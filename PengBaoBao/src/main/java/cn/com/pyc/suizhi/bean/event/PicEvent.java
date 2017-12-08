package cn.com.pyc.suizhi.bean.event;

import cn.com.pyc.bean.event.BaseEvent;

/**
 * Created by songyumei on 2017/8/28.
 */
public class PicEvent extends BaseEvent{
    private int type;
    private String picUrl;

    public PicEvent(int type, String picUrl) {
        this.type = type;
        this.picUrl = picUrl;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
