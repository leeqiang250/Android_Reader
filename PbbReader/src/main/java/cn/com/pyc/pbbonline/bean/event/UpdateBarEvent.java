package cn.com.pyc.pbbonline.bean.event;

public class UpdateBarEvent extends BaseOnEvent
{

	private boolean isShowBar;

	public UpdateBarEvent(boolean isShowBar)
	{
		super();
		this.isShowBar = isShowBar;
	}

	public boolean isShowBar()
	{
		return isShowBar;
	}

}
