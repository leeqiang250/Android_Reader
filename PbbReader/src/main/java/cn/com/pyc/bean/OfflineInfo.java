package cn.com.pyc.bean;

import java.util.Arrays;

import com.qlk.util.tool.DataConvert;

import cn.com.pyc.utils.Constant;

/**
 * 2015-3-24 后就不分离线在线了，这个类现在只是为兼容旧的离线文件服务
 * 
 * @deprecated
 */
public class OfflineInfo
{
	private final int structFlag = 0x00435950;

	private int applyId;		// 申请时由服务器分配
	private int offline;	// 是否是离线文件
	private int actived;	// 是否已激活（可以查看）
	private int timeModify;		// 文件是否被修改过，如果非本程序修改，则不再支持文件查看（除非联网验证）
	private int openCount;
	private int openedCount;
	private int canPrint;
	private int days;		// 能看几天
	private int years;		// 能看几年
	private int showLimit;	// 是否在界面上显示限制条件
	private int contactMust;	// 卖家要求买家输入的联系方式（具体含义见文档）
	private int selfMust;	// 卖家要求买家输入的自定义信息（具体含义见文档）
	private int secret; // 买家输入contact或者self时是否不可见（具体含义见文档）
	private String fileCreateTime;	// 文件创建时间，android系统中没有这个，可以不用写
	private String fileModifyTime;	// 文件修改时间，只要文件内容变动，这个字段就得更新
	private String lastOpenTime;		// 最后一次打开时间
	private String outData;		// 允许打开的最后期限
	private String firstOpenTime;	// 初次打开时间
	private String emailBuyer;		// 买家的Email
	private String qqBuyer;	// 买家的QQ
	private String phoneBuyer;	// 买家手机号
	private String selfDefineKey1;	// 卖家要求买家输入的自定义信息名称
	private String selfDefineKey2;
	private String selfDefineValue1;	// 买家输入的自定义信息内容
	private String selfDefineValue2;
	private String hardNo;	// 设备ID（必须唯一）
	private String filePath;	// 文件路径
	private byte[] encodeKey;

	public OfflineInfo()
	{

	}

	/**
	 * 将以上字段转换成SmInfo中对应的字段（SmInfo可与服务器交互，OfflineInfo不能）
	 */
	public void convertToSmInfo(SmInfo smInfo)
	{
		smInfo.setApplyId(getApplyId());	// 申请后还未激活前，这步不能少ֵ

		if (getActived() == 0)
		{
			return;		// 未激活，则下面转换没有意义
		}

		smInfo.setPayFile(Constant.C_TRUE);
		smInfo.setApplyId(getApplyId());
		// smInfo.setOffline(getOffline());
		smInfo.setActived(getActived());
		smInfo.setOpenCount(getOpenCount());
		smInfo.setOpenedCount(getOpenedCount());
		smInfo.setCanPrint(getCanPrint());
		smInfo.setDays(getDays());
		smInfo.setYears(getYears());
		smInfo.setShowLimit(getShowLimit());
		smInfo.setContactMust(getContactMust());
		smInfo.setSelfMust(getSelfMust());
		smInfo.setSecret(getSecret());
		smInfo.setMakeTime(getFileCreateTime());
		smInfo.setFirstOpenTime(getFirstOpenTime());
		smInfo.setEmailBuyer(getEmailBuyer());
		smInfo.setQqBuyer(getQqBuyer());
		smInfo.setPhoneBuyer(getPhoneBuyer());
		smInfo.setSelfDefineKey1(getSelfDefineKey1());
		smInfo.setSelfDefineKey2(getSelfDefineKey2());
		smInfo.setSelfDefineValue1(getSelfDefineValue1());
		smInfo.setSelfDefineValue2(getSelfDefineValue2());
		smInfo.setHardNo(getHardNo());
		smInfo.setEncodeKey(getEncodeKey());
		smInfo.setNeedApply(1 ^ getActived());		// 这两者是异或关系

		// outData和remainDays和remainYears相互转换
		smInfo.setOutData(getOutData());
		SmInfo.calculateRemainDaysAndYears(smInfo);
	}

	public static final String calculateOutData(int year, int day)
	{
		if (year + day > 0)
		{
			long curTime = System.currentTimeMillis();
			long time = 1L * (year * 365 + day) * 24 * 60 * 60 * 1000;	// intתlongԽ���ˣ��ȳ���1Lתlong
			return DataConvert.toDate(curTime + time);
		}
		else
		{
			return "";
		}
	}

