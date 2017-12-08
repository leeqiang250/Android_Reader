package cn.com.pyc.bean.event;

/**
 * Created by songyumei on 2017/8/9.
 */
public class BaseEvent {
    public static class Type {
        public static final byte UPDATE_SETTING = -1;//更新个人中心界面
        public static final byte UPDATE_DISCOVER = 1;//更新发现界面
        public static final byte UI_BROWSER_FINISH = 2;      //结束BrowserUI

        public static final byte FILE_UPDATE = 21;     //文件更新
        public static final byte FILE_CANCEL = 24;     //文件下载取消

        public static final byte UI_PIC_ICON = 50;//更新用户头像
        public static final byte UI_HOME_TAB_1 = 51;//切换到第1个tab
        public static final byte UI_HOME_TAB_2 = 52;//切换到第2个tab
        public static final byte UI_HOME_TAB_3 = 53;//切换到第3个tab;
    }
}
