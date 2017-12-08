package com.sz.mobilesdk.database.bean;

import java.util.LinkedList;
import java.util.List;

public class Permission
{
	private String id = "";
	private String asset_id; // 实际上是外键 Permission是表Asset的子表
	private String element; // 有Display play
	private String create_time;
	private String expired; //新加字段
	private List<Perconstraint> perconstraints = new LinkedList<Perconstraint>();

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getAsset_id()
	{
		return asset_id;
	}

	public void setAsset_id(String asset_id)
	{
		this.asset_id = asset_id;
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

	public String getExpired() {
		return expired;
	}

	public void setExpired(String expired) {
		this.expired = expired;
	}

	public void setPerconstraints(List<Perconstraint> perconstraints)
	{
		this.perconstraints = perconstraints;
	}

	public List<Perconstraint> getPerconstraints()
	{
		return perconstraints;
	}

	public void addPerconstraint(Perconstraint perconstraint)
	{
		this.perconstraints.add(perconstraint);
	}
}
