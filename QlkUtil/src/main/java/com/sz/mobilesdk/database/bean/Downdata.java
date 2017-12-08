package com.sz.mobilesdk.database.bean;

/**
 * 存储当前用户下载的数据
 * 
 * @author hudq
 */
public class Downdata
{
	private String id = "";			// 唯一id
	private String myProduct_id; 	// 文件的唯一ID

	private long completeSize;		// 已完成大小
	private String ftpPath; 		// ftp下载文件路径

	private String isDownload; 		// 0 正常下载，1 下载异常
	private String progress; 		// 下载进度百分比
	private String totalSize; 		// 网络上文件总大小
	private String fileOffsetstr;	// 本地已下载文件大小，格式化显示(eg： 3.2M)
	private String localpath; 		// 本地文件保存路径

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getMyProduct_id()
	{
		return myProduct_id;
	}

	public void setMyProduct_id(String myProduct_id)
	{
		this.myProduct_id = myProduct_id;
	}

	public long getCompleteSize()
	{
		return completeSize;
	}

	public void setCompleteSize(long completeSize)
	{
		this.completeSize = completeSize;
	}

	public String getFtpPath()
	{
		return ftpPath;
	}

	public void setFtpPath(String ftpPath)
	{
		this.ftpPath = ftpPath;
	}

	public String getIsDownload()
	{
		return isDownload;
	}

	public void setIsDownload(String isDownload)
	{
		this.isDownload = isDownload;
	}

	public String getProgress()
	{
		return progress;
	}

	public void setProgress(String progress)
	{
		this.progress = progress;
	}

	public String getTotalSize()
	{
		return totalSize;
	}

	public void setTotalSize(String totalSize)
	{
		this.totalSize = totalSize;
	}

	public String getFileOffsetstr()
	{
		return fileOffsetstr;
	}

	public void setFileOffsetstr(String fileOffsetstr)
	{
		this.fileOffsetstr = fileOffsetstr;
	}

	public String getLocalpath()
	{
		return localpath;
	}

	public void setLocalpath(String localpath)
	{
		this.localpath = localpath;
	}

	@Override
	public String toString()
	{
		return "Downdata [id=" + id + ", myProduct_id=" + myProduct_id + ", completeSize=" + completeSize + ", ftpPath=" + ftpPath + ", isDownload="
				+ isDownload + ", progress=" + progress + ", totalSize=" + totalSize + ", fileOffsetstr=" + fileOffsetstr + ", localpath=" + localpath + "]";
	}

}
