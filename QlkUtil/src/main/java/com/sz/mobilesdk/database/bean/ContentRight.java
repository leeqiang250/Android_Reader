package com.sz.mobilesdk.database.bean;

public class ContentRight
{
	public int order;
	public boolean permitted;
	public long availableTime;
	public long odd_datetime_end;

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public boolean getPermitted()
	{
		return permitted;
	}

	public void setPermitted(boolean permitted)
	{
		this.permitted = permitted;
	}

	public long getAvailableTime()
	{
		return availableTime;
	}

	public void setAvailableTime(long availableTime)
	{
		this.availableTime = availableTime;
	}

	public long getOdd_datetime_end()
	{
		return odd_datetime_end;
	}

	public void setOdd_datetime_end(long odd_datetime_end)
	{
		this.odd_datetime_end = odd_datetime_end;
	}

}