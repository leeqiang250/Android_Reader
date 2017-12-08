package com.sz.mobilesdk.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.sz.mobilesdk.models.xml.OEX_Agreement;
import com.sz.mobilesdk.models.xml.OEX_Agreement.OEX_Asset;
import com.sz.mobilesdk.models.xml.OEX_Agreement.OEX_Permission;
import com.sz.mobilesdk.models.xml.OEX_Rights;

/**
 * 解析xml工具类
 */
public class PullXMLReader
{

	/* 权限类型 */
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY = "display";
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_PLAY = "play";
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_PRINT = "print";
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE = "execute";
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_EXPORT = "export";

	/**
	 * 读取xml文件里的内容并解析
	 * 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static OEX_Rights readXML(InputStream inputStream) throws Exception
	{
		boolean isFristUID = true;
		boolean isFristAssent = true;

		OEX_Rights right = null;
		OEX_Agreement agreement = null;
		OEX_Asset asset = null;
		OEX_Permission permission = null;
		Map<String, String> map = null;
		String value = null;
		try
		{
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(inputStream, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				String startTag = parser.getName();
				if (eventType == XmlPullParser.START_TAG)
				{
					switch (startTag)
					{
						case "rights":
							right = new OEX_Rights();
							break;
						case "version":
							right.setContextMap("version", parser.nextText());
							break;
						case "uid":
							if (isFristUID)
							{
								right.setContextMap("uid", parser.nextText());
								isFristUID = false;
							} else
							{
								asset.setOdd_uid(parser.nextText());
							}
							break;
						case "agreement":
							agreement = new OEX_Agreement();
							break;
						case "asset":
							if (isFristAssent)
							{
								asset = agreement.new OEX_Asset();
							} else
							{
								permission.setAssent_id(parser
										.getAttributeValue(0));
							}
							break;
						case "DigestMethod":
							asset.setDigest_algorithm_key(parser
									.getAttributeValue(0));
							break;
						case "DigestValue":
							asset.setDigest_algorithm_value(parser.nextText());
							break;
						case "EncryptionMethod":
							asset.setEnc_algorithm(parser.getAttributeValue(0));
							break;
						case "RetrievalMethod":
							asset.setRetrieval_url(parser.getAttributeValue(0));
							break;
						case "CipherValue":
							asset.setCipheralue(parser.nextText());
							break;
						case "permission":
							permission = agreement.new OEX_Permission();
							break;
						case "display":
						case "play":
						case "print":
						case "execute":
						case "export":
							permission.setType(getType(startTag));
							break;
						case "constraint":
							map = new HashMap<String, String>();
							break;
						case "datetime":
							// <o-dd:datetime o-dd:start="1448872142446"
							// o-dd:end="1546531199000">1131</o-dd:datetime>
							// String end = parser.getAttributeValue(0);
							// String start = parser.getAttributeValue(1);
							String startTime = parser.getAttributeValue(null,
									"start");
							String endTime = parser.getAttributeValue(null,
									"end");
							SZLog.v("PullXML", "startTime: " + startTime);
							SZLog.v("PullXML", "endedTime: " + endTime);

							permission.setStartTime(startTime);
							permission.setEndTime(endTime);
							value = parser.nextText();
							permission.setDays(value);
							map.put(startTag, value);
							break;
						case "individual":
							value = parser.nextText();
							permission.setIndividual(value);
							map.put(startTag, value);
							break;
						case "system":
							value = parser.nextText();
							permission.setSystem(value);
							map.put(startTag, value);
							break;
						case "accumulated":
							value = parser.nextText();
							permission.setAccumulated(value);
							map.put(startTag, value);
							break;
					}
				} else if (eventType == XmlPullParser.END_TAG)
				{
					switch (startTag)
					{
						case "asset":
							if (isFristAssent && asset != null)
							{
								agreement.setAssets(asset);
								isFristAssent = false;
							}
							break;
						case "constraint":
							permission.setAttributes(map);
							map = null;
							break;
						case "permission":
							isFristAssent = true;
							agreement.setPermission(permission);
							break;

						case "agreement":
							right.setAgreement(agreement);
							break;
					}

				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		return right;
	}

	// 权限类型
	public static OEX_Agreement.PERMISSION_TYPE getType(String s)
	{
		switch (s)
		{
			case OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY:
				return OEX_Agreement.PERMISSION_TYPE.DISPLAY;
			case OEX_AGREEMENT_PERMISSION_TYPE_PLAY:
				return OEX_Agreement.PERMISSION_TYPE.PLAY;
			case OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE:
				return OEX_Agreement.PERMISSION_TYPE.EXECUTE;
			case OEX_AGREEMENT_PERMISSION_TYPE_EXPORT:
				return OEX_Agreement.PERMISSION_TYPE.EXPORT;
			case OEX_AGREEMENT_PERMISSION_TYPE_PRINT:
				return OEX_Agreement.PERMISSION_TYPE.PRINT;
		}
		return null;
	}

}
