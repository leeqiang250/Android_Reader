package cn.com.pyc.conn;

import android.content.Context;
import android.text.TextUtils;

import com.qlk.util.global.GlobalObserver;
import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool._SysoXXX;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;

import java.util.Arrays;

import cn.com.pyc.base.Result;
import cn.com.pyc.bean.OfflineInfo;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.conn.SmResult.OpenFailure;
import cn.com.pyc.db.SerisesDao;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.db.sm.ReceiveDao;
import cn.com.pyc.db.sm.SendDao;
import cn.com.pyc.db.sm.SmDao;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

import static cn.com.pyc.bean.ProtocolInfo.EMAIL_LEN;
import static cn.com.pyc.bean.ProtocolInfo.FILE_NAME_LEN;
import static cn.com.pyc.bean.ProtocolInfo.HARDNO_LEN;
import static cn.com.pyc.bean.ProtocolInfo.HASH_VALUE_LEN;
import static cn.com.pyc.bean.ProtocolInfo.INT_LEN;
import static cn.com.pyc.bean.ProtocolInfo.KEY_LEN;
import static cn.com.pyc.bean.ProtocolInfo.LIMIT_TIME_LEN;
import static cn.com.pyc.bean.ProtocolInfo.MAC_LEN;
import static cn.com.pyc.bean.ProtocolInfo.MAKE_TIME_LEN;
import static cn.com.pyc.bean.ProtocolInfo.MESSAGE_ID_LEN;
import static cn.com.pyc.bean.ProtocolInfo.NICK_LEN;
import static cn.com.pyc.bean.ProtocolInfo.ORDERNO_LEN;
import static cn.com.pyc.bean.ProtocolInfo.PHONE_LEN;
import static cn.com.pyc.bean.ProtocolInfo.QQ_LEN;
import static cn.com.pyc.bean.ProtocolInfo.REMARK_LEN;
import static cn.com.pyc.bean.ProtocolInfo.SELF_DEFINE_LEN;
import static cn.com.pyc.bean.ProtocolInfo.SERIESNAME_LEN;
import static cn.com.pyc.bean.ProtocolInfo.SHORT_LEN;
import static cn.com.pyc.bean.ProtocolInfo.SHOW_INFO_LEN;
import static cn.com.pyc.bean.ProtocolInfo.SM_TOTAL_LEN;
import static cn.com.pyc.bean.ProtocolInfo.SYSINFO_LEN;
import static cn.com.pyc.bean.ProtocolInfo.TIME_LEN;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_APPLY_ACTIVATE;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_GET_PHONE_SECURITY_CODE;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_GET_SECURITY_CODE;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_GET_SM_INFO;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_GET_SM_INFOS;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_MAKE_FREE_FILE;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_MAKE_PAY_FILE;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_MODIFY_LIMIT;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_OFFLINE_VERIFY;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_OPEN_FILE;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_PLAY_TIME;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_REAPPLY;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_SCAN_APPLY_INFO;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_SEND_PHONE_SECURITY_CODE;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_STOP_READ;
import static cn.com.pyc.bean.ProtocolInfo.TYPE_UPLOAD_HASH;
import static cn.com.pyc.bean.ProtocolInfo.USERNAME_LEN;
import static cn.com.pyc.bean.ProtocolInfo.VERSION_LEN;

/**
 * 与服务器交互，兼顾更新数据库并通知刷新
 * <p>
 * SmInfo中有二义性的字段，都在本类中分析分割
 *
 * @author QiLiKing
 */
public class SmConnect extends Connect
{
	private XCoder xcoder;

	public SmConnect(Context context)
	{
		super(context);
		xcoder = new XCoder(context);
	}

	@Override
	protected int getPort(int type)
	{
		if (type == TYPE_MAKE_PAY_FILE || type == TYPE_UPLOAD_HASH || type == TYPE_MAKE_FREE_FILE)
		{
			return 5006;
		}
		else
		{
			return 5005;
		}
	}

	/*-************************************************
	 * 说明：
	 * 		各业务皆有notice的返回值。
	 * 		设定：notice在执行anaylsisRcvData的时候刷新
	 * 
	 * 另外：
	 * 		如果检测到SmResult.needUpdate，则更自动更新
	 * 		数据库，并且notify
	 * 
	 * 注意：
	 * 		各业务传入的sminfo因为要存入数据库的关系，
	 * 		必须是有所有数据库有效字段的info（见备注）
	 * 		
	 * 备注：
	 * 		数据库有效字段：id、nick、stopRead、
	 * 		openCount、openedCount、startTime、endTime、
	 * 		singOpenTime、remark等
	 *************************************************/

	/**
	 * 调用结束后，使用smInfo或者SmResult中的SmInfo都可以，他们是一致的。 下同
	 *
	 * @param smInfo
	 *            调用结束后它就是最新的全面的信息 下同
	 * @param isPayMode
	 * @return 此时SmResult中的SmInfo就是参数smInfo 下同
	 */
	public SmResult makeFile(SmInfo smInfo, boolean isPayMode)
	{
		final int type = isPayMode ? TYPE_MAKE_PAY_FILE : TYPE_MAKE_FREE_FILE;
		smInfo.setVersionStr(PhoneInfo.getVersionStr(mContext));
		smInfo.setAppType(PhoneInfo.appType);		// 服务器以此区分客户端，填错的话则返回的数据中会出现乱码。下同！
		smInfo.setVersion(PhoneInfo.version);
		smInfo.setFileVersion(PhoneInfo.fileVersion);
		SmResult sr = execute(smInfo, type, true);
		if (sr.getSmInfo() != null)
		{
			SmInfo rcvInfo = sr.getSmInfo();
			smInfo.setFid(rcvInfo.getFid());
			if (isPayMode)
			{
				smInfo.setOrderNo(rcvInfo.getOrderNo());	// 制作payFile时有效
			}
			postNotice(rcvInfo.getNotice());
			sr.setSmInfo(smInfo);
		}
		return sr;
	}

	/**
	 * 上传hash
	 *
	 * @param smInfo
	 * @param autoToast
	 * @return
	 */
	public SmResult uploadHash(SmInfo smInfo, boolean autoToast)
	{
		SmResult sr = execute(smInfo, TYPE_UPLOAD_HASH, false);
		if (sr.succeed())
		{
			// 存入数据库
			smInfo.setMakeTime(DataConvert.toDate(System.currentTimeMillis()));	// 制作时间是客户端自己填写
			smInfo.setMakerAllowed(Constant.C_TRUE);	// 这一步不能少
			SendDao.getInstance(mContext).updateOrInsert(smInfo);
			sr.setSuccessReason("制作成功");
			sr.setSmInfo(smInfo);
		}
		if (autoToast)
		{
			sr.showResult(mContext);
		}
		return sr;
	}

