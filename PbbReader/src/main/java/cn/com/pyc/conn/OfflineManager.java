package cn.com.pyc.conn;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.com.pyc.bean.OfflineInfo;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.xcoder.SmHeadExtraData;
import cn.com.pyc.xcoder.XCoder;
import cn.com.pyc.xcoder.XCoderResult;

import static cn.com.pyc.bean.ProtocolInfo.EMAIL_LEN;
import static cn.com.pyc.bean.ProtocolInfo.HARDNO_LEN;
import static cn.com.pyc.bean.ProtocolInfo.INT_LEN;
import static cn.com.pyc.bean.ProtocolInfo.KEY_LEN;
import static cn.com.pyc.bean.ProtocolInfo.OFFLINE_TOTAL_LEN;
import static cn.com.pyc.bean.ProtocolInfo.PHONE_LEN;
import static cn.com.pyc.bean.ProtocolInfo.QQ_LEN;
import static cn.com.pyc.bean.ProtocolInfo.SELF_DEFINE_LEN;
import static cn.com.pyc.bean.ProtocolInfo.SM_FILE_PATH_LEN;
import static cn.com.pyc.bean.ProtocolInfo.TIME_LEN;

/**
 * 本类不需联网,它的数据都是从文件中获得的
 * 其实这些方法也可以直接放到OfflineInfo中,但为了形式统一,建立本类
 * 
 * @author QiLiKing 2014-11-20
 */
public class OfflineManager
{
	private OfflineManager()
	{
	}

	/**
	 * SmInfo中必须包含filePath和sessionKey
	 * 返回最新的离线结构的信息
	 * 
	 * @param smInfo
	 * @return null IO错误
	 */
	public static OfflineInfo getLatestInfo(SmInfo smInfo)
	{
		FileInputStream fis = null;
		try
		{
			final String filePath = smInfo.getFilePath();
			//Log.v("OfflineManager",filePath);
			File file = new File(filePath);
			final long len = file.length() - OFFLINE_TOTAL_LEN - SmHeadExtraData.FLAG_LEN;
			byte[] buffer = new byte[OFFLINE_TOTAL_LEN];

			fis = new FileInputStream(file);
			fis.skip(len); // 注意长度
			fis.read(buffer);

			XCoder.setEncryptKey(smInfo.getSessionKey(), 1);
			XCoder.decodeBuffer(buffer, buffer, OFFLINE_TOTAL_LEN); // 解密数据
			OfflineInfo info = analysisBuffer(buffer);
			return info;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			IOUtil.close(fis, null);
		}
	}

	/**
	 * *SmInfo中必须包含filePath和sessionKey
	 * 返回最新的离线结构的信息
	 * 
	 * @param smInfo
	 *            返回时已是最新数据
	 * @return
	 */
	public static XCoderResult analysisFlagFromFile(SmInfo smInfo)
	{
		XCoderResult xr = new XCoderResult();
		OfflineInfo offlineInfo = getLatestInfo(smInfo);
		if (offlineInfo != null)
		{
			offlineInfo.convertToSmInfo(smInfo);
			xr.setSmInfo(smInfo);
		}
		else
		{
			xr.setSuc(XCoderResult.FILE_ERROR);
		}
		return xr;
	}

	/**
	 * 每次打开、申请成功后、打开时显示已经申请过、离线验证；只有这几种情况下更新
	 * 
	 * @param offlineInfo
	 *            必须保证offlineInfo的timeModify等是有效的（SmInfo.
	 *            convertToOfflineInfo不会转换某些字段）
	 */
	public static void updateFile(SmInfo smInfo, OfflineInfo offlineInfo)
	{
		// 2015-03-16的版本，将离线结构移入数据库中，不再更新文件
		// RandomAccessFile raf = null;
		// try
		// {
		// File file = new File(smInfo.getFilePath()); // 只要更新,就肯定是原文件
		// raf = new RandomAccessFile(file, "rw");
		// final long len = file.length() - OFFLINE_TOTAL_LEN
		// - SmHeadExtraData.FLAG_LEN; // 定位到离线结构起始位置
		// raf.seek(len);
		// if (offlineInfo.isClear())
		// {
		// offlineInfo = new OfflineInfo();
		// }
		// raf.write(packageBuffer(offlineInfo, smInfo.getSessionKey())); //
		// 写入数据
		// if (!offlineInfo.isClear()
		// && !TextUtils.isEmpty(offlineInfo.getFileModifyTime()))
		// {
		// long time = DataConvert.toLongTime(offlineInfo
		// .getFileModifyTime());
		// file.setLastModified(time); // 更新文件时间
		// }
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// }
		// finally
		// {
		// if (raf != null)
		// {
		// try
		// {
		// raf.close();
		// }
		// catch (IOException e)
		// {
		// e.printStackTrace();
		// }
		// }
		// }
	}

