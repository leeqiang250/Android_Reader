package cn.com.pyc.pbbonline.model;

import com.sz.mobilesdk.models.BaseModel;

/**
 * 校验后返回数值对象模型！
 * 
 * @author hudq
 */
public class CheckResultBean extends BaseModel
{
	private String ftpUrl;

	public void setFtpUrl(String ftpUrl)
	{
		this.ftpUrl = ftpUrl;
	}

	public String getFtpUrl()
	{
		return ftpUrl;
	}
}
