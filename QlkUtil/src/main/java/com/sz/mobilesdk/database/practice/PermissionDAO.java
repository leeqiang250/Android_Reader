package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Permission;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPractice;

public interface PermissionDAO extends SZBaseDAOPractice<Permission>
{
	// 接收Asset的id数组 删除 调用 PermissionConstraint
}
