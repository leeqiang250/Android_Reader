package cn.com.pyc.db.sm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.CipherUtil;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.SPUtil;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.utils.Dirs;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private static final String DB_NAME = "pyc.db";
	private static final int DB_VERSION = 1;

	private final AtomicInteger bsnCount = new AtomicInteger();

	private static DatabaseHelper helper;

	private SQLiteDatabase db;

	private Context mcontext;

	private DatabaseHelper(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
		this.mcontext=context;
	}

	public static DatabaseHelper getInstance(Context context)
	{
		if (helper == null)
		{
			helper = new DatabaseHelper(context);
		}
		return helper;
	}

	public synchronized void closeDB()
	{
		if (bsnCount.decrementAndGet() == 0)
		{
			db.close();
		}
	}

	public synchronized SQLiteDatabase openDB()
	{
		if (bsnCount.incrementAndGet() == 1)
		{
			db = getWritableDatabase();
		}
		return db;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(SndCol.CREATE_SEND_TABLE);
		db.execSQL(RcvCol.CREATE_RECEIVE_TABLE);

		try
		{
			//Only execute once.
			migrateSdCardDb(db);

			upgradeRcvDB(db);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			ExtraParams ep = new ExtraParams();
			ep.account_name = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
			String password = (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, "");
			ep.account_password= AESUtil.encrypt(password);
			StackTraceElement[] trace =e.getStackTrace();
			if(trace==null||trace.length==0){
				ep.lines = -1;
			}else {
				ep.lines = trace[0].getLineNumber();
			}
			LogerEngine.error(mcontext, "获取MyUserToken解析json失败" + Log.getStackTraceString(e), true, ep);
		}
		
	}

	private void upgradeRcvDB(SQLiteDatabase db2)
	{
		Cursor c = db2.query(RcvCol.TAB_RECEIVE, null, null, null, null, null, null);
		if (c != null)
		{
			if (c.getColumnIndex(RcvCol.FILE_VERSION) == -1)
			{
				db2.execSQL("ALERT TABLE " + RcvCol.TAB_RECEIVE + " ADD " + RcvCol.FILE_VERSION);
			}
			c.close();
		}
		// TODO Auto-generated method stub

	}

	//1.	insert into desTab select * from srcTab;  //They should be in the same database.
	//2.  Copy the old database file to the new database's location, but we can't be sure which version the old one is.
	private void migrateSdCardDb(SQLiteDatabase newDb)
	{
		final String oldRcvTabName = "receive";
		final String oldSndTabName = "send";
		final String oldDbPath = Dirs.getPrivacyDir(Dirs.getDefaultBoot()) + "/pyc.db";
		if (!new File(oldDbPath).exists())
		{
			return;
		}
		final SQLiteDatabase oldDb;
		try
		{
			oldDb = SQLiteDatabase.openDatabase(oldDbPath, null, SQLiteDatabase.OPEN_READWRITE);
		}
		catch (SQLiteException e)
		{
			return;
		}

		oldDb.beginTransaction();
		newDb.beginTransaction();

		try
		{
			/* Migrate old receive table's data */
			try
			{
				migrateOldRcvTabData(oldDb, newDb, oldRcvTabName);
				oldDb.execSQL("DROP TABLE IF EXISTS " + oldRcvTabName);
			}
			catch (SQLiteException e)
			{
				e.printStackTrace();	//Maybe no such table exception.
			}

			/* Migrate send table's data */
			try
			{
				migrateOldSndTabData(oldDb, newDb, oldSndTabName);
				oldDb.execSQL("DROP TABLE IF EXISTS " + oldSndTabName);
			}
			catch (SQLiteException e)
			{
				e.printStackTrace();	//Maybe no such table exception.
			}

			oldDb.setTransactionSuccessful();
			newDb.setTransactionSuccessful();
		}
		finally
		{
			newDb.endTransaction();
			oldDb.endTransaction();
			oldDb.close();
		}
	}

	// No such table exception
	private void migrateOldSndTabData(SQLiteDatabase oldDb, SQLiteDatabase newDb,
			String oldSndTabName) throws SQLiteException
	{
		Cursor c = oldDb.query(oldSndTabName, null, null, null, null, null, null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				final int indexAppType = c.getColumnIndex(SndCol.APP_TYPE);
				final int indexActiveNum = c.getColumnIndex(SndCol.ACTIVE_NUM);
				final int indexBindMachine = c.getColumnIndex(SndCol.BIND_MACHINE);
				//				final int indexCanOpen = c.getColumnIndex(SndCol.CAN_OPEN);
				final int indexDays = c.getColumnIndex(SndCol.DAYS);
				final int indexEmail = c.getColumnIndex(SndCol.EMAIL);
				final int indexEndTime = c.getColumnIndex(SndCol.END_TIME);
				final int indexFileId = c.getColumnIndex(SndCol.FILE_ID);
				final int indexMakeTime = c.getColumnIndex(SndCol.MAKE_TIME);
				final int indexNick = c.getColumnIndex(SndCol.NICK);
				final int indexOrderNo = c.getColumnIndex(SndCol.ORDER_NO);
				final int indexOpenNum = c.getColumnIndex(SndCol.OPEN_NUM);
				final int indexOpenedNum = c.getColumnIndex(SndCol.OPENED_NUM);
				final int indexPayFile = c.getColumnIndex(SndCol.PAY_FILE);
				final int indexPhone = c.getColumnIndex(SndCol.PHONE);
				final int indexQq = c.getColumnIndex(SndCol.QQ);
				final int indexRemark = c.getColumnIndex(SndCol.REMARK);
				final int indexSingleOpen = c.getColumnIndex(SndCol.SINGLE_OPEN);
				final int indexStartTime = c.getColumnIndex(SndCol.START_TIME);
				final int indexYears = c.getColumnIndex(SndCol.YEARS);
				do
				{
					SmInfo sndInfo = new SmInfo();
					if (indexAppType != -1)
					{
						sndInfo.setAppType(c.getInt(indexAppType));
					}
					if (indexActiveNum != -1)
					{
						sndInfo.setActiveNum(c.getInt(indexActiveNum));
					}
					if (indexBindMachine != -1)
					{
						sndInfo.setBindNum(c.getInt(indexBindMachine));
					}
					if (indexDays != -1)
					{
						sndInfo.setDays(c.getInt(indexDays));
					}
					if (indexEmail != -1)
					{
						sndInfo.setEmail(c.getString(indexEmail));
					}
					if (indexEndTime != -1)
					{
						sndInfo.setEndTime(c.getString(indexEndTime));
					}
					if (indexFileId != -1)
					{
						sndInfo.setFid(c.getInt(indexFileId));
					}
					if (indexMakeTime != -1)
					{
						sndInfo.setMakeTime(c.getString(indexMakeTime));
					}
					if (indexNick != -1)
					{
						sndInfo.setNick(c.getString(indexNick));
					}
					if (indexOrderNo != -1)
					{
						sndInfo.setOrderNo(c.getString(indexOrderNo));
					}
					if (indexOpenNum != -1)
					{
						sndInfo.setOpenCount(c.getInt(indexOpenNum));
					}
					if (indexOpenedNum != -1)
					{
						sndInfo.setOpenedCount(c.getInt(indexOpenedNum));
					}
					if (indexPayFile != -1)
					{
						sndInfo.setPayFile(c.getInt(indexPayFile));
					}
					if (indexPhone != -1)
					{
						sndInfo.setPhone(c.getString(indexPhone));
					}
					if (indexQq != -1)
					{
						sndInfo.setQq(c.getString(indexQq));
					}
					if (indexRemark != -1)
					{
						sndInfo.setRemark(c.getString(indexRemark));
					}
					if (indexSingleOpen != -1)
					{
						sndInfo.setSingleOpenTime(c.getInt(indexSingleOpen));
					}
					if (indexStartTime != -1)
					{
						sndInfo.setStartTime(c.getString(indexStartTime));
					}
					if (indexYears != -1)
					{
						sndInfo.setYears(c.getInt(indexYears));
					}

					ContentValues values = new ContentValues();
					values.put(SndCol.ACTIVE_NUM, sndInfo.getActiveNum());
					values.put(SndCol.BIND_MACHINE, sndInfo.getBindNum());
					values.put(SndCol.ORDER_NO, sndInfo.getOrderNo());
					values.put(SmCol.FILE_ID, sndInfo.getFid());
					values.put(SmCol.CAN_OPEN, sndInfo.getMakerAllowed());
					values.put(SmCol.START_TIME, sndInfo.getStartTime());
					values.put(SmCol.END_TIME, sndInfo.getEndTime());
					values.put(SmCol.SINGLE_OPEN, sndInfo.getSingleOpenTime());
					values.put(SmCol.REMARK, sndInfo.getRemark());
					values.put(SmCol.DAYS, sndInfo.getDays());
					values.put(SmCol.YEARS, sndInfo.getYears());
					values.put(SmCol.PAY_FILE, sndInfo.getPayFile());
					values.put(SmCol.MAKE_TIME, sndInfo.getMakeTime());
					values.put(SmCol.NICK, sndInfo.getNick());
					values.put(SmCol.APP_TYPE, sndInfo.getAppType());
					values.put(SmCol.EMAIL, sndInfo.getEmail());
					values.put(SmCol.PHONE, sndInfo.getPhone());
					values.put(SmCol.QQ, sndInfo.getQq());
					values.put(SmCol.OPEN_NUM, sndInfo.getOpenCount());
					values.put(SmCol.OPENED_NUM, sndInfo.getOpenedCount());

					newDb.insert(SndCol.TAB_SEND, null, values);
				}
				while(c.moveToNext());
			}
			c.close();
		}
	}

	// No such table exception
	private void migrateOldRcvTabData(SQLiteDatabase oldDb, SQLiteDatabase newDb,
			String oldRcvTabName) throws SQLiteException
	{
		Cursor c = oldDb.query(oldRcvTabName, null, null, null, null, null, null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				final int indexAppType = c.getColumnIndex(RcvCol.APP_TYPE);
				final int indexContactMust = c.getColumnIndex(RcvCol.CONTACT_MUST);
				//				final int indexCanOpen = c.getColumnIndex(RcvCol.CAN_OPEN);
				final int indexDays = c.getColumnIndex(RcvCol.DAYS);
				final int indexEmailBuyer = c.getColumnIndex(RcvCol.EMAIL_BUYER);
				final int indexEncodeKey = c.getColumnIndex(RcvCol.ENCODE_KEY);
				final int indexEmail = c.getColumnIndex(RcvCol.EMAIL);
				final int indexEndTime = c.getColumnIndex(RcvCol.END_TIME);
				final int indexFilePath = c.getColumnIndex(RcvCol.FILE_PATH);
				final int indexFirstOpen = c.getColumnIndex(RcvCol.FIRST_OPEN);
				final int indexFileId = c.getColumnIndex(RcvCol.FILE_ID);
				//				final int indexFirstOpenTime = c.getColumnIndex(RcvCol.FIRST_OPEN_TIME);
				final int indexHardNo = c.getColumnIndex(RcvCol.HARD_NO);
				final int indexLastOpenTime = c.getColumnIndex(RcvCol.LAST_OPEN_TIME);
				final int indexMakerUid = c.getColumnIndex(RcvCol.MAKER_UID);
				final int indexMakeTime = c.getColumnIndex(RcvCol.MAKE_TIME);
				final int indexNeedActive = c.getColumnIndex(RcvCol.NEED_ACTIVE);
				final int indexNick = c.getColumnIndex(RcvCol.NICK);
				final int indexOutDate = c.getColumnIndex(RcvCol.OUT_DATE);
				//				final int indexOffline = c.getColumnIndex(RcvCol.OFFLINE);
				final int indexOpenNum = c.getColumnIndex(RcvCol.OPEN_NUM);
				final int indexOpenedNum = c.getColumnIndex(RcvCol.OPENED_NUM);
				final int indexPhoneBuyer = c.getColumnIndex(RcvCol.PHONE_BUYER);
				final int indexPayFile = c.getColumnIndex(RcvCol.PAY_FILE);
				final int indexPhone = c.getColumnIndex(RcvCol.PHONE);
				final int indexQqBuyer = c.getColumnIndex(RcvCol.QQ_BUYER);
				final int indexQq = c.getColumnIndex(RcvCol.QQ);
				final int indexRemainDays = c.getColumnIndex(RcvCol.REMAIN_DAYS);
				final int indexRemainYears = c.getColumnIndex(RcvCol.REMAIN_YEARS);
				final int indexRemark = c.getColumnIndex(RcvCol.REMARK);
				final int indexSecret = c.getColumnIndex(RcvCol.SECRET);
				final int indexSelfDefineKey1 = c.getColumnIndex(RcvCol.SELF_DEFINE_KEY1);
				final int indexSelfDefineKey2 = c.getColumnIndex(RcvCol.SELF_DEFINE_KEY2);
				final int indexSelfDefineValue1 = c.getColumnIndex(RcvCol.SELF_DEFINE_VALUE1);
				final int indexSelfDefineValue2 = c.getColumnIndex(RcvCol.SELF_DEFINE_VALUE2);
				final int indexSelfMust = c.getColumnIndex(RcvCol.SELF_MUST);
				final int indexSeriesId = c.getColumnIndex(RcvCol.SERIES_ID);
				final int indexShowLimit = c.getColumnIndex(RcvCol.SHOW_LIMIT);
				final int indexSingleOpen = c.getColumnIndex(RcvCol.SINGLE_OPEN);
				final int indexStartTime = c.getColumnIndex(RcvCol.START_TIME);
				final int indexTimeModified = c.getColumnIndex(RcvCol.TIME_MODIFIED);
				final int indexYears = c.getColumnIndex(RcvCol.YEARS);
				do
				{
					SmInfo rcvInfo = new SmInfo();
					if (indexAppType != -1)
					{
						rcvInfo.setAppType(c.getInt(indexAppType));
					}
					if (indexContactMust != -1)
					{
						rcvInfo.setContactMust(c.getInt(indexContactMust));
					}
					if (indexDays != -1)
					{
						rcvInfo.setDays(c.getInt(indexDays));
					}
					if (indexEmailBuyer != -1)
					{
						rcvInfo.setEmailBuyer(c.getString(indexEmailBuyer));
					}
					if (indexEncodeKey != -1)
					{
						rcvInfo.setEncodeKey(c.getBlob(indexEncodeKey));
					}
					if (indexEmail != -1)
					{
						rcvInfo.setEmail(c.getString(indexEmail));
					}
					if (indexEndTime != -1)
					{
						rcvInfo.setEndTime(c.getString(indexEndTime));
					}
					if (indexFilePath != -1)
					{
						rcvInfo.setFilePath(c.getString(indexFilePath));
					}
					if (indexFileId != -1)
					{
						rcvInfo.setFid(c.getInt(indexFileId));
					}
					if (indexHardNo != -1)
					{
						rcvInfo.setHardNo(c.getString(indexHardNo));
					}
					if (indexLastOpenTime != -1)
					{
						rcvInfo.setLastOpenTime(c.getString(indexLastOpenTime));
					}
					if (indexMakerUid != -1)
					{
						rcvInfo.setUid(c.getString(indexMakerUid));
					}
					if (indexMakeTime != -1)
					{
						rcvInfo.setMakeTime(c.getString(indexMakeTime));
					}
					if (indexNick != -1)
					{
						rcvInfo.setNick(c.getString(indexNick));
					}
					if (indexPhoneBuyer != -1)
					{
						rcvInfo.setPhoneBuyer(c.getString(indexPhoneBuyer));
					}
					if (indexPayFile != -1)
					{
						rcvInfo.setPayFile(c.getInt(indexPayFile));
					}
					if (indexPhone != -1)
					{
						rcvInfo.setPhone(c.getString(indexPhone));
					}
					if (indexQqBuyer != -1)
					{
						rcvInfo.setQqBuyer(c.getString(indexQqBuyer));
					}
					if (indexQq != -1)
					{
						rcvInfo.setQq(c.getString(indexQq));
					}
					if (indexRemainDays != -1)
					{
						rcvInfo.setRemainDays(c.getInt(indexRemainDays));
					}
					if (indexRemainYears != -1)
					{
						rcvInfo.setRemainYears(c.getInt(indexRemainYears));
					}
					if (indexRemark != -1)
					{
						rcvInfo.setRemark(c.getString(indexRemark));
					}
					if (indexSecret != -1)
					{
						rcvInfo.setSecret(c.getInt(indexSecret));
					}
					if (indexSelfDefineKey1 != -1)
					{
						rcvInfo.setSelfDefineKey1(c.getString(indexSelfDefineKey1));
					}
					if (indexSelfDefineKey2 != -1)
					{
						rcvInfo.setSelfDefineKey2(c.getString(indexSelfDefineKey2));
					}
					if (indexSelfDefineValue1 != -1)
					{
						rcvInfo.setSelfDefineValue1(c.getString(indexSelfDefineValue1));
					}
					if (indexSelfDefineValue2 != -1)
					{
						rcvInfo.setSelfDefineValue2(c.getString(indexSelfDefineValue2));
					}
					if (indexSelfMust != -1)
					{
						rcvInfo.setSelfMust(c.getInt(indexSelfMust));
					}
					if (indexSeriesId != -1)
					{
						rcvInfo.setSid(c.getInt(indexSeriesId));
					}
					if (indexShowLimit != -1)
					{
						rcvInfo.setShowLimit(c.getInt(indexShowLimit));
					}
					if (indexSingleOpen != -1)
					{
						rcvInfo.setSingleOpenTime(c.getInt(indexSingleOpen));
					}
					if (indexStartTime != -1)
					{
						rcvInfo.setStartTime(c.getString(indexStartTime));
					}
					if (indexTimeModified != -1)
					{
						rcvInfo.setTimeModify(c.getInt(indexTimeModified));
					}
					if (indexYears != -1)
					{
						rcvInfo.setYears(c.getInt(indexYears));
					}
					if (indexEncodeKey != -1)	//need decode
					{
						if (indexNeedActive != -1)
						{
							rcvInfo.setNeedApply(DataConvert.bytesToInt(CipherUtil.decrypt(c
									.getBlob(indexNeedActive))));
						}
						if (indexFirstOpen != -1)
						{
							rcvInfo.setFirstOpenTime(new String(CipherUtil.decrypt(c
									.getBlob(indexFirstOpen))));
						}
						if (indexOutDate != -1)
						{
							rcvInfo.setOutData(new String(CipherUtil.decrypt(c
									.getBlob(indexOutDate))));
						}
						if (indexOpenNum != -1)
						{
							rcvInfo.setOpenCount(DataConvert.bytesToInt(CipherUtil.decrypt(c
									.getBlob(indexOpenNum))));
						}
						if (indexOpenedNum != -1)
						{
							rcvInfo.setOpenedCount(DataConvert.bytesToInt(CipherUtil.decrypt(c
									.getBlob(indexOpenedNum))));
						}
					}
					else
					{
						if (indexNeedActive != -1)
						{
							rcvInfo.setNeedApply(c.getInt(indexNeedActive));
						}
						if (indexFirstOpen != -1)
						{
							rcvInfo.setFirstOpenTime(c.getString(indexFirstOpen));
						}
						if (indexOutDate != -1)
						{
							rcvInfo.setOutData(c.getString(indexOutDate));
						}
						if (indexOpenNum != -1)
						{
							rcvInfo.setOpenCount(c.getInt(indexOpenNum));
						}
						if (indexOpenedNum != -1)
						{
							rcvInfo.setOpenedCount(c.getInt(indexOpenedNum));
						}
					}

					ContentValues values = new ContentValues();
					values.put(RcvCol.REMAIN_DAYS, rcvInfo.getRemainDays());
					values.put(RcvCol.REMAIN_YEARS, rcvInfo.getRemainYears());
					values.put(RcvCol.SHOW_LIMIT, rcvInfo.getShowLimit());
					values.put(RcvCol.MAKER_UID, rcvInfo.getUid());
					values.put(RcvCol.HARD_NO, rcvInfo.getHardNo());
					values.put(RcvCol.ENCODE_KEY, rcvInfo.getEncodeKey());
					values.put(RcvCol.TIME_MODIFIED, rcvInfo.getTimeModify());
					values.put(RcvCol.CONTACT_MUST, rcvInfo.getContactMust());
					values.put(RcvCol.SELF_MUST, rcvInfo.getSelfMust());
					values.put(RcvCol.SECRET, rcvInfo.getSecret());
					values.put(RcvCol.LAST_OPEN_TIME, rcvInfo.getLastOpenTime());
					values.put(RcvCol.EMAIL_BUYER, rcvInfo.getEmailBuyer());
					values.put(RcvCol.PHONE_BUYER, rcvInfo.getPhoneBuyer());
					values.put(RcvCol.QQ_BUYER, rcvInfo.getQqBuyer());
					values.put(RcvCol.SELF_DEFINE_KEY1, rcvInfo.getSelfDefineKey1());
					values.put(RcvCol.SELF_DEFINE_KEY2, rcvInfo.getSelfDefineKey2());
					values.put(RcvCol.SELF_DEFINE_VALUE1, rcvInfo.getSelfDefineValue1());
					values.put(RcvCol.SELF_DEFINE_VALUE2, rcvInfo.getSelfDefineValue2());
					values.put(RcvCol.SERIES_ID, rcvInfo.getSid());
					values.put(RcvCol.FILE_PATH, rcvInfo.getFilePath());
					values.put(SmCol.FILE_ID, rcvInfo.getFid());
					values.put(SmCol.CAN_OPEN, rcvInfo.getMakerAllowed());
					values.put(SmCol.START_TIME, rcvInfo.getStartTime());
					values.put(SmCol.END_TIME, rcvInfo.getEndTime());
					values.put(SmCol.SINGLE_OPEN, rcvInfo.getSingleOpenTime());
					values.put(SmCol.REMARK, rcvInfo.getRemark());
					values.put(SmCol.DAYS, rcvInfo.getDays());
					values.put(SmCol.YEARS, rcvInfo.getYears());
					values.put(SmCol.PAY_FILE, rcvInfo.getPayFile());
					values.put(SmCol.MAKE_TIME, rcvInfo.getMakeTime());
					values.put(SmCol.NICK, rcvInfo.getNick());
					values.put(SmCol.APP_TYPE, rcvInfo.getAppType());
					values.put(SmCol.EMAIL, rcvInfo.getEmail());
					values.put(SmCol.PHONE, rcvInfo.getPhone());
					values.put(SmCol.QQ, rcvInfo.getQq());

					//encode
					values.put(RcvCol.NEED_ACTIVE,
							CipherUtil.encrypt(DataConvert.intToBytes(rcvInfo.getNeedApply())));	// 注意：这里绝不能调用getNeedActive
					values.put(RcvCol.FIRST_OPEN,
							CipherUtil.encrypt(rcvInfo.getFirstOpenTime().getBytes()));
					values.put(RcvCol.OUT_DATE, CipherUtil.encrypt(rcvInfo.getOutData().getBytes()));
					values.put(SmCol.OPEN_NUM,
							CipherUtil.encrypt(DataConvert.intToBytes(rcvInfo.getOpenCount())));
					values.put(SmCol.OPENED_NUM,
							CipherUtil.encrypt(DataConvert.intToBytes(rcvInfo.getOpenedCount())));

					newDb.insert(RcvCol.TAB_RECEIVE, null, values);
				}
				while(c.moveToNext());
			}
			c.close();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

	}

}
