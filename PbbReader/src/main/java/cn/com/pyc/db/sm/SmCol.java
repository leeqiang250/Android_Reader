package cn.com.pyc.db.sm;

public interface SmCol
{
	/** 两张表共用sql语句 */
	public static final String COMMON_TAB_SQL = SmCol.FILE_ID + " INTEGER," + SmCol.PAY_FILE
			+ " INTEGER," + SmCol.CAN_OPEN + " INTEGER," + SmCol.OPEN_NUM + " INTEGER,"
			+ SmCol.OPENED_NUM + " INTEGER," + SmCol.DAYS + " INTEGER," + SmCol.YEARS + " INTEGER,"
			+ SmCol.APP_TYPE + " INTEGER," + SmCol.SINGLE_OPEN + " INTEGER," + SmCol.NICK
			+ " TEXT," + SmCol.START_TIME + " TEXT," + SmCol.END_TIME + " TEXT," + SmCol.REMARK
			+ " TEXT," + SmCol.EMAIL + " TEXT," + SmCol.PHONE + " TEXT," + SmCol.OFFLINE
			+ " INTEGER," + SmCol.QQ + " TEXT," + SmCol.FIRST_OPEN_TIME + " TEXT,"
			+ SmCol.MAKE_TIME + " TEXT)";

	public static final String FILE_ID = "file_id"; // 以它为索引来检索信息
	public static final String START_TIME = "start_time";
	public static final String END_TIME = "end_time";
	public static final String OPEN_NUM = "open_num";
	public static final String OPENED_NUM = "opened_num";
	public static final String CAN_OPEN = "can_open";
	public static final String SINGLE_OPEN = "single_open";
	public static final String REMARK = "remark";
	public static final String DAYS = "days";
	public static final String YEARS = "years";
	public static final String PAY_FILE = "pay_file";
	public static final String MAKE_TIME = "make_time";
	public static final String NICK = "nick";
	public static final String APP_TYPE = "client_type";
	public final static String PHONE = "phone";
	public final static String EMAIL = "email";
	public final static String QQ = "qq";
	@Deprecated
	public final static String OFFLINE = "offline";
	@Deprecated
	public final static String FIRST_OPEN_TIME = "first_open_time";
}
