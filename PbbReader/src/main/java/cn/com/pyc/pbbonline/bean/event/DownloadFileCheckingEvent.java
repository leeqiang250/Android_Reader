package cn.com.pyc.pbbonline.bean.event;

import com.sz.mobilesdk.models.FileData;

public class DownloadFileCheckingEvent extends BaseOnEvent
{

	private FileData data;

	public DownloadFileCheckingEvent()
	{
	}

	public DownloadFileCheckingEvent(FileData data)
	{
		super();
		this.data = data;
	}

	public FileData getData()
	{
		return data;
	}

	public void setData(FileData data)
	{
		this.data = data;
	}

}
