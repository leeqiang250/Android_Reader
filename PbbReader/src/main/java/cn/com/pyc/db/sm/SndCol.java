package cn.com.pyc.db.sm;

public interface SndCol extends SmCol
{
	public static final String TAB_SEND = "send";

	public static final String CREATE_SEND_TABLE = "CREATE TABLE IF NOT EXISTS " + TAB_SEND
			+ "(_ID INTEGER PRIMARY KEY AUTOINCREMENT," + SndCol.ACTIVE_NUM + " INTEGER,"
			+ SndCol.BIND_MACHINE + " INTEGER," + SndCol.ORDER_NO + " TEXT," + SmCol.COMMON_TAB_SQL;

	public static final String BIND_MACHINE = "bind_machine";
	public static final String ACTIVE_NUM = "active";
	public static final String ORDER_NO = "order_no";
}
