package cn.com.pyc.bean.event;

/**
 * Created by hudaqiang on 2017/9/22.
 */

public class ClearPBBandSZEvent extends BaseEvent {

    private boolean isComplete;

    public ClearPBBandSZEvent(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public boolean isComplete() {
        return isComplete;
    }
}
