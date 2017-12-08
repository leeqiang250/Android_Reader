package com.sz.mobilesdk.database.bean;

public class Perconattribute
{
	private String id = "";
	private String perconstraint_id; // 外键 表Perconattribute是表Perconstraint的子表
	private String element; // 开始时间 截止时间
	private String value;
	private String create_time;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getPerconstraint_id()
	{
		return perconstraint_id;
	}

	public void setPerconstraint_id(String perconstraint_id)
	{
		this.perconstraint_id = perconstraint_id;
	}

	public String getElement()
	{
		return element;
	}

	public void setElement(String element)
	{
		this.element = element;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
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
