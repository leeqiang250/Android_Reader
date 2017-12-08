package cn.com.pyc.bean;

import java.util.Arrays;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.ArrayUtil;

import cn.com.pyc.utils.Constant;
import cn.com.pyc.utils.LoginControll;

import android.text.TextUtils;

/**
 * 基本情况和SmInfo类似
 * 
 * @author QiLiKing 2014-11-20
 */
public class UserInfo
{
	private static final int BIND_EMAIL = 8;	// 8、16、32是以前业务的用法，现在由pc端导入的temp文件仍沿用此方法
	private static final int BIND_PHONE = 16;
	private static final int BIND_QQ = 32;

	private int appType; // 客户端android:28;winphone:29;ios:30;pc:18
	private int notice;		// 通知个数 每个业务都有
	private String redbag;

	private int random;
	private String userName;
	private String psd;
	private String mac;		// !< 本机网卡MAC地址
	private String versionStr;		// 鹏保宝版本号
	private String phone;
	private String email;
	private String nick;	// 注意各个字段的初始化，不能为null
	private int status;  //判断是否为企业账户


	/**
	 * ！！！密钥绝对不能转成String参与各个业务，有时会使乱码的！！！<br>
	 * 目前有三处用到uid：
	 * isKeyNull判断、FlagData中analysisFlagFromFile和analysisBuffer
	 */
	private byte[] uid;
	private String openId;
	private String qqNick;
	private String money;
	private int emailBinded;
	private int phoneBinded;
	private int qqBinded;

	/*-*******************************************
	 * Null的判断
	 ********************************************/

	public UserInfo()
	{
		setRandom(ProtocolInfo.random());
	}

	public boolean isKeyNull()
	{
		// 此处uid不能转成String，有的会是乱码s
		return !(LoginControll.checkLogin()) || (TextUtils.isEmpty(getUserName()) || ArrayUtil.isEmpty(getUid()));
	}

	public boolean isPsdNull()
	{
		return TextUtils.isEmpty(getPsd());
	}

	public boolean isNickNull()
	{
		return TextUtils.isEmpty(getNick());
	}
	
	/**
	 * UserInfo u2=u1=u0=new UserInfo();	//事例1
	 * UserInfo u4 = new UserInfo();	//事例2
	 * 若u1 = u4;
	 * 則u2的值還是事例1的值而非事例2
	 * @param info
	 */
	public void copyInfo(UserInfo info)
	{
		this.appType = info.appType;
		this.email = info.email;
		this.emailBinded = info.emailBinded;
		this.mac = info.mac;
		this.money = info.money;
		this.redbag = info.redbag;
		this.nick = info.nick;
		this.notice = info.notice;
		this.openId = info.openId;
		this.phone = info.phone;
		this.status = info.status;
		this.phoneBinded = info.phoneBinded;
		this.psd = info.psd;
		this.qqBinded = info.qqBinded;
		this.qqNick = info.qqNick;
		this.random = info.random;
		this.uid = info.uid;
		this.userName = info.userName;
		this.versionStr = info.versionStr;
	}


	/**
	 * 以前的业务是用“&”判断的。现在由pc端安装手机端鹏保宝时仍沿用此方法
	 * 
	 * @param value
	 */
	public void setBindedValue(int value)
	{
		// 注意，if(true)是必要的（value是从sp中取得的，当sp中没有对应key值时它会返回默认值false，而这个值是没有意义的。所以不做判断）
		// 如果是从pc端传入的value，则由于emailBinded等值默认是0（即false），不会有什么影响
		if ((BIND_EMAIL & value) == BIND_EMAIL)
		{
			setEmailBinded(Constant.C_TRUE);
		}
		if ((BIND_PHONE & value) == BIND_PHONE)
		{
			setPhoneBinded(Constant.C_TRUE);
		}
		if ((BIND_QQ & value) == BIND_QQ)
		{
			setQqBinded(Constant.C_TRUE);
		}
	}

	/*-************************
	 * 延伸方法
	 *************************/

