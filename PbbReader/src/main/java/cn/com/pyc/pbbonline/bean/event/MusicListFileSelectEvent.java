package cn.com.pyc.pbbonline.bean.event;

// 专辑下文件，音乐时点击文件消息通知。
// 已废弃
public class MusicListFileSelectEvent extends BaseOnEvent
{
	private String contentId;
	private String name;

	public MusicListFileSelectEvent(String contentId, String name)
	{
		super();
		this.contentId = contentId;
		this.name = name;
	}

	public String getContentId()
	{
		return contentId;
	}

	public String getName()
	{
		return name;
	}
}
