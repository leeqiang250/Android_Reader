package cn.com.pyc.pbbonline.db.manager;

import java.io.File;

import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.sqlite.SqlInfoBuilder;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;

import android.text.TextUtils;
import cn.com.pyc.pbbonline.bean.ClickIndex;

import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SZLog;

/**
 * 保存专辑中文件的点击索引 <br/>
 * 
 * @author qd
 */
@Deprecated
public class ClickIndexDBManager
{

	private String TAG = ClickIndexDBManager.class.getSimpleName();
	private static final String DB_NAME = "configs.db";

	private DbManager dbManager;

	/**
	 * 默认配置
	 * 
	 * @param dbName
	 * @return
	 */
	private DbManager.DaoConfig daoConfig()
	{
		File file = new File(PathUtil.getSZSDRootPath() + "dbDir/");
		if (file != null && !file.exists())
		{
			file.mkdirs();
		}
		DbManager.DaoConfig daoConfig = new DbManager.DaoConfig().setDbName(DB_NAME).setDbDir(file)
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

	public static ClickIndexDBManager Builder()
	{
		return new ClickIndexDBManager();
	}

	private ClickIndexDBManager()
	{
		dbManager = x.getDb(daoConfig());
		createTableIfNotExist();
	}
	
	private void createTableIfNotExist()
	{
		try
		{
			TableEntity<ClickIndex> table = dbManager.getTable(ClickIndex.class);
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

	/**
	 * 删除SaveIndex表
	 */
	public void dropTable()
	{
		try
		{
			if (dbManager != null)
			{
				SZLog.e(TAG, "dropTable: ClickIndex");
				dbManager.dropTable(ClickIndex.class);
			}
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 删除数据库
	 */
	public void dropDb()
	{
		try
		{
			SZLog.e(TAG, "dropDb");
			if (dbManager != null)
			{
				dbManager.close();
				dbManager.dropDb();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
				SZLog.d(TAG, "deleteAll: ClickIndex");
				dbManager.delete(ClickIndex.class);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 保存数据实体对象
	 * 
	 * @param index
	 * @param myProId
	 * @param contentId
	 * @param fileType
	 *            文件类型VIDEO,PDF，MUSIC
	 * @return
	 */
	public boolean saveDb(int index, String myProId, String contentId, String fileType)
	{
		if(dbManager == null) return false;
		boolean flag = true;
		try
		{
			// 查询
			ClickIndex so = findByMyProIdAndFileType(myProId, fileType);
			if (so == null)
			{
				so = new ClickIndex();
				so.setPositonIndex(String.valueOf(index));
				so.setMyProId(myProId);
				so.setContentId(contentId);
				so.setTime(System.currentTimeMillis());
				so.setFileType(fileType);
				dbManager.save(so);
				SZLog.d(TAG, "save click index");
			}
			else
			{
				// 更新数据
				so.setPositonIndex(String.valueOf(index));
				so.setContentId(contentId);
				so.setFileType(fileType);
				so.setTime(System.currentTimeMillis());
				dbManager.update(so, "positonIndex", "contentId", "fileType", "time");
				SZLog.d(TAG, "update click index");
			}
		}
		catch (DbException e)
		{
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 根据myProId删除一条数据
	 * 
	 * @param myProId
	 * @return
	 */
	public boolean deleteByMyProId(String myProId)
	{
		if (dbManager == null)
			return false;
		boolean flag = true;
		try
		{
			dbManager.delete(ClickIndex.class, WhereBuilder.b("myProId", "=", myProId));
		}
		catch (DbException e)
		{
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 根据myProId,fileType查询<br\>
	 * 返回值判断是否为null
	 * 
	 * @param myProId
	 * @param fileType
	 * @return
	 */
	private ClickIndex findByMyProIdAndFileType(String myProId, String fileType)
	{
		if (dbManager == null)
			return null;
		ClickIndex o = null;
		try
		{
			o = dbManager.selector(ClickIndex.class).where(WhereBuilder.b("myProId", "=", myProId))
					.and(WhereBuilder.b("fileType", "=", fileType)).findFirst();
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * 根据myProId查询获取当前index(返回最后保存的index值。)
	 * 
	 * @param myProId
	 * @return 返回最后保存的index值
	 */
	public int findIndexByMyProId(String myProId)
	{
		if (dbManager == null)
			return -1;
		ClickIndex o = null;
		try
		{
			o = dbManager.selector(ClickIndex.class).where(WhereBuilder.b("myProId", "=", myProId))
					.orderBy("time", true).findFirst();
		}
		catch (DbException e)
		{
			e.printStackTrace();
		}
		return o != null ? ConvertToUtil.toInt(o.getPositonIndex()) : -1;
	}

}