	/**
	 * 将active和applyId复原即可，阅读时是根据这两个字段判断的
	 */
	public void clear()
	{
		setActived(Constant.C_FALSE);
		setApplyId(Constant.C_FALSE);
	}

	public boolean isClear()
	{
		return getActived() == Constant.C_FALSE
				&& getApplyId() == Constant.C_FALSE;
	}

	/*-*******************************
	 * TODO Getter and Setter
	 ********************************/

	public int getApplyId()
	{
		return applyId;
	}

	public void setApplyId(int applyId)
	{
		this.applyId = applyId;
	}

	public int getOffline()
	{
		return offline;
	}

	public void setOffline(int offline)
	{
		this.offline = offline;
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

	public int getOpenCount()
	{
		return openCount;
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

	public void setYears(int years)
	{
		this.years = years;
	}

	public int getShowLimit()
	{
		return showLimit;
	}

	public void setShowLimit(int showLimit)
	{
		this.showLimit = showLimit;
	}

	public int getContactMust()
	{
		return contactMust;
	}

	public void setContactMust(int contactMust)
	{
		this.contactMust = contactMust;
	}

	public int getSelfMust()
	{
		return selfMust;
	}

	public void setSelfMust(int selfMust)
	{
		this.selfMust = selfMust;
	}

	public int getSecret()
	{
		return secret;
	}

	public void setSecret(int secret)
	{
		this.secret = secret;
	}

	public String getFileCreateTime()
	{
		return DataConvert.getSafeStr(fileCreateTime);
	}

	public void setFileCreateTime(String fileCreateTime)
	{
		this.fileCreateTime = fileCreateTime;
	}

	public String getFileModifyTime()
	{
		return DataConvert.getSafeStr(fileModifyTime);
	}

	public void setFileModifyTime(String fileModifyTime)
	{
		this.fileModifyTime = fileModifyTime;
	}

	public String getLastOpenTime()
	{
		return DataConvert.getSafeStr(lastOpenTime);
	}

	public void setLastOpenTime(String lastOpenTime)
	{
		this.lastOpenTime = lastOpenTime;
	}

	public String getOutData()
	{
		return DataConvert.getSafeStr(outData);
	}

	public void setOutData(String outData)
	{
		this.outData = outData;
	}

	public String getFirstOpenTime()
	{
		return DataConvert.getSafeStr(firstOpenTime);
	}

	public void setFirstOpenTime(String firstOpenTime)
	{
		this.firstOpenTime = firstOpenTime;
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

	public String getHardNo()
	{
		return DataConvert.getSafeStr(hardNo);
	}

	public void setHardNo(String hardNo)
	{
		this.hardNo = hardNo;
	}

	public String getFilePath()
	{
		return DataConvert.getSafeStr(filePath);
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public byte[] getEncodeKey()
	{
		return DataConvert.getSafeBytes(encodeKey);
	}

	public void setEncodeKey(byte[] encodeKey)
	{
		this.encodeKey = encodeKey;
	}

	public int getStructFlag()
	{
		return structFlag;
	}

	@Override
	public String toString()
	{
		return "OfflineInfo [structFlag=" + structFlag + ", applyId=" + applyId
				+ ", offline=" + offline + ", actived=" + actived
				+ ", timeModify=" + timeModify + ", openCount=" + openCount
				+ ", openedCount=" + openedCount + ", canPrint=" + canPrint
				+ ", days=" + days + ", years=" + years + ", showLimit="
				+ showLimit + ", contactMust=" + contactMust + ", selfMust="
				+ selfMust + ", secret=" + secret + ", fileCreateTime="
				+ fileCreateTime + ", fileModifyTime=" + fileModifyTime
				+ ", lastOpenTime=" + lastOpenTime + ", outData=" + outData
				+ ", firstOpenTime=" + firstOpenTime + ", emailBuyer="
				+ emailBuyer + ", qqBuyer=" + qqBuyer + ", phoneBuyer="
				+ phoneBuyer + ", selfDefineKey1=" + selfDefineKey1
				+ ", selfDefineKey2=" + selfDefineKey2 + ", selfDefineValue1="
				+ selfDefineValue1 + ", selfDefineValue2=" + selfDefineValue2
				+ ", hardNo=" + hardNo + ", filePath=" + filePath
				+ ", encodeKey=" + Arrays.toString(encodeKey) + "]";
	}

}
