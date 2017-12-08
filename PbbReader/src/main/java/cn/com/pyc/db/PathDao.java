package cn.com.pyc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.qlk.util.tool.Util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import cn.com.pyc.utils.Dirs;

/**
 * 注意：表中存储的都是相对路径，防止系统升级所带来的根目录路径变化
 * <p>
 * 如:/mnt/sdcard/myfolder/0.jpg被存为0/myfolder/0.jpg
 * <p>
 * 所以在操作时要分外注意
 */
public class PathDao
{
	private static final String TAB_PATH = "path";
	private static final String COL_CIPHER_PATH = "cipher";
	private static final String COL_PLAIN_PATH = "plain";

	private Context context;
	private DiskDB ddb;
	private static PathDao pathDao;

	public static PathDao getInstance(Context context)
	{
		if (pathDao == null)
		{
			pathDao = new PathDao(context);
		}
		return pathDao;
	}

	private PathDao(Context context)
	{
		this.context = context;
		ddb = DiskDB.getInstance();

		String sql = "CREATE TABLE IF NOT EXISTS " + TAB_PATH
				+ "(_ID INTEGER PRIMARY KEY AUTOINCREMENT," //
				+ COL_CIPHER_PATH + " TEXT," //
				+ COL_PLAIN_PATH + " TEXT)";
		SQLiteDatabase db = ddb.openDB();
		db.execSQL(sql);

		migrateDB(db);
		ddb.closeDB();
	}

	// 迁移数据库---这里出现的明文字符串都是以前版本的数据库所用字段
	private void migrateDB(SQLiteDatabase db)
	{
		ArrayList<String> cardsPaths = Dirs.getCardsPaths();
		for (String boot : cardsPaths)
		{
			String dbPath = Dirs.getUserDir(context, boot) + "/pyc_safe.db";
			if (new File(dbPath).exists())
			{
				final HashMap<String, String> mapPaths = new HashMap<String, String>();
				SQLiteDatabase dbOriginal = SQLiteDatabase.openDatabase(dbPath, null,
						SQLiteDatabase.OPEN_READONLY);
				Cursor c = dbOriginal.query("privacy_path", null, null, null, null, null, null);
				if (c != null && c.moveToFirst())
				{
					final int cipherIndex = c.getColumnIndex("new_path");
					final int plainIndex = c.getColumnIndex("old_path");
					do
					{
						mapPaths.put(c.getString(plainIndex), c.getString(cipherIndex));
					}
					while(c.moveToNext());
					c.close();
				}
				dbOriginal.close();
				insert(mapPaths);
				new File(dbPath).delete();
			}
		}
	}

