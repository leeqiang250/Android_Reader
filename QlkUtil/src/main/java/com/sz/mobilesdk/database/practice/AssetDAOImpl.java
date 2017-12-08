package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;

public class AssetDAOImpl extends SZBaseDAOPracticeImpl<Asset> implements
		AssetDAO
{

	private static AssetDAOImpl adi = null;

	public static AssetDAOImpl getInstance()
	{
		if (adi == null)
		{
			adi = new AssetDAOImpl();
		}
		return adi;
	}

}
