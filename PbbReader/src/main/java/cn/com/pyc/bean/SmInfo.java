package cn.com.pyc.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import com.qlk.util.tool.DataConvert;

import cn.com.pyc.utils.Constant;
import android.text.TextUtils;

/**
 * 本类包含协议中的所有字段<br>
 * 对于有“二义性”的字段，则根据各个意义拆分
 * <p>
 * 只有自由传播有“每次能看”<br>
 * 只有付费文件有“不能查看限制条件”
 * 
 * @author QiLiKing 2014-11-20
 */
public class SmInfo implements Serializable
{
	/*-
	 * 注意：sucOpenFlag和stopRead
	 * 	1.单个刷新时：根据服务器返回值suc可以直观分析出sucOpenFlag和stopRead的值
	 * 	2.批量刷新是：只有stopRead可以确定，而sucOpenFlag的值放在
	 * 	    调用getSucOpenFlag的时候分析
	 */

	// 文件能否正常浏览的一些标识，主要用于adapter的groupView的“邮件”图标颜色
	public enum SucFlag
	{
		/**
		 * 可以打开，还没看过
		 */
		NeverOpen,

		/**
		 * 可以打开，已经看过了
		 */
		CanOpen,

		/**
		 * 不能打开，还没看过
		 */
		NeverOpenLimitOut,

		/**
		 * 不能打开，已经看过了
		 */
		LimitOut
	}

	public static final int NO_SID = -1;

	private static final long serialVersionUID = 1L;
	public static final String REMARK_DEFAULT = "你懂的，直接看吧！";

	// 必须填写的信息---1、2、4是服务器定义的，不能修改。映射字段bindnum
	private static final int MUST_QQ = 1;
	private static final int MUST_PHONE = 2;
	private static final int MUST_EMAIL = 4;

	// 自定义字段保密---1、2、3是服务器定义的，不能修改。映射字段ID
	private static final int SECRET_DEFINE1 = 1;
	private static final int SECRET_DEFINE2 = 2;
	private static final int SECRET_DEFINE_BOTH = 3;

	public static final int SUCCESS_CAN_OPEN = 1;			// 可以打开，并且已经看过至少一次了，见SUCCESS_NEVER_OPEN
	public static final int SUCCESS_LIMIT_OUT = -4;			// 限制条件过期
	public static final int SUCCESS_NEVER_OPEN = -5;			// 一次都没打开过，还没过期
	public static final int SUCCESS_NEVER_OPEN_LIMIT_OUT = -6;			// 一次都没打开过，并且已过期，不能看了

	/*
	 * 排序遵从设计文档
	 * 对于文档中有二义性的字段，本类皆为每一种意义定义一个新的字段
	 */
	private int openCount;
	private int openedCount;									// !<
	// 文件已经被打开次数|离线验证时传给服务器
	private int canPrint;
	private int makerAllowed;									// 制作者是否允许打开，文档的iCanOpen
	private int fid;											// 数据库记录ID
	// |
	// 查看时返回自定义字段是否保密输入
	// 1：字段1需保密
	// 2：字段2需保密
	// 3：两个都需保密
	private int secret;										// 自定义字段是否保密输入。映射字段：fid
	private int singleOpenTime;								// 单次阅读限制时间（秒）
	private int appType;										// 客户端android:28;winphone:29;ios:30;pc:18
	private int random;
	private int version;										// 客户端版本，用来比对是否过期
	// |
	// 能查看时，返回查看记录ID，结束查看时调用结束业务，传给服务器
	private int playId;										// 查看记录ID。映射字段：version
	private int payFile;										// 是否是付费文件（根据suc返回值判断）
	private int days;											// payFile的天数，0表示无限制
	private int years;											// payFile的年数，0表示无限制
	private int notice;										// 通知个数（几乎每个业务都有值）
	private int remainDays;
	private int remainYears;
	private int orderId;										// 订单id，文档的ooid
	private int bindNum;										// 绑定机器数|
	// 需要申请时为必填项（1：QQ
	// 2:PHONE
	// 4:EMAIL，其它值则是原来的规则）
	private int contactMust;									// qq,email,phone是否是必填项。映射字段bindnum
	private int activeNum;										// 激活数
	// |
	// 自定义表中选择的ID（制作时传输，pc端）|
	// 自定义字段个数（查看时接收）
	private int selfMust;										// 自定义字段个数。映射字段activeNum
	private int showLimit;										// 1:查看时不显示条件信息（制作与查看，1表示允许，只payFile有效）
	// 2：无需再次申请（制作时，pc端）4：需要水印（制作时，pc端）。8:需要验证码。文档的otherset
	private int applyId;										// 申请的ID，如果重新申请，需提交该ID，离线阅读的申请也要提交该值
	private int needApply;										// 需要申请激活（查看时从suc返回值确定）
	private int needReApply;									// 需要重新申请（pc端驳回）
	// private int offline; // 是否是离线文件。文档的iCanClient
	private int tableId;										// PC制作时，传输选择的exce表ID
	private int fileVersion;									// 1表示文件有3个结构（离线结构）
	private int showDiff;										// 客户端显示效果，申请激活后的提示效果：PC端变背景，移动端变字体颜色

