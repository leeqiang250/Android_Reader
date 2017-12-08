package cn.com.pyc.pbbonline.bean.event;

import cn.com.pyc.pbbonline.bean.SZFile;

public class MusicSwitchNameEvent extends BaseOnEvent
{

	private SZFile file;

	public MusicSwitchNameEvent(SZFile file)
	{
		super();
		this.file = file;
	}

	public SZFile getSZFile()
	{
		return file;
	}

}
