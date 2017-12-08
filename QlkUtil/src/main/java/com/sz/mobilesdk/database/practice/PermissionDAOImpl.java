package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Permission;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;

public class PermissionDAOImpl extends SZBaseDAOPracticeImpl<Permission> implements PermissionDAO
{
	private static PermissionDAOImpl daoimpl = new PermissionDAOImpl();

	public static PermissionDAOImpl getInstance()
	{
		if (daoimpl == null)
		{
			daoimpl = new PermissionDAOImpl();
		}
		return daoimpl;
	}

}