	private String userName;										// 制作者的用户名
	private String fileName;										// 文件名（包括.pbb)
	private String filePath;										// 文件真实路径
	private String startTime;										// ""表示无限制
	private String endTime;										// ""表示无限制
	private String outData;										// 付费文件过期时间
	private String mac;											// !<
	// 本机网卡MAC地址
	private String nick;											// 卖家昵称（查看时下传）|机器名（制作时上传）
	private String remark;										// 外发文件描述文本
	private String versionStr;									// 鹏保宝版本号
	private String email;
	private String qq;
	private String phone;											// 电话，告诉对方联系方式|自由传播查看时验证码，|
	// 自由传播时获取验证码手机号
	private String securityCode;									// 验证码。对应字段phone
	private String emailBuyer;									// 离线文件存储的是买家的联系方式；数据库中存储的永远是卖家的联系方式
	private String qqBuyer;
	private String phoneBuyer;
	private String hardNo;										// 客户端一个唯一的标识
	private String sysInfo;										// 客户端系统信息
	private byte[] hash;
	private byte[] encodeKey;										// !!!它在任何时候都不要清空，会影响打开时的判断
	private byte[] sessionKey;
	private String firstOpenTime;									// 离线文件时有效
	private String makeTime;										// 对应文档的outtime
	private String orderNo;										// 订单编号，不同于orderId
	private String selfDefineKey1;								// hardNo
	private String selfDefineKey2;								// userName
	private String selfDefineValue1;								// 对应文档的selfDefine1
	private String selfDefineValue2;								// 对应文档的selfDefine2
	private String showInfo;										// 客户端显示，申请激活处理结果，未处理|处理结果
	private String msgId;											// 自由传播时，及获取验证码时传输验证码消息ID

	private int timeModify;									// 时间修改标志，如果检测到时间修改，置该字段为1，离线查看流程中只要检测到该字段为1，就提示去联网验证，只有联网验证后，该字段才置为0；
	private String lastOpenTime;

	private String uid;											// 为广告特设

	private int actived;										// 为离线结构特设

	private long codeLen;		// 文件加密的长度
	private long offset;	//二代加密算法导致正文的偏移量
	private long fileLen;

	private String seriesName; //xiong-系列名称
	private int sid;
	private int seriseFilesNum;
	private int seriesReceiveNum;	//已经接收的系列文件
	private String shopUrl;

	

	public SmInfo()
	{
		setRandom(ProtocolInfo.random());
	}

	public boolean isNewFile()
	{
		return (fileVersion & 4) > 0;
	}

	/**
	 * 清空所有与买家有关的信息
	 */
	public void resetApplyInfo()
	{
		// 这四个不能复原，要根据他们判断是否过期的
		// openedCount = 0;
		// remainDays = 0;
		// remainYears = 0;
		// endTime = startTime; //批量时，根据它算出remainXX

		// offline = 0; //这个需要保留
		applyId = 0;
		needApply = Constant.C_TRUE;
		firstOpenTime = null;

		// 如果想在下次申请时直接显示出上次信息，则可以不清空
		// emailBuyer = null;
		// qqBuyer = null;
		// phoneBuyer = null;
		// selfDefineValue1 = null;
		// selfDefineValue2 = null;
	}

