package cn.com.pyc.pbbonline.db;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 分享
 * 
 * @author hudq
 */
@Table(name = "Shared")
public class Shared
{

	// id，必须存在列
	@Column(name = "id", isId = true)
	private int id;

	//分享id。唯一性
	@Column(name = "shareId")
	private String shareId;

	//分享名称，即主题Theme
	@Column(name = "theme")
	private String theme;

	//分享者
	@Column(name = "owner")
	private String owner;

	//分享地址
	@Column(name = "shareUrl")
	private String shareUrl;

	//时间
	@Column(name = "time")
	private long time;

	//分享的方式。（按设备，按身份，按人数）
	@Column(name = "shareMode")
	private String shareMode;

	//账户名
	//按设备：guestpbb
	//按身份、按人数：手机号或""。
	@Column(name = "accountName")
	private String accountName;

	//是否是新分享，包括新分享和更新的分享
	@Column(name = "whetherNew")
	private boolean whetherNew;

	//是否更新（whetherNew为true，isUpdate不一定true,反之，isUpdate为true，whetherNew一定为true）
	@Column(name = "isUpdate")
	private boolean isUpdate;

	//是否被收回，默认false
	@Column(name = "isRevoke")
	private boolean isRevoke;

	//标记是否被删除
	@Column(name = "isDelete")
	private boolean isDelete;

	public Shared()
	{
	}

	/**
	 * @param shareId
	 *            分享id
	 * @param theme
	 *            分享主题名称
	 * @param ower
	 *            分享人
	 */
	public Shared(String shareId, String theme, String ower)
	{
		this.shareId = shareId;
		this.theme = theme;
		this.owner = ower;

		//初始化，此三种状态为false
		this.isUpdate = false;
		this.isRevoke = false;
		this.isDelete = false;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getShareId()
	{
		return shareId;
	}

	public void setShareId(String shareId)
	{
		this.shareId = shareId;
	}

	public String getTheme()
	{
		return theme;
	}

	public void setTheme(String theme)
	{
		this.theme = theme;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getShareUrl()
	{
		return shareUrl;
	}

	public void setShareUrl(String shareUrl)
	{
		this.shareUrl = shareUrl;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public String getShareMode()
	{
		return shareMode;
	}

	public void setShareMode(String shareMode)
	{
		this.shareMode = shareMode;
	}

	public String getAccountName()
	{
		return accountName;
	}

	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	public boolean isWhetherNew()
	{
		return whetherNew;
	}

	public void setWhetherNew(boolean whetherNew)
	{
		this.whetherNew = whetherNew;
	}

	public boolean isUpdate()
	{
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate)
	{
		this.isUpdate = isUpdate;
	}

	public boolean isRevoke()
	{
		return isRevoke;
	}

	public void setRevoke(boolean isRevoke)
	{
		this.isRevoke = isRevoke;
	}

	public void setDelete(boolean isDelete)
	{
		this.isDelete = isDelete;
	}

	public boolean isDelete()
	{
		return isDelete;
	}

}
