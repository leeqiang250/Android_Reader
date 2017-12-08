package cn.com.pyc.pbbonline.model;

import java.io.Serializable;

import cn.com.pyc.pbbonline.model.JPDataBean;

public class JPushDataBean implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9146633200917946985L;
	private String action;
	private JPDataBean data;

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public JPDataBean getData()
	{
		return data;
	}

	public void setData(JPDataBean data)
	{
		this.data = data;
	}

	@Override
	public String toString()
	{
		return "JPushDataBean [action=" + action + ", data=" + data + "]";
	}
	
}
