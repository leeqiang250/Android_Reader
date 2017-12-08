package com.sz.mobilesdk.util;

import java.util.List;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;
import android.util.Log;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.DBHelper;
import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.bean.Bookmark;
import com.sz.mobilesdk.database.bean.Downdata;
import com.sz.mobilesdk.database.bean.Perconattribute;
import com.sz.mobilesdk.database.bean.Perconstraint;
import com.sz.mobilesdk.database.bean.Permission;
import com.sz.mobilesdk.database.bean.Right;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.database.practice.AlbumDAOImpl;
import com.sz.mobilesdk.database.practice.AssetDAOImpl;
import com.sz.mobilesdk.database.practice.BookmarkDAOImpl;
import com.sz.mobilesdk.database.practice.DowndataDAOImpl;
import com.sz.mobilesdk.database.practice.PerconattributeDAOImpl;
import com.sz.mobilesdk.database.practice.PerconstraintDAOImpl;
import com.sz.mobilesdk.database.practice.PermissionDAOImpl;
import com.sz.mobilesdk.database.practice.RightDAOImpl;
import com.sz.mobilesdk.manager.db.DownData2DBManager;

public class SZDbUtil
{

	private static final String TAG = "SZDbUtil";
	private static DBHelper helper;

	/**
	 * 实例化数据DRMDBHelper,Context <br/>
	 * dbName是以userName+"_"+id命名的
	 * 
	 * @param mContext
	 */
	public SZDbUtil(Context mContext)
	{
		String userName = SZInitInterface.getUserName("");
		String id = (String) SPUtil.get(Fields.FIELDS_ID, "");
		String dbName = userName + Fields._LINE + id;
		helper = DBHelper.getInstance(mContext, dbName);
	}