	/**
	 * encodeKey是否有效
	 */
	public boolean hasValidEncodeKey()
	{
		byte[] temp = getEncodeKey();
		for (int i = 0; i < temp.length; i++)
		{
			if (temp[i] != 0)
			{
				return true;
			}
		}
		return false;	// 全是0，表示为空
	}

	/**
	 * 限制条件有效
	 * 
	 * @return
	 */
	public boolean valid()
	{
		return isPayFile() || isCountLimit() || isFreeDataLimit();		// 自由传播必须选一个；手动激活可以不选
	}

	/**
	 * 限制条件无效---联网失败或者无效返回值
	 * 
	 * @return
	 */
	public boolean invalid()
	{
		return !valid();
	}


	public int getFileType(){

		String path = getFilePath();

		if (path != null && !TextUtils.isEmpty(path)){
			String p = path.substring(path.length()-8,path.length());
			String m = p.substring(1,4);
			if (m.equals("mp4")){
				return 3;
			}else if (m.equals("pdf")){
				return 4;
			}else if (m.equals("jpg") || m.equals("jpeg") || m.equals("png")){
				return 5;
			}else if (m.equals("mp3")){
				return 6;
			}

		}
		return 0;
	}
	/**
	 * 文件可不可以打开，不是直接根据suc返回值判断的，而是根据各个条件判断
	 * 
	 * @return
	 */
	public boolean canOpen()
	{
		SucFlag flag = getSucOpenFlag();
		return flag.equals(SucFlag.CanOpen) || flag.equals(SucFlag.NeverOpen);
	}

