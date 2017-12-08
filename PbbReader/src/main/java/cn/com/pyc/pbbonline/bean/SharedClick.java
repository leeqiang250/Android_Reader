package cn.com.pyc.pbbonline.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "SharedClick")
public class SharedClick
{
	// id，必须存在列
	@Column(name = "id", isId = true)
	private int id;
	//扫描二维码的id。唯一性
	@Column(name = "shareId")
	private String shareId;

	@Column(name = "isClick")
	private boolean isClick = false;

	//时间
	@Column(name = "time")
	private long time;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getShareId()
	{
		return shareId;
	}

	public void setShareId(String shareId)
	{
		this.shareId = shareId;
	}

	public boolean isClick()
	{
		return isClick;
	}

	public void setClick(boolean isClick)
	{
		this.isClick = isClick;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

}
