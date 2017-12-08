package cn.com.pyc.pbbonline.model;

import java.io.Serializable;
import java.util.List;

public class JPDataBean implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6540864324027551187L;
	private String num;
	private String shareID;
	private String url;
	private String create_time;
	private String message;
	private String theme;
	private String owner;
	private String share_mode;
	private String share_prompt;
	private List<String> filePath;
	private List<String> folderPath;

	
	public String getShare_prompt()
	{
		return share_prompt;
	}

	public void setShare_prompt(String share_prompt)
	{
		this.share_prompt = share_prompt;
	}

	public String getTheme()
	{
		return theme;
	}

	public void setTheme(String theme)
	{
		this.theme = theme;
	}

	public String getCreate_time()
	{
		return create_time;
	}

	public void setCreate_time(String create_time)
	{
		this.create_time = create_time;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public List<String> getFilePath()
	{
		return filePath;
	}

	public void setFilePath(List<String> filePaths)
	{
		this.filePath = filePaths;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getNum()
	{
		return num;
	}

	public void setNum(String num)
	{
		this.num = num;
	}

	public String getShareID()
	{
		return shareID;
	}

	public void setShareID(String shareID)
	{
		this.shareID = shareID;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getShare_mode()
	{
		return share_mode;
	}

	public void setShare_mode(String share_mode)
	{
		this.share_mode = share_mode;
	}

	public List<String> getFolderPath()
	{
		return folderPath;
	}

	public void setFolderPath(List<String> folderPath)
	{
		this.folderPath = folderPath;
	}

	@Override
	public String toString()
	{
		return "JPDataBean [num=" + num + ", shareID=" + shareID + ", url=" + url
				+ ", create_time=" + create_time + ", message=" + message + ", theme=" + theme
				+ ", owner=" + owner + ", share_mode=" + share_mode + ", filePath=" + filePath
				+ ", folderPath=" + folderPath + "]";
	}

}
