package com.sz.mobilesdk.manager.db;

import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import com.sz.mobilesdk.models.DownData2;
import com.sz.mobilesdk.util.SZLog;

/**
 * 下载文件数据管理,断点下载: DownData2数据表
 * 
 */
public class DownData2DBManager
{
	private DbManager dbManager;

	public static DownData2DBManager Builder()
	{
		return new DownData2DBManager();
	}

	private DownData2DBManager()
	{
		dbManager = x.getDb(DbConfig.daoConfig());
		DbConfig.createTableIfNotExist(dbManager, DownData2.class);
	}

	/**
	 * 删除所有记录
	 */
	public void deleteAll()
	{
		try
		{
			if (dbManager != null)
			{
				dbManager.delete(DownData2.class);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 保存或更新记录
	 * 
	 * @param data2
	 */
	public void saveOrUpdate(DownData2 data2)
	{
		try
		{
			// dbManager.replace(data2); // 没有就插入，有就更新
			dbManager.saveOrUpdate(data2);
			SZLog.i("dddbm", "save or update download data.");
		} catch (DbException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 根据fileId查询记录
	 * 
	 * @param fileId
	 * @return
	 */
	public DownData2 findByFileId(String fileId)
	{
		DownData2 data2 = null;
		try
		{
			data2 = dbManager.selector(DownData2.class)
					.where(WhereBuilder.b("fileId", "=", fileId)).findFirst();
		} catch (DbException e)
		{
			e.printStackTrace();
		}
		return data2;
	}

	/**
	 * 通过fileId删除记录
	 * 
	 * @param fileId
	 * @return
	 */
	public int deleteByFileId(String fileId)
	{
		int result = -1;
		try
		{
			result = dbManager.delete(DownData2.class,
					WhereBuilder.b("fileId", "=", fileId));
		} catch (DbException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 删除整个文件夹下的文件对应的记录
	 * 
	 * @param folderId
	 * @return
	 */
	public int deleteByFolderId(String folderId)
	{
		int result = -1;
		try
		{
			result = dbManager.delete(DownData2.class,
					WhereBuilder.b("myProId", "=", folderId));
		} catch (DbException e)
		{
			e.printStackTrace();
		}
		return result;
	}

}
