package cn.com.pyc.pbbonline.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sz.mobilesdk.util.SZLog;

import cn.com.pyc.pbbonline.model.JPDataBean;
import cn.com.pyc.pbbonline.model.JPushDataBean;

public class ParserJPushBeanUtil
{

	/**
	 * 解析json:<br/>
	 * 
	 * data可能为null
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static JPushDataBean parserJPushJson(String jsonStr)
	{
		JPushDataBean bean = new JPushDataBean();
		try
		{
			JSONObject jObj = new JSONObject(jsonStr);
			if (jObj.has("action"))
			{
				bean.setAction(jObj.optString("action"));
			}
			if (jObj.has("data"))
			{
				bean.setData(parserJPDataBean(jObj.getString("data")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			SZLog.e("parser", "jsonData is invalid ！");
		}
		return bean;
	}

	private static JPDataBean parserJPDataBean(String jsonString) throws org.json.JSONException
	{
		JPDataBean data = new JPDataBean();
		JSONObject jo = new JSONObject(jsonString);
		if (jo.has("create_time"))
			data.setCreate_time(jo.optString("create_time"));
		if (jo.has("message"))
			data.setMessage(jo.optString("message"));
		if (jo.has("num"))
			data.setNum(jo.optString("num"));
		if (jo.has("owner"))
			data.setOwner(jo.getString("owner"));
		if (jo.has("shareID"))
			data.setShareID(jo.getString("shareID"));
		if (jo.has("share_mode"))
			data.setShare_mode(jo.getString("share_mode"));
		if (jo.has("theme"))
			data.setTheme(jo.getString("theme"));
		if (jo.has("url"))
			data.setUrl(jo.getString("url"));
		if (jo.has("folderPath"))
		{
			List<String> folderPaths = new ArrayList<String>();
			JSONArray array = new JSONArray(jo.getString("folderPath"));
			for (int i = 0; i < array.length(); i++)
			{
				SZLog.d("JFolder", array.getString(i));
				folderPaths.add(array.getString(i));
			}
			data.setFolderPath(folderPaths);
		}
		if (jo.has("filePath"))
		{
			List<String> filePaths = new ArrayList<String>();
			JSONArray array = new JSONArray(jo.getString("filePath"));
			for (int i = 0; i < array.length(); i++)
			{
				SZLog.d("JFile", array.getString(i));
				filePaths.add(array.getString(i));
			}
			data.setFilePath(filePaths);
		}

		return data;
	}

}
