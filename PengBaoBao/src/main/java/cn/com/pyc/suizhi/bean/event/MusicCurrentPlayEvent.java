package cn.com.pyc.suizhi.bean.event;

import cn.com.pyc.bean.event.BaseEvent;

/**
 * 当前歌曲的id
 */
public class MusicCurrentPlayEvent extends BaseEvent {

    private String fileId;

    public MusicCurrentPlayEvent(String fileId) {
        super();
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }
}
