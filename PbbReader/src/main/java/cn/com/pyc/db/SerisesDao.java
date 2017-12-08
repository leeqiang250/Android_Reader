package cn.com.pyc.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.com.pyc.bean.SmInfo;

public class SerisesDao
{
	private DiskDB_2 ddb;
	private static final String TAB_SERISES = "serises";

	private static SerisesDao sd;

	public static SerisesDao getInstance()
	{
		if (sd == null)
		{
			sd = new SerisesDao();
		}
		return sd;
	}

	private SerisesDao()
	{
		String sql = "CREATE TABLE IF NOT EXISTS  " + TAB_SERISES
				+ "(_ID INTEGER PRIMARY KEY AUTOINCREMENT," + "serise_id INTEGER,"
				+ "serise_name TEXT," + "serise_file_num INTEGER," + "seller_nick TEXT)";

		ddb = DiskDB_2.getInstance();
		SQLiteDatabase db = ddb.openDB();
		db.execSQL(sql);
		ddb.closeDB();
	}

	/**
	 * @deprecated 使用updateOrInsert
	 */
	@SuppressWarnings("unused")
	private void insert(SmInfo info)
	{
		if (info == null || info.getSid() < 0)
		{
			return;
		}
		ddb.openDB().insert(TAB_SERISES, null, wrapContentValues(info));
		ddb.closeDB();
	}

	/**
	 * 强烈建议用它代替insert和update
	 * <p>
	 * 方便：不管插入或者更新，用这一个方法即可
	 * 
	 * @param info
	 */
	public final void updateOrInsert(SmInfo info)
	{
		if (info == null || info.getSid() < 0)
		{
			return;
		}
		ContentValues values = wrapContentValues(info);
		SQLiteDatabase db = ddb.openDB();
		db.beginTransaction();
		try
		{
			int row = db.update(TAB_SERISES, values, SerisesCol.SERISE_ID + "=?", new String[]
			{ String.valueOf(info.getSid()) });
			if (row == 0)
			{
				db.insert(TAB_SERISES, null, values);
			}
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
			ddb.closeDB();
		}
	}

	public void query(SmInfo info)
	{
		if (info == null || info.getSid() < 0)
		{
			return;
		}
		Cursor c = ddb.openDB().query(TAB_SERISES, null, SerisesCol.SERISE_ID + " =?", new String[]
		{ String.valueOf(info.getSid()) }, null, null, null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				fillInfos(c, info);
			}
			c.close();
		}
		ddb.closeDB();
	}

	/**
	 * @deprecated 使用updateOrInsert替代
	 * @param info
	 */
	@SuppressWarnings("unused")
	private void update(SmInfo info)
	{
		if (info == null || info.getSid() < 0)
		{
			return;
		}
		ddb.openDB().update(TAB_SERISES, wrapContentValues(info), SerisesCol.SERISE_ID + "=?",
				new String[]
				{ String.valueOf(info.getSid()) });
		ddb.closeDB();
	}

	/**
	 * 获取系列表中所有信息
	 * 
	 * @return
	 */
	public ArrayList<SmInfo> getSeriesInfos()
	{
		ArrayList<SmInfo> infos = new ArrayList<SmInfo>();
		SQLiteDatabase db = ddb.openDB();
		Cursor c = db.query(TAB_SERISES, null, null, null, null, null, null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				do
				{
					SmInfo info = new SmInfo();
					fillInfos(c, info);
					infos.add(info);
				}
				while(c.moveToNext());
			}
			c.close();
		}
		ddb.closeDB();

		return infos;
	}

	/**
	 * 删除某一系列
	 * 
	 * @param sinfo
	 */
	public void delete(SmInfo info)
	{
		if (info == null || info.getSid() < 0)
		{
			return;
		}
		ddb.openDB().delete(TAB_SERISES, SerisesCol.SERISE_ID + "=?", new String[]
		{ String.valueOf(info.getSid()) });
		ddb.closeDB();
	}

	private void fillInfos(Cursor c, SmInfo info)
	{
		info.setSeriesName(c.getString(c.getColumnIndex(SerisesCol.SERISE_NAME)));
		info.setSid(c.getInt(c.getColumnIndex(SerisesCol.SERISE_ID)));
		info.setSeriseFilesNum(c.getInt(c.getColumnIndex(SerisesCol.SERISE_FILE_NUM)));
		info.setNick(c.getString(c.getColumnIndex(SerisesCol.SERISE_SELLER_NICK)));
	}

	private ContentValues wrapContentValues(SmInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(SerisesCol.SERISE_ID, info.getSid());
		values.put(SerisesCol.SERISE_NAME, info.getSeriesName());
		values.put(SerisesCol.SERISE_FILE_NUM, info.getSeriseFilesNum());
		values.put(SerisesCol.SERISE_SELLER_NICK, info.getNick());
		return values;
	}

	interface SerisesCol
	{
		public static final String SERISE_ID = "serise_id";
		public static final String SERISE_NAME = "serise_name";
		public static final String SERISE_FILE_NUM = "serise_file_num";
		public static final String SERISE_SELLER_NICK = "seller_nick";
	}
}