	public void checkAlbumContentTable()
	{
		AlbumContentDAOImpl.getInstance().create(AlbumContent.class);
		String tableName = AlbumContent.class.getSimpleName();
		// 检查表字段
		boolean myProIdExist = checkColumnExist(tableName, "myProId");
		SZLog.w("myProIdExist = " + myProIdExist);
		try
		{
			if (!myProIdExist)
			{
				helper.ExecSQL("ALTER TABLE " + tableName
						+ " ADD COLUMN myProId");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 创建数据表
	 */
	public boolean createDBTable()
	{
		AlbumDAOImpl.getInstance().create(Album.class);
		checkAlbumContentTable();

		DowndataDAOImpl.getInstance().create(Downdata.class);
		RightDAOImpl.getInstance().create(Right.class);
		AssetDAOImpl.getInstance().create(Asset.class);
		PermissionDAOImpl.getInstance().create(Permission.class);
		PerconstraintDAOImpl.getInstance().create(Perconstraint.class);
		PerconattributeDAOImpl.getInstance().create(Perconattribute.class);
		BookmarkDAOImpl.getInstance().create(Bookmark.class);

		Log.v(TAG, "create dbTable success!");
		return true;
	}

	// ////////////////////////////////////////////
	// ////////////////////////////////////////////
	// ///////////以下是static静态方法////////////////////
	// ////////////////////////////////////////////
	// ////////////////////////////////////////////
	/**
	 * 销毁数据库实例,db和helper
	 */
	public static void destoryDBHelper()
	{
		DBHelper.setDBHelperNULL();
		helper = null;
	}

	/**
	 * 删除数据库
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static boolean deleteDatabase(Context context, String name)
	{
		return context.deleteDatabase(name);
	}

	/**
	 * 清除所有表中的数据
	 */
	public static void deleteTableData()
	{
		AlbumDAOImpl daoImpl = AlbumDAOImpl.getInstance();
		daoImpl.DeleteTableData(Downdata.class.getSimpleName());
		daoImpl.DeleteTableData(Perconattribute.class.getSimpleName());
		daoImpl.DeleteTableData(Perconstraint.class.getSimpleName());
		daoImpl.DeleteTableData(Permission.class.getSimpleName());
		daoImpl.DeleteTableData(Asset.class.getSimpleName());
		daoImpl.DeleteTableData(Right.class.getSimpleName());
		daoImpl.DeleteTableData(AlbumContent.class.getSimpleName());
		daoImpl.DeleteTableData(Album.class.getSimpleName());
		daoImpl.DeleteTableData(Bookmark.class.getSimpleName());
		// daoImpl.DeleteTableData("sqlite_sequence");
		DownData2DBManager.Builder().deleteAll();
		Log.v(TAG, "clear table data success!");
	}

	public static void dropTable()
	{
		AlbumDAOImpl daoImpl = AlbumDAOImpl.getInstance();
		daoImpl.DropTable(Downdata.class.getSimpleName());
		daoImpl.DropTable(Perconattribute.class.getSimpleName());
		daoImpl.DropTable(Perconstraint.class.getSimpleName());
		daoImpl.DropTable(Permission.class.getSimpleName());
		daoImpl.DropTable(Asset.class.getSimpleName());
		daoImpl.DropTable(Right.class.getSimpleName());
		daoImpl.DropTable(AlbumContent.class.getSimpleName());
		daoImpl.DropTable(Album.class.getSimpleName());
		daoImpl.DropTable(Bookmark.class.getSimpleName());
		// daoImpl.DeleteTableData("sqlite_sequence");
		Log.v(TAG, "drop table success!");
	}

	/**
	 * 方法：检查某表列是否存在
	 * 
	 * @param tableName
	 *            表名
	 * @param columnName
	 *            列名
	 * @return
	 */
	protected static boolean checkColumnExist(String tableName,
			String columnName)
	{
		boolean result = false;
		Cursor cursor = null;
		try
		{
			// 查询一行
			SQLiteDatabase db = helper.getDB();
			cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0",
					null);
			result = (cursor != null && cursor.getColumnIndex(columnName) != -1);
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != cursor && !cursor.isClosed())
			{
				cursor.close();
				cursor = null;
			}
		}

		return result;
	}

	/**
	 * 删除专辑的相关内容（文件数据，权限） <br/>
	 * 更新专辑时操作
	 * 
	 * @param mContext
	 * @param myProId
	 */
	public static void deleteAlbumAttachInfos(String myProId)
	{
		// 获取专辑ID
		String album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProId);
		// 1.删除专辑album
		AlbumDAOImpl.getInstance().deleteAlbumByMyProId(myProId);
		// 2.删除文件albumcontent
		AlbumDAOImpl.getInstance().deleteAlbumContentByMyProId(myProId);

		SZLog.v(TAG, "album_id: " + album_Id);
		if (album_Id == null) return;

		String albumContentId = AlbumDAOImpl.getInstance().findAlbumContentId(
				album_Id);
		if (albumContentId != null)
		{
			// 2.删除AlbumContent,如果存在的话
			AlbumDAOImpl.getInstance().DeleteAlbumContent(album_Id);
		}

		String RightContentId = AlbumDAOImpl.getInstance()
				.findRightId(album_Id);
		if (RightContentId != null)
		{
			// 3.删除Right
			AlbumDAOImpl.getInstance().DeleteRight(album_Id);
		}
		List<String> assetIdList = AlbumDAOImpl.getInstance().findAssetId(
				album_Id);
		if (assetIdList != null && !assetIdList.isEmpty())
		{
			// 4.删除Asset
			AlbumDAOImpl.getInstance().DeleteAsset(album_Id);

			for (int i = 0; i < assetIdList.size(); i++)
			{
				String assetId = assetIdList.get(i);
				// 5.删除书签
				BookmarkDAOImpl.getInstance().DeleteBookMark(assetId);
				String PermissionId = AlbumDAOImpl.getInstance()
						.findPermissionId(assetId);
				if (PermissionId != null)
				{
					// 6.删除Permission
					AlbumDAOImpl.getInstance().DeletePermission(assetId);
					List<String> perconstraintIdList = AlbumDAOImpl
							.getInstance().findPerconstraintId(PermissionId);
					if (perconstraintIdList != null)
					{
						// 7.删除Perconstraint
						AlbumDAOImpl.getInstance().DeletePerconstraint(
								PermissionId);
					}
				}
			}
		}
	}

}
