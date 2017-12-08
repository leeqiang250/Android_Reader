package cn.com.pyc.bean.event;


/**
 * Created by songyumei on 2017/8/9.
 */
public class ConductUIEvent extends BaseEvent {
    private int type;

    public ConductUIEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
