package cn.com.pyc.pbbonline.common;

public final class ShareMode
{
	//按身份限制分享
	public static final String SHAREUSER = "shareuser";
	//按设备限制分享
	public static final String SHAREDEVICE = "sharedevice";
	//按人数限制分享
	public static final String SHARECOUNT = "sharecount";

	public static class Mode
	{
		//默认按设备
		public static String value = SHAREDEVICE;
	}

	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////

	//推送消息 文件新分享
	public static final String NEWSHARE = "NewShare";
	//推送消息 收回分享
	public static final String REVOKESHARE = "RevokeShare";
	//推送消息 更新
	public static final String UPDATA = "updata";
	//推送消息 追加文件
	public static final String ADDFFILE = "AddFile";
	//推送消息 收回文件
	public static final String REVOKEFILE = "RevokeFile";
	//推送消息 收回文件夹
	public static final String REVOKEFOLDER = "RevokeFolder";


	
}
