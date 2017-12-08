package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Perconattribute;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;

public class PerconattributeDAOImpl extends SZBaseDAOPracticeImpl<Perconattribute> implements PerconattributeDAO
{
	private static PerconattributeDAOImpl daoimpl = new PerconattributeDAOImpl();

	public static PerconattributeDAOImpl getInstance()
	{
		if (daoimpl == null)
		{
			daoimpl = new PerconattributeDAOImpl();
		}
		return daoimpl;
	}
}
