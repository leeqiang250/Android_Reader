package com.sz.mobilesdk.authentication;

import java.util.List;
import java.util.Vector;

import android.content.ContentValues;

import com.sz.mobilesdk.database.bean.Perconattribute;
import com.sz.mobilesdk.database.bean.Perconstraint;
import com.sz.mobilesdk.database.bean.Permission;
import com.sz.mobilesdk.database.practice.PermissionDAOImpl;

public class SZPermissionCommon
{

	static final String kConstraintDatetime = "datetime";

	static final String kConstraintAccumulated = "accumulated";

	static final String kConstraintIndividual = "individual";

	static final String kConstraintSystem = "system";

	static final String kConAttrStart = "start";

	static final String kConAttrEnd = "end";

	private PermissionDAOImpl permissionDAO = new PermissionDAOImpl();

	// the uid of permission
	String id = null;

	String asset_id = null;

	// the name of permission element
	String element = null;

	// 授权次数
	int odd_count = 0;

	// 授权次数
	int odd_timed_count = 0;

	// 授权时段
	long odd_datetime = 0L; // double
	public long odd_datetime_start = 0L; // double
	public long odd_datetime_end = 0L; // double

	// 授权周期
	double odd_interval = 0.0;

	// 累计使用时间
	double odd_accumulated = 0.0;

	// 绑定的授权用户
	String odd_individual = null;

	// 绑定的授权系统
	public String odd_system = null;

	@SuppressWarnings("unchecked")
	public void initWithPermission(Permission aPermission)
	{
		this.id = aPermission.getId();

		this.asset_id = aPermission.getAsset_id();

		this.element = aPermission.getElement();

		ContentValues contentValues = new ContentValues();
		aPermission.setPerconstraints((List<Perconstraint>) permissionDAO
				.findByQuery(new String[] { "permission_id" },
						new String[] { aPermission.getId() },
						Perconstraint.class));
		for (int i = 0; i < aPermission.getPerconstraints().size(); i++)
		{
			contentValues.put(aPermission.getPerconstraints().get(i)
					.getElement(), aPermission.getPerconstraints().get(i)
					.getValue());
		}

		this.odd_accumulated = Double.parseDouble(contentValues
				.getAsString(kConstraintAccumulated));

		this.odd_system = contentValues.getAsString(kConstraintSystem);

		this.odd_individual = contentValues.getAsString(kConstraintIndividual);

		if (contentValues.getAsString(kConstraintDatetime) != null)
		{
			// 如果datetime出现字符串会出错。
			this.odd_datetime = Long.parseLong(contentValues
					.getAsString(kConstraintDatetime));
			for (Perconstraint perconstraint : aPermission.getPerconstraints())
			{
				if (perconstraint.getElement().equals(kConstraintDatetime))
				{
					List<Perconattribute> perconattributes = new Vector<Perconattribute>();
					perconattributes = (List<Perconattribute>) permissionDAO
							.findByQuery(new String[] { "perconstraint_id" },
									new String[] { perconstraint.getId() },
									Perconattribute.class);
					for (Perconattribute perconattribute : perconattributes)
					{
						if (kConAttrStart.equals(perconattribute.getElement()))
							this.odd_datetime_start = Long
									.valueOf(perconattribute.getValue());
						else if (kConAttrEnd.equals(perconattribute
								.getElement()))
							this.odd_datetime_end = Long
									.valueOf(perconattribute.getValue());
					}
				}
			}
		}
	}

	// void fillWithPermission(Permission aPermission);
	// Boolean isDayPermission();
	// Boolean addTime();

	public boolean isAllPermission()
	{
		return this.odd_datetime == 0L;
	}

}
