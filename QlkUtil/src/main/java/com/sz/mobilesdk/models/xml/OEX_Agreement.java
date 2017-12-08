package com.sz.mobilesdk.models.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sz.mobilesdk.models.BaseModel;

/**
 * oex_argeement
 */
public class OEX_Agreement extends BaseModel
{
	private List<OEX_Asset> assetsList = new ArrayList<OEX_Asset>();
	private List<OEX_Permission> permissionList = new ArrayList<OEX_Permission>();

	public List<OEX_Asset> getAssets()
	{
		return assetsList;
	}

	public void setAssets(OEX_Asset assets)
	{
		assetsList.add(assets);
	}

	public List<OEX_Permission> getPermission()
	{
		return permissionList;
	}

	public void setPermission(OEX_Permission permission)
	{
		permissionList.add(permission);
	}

	/**
	 * o-ex:asset
	 */
	public class OEX_Asset extends BaseModel
	{
		private String oex_id;
		private String odd_uid;
		private String digest_algorithm_key;
		private String digest_algorithm_value;
		private String enc_algorithm;
		private String retrieval_url;
		private String cipheralue;

		public String getOex_id()
		{
			return oex_id;
		}

		public void setOex_id(String oex_id)
		{
			this.oex_id = oex_id;
		}

		public String getOdd_uid()
		{
			return odd_uid;
		}

		public void setOdd_uid(String odd_uid)
		{
			this.odd_uid = odd_uid;
		}

		public String getDigest_algorithm_key()
		{
			return digest_algorithm_key;
		}

		public void setDigest_algorithm_key(String digest_algorithm_key)
		{
			this.digest_algorithm_key = digest_algorithm_key;
		}

		public String getDigest_algorithm_value()
		{
			return digest_algorithm_value;
		}

		public void setDigest_algorithm_value(String digest_algorithm_value)
		{
			this.digest_algorithm_value = digest_algorithm_value;
		}

		public String getEnc_algorithm()
		{
			return enc_algorithm;
		}

		public void setEnc_algorithm(String enc_algorithm)
		{
			this.enc_algorithm = enc_algorithm;
		}

		public String getRetrieval_url()
		{
			return retrieval_url;
		}

		public void setRetrieval_url(String retrieval_url)
		{
			this.retrieval_url = retrieval_url;
		}

		public String getCipheralue()
		{
			return cipheralue;
		}

		public void setCipheralue(String cipheralue)
		{
			this.cipheralue = cipheralue;
		}

	}

	/**
	 * 对应xml文件中的oex-permission
	 */
	public class OEX_Permission
	{

		private String assent_id;
		private String endTime;
		private String startTime;
		private String days;
		private String individual;
		private String accumulated;
		private String system;// 记录绑定的操作系统
		private String mode;// 表示数字输出的模式
		private PERMISSION_TYPE type;

		private List<Map<String, String>> attributes = new ArrayList<Map<String, String>>();

		public List<Map<String, String>> getAttributes()
		{
			return attributes;
		}

		public String getEndTime()
		{
			return endTime;
		}

		public void setAttributes(Map<String, String> attributeMap)
		{
			this.attributes.add(attributeMap);
		}

		public void setType(PERMISSION_TYPE type)
		{
			this.type = type;
		}

		public String getAssent_id()
		{
			return assent_id;
		}

		public void setAssent_id(String assent_id)
		{
			this.assent_id = assent_id;
		}

		public void setEndTime(String endTime)
		{
			this.endTime = endTime;
		}

		public String getStartTime()
		{
			return startTime;
		}

		public void setStartTime(String startTime)
		{
			this.startTime = startTime;
		}

		public String getDays()
		{
			return days;
		}

		public void setDays(String days)
		{
			this.days = days;
		}

		public String getIndividual()
		{
			return individual;
		}

		public void setIndividual(String individual)
		{
			this.individual = individual;
		}

		public String getAccumulated()
		{
			return accumulated;
		}

		public void setAccumulated(String accumulated)
		{
			this.accumulated = accumulated;
		}

		public String getSystem()
		{
			return system;
		}

		public void setSystem(String system)
		{
			this.system = system;
		}

		public String getMode()
		{
			return mode;
		}

		public void setMode(String mode)
		{
			this.mode = mode;
		}

		public PERMISSION_TYPE getType()
		{
			return type;
		}
	}

	public enum PERMISSION_TYPE
	{
		DISPLAY, PLAY, PRINT, EXECUTE, EXPORT;
	}

}
