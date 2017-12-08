package com.sz.mobilesdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.util.Log;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.util.SZLog;

public class DBHelper extends net.sqlcipher.database.SQLiteOpenHelper
{
	public static final String DB_LABLE = ".sqlite"; // 数据库后缀扩展名
	private static final int DB_VERSION = 3; // 数据库版本，无需修改！
	private static volatile DBHelper INSTANCE; // 数据库实例
	private static volatile net.sqlcipher.database.SQLiteDatabase db;

	/**
	 * 设置SQlite close,销毁当前DBHelper<br/>
	 * 
	 * <br/>
	 * 
	 * DBHelper = null; db.close。
	 */
	public static void setDBHelperNULL()
	{
		if (db != null)
		{
			if (db.isOpen()) db.close();
		}
		db = null;
		if (INSTANCE != null) INSTANCE.close();
		INSTANCE = null;

		Log.v("", "mDBHelper is null,and db also set null");
	}

	private DBHelper(Context context, String dbName)
	{
		super(context, dbName, null, DB_VERSION);
	}

	/**
	 * (访问同步) <br>
	 * 
	 * 获取DBHelper实例
	 * 
	 * @param context
	 * @param name
	 *            user login Name
	 * @return SQLiteOpenHelper实例
	 */
	public static DBHelper getInstance(Context context, String name)
	{
		if (INSTANCE == null)
		{
			synchronized (DBHelper.class)
			{
				if (INSTANCE == null)
				{
					if (SZInitInterface.isDBEncrypt)
					{
						// 创建数据库之前调用，sqlClipher加密库。
						net.sqlcipher.database.SQLiteDatabase.loadLibs(context);
					}
					String dbName = name + DB_LABLE;
					SZLog.d("dBase called: " + dbName);
					INSTANCE = new DBHelper(context, dbName);
					db = INSTANCE.getWritableDatabase(Constant
							.getDBCliperValue());
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public void onCreate(net.sqlcipher.database.SQLiteDatabase db)
	{
	}

	@Override
	public void onUpgrade(net.sqlcipher.database.SQLiteDatabase db, int arg1,
			int arg2)
	{
	}

	// ---------------------------------------------------------------
	// --------------------------以下应用数据表相关-----------------------
	// ---------------------------------------------------------------
	public void DeleteBookmark(String Asset_id)
	{
		getDB();
		db.execSQL("DELETE FROM Bookmark WHERE content_ids=?",
				new String[] { Asset_id });
	}

	public void DeleteAlbum(String id)
	{
		getDB();
		db.execSQL("DELETE FROM Album WHERE _id=?", new String[] { id });
	}

	public void DeleteAsset(String right_id)
	{
		getDB();
		db.execSQL("DELETE FROM Asset WHERE right_id=?",
				new String[] { right_id });
	}

	public void DeletePermission(String _id)
	{
		getDB();
		db.execSQL("DELETE FROM Permission WHERE _id=?", new String[] { _id });
	}

	public void DeletePerconstraint(String permission_id)
	{
		getDB();
		db.execSQL("DELETE FROM Perconstraint WHERE Permission_id=?",
				new String[] { permission_id });
	}

	public void DeleteAlbumContent(String album_id)
	{
		getDB();
		db.execSQL("DELETE FROM AlbumContent WHERE album_id=?",
				new String[] { album_id });
	}

	public void DeleteRight(String _id)
	{
		getDB();
		db.execSQL("DELETE FROM Right WHERE _id=?", new String[] { _id });
	}

	public void DeleteAlbumContentByCollectionId(String CollectionId) {
		getDB();
		db.execSQL("DELETE FROM AlbumContent WHERE collectionId =?",
				new Object[]{CollectionId});
	}

	// ---------------------------------------------------------------
	// --------------------------以上应用数据表相关-----------------------
	// ---------------------------------------------------------------

	public void DeleteTableData(String tableName)
	{
		getDB();
		// db.execSQL("DELETE FROM " + tableName);
		int result = db.delete(tableName, null, null);
		SZLog.i("DeleteTableData: " + tableName + ", result: " + result);
		// SZLog.i("DeleteTableData: " + tableName);
	}

	public void DropTable(String tableName)
	{
		getDB();
		db.execSQL("DROP TABLE " + tableName);
		SZLog.i("DropTable: " + tableName);
	}

	/**
	 * 插入数据
	 * 
	 * @param Table_Name
	 * @param values
	 * @return
	 */
	public long insert(String Table_Name, ContentValues values)
	{
		getDB();
		return db.insert(Table_Name, null, values);
	}

	/**
	 * 删除
	 * 
	 * @param Table_Name
	 * @param id
	 * @return 影响行数
	 */
	public int delete(String Table_Name, int id)
	{
		getDB();
		return delete(Table_Name, String.valueOf(id));
	}

	public int delete(String Table_Name, String id)
	{
		getDB();
		return db.delete(Table_Name, BaseColumns._ID + "=?",
				new String[] { id });
	}

	/**
	 * @param Table_Name
	 * @param values
	 * @param WhereClause
	 * @param whereArgs
	 * @return 影响行数
	 */
	public int update(String Table_Name, ContentValues values,
			String WhereClause, String[] whereArgs)
	{
		getDB();
		return db.update(Table_Name, values, WhereClause, whereArgs);
	}

	/**
	 * 查询
	 * 
	 * @param Table_Name
	 * @param columns
	 * @param whereStr
	 * @param whereArgs
	 * @return
	 */
	public net.sqlcipher.Cursor query(String Table_Name, String[] columns,
			String whereStr, String[] whereArgs)
	{
		return query(Table_Name, columns, whereStr, whereArgs, null, null, null);
	}

	public net.sqlcipher.Cursor query(String Table_Name, String[] columns,
			String whereStr, String[] whereArgs, String groupBy, String having,
			String orderBy)
	{
		getReadDB();
		return db.query(Table_Name, columns, whereStr, whereArgs, groupBy,
				having, orderBy);
	}

	public net.sqlcipher.Cursor rawQuery(String sql, String[] args)
	{
		getReadDB();
		return db.rawQuery(sql, args);
	}

	/**
	 * Sql语句执行
	 * 
	 * @param sql
	 */
	public void ExecSQL(String sql)
	{
		getDB();
		db.execSQL(sql);
	}

	public void closeDb()
	{
		if (db != null)
		{
			if (db.isOpen())
			{
				db.close();
			}
			db = null;
		}
	}

	public net.sqlcipher.database.SQLiteDatabase getDB()
	{
		if (db == null)
		{
			db = getWritableDatabase(Constant.getDBCliperValue());
		}
		return db;
	}

	private net.sqlcipher.database.SQLiteDatabase getReadDB()
	{
		if (db == null)
		{
			db = getReadableDatabase(Constant.getDBCliperValue());
		}
		return db;
	}

}
