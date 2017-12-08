package cn.com.pyc.pbbonline.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 保存的索引表 <br\>
 * 代码混淆时候注解必须
 * 
 * @author qd
 */

@Table(name = "ClickIndex")
public class ClickIndex
{

	// id，必须存在列
	@Column(name = "id", isId = true)
	private int id;
	// 保存的阅读进度索引
	@Column(name = "positonIndex")
	private String positonIndex;
	// 专辑对应的myProId
	@Column(name = "myProId")
	private String myProId;
	// 保存的时间
	@Column(name = "time")
	private long time;
	// 文件的contentid
	@Column(name = "contentId")
	private String contentId;
	// 保存索引对应的文件类型
	@Column(name = "fileType")
	private String fileType;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getPositonIndex()
	{
		return positonIndex;
	}

	public void setPositonIndex(String positonIndex)
	{
		this.positonIndex = positonIndex;
	}

	public String getMyProId()
	{
		return myProId;
	}

	public void setMyProId(String myProId)
	{
		this.myProId = myProId;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}
	
	public void setContentId(String contentId)
	{
		this.contentId = contentId;
	}
	
	public String getContentId()
	{
		return contentId;
	}

	public void setFileType(String fileType)
	{
		this.fileType = fileType;
	}

	public String getFileType()
	{
		return fileType;
	}

}
