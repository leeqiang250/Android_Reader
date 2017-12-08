package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Downdata;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;

/**
 * 当前用户下载数据的DAO
 */
public class DowndataDAOImpl extends SZBaseDAOPracticeImpl<Downdata> implements DowndataDAO
{
	private static DowndataDAOImpl daoInstance = new DowndataDAOImpl();

	public static DowndataDAOImpl getInstance()
	{
		return daoInstance;
	}

}
