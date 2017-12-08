package cn.com.pyc.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.qlk.util.tool.Util.CipherUtil;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.SPUtil;

import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.utils.Constant;

/*-
 * 为了保证与用户相关的操作所用的userInfo都是同一个，信息一致
 * 故将该类设计成单例的，globUserInfo在程序的生命周期内也只有一个
 */
public class UserDao extends SQLiteOpenHelper
{
	private final static String DB_NAME = "pyc_safe.db";

	private final static String TAB_USER = "user";

	/*-
	 * 表升级的历史版本
	 */
	private static class DBVersion
	{
		// public static final int VERSION_ORIGINAL = 1;
		public static final int VERSION_NICK = 2; // 第2版加入昵称
		public static final int VERSION_BLOB_UID = 3; // 第1版数据库uid是以text存储的
		public static final int VERSION_QQ = 4; // 加入QQ昵称
		public static final int VERSION_BIND = 5; // 加入email、phone和qq是否绑定以及money
		public static final int VERSION_SECRET_PSD = 6;	// 密码加密处理
		public static final int DB_VERSION = VERSION_SECRET_PSD;
	}

	private Context context;
	private int id;
	private static UserDao dao;
	private UserInfo userInfo;

	public static UserDao getDB(Context context)
	{
		if (dao == null)
		{
			dao = new UserDao(context);
		}
		return dao;
	}

