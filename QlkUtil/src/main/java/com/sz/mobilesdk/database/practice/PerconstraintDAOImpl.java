package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Perconstraint;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;

public class PerconstraintDAOImpl extends SZBaseDAOPracticeImpl<Perconstraint>
		implements PerconstraintDAO
{
	private static PerconstraintDAOImpl daoimpl = new PerconstraintDAOImpl();

	public static PerconstraintDAOImpl getInstance()
	{
		if (daoimpl == null)
		{
			daoimpl = new PerconstraintDAOImpl();
		}
		return daoimpl;
	}
}
