package cn.com.pyc.pbbonline.bean;

import com.sz.mobilesdk.models.BaseModel;

public class LoginBean extends BaseModel
{

	private String token;
	private String password;
	private String flag;
	
	
	public String getFlag()
	{
		return flag;
	}

	public void setFlag(String flag)
	{
		this.flag = flag;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}
	
	
}
