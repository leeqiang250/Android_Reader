package com.sz.mobilesdk.models.xml;

import java.util.HashMap;
import java.util.Map;

import com.sz.mobilesdk.models.BaseModel;
import com.sz.mobilesdk.util.StringUtil;

/**
 * 存储证书信息的类 oex-rights
 */
public class OEX_Rights extends BaseModel
{
	private String assent_id;
	private Map<String, String> contextMap = new HashMap<String, String>();
	private OEX_Agreement agreement;

	public String getAssent_id()
	{
		return assent_id;
	}

	public void setAssent_id(String assent_id)
	{
		this.assent_id = assent_id;
	}

	public Map<String, String> getContextMap()
	{
		return contextMap;
	}

	public void setContextMap(String key, String value)
	{
		if (!StringUtil.isEmpty(key) && !StringUtil.isEmpty(value))
		{
			contextMap.put(key, value);
		}

	}

	public OEX_Agreement getAgreement()
	{
		return agreement;
	}

	public void setAgreement(OEX_Agreement agreement)
	{
		this.agreement = agreement;
	}
}
