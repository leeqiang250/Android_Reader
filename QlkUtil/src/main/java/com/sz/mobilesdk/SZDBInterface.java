package com.sz.mobilesdk;

import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.database.DBHelper;
import com.sz.mobilesdk.util.SZDbUtil;
import com.sz.mobilesdk.util.SZLog;

/**
 * 数据库操作处理
 * 
 * @author hudq
 * 
 */
public final class SZDBInterface
{

	/**
	 * 创建数据库时调用 <br/>
	 * 建议放在后台服务中进行。<br/>
	 * dbName是以userName+"_"+id命名的
	 */
	public static boolean createDB()
	{
		SZDbUtil szDbUtil = new SZDbUtil(SZApplication.getInstance());
		return szDbUtil.createDBTable();
	}

	/**
	 * 销毁数据库实例值db，将db关闭并置为null <br/>
	 * 
	 * 注销、切换账号时，可能会需要重新创建数据库，需要销毁原来db引用值。
	 */
	public static void destoryDBHelper()
	{
		SZDbUtil.destoryDBHelper();
	}

	/**
	 * 清除所有表中的数据
	 */
	public static void deleteAllTableData()
	{
		SZDbUtil.deleteTableData();
	}

	/**
	 * 删除所有表
	 */
	public static void dropAllTable()
	{
		SZDbUtil.dropTable();
	}

	/**
	 * 删除数据库db <br/>
	 * Delete an existing private SQLiteDatabase associated with this Context's
	 * application package.
	 * 
	 * @param DbName
	 */
	public static void dropDatabase(String dBName)
	{
		dBName = dBName + DBHelper.DB_LABLE;
		boolean isDrop = SZDbUtil.deleteDatabase(SZApplication.getInstance(),
				dBName);
		SZLog.v("sz", "dropDb " + dBName + ": " + isDrop);
	}
}
