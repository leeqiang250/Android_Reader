package cn.com.pyc.pbbonline.bean.event;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (在鹏宝宝项目中，修改已发送的文件权限后,通知刷新界面。)
 * @date 2016/12/14 17:02
 */
public class RefreshModifyPowerEvent extends BaseOnEvent {

    private String mpath;
    public RefreshModifyPowerEvent()
    {
    }

    public RefreshModifyPowerEvent(String path)
    {
        super();
        this.setMpath(path);
    }


    public String getMpath() {
        return mpath;
    }

    public void setMpath(String mpath) {
        this.mpath = mpath;
    }
}
