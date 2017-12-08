package cn.com.pyc.suizhi.bean.event;

import cn.com.pyc.suizhi.model.FileData;

/**
 * 文件处理操作
 * Created by hudaqiang on 2017/7/14.
 */
public class FileHandleEvent extends cn.com.pyc.bean.event.BaseEvent {

    private int type;
    private FileData data;

    public FileHandleEvent(int type) {
        this.type = type;
    }

    public FileHandleEvent(int type, FileData data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public FileData getData() {
        return data;
    }
}
