package com.qlk.util.event;

/**
 * Created by jerry on 2017/9/1.
 * <p>
 * 通过Eventbus 把本地搜索到到路径 发送给接收界面 CodeAndReaderActivity
 * <p>
 * 本类作为传递数据到承载
 */

public class PathsEvent {

    public static final byte P_PATH = 10;
    public static final byte P_CLIPER = 11;
    //public static final byte P_FAIL = 12;
    //public static final byte P_NO_DATA = 13;

    private int type;

    public PathsEvent(int type) {

        this.type = type;
    }

    public int getType() {
        return type;
    }
}
