package cn.com.pyc.pbbonline.bean.event;

public class SaveShareRecordEvent extends BaseOnEvent
{
	private String theme;
	private String owner;

	public SaveShareRecordEvent(String theme, String owner)
	{
		super();
		this.theme = theme;
		this.owner = owner;
	}

	public String getTheme()
	{
		return theme;
	}

	public String getOwner()
	{
		return owner;
	}

}
