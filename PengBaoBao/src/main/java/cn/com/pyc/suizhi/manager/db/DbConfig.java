package cn.com.pyc.suizhi.manager.db;

import android.text.TextUtils;

import org.xutils.DbManager;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.sqlite.SqlInfoBuilder;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;

public class DbConfig
{
	public static final String DB_NAME = "SZData.db";

	/**
	 * 默认配置
	 * 
	 * @return
	 */
	public static DbManager.DaoConfig daoConfig()
	{
		DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
				.setDbName(DB_NAME).setDbVersion(2).setAllowTransaction(true)
				.setDbOpenListener(new DbManager.DbOpenListener()
				{
					@Override
					public void onDbOpened(DbManager db)
					{
						// 开启WAL, 对写入加速提升巨大
						db.getDatabase().enableWriteAheadLogging();
					}
				}).setDbUpgradeListener(new DbManager.DbUpgradeListener()
				{
					@Override
					public void onUpgrade(DbManager db, int oldVersion,
										  int newVersion)
					{
						// TODO: ...
						// db.addColumn(...);
						// db.dropTable(...);
						// ...
						// or
						// db.dropDb();
					}
				});
		return daoConfig;
	}

	/**
	 * 是否存在表 clazz;不存在创建
	 * 
	 * @param dbManager
	 * @param clazz
	 */
	public static void createTableIfNotExist(DbManager dbManager, Class<?> clazz)
	{
		if (dbManager == null)
			throw new IllegalArgumentException("dbManager require init.");

		try
		{
			TableEntity<?> table = dbManager.getTable(clazz);
			if (!table.tableIsExist())
			{
				synchronized (table.getClass())
				{
					if (!table.tableIsExist())
					{
						SqlInfo sqlInfo = SqlInfoBuilder
								.buildCreateTableSqlInfo(table);
						dbManager.execNonQuery(sqlInfo);
						String execAfterTableCreated = table.getOnCreated();
						if (!TextUtils.isEmpty(execAfterTableCreated))
						{
							dbManager.execNonQuery(execAfterTableCreated);
						}
					}
				}
			}
		} catch (DbException e)
		{
			e.printStackTrace();
		}
	}
}
