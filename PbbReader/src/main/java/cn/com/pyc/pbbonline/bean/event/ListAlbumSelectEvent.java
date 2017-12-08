package cn.com.pyc.pbbonline.bean.event;

public class ListAlbumSelectEvent extends BaseOnEvent
{
	private String contentId;
	private String fileType;

	public ListAlbumSelectEvent(String contentId, String fileType)
	{
		super();
		this.contentId = contentId;
		this.fileType = fileType;
	}

	public String getContentId()
	{
		return contentId;
	}

	public String getFileType()
	{
		return fileType;
	}

}
