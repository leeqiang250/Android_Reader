package cn.com.pyc.pbbonline.bean.event;

import com.sz.mobilesdk.models.FolderInfo;

public class DownloadFolderEvent extends BaseOnEvent
{

	private FolderInfo o;

	public DownloadFolderEvent(FolderInfo o)
	{
		super();
		this.o = o;
	}

	public void setO(FolderInfo o)
	{
		this.o = o;
	}

	public FolderInfo getO()
	{
		return o;
	}

}
