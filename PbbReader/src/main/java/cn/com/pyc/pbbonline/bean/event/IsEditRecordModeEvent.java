package cn.com.pyc.pbbonline.bean.event;

public class IsEditRecordModeEvent
{
	private boolean isShow;

	public IsEditRecordModeEvent(boolean isShow)
	{
		super();
		this.isShow = isShow;
	}

	public boolean isShow()
	{
		return isShow;
	}
}