	/**
	 * 查看文件
	 * 只有此业务才会向数据库存储hardNo
	 *
	 * @param smInfo
	 * @param autoNotify
	 * @return
	 */
	public SmResult openFile(SmInfo smInfo, boolean autoNotify, boolean autoToast)
	{
		boolean suc = ReceiveDao.getInstance(mContext).query(smInfo);
		//newFile是以startTime等为限制条件的，以前的离线结构不支持
		if (!suc || !smInfo.hasValidEncodeKey() && !smInfo.isNewFile())	// 没有记录或者encodeKey为空，就读离线结构
		{
			OfflineInfo offlineInfo = OfflineManager.getLatestInfo(smInfo);

			if (offlineInfo != null && offlineInfo.getApplyId() > 0 && offlineInfo.getActived() > 0)
			{
				offlineInfo.convertToSmInfo(smInfo);
			}
		}

		_SysoXXX.message("发送数据：" + smInfo.getFid() + "; " + smInfo.toString());
		// 联网
		SmResult sr = execute(pkgOpenFileClientSendInfo(smInfo), TYPE_OPEN_FILE, true);

		if (sr.getSmInfo() != null)	// 能连上
		{
			analysisOpenFileServerBackInfo(smInfo, sr);

			/* 查看是sid字段是由 apptype 代替的 */
			if (smInfo.getSid() > 0)
			{
				SerisesDao.getInstance().updateOrInsert(smInfo);
			}

		}
		else
		{
			// 连不上，根据本地数据判断打开 本地判断只包含付费文件
			if (smInfo.isPayFile())
			{
				boolean canOpen = smInfo.canOpen();

				if (!canOpen)	// 时间过期了，清零
				{
					if (smInfo.getNeedApply() == Constant.C_TRUE)
					{
						sr.setFailureReason("服务器繁忙");
						sr.setFailure(OpenFailure.Unknown);
					}else{
						smInfo.resetApplyInfo();
						sr.setFailureReason("超出限制，不能阅读");
						sr.setFailure(OpenFailure.Unknown);
					}
				}
				// 读取保存的对应fid的文件key,主要是离线下使用
                String key = (String)SPUtil.get(smInfo.getFid() + "", "");
                if (!TextUtils.isEmpty(key) && !CommonUtil.isNetConnect(SZApplication.getInstance())) {
                    try {
						smInfo.setEncodeKey(key.getBytes("ISO-8859-1"));
						SPUtil.remove(smInfo.getFid() + "");
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
				// 先检查key
				if (canOpen && !smInfo.hasValidEncodeKey())
				{
					canOpen = false;	// 需要联网获取key
				}
				//add 离线时，赋值设备号
				if(!CommonUtil.isNetConnect(SZApplication.getInstance())){
					smInfo.setHardNo(PhoneInfo.getUUID(mContext));
				}
				// 检查设备
				if (canOpen && !PhoneInfo.getUUID(mContext).equals(smInfo.getHardNo()))
				{
					canOpen = false;
					sr.setSuc(SmResult.DEVICE_CHANGED);
				}

				// 系统时间是否改变
				boolean flagModify = smInfo.getTimeModify() == Constant.C_TRUE;	// 第一次检测到timeModify了，就将falg置为1，即使用户把时间改回来，也得去验证
				boolean timeModify = System.currentTimeMillis() < DataConvert.toLongTime(smInfo
						.getLastOpenTime());		// 时间往前改了（往后改无所谓）
				if (canOpen && (flagModify || timeModify))
				{
					canOpen = false;
					smInfo.setTimeModify(Constant.C_TRUE);
					sr.setSuc(SmResult.NEED_VERIFY);
				}

				if (canOpen)
				{
					sr.setSuc(SmResult.CAN_OPEN);
					smInfo.setTimeModify(0);
					smInfo.setLastOpenTime(DataConvert.toDate(System.currentTimeMillis()));
					smInfo.setOpenedCount(smInfo.getOpenedCount() + 1); // 次数加一
				}
			}
		}
		ReceiveDao.getInstance(mContext).updateOrInsert(smInfo);
		sr.setSmInfo(smInfo);

		if (autoNotify)
		{
			postNotifyRefresh();
		}

		if (autoToast)
		{
			//			sr.showResult(mContext);
		}

		return sr;
	}

//	private String byte2String(byte[] bytes) {
//		StringBuilder builder = new StringBuilder();
//		for (byte b : bytes) {
//			builder.append(b+"")
//					.append(",");
//		}
//		Log.e("keyStr",builder.toString());
//		return builder.toString();
//	}
//	private String string2Byte(String bytes) {
//		byte[] bs = new byte[ProtocolInfo.KEY_LEN];
//		String[] strs = bytes.split(",");
//		for (int i = 0; i < strs.length; i++) {
//			if(!TextUtils.isEmpty(strs[i])) {
//				bs[i] = strs[i].t;
//			}
//		}
//		return builder.toString();
//	}

	private SmInfo pkgOpenFileClientSendInfo(SmInfo srcInfo)
	{
		// Logname、ID、HashValue、hardno、sysinfo 、apptype、random、version、
		// MacAddr、nick（计算机名）、applyid（主要针对离线阅读，如果离线申请成功后）、
		// phone（自由需要手机号验证码）、messageid（验证码获取业务返回的）
		SmInfo sndInfo = new SmInfo();
		//		sndInfo.setUserName(srcInfo.getUserName());
		//打开文件时时传绥之那边注册时拿到的用户手机号
		sndInfo.setUserName((String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, ""));
		sndInfo.setFid(srcInfo.getFid());
		sndInfo.setHash(srcInfo.getHash());
		sndInfo.setHardNo(PhoneInfo.getUUID(mContext));
		sndInfo.setSysInfo(PhoneInfo.sysInfo);
		sndInfo.setAppType(PhoneInfo.appType);
//		sndInfo.setVersion(PhoneInfo.version);
		sndInfo.setVersion(PhoneInfo.getAPIVersion(mContext));
		sndInfo.setMac(srcInfo.getMac());
		sndInfo.setApplyId(srcInfo.getApplyId());
		sndInfo.setPhone(srcInfo.getSecurityCode());		// 传入时验证码用phone字段代替
		sndInfo.setMsgId(srcInfo.getMsgId());
		sndInfo.setOpenedCount(srcInfo.getOpenedCount());		// 让服务器同步本地数据

		return sndInfo;
	}

	private void analysisOpenFileServerBackInfo(SmInfo desInfo, SmResult sr)
	{
		//System.out.println("22222222222222222222222");
		SmInfo rcvInfo = sr.getSmInfo();
		// fileOutName、startTime、endTime、fileopennum、fileopenednum
		// 、iCanOpen、HashValue、EncodeKey、
		// iOpenTimeLong、remark、nick、random、daynum、yearnum
		// noticnum、dayremain、yearremain、ooid、maketime、apptype、
		desInfo.setSeriesName(rcvInfo.getSeriesName()); // xiong-获取服务器返回系列名
		desInfo.setSeriseFilesNum(rcvInfo.getTableId());// xiong
		desInfo.setSid(rcvInfo.getAppType());// xiong 现改为由apptype 返回系列id
		desInfo.setFileName(rcvInfo.getFileName());
		desInfo.setStartTime(rcvInfo.getStartTime());
		desInfo.setEndTime(rcvInfo.getEndTime());
		desInfo.setOpenCount(rcvInfo.getOpenCount());
		desInfo.setOpenedCount(rcvInfo.getOpenedCount());
		desInfo.setMakerAllowed(rcvInfo.getMakerAllowed());
		desInfo.setHash(rcvInfo.getHash());
		desInfo.setFileVersion(rcvInfo.getFileVersion());//返回4表示 新的时间范围限制
		// 服务器只有在可以打开时才会返回encodeKey，其他情况返回0。
		// 而我们是根据smInfo.hasValidEncodeKey()来兼容旧的离线文件的
		// 所以当以前的离线结构看过几次时（比如openedCount=2），升级到不区分离线在线的版本后，
		// 文件看过期，之后再申请时，起初不能看，故服务器encodeKey为空，此时会清空数据库的key，在下次打开时就会重新加载离线结构的openedCount的值（即2）
		// 这时候同步服务器数据就会少openedCount的阅读次数。
		// 因此，将desInfo.setEncodeKey(rcvInfo.getEncodeKey());放在if里面
		if (sr.canOpen())
		{
			desInfo.setEncodeKey(rcvInfo.getEncodeKey());
			System.out.println("encodeKey: " + Arrays.toString(rcvInfo.getEncodeKey()));
			try {
				//存储这个key，和文件fid对应存储
				SPUtil.save(desInfo.getFid() + "", new String(rcvInfo.getEncodeKey(), "ISO-8859-1"));
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		desInfo.setSingleOpenTime(rcvInfo.getSingleOpenTime());
		desInfo.setRemark(rcvInfo.getRemark());
		desInfo.setNick(rcvInfo.getNick());
		desInfo.setDays(rcvInfo.getDays());
		desInfo.setYears(rcvInfo.getYears());
		desInfo.setRemainDays(rcvInfo.getRemainDays());
		desInfo.setRemainYears(rcvInfo.getRemainYears());
		desInfo.setOrderId(rcvInfo.getOrderId());
		desInfo.setMakeTime(rcvInfo.getMakeTime());
		desInfo.setAppType(rcvInfo.getAppType());
		// 、bindnum（选择列）、activenum（自定义字段数）、otherset（是否能看约束条件，是否需要手机号）
		// Id（查看时返回自定义字段是否保密输入 1：字段1需保密 2：字段2需保密 3：两个都需保密）
		// Showinfo（已经申请，申请成功界面提示内容）
		// Applyid（已经申请的ID，重新申请，或者记入离线结构图）
		// iCanClient
		// need_reapply、need_showdiff
		desInfo.setContactMust(rcvInfo.getBindNum());
		desInfo.setSelfMust(rcvInfo.getActiveNum());
		final int otherset = rcvInfo.getShowLimit();
		desInfo.setShowLimit((otherset & 1) == 1 ? Constant.C_TRUE : Constant.C_FALSE);
		desInfo.setSecret(rcvInfo.getFid());	// 只有在需要申请激活时才有效
		desInfo.setShowInfo(rcvInfo.getShowInfo());
		desInfo.setApplyId(rcvInfo.getApplyId());
		// desInfo.setOffline(rcvInfo.getOffline());
		desInfo.setNeedReApply(rcvInfo.getNeedReApply());
		desInfo.setShowDiff(rcvInfo.getShowDiff());

		// QQ、EMAIL、PHONE（能查看时，买家的，不能时卖家的）
		// selfdefine1（能查看时，买家输入，不能查看时，自定义名称）、selfdefine2（能查看时，买家输入，不能查看时，自定义名称）、
		// Hardno(能查看时自定义1字段名称) Version(能查看时查看记录ID)
		// Logname(能查看时自定义字段2名称)
		if (sr.isPayFile())
		{
			if (sr.canOpen())
			{
				desInfo.setQqBuyer(rcvInfo.getQq());
				desInfo.setEmailBuyer(rcvInfo.getEmail());
				desInfo.setPhoneBuyer(rcvInfo.getPhone());
				desInfo.setSelfDefineValue1(rcvInfo.getSelfDefineValue1());
				desInfo.setSelfDefineValue2(rcvInfo.getSelfDefineValue2());
				desInfo.setSelfDefineKey1(rcvInfo.getHardNo());
				desInfo.setSelfDefineKey2(rcvInfo.getUserName());
				desInfo.setPlayId(rcvInfo.getVersion());
				desInfo.setOpenedCount(rcvInfo.getOpenedCount() + 1);	// 注意这里的位置，不要被上面的赋值覆盖
				desInfo.setHardNo(PhoneInfo.getUUID(mContext));	// 存储绑定信息

				desInfo.setTimeModify(Constant.C_FALSE);
				desInfo.setLastOpenTime(DataConvert.toDate(System.currentTimeMillis()));

				// 第一次能查看时赋值阅读时间
				if (TextUtils.isEmpty(desInfo.getFirstOpenTime()))
				{
					desInfo.setFirstOpenTime(sr.canOpen() ? DataConvert.toDate(System
							.currentTimeMillis()) : "");		// 付费文件的查看没有首次阅读时间返回
				}

				if (desInfo.isPayDataLimit())
				{
					// 第一次阅读时remain为0
					if (desInfo.getRemainDays() + desInfo.getRemainYears() == 0)
					{
						desInfo.setOutData(OfflineInfo.calculateOutData(rcvInfo.getYears(),
								rcvInfo.getDays()));
					}
					else
					{
						desInfo.setOutData(OfflineInfo.calculateOutData(rcvInfo.getRemainYears(),
								rcvInfo.getRemainDays()));
					}
				}
			}
			else
			{
				desInfo.setQq(rcvInfo.getQq());
				desInfo.setEmail(rcvInfo.getEmail());
				desInfo.setPhone(rcvInfo.getPhone());
				desInfo.setSelfDefineKey1(rcvInfo.getSelfDefineValue1());
				desInfo.setSelfDefineKey2(rcvInfo.getSelfDefineValue2());
				desInfo.setFirstOpenTime("");		// 同一个文件过期后又激活了，本地的首次阅读时间应该清空
			}
		}
		else
		{
			desInfo.setQq(rcvInfo.getQq());
			desInfo.setEmail(rcvInfo.getEmail());
			desInfo.setPhone(rcvInfo.getPhone());
			if (sr.canOpen())
			{
				desInfo.setPlayId(rcvInfo.getVersion());
				desInfo.setOpenedCount(rcvInfo.getOpenedCount() + 1);	// 注意这里的位置，不要被上面的赋值覆盖
			}
		}

		// suc返回值
		desInfo.setPayFile(sr.isPayFile() ? Constant.C_TRUE : Constant.C_FALSE);
		desInfo.setNeedApply(sr.needApply() ? Constant.C_TRUE : Constant.C_FALSE);
		if ((otherset & 8) == 8)
		{
			sr.setSuc(SmResult.NEED_SECURITY);		// 需要验证码，这个算打不开的原因之一，放在suc里处理比较好。处理前先分析服务器的suc
		}
		postNotice(rcvInfo.getNotice());
	}

	/**
	 * 离线验证
	 *
	 * @param smInfo
	 * @return
	 */
	// applyid、hardno、apptype、random、apptype、fileopenednum、applyid
	public SmResult verifyOffline(SmInfo smInfo, boolean autoToast)
	{
		ReceiveDao.getInstance(mContext).query(smInfo);
		SmInfo sndInfo = new SmInfo();
		sndInfo.setApplyId(smInfo.getApplyId());
		sndInfo.setHardNo(PhoneInfo.getUUID(mContext));
		sndInfo.setAppType(PhoneInfo.appType);
		sndInfo.setOpenedCount(smInfo.getOpenedCount());
		SmResult sr = execute(sndInfo, TYPE_OFFLINE_VERIFY, true);
		if (sr.getSmInfo() != null)
		{
			SmInfo rcvInfo = sr.getSmInfo();
			smInfo.setRemainDays(rcvInfo.getRemainDays());
			smInfo.setRemainYears(rcvInfo.getRemainYears());
			postNotice(rcvInfo.getNotice());
			if (sr.canOpen())
			{
				sr.setSuccessReason("验证成功");
				smInfo.setTimeModify(Constant.C_FALSE);
				smInfo.setOutData(OfflineInfo.calculateOutData(rcvInfo.getRemainYears(),
						rcvInfo.getRemainDays()));
			}
			else
			{
				sr.setFailureReason("文件时效已过，无法阅读");
				smInfo.resetApplyInfo();
			}
			ReceiveDao.getInstance(mContext).updateOrInsert(smInfo);
			sr.setSmInfo(smInfo);
		}

		if (autoToast)
		{
			sr.showResult(mContext);
		}

		return sr;
	}

	/*-
	 * 发送数据：
	 * Logname、ID、hardno 、sysinfo 、email、QQ、phone
	 * 、apptype、random、version、ooid、selfdefine1、selfdefine2（根据activenum）
	 *
	 * 接收数据：
	 * Random、QQ、PHONE、EMAIL、remark、showinfo、
	 * applyid、iCanClient
	 */
	public SmResult applyActivate(SmInfo smInfo, boolean autoToast)
	{
		boolean isReApply = smInfo.getNeedReApply() == Constant.C_TRUE;
		final int type = isReApply ? TYPE_REAPPLY : TYPE_APPLY_ACTIVATE;
		SmInfo sndInfo = new SmInfo();
		//		sndInfo.setUserName(smInfo.getUserName());
		//申请时传绥之那边注册时拿到的用户手机号
		sndInfo.setUserName((String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, ""));
		sndInfo.setFid(smInfo.getFid());
		sndInfo.setHardNo(PhoneInfo.getUUID(mContext));
		sndInfo.setSysInfo(PhoneInfo.sysInfo);
		sndInfo.setEmail(smInfo.getEmailBuyer());		// 注意这里传入的买家的email用email代替 下同
		sndInfo.setQq(smInfo.getQqBuyer());
		sndInfo.setPhone(smInfo.getPhoneBuyer());
		sndInfo.setAppType(PhoneInfo.appType);
//		sndInfo.setVersion(PhoneInfo.version);
		sndInfo.setVersion(PhoneInfo.getAPIVersion(mContext));
		sndInfo.setOrderId(smInfo.getOrderId());
		sndInfo.setSelfDefineValue1(smInfo.getSelfDefineValue1());
		sndInfo.setSelfDefineValue2(smInfo.getSelfDefineValue2());
		if (isReApply)
		{
			sndInfo.setApplyId(smInfo.getApplyId());
		}
		SmResult sr = execute(sndInfo, type, true);
		if (sr.getSmInfo() != null)
		{
			SmInfo rcvInfo = sr.getSmInfo();
			smInfo.setEmail(rcvInfo.getEmail());	// 此时是卖家的联系方式
			smInfo.setQq(rcvInfo.getQq());
			smInfo.setPhone(rcvInfo.getPhone());
			smInfo.setRemark(rcvInfo.getRemark());
			smInfo.setShowInfo(rcvInfo.getShowInfo());
			smInfo.setApplyId(rcvInfo.getApplyId());
			// smInfo.setOffline(rcvInfo.getOffline());

			if (isReApply)
			{
				smInfo.setNeedReApply(Constant.C_FALSE);	// 申请后跳转到Success界面后会根据此值显示“重新申请”按钮
				smInfo.setShowDiff(Constant.C_FALSE);		// 不红色显示提示信息
			}

			ReceiveDao.getInstance(mContext).updateOrInsert(smInfo);
		}

		if (autoToast)
		{
			sr.showResult(mContext);
		}
		return sr;
	}

	public SmResult getApplyInfo(SmInfo smInfo)
	{
		SmInfo sndInfo = new SmInfo();
		sndInfo.setHardNo(PhoneInfo.getUUID(mContext));
		sndInfo.setAppType(PhoneInfo.appType);
		sndInfo.setApplyId(smInfo.getApplyId());
		sndInfo.setFid(smInfo.getFid());//xiong:2016/9/22
		//System.out.println("重新申请，增加FID："+smInfo.getFid());

		SmResult sr = execute(sndInfo, TYPE_SCAN_APPLY_INFO, true);
		if (sr.getSmInfo() != null)
		{
			SmInfo rcvInfo = sr.getSmInfo();
			smInfo.setQqBuyer(rcvInfo.getQq());
			smInfo.setEmailBuyer(rcvInfo.getEmail());
			smInfo.setPhoneBuyer(rcvInfo.getPhone());
			smInfo.setContactMust(rcvInfo.getBindNum());
			smInfo.setSelfDefineValue1(rcvInfo.getSelfDefineValue1());
			smInfo.setSelfDefineValue2(rcvInfo.getSelfDefineValue2());
			smInfo.setSelfMust(rcvInfo.getActiveNum());
			smInfo.setSecret(rcvInfo.getFid());
			smInfo.setSelfDefineKey1(rcvInfo.getHardNo());
			smInfo.setSelfDefineKey2(rcvInfo.getUserName());
		}
		return sr;
	}

	/**
	 * 刷新时如果是离线文件，则需要获取最新信息
	 * 获取文件信息（单个刷新）
	 *
	 * @param smInfo
	 *            成功后会将它重置为最新值
	 * @param autoNotify
	 * @return
	 */
	public SmResult getFileInfo(SmInfo smInfo, boolean autoNotify, boolean autoToast,
			boolean isReceive)
	{
		SmInfo sndInfo = new SmInfo();
		sndInfo.setFid(smInfo.getFid());
//		sndInfo.setVersion(PhoneInfo.version);
		sndInfo.setVersion(PhoneInfo.getAPIVersion(mContext));
		sndInfo.setHardNo(isReceive ? PhoneInfo.getUUID(mContext) : "");		// 发送置空，接收赋值
		SmResult sr = execute(sndInfo, TYPE_GET_SM_INFO, true);
		if (sr.succeed() && sr.getSmInfo() != null)
		{
			// fileOutName、startTime、endTime、fileopennum、fileopenednum
			// 、iCanOpen、iOpenTimeLong、remark、nick、random、daynum、、yearnum
			// noticnum、dayremain、yearremain、maketime、bindnum、activenum、apptype、orderno、otherset
			SmInfo rcvInfo = sr.getSmInfo();
			smInfo.setFileVersion(rcvInfo.getFileVersion());
			smInfo.setSeriesName(rcvInfo.getSeriesName()); // xiong
			smInfo.setSid(rcvInfo.getAppType()); // xiong
			smInfo.setFileName(rcvInfo.getFileName());
			smInfo.setOpenCount(rcvInfo.getOpenCount());
			smInfo.setMakerAllowed(rcvInfo.getMakerAllowed());
			smInfo.setSingleOpenTime(rcvInfo.getSingleOpenTime());
			smInfo.setRemark(rcvInfo.getRemark());
			smInfo.setNick(rcvInfo.getNick());
			smInfo.setDays(rcvInfo.getDays());
			smInfo.setYears(rcvInfo.getYears());
			smInfo.setMakeTime(rcvInfo.getMakeTime());
			smInfo.setBindNum(rcvInfo.getBindNum());
			smInfo.setActiveNum(rcvInfo.getActiveNum());
			smInfo.setAppType(rcvInfo.getAppType());
			smInfo.setOrderNo(rcvInfo.getOrderNo());
			smInfo.setShowLimit(rcvInfo.getShowLimit());		// otherset，刷新时就是表示能不能看限制条件
			smInfo.setEmail(rcvInfo.getEmail());	// 设计文档貌似漏写了这三个返回值
			smInfo.setQq(rcvInfo.getQq());
			smInfo.setPhone(rcvInfo.getPhone());

			smInfo.setRemainDays(rcvInfo.getRemainDays());
			smInfo.setRemainYears(rcvInfo.getRemainYears());
			if (!smInfo.isPayFile())	// 刷新没做服务器同步，所以次数以本地为准
			{
				smInfo.setOpenedCount(rcvInfo.getOpenedCount());
			}
			smInfo.setStartTime(rcvInfo.getStartTime());
			smInfo.setEndTime(rcvInfo.getEndTime());

			// suc返回值
			smInfo.setPayFile(sr.isPayFile() ? Constant.C_TRUE : Constant.C_FALSE);
			// if (smInfo.isPayFile() && !smInfo.isOfflineFile())
			if (smInfo.isPayFile())
			{
				// 不能使用sr.needApply()，因为该业务的第一位表示获取信息是否成功，而不是能不能打开
				smInfo.setNeedApply(sr.isUserStopRead() ? Constant.C_TRUE : Constant.C_FALSE);	// 这个业务比较特殊，详见设计文档
				if (rcvInfo.isCountLimit() && rcvInfo.getOpenedCount() == rcvInfo.getOpenCount())
				{
					smInfo.setNeedApply(Constant.C_TRUE);
				}
			}
			postNotice(rcvInfo.getNotice());
//			sr.setSuccessReason("更新成功");
			SmDao.getInstance(mContext, isReceive).updateOrInsert(smInfo);
		}
		if (autoNotify)
		{
			postNotifyRefresh();
		}
		if (autoToast)
		{
			sr.showResult(mContext);
		}
		return sr;
	}

	/**
	 * 这个方法比较特殊，它不是按字段来与服务器交互的（批量刷新）
	 *
	 * @param data
	 * @return
	 */
	public byte[] getFileInfos(byte[] data, boolean isReceive, boolean autoToast)
	{
		final int type = TYPE_GET_SM_INFOS;
		byte[] snd = new byte[SM_TOTAL_LEN];
		byte[] rcv = new byte[SM_TOTAL_LEN];
		System.arraycopy(DataConvert.intToBytes(type), 0, snd, 0, INT_LEN);	// type
		System.arraycopy(data, 0, snd, INT_LEN * 3, data.length);		// fids,*3是pos1和pos2
		if (isReceive)
		{
			byte[] bhardno = PhoneInfo.getUUID(mContext).getBytes();
			System.arraycopy(bhardno, 0, snd, 837, bhardno.length);		// hardNo是第829+8位
		}
		snd = XCoder.encryptV2(snd, 12);
		check0A(snd);
		int fail = commit(snd, rcv, type);
		SmResult sr = new SmResult(type);
		sr.setSuc(fail);
		if (fail == Result.NET_NORMAL)
		{
			uncheck0A(rcv);
			rcv = XCoder.decryptV2(rcv, 12);
			byte[] result = new byte[rcv.length - INT_LEN * 3];
			System.arraycopy(rcv, INT_LEN * 3, result, 0, result.length);
			return result; // 成功不提示，注意此处已return
		}

		// 失败提示
		if (autoToast)
		{
			sr.showResult(mContext);
		}
		return null;
	}

	/**
	 * 终止阅读
	 *
	 * @param smInfo
	 *            只需要传入info，方法内会自动改变stopRead的值
	 * @return
	 */
	public SmResult stopRead(SmInfo smInfo, boolean autoNotify, boolean autoToast)
	{
		SmInfo sndInfo = new SmInfo();
		sndInfo.setUserName(smInfo.getUserName());
		sndInfo.setFid(smInfo.getFid());
		sndInfo.setMakerAllowed(Constant.C_TRUE ^ smInfo.getMakerAllowed());		// 1变0,0变1
//		sndInfo.setVersion(PhoneInfo.version);
		sndInfo.setVersion(PhoneInfo.getAPIVersion(mContext));
		SmResult sr = execute(sndInfo, TYPE_STOP_READ, false);
		if (sr.succeed())
		{
			smInfo.setMakerAllowed(sndInfo.getMakerAllowed());	// 更改标志位
			SmDao.getInstance(mContext, false).updateOrInsert(smInfo);
			sr.setSuccessReason(sndInfo.isMakerAllowed() ? "已取消" : "已终止");	// 注意这里：“取消终止”成功后，变为allowed
			if (autoNotify)
			{
				postNotifyRefresh();
			}
		}
		if (autoToast)
		{
			sr.showResult(mContext);
		}
		return sr;
	}

	/**
	 * 修改条件
	 *
	 * @param smInfo
	 * @param autoNotify
	 * @param autoToast
	 * @return
	 */
	public SmResult modifyLimit(SmInfo smInfo, boolean autoNotify, boolean autoToast)
	{
		smInfo.setVersion(PhoneInfo.version);
		SmResult sr = execute(smInfo, TYPE_MODIFY_LIMIT, false);
		if (sr.succeed())
		{
			sr.setSuccessReason("修改成功");
			SmDao.getInstance(mContext, false).updateOrInsert(smInfo);
			if (autoNotify)
			{
				// 这里不是Refresh。否则，在刷新的过程中点击制作，则会出现制作界面自动消失的情况
				GlobalObserver.getGOb().postNotifyObservers(ObTag.ChangeLimit);
			}
		}
		if (autoToast)
		{
			sr.showResult(mContext);
		}
		return sr;
	}

	// 播放时间段《后台业务》
	public SmResult sendPlayTime(SmInfo smInfo)
	{
		SmInfo sndInfo = new SmInfo();
		sndInfo.setFid(smInfo.getFid());
		sndInfo.setVersion(smInfo.getPlayId());
		return execute(sndInfo, TYPE_PLAY_TIME, false);
	}

	/**
	 * 获取验证码
	 *
	 * @param smInfo
	 * @return
	 */
	public SmResult getSecurityCode(SmInfo smInfo, boolean autoToast)
	{
		smInfo.setVersion(PhoneInfo.version);
		SmResult sr = execute(smInfo, TYPE_GET_SECURITY_CODE, true);

		if (sr.succeed())
		{
			// 存入数据库
			SmInfo rcvInfo = sr.getSmInfo();
			smInfo.setMsgId(rcvInfo.getMsgId());
			smInfo.setSecurityCode(rcvInfo.getPhone());
			sr.setSuccessReason("验证码已发出");
			sr.setSmInfo(smInfo);
		}
//		if (autoToast)
//		{
//			sr.showResult(mContext);
//		}
		return sr;
	}

	/**
	 * 获取手机验证码
	 *
	 * @param smInfo
	 * @return
	 */
	public SmResult getPhoneSecurityCode(SmInfo smInfo, boolean autoToast)
	{
		smInfo.setVersion(PhoneInfo.version);
		SmResult sr = execute(smInfo, TYPE_GET_PHONE_SECURITY_CODE, true);
		if (sr.succeed())
		{
			// 存入数据库
			SmInfo rcvInfo = sr.getSmInfo();
			smInfo.setMsgId(rcvInfo.getMsgId());
			smInfo.setSecurityCode(rcvInfo.getPhone());
			sr.setSuccessReason("验证码已发出");
			sr.setSmInfo(smInfo);
		}
		return sr;
	}

	/**
	 * 提交手机验证码
	 *
	 * @param smInfo
	 * @return
	 */
	public SmResult sendPhoneSecurityCode(SmInfo smInfo, boolean autoToast)
	{
		UserInfo userInfo = UserDao.getDB(mContext).getUserInfo();
		smInfo.setUserName(userInfo.getUserName());
		smInfo.setVersion(PhoneInfo.version);
		SmResult sr = execute(smInfo, TYPE_SEND_PHONE_SECURITY_CODE, false);

		if (sr.succeed())
		{
			// 存入数据库
			userInfo.setPhoneBinded(true);
			UserDao.getDB(mContext).saveUserInfo(userInfo);
			sr.setSuccessReason("手机验证成功！！");
			GlobalObserver.getGOb().postNotifyObservers(ObTag.Key);
		}
		if (autoToast)
		{
			sr.showResult(mContext);
		}
		return sr;
	}

	private void postNotice(int notice)
	{
		ObTag.Notice.arg1 = notice;
		GlobalObserver.getGOb().postNotifyObservers(ObTag.Notice);
	}

	private void postNotifyRefresh()
	{
		GlobalObserver.getGOb().postNotifyObservers(ObTag.Refresh);
	}

	/*-
	 * 各业务的实际执行者
	 */
	private SmResult execute(SmInfo sndInfo, int type, boolean needAnalysisSmInfo)
	{
		byte[] snd = packageSendData(type, sndInfo);
		byte[] rcv = new byte[SM_TOTAL_LEN];
		int fail = commit(snd, rcv, type);
		System.out.println("SmResult execute(commit) = " + fail);
		SmResult sr = new SmResult(type);
		sr.setSuc(fail);
		if (fail == Result.NET_NORMAL)
		{
			//System.out.println("SmResult execute normal");
			analysisRcvData(rcv, sr, type, sndInfo.getRandom(), needAnalysisSmInfo);
		}
		return sr;
	}

	/*-**********************************************************************
	 * 下面两个方法操作时一定要特别注意字段对齐（len值要正确）
	 * 
	 * 一处len错位，则业务很可能失败
	 * 
	 * 这里为了方便，也为了不容易出错，设计这两个封装方法：任何一个业务都将每个字段赋值（不管该业务用没用到）
	 ***********************************************************************/

	private byte[] packageSendData(int type, SmInfo info)
	{
//		_SysoXXX.array(info.getHash(), "算出的hash");

		byte[] snd = new byte[SM_TOTAL_LEN];
		int len = 0;
		byte[] buf = null;

		// type
		buf = DataConvert.intToBytes(type);
		System.arraycopy(buf, 0, snd, len, buf.length);

		// openCount
		len += INT_LEN * 3;
		buf = DataConvert.intToBytes(info.getOpenCount());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// openedCount
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getOpenedCount());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// stopRead
		len += INT_LEN * 2;
		buf = DataConvert.intToBytes(info.getMakerAllowed());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// id
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getFid());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// singleOpenTime
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getSingleOpenTime());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// appType
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getAppType());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// random
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getRandom());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// version
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getVersion());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// days
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getDays());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// years
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getYears());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// ooid
		len += INT_LEN * 4;
		buf = DataConvert.intToBytes(info.getOrderId());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// appId 申请的ID，如果重新申请，需提交该ID，离线阅读的申请也要提交该值。
		len += INT_LEN * 4;
		buf = DataConvert.intToBytes(info.getApplyId());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// offline
		len += INT_LEN * 2;
		// buf = intToBytes(info.getOffline());
		// System.arraycopy(buf, 0, snd, len, buf.length);

		// tableId
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getTableId());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// fileVersion
		len += INT_LEN;
		buf = DataConvert.intToBytes(info.getFileVersion());
		System.arraycopy(buf, 0, snd, len, buf.length);

		// userName
		len += INT_LEN * 2;
		buf = info.getUserName().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// fileName
		len += USERNAME_LEN;
		buf = info.getFileName().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// startTime
		len += FILE_NAME_LEN;
		buf = info.getStartTime().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// endTime
		len += LIMIT_TIME_LEN;
		buf = info.getEndTime().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// mac
		len += LIMIT_TIME_LEN;
		buf = info.getMac().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// nick
		len += MAC_LEN;
		buf = info.getNick().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// remark
		len += NICK_LEN;
		buf = info.getRemark().getBytes();
		// 现在这个限制也可以去掉，这个限制是最开始的业务remark是最后一个字段，会造成数组越界
		System.arraycopy(buf, 0, snd, len, buf.length > REMARK_LEN ? REMARK_LEN : buf.length); // 虽然输入时做了限制，但在输入中文时会超过200

		// versionStr
		len += REMARK_LEN;
		buf = info.getVersionStr().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// email
		len += VERSION_LEN;
		buf = info.getEmail().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// QQ
		len += EMAIL_LEN;
		buf = info.getQq().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// phone
		len += QQ_LEN;
		buf = info.getPhone().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// hardNo
		len += PHONE_LEN;
		buf = info.getHardNo().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// sysInfo
		len += HARDNO_LEN;
		buf = info.getSysInfo().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// hash
		len += SYSINFO_LEN;
		buf = info.getHash();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// encodeKey
		len += HASH_VALUE_LEN;
		buf = info.getEncodeKey();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// sessionKey
		len += KEY_LEN;
		buf = info.getSessionKey();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// selfDefine1
		len += KEY_LEN + 2 * TIME_LEN + ORDERNO_LEN;
		buf = info.getSelfDefineValue1().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// selfDefine2
		len += SELF_DEFINE_LEN;
		buf = info.getSelfDefineValue2().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// showInfo
		len += SELF_DEFINE_LEN;
		buf = info.getShowInfo().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// msgId
		len += SHOW_INFO_LEN;
		buf = info.getMsgId().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// seriseName
		len += MESSAGE_ID_LEN;
		buf = info.getSeriesName().getBytes();
		System.arraycopy(buf, 0, snd, len, buf.length);

		// notuse
		XCoder.encryptV2(snd, 12);
		check0A(snd);	//加密后check
		return snd;
	}

	private void check0A(byte[] buf)
	{
		int pos1 = 0;
		int pos2 = 0;
		for (int i = 0; i < buf.length; i++)
		{
			byte b = buf[i];
			if (b == 0x0A)
			{
				if (buf[i + 1] == 0x0A)
				{
					if (pos1 == 0)
					{
						pos1 = i + 1;
						buf[i + 1] = 0x00;
						_SysoXXX.message("发送第一个0A" + pos1);
					}
					else if (pos2 == 0)
					{
						pos2 = i + 1;
						buf[i + 1] = 0x00;
						_SysoXXX.message("发送第二个0A" + pos1);
					}
					else
					{
						break;	//最多取两个
					}
				}
			}
		}
		if (pos1 != 0)
		{
			byte[] pos1Buf = DataConvert.intToBytes(pos1);
			System.arraycopy(pos1Buf, 0, buf, 4, pos1Buf.length);
		}
		if (pos2 != 0)
		{
			byte[] pos2Buf = DataConvert.intToBytes(pos2);
			System.arraycopy(pos2Buf, 0, buf, 8, pos2Buf.length);
		}

		byte[] buf23 = new byte[8];
		System.arraycopy(buf, 4, buf23, 0, 8);
//		_SysoXXX.array(buf23, "发送的position");
	}

	/*-
	 * 分析服务器返回数据
	 *
	 * 分析流程：校验码分析---type分析---suc分析---其他分析
	 */
	private void analysisRcvData(byte[] rcv, SmResult sr, int correctType, int random,
			boolean needAnalysisSmInfo)
	{
		int len = 0;
		byte[] bufTemp = null;
		uncheck0A(rcv);	//先check再解密
		rcv = XCoder.decryptV2(rcv, 12);
		// 先判断random,这个不一致，其他一切皆无意义
		bufTemp = DataConvert.getBytes(rcv, INT_LEN * 10, INT_LEN);
		if (DataConvert.bytesToInt(bufTemp) != random)
		{
			sr.setFailureReason("数据验证错误");
			return;
		}

		// type
		bufTemp = DataConvert.getBytes(rcv, len, SHORT_LEN);
		short type = DataConvert.bytesToShort(bufTemp);
		if (type != correctType)
		{
			sr.setFailureReason("请求type不一致");
			return;
		}

		// suc
		len += SHORT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, SHORT_LEN);
		short suc = DataConvert.bytesToShort(bufTemp);
		sr.setSuc(suc); // 此时调用sr.succeed()才有意义
