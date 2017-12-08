package cn.com.pyc.conn;

import com.qlk.util.global.GlobalObserver;
import com.qlk.util.tool.DataConvert;

import cn.com.pyc.base.Result;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;
import android.content.Context;
import static cn.com.pyc.bean.ProtocolInfo.*;

/*-
 * 与服务器交互，兼顾更新数据库并通知刷新
 */
public class UserConnect extends Connect
{
	private UserDao db;

	public UserConnect(Context context)
	{
		super(context);
		db = UserDao.getDB(context);
	}

	@Override
	protected int getPort(int type)
	{
		return 5004;
	}

	/*-*************************************
	 * 说明：
	 * 		各业务皆有notice、money的返回值。
	 * 		如果该业务没有autoNotify，则表明
	 * 		业务执行完后它的调用者不会执行刷新
	 * 		操作，所以设定：notice在执行
	 * 		anaylsisRcvData的时候刷新；
	 * 		money则单独调用onlyMoneyChanged来刷新
	 * 
	 * 注意：
	 * 		根据说明，如果业务没有autoNotify，
	 * 		则需要注意业务内是否调用了
	 * 		onlyMoneyChanged
	 * 
	 * 另外：
	 * 		每个业务都有autoToast，如果不想使用
	 * 		autoToat（或者业务没有autoToast），
	 * 		也不使用返回值UserResult
	 * 		的failureReason来通知用户，则需要
	 * 		考虑到failureReason包括联网失败
	 * 		和业务失败的情况，要分开对待
	 * 		（如果觉得麻烦，则可以将联网失败的
	 * 		reason在commit方法中输出，这样failureReason
	 * 		就只代表业务失败的情况了）
	 * 
	 * 		带有“后台业务”标签的则不用返回failureReason
	 * 		也不用toast
	 * 
	 ***************************************/

	// 注册
	public UserResult register(String nick, String psd, boolean autoNotify, boolean autoToast)
	{
		final int type = TYPE_REGISTER;
		UserInfo sndInfo = new UserInfo();
		sndInfo.setNick(nick);
		sndInfo.setPsd(psd);
		sndInfo.setAppType(PhoneInfo.appType);
		sndInfo.setVersionStr(PhoneInfo.getVersionStr(mContext));
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			sndInfo.setUserName(rcvInfo.getUserName());
			sndInfo.setUid(rcvInfo.getUid());
			sndInfo.setMoney(rcvInfo.getMoney());
			db.saveUserInfo(sndInfo);
			postNotifyKey(autoNotify);
			ur.setSuccessReason("领取成功");
		}
		if (autoToast)
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	// QQ注册
	public UserResult qqRegister(String qqNick, String openId, boolean autoNotify, boolean autoToast)
	{
		final int type = TYPE_QQ_REGISTER;
		UserInfo sndInfo = new UserInfo();
		sndInfo.setNick(qqNick);	// 传给服务器时用的nick字段
		sndInfo.setOpenId(openId);
		sndInfo.setAppType(PhoneInfo.appType);
		sndInfo.setVersionStr(PhoneInfo.getVersionStr(mContext));
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			sndInfo.setQqNick(qqNick);
			sndInfo.setQqBinded(Constant.C_TRUE);
			sndInfo.setUserName(rcvInfo.getUserName());
			sndInfo.setUid(rcvInfo.getUid());
			sndInfo.setMoney(rcvInfo.getMoney());
			db.saveUserInfo(sndInfo);
			postNotifyKey(autoNotify);
			ur.setSuccessReason("领取成功");
		}
		if (autoToast)
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	// 找回钥匙 --- name可以是邮箱、手机号、昵称或者钥匙编号（重复昵称怎么办？）
	public UserResult findKeyBack(String name, String psd, boolean autoNotify, boolean autoToast)
	{
		final int type = TYPE_FIND_KEY_BACK;
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(name);
		sndInfo.setPsd(psd);
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			rcvInfo.setQqBinded(ur.isQQBinded());
			rcvInfo.setEmailBinded(ur.isEmailBinded());
			rcvInfo.setPhoneBinded(ur.isPhoneBinded());

			db.saveUserInfo(rcvInfo);
			postNotifyKey(autoNotify);
			ur.setSuccessReason("找回成功");
		}
		if (autoToast)
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	// QQ找回
	public UserResult findKeyBackByQQ(String openId, boolean autoNotify, boolean autoToast)
	{
		final int type = TYPE_QQ_FIND_KEY_BACK;
		UserInfo sndInfo = new UserInfo();
		sndInfo.setOpenId(openId);
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			rcvInfo.setQqBinded(true);
			rcvInfo.setEmailBinded(ur.isEmailBinded());
			rcvInfo.setPhoneBinded(ur.isPhoneBinded());
			db.saveUserInfo(rcvInfo);
			postNotifyKey(autoNotify);
			ur.setSuccessReason("找回成功");
		}
		if (autoToast)
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	// 忘记密码---这里没有money和notice的返回值
	public UserResult findPsdBack(String email, boolean autoToast)
	{
		final int type = TYPE_FIND_PSD_BACK;
		final UserInfo userInfo = db.getUserInfo();		// 每次获取最新的信息。下同
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(userInfo.getUserName());
		sndInfo.setEmail(email);		// 之所以不用userInfo代替sndInfo，是因为此时还不确定email是不是userName的绑定email。下同
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			userInfo.setEmail(rcvInfo.getEmail());		// 传入时email可以为空，所以这里要重置
			userInfo.setEmailBinded(true);
			db.saveUserInfo(userInfo);		// 注意，存储的是userInfo，下同
			ur.setSuccessReason("密码已发到邮箱：" + rcvInfo.getEmail() + "，请及时查收");
		}

