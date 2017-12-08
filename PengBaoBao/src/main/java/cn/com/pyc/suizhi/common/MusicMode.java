package cn.com.pyc.suizhi.common;

/**
 * 音乐播放状态
 * <p>
 * Created by hudq on 2017/3/24.
 */

public final class MusicMode {

    //列表循环
    public static final int CIRCLE = 32;
    //单曲循环
    public static final int SINGLE = 64;
    //随机播放
    public static final int RANDOM = 128;

    //上一首
    public static final int PREVIOUS = 10;
    //下一首
    public static final int NEXT = 11;


    public static class Status {
        //播放
        public static final int PLAY = 1;
        //暂停
        public static final int PAUSE = 2;
        //停止
        public static final int STOP = 3;
        //释放
        public static final int RELEASE = 4;
        //继续
        public static final int CONTINUE = 5;
        //进度
        public static final int PROGRESS = 6;
    }

    public static class Suspend {
        //显示悬浮
        public static final int SHOW = 100;
        //旋转
        public static final int ROTATE = 101;
        //静止
        public static final int HALT = 102;
        //移除
        public static final int REMOVE = 103;
        //停止
        public static final int END = 104;
    }


    /**
     * 播放状态
     */
    public static volatile int STATUS = cn.com.pyc.suizhi.common.MusicMode.Status.STOP;
}