	/*
	 * 因为setSucOpenFlag只有单个刷新时被调用，批量刷新时并无同步修改sucOpenFlag的值，所以此处要做判断
	 */
	public SucFlag getSucOpenFlag()
	{
		if (getNeedApply() == Constant.C_TRUE)
		{
			return SucFlag.NeverOpenLimitOut;
		}

		boolean userAllowed = isPayFile() ? true : isMakerAllowed();
		boolean countAllowed = !isCountLimit() || getLeftCount() > 0;

		/*-
		 * 先判断日期无限制的情况（1），如果有限制，再根据具体信息判断是否过期（2或3）
		 */
		boolean dataAllowed = !isFreeDataLimit() || !isPayDataLimit();	// 1
		if (isFreeDataLimit())	// 同时也意味着它是自由传播文件 // 2
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
			String todayTime = sdf.format(System.currentTimeMillis());
			try
			{
				long start = sdf.parse(getStartTime()).getTime();
				long end = sdf.parse(getEndTime()).getTime();
				long today = sdf.parse(todayTime).getTime();
				dataAllowed = start <= today && end >= today;
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		if (isPayDataLimit())	// 同时也意味着它是付费文件 // 3
		{
			dataAllowed = true;
			if (!TextUtils.isEmpty(getFirstOpenTime()))
			{
				dataAllowed = getRemainDays() + getRemainYears() > 0;
			}
		}

		if (getOpenedCount() == 0)	// 没看过
		{
			if (userAllowed && countAllowed && dataAllowed)
			{
				return SucFlag.NeverOpen;
			}
			else
			{
				return SucFlag.NeverOpenLimitOut;
			}
		}
		else
		// 已经看过了
		{
			if (userAllowed && countAllowed && dataAllowed)
			{
				return SucFlag.CanOpen;
			}
			else
			{
				return isPayFile() ? SucFlag.NeverOpenLimitOut : SucFlag.LimitOut;	// 加入payFile判断是为了在文件过期后还未再次激活前，查看和刷新业务信息的展示一致
			}
		}
	}

	/**
	 * 时间是否限制---自由
	 * 
	 * @return
	 */
	public boolean isFreeDataLimit()
	{
		String start = getStartTime();
		String end = getEndTime();
		// 虽然提交时是""，但服务器返回的是"1900-01-01" (pc端端是 1980-01-01或者1990-01-01)
		// 用&&，是因为老版本离线文件用的是endTime字段
		return !((start.equals("1900-01-01") && end.equals("1900-01-01"))
				|| (start.equals("") && end.equals(""))
				|| (start.equals("1980-01-01") && end.equals("1980-01-01")));
	}

	/**
	 * 时间是否限制---付费
	 * 
	 * @return
	 */
	public boolean isPayDataLimit()
	{
		return  getYears() + getDays() > 0;
	}

	/**
	 * 是否是无限天---付费
	 *
	 * @return
	 */
	public boolean isPayDaysUnLimit()
	{
		return  getDays() >= 0;
	}

	/**
	 * 剩余次数（付费和自由都调用此接口查询）
	 * 
	 * @return
	 */
	public int getLeftCount()
	{
		int left = getOpenCount() - getOpenedCount();
		if (left > getOpenCount())
		{
			left = getOpenCount();
		}
		if (left < 0)
		{
			left = 0;
		}
		return left;
	}

	/**
	 * 自由的剩余天数
	 * 
	 * @return
	 */
	public long getFreeLeftDays()
	{
		String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
				.format(new Date(System.currentTimeMillis()));
		long startToEnd = DataConvert.transformTime(getStartTime(), getEndTime(), null, null);
		long todayToEnd = DataConvert.transformTime(today, getEndTime(), null, null);
		long leftDays = todayToEnd > startToEnd ? startToEnd : todayToEnd;	// 使用数学中的包含来判断
		if (leftDays < 0)
		{
			// today比endTime大
			leftDays = 0;
		}
		return leftDays;
	}

	/**
	 * 自定义字段1是否保密输入
	 * 
	 * @return
	 */
	public boolean isSelfDefineSecret1()
	{
		return secret == SECRET_DEFINE1 || secret == SECRET_DEFINE_BOTH;
	}

	/**
	 * 自定义字段2是否保密输入
	 * 
	 * @return
	 */
	public boolean isSelfDefineSecret2()
	{
		return secret == SECRET_DEFINE2 || secret == SECRET_DEFINE_BOTH;
	}

	/**
	 * 是否有必须输入的项，如果没有，则按传统模式进行：手机和qq必须有一个
	 * 
	 * @return
	 */
	public boolean hasMustItem()
	{
		return isQQMust() || isPhoneMust() || isEmailMust() || getSelfMust() > 0;
	}

	/**
	 * QQ是否是必填项（申请激活时）
	 * 
	 * @return
	 */
	public boolean isQQMust()
	{
		return (getContactMust() & MUST_QQ) == MUST_QQ;
	}

	/**
	 * phone是否是必填项（申请激活时）
	 * 
	 * @return
	 */
	public boolean isPhoneMust()
	{
		return (getContactMust() & MUST_PHONE) == MUST_PHONE;
		//		return false; //小米4手机手机号输入框点击无法定位光标问题，暂时放开手机号输入限制
	}

	/**
	 * email是否是必填项（申请激活时）
	 * 
	 * @return
	 */
	public boolean isEmailMust()
	{
		return (getContactMust() & MUST_EMAIL) == MUST_EMAIL;
	}

	/*-*******************************
	 * TODO Getter的延伸方法
	 ********************************/

	/**
	 * 制作者是否允许显示限制条件
	 * 
	 * @return
	 */
	public boolean canShowLimit()
	{
		return !isPayFile() || getShowLimit() == Constant.C_TRUE;	// 自由传播没有不能看的限制
	}

	/**
	 * 是否需要申请激活
	 * 
	 * @return
	 */
	public boolean isNeedApply()
	{
		return isPayFile() && (getNeedApply() == Constant.C_TRUE || !canOpen());		// 前提得是付费文件
	}

	// /**
	// * @return true 离线结构offline或者服务器存储的数据
	// */
	// public boolean isOfflineFile()
	// {
	// return getOffline() == Constant.C_TRUE;
	// }

	/**
	 * 是不是付费文件
	 * 
	 * @return
	 */
	public boolean isPayFile()
	{
		return getPayFile() == Constant.C_TRUE ;
	}

	/**
	 * 制作者是否禁止了
	 * 
	 * @return
	 */
	public boolean isMakerAllowed()
	{
		return isPayFile() || getMakerAllowed() == Constant.C_TRUE;
	}

	/**
	 * 次数是否限制（付费和自由都调用此接口查询）
	 * 
	 * @return
	 */
	public boolean isCountLimit()
	{
		return getOpenCount() > 0;
	}

	/**
	 * 是否是无限次数xiong
	 *
	 * @return
	 */
	public boolean isCountUnLimit()
	{
		return getOpenCount() >= 0;
	}

	/*-******************************
	 * TODO Getter and Setter
	 ********************************/
	
	public String getShopUrl()
	{
		return shopUrl;
	}

	public void setShopUrl(String shopUrl)
	{
		this.shopUrl = shopUrl;
	}

	public int getOpenCount()
	{
		return openCount;
	}

	public long getOffset()
	{
		return offset;
	}

	public void setOffset(long offset)
	{
		this.offset = offset;
	}

	public void setOpenCount(int openCount)
	{
		this.openCount = openCount;
	}

	public int getOpenedCount()
	{
		return openedCount;
	}

	public void setOpenedCount(int openedCount)
	{
		this.openedCount = openedCount;
	}

	public int getCanPrint()
	{
		return canPrint;
	}

	public void setCanPrint(int canPrint)
	{
		this.canPrint = canPrint;
	}

	public int getMakerAllowed()
	{
		return makerAllowed;
	}

	public void setMakerAllowed(int makerAllowed)
	{
		this.makerAllowed = makerAllowed;
	}

	public int getFid()
	{
		return fid;
	}

	public void setFid(int fid)
	{
		this.fid = fid;
	}

	public int getSecret()
	{
		return secret;
	}

	public void setSecret(int secret)
	{
		this.secret = secret;
	}

	public int getSingleOpenTime()
	{
		return singleOpenTime;
	}

	public void setSingleOpenTime(int singleOpenTime)
	{
		this.singleOpenTime = singleOpenTime;
	}

	public int getAppType()
	{
		return appType;
	}

	public void setAppType(int appType)
	{
		this.appType = appType;
	}

	public int getRandom()
	{
		return random;
	}

	public void setRandom(int random)
	{
		this.random = random;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public int getPlayId()
	{
		return playId;
	}

	public void setPlayId(int playId)
	{
		this.playId = playId;
	}

	public int getPayFile()
	{
		return payFile;
	}

	public void setPayFile(int payFile)
	{
		this.payFile = payFile;
	}

	public int getDays()
	{
		return days;
	}

	public void setDays(int days)
	{
		this.days = days;
	}

	public int getYears()
	{
		return years;
	}

	public long getFileLen()
	{
		return fileLen;
	}

	public void setFileLen(long fileLen)
	{
		this.fileLen = fileLen;
	}

	public void setYears(int years)
	{
		this.years = years;
	}

	public int getNotice()
	{
		return notice;
	}

	public int getSid()
	{
		return sid > 0 ? sid : NO_SID;
	}

	public void setSid(int sid)
	{
		this.sid = sid;
	}

	public int getSeriseFilesNum()
	{
		return seriseFilesNum;
	}

	public void setSeriseFilesNum(int seriseFilesNum)
	{
		this.seriseFilesNum = seriseFilesNum;
	}

	public void setNotice(int notice)
	{
		this.notice = notice;
	}

	public int getRemainDays()
	{
		return remainDays;
	}

	public void setRemainDays(int remainDays)
	{
		this.remainDays = remainDays;
	}

	public int getRemainYears()
	{
		return remainYears;
	}

	public void setRemainYears(int remainYears)
	{
		this.remainYears = remainYears;
	}

	public int getOrderId()
	{
		return orderId;
	}

	public void setOrderId(int orderId)
	{
		this.orderId = orderId;
	}

	public int getBindNum()
	{
		return bindNum;
	}

	public void setBindNum(int bindNum)
	{
		this.bindNum = bindNum;
	}

	public int getContactMust()
	{
		return contactMust;
	}

	public void setContactMust(int contactMust)
	{
		this.contactMust = contactMust;
	}

	public int getActiveNum()
	{
		return activeNum;
	}

	public void setActiveNum(int activeNum)
	{
		this.activeNum = activeNum;
	}

	public int getSelfMust()
	{
		return selfMust;
	}

	public void setSelfMust(int selfMust)
	{
		this.selfMust = selfMust;
	}

	public int getShowLimit()
	{
		return showLimit;
	}

	public void setShowLimit(int showLimit)
	{
		this.showLimit = showLimit;
	}

	public int getApplyId()
	{
		return applyId;
	}

	public void setApplyId(int applyId)
	{
		this.applyId = applyId;
	}

	public int getNeedApply()
	{
		return needApply;
	}

	public void setNeedApply(int needApply)
	{
		this.needApply = needApply;
	}

	public int getNeedReApply()
	{
		return needReApply;
	}

	public void setNeedReApply(int needReApply)
	{
		this.needReApply = needReApply;
	}

	// public int getOffline()
	// {
	// return offline;
	// }
	//
	// public void setOffline(int offline)
	// {
	// this.offline = offline;
	// }

	public int getTableId()
	{
		return tableId;
	}

	public void setTableId(int tableId)
	{
		this.tableId = tableId;
	}

	public int getShowDiff()
	{
		return showDiff;
	}

	public void setShowDiff(int showDiff)
	{
		this.showDiff = showDiff;
	}

	public String getUserName()
	{
		return DataConvert.getSafeStr(userName);
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getFileName()
	{
		return DataConvert.getSafeStr(fileName);
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFilePath()
	{
		return DataConvert.getSafeStr(filePath);
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public String getStartTime()
	{
		return DataConvert.getSafeStr(startTime);
	}

	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

	public String getEndTime()
	{
		return DataConvert.getSafeStr(endTime);
	}

	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}

	public String getOutData()
	{
		return DataConvert.getSafeStr(outData);
	}

	public void setOutData(String outData)
	{
		this.outData = outData;
	}

	public String getMac()
	{
		return DataConvert.getSafeStr(mac);
	}

	public void setMac(String mac)
	{
		this.mac = mac;
	}

	public String getNick()
	{
		return DataConvert.getSafeStr(nick);
	}

	public void setNick(String nick)
	{
		this.nick = nick;
	}

	public String getRemark()
	{
		return DataConvert.getSafeStr(remark);
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getVersionStr()
	{
		return DataConvert.getSafeStr(versionStr);
	}

	public void setVersionStr(String versionStr)
	{
		this.versionStr = versionStr;
	}

	public String getEmail()
	{
		return DataConvert.getSafeStr(email);
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getQq()
	{
		return DataConvert.getSafeStr(qq);
	}

	public void setQq(String qq)
	{
		this.qq = qq;
	}

	public String getPhone()
	{
		return DataConvert.getSafeStr(phone);
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getSecurityCode()
	{
		return DataConvert.getSafeStr(securityCode);
	}

	public void setSecurityCode(String securityCode)
	{
		this.securityCode = securityCode;
	}

	public int getSeriesReceiveNum()
	{
		return seriesReceiveNum;
	}

	public void setSeriesReceiveNum(int seriesReceiveNum)
	{
		this.seriesReceiveNum = seriesReceiveNum;
	}

	public String getEmailBuyer()
	{
		return DataConvert.getSafeStr(emailBuyer);
	}

	public void setEmailBuyer(String emailBuyer)
	{
		this.emailBuyer = emailBuyer;
	}

	public String getQqBuyer()
	{
		return DataConvert.getSafeStr(qqBuyer);
	}

	public void setQqBuyer(String qqBuyer)
	{
		this.qqBuyer = qqBuyer;
	}

	public String getPhoneBuyer()
	{
		return DataConvert.getSafeStr(phoneBuyer);
	}

	public void setPhoneBuyer(String phoneBuyer)
	{
		this.phoneBuyer = phoneBuyer;
	}

	public String getHardNo()
	{
		return DataConvert.getSafeStr(hardNo);
	}

	public void setHardNo(String hardNo)
	{
		this.hardNo = hardNo;
	}

	public String getSysInfo()
	{
		return DataConvert.getSafeStr(sysInfo);
	}

	public void setSysInfo(String sysInfo)
	{
		this.sysInfo = sysInfo;
	}

	public byte[] getHash()
	{
		return DataConvert.getSafeBytes(hash);
	}

	public void setHash(byte[] hash)
	{
		this.hash = hash;
	}

	public byte[] getEncodeKey()
	{
		return DataConvert.getSafeBytes(encodeKey);
	}

	public void setEncodeKey(byte[] encodeKey)
	{
		this.encodeKey = encodeKey;
	}

	public byte[] getSessionKey()
	{
		return DataConvert.getSafeBytes(sessionKey);
	}

	public void setSessionKey(byte[] sessionKey)
	{
		this.sessionKey = sessionKey;
	}

	public String getFirstOpenTime()
	{
		return DataConvert.getSafeStr(firstOpenTime);
	}

	public void setFirstOpenTime(String firstOpenTime)
	{
		this.firstOpenTime = firstOpenTime;
	}

	public String getMakeTime()
	{
		return DataConvert.getSafeStr(makeTime);
	}

	public void setMakeTime(String makeTime)
	{
		this.makeTime = makeTime;
	}

	public String getOrderNo()
	{
		return DataConvert.getSafeStr(orderNo);
	}

	public void setOrderNo(String orderNo)
	{
		this.orderNo = orderNo;
	}

	public String getSelfDefineKey1()
	{
		return DataConvert.getSafeStr(selfDefineKey1);
	}

	public void setSelfDefineKey1(String selfDefineKey1)
	{
		this.selfDefineKey1 = selfDefineKey1;
	}

	public String getSelfDefineKey2()
	{
		return DataConvert.getSafeStr(selfDefineKey2);
	}

	public void setSelfDefineKey2(String selfDefineKey2)
	{
		this.selfDefineKey2 = selfDefineKey2;
	}

	public String getSelfDefineValue1()
	{
		return DataConvert.getSafeStr(selfDefineValue1);
	}

	public void setSelfDefineValue1(String selfDefineValue1)
	{
		this.selfDefineValue1 = selfDefineValue1;
	}

	public String getSelfDefineValue2()
	{
		return DataConvert.getSafeStr(selfDefineValue2);
	}

	public void setSelfDefineValue2(String selfDefineValue2)
	{
		this.selfDefineValue2 = selfDefineValue2;
	}

	public String getShowInfo()
	{
		return DataConvert.getSafeStr(showInfo);
	}

	public void setShowInfo(String showInfo)
	{
		this.showInfo = showInfo;
	}

	public String getMsgId()
	{
		return DataConvert.getSafeStr(msgId);
	}

	public void setMsgId(String msgId)
	{
		this.msgId = msgId;
	}

	public void setFileVersion(int fileVersion)
	{
		this.fileVersion = fileVersion;
	}

	public int getFileVersion()
	{
		return fileVersion;
	}

	public String getUid()
	{
		return DataConvert.getSafeStr(uid);
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public int getActived()
	{
		return actived;
	}

	public void setActived(int actived)
	{
		this.actived = actived;
	}

	public int getTimeModify()
	{
		return timeModify;
	}

	public void setTimeModify(int timeModify)
	{
		this.timeModify = timeModify;
	}

	public String getLastOpenTime()
	{
		return DataConvert.getSafeStr(lastOpenTime);
	}

	public void setLastOpenTime(String lastOpenTime)
	{
		this.lastOpenTime = lastOpenTime;
	}

	public long getCodeLen()
	{
		return codeLen;
	}

	public void setCodeLen(long codeLen)
	{
		this.codeLen = codeLen;
	}

	public String getSeriesName()
	{
		return DataConvert.getSafeStr(seriesName);
	}

	public void setSeriesName(String seriesName)
	{
		this.seriesName = seriesName;
	}

	/*-*****************************
	 * 其他方法
	 *****************************/

	public static void calculateRemainDaysAndYears(SmInfo info)
	{
		String outData = info.getOutData();
		if (TextUtils.isEmpty(outData) || !info.isPayDataLimit())
		{
			return;
		}

		final long end = DataConvert.toLongTime(outData);
		final long cur = System.currentTimeMillis();
		final long perDay = 1L * 24 * 60 * 60 * 1000;

		int remainDay = 0;
		int remainYear = 0;
		if (end > cur)
		{
			remainYear = (int) (((end - cur) / perDay / 365));
			remainDay = (int) (((end - cur) / perDay % 365));
			if ((end - cur) % perDay > 0)
			{
				remainDay++;	// 不足一天也算一天
			}
			//			if (remainDay == 365)	// 排除天数为365的情况
			//			{
			//				remainYear++;
			//				remainDay = 0;
			//			}
		}
		if (remainYear > info.getYears())
		{
			remainYear = info.getYears();	// 修改了系统时间
		}
		if (info.getYears() == 0 && remainDay > info.getDays())
		{
			remainDay = info.getDays();	// 修改了系统时间，同时考虑倒计时不足一年的情况
		}
		info.setRemainDays(remainDay);
		info.setRemainYears(remainYear);
	}

	@Override
	public String toString()
	{
		return "SmInfo [openCount=" + openCount + ", openedCount=" + openedCount + ", canPrint="
				+ canPrint + ", makerAllowed=" + makerAllowed + ", fid=" + fid + ", secret="
				+ secret + ", singleOpenTime=" + singleOpenTime + ", appType=" + appType
				+ ", random=" + random + ", version=" + version + ", playId=" + playId
				+ ", payFile=" + payFile + ", days=" + days + ", years=" + years + ", notice="
				+ notice + ", remainDays=" + remainDays + ", remainYears=" + remainYears
				+ ", orderId=" + orderId + ", bindNum=" + bindNum + ", contactMust=" + contactMust
				+ ", activeNum=" + activeNum + ", selfMust=" + selfMust + ", showLimit=" + showLimit
				+ ", applyId=" + applyId + ", needApply=" + needApply + ", needReApply="
				+ needReApply + ", tableId=" + tableId + ", fileVersion=" + fileVersion
				+ ", showDiff=" + showDiff + ", userName=" + userName + ", fileName=" + fileName
				+ ", filePath=" + filePath + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", outData=" + outData + ", mac=" + mac + ", nick=" + nick + ", remark=" + remark
				+ ", versionStr=" + versionStr + ", email=" + email + ", qq=" + qq + ", phone="
				+ phone + ", securityCode=" + securityCode + ", emailBuyer=" + emailBuyer
				+ ", qqBuyer=" + qqBuyer + ", phoneBuyer=" + phoneBuyer + ", hardNo=" + hardNo
				+ ", sysInfo=" + sysInfo + ", hash=" + Arrays.toString(hash) + ", encodeKey="
				+ Arrays.toString(encodeKey) + ", sessionKey=" + Arrays.toString(sessionKey)
				+ ", firstOpenTime=" + firstOpenTime + ", makeTime=" + makeTime + ", orderNo="
				+ orderNo + ", selfDefineKey1=" + selfDefineKey1 + ", selfDefineKey2="
				+ selfDefineKey2 + ", selfDefineValue1=" + selfDefineValue1 + ", selfDefineValue2="
				+ selfDefineValue2 + ", showInfo=" + showInfo + ", msgId=" + msgId + ", timeModify="
				+ timeModify + ", lastOpenTime=" + lastOpenTime + ", uid=" + uid + ", actived="
				+ actived + ", codeLen=" + codeLen + ", seriseName=" + seriesName + ", sid=" + sid
				+ ", seriseFilesNum=" + seriseFilesNum + "]";
	}

}
