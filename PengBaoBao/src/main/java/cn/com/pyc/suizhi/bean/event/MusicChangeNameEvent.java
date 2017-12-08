package cn.com.pyc.suizhi.bean.event;

import cn.com.pyc.bean.event.BaseEvent;

/**
 * 切换歌曲的名称
 */
public class MusicChangeNameEvent extends BaseEvent {

    private String musicName;

    public MusicChangeNameEvent(String musicName) {
        super();
        this.musicName = musicName;
    }

    public String getMusicName() {
        return musicName;
    }

}
