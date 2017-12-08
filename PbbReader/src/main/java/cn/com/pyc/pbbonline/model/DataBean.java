package cn.com.pyc.pbbonline.model;

import java.io.Serializable;

/**
 * 文件夹头部数据信息
 */
public class DataBean implements Serializable
{
	/**
	 * "create_time": 1459843852561,
	 * "filecount": 1,
	 * "iswithdraw": false,
	 * "last_modify_time": 1459843852561,
	 * "limit_num": 10,
	 * "max_user_num": 0,
	 * "message": "放假爱上对方金坷垃时间飞快的撒开发的多久啊师傅空间啊速度开了",
	 * "myshare_id": "961276c5-57ff-4c63-8784-8595a07dcb0d",
	 * "owner": "15501149225",
	 * "receive_device_num": 0,
	 * "receive_time": 0,
	 * "receive_user_num": 0,
	 * "received": false,
	 * "share_mode": "shareuser",
	 * "share_time_type": "unlimit",
	 * "theme": "测试按身份分享"
	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = -2441338646396665529L;
	/**
	 * "create_time": 1459844826196,
	 * "filecount": 2,
	 * "iswithdraw": false,
	 * "last_modify_time": 1459844826196,
	 * "max_device_num": 10,
	 * "message": "放大斯洛伐克了；爱的索科洛夫卡桑德拉疯狂拉升地方",
	 * "myshare_id": "a62c88fe-80e0-4f9b-a3bd-bf504ee342db",
	 * "owner": "15501149225",
	 * "receive_device_num": 0,
	 * "share_mode": "sharedevice",
	 * "share_time_type": "unlimit",
	 * "theme": "测试按设备分享"
	 */
	private long create_time;
	private int filecount;
	private boolean iswithdraw;
	private long last_modify_time;
	private int limit_num;
	private int max_user_num;
	private String message;
	private String myshare_id;
	private String url;
	private String owner;
	private int receive_device_num;
	private long receive_time;
	private int receive_user_num;
	private boolean received;
	private String share_mode;
	private String share_time_type;
	private String share_time;
	private String theme;
	private int max_device_num;

	public int getMax_device_num()
	{
		return max_device_num;
	}

	public void setMax_device_num(int max_device_num)
	{
		this.max_device_num = max_device_num;
	}

	public int getReceive_device_num()
	{
		return receive_device_num;
	}

	public void setReceive_device_num(int receive_device_num)
	{
		this.receive_device_num = receive_device_num;
	}

	public boolean isIswithdraw()
	{
		return iswithdraw;
	}

	public void setIswithdraw(boolean iswithdraw)
	{
		this.iswithdraw = iswithdraw;
	}

	public String getMyshare_id()
	{
		return myshare_id;
	}

	public void setMyshare_id(String myshare_id)
	{
		this.myshare_id = myshare_id;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	public String getTheme()
	{
		return theme;
	}

	public void setTheme(String theme)
	{
		this.theme = theme;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getShare_mode()
	{
		return share_mode;
	}

	public void setShare_mode(String share_mode)
	{
		this.share_mode = share_mode;
	}

	public String getShare_time_type()
	{
		return share_time_type;
	}

	public void setShare_time_type(String share_time_type)
	{
		this.share_time_type = share_time_type;
	}

	public String getShare_time()
	{
		return share_time;
	}

	public void setShare_time(String share_time)
	{
		this.share_time = share_time;
	}

	public long getCreate_time()
	{
		return create_time;
	}

	public void setCreate_time(long create_time)
	{
		this.create_time = create_time;
	}

	public long getLast_modify_time()
	{
		return last_modify_time;
	}

	public void setLast_modify_time(long last_modify_time)
	{
		this.last_modify_time = last_modify_time;
	}

	public int getFilecount()
	{
		return filecount;
	}

	public void setFilecount(int filecount)
	{
		this.filecount = filecount;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public int getLimit_num()
	{
		return limit_num;
	}

	public void setLimit_num(int limit_num)
	{
		this.limit_num = limit_num;
	}

	public int getMax_user_num()
	{
		return max_user_num;
	}

	public void setMax_user_num(int max_user_num)
	{
		this.max_user_num = max_user_num;
	}

	public long getReceive_time()
	{
		return receive_time;
	}

	public void setReceive_time(long receive_time)
	{
		this.receive_time = receive_time;
	}

	public int getReceive_user_num()
	{
		return receive_user_num;
	}

	public void setReceive_user_num(int receive_user_num)
	{
		this.receive_user_num = receive_user_num;
	}

	public boolean isReceived()
	{
		return received;
	}

	public void setReceived(boolean received)
	{
		this.received = received;
	}

	/**
	 * 永久有效
	 * 
	 * @return
	 */
	public boolean isUnlimmit()
	{
		return "unlimit".equals(this.share_time_type);
	}

	/**
	 * 从某日期至某日期
	 * 
	 * @return
	 */
	public boolean isDayRange()
	{
		return "dayrange".equals(this.share_time_type);
	}

	/**
	 * 从第一次打开起，n天内有效
	 * 
	 * @return
	 */
	public boolean isDays()
	{
		return "days".equals(this.share_time_type);
	}

}
