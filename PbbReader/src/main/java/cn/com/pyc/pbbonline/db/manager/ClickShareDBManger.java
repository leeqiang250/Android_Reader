package cn.com.pyc.pbbonline.db.manager;

import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import com.sz.mobilesdk.manager.db.DbConfig;

import cn.com.pyc.pbbonline.bean.SharedClick;

public class ClickShareDBManger
{
	//记录存储点击状态，shareId为关联键
	private DbManager db;

	//	/**
	//	 * 默认配置
	//	 * 
	//	 * @param dbName
	//	 * @return
	//	 */
	//	private DbManager.DaoConfig daoConfig()
	//	{
	//		DbManager.DaoConfig daoConfig = new DbManager.DaoConfig().setDbName(DB_NAME)
	//				.setDbVersion(1).setDbOpenListener(new DbManager.DbOpenListener()
	//				{
	//					@Override
	//					public void onDbOpened(DbManager db)
	//					{
	//						// 开启WAL, 对写入加速提升巨大
	//						db.getDatabase().enableWriteAheadLogging();
	//					}
	//				}).setDbUpgradeListener(new DbManager.DbUpgradeListener()
	//				{
	//					@Override
	//					public void onUpgrade(DbManager db, int oldVersion, int newVersion)
	//					{
	//						// TODO: ...
	//						// db.addColumn(...);
	//						// db.dropTable(...);
	//						// ...
	//						// or
	//						// db.dropDb();
	//					}
	//				});
	//		return daoConfig;
	//	}

	/**
	 * @param ctx
	 * @return
	 */
	public static ClickShareDBManger Builder()
	{
		return new ClickShareDBManger();
	}

	private ClickShareDBManger()
	{
		db = x.getDb(DbConfig.daoConfig());
		DbConfig.createTableIfNotExist(db, SharedClick.class);
	}

	//保存点击状态
	public void saveClick(String shareId, boolean isClick)
	{
		SharedClick sc = findClickByShareId(shareId);
		if (sc == null)
		{
			sc = new SharedClick();
			sc.setShareId(shareId);
			sc.setClick(isClick);
			sc.setTime(System.currentTimeMillis());
			try
			{
				db.save(sc);
			}
			catch (DbException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 查询点击状态
	 * 
	 * @param shareId
	 * @return
	 */
	public SharedClick findClickByShareId(String shareId)
	{
		SharedClick sc = null;
		try
		{
			sc = db.selector(SharedClick.class).where("shareId", "=", shareId).findFirst();
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return sc;
	}

	public void deleteClickByShareId(String shareId)
	{
		try
		{
			db.delete(SharedClick.class, WhereBuilder.b("shareId", "=", shareId));
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
	}

}
