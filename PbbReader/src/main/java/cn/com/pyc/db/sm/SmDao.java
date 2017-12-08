package cn.com.pyc.db.sm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.CipherUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cn.com.pyc.bean.SmInfo;

/**
 * The database has several versions, we use encryption algorithm for receive
 * table after adding "EncodeKey".<br>
 * Be sure that only for receive table not include send table. <br>
 * These cipher columns are: openNum, openedNum, needApply, firstOpenTime and
 * outData.
 * 
 * @author QiLiKing 2015-9-10 下午3:32:39
 */
public abstract class SmDao
{
	protected Context context;

	public SmDao(Context context)
	{
		this.context = context;
	}

	public static SmDao getInstance(Context context, boolean isReceive)
	{
		if (isReceive)
		{
			return ReceiveDao.getInstance(context);
		}
		else
		{
			return SendDao.getInstance(context);
		}
	}

	protected abstract String getTabName();

	public void delete(SmInfo info)
	{
		if (info == null)
		{
			return;
		}
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		db.delete(getTabName(), SmCol.FILE_ID + "=?", new String[]
		{ String.valueOf(info.getFid()) });
		DatabaseHelper.getInstance(context).closeDB();
	}

	public void delete(Collection<SmInfo> infos)
	{
		if (infos == null || infos.isEmpty())
		{
			return;
		}
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		db.beginTransaction();
		try
		{
			for (SmInfo info : infos)
			{
				db.delete(getTabName(), SmCol.FILE_ID + "=?", new String[]
				{ String.valueOf(info.getFid()) });
			}
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
			DatabaseHelper.getInstance(context).closeDB();
		}
	}

	public void updateOrInsert(SmInfo info)
	{
		if (info == null)
		{
			return;
		}
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		ContentValues values = wrapContentValues(info, isFromReceive());
		db.beginTransaction();
		try
		{
			int row = db.update(getTabName(), values, SmCol.FILE_ID + "=?", new String[]
			{ String.valueOf(info.getFid()) });
			if (row == 0)
			{
				db.insert(getTabName(), null, values);
			}
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
			DatabaseHelper.getInstance(context).closeDB();
		}
	}

	public void updateOrInsert(Collection<SmInfo> infos)
	{
		if (infos == null || infos.isEmpty())
		{
			return;
		}
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		db.beginTransaction();
		try
		{
			/* 不晓得注释部分和现在的哪个更好些 */
			// Cursor c = db.query(getTabName(), new String[]
			// { SmColumes.FILE_ID }, null, null, null, null, null);
			// HashSet<Integer> set = new HashSet<>();
			// if (c != null)
			// {
			// if (c.moveToFirst())
			// {
			// do
			// {
			// set.add(c.getInt(0));
			// }
			// while(c.moveToNext());
			// }
			// c.close();
			// }
			for (SmInfo info : infos)
			{
				ContentValues values = wrapContentValues(info, isFromReceive());
				int row = db.update(getTabName(), values, SmCol.FILE_ID + "=?", new String[]
				{ String.valueOf(info.getFid()) });
				if (row == 0)
				{
					db.insert(getTabName(), null, values);
				}
				// // HashSet性质：如果没有值，则add时返回true
				// if (set.add(info.getFid()))
				// {
				// db.insert(getTabName(), null, values);
				// }
				// else
				// {
				// db.update(getTabName(), values, SmColumes.FILE_ID + "=?",
				// new String[]
				// { String.valueOf(info.getFid()) });
				// }
			}
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
			DatabaseHelper.getInstance(context).closeDB();
		}
	}

	public List<SmInfo> getSmInfos() {
		List<SmInfo> smInfos = new ArrayList<>();
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		Cursor c = db.query(getTabName(), null, null, null, null, null, null);
		if (c != null)
		{
			while (c.moveToNext())
			{
				SmInfo info = new SmInfo();
				fillInfos(c,info,isFromReceive());
				smInfos.add(info);
			}
			c.close();
		}
		DatabaseHelper.getInstance(context).closeDB();
		return smInfos;
	}

	public boolean query()
	{
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		Cursor c = db.query(getTabName(), null, null, null, null, null, null);
		boolean suc = false;
		if (c != null)
		{
			if (c.moveToFirst())
			{
				suc = true;
			}
			c.close();
		}
		DatabaseHelper.getInstance(context).closeDB();

		return suc;
	}

	/**
	 * @param info
	 * @return true有记录
	 */
	public boolean query(SmInfo info)
	{
		if (info == null)
		{
			return false;
		}
		SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
		Cursor c = db.query(getTabName(), null, SmCol.FILE_ID + "=?", new String[]
		{ String.valueOf(info.getFid()) }, null, null, null);
		boolean suc = false;
		if (c != null)
		{
			if (c.moveToFirst())
			{
				fillInfos(c, info, isFromReceive());
				suc = true;
			}
			c.close();
		}
		DatabaseHelper.getInstance(context).closeDB();

		return suc;
	}