	public void setEmailBinded(boolean bind)
	{
		setEmailBinded(bind ? Constant.C_TRUE : Constant.C_FALSE);
	}

	public void setPhoneBinded(boolean bind)
	{
		setPhoneBinded(bind ? Constant.C_TRUE : Constant.C_FALSE);
	}

	public void setQqBinded(boolean bind)
	{
		setQqBinded(bind ? Constant.C_TRUE : Constant.C_FALSE);
	}

	public boolean isEmailBinded()
	{
		return getEmailBinded() == Constant.C_TRUE;
	}

	public boolean isQqBinded()
	{
		return getQqBinded() == Constant.C_TRUE;
	}

	public boolean isPhoneBinded()
	{
		return getPhoneBinded() == Constant.C_TRUE;
	}

	/*-************************
	 * Getter and Setter
	 *************************/

	public int getAppType()
	{
		return appType;
	}

	public void setAppType(int appType)
	{
		this.appType = appType;
	}

	public int getNotice()
	{
		return notice;
	}

	public void setNotice(int notice)
	{
		this.notice = notice;
	}

	public int getRandom()
	{
		return random;
	}

	public void setRandom(int random)
	{
		this.random = random;
	}

	public String getUserName()
	{
		return DataConvert.getSafeStr(userName);
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getPsd()
	{
		return DataConvert.getSafeStr(psd);
	}

	public void setPsd(String psd)
	{
		this.psd = psd;
	}

	public String getMac()
	{
		return DataConvert.getSafeStr(mac);
	}

	public void setMac(String mac)
	{
		this.mac = mac;
	}

	public String getVersionStr()
	{
		return DataConvert.getSafeStr(versionStr);
	}

	public void setVersionStr(String versionStr)
	{
		this.versionStr = versionStr;
	}

	public String getPhone()
	{
		return DataConvert.getSafeStr(phone);
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getEmail()
	{
		return DataConvert.getSafeStr(email);
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getNick()
	{
		return DataConvert.getSafeStr(nick);
	}

	public void setNick(String nick)
	{
		this.nick = nick;
	}

	public byte[] getUid()
	{
		return DataConvert.getSafeBytes(uid);
	}

	public void setUid(byte[] uid)
	{
		this.uid = uid;
	}

	public String getOpenId()
	{
		return DataConvert.getSafeStr(openId);
	}

	public void setOpenId(String openId)
	{
		this.openId = openId;
	}

	public String getQqNick()
	{
		return DataConvert.getSafeStr(qqNick);
	}

	public void setQqNick(String qqNick)
	{
		this.qqNick = qqNick;
	}

	public String getMoney()
	{
		return DataConvert.getSafeStr(money);
	}

	public void setMoney(String money)
	{
		this.money = money;
	}
	
	public String getRedbag()
	{
		return redbag;
	}

	public void setRedbag(String redbag)
	{
		this.redbag = redbag;
	}

	public int getEmailBinded()
	{
		return emailBinded;
	}

	public void setEmailBinded(int emailBinded)
	{
		this.emailBinded = emailBinded;
	}

	public int getPhoneBinded()
	{
		return phoneBinded;
	}

	public void setPhoneBinded(int phoneBinded)
	{
		this.phoneBinded = phoneBinded;
	}

	public int getQqBinded()
	{
		return qqBinded;
	}

	public void setQqBinded(int qqBinded)
	{
		this.qqBinded = qqBinded;
	}
	
	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	@Override
	public String toString()
	{
		return "UserInfo [appType=" + appType + ", notice=" + notice
				+ ", random=" + random + ", userName=" + userName + ", psd="
				+ psd + ", mac=" + mac + ", versionStr=" + versionStr
				+ ", phone=" + phone + ", email=" + email + ", nick=" + nick
				+ ", uid=" + Arrays.toString(uid) + ", openId=" + openId
				+ ", qqNick=" + qqNick + ", money=" + money + ", emailBinded="
				+ emailBinded + ", phoneBinded=" + phoneBinded + ", qqBinded="
				+ qqBinded + ",redbag="+redbag+"]";
	}

}