	/**
	 * 将它也开放是考虑到加密时写入离线数据的需要
	 * 
	 * @param info
	 * @param sessionKey
	 * @return
	 */
	public static byte[] packageBuffer(OfflineInfo info, byte[] sessionKey)
	{
		int len = 0;
		byte[] temp = null;
		byte[] buffer = new byte[OFFLINE_TOTAL_LEN];

		// structFlag
		temp = DataConvert.intToBytes(info.getStructFlag());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// applayId
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getApplyId());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// isOffline
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getOffline());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// actived
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getActived());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// timeModifid
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getTimeModify());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// openCount
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getOpenCount());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// openedCount
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getOpenedCount());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// days
		len += 2 * INT_LEN;
		temp = DataConvert.intToBytes(info.getDays());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// years
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getYears());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// showLimit
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getShowLimit());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// choseNum
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getContactMust());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// fieldNum
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getSelfMust());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// secret
		len += INT_LEN;
		temp = DataConvert.intToBytes(info.getSecret());
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// fileCreateTime
		len += INT_LEN;
		temp = info.getFileCreateTime().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// fileModifyTime
		len += TIME_LEN;
		temp = info.getFileModifyTime().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// lastOpenTime
		len += TIME_LEN;
		temp = info.getLastOpenTime().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// endTime
		len += TIME_LEN;
		temp = info.getOutData().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// firstOpenTime
		len += TIME_LEN;
		temp = info.getFirstOpenTime().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// qq
		len += TIME_LEN;
		temp = info.getQqBuyer().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// email
		len += QQ_LEN;
		temp = info.getEmailBuyer().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// phone
		len += EMAIL_LEN;
		temp = info.getPhoneBuyer().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// selfDefineKey1
		len += PHONE_LEN;
		temp = info.getSelfDefineKey1().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// selfDefineKey2
		len += SELF_DEFINE_LEN;
		temp = info.getSelfDefineKey2().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// selfDefine1
		len += SELF_DEFINE_LEN;
		temp = info.getSelfDefineValue1().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// selfDefine2
		len += SELF_DEFINE_LEN;
		temp = info.getSelfDefineValue2().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// hardNo
		len += SELF_DEFINE_LEN;
		temp = info.getHardNo().getBytes();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// smFilePath
		len += HARDNO_LEN;
		temp = info.getFilePath().getBytes();
		int l = temp.length >= SM_FILE_PATH_LEN ? SM_FILE_PATH_LEN : temp.length;	// 路径可能会超出范围
		System.arraycopy(temp, 0, buffer, len, l);

		// encodeKey
		len += SM_FILE_PATH_LEN;
		temp = info.getEncodeKey();
		System.arraycopy(temp, 0, buffer, len, temp.length);

		// _SysoXXX.array(flagBuffer, "加密前的数据");

		// 加密
		XCoder.setEncryptKey(sessionKey, 1);
		XCoder.codeBuffer(buffer, buffer, OFFLINE_TOTAL_LEN);
		// _SysoXXX.array(os.getSessionKey(), "加密离线结构的sessionKey");
		// _SysoXXX.array(flagBuffer, "加密后的数据");
		return buffer;
	}

	private static OfflineInfo analysisBuffer(byte[] buffer)
	{
		int len = INT_LEN;
		byte[] temp = new byte[INT_LEN];
		OfflineInfo info = new OfflineInfo();

		// applayId
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setApplyId(DataConvert.bytesToInt(temp));

		// isOffline
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setOffline(DataConvert.bytesToInt(temp));

		// actived
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setActived(DataConvert.bytesToInt(temp));

		// timeModifid
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setTimeModify(DataConvert.bytesToInt(temp));

		// openCount
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setOpenCount(DataConvert.bytesToInt(temp));

		// openedCount
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setOpenedCount(DataConvert.bytesToInt(temp));

		// days
		len += 2 * INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setDays(DataConvert.bytesToInt(temp));

		// years
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setYears(DataConvert.bytesToInt(temp));

		// showLimit
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setShowLimit(DataConvert.bytesToInt(temp));

		// choseNum
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setContactMust(DataConvert.bytesToInt(temp));

		// fieldNum
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setSelfMust(DataConvert.bytesToInt(temp));

		// secret
		len += INT_LEN;
		System.arraycopy(buffer, len, temp, 0, INT_LEN);
		info.setSecret(DataConvert.bytesToInt(temp));

		// fileCreateTime
		len += INT_LEN;
		temp = DataConvert.getBytes(buffer, len, TIME_LEN);
		info.setFileCreateTime(DataConvert.getSafeStr(temp));

		// fileModifyTime
		len += TIME_LEN;
		temp = DataConvert.getBytes(buffer, len, TIME_LEN);
		info.setFileModifyTime(DataConvert.getSafeStr(temp));

		// lastOpenTime
		len += TIME_LEN;
		temp = DataConvert.getBytes(buffer, len, TIME_LEN);
		info.setLastOpenTime(DataConvert.getSafeStr(temp));

		// endTime
		len += TIME_LEN;
		temp = DataConvert.getBytes(buffer, len, TIME_LEN);
		info.setOutData(DataConvert.getSafeStr(temp));

		// firstOpenTime
		len += TIME_LEN;
		temp = DataConvert.getBytes(buffer, len, TIME_LEN);
		info.setFirstOpenTime(DataConvert.getSafeStr(temp));

		// qq
		len += TIME_LEN;
		temp = DataConvert.getBytes(buffer, len, QQ_LEN);
		info.setQqBuyer(DataConvert.getSafeStr(temp));

		// email
		len += QQ_LEN;
		temp = DataConvert.getBytes(buffer, len, EMAIL_LEN);
		info.setEmailBuyer(DataConvert.getSafeStr(temp));

		// phone
		len += EMAIL_LEN;
		temp = DataConvert.getBytes(buffer, len, PHONE_LEN);
		info.setPhoneBuyer(DataConvert.getSafeStr(temp));

		// selfDefineKey1
		len += PHONE_LEN;
		temp = DataConvert.getBytes(buffer, len, SELF_DEFINE_LEN);
		info.setSelfDefineKey1(DataConvert.getSafeStr(temp));

		// selfDefineKey2
		len += SELF_DEFINE_LEN;
		temp = DataConvert.getBytes(buffer, len, SELF_DEFINE_LEN);
		info.setSelfDefineKey2(DataConvert.getSafeStr(temp));

		// selfDefine1
		len += SELF_DEFINE_LEN;
		temp = DataConvert.getBytes(buffer, len, SELF_DEFINE_LEN);
		info.setSelfDefineValue1(DataConvert.getSafeStr(temp));

		// selfDefine2
		len += SELF_DEFINE_LEN;
		temp = DataConvert.getBytes(buffer, len, SELF_DEFINE_LEN);
		info.setSelfDefineValue2(DataConvert.getSafeStr(temp));

		// hardNo
		len += SELF_DEFINE_LEN;
		temp = DataConvert.getBytes(buffer, len, HARDNO_LEN);
		info.setHardNo(DataConvert.getSafeStr(temp));

		// smFilePath
		len += HARDNO_LEN;
		temp = DataConvert.getBytes(buffer, len, SM_FILE_PATH_LEN);
		info.setFilePath(DataConvert.getSafeStr(temp));

		// encodeKey
		len += SM_FILE_PATH_LEN;
		temp = DataConvert.getBytes(buffer, len, KEY_LEN);
		info.setEncodeKey(temp);

		// _SysoXXX.message("分析文件：" + os.toString());
		return info;
	}

}
