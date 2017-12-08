package cn.com.pyc.pbbonline.bean.event;

import com.sz.mobilesdk.models.FolderInfo;

/**
 * 专辑需要更新时候的事件主体
 * 
 * @author hudq
 */
public class UpdateFolderEvent extends BaseOnEvent
{

	private String myProId;
	private FolderInfo o;

	public UpdateFolderEvent(FolderInfo o, String myProId)
	{
		super();
		this.o = o;
		this.myProId = myProId;
	}

	public FolderInfo getO()
	{
		return o;
	}

	public String getMyProId()
	{
		return myProId;
	}

}
