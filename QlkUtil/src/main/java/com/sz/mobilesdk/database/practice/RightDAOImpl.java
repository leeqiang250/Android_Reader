package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Right;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;

public class RightDAOImpl extends SZBaseDAOPracticeImpl<Right> implements
		RightDAO
{

	private static RightDAOImpl daoimpl = new RightDAOImpl();

	public static RightDAOImpl getInstance()
	{
		if (daoimpl == null)
		{
			daoimpl = new RightDAOImpl();
		}
		return daoimpl;
	}

	private RightDAOImpl()
	{
	}

}
