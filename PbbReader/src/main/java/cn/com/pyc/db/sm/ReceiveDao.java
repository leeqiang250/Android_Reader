package cn.com.pyc.db.sm;

import java.util.ArrayList;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.CipherUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.com.pyc.bean.SmInfo;

/**
 * 接受列表的加解密办法：<br>
 * <dd>初始化时检查有无encodeKey字段：没有则先读取所有信息，然后加密存入；有则不管
 * <tr>
 * <dd>公共接口读时直接解密，写时直接加密
 * 
 * @author QiLiKing 2015-3-25
 */
public class ReceiveDao extends SmDao
{
	private static ReceiveDao receiveDao;

	public static ReceiveDao getInstance(Context context)
	{
		if (receiveDao == null)
		{
			receiveDao = new ReceiveDao(context);
		}
		return receiveDao;
	}

	public ReceiveDao(Context context)
	{
		super(context);
	}

	/**
	 * 查询sid系列所有文件
	 * 
	 * @param sid
	 * @return
	 */
	public ArrayList<SmInfo> getSidFiles(int sid)
	{
		ArrayList<SmInfo> infos = new ArrayList<SmInfo>();
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		Cursor c = db.query(getTabName(), null, RcvCol.SERIES_ID + "=?", new String[]
		{ String.valueOf(sid) }, null, null, null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				do
				{
					SmInfo info = new SmInfo();
					fillInfos(c, info, true);
					infos.add(info);
				}
				while(c.moveToNext());
			}
			c.close();
		}
		DatabaseHelper.getInstance(context).closeDB();

