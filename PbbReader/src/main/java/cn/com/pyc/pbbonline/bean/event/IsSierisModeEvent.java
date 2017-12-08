package cn.com.pyc.pbbonline.bean.event;

public class IsSierisModeEvent
{
	private int viewType;
	
	public IsSierisModeEvent(int viewType)
	{
		super();
		this.viewType = viewType;
	}
	
	public int getViewType()
	{
		return viewType;
	}

}
