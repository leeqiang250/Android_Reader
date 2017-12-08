package com.sz.mobilesdk.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.sz.mobilesdk.manager.DownloadTaskManager;

import java.util.List;
import java.util.Map;

/**
 * 要下载的文件夹信息. <br/>
 */
@Deprecated
public class FolderInfo implements Parcelable
{
	private boolean active;
	private String authors;
	private String myProId;
	private String picture_ratio;
	private String picture_url;
	private String publishDate;
	private long folderSize;
	private String productName;
	private transient Map<String,String> fileIds;			//有效的id（id : file_id）
	private transient List<String> delFileIds;				//删除的文件id

	// 下载相关
	// 任务状态，初始状态DownloadTaskManager.INIT
	private int taskState = DownloadTaskManager.INIT;
	private int position;		// 下载的item位置
	private int progress;		// 下载保存进度
	private long currentSize;	// 当前下载大小
	private long totalSize;		// 总大小
	private String ftpUrl;		// 下载对应的ftpPath。没有即为空
	private String theme;		// 对应的分享名字

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		/** 必须按照变量顺序,否则出错，但是Parcelable效率高 */

		dest.writeInt(this.active ? 1 : 0);
		dest.writeString(this.authors);
		dest.writeString(this.myProId);
		dest.writeString(this.picture_ratio);
		dest.writeString(this.picture_url);
		dest.writeString(this.publishDate);
		dest.writeLong(this.folderSize);
		dest.writeString(this.productName);

		dest.writeInt(this.taskState);
		dest.writeInt(this.position);
		dest.writeInt(this.progress);
		dest.writeLong(this.currentSize);
		dest.writeLong(this.totalSize);
		dest.writeString(this.ftpUrl);
		dest.writeString(this.theme);
	}

	public final static Parcelable.Creator<FolderInfo> CREATOR = new Creator<FolderInfo>()
	{

		@Override
		public FolderInfo[] newArray(int size)
		{

			return new FolderInfo[size];
		}

		@Override
		public FolderInfo createFromParcel(Parcel source)
		{
			FolderInfo o = new FolderInfo();
			o.setActive((source.readInt() == 1) ? true : false);
			o.setAuthors(source.readString());
			o.setMyProId(source.readString());
			o.setPicture_ratio(source.readString());
			o.setPicture_url(source.readString());
			o.setPublishDate(source.readString());
			o.setFolderSize(source.readLong());
			o.setProductName(source.readString());

			o.setTaskState(source.readInt());
			o.setPosition(source.readInt());
			o.setProgress(source.readInt());
			o.setCurrentSize(source.readLong());
			o.setTotalSize(source.readLong());
			o.setFtpUrl(source.readString());
			o.setTheme(source.readString());
			return o;
		}
	};

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public void setAuthors(String authors)
	{
		this.authors = authors;
	}

	public String getAuthors()
	{
		return authors;
	}

	public String getMyProId()
	{
		return myProId;
	}

	public void setMyProId(String myProId)
	{
		this.myProId = myProId;
	}

	public String getPicture_ratio()
	{
		return picture_ratio;
	}

	public void setPicture_ratio(String picture_ratio)
	{
		this.picture_ratio = picture_ratio;
	}

	public String getPicture_url()
	{
		return picture_url;
	}

	public void setPicture_url(String picture_url)
	{
		this.picture_url = picture_url;
	}

	public String getPublishDate()
	{
		return publishDate;
	}

	public void setPublishDate(String publishDate)
	{
		this.publishDate = publishDate;
	}

	public long getFolderSize()
	{
		return folderSize;
	}

	public void setFolderSize(long folderSize)
	{
		this.folderSize = folderSize;
	}

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
	}
	
	public Map<String, String> getFileIds()
	{
		return fileIds;
	}
	
	public void setFileIds(Map<String, String> fileIds)
	{
		this.fileIds = fileIds;
	}
	
	public List<String> getDelFileIds()
	{
		return delFileIds;
	}
	
	public void setDelFileIds(List<String> delFileIds)
	{
		this.delFileIds = delFileIds;
	}

	public int getTaskState()
	{
		return taskState;
	}

	public void setTaskState(int taskState)
	{
		this.taskState = taskState;
	}

	public int getPosition()
	{
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public long getCurrentSize()
	{
		return currentSize;
	}

	public void setCurrentSize(long currentSize)
	{
		this.currentSize = currentSize;
	}

	public long getTotalSize()
	{
		return totalSize;
	}

	public void setTotalSize(long totalSize)
	{
		this.totalSize = totalSize;
	}

	public void setFtpUrl(String ftpUrl)
	{
		this.ftpUrl = ftpUrl;
	}

	public String getFtpUrl()
	{
		return ftpUrl;
	}

	public String getTheme()
	{
		return theme;
	}

	public void setTheme(String theme)
	{
		this.theme = theme;
	}

}