		return infos;
	}

	/**
	 * 为广告特设的方法
	 * 
	 * @param fid
	 * @param uid
	 */
	public void updateOrInsertUid(int fid, String uid)
	{
		ContentValues values = new ContentValues();
		values.put(RcvCol.MAKER_UID, uid);
		values.put(SmCol.FILE_ID, fid);
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		Cursor c = db.query(getTabName(), null, SmCol.FILE_ID + " =?", new String[]
		{ String.valueOf(fid) }, null, null, null);
		if (c != null && c.moveToFirst())
		{
			c.close();
			db.update(getTabName(), values, SmCol.FILE_ID + "=?", new String[]
			{ String.valueOf(fid) });
		}
		else
		{
			db.insert(getTabName(), null, values);
		}

		DatabaseHelper.getInstance(context).closeDB();
	}

	@Override
	protected String getTabName()
	{
		return RcvCol.TAB_RECEIVE;
	}

	@Override
	protected void fillInfos(Cursor c, SmInfo info, boolean needDecode)
	{
		super.fillInfos(c, info, needDecode);
		info.setRemainDays(c.getInt(c.getColumnIndex(RcvCol.REMAIN_DAYS)));
		info.setRemainYears(c.getInt(c.getColumnIndex(RcvCol.REMAIN_YEARS)));
		info.setShowLimit(c.getInt(c.getColumnIndex(RcvCol.SHOW_LIMIT)));
		info.setUid(c.getString(c.getColumnIndex(RcvCol.MAKER_UID)));
		info.setHardNo(c.getString(c.getColumnIndex(RcvCol.HARD_NO)));
		info.setEncodeKey(c.getBlob(c.getColumnIndex(RcvCol.ENCODE_KEY)));
		info.setTimeModify(c.getInt(c.getColumnIndex(RcvCol.TIME_MODIFIED)));
		info.setContactMust(c.getInt(c.getColumnIndex(RcvCol.CONTACT_MUST)));
		info.setSelfMust(c.getInt(c.getColumnIndex(RcvCol.SELF_MUST)));
		info.setSecret(c.getInt(c.getColumnIndex(RcvCol.SECRET)));
		info.setLastOpenTime(c.getString(c.getColumnIndex(RcvCol.LAST_OPEN_TIME)));
		info.setEmailBuyer(c.getString(c.getColumnIndex(RcvCol.EMAIL_BUYER)));
		info.setPhoneBuyer(c.getString(c.getColumnIndex(RcvCol.PHONE_BUYER)));
		info.setQqBuyer(c.getString(c.getColumnIndex(RcvCol.QQ_BUYER)));
		info.setSelfDefineKey1(c.getString(c.getColumnIndex(RcvCol.SELF_DEFINE_KEY1)));
		info.setSelfDefineKey2(c.getString(c.getColumnIndex(RcvCol.SELF_DEFINE_KEY2)));
		info.setSelfDefineValue1(c.getString(c.getColumnIndex(RcvCol.SELF_DEFINE_VALUE1)));
		info.setSelfDefineValue2(c.getString(c.getColumnIndex(RcvCol.SELF_DEFINE_VALUE2)));
		info.setSid(c.getInt(c.getColumnIndex(RcvCol.SERIES_ID)));
		info.setFilePath(c.getString(c.getColumnIndex(RcvCol.FILE_PATH)));
		int index = c.getColumnIndex(RcvCol.FILE_VERSION);
		if (index != -1)
		{
			//根据错误日志，这里可能为－1
			info.setFileVersion(c.getInt(index));
		}

		if (needDecode)
		{
			info.setNeedApply(DataConvert.bytesToInt(
					CipherUtil.decrypt(c.getBlob(c.getColumnIndex(RcvCol.NEED_ACTIVE)))));
			info.setFirstOpenTime(
					new String(CipherUtil.decrypt(c.getBlob(c.getColumnIndex(RcvCol.FIRST_OPEN)))));
			info.setOutData(
					new String(CipherUtil.decrypt(c.getBlob(c.getColumnIndex(RcvCol.OUT_DATE)))));
		}
		else
		{
			info.setNeedApply(c.getInt(c.getColumnIndex(RcvCol.NEED_ACTIVE)));
			info.setFirstOpenTime(c.getString(c.getColumnIndex(RcvCol.FIRST_OPEN)));
			info.setOutData(c.getString(c.getColumnIndex(RcvCol.OUT_DATE)));
		}

		SmInfo.calculateRemainDaysAndYears(info);	// 每次查询都要重新计算剩余时间
	}

	@Override
	protected ContentValues wrapContentValues(SmInfo info, boolean needCode)
	{
		ContentValues values = super.wrapContentValues(info, needCode);
		values.put(RcvCol.REMAIN_DAYS, info.getRemainDays());
		values.put(RcvCol.REMAIN_YEARS, info.getRemainYears());
		values.put(RcvCol.SHOW_LIMIT, info.getShowLimit());
		values.put(RcvCol.MAKER_UID, info.getUid());
		values.put(RcvCol.HARD_NO, info.getHardNo());
		values.put(RcvCol.ENCODE_KEY, info.getEncodeKey());
		values.put(RcvCol.TIME_MODIFIED, info.getTimeModify());
		values.put(RcvCol.CONTACT_MUST, info.getContactMust());
		values.put(RcvCol.SELF_MUST, info.getSelfMust());
		values.put(RcvCol.SECRET, info.getSecret());
		values.put(RcvCol.LAST_OPEN_TIME, info.getLastOpenTime());
		values.put(RcvCol.EMAIL_BUYER, info.getEmailBuyer());
		values.put(RcvCol.PHONE_BUYER, info.getPhoneBuyer());
		values.put(RcvCol.QQ_BUYER, info.getQqBuyer());
		values.put(RcvCol.SELF_DEFINE_KEY1, info.getSelfDefineKey1());
		values.put(RcvCol.SELF_DEFINE_KEY2, info.getSelfDefineKey2());
		values.put(RcvCol.SELF_DEFINE_VALUE1, info.getSelfDefineValue1());
		values.put(RcvCol.SELF_DEFINE_VALUE2, info.getSelfDefineValue2());
		values.put(RcvCol.SERIES_ID, info.getSid());
		values.put(RcvCol.FILE_PATH, info.getFilePath());
		values.put(RcvCol.FILE_VERSION, info.getFileVersion());

		if (needCode)
		{
			values.put(RcvCol.NEED_ACTIVE,
					CipherUtil.encrypt(DataConvert.intToBytes(info.getNeedApply())));	// 注意：这里绝不能调用getNeedActive
			values.put(RcvCol.FIRST_OPEN, CipherUtil.encrypt(info.getFirstOpenTime().getBytes()));
			values.put(RcvCol.OUT_DATE, CipherUtil.encrypt(info.getOutData().getBytes()));
		}
		else
		{
			values.put(RcvCol.NEED_ACTIVE, info.getNeedApply());	// 注意：这里绝不能调用getNeedActive
			values.put(RcvCol.FIRST_OPEN, info.getFirstOpenTime());
			values.put(RcvCol.OUT_DATE, info.getOutData());
		}
		return values;
	}

}
