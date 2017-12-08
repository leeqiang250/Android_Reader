package cn.com.pyc.pbbonline.bean.event;

public class MusicCircleEvent extends BaseOnEvent
{
	private boolean isPlay;

	public MusicCircleEvent(boolean isPlay)
	{
		super();
		this.isPlay = isPlay;
	}

	public boolean isPlay()
	{
		return isPlay;
	}

}