		if (autoToast)
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	/**
	 * 如果UserResult.succeed()返回true则表示密码改变
	 * 
	 * @return
	 */
	// 找回密码《后台业务》
	public UserResult synchronizePsd()
	{
		final int type = TYPE_SYNCHRONIZE_PSD;
		final UserInfo userInfo = db.getUserInfo();
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(userInfo.getUserName());
		return execute(sndInfo, type);
	}

	// 验证/修改邮箱
	public UserResult bindEmail(String email, boolean autoNotify, boolean autoToast)
	{
		final int type = TYPE_BIND_EMAIL;
		final UserInfo userInfo = db.getUserInfo();
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(userInfo.getUserName());
		sndInfo.setEmail(email);
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			userInfo.setMoney(rcvInfo.getMoney());
			userInfo.setEmail(email);
			userInfo.setEmailBinded(false);
			db.saveUserInfo(userInfo);
			postNotifyKey(autoNotify);
			ur.setSuccessReason("请查收邮件进行验证");
		}
		if (autoToast)
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	// 创建/修改密码
	public UserResult modifyPassword(String psd, boolean autoNotify)
	{
		final int type = TYPE_MODIFY_PSD;
		final UserInfo userInfo = db.getUserInfo();
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(userInfo.getUserName());
		sndInfo.setPsd(psd);
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			userInfo.setMoney(rcvInfo.getMoney());
			userInfo.setPsd(psd);
			db.saveUserInfo(userInfo);
			postNotifyKey(autoNotify);
		}
		else
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	// 创建/修改昵称
	public UserResult modifyNick(String nick, boolean autoNotify)
	{
		final int type = TYPE_MODIFY_NICK;
		final UserInfo userInfo = db.getUserInfo();
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(userInfo.getUserName());
		sndInfo.setNick(nick);
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			userInfo.setMoney(rcvInfo.getMoney());
			userInfo.setNick(nick);
			db.saveUserInfo(userInfo);
			postNotifyKey(autoNotify);
		}
		else
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	// 绑定QQ
	public UserResult bindQQ(String qqNick, String openId, boolean autoNotify, boolean autoToast)
	{
		final int type = TYPE_BIND_QQ;
		final UserInfo userInfo = db.getUserInfo();
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(userInfo.getUserName());
		sndInfo.setNick(qqNick);	// 传入时用nick字段
		sndInfo.setOpenId(openId);
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			userInfo.setMoney(rcvInfo.getMoney());
			userInfo.setQqNick(qqNick);
			if (userInfo.isNickNull())
			{
				userInfo.setNick(qqNick);
			}
			userInfo.setQqBinded(true);
			db.saveUserInfo(userInfo);
			postNotifyKey(autoNotify);
			ur.setSuccessReason("绑定成功");
		}
		if (autoToast)
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	// 获取通知个数---必须保证已有钥匙《后台业务》
	public UserResult getNoticeNum()
	{
		final UserInfo userInfo = db.getUserInfo();
		if (userInfo.isKeyNull())
		{
			return null;
		}
		final int type = TYPE_GET_NOTICE;
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(userInfo.getUserName());
		UserResult ur = execute(sndInfo, type);
//		if (ur.getUserInfo() != null)
//		{
//			UserInfo rcvInfo = ur.getUserInfo();
//			userInfo.setMoney(rcvInfo.getMoney());
//			db.saveUserInfo(userInfo);
//		}
		return ur;
	}

	// 取得用户信息
	public UserResult synchronizedUserInfo(boolean autoNotify, boolean autoToast)
	{
		final int type = TYPE_GET_USER_INFO;
		final UserInfo userInfo = db.getUserInfo();
		UserInfo sndInfo = new UserInfo();
		sndInfo.setUserName(userInfo.getUserName());
		UserResult ur = execute(sndInfo, type);
		if (ur.getUserInfo() != null)
		{
			UserInfo rcvInfo = ur.getUserInfo();
			rcvInfo.setUid(userInfo.getUid());	// 返回信息就少了个uid
			rcvInfo.setEmailBinded(ur.isEmailBinded());
			rcvInfo.setPhoneBinded(ur.isPhoneBinded());
			rcvInfo.setQqBinded(ur.isQQBinded());
			db.saveUserInfo(rcvInfo);
			postNotifyKey(autoNotify);
			ur.setSuccessReason("更新成功");
		}

		if (autoToast)
		{
			ur.showResult(mContext);
		}
		return ur;
	}

