package com.sz.mobilesdk;

import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.bean.ContentRight;
import com.sz.mobilesdk.database.practice.AlbumDAOImpl;
import com.sz.mobilesdk.util.MediaUtils;
import com.sz.mobilesdk.util.SZDbUtil;

import java.util.List;

/**
 * 专辑操作，（获取权限，查询、移除，获取权限时间等等）
 * <p>
 * 已过时！
 */
@Deprecated
public class SZAlbumInterface
{

	/**
	 * 初始化专辑相关获取内容（子专辑信息，权限，密钥等）
	 */
	public static void initMedias(String albumID)
	{
		MediaUtils.getInstance().initMedia(albumID);
	}

	/**
	 * 获取专辑的子专辑基本信息集合
	 * 
	 * @return
	 */
	public static List<AlbumContent> getAlbumContentList()
	{
		return MediaUtils.getInstance().getMediaList();
	}

	/**
	 * 根据myProId查询专辑 ,如果未查询到，返回null
	 */
	public static Album findAlbumByMyProId(String myProId)
	{
		Album album = AlbumDAOImpl.getInstance().findAlbumByMyProId(myProId);
		return album;
	}

	/**
	 * 删除专辑的相关内容（db数据，权限等） <br/>
	 * 更新或删除单个专辑时操作
	 * 
	 * @param myProId
	 *            文件的proId
	 */
	public static void deleteAlbumAttachInfo(String myProId)
	{
		SZDbUtil.deleteAlbumAttachInfos(myProId);
	}

	/**
	 * 获取专辑的子专辑权限信息集合
	 * 
	 * @return
	 */
	public static List<ContentRight> getAlbumMediaRightList()
	{
		return MediaUtils.getInstance().getMediaRight();
	}

	/**
	 * 获取专辑的子专辑密钥集合
	 * 
	 * @return
	 */
	public static List<Asset> getAlbumAssetList()
	{
		return MediaUtils.getInstance().getAssetList();
	}

}
