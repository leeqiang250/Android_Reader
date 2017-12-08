package cn.com.pyc.pbbonline.bean.event;

public class ShareRecordShowBgLoading extends BaseOnEvent
{

	private boolean isShowLoading;

	public ShareRecordShowBgLoading(boolean isShowLoading)
	{
		super();
		this.isShowLoading = isShowLoading;
	}

	public boolean isShowLoading()
	{
		return isShowLoading;
	}

}