	/**
	 * 以RelativePath存储
	 * 
	 * @param mapPaths
	 *            key是plainPath，value是cipherPath
	 */
	public void insert(HashMap<String, String> mapPaths)
	{
		if (mapPaths == null || mapPaths.isEmpty())
		{
			return;
		}
		Iterator<String> iterator = mapPaths.keySet().iterator();
		SQLiteDatabase db = ddb.openDB();
		db.beginTransaction();
		try
		{
			ContentValues values = new ContentValues();
			while(iterator.hasNext())
			{
				String plain = iterator.next();
				values.put(COL_PLAIN_PATH, Dirs.getRelativePath(plain));
				values.put(COL_CIPHER_PATH, Dirs.getRelativePath(mapPaths.get(plain)));
				db.insert(TAB_PATH, null, values);
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
	 * 以RelativePath存储
	 * 
	 * @param plainPath
	 * @param cipherPath
	 */
	public void insert(String plainPath, String cipherPath)
	{
		if (TextUtils.isEmpty(plainPath) || TextUtils.isEmpty(cipherPath))
		{
			return;
		}
		ContentValues values = new ContentValues();
		values.put(COL_PLAIN_PATH, Dirs.getRelativePath(plainPath));
		values.put(COL_CIPHER_PATH, Dirs.getRelativePath(cipherPath));
		ddb.openDB().insert(TAB_PATH, null, values);
		ddb.closeDB();
	}

	/**
	 * 这里是根据cipherPath删除的
	 * 
	 * @param cipherPaths
	 *            cipherPath的集合
	 */
	// 以RelativePath删除
	public void delete(Collection<String> cipherPaths)
	{
		if (cipherPaths == null || cipherPaths.isEmpty())
		{
			return;
		}
		Iterator<String> iterator = cipherPaths.iterator();
		SQLiteDatabase db = ddb.openDB();
		db.beginTransaction();
		try
		{
			while(iterator.hasNext())
			{
				db.delete(TAB_PATH, COL_CIPHER_PATH + "=?", new String[]
				{ Dirs.getRelativePath(iterator.next()) });
			}
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
			ddb.closeDB();
		}
	}

	// 以RelativePath删除
	public void delete(String cipherPath)
	{
		if (TextUtils.isEmpty(cipherPath))
		{
			return;
		}
		ddb.openDB().delete(TAB_PATH, COL_CIPHER_PATH + "=?", new String[]
		{ Dirs.getRelativePath(cipherPath) });
		ddb.closeDB();
	}

	// 以RelativePath为索引查找，将结果转换为AbsolutePath
	public String queryPlainPath(String cipherPath)
	{
		String plainPath = null;
		if (!TextUtils.isEmpty(cipherPath))
		{
			Cursor c = ddb.openDB().query(TAB_PATH, null, COL_CIPHER_PATH + "=?", new String[]
			{ Dirs.getRelativePath(cipherPath) }, null, null, null);
			if (c != null)
			{
				if (c.moveToFirst())
				{
					plainPath = Dirs.getAbsolutePath(c.getString(c.getColumnIndex(COL_PLAIN_PATH)));
				}
				c.close();
			}
			ddb.closeDB();	// 注意这里，cursor关闭之后才代表业务结束
		}

		// 没有记录或者原文件路径没有写权限了
		if (plainPath == null || !FileUtil.fileCanCreate(plainPath))
		{
			plainPath = Dirs.getDefaultMediaPath(cipherPath);
		}
		return plainPath;
	}

	/**
	 * @param cipherPaths
	 * @return key是ciperPath，value是plainPath
	 */
	// 将表中path转换为AbsolutePath比照cipherPaths
	public HashMap<String, String> queryPlainPaths(ArrayList<String> cipherPaths)
	{
		final HashMap<String, String> mapPaths = new HashMap<String, String>();
		if (!(cipherPaths == null || cipherPaths.isEmpty()))
		{
			Cursor c = ddb.openDB().query(TAB_PATH, null, null, null, null, null, null);
			if (c != null)
			{
				if (c.moveToFirst())
				{
					int cipherIndex = c.getColumnIndex(COL_CIPHER_PATH);
					int plainIndex = c.getColumnIndex(COL_PLAIN_PATH);
					String cipherPath, plainPath;
					do
					{
						cipherPath = Dirs.getAbsolutePath(c.getString(cipherIndex));
						int index = cipherPaths.indexOf(cipherPath);
						if (index != -1)
						{
							plainPath = Dirs.getAbsolutePath(c.getString(plainIndex));
							// 没有记录或者原文件路径没有写权限了
							if (!FileUtil.fileCanCreate(plainPath))
							{
								plainPath = Dirs.getDefaultMediaPath(cipherPath);
							}
							mapPaths.put(cipherPath, plainPath);
							cipherPaths.remove(index);	//移除，后面会用到
						}
					}
					while(c.moveToNext());
				}
				c.close();
			}
			ddb.closeDB();

			// 剩下的就是数据库中没有记录
			for (String path : cipherPaths)
			{
				mapPaths.put(path, Dirs.getDefaultMediaPath(path));
			}
		}
		return mapPaths;
	}
}
