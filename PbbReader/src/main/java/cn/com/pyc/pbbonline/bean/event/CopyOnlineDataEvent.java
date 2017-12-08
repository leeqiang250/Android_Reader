package cn.com.pyc.pbbonline.bean.event;

public class CopyOnlineDataEvent extends BaseOnEvent
{
	private int order;
	private int progress;

	public CopyOnlineDataEvent()
	{
	}

	public CopyOnlineDataEvent(int order, int progress)
	{
		super();
		this.order = order;
		this.progress = progress;
	}

	public int getProgress()
	{
		return progress;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

}
