package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Bookmark;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;

public class BookmarkDAOImpl extends SZBaseDAOPracticeImpl<Bookmark> implements
		BookmarkDAO
{
	private static BookmarkDAOImpl daoInstance = new BookmarkDAOImpl();

	public static BookmarkDAOImpl getInstance()
	{
		if (daoInstance == null) daoInstance = new BookmarkDAOImpl();
		return daoInstance;
	}
}
