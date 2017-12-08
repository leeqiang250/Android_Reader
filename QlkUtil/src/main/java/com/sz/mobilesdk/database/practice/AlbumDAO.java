package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPractice;

public interface AlbumDAO extends SZBaseDAOPractice<Album>
{
	// 根据专辑ID 删除元组 调用AlbumContent
	public boolean cascadedDelete(String id);
}
