package cn.com.pyc.pbbonline.db;

import java.util.ArrayList;
import java.util.List;

import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.sqlite.SqlInfoBuilder;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;

import android.text.TextUtils;
import cn.com.pyc.pbbonline.common.ShareMode;

public class SharedDaoImpl implements ISharedDao
{
	private static final String DB_NAME = "shared.sqlite";
	private DbManager dbManager;

	/**
	 * 默认配置
	 * 
	 * @param dbName
	 * @return
	 */
	private DbManager.DaoConfig daoConfig()
	{
		DbManager.DaoConfig daoConfig = new DbManager.DaoConfig().setDbName(DB_NAME)
				.setDbVersion(1).setDbOpenListener(new DbManager.DbOpenListener()
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
					public void onUpgrade(DbManager db, int oldVersion, int newVersion)
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

	public SharedDaoImpl()
	{
		dbManager = x.getDb(daoConfig());
		createTableIfNotExist();
	}

	private void createTableIfNotExist()
	{
		try
		{
			TableEntity<Shared> table = dbManager.getTable(Shared.class);
			if (!table.tableIsExist())
			{
				synchronized (table.getClass())
				{
					if (!table.tableIsExist())
					{
						SqlInfo sqlInfo = SqlInfoBuilder.buildCreateTableSqlInfo(table);
						dbManager.execNonQuery(sqlInfo);
						String execAfterTableCreated = table.getOnCreated();
						if (!TextUtils.isEmpty(execAfterTableCreated))
						{
							dbManager.execNonQuery(execAfterTableCreated);
						}
					}
				}
			}
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean saveSharedAll(List<Shared> shareds)
	{
		boolean flag = false;
		try
		{
			dbManager.save(shareds);
			flag = true;
		}
		catch (DbException e)
		{
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public boolean saveShared(Shared shared)
	{
		boolean flag = false;
		try
		{
			dbManager.save(shared);
			flag = true;
		}
		catch (DbException e)
		{
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	@Override
	public boolean deleteByShareId(String shareId)
	{
		boolean flag = true;
		try
		{
			dbManager.delete(Shared.class, WhereBuilder.b("shareId", "=", shareId));
		}
		catch (DbException e)
		{
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	@Override
	public boolean deleteByAccount(String accountName)
	{
		boolean flag = true;
		try
		{
			dbManager.delete(Shared.class, WhereBuilder.b("accountName", "=", accountName));
		}
		catch (DbException e)
		{
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	@Override
	public boolean updateShared(Shared shared)
	{
		try
		{
			//db.update(shared, WhereBuilder.b("shareId", "=", shared.getShareId()), "isUpdate",
			//		"time", "isDelete");
			dbManager.update(shared, "isUpdate", "time", "isDelete");
			return true;
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<Shared> findAll()
	{
		List<Shared> list = new ArrayList<Shared>();
		try
		{
			list = dbManager.selector(Shared.class).orderBy("time", true).findAll();
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public Shared findByShareId(String shareId)
	{
		Shared o = null;
		try
		{
			o = dbManager.selector(Shared.class).where(WhereBuilder.b("shareId", "=", shareId))
					.findFirst();
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return o;
	}

	@Override
	public List<Shared> findByAccount(String accountName)
	{
		List<Shared> shareds = new ArrayList<Shared>();
		try
		{
			shareds = dbManager
					.selector(Shared.class)
					.where(WhereBuilder.b("accountName", "=", accountName).and("isDelete", "=",
							false)).orderBy("time", true).findAll();
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return shareds;
	}

	@Override
	public boolean isExistData()
	{
		Shared o = null;
		try
		{
			o = dbManager.findFirst(Shared.class);
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return o != null;
	}

	@Override
	public boolean deleteAll()
	{
		boolean flag = false;
		try
		{
			dbManager.delete(Shared.class);
			flag = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public List<Shared> findByDevice()
	{
		List<Shared> shareds = new ArrayList<Shared>();
		try
		{
			shareds = dbManager
					.selector(Shared.class)
					.where(WhereBuilder.b("shareMode", "=", ShareMode.SHAREDEVICE).and("isDelete",
							"=", false)).orderBy("time", true).findAll();
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return shareds;
	}

	@Override
	public boolean modifyNewShared(Shared shared)
	{
		try
		{
			dbManager.update(shared, "whetherNew", "isUpdate");
			return true;
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean updateRevokeShared(Shared shared)
	{
		try
		{
			dbManager.update(shared, "isRevoke", "time");
			return true;
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<Shared> findByDelete(boolean isDelete)
	{
		List<Shared> shareds = new ArrayList<Shared>();
		try
		{
			shareds = dbManager.selector(Shared.class).where(WhereBuilder.b("isDelete", "=", true))
					.orderBy("time", true).findAll();
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return shareds;
	}

	@Override
	public boolean updateDeleteFlag(Shared shared)
	{
		try
		{
			dbManager.update(shared, "isDelete");
			return true;
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean deleteUserByFlag(boolean isDelete, boolean isRevoke)
	{
		boolean flag = true;
		try
		{
			dbManager.delete(
					Shared.class,
					WhereBuilder.b("isDelete", "=", isDelete).and("isRevoke", "=", isRevoke)
							.and("shareMode", "!=", ShareMode.SHAREDEVICE));
		}
		catch (DbException e)
		{
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

}
