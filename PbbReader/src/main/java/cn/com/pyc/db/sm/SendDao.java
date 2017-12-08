package cn.com.pyc.db.sm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import cn.com.pyc.bean.SmInfo;

public class SendDao extends SmDao
{
	private static SendDao sendDao;

	public static SendDao getInstance(Context context)
	{
		if (sendDao == null)
		{
			sendDao = new SendDao(context);
		}
		return sendDao;
	}

	public SendDao(Context context)
	{
		super(context);
	}

	@Override
	protected String getTabName()
	{
		return SndCol.TAB_SEND;
	}

	@Override
	protected void fillInfos(Cursor c, SmInfo info, boolean needDecode)
	{
		super.fillInfos(c, info, needDecode);
		info.setActiveNum(c.getInt(c.getColumnIndex(SndCol.ACTIVE_NUM)));
		info.setBindNum(c.getInt(c.getColumnIndex(SndCol.BIND_MACHINE)));
		info.setOrderNo(c.getString(c.getColumnIndex(SndCol.ORDER_NO)));
	}

	@Override
	protected ContentValues wrapContentValues(SmInfo info, boolean needCode)
	{
		ContentValues values = super.wrapContentValues(info, needCode);
		values.put(SndCol.ACTIVE_NUM, info.getActiveNum());
		values.put(SndCol.BIND_MACHINE, info.getBindNum());
		values.put(SndCol.ORDER_NO, info.getOrderNo());
		return values;
	}
}
