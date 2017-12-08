package cn.com.pyc.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * 广告表
 * 
 * @author QiLiKing 2015-7-29 下午3:31:47
 */
public class AdDao
{
	private static final String TAB_AD = "ad";
	private static final String COL_UID = "uid";	// 用户uid
	private static final String COL_NAME = "name";	// 广告图片名称

	private DiskDB_2 ddb;

	public AdDao()
	{
		ddb = DiskDB_2.getInstance();

		String sql = "CREATE TABLE IF NOT EXISTS " + TAB_AD
				+ "(_ID INTEGER PRIMARY KEY AUTOINCREMENT," //
				+ COL_UID + " TEXT," //
				+ COL_NAME + " TEXT)";
		ddb.openDB().execSQL(sql);

		ddb.closeDB();
	}

	/**
	 * 有则更新，无则插入
	 * 
	 * @param uid
	 * @param name
	 */
	public void updateOrInsert(String uid, String name)
	{
		if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(name))
		{
			return;
		}
		ContentValues values = new ContentValues();
		values.put(COL_UID, uid);
		values.put(COL_NAME, name);
		SQLiteDatabase db = ddb.openDB();
		/* 两个业务以上时，用事物管理，增加效率 */
		db.beginTransaction();
		try
		{
			int row = db.update(TAB_AD, values, COL_UID + "=?", new String[]
			{ uid });
			if (row == 0)
			{
				db.insert(TAB_AD, null, values);
			}
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
			ddb.closeDB();
		}
	}

	/**
	 * @param uid
	 * @return null failure
	 */
	public String query(String uid)
	{
		if (TextUtils.isEmpty(uid))
		{
			return null;
		}
		Cursor c = ddb.openDB().query(TAB_AD, null, COL_UID + "=?", new String[]
		{ uid }, null, null, null);
		String name = null;
		if (c != null)
		{
			if (c.moveToFirst())
			{
				name = c.getString(c.getColumnIndex(COL_NAME));
			}
			c.close();
		}
		ddb.closeDB();	// 注意这里，cursor关闭之后才代表业务结束

		return name;
	}

}
