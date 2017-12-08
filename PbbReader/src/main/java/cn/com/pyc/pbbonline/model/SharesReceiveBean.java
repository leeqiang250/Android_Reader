package cn.com.pyc.pbbonline.model;

import java.io.Serializable;

public class SharesReceiveBean implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3777275692522187959L;
	private String id;
	private String account_id;
	private String theme;
	private String message;
	private String share_mode;
	private String share_time_type;
	private String share_time;
	private String receiveTime;
	private String isshared;
	private String create_time;
	private String last_modify_time;
	private String active;
	private String owner;
	private String received;
	private String url;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getAccount_id()
	{
		return account_id;
	}

	public void setAccount_id(String account_id)
	{
		this.account_id = account_id;
	}

	public String getTheme()
	{
		return theme;
	}

	public void setTheme(String theme)
	{
		this.theme = theme;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getShare_mode()
	{
		return share_mode;
	}

	public void setShare_mode(String share_mode)
	{
		this.share_mode = share_mode;
	}

	public String getShare_time_type()
	{
		return share_time_type;
	}

	public void setShare_time_type(String share_time_type)
	{
		this.share_time_type = share_time_type;
	}

	public String getShare_time()
	{
		return share_time;
	}

	public void setShare_time(String share_time)
	{
		this.share_time = share_time;
	}

	public String getIsshared()
	{
		return isshared;
	}

	public void setIsshared(String isshared)
	{
		this.isshared = isshared;
	}

	public String getCreate_time()
	{
		return create_time;
	}

	public void setCreate_time(String create_time)
	{
		this.create_time = create_time;
	}

	public String getLast_modify_time()
	{
		return last_modify_time;
	}

	public void setLast_modify_time(String last_modify_time)
	{
		this.last_modify_time = last_modify_time;
	}

	public String getActive()
	{
		return active;
	}

	public void setActive(String active)
	{
		this.active = active;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getReceived()
	{
		return received;
	}

	public void setReceived(String received)
	{
		this.received = received;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public void setReceiveTime(String receiveTime)
	{
		this.receiveTime = receiveTime;
	}

	public String getReceiveTime()
	{
		return receiveTime;
	}

}