	private UserDao(Context context)
	{
		super(context, DB_NAME, null, DBVersion.DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		init(db, null);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		UserInfo info = getInfo(db, oldVersion);
		String sql = "DROP TABLE " + TAB_USER;
		db.execSQL(sql);
		init(db, info);
	}

	/*-
	 * userInfo 为空则表示新建表，否则表示升级表
	 */
	private void init(SQLiteDatabase db, UserInfo userInfo)
	{
		// 初始化表
		String sql = "CREATE TABLE IF NOT EXISTS  " + TAB_USER
				+ "(_ID INTEGER PRIMARY KEY AUTOINCREMENT," //
				+ MyDBColumes.NICK + " TEXT,"//
				+ MyDBColumes.NAME + " TEXT,"//
				+ MyDBColumes.PSD + " BLOB," //
				+ MyDBColumes.UID + " BLOB," //
				+ MyDBColumes.PHONE + " TEXT," //
				+ MyDBColumes.EMAIL + " TEXT," //
				+ MyDBColumes.QQ_NICK + " TEXT,"//
				+ MyDBColumes.MONEY + " TEXT,"//
				+ MyDBColumes.EMAIL_BIND + " INTEGER," //
				+ MyDBColumes.PHONE_BIND + " INTEGER,"//
				+ MyDBColumes.QQ_BIND + " INTEGER)";
		db.execSQL(sql);

		if (userInfo == null)
		{
			userInfo = new UserInfo();
		}

		getBindedValue(userInfo);
		db.insert(TAB_USER, null, wrapContentValues(userInfo));
	}

	// 注意，if(true)是必要的（value是从sp中取得的，当sp中没有对应key值时它会返回默认值false，而这个值是没有意义的。所以不做判断）
	private void getBindedValue(UserInfo userInfo)
	{
		// 以前bind是以sp存储的，现在移到数据库中
		SharedPreferences sp = PbbSP.getGSP(context).getSharedPreferences();
		// 第二迭代改为int值，以前都是以各自的Boolean值存储
		if (sp.getBoolean(PbbSP.SP_EMAIL_BINDED, false))
		{
			// 以前业务suc==8表示邮箱绑定，下同
			userInfo.setEmailBinded(Constant.C_TRUE);
		}
		if (sp.getBoolean(PbbSP.SP_PHONE_BINDED, false))
		{
			userInfo.setPhoneBinded(Constant.C_TRUE);
		}
		userInfo.setBindedValue(sp.getInt(PbbSP.SP_BINDED_SUCCESS, 0));
		// 清空sp
		SharedPreferences.Editor editor = sp.edit();
		editor.remove(PbbSP.SP_EMAIL_BINDED);
		editor.remove(PbbSP.SP_PHONE_BINDED);
		editor.remove(PbbSP.SP_BINDED_SUCCESS);
		editor.commit();
	}

	/*-
	 * 表升级策略：出原表中所有数据---删除原表---建立新表---恢复原表数据
	 */
	private UserInfo getInfo(SQLiteDatabase db, int oldVersion)
	{
		UserInfo info = new UserInfo();
		Cursor c = db.query(TAB_USER, null, null, null, null, null, null);
		if (c != null && c.moveToFirst())
		{
			do
			{
				id = c.getInt(0);
				info.setUserName(c.getString(c.getColumnIndex(MyDBColumes.NAME)));
				info.setPhone(c.getString(c.getColumnIndex(MyDBColumes.PHONE)));
				info.setEmail(c.getString(c.getColumnIndex(MyDBColumes.EMAIL)));
				// 版本2改动
				if (oldVersion >= DBVersion.VERSION_NICK)
				{
					info.setNick(c.getString(c.getColumnIndex(MyDBColumes.NICK)));
				}
				// 版本3改动
				if (oldVersion >= DBVersion.VERSION_BLOB_UID)
				{
					info.setUid(c.getBlob(c.getColumnIndex(MyDBColumes.UID)));
				}
				else
				{
					info.setUid(c.getString(c.getColumnIndex(MyDBColumes.UID)).getBytes()); // 此时uid是以text存储的
				}
				// 版本4改动
				if (oldVersion >= DBVersion.VERSION_QQ)
				{
					info.setQqNick(c.getString(c.getColumnIndex(MyDBColumes.QQ_NICK)));
				}

				// 版本5改动
				if (oldVersion >= DBVersion.VERSION_BIND)
				{
					// sqlite数据库没有布尔型，它都是以0表示false，1表示true的
					info.setMoney(c.getString(c.getColumnIndex(MyDBColumes.MONEY)));
					info.setEmailBinded(c.getInt(c.getColumnIndex(MyDBColumes.EMAIL_BIND)));
					info.setPhoneBinded(c.getInt(c.getColumnIndex(MyDBColumes.PHONE_BIND)));
					info.setQqBinded(c.getInt(c.getColumnIndex(MyDBColumes.QQ_BIND)));
				}

				// 版本6后密码就是密文了
				if (oldVersion <= DBVersion.VERSION_BIND)
				{
					info.setPsd(c.getString(c.getColumnIndex(MyDBColumes.PSD)));
				}
				else
				{
					info.setPsd(new String(CipherUtil.decrypt(c.getBlob(c
							.getColumnIndex(MyDBColumes.PSD)))));	// 解密
				}
			}
			while(c.moveToNext());
			c.close();
		}
		return info;
	}

	public UserInfo getUserInfo()
	{
		if (userInfo == null)
		{
			SQLiteDatabase db = getWritableDatabase();
			Cursor c = db.query(TAB_USER, null, null, null, null, null, null);
			userInfo = new UserInfo();
			try
			{
				if (c != null)
				{
					if (c.moveToFirst())
					{
						id = c.getInt(0);
						userInfo.setNick(c.getString(c.getColumnIndex(MyDBColumes.NICK)));
						userInfo.setUserName(c.getString(c.getColumnIndex(MyDBColumes.NAME)));
						userInfo.setPsd(new String(CipherUtil.decrypt(c.getBlob(c
								.getColumnIndex(MyDBColumes.PSD)))));	// 解密
						userInfo.setUid(c.getBlob(c.getColumnIndex(MyDBColumes.UID)));
						userInfo.setPhone(c.getString(c.getColumnIndex(MyDBColumes.PHONE)));
						userInfo.setEmail(c.getString(c.getColumnIndex(MyDBColumes.EMAIL)));
						userInfo.setQqNick(c.getString(c.getColumnIndex(MyDBColumes.QQ_NICK)));
						userInfo.setMoney(c.getString(c.getColumnIndex(MyDBColumes.MONEY)));
						userInfo.setEmailBinded(c.getInt(c.getColumnIndex(MyDBColumes.EMAIL_BIND)));
						userInfo.setPhoneBinded(c.getInt(c.getColumnIndex(MyDBColumes.PHONE_BIND)));
						userInfo.setQqBinded(c.getInt(c.getColumnIndex(MyDBColumes.QQ_BIND)));
					}
					c.close();
				}
			}
			catch (Exception e)
			{
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
				LogerEngine.error(context, "查询数据库获取user信息失败" + Log.getStackTraceString(e), true, ep);
				e.printStackTrace();
				userInfo = null;
				getUserInfo(); // 数据库升级时先drop table然后create
								// table，在第一次调用getUserInfo()时表仍然是drop之前的那张表，不知道是什么原因
			}
			db.close();
		}
		return userInfo;
	}

	/**
	 * 要想改动数据库的值，必须通过globUserInfo来实现
	 * <p>
	 * 注意globUserInfo中不能有无效字段
	 */
	public void saveUserInfo(UserInfo info)
	{
		if (info == null)
		{
			return;
		}
		userInfo.copyInfo(info);
		SQLiteDatabase db = getWritableDatabase();
		db.update(TAB_USER, wrapContentValues(info), "_id=?", new String[]
		{ String.valueOf(id) });
		db.close();
	}

	private ContentValues wrapContentValues(UserInfo info)
	{
		ContentValues values = new ContentValues();
		values.put(MyDBColumes.NICK, info.getNick());
		values.put(MyDBColumes.NAME, info.getUserName());
		values.put(MyDBColumes.PSD, CipherUtil.encrypt(info.getPsd().getBytes()));	// 加密保存
		values.put(MyDBColumes.UID, info.getUid());
		values.put(MyDBColumes.EMAIL, info.getEmail());
		values.put(MyDBColumes.PHONE, info.getPhone());
		values.put(MyDBColumes.QQ_NICK, info.getQqNick());
		values.put(MyDBColumes.MONEY, info.getMoney());
		values.put(MyDBColumes.EMAIL_BIND, info.isEmailBinded());
		values.put(MyDBColumes.PHONE_BIND, info.isPhoneBinded());
		values.put(MyDBColumes.QQ_BIND, info.isQqBinded());
		return values;
	}

	public interface MyDBColumes
	{
		public final static String NAME = "name";
		public final static String PSD = "psd";
		public final static String UID = "uid";
		public final static String PHONE = "phone";
		public final static String EMAIL = "email";
		public final static String NICK = "nick"; // 昵称
		public final static String QQ_NICK = "qq_nick"; // qq昵称
		public final static String MONEY = "money";
		public final static String EMAIL_BIND = "email_bind";
		public final static String PHONE_BIND = "phone_bind";
		public final static String QQ_BIND = "qq_bind";
	}
}
