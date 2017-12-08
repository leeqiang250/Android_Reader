package cn.com.pyc.suizhi.manager.db;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 存储当前用户下载文件的数据
 */
@Table(name = "DownDataPat")
public class DownDataPat
{

	// id，必须存在列
	@Column(name = "id", isId = true)
	private int id;

	@Column(name = "myProId")
	private String myProId; // 专辑id(一个专辑对应多个文件)

	@Column(name = "fileId")
	private String fileId; // 文件id

	@Column(name = "fileName")
	private String fileName; // 文件名

	@Column(name = "fileSize")
	private long fileSize;// 文件大小

	@Column(name = "downloadUrl")
	private String downloadUrl; // 下载Url路径

	@Column(name = "localPath")
	private String localPath; // 本地路径

	@Column(name = "completeSize")
	private long completeSize;// 已完成大小

	@Column(name = "progress")
	private int progress; // 进度

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getMyProId()
	{
		return myProId;
	}

	public void setMyProId(String myProId)
	{
		this.myProId = myProId;
	}

	public String getFileId()
	{
		return fileId;
	}

	public void setFileId(String fileId)
	{
		this.fileId = fileId;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getLocalPath()
	{
		return localPath;
	}

	public void setLocalPath(String localPath)
	{
		this.localPath = localPath;
	}

	public long getCompleteSize()
	{
		return completeSize;
	}

	public void setCompleteSize(long completeSize)
	{
		this.completeSize = completeSize;
	}

	public long getFileSize()
	{
		return fileSize;
	}

	public void setFileSize(long fileSize)
	{
		this.fileSize = fileSize;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}
	
}
