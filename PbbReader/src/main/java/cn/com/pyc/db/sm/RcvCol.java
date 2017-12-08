package cn.com.pyc.db.sm;

public interface RcvCol extends SmCol
{
	public static final String TAB_RECEIVE = "receive";

	public static final String CREATE_RECEIVE_TABLE = "CREATE TABLE IF NOT EXISTS " + TAB_RECEIVE
			+ "(_ID INTEGER PRIMARY KEY AUTOINCREMENT,"	//
			+ RcvCol.REMAIN_DAYS + " TEXT," //
			+ RcvCol.REMAIN_YEARS + " TEXT,"//
			+ RcvCol.NEED_ACTIVE + " INTEGER,"//
			+ RcvCol.FIRST_OPEN + " TEXT," //
			+ RcvCol.SHOW_LIMIT + " INTEGER," //
			+ RcvCol.HARD_NO + " TEXT,"//
			+ RcvCol.MAKER_UID + " TEXT,"//
			+ RcvCol.OUT_DATE + " BLOB,"//
			+ RcvCol.ENCODE_KEY + " BLOB,"//
			+ RcvCol.TIME_MODIFIED + " INTEGER,"//
			+ RcvCol.CONTACT_MUST + " INTEGER,"//
			+ RcvCol.SELF_MUST + " INTEGER,"//
			+ RcvCol.SECRET + " INTEGER,"//
			+ RcvCol.LAST_OPEN_TIME + " TEXT,"//
			+ RcvCol.EMAIL_BUYER + " TEXT,"//
			+ RcvCol.PHONE_BUYER + " TEXT,"//
			+ RcvCol.QQ_BUYER + " TEXT,"//
			+ RcvCol.SELF_DEFINE_KEY1 + " TEXT,"//
			+ RcvCol.SELF_DEFINE_KEY2 + " TEXT,"//
			+ RcvCol.SELF_DEFINE_VALUE1 + " TEXT,"//
			+ RcvCol.SELF_DEFINE_VALUE2 + " TEXT,"//
			+ RcvCol.SERIES_ID + " INTEGER,"// xiong-增加系列id字段
			+ RcvCol.FILE_PATH + " TEXT,"// xiong-增加系列id字段
			+ RcvCol.FILE_VERSION + " INTEGER,"
			+ SmCol.COMMON_TAB_SQL;

	public final static String REMAIN_DAYS = "remain_days";
	public final static String REMAIN_YEARS = "remain_years";
	public final static String NEED_ACTIVE = "need_active";
	public static final String FIRST_OPEN = "first_open";
	public static final String SHOW_LIMIT = "show_limit";
	public static final String MAKER_UID = "maker_uid";
	public static final String SERIES_ID = "serises_id"; // xiong-增加系列ID
															// 字段
	public final static String FILE_PATH = "file_path";
	public final static String FILE_VERSION = "file_version";

	// 取消离线结构后添加的
	public static final String HARD_NO = "hard_no";	// 付费文件绑定的设备号
	public static final String OUT_DATE = "out_date";	// 付费文件到期时间
	public static final String ENCODE_KEY = "encode_key";
	public static final String TIME_MODIFIED = "time_modified";
	public static final String CONTACT_MUST = "contact_must";
	public static final String SELF_MUST = "self_must";
	public static final String SECRET = "secret";
	public static final String LAST_OPEN_TIME = "last_open_time";
	public static final String EMAIL_BUYER = "email_buyer";
	public static final String QQ_BUYER = "qq_buyer";
	public static final String PHONE_BUYER = "phone_buyer";
	public static final String SELF_DEFINE_KEY1 = "self_define_key1";
	public static final String SELF_DEFINE_KEY2 = "self_define_key2";
	public static final String SELF_DEFINE_VALUE1 = "self_define_value1";
	public static final String SELF_DEFINE_VALUE2 = "self_define_value2";
}
