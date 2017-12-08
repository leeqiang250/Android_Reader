package cn.com.pyc.pbbonline.util;

import cn.com.pyc.pbbonline.bean.event.SaveShareRecordEvent;
import cn.com.pyc.pbbonline.common.ShareMode;
import de.greenrobot.event.EventBus;

/**
 * 发送保存分享的通知工具管理类
 */
public class SendMsgShareUtil
{
	/**
	 * 保存分享记录
	 * 
	 * @param theme
	 * @param owner
	 */
	public static void sendMsg2SaveRecord(String theme, String owner)
	{
		EventBus.getDefault().post(new SaveShareRecordEvent(theme, owner));
	}

	/**
	 * 按设备分享,绑定成功，发送通知保存记录
	 * 
	 * @param theme
	 * @param owner
	 */
	public static void sendMsg2SaveRecordByDevice(String theme, String owner)
	{
		if (ShareMode.SHAREDEVICE.equals(ShareMode.Mode.value))
		{
			EventBus.getDefault().post(new SaveShareRecordEvent(theme, owner));
		}
	}

	/**
	 * 按身份、人数分享，领取成功，发送通知保存记录
	 * 
	 * @param theme
	 * @param owner
	 */
	public static void sendMsg2SaveRecordByUser(String theme, String owner)
	{
		if (ShareMode.SHAREUSER.equals(ShareMode.Mode.value)
				|| ShareMode.SHARECOUNT.equals(ShareMode.Mode.value))
		{
			//按身份、人数分享，领取成功，发送通知保存记录
			EventBus.getDefault().post(new SaveShareRecordEvent(theme, owner));
		}
	}

}
