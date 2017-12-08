package cn.com.pyc.pbbonline.util;

import java.io.File;
import java.util.List;
import java.util.Locale;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.bean.Permission;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.database.practice.AssetDAOImpl;
import com.sz.mobilesdk.database.practice.DowndataDAOImpl;
import com.sz.mobilesdk.database.practice.PerconstraintDAOImpl;
import com.sz.mobilesdk.database.practice.PermissionDAOImpl;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SZDbUtil;
import com.sz.mobilesdk.util.SZLog;

public class DeleteFileUtil
{

	/**
	 * 删除文件夹 专辑 (已下载的)
	 * 
	 * @param shareId
	 * @param folderId
	 */
	public static void deleteFolder(String shareId, String folderId)
	{
		SZDbUtil.deleteAlbumAttachInfos(folderId);
		DowndataDAOImpl.getInstance().deleteDowndataByMyProId(folderId);
		String _path = DirsUtil.getSaveFilePath(SZInitInterface.getUserName(""), shareId, folderId);
		FileUtil.delAllFile(_path);
		if (PathUtil.DEF_DOWNLOAD_DRM_PATH != null)
		{
			//如果存在未删除的drm包，则删除
			String drmPath = PathUtil.DEF_DOWNLOAD_DRM_PATH + "/";
			File[] files = new File(drmPath).listFiles();
			if (files != null && files.length > 0)
			{
				FileUtil.delAllFile(drmPath);
			}
		}

		SZLog.v("", "deleteFolder");
	}

	/**
	 * 删除文件(已下载的)
	 * 
	 * @param shareId
	 * @param folderId
	 * @param fileId
	 */
	public static void deleteFile(String shareId, String folderId, String fileId)
	{
		AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(fileId);
		if (ac == null)
			return;
		Asset asset = (Asset) AssetDAOImpl.getInstance().findByQuery(new String[]
		{ "_id" }, new String[]
		{ ac.getAsset_id() }, Asset.class).get(0);
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) PermissionDAOImpl.getInstance()
				.findByQuery(new String[]
				{ "asset_id" }, new String[]
				{ ac.getAsset_id() }, Permission.class);
		if (permissions != null)
		{
			for (Permission permission : permissions)
			{
				PerconstraintDAOImpl.getInstance().DeletePerconstraint(permission.getId());
				PermissionDAOImpl.getInstance().delete(permission);
			}
		}

		AlbumContentDAOImpl.getInstance().deleteAlbumContentByContenId(fileId);
		AssetDAOImpl.getInstance().delete(asset);

		String folderDir = DirsUtil.getSaveFilePath(SZInitInterface.getUserName(""), shareId,
				folderId);
		String ext = ac.getFileType().toLowerCase(Locale.getDefault());
		String filePath = folderDir + fileId + "." + ext;
		FileUtil.deleteFileWithPath(filePath);
		
		if (PathUtil.DEF_DOWNLOAD_DRM_PATH != null)
		{
			//如果存在未删除的drm包，则删除
			String drmPath = PathUtil.DEF_DOWNLOAD_DRM_PATH + "/" + fileId + Fields._DRM;
			if (new File(drmPath).exists())
				FileUtil.deleteFileWithPath(drmPath);
		}

		SZLog.v("", "deleteFile");
	}

}
