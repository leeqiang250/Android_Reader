package com.sz.mobilesdk.database.bean;

import java.util.LinkedList;
import java.util.List;

public class Right
{

	private String id = "";
	private String right_uid;
	private String username;
	private String create_time;
	private String pro_album_id;
	private String right_version;
	private String account_id;
	private List<Asset> assets = new LinkedList<Asset>();

	public String getCreate_time()
	{
		return this.create_time;
	}

	public void setCreate_time(String create_time)
	{
		this.create_time = create_time;
	}

	public String getRight_uid()
	{
		return this.right_uid;
	}

	public void setRight_uid(String rights_uid)
	{
		this.right_uid = rights_uid;
	}

	public String getUsername()
	{
		return this.username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getRight_version()
	{
		return this.right_version;
	}

	public void setRight_version(String rights_version)
	{
		this.right_version = rights_version;
	}

	public String getPro_album_id()
	{
		return this.pro_album_id;
	}

	public void setPro_album_id(String pro_album_id)
	{
		this.pro_album_id = pro_album_id;
	}

	public String getAccount_id()
	{
		return this.account_id;
	}

	public void setAccount_id(String account_id)
	{
		this.account_id = account_id;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setAssets(LinkedList<Asset> assets)
	{
		this.assets = assets;
	}

	public List<Asset> getAssets()
	{
		return assets;
	}
}
