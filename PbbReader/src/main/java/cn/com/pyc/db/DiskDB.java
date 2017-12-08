package cn.com.pyc.db;

import cn.com.pyc.utils.Dirs;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import android.database.sqlite.SQLiteDatabase;

/**
 * 鹏保宝只有一个数据库pyc.db，所有表都放在这里面
 * 单一表的多线程访问或者多张表的同时访问，都可能造成数据库异常
 * 因此创建此类，采用业务计数的方式来管理数据库的开关操作，避免错误发生
 * 一定要记住openDB的调用次数一定不能超过closeDB
 * （本来计数是单独一个方法的bsnCountXXX，为了方便就放在closeDB和openDB中了，
 * 如果觉着容易出错，可以再改回来）
 * <p>
 * 业务升级则委托给各个表自行管理
 */
public final class DiskDB
{
	private static DiskDB ddb;

	public static synchronized DiskDB getInstance()
	{
		if (ddb == null)
		{
			ddb = new DiskDB();
		}

		return ddb;
	}

	/**
	 * 和closeDB成对出现，次数不能多于closeDB
	 * 
	 * @return
	 */
	public synchronized SQLiteDatabase openDB()
	{
		if (bsnCount.incrementAndGet() == 1)
		{
			db = SQLiteDatabase.openOrCreateDatabase(DB_PATH, null);
		}
		if (db.isReadOnly())
		{
			//错误日志中反应有时候该db是readOnly的
			new File(DB_PATH).delete();
			db = SQLiteDatabase.openOrCreateDatabase(DB_PATH, null);
		}
		return db;
	}

	//	public synchronized int getVersion()
	//	{
	//		return db.getVersion();
	//	}
	//
	//	public synchronized void setVersion(int version)
	//	{
	//		if (version > 0)
	//		{
	//			db.setVersion(version);
	//		}
	//	}

	/**
	 * 和openDB成对出现，但次数可以多于openDB
	 */
	public synchronized void closeDB()
	{
		if (bsnCount.decrementAndGet() == 0)
		{
			db.close();
		}
	}

	private DiskDB()
	{
	}

	/*-************************************
	 * 数据库操作
	 *************************************/

	public static final String DB_PATH = Dirs.getPrivacyDir(Dirs.getDefaultBoot()) + "/pyc.db";

	private SQLiteDatabase db;

	/*-
	 * 此数据库中有三张表，分属两个类PathDao和SmDao，
	 * 使用单例加synchronized也不能避免同步问题，故以业务数量来解决
	 * 而且这样要比synchronized高效
	 * 
	 * 当其大于1时表示有同步出现
	 * 
	 * bsnCount管辖范围：PathDao、smDao以及将来可能加入pyc.db的表
	 */
	private final AtomicInteger bsnCount = new AtomicInteger();	// 当前数据库业务数量，为0时方可执行close操作

	@Override
	protected void finalize() throws Throwable
	{
		closeDB();
		super.finalize();
	}

}
