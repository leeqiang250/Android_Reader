package com.sz.mobilesdk.models;

/**
 * 基础类型model
 * 
 */
public class BaseModel
{

	private boolean result;

	private String msg;

	private String code;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * 成功
	 * 
	 * @return
	 */
	public boolean isSuccess()
	{
		return this.result;
	}

	public String getMsg()
	{
		return this.msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public boolean isResult()
	{
		return result;
	}

	public void setResult(boolean result)
	{
		this.result = result;
	}

}