//		_SysoXXX.message("suc:" + Integer.toBinaryString(suc));
		if (suc <= 0)
		{
			// 这里不能像UserConnect那样if (!ur.succeed())判断
			return;
		}

		if (!needAnalysisSmInfo)
		{
			return;
		}

		SmInfo smInfo = new SmInfo();
		// openCount
		len += SHORT_LEN + INT_LEN * 2;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setOpenCount(DataConvert.bytesToInt(bufTemp));

		// openedCount
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setOpenedCount(DataConvert.bytesToInt(bufTemp));

		// stopRead
		len += INT_LEN * 2;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setMakerAllowed(DataConvert.bytesToInt(bufTemp));

		// id
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setFid(DataConvert.bytesToInt(bufTemp));

		// singleOpenTime
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setSingleOpenTime(DataConvert.bytesToInt(bufTemp));

		// appType
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setAppType(DataConvert.bytesToInt(bufTemp));

		// version
		len += INT_LEN * 2;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setVersion(DataConvert.bytesToInt(bufTemp));

		// days
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setDays(DataConvert.bytesToInt(bufTemp));

		// years
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setYears(DataConvert.bytesToInt(bufTemp));

		// notice
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setNotice(DataConvert.bytesToInt(bufTemp));

		// remainDays
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setRemainDays(DataConvert.bytesToInt(bufTemp));

		// remainYears
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setRemainYears(DataConvert.bytesToInt(bufTemp));

		// ooid
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setOrderId(DataConvert.bytesToInt(bufTemp));

		// bindNum
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setBindNum(DataConvert.bytesToInt(bufTemp));

		// activeNum
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setActiveNum(DataConvert.bytesToInt(bufTemp));

		// showLimit
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setShowLimit(DataConvert.bytesToInt(bufTemp));

		// appId
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setApplyId(DataConvert.bytesToInt(bufTemp));

		// needReapply
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setNeedReApply(DataConvert.bytesToInt(bufTemp));

		// offline
		len += INT_LEN;
		// bufTemp = getBytes(rcv, len, INT_LEN);
		// smInfo.setOffline(bytesToInt(bufTemp));

		// tableID
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setTableId(DataConvert.bytesToInt(bufTemp));

		// fileversion
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setFileVersion(DataConvert.bytesToInt(bufTemp));

		// needShowDiff
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, INT_LEN);
		smInfo.setShowDiff(DataConvert.bytesToInt(bufTemp));

		// userName
		len += INT_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, USERNAME_LEN);
		// 减一不减一都行，因为服务器是C++风格，最后一个是结束符“\0”，下同。
		// bufTemp = getBytes(rcv, len, USERNAME_LEN - 1);
		smInfo.setUserName(DataConvert.getSafeStr(bufTemp));

		// fileName
		len += USERNAME_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, FILE_NAME_LEN);
		smInfo.setFileName(DataConvert.getSafeStr(bufTemp));

		// startTime
		len += FILE_NAME_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, LIMIT_TIME_LEN);
		smInfo.setStartTime(DataConvert.getSafeStr(bufTemp));

		// endTime
		len += LIMIT_TIME_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, LIMIT_TIME_LEN);
		smInfo.setEndTime(DataConvert.getSafeStr(bufTemp));

		// nick
		len += LIMIT_TIME_LEN + MAC_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, NICK_LEN);
		smInfo.setNick(DataConvert.getSafeStr(bufTemp));

		// remark
		len += NICK_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, REMARK_LEN);
		smInfo.setRemark(DataConvert.getSafeStr(bufTemp));

		// email
		len += REMARK_LEN + VERSION_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, EMAIL_LEN);
		smInfo.setEmail(DataConvert.getSafeStr(bufTemp));

		// QQ
		len += EMAIL_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, QQ_LEN);
		smInfo.setQq(DataConvert.getSafeStr(bufTemp));

		// phone
		len += QQ_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, PHONE_LEN);
		smInfo.setPhone(DataConvert.getSafeStr(bufTemp));

		// hardNo
		len += PHONE_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, HARDNO_LEN);
		smInfo.setHardNo(DataConvert.getSafeStr(bufTemp));

		// encodeKey
		len += HARDNO_LEN + SYSINFO_LEN + HASH_VALUE_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, KEY_LEN);
		smInfo.setEncodeKey(bufTemp);

		// firstOpen
		len += KEY_LEN * 2;
		bufTemp = DataConvert.getBytes(rcv, len, TIME_LEN);
		smInfo.setFirstOpenTime(DataConvert.getSafeStr(bufTemp));

		// makeTime
		len += TIME_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, MAKE_TIME_LEN);
		smInfo.setMakeTime(DataConvert.getSafeStr(bufTemp));

		// orderNo
		len += MAKE_TIME_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, ORDERNO_LEN);
		smInfo.setOrderNo(DataConvert.getSafeStr(bufTemp));

		// selfDefine1
		len += ORDERNO_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, SELF_DEFINE_LEN);
		smInfo.setSelfDefineValue1(DataConvert.getSafeStr(bufTemp));

		// selfDefine2
		len += SELF_DEFINE_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, SELF_DEFINE_LEN);
		smInfo.setSelfDefineValue2(DataConvert.getSafeStr(bufTemp));

		// showInfo
		len += SELF_DEFINE_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, SHOW_INFO_LEN);
		smInfo.setShowInfo(DataConvert.getSafeStr(bufTemp));

		// msgId
		len += SHOW_INFO_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, MESSAGE_ID_LEN);
		smInfo.setMsgId(DataConvert.getSafeStr(bufTemp));

		// serisesName
		len += MESSAGE_ID_LEN;
		bufTemp = DataConvert.getBytes(rcv, len, SERIESNAME_LEN);
		smInfo.setSeriesName(DataConvert.getSafeStr(bufTemp));

		sr.setSmInfo(smInfo);

//		_SysoXXX.message("解析后的数据：" + smInfo.toString());
	}

	private void uncheck0A(byte[] rcv)
	{
		byte[] buf = new byte[8];
		System.arraycopy(rcv, 4, buf, 0, 8);
//		_SysoXXX.array(buf, "接收的position");

		byte[] intBuf = new byte[4];
		//pos1
		System.arraycopy(rcv, 4, intBuf, 0, 4);
		int pos1 = DataConvert.bytesToInt(intBuf);
		if (pos1 > 0)	//写大于0是防止转错
		{
			if (rcv[pos1] == 0x00)	//做进一步验证
			{
				rcv[pos1] = 0x0A;
//				_SysoXXX.message("位置" + pos1 + "被替换");
			}
		}
		//pos2
		System.arraycopy(rcv, 8, intBuf, 0, 4);
		int pos2 = DataConvert.bytesToInt(intBuf);
		if (pos2 > 0)	//写大于0是防止转错
		{
			if (rcv[pos2] == 0x00)	//做进一步验证
			{
				rcv[pos2] = 0x0A;
//				_SysoXXX.message("位置" + pos2 + "被替换");
			}
		}
	}
}
