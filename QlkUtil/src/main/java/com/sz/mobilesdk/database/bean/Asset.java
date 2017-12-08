package com.sz.mobilesdk.database.bean;

import java.util.LinkedList;
import java.util.List;

public class Asset
{
	private String id = "";
	private String right_id;
	private String asset_uid;
	private String create_time;
	private String digest_method;
	private String right_version;
	private String digest_value;
	private String cek_encrypt_method;
	private String cek_retrieval_key;
	private String cek_cipher_value; // 当前用到的密钥信息
	private String username;
	private List<Permission> permissions = new LinkedList<Permission>();

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getRight_id()
	{
		return right_id;
	}

	public void setRight_id(String right_id)
	{
		this.right_id = right_id;
	}

	public String getAsset_uid()
	{
		return asset_uid;
	}

	public void setAsset_uid(String asset_uid)
	{
		this.asset_uid = asset_uid;
	}

	public String getCreate_time()
	{
		return create_time;
	}

	public void setCreate_time(String create_time)
	{
		this.create_time = create_time;
	}

	public String getRight_version()
	{
		return right_version;
	}

	public void setRight_version(String right_version)
	{
		this.right_version = right_version;
	}

	public String getDigest_method()
	{
		return digest_method;
	}

	public void setDigest_method(String digest_method)
	{
		this.digest_method = digest_method;
	}

	public String getDigest_value()
	{
		return digest_value;
	}

	public void setDigest_value(String digest_value)
	{
		this.digest_value = digest_value;
	}

	public String getCek_retrieval_key()
	{
		return cek_retrieval_key;
	}

	public void setCek_retrieval_key(String cek_retrieval_key)
	{
		this.cek_retrieval_key = cek_retrieval_key;
	}

	public String getCek_encrypt_method()
	{
		return cek_encrypt_method;
	}

	public void setCek_encrypt_method(String cek_encrypt_method)
	{
		this.cek_encrypt_method = cek_encrypt_method;
	}

	public String getCek_cipher_value()
	{
		return cek_cipher_value;
	}

	public void setCek_cipher_value(String cek_cipher_value)
	{
		this.cek_cipher_value = cek_cipher_value;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPermissions(List<Permission> permissions)
	{
		this.permissions = permissions;
	}

	public List<Permission> getPermissions()
	{
		return permissions;
	}

	public void addPermission(Permission permission)
	{
		this.permissions.add(permission);
	}
}