	/**
	 * @param mapInfos
	 * @return 没有查询到的infos
	 */
	public HashMap<String, SmInfo> query(HashMap<String, SmInfo> mapInfos)
	{
		HashMap<String, SmInfo> failedInfos = new HashMap<>();
		if (mapInfos != null && !mapInfos.isEmpty())
		{
			SQLiteDatabase db = DatabaseHelper.getInstance(context).openDB();
			db.beginTransaction();
			try
			{
				Iterator<String> iterator = mapInfos.keySet().iterator();
				while(iterator.hasNext())
				{
					String path = iterator.next();
					SmInfo info = mapInfos.get(path);
					Cursor c = db.query(getTabName(), null, SmCol.FILE_ID + "=?", new String[]
					{ String.valueOf(info.getFid()) }, null, null, null);
					if (c != null)
					{
						if (c.moveToFirst())
						{
							fillInfos(c, info, isFromReceive());
							continue;
						}
						c.close();
					}
					// 运行到这里说明没有记录
					failedInfos.put(path, info);
				}
				db.setTransactionSuccessful();
			}
			finally
			{
				db.endTransaction();
				DatabaseHelper.getInstance(context).closeDB();
			}
		}

		return failedInfos;
	}

	private boolean isFromReceive()
	{
		return this instanceof ReceiveDao;
	}

	/**
	 * 调用此方法后一定要检查返回值
	 * 
	 * @param c
	 * @param info
	 * @return false表示操作失败（cursor的问题）
	 */
	protected void fillInfos(Cursor c, SmInfo info, boolean needDecode)
	{
		info.setFid(c.getInt(c.getColumnIndex(SmCol.FILE_ID)));
		info.setMakerAllowed(c.getInt(c.getColumnIndex(SmCol.CAN_OPEN)));
		info.setStartTime(c.getString(c.getColumnIndex(SmCol.START_TIME)));
		info.setEndTime(c.getString(c.getColumnIndex(SmCol.END_TIME)));
		info.setSingleOpenTime(c.getInt(c.getColumnIndex(SmCol.SINGLE_OPEN)));
		info.setRemark(c.getString(c.getColumnIndex(SmCol.REMARK)));
		info.setDays(c.getInt(c.getColumnIndex(SmCol.DAYS)));
		info.setYears(c.getInt(c.getColumnIndex(SmCol.YEARS)));
		info.setPayFile(c.getInt(c.getColumnIndex(SmCol.PAY_FILE)));
		info.setMakeTime(c.getString(c.getColumnIndex(SmCol.MAKE_TIME)));
		info.setNick(c.getString(c.getColumnIndex(SmCol.NICK))); // 发送列表会根据nick是否为空来判断需不需要刷新
		info.setAppType(c.getInt(c.getColumnIndex(SmCol.APP_TYPE)));
		info.setEmail(c.getString(c.getColumnIndex(SmCol.EMAIL)));
		info.setPhone(c.getString(c.getColumnIndex(SmCol.PHONE)));
		info.setQq(c.getString(c.getColumnIndex(SmCol.QQ)));
		// info.setOffline(c.getInt(c.getColumnIndex(SmColumes.OFFLINE)));
		info.setFirstOpenTime(c.getString(c.getColumnIndex(SmCol.FIRST_OPEN_TIME)));

		if (needDecode)
		{
			info.setOpenCount(DataConvert
					.bytesToInt(CipherUtil.decrypt(c.getBlob(c.getColumnIndex(SmCol.OPEN_NUM)))));
			info.setOpenedCount(DataConvert
					.bytesToInt(CipherUtil.decrypt(c.getBlob(c.getColumnIndex(SmCol.OPENED_NUM)))));
		}
		else
		{
			info.setOpenCount(c.getInt(c.getColumnIndex(SmCol.OPEN_NUM)));
			info.setOpenedCount(c.getInt(c.getColumnIndex(SmCol.OPENED_NUM)));
		}
	}

	/*-
	 * 两张表共用
	 */
	protected ContentValues wrapContentValues(SmInfo info, boolean needCode)
	{
		ContentValues values = new ContentValues();
		values.put(SmCol.FILE_ID, info.getFid());
		values.put(SmCol.CAN_OPEN, info.getMakerAllowed());
		values.put(SmCol.START_TIME, info.getStartTime());
		values.put(SmCol.END_TIME, info.getEndTime());
		values.put(SmCol.SINGLE_OPEN, info.getSingleOpenTime());
		values.put(SmCol.REMARK, info.getRemark());
		values.put(SmCol.DAYS, info.getDays());
		values.put(SmCol.YEARS, info.getYears());
		values.put(SmCol.PAY_FILE, info.getPayFile());
		values.put(SmCol.MAKE_TIME, info.getMakeTime());
		values.put(SmCol.NICK, info.getNick());
		values.put(SmCol.APP_TYPE, info.getAppType());
		values.put(SmCol.EMAIL, info.getEmail());
		values.put(SmCol.PHONE, info.getPhone());
		values.put(SmCol.QQ, info.getQq());
		// values.put(SmColumes.OFFLINE, info.getOffline());
		values.put(SmCol.FIRST_OPEN_TIME, info.getFirstOpenTime());

		if (needCode)
		{
			values.put(SmCol.OPEN_NUM,
					CipherUtil.encrypt(DataConvert.intToBytes(info.getOpenCount())));
			values.put(SmCol.OPENED_NUM,
					CipherUtil.encrypt(DataConvert.intToBytes(info.getOpenedCount())));
		}
		else
		{
			values.put(SmCol.OPEN_NUM, info.getOpenCount());
			values.put(SmCol.OPENED_NUM, info.getOpenedCount());
		}
		return values;
	}
}
