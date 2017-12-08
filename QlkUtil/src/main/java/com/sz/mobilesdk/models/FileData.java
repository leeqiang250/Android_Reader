package com.sz.mobilesdk.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.sz.mobilesdk.manager.DownloadTaskManager2;

@Deprecated
public class FileData implements Parcelable
{
	private String files_id;
	private String id;
	private long last_modify_time;
	private String name;
	private String sharefolder_id;
	private long fileSize;

	// 下载相关
	private String ftpUrl;
	private int position;
	private int taskState = DownloadTaskManager2.INIT;
	private int progress;

	private String sharefolder_publish_date;

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(this.files_id);
		dest.writeString(this.id);
		dest.writeLong(this.last_modify_time);
		dest.writeString(this.name);
		dest.writeString(this.sharefolder_id);
		dest.writeLong(this.fileSize);

		dest.writeString(this.ftpUrl);
		dest.writeInt(this.position);
		dest.writeInt(this.taskState);
		dest.writeInt(this.progress);

		dest.writeString(this.sharefolder_publish_date);
	}

	public final static Parcelable.Creator<FileData> CREATOR = new Creator<FileData>()
	{
		@Override
		public FileData[] newArray(int size)
		{
			return new FileData[size];
		}

		@Override
		public FileData createFromParcel(Parcel source)
		{
			FileData data = new FileData();
			data.setFiles_id(source.readString());
			data.setId(source.readString());
			data.setLast_modify_time(source.readLong());
			data.setName(source.readString());
			data.setSharefolder_id(source.readString());
			data.setFileSize(source.readLong());

			data.setFtpUrl(source.readString());
			data.setPosition(source.readInt());
			data.setTaskState(source.readInt());
			data.setProgress(source.readInt());

			data.setSharefolder_publish_date(source.readString());

			return data;
		}
	};

	public String getFiles_id()
	{
		return files_id;
	}

	public void setFiles_id(String files_id)
	{
		this.files_id = files_id;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public long getLast_modify_time()
	{
		return last_modify_time;
	}

	public void setLast_modify_time(long last_modify_time)
	{
		this.last_modify_time = last_modify_time;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSharefolder_id()
	{
		return sharefolder_id;
	}

	public void setSharefolder_id(String sharefolder_id)
	{
		this.sharefolder_id = sharefolder_id;
	}

	public long getFileSize()
	{
		return fileSize;
	}

	public void setFileSize(long fileSize)
	{
		this.fileSize = fileSize;
	}

	public String getFtpUrl()
	{
		return ftpUrl;
	}

	public void setFtpUrl(String ftpUrl)
	{
		this.ftpUrl = ftpUrl;
	}

	public int getPosition()
	{
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public int getTaskState()
	{
		return taskState;
	}

	public void setTaskState(int taskState)
	{
		this.taskState = taskState;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public String getSharefolder_publish_date()
	{
		return sharefolder_publish_date;
	}

	public void setSharefolder_publish_date(String sharefolder_publish_date)
	{
		this.sharefolder_publish_date = sharefolder_publish_date;
	}

}
