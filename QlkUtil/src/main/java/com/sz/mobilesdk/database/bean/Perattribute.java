package com.sz.mobilesdk.database.bean;

public class Perattribute
{
	private String id = "";
	private String permission_id;
	private String element;
	private String create_time;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getPermission_id()
	{
		return permission_id;
	}

	public void setPermission_id(String permission_id)
	{
		this.permission_id = permission_id;
	}

	public String getElement()
	{
		return element;
	}

	public void setElement(String element)
	{
		this.element = element;
	}

	public String getCreate_time()
	{
		return create_time;
	}

	public void setCreate_time(String create_time)
	{
		this.create_time = create_time;
	}
}