	private UserResult execute(UserInfo sndInfo, int type)
	{
		byte[] snd = packageSendData(type, sndInfo);
		byte[] rcv = new byte[USER_TOTAL_LEN];
		int fail = commit(snd, rcv);
		UserResult ur = new UserResult(type);
		ur.setSuc(fail);
		if (fail == Result.NET_NORMAL)
		{
			anaylsisRcvData(rcv, ur, type, sndInfo.getRandom());
		}
		return ur;
	}

	private void postNotifyKey(boolean autoNotify)
	{
		if (autoNotify)
		{
			GlobalObserver.getGOb().postNotifyObservers(ObTag.Key);
		}
	}

	private byte[] packageSendData(int type, UserInfo info)
	{
		byte[] snd = new byte[USER_TOTAL_LEN];
		int len = 0;
		byte[] buf = null;

		// type
		buf = DataConvert.intToBytes(type);
		System.arraycopy(buf, 0, snd, len, INT_LEN);

		// appType
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getAppType());
		System.arraycopy(buf, 0, snd, len, INT_LEN);

		// random
		len += INT_LEN * 2;
		buf = DataConvert.intToBytes(info.getRandom());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// username
		len += INT_LEN * 4;
		buf = info.getUserName().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// password
		len += USERNAME_LEN;
		buf = info.getPsd().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// version
		len += PASSWORD_LEN + MAC_LEN;
		buf = info.getVersionStr().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// phone
		len += VERSION_LEN;
		buf = info.getPhone().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// email
		len += PHONE_LEN;
		buf = info.getEmail().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// nick
		len += EMAIL_LEN;
		buf = info.getNick().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// openId
		len += NICK_LEN + UID_LEN;
		buf = info.getOpenId().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);
		return XCoder.encrypt(snd);
	}

	/*-
	 * 分析服务器返回数据
	 * 
	 * 分析流程：校验码分析---type分析---suc分析---其他分析
	 */
	private void anaylsisRcvData(byte[] rcv, UserResult ur, int correctType, int random)
	{
		int len = 0;
		byte[] bufTemp = null;
		rcv = XCoder.decrypt(rcv);
		// 先判断random,这个不一致，其他一切皆无意义
		bufTemp = DataConvert.getBytes(rcv, INT_LEN * 3, INT_LEN);
		if (DataConvert.bytesToInt(bufTemp) != random)
		{
			ur.setFailureReason("数据验证错误");
			return;
		}

		// type
		bufTemp = DataConvert.getBytes(rcv, len, SHORT_LEN);
		short type = DataConvert.bytesToShort(bufTemp);
		if (type != correctType)
		{
			ur.setFailureReason("请求type不一致");
			return;
		}

		// suc
		len += SHORT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, SHORT_LEN);
		short suc = DataConvert.bytesToShort(bufTemp);
		ur.setSuc(suc);
		if (!ur.succeed())
		{
			return;
		}

		UserInfo rcvInfo = new UserInfo();

		// notice
		len += INT_LEN + SHORT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		rcvInfo.setNotice(DataConvert.bytesToInt(bufTemp));
		
		// status 判断是否为企业用户
		len += INT_LEN * 2;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		rcvInfo.setStatus(DataConvert.bytesToInt(bufTemp));

		// username
		len += INT_LEN * 3;
		bufTemp = DataConvert.getBytes(rcv, len, USERNAME_LEN);
		rcvInfo.setUserName(DataConvert.getSafeStr(bufTemp));

		// password
		len += USERNAME_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, PASSWORD_LEN);
		rcvInfo.setPsd(DataConvert.getSafeStr(bufTemp));

		// phone
		len += PASSWORD_LEN + MAC_LEN + VERSION_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, PHONE_LEN);
		rcvInfo.setPhone(DataConvert.getSafeStr(bufTemp));

		// email
		len += PHONE_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, EMAIL_LEN);
		rcvInfo.setEmail(DataConvert.getSafeStr(bufTemp));

		// nick
		len += EMAIL_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, NICK_LEN);
		rcvInfo.setNick(DataConvert.getSafeStr(bufTemp));

		// uid
		len += NICK_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, UID_LEN);
		rcvInfo.setUid(bufTemp);

		// qqnick
		len += OPENID_LEN + UID_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, NICK_LEN);
		rcvInfo.setQqNick(DataConvert.getSafeStr(bufTemp));

		// money
		len += NICK_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, NICK_LEN);
		rcvInfo.setMoney(DataConvert.getSafeStr(bufTemp));

		// 因为每个业务都有值，所以放在这里notice
		if (rcvInfo.getNotice() >= 0)
		{
			ObTag.Notice.arg1 = rcvInfo.getNotice();
			GlobalObserver.getGOb().postNotifyObservers(ObTag.Notice);
		}

		ur.setUserInfo(rcvInfo);
	}

}
