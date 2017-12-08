package com.sz.mobilesdk.database.bean;

import java.util.LinkedList;
import java.util.List;

public class Perconstraint
{
	private String id = "";
	private String permission_id; // 外键 Perconstraint是表Permission的子表
	private String element; // individual accumulated(累计时间) system(系统)
	private String value; // s001 0(永久有效) 或者时间 机器硬件信息
	private String create_time;
	private List<Perconattribute> perconattributes = new LinkedList<Perconattribute>();

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

	public void setPerconattributes(List<Perconattribute> perconattributes)
	{
		this.perconattributes = perconattributes;
	}

	public List<Perconattribute> getPerconattributes()
	{
		return perconattributes;
	}

	public void addPerconattributes(Perconattribute perconattribute)
	{
		this.perconattributes.add(perconattribute);
	}
}
