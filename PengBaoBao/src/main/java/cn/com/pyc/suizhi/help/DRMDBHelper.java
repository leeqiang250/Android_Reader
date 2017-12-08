package cn.com.pyc.suizhi.help;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

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
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.SZLog;
import com.tencent.tauth.Tencent;

import net.sqlcipher.Cursor;

import java.util.List;
import java.util.Locale;

import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConfig;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.manager.db.DownDataPatDBManager;
import cn.com.pyc.suizhi.util.SZPathUtil;
import cn.com.pyc.utils.ClearKeyUtil;

public class DRMDBHelper {

    //private Context mContext;
    private DBHelper helper;

    /**
     * 实例化数据DRMDBHelper
     *
     * @param context
     */
    public DRMDBHelper(Context context) {
        //mContext = context;
        //helper = DBHelper.getInstance(mContext, Constant.getName());
        helper = DBHelper.getInstance(context, SZConstant.getAccountId());
    }

    /**
     * 方法：检查某表列是否存在
     *
     * @param tableName  表名
     * @param columnName 列名
     * @return
     */
    public boolean checkColumnExist(String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            // 查询一行
            cursor = helper.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
            result = (cursor != null && cursor.getColumnIndex(columnName) != -1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 创建并且检查Album表,看新增加的字段是否存在，不存在就增加
     */
    private void checkAlbumTable() {
        AlbumDAOImpl.getInstance().create(Album.class);
        String tableName = Album.class.getSimpleName();
        // 检查表字段
        boolean authorExist = checkColumnExist(tableName, "author");
        boolean ratioExist = checkColumnExist(tableName, "picture_ratio");
        boolean dateExist = checkColumnExist(tableName, "publishDate");
        //boolean addtimeExist = checkColumnExist(tableName, "save_Last_add_time");
        //boolean modifytimeExist = checkColumnExist(tableName, "save_Last_modify_time");

        SZLog.w("authorExist = " + authorExist);
        SZLog.w("ratioExist = " + ratioExist);
        SZLog.w("publishDateExist = " + dateExist);
        //SZLog.w("addtimeExist = " + addtimeExist);
        //SZLog.w("modifytimeExist = " + modifytimeExist);
        try {
            if (!authorExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN author");
            }
            if (!ratioExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN picture_ratio");
            }
            if (!dateExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN publishDate");
            }
//            if (!addtimeExist) {
//                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN save_Last_add_time");
//            }
//            if (!modifytimeExist) {
//                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN save_Last_modify_time");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAlbumContentTable() {
        AlbumContentDAOImpl.getInstance().create(AlbumContent.class);
        String tableName = AlbumContent.class.getSimpleName();
        // 检查表字段
        boolean currentItemIdExist = checkColumnExist(tableName, "currentItemId");
        boolean latestItemIdExist = checkColumnExist(tableName, "latestItemId");
        boolean collectionIdExist = checkColumnExist(tableName, "collectionId");
        boolean myProIdExist = checkColumnExist(tableName, "myProId");
        boolean fileTypeExist = checkColumnExist(tableName, "fileType");
        boolean musicLrcIdExist = checkColumnExist(tableName, "musicLrcId");
        boolean contentSizeExist = checkColumnExist(tableName, "contentSize");
        SZLog.w("currentItemIdExist = " + currentItemIdExist);
        SZLog.w("latestItemIdExist = " + latestItemIdExist);
        SZLog.w("collectionIdExist = " + collectionIdExist);
        SZLog.w("myProIdExist = " + myProIdExist);
        SZLog.w("fileTypeExist = " + fileTypeExist);
        SZLog.w("musicLrcIdExist = " + musicLrcIdExist);
        SZLog.w("contentSizeExist = " + contentSizeExist);
        try {
            if (!currentItemIdExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN currentItemId");
            }
            if (!latestItemIdExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN latestItemId");
            }
            if (!collectionIdExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN collectionId");
            }
            if (!myProIdExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN myProId");
            }
            if (!fileTypeExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN fileType");
            }
            if (!musicLrcIdExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN musicLrcId");
            }
            if (!contentSizeExist) {
                helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN contentSize");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermissionTable() {
        PermissionDAOImpl.getInstance().create(Permission.class);
        String tableName = Permission.class.getSimpleName();
        boolean expiredExist = checkColumnExist(Permission.class.getSimpleName(), "expired");
        SZLog.w("expiredExist = " + expiredExist);
        if (!expiredExist) {
            helper.ExecSQL("ALTER TABLE " + tableName + " ADD COLUMN expired");
        }
    }

    /**
     * 检查相关数据表字段
     */
    public void checkTable() {
        checkAlbumTable();
        checkAlbumContentTable();
        checkPermissionTable();
        //AlbumContentHistoryDAOImpl.getInstance().create(AlbumContentHistory.class);
    }

    /**
     * 创建数据表
     */
    public boolean createDBTable() {
        checkTable();

        DowndataDAOImpl.getInstance().create(Downdata.class);
        RightDAOImpl.getInstance().create(Right.class);
        AssetDAOImpl.getInstance().create(Asset.class);
        PerconstraintDAOImpl.getInstance().create(Perconstraint.class);
        PerconattributeDAOImpl.getInstance().create(Perconattribute.class);
        BookmarkDAOImpl.getInstance().create(Bookmark.class);

        return true;
    }

    // ////////////////////////////////////////////
    // ////////////////////////////////////////////
    // ///////////以下是static静态方法////////////////////
    // ////////////////////////////////////////////
    // ////////////////////////////////////////////

    /**
     * 销毁数据库实例,db和helper <br/>
     * 创建数据库之前，调用销毁，否则可能创建不成功！
     */
    public static void destroyDBHelper() {
        DBHelper.setDBHelperNULL();
    }

    /**
     * 清除表数据，表名称和createDBTable()中创建名一致
     */
    public static void deleteTableData() {
        DowndataDAOImpl.getInstance().DeleteTableData(Downdata.class.getSimpleName());

        AlbumDAOImpl.getInstance().DeleteTableData(Perconattribute.class.getSimpleName());
        AlbumDAOImpl.getInstance().DeleteTableData(Perconstraint.class.getSimpleName());
        AlbumDAOImpl.getInstance().DeleteTableData(Permission.class.getSimpleName());

        AlbumDAOImpl.getInstance().DeleteTableData(Asset.class.getSimpleName());
        AlbumDAOImpl.getInstance().DeleteTableData(Right.class.getSimpleName());

        AlbumDAOImpl.getInstance().DeleteTableData(AlbumContent.class.getSimpleName());
        AlbumDAOImpl.getInstance().DeleteTableData(Album.class.getSimpleName());

        BookmarkDAOImpl.getInstance().DeleteTableData(Bookmark.class.getSimpleName());
        // AlbumDAOImpl.getInstance().DeleteTableData("sqlite_sequence");
    }

    /*
     * 删除专辑的相关内容（文件数据，权限，书签，当前阅读索引） <br/>
     * 更新专辑时操作
     *
     * @param myProId 文件夹对应的id
     */
//    @Deprecated
//    public static void deleteAlbumAttachInfos(String myProId) {
//        // 当前阅读索引重新计数
//        MediaUtils.MPDF_CURRENTPOS = -1;
//        ClickIndexDBManager.Builder().deleteByMyProId(myProId);
//        // 1.删除专辑album
//        AlbumDAOImpl.getInstance().deleteAlbumByMyProId(myProId);
//
//        // 获取专辑ID
//        String album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProId);
//        if (album_Id == null) return;
//
//        String albumContentId = AlbumDAOImpl.getInstance().findAlbumContentId(
//                album_Id);
//        if (albumContentId != null) {
//            // 2.删除AlbumContent
//            AlbumDAOImpl.getInstance().DeleteAlbumContent(album_Id);
//        }
//
//        String RightContentId = AlbumDAOImpl.getInstance()
//                .findRightId(album_Id);
//        if (RightContentId != null) {
//            // 3.删除Right
//            AlbumDAOImpl.getInstance().DeleteRight(album_Id);
//        }
//        List<String> assetIdList = AlbumDAOImpl.getInstance().findAssetId(
//                album_Id);
//        if (assetIdList != null && !assetIdList.isEmpty()) {
//            // 4.删除Asset
//            AlbumDAOImpl.getInstance().DeleteAsset(album_Id);
//
//            for (int i = 0; i < assetIdList.size(); i++) {
//                String assetId = assetIdList.get(i);
//                // 5.删除书签
//                BookmarkDAOImpl.getInstance().DeleteBookMark(assetId);
//                String PermissionId = AlbumDAOImpl.getInstance()
//                        .findPermissionId(assetId);
//                if (PermissionId != null) {
//                    // 6.删除Permission
//                    AlbumDAOImpl.getInstance().DeletePermission(assetId);
//                    ArrayList<String> perconstraintIdList = AlbumDAOImpl
//                            .getInstance().findPerconstraintId(PermissionId);
//                    if (perconstraintIdList != null) {
//                        // 7.删除Perconstraint
//                        AlbumDAOImpl.getInstance().DeletePerconstraint(
//                                PermissionId);
//                    }
//                }
//            }
//        }
//    }

    /*
     * 删除文件相关内容
     *
     * @param contentId 文件id
     */
//    @Deprecated
//    public static void deleteAlbumContentAttachInfos(String contentId) {
//        // TODO：
//        // 通过contentId查询id
//        String album_id = AlbumContentDAOImpl.getInstance().findAlbumIdByContentId(contentId);
//        SZLog.v("deleteFile:album_id = " + album_id);
//        if (album_id == null) return;
//
//        // 1.删除AlbumContent
//        AlbumContentDAOImpl.getInstance().deleteAlbumContentByContentId(
//                contentId);
//
//        // 2.删除Right
//        RightDAOImpl.getInstance().DeleteRight(album_id);
//
//        List<String> assetIdList = AssetDAOImpl.getInstance().findAssetId(
//                album_id);
//
//        if (assetIdList != null && !assetIdList.isEmpty()) {
//            // 3.删除Asset
//            AssetDAOImpl.getInstance().DeleteAsset(album_id);
//
//            for (int i = 0, count = assetIdList.size(); i < count; i++) {
//                String assetId = assetIdList.get(i);
//                String permissionId = AlbumDAOImpl.getInstance()
//                        .findPermissionId(assetId);
//                if (permissionId != null) {
//                    // 4.删除Permission
//                    PermissionDAOImpl.getInstance().DeletePermission(assetId);
//                }
//                List<String> perconstraintIdList = AlbumDAOImpl.getInstance()
//                        .findPerconstraintId(permissionId);
//                if (perconstraintIdList != null) {
//                    // 5.删除Perconstraint
//                    PerconstraintDAOImpl.getInstance().DeletePerconstraint(
//                            permissionId);
//                }
//                // 6.删除书签
//                BookmarkDAOImpl.getInstance().deleteBookMark(assetId);
//            }
//        }
//    }

    /**
     * 删除文件对应的数据库信息
     *
     * @param fileId 文件id
     * @return
     */
    public static AlbumContent deleteFileDBDatas(String fileId) {
        AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(fileId);
        if (ac == null) return null;
        List<?> byQuery = AssetDAOImpl.getInstance().findByQuery(new String[]
                {"_id"}, new String[]{ac.getAsset_id()}, Asset.class);
        Asset asset = null;
        if (byQuery.size() != 0) {
            asset = (Asset) byQuery.get(0);
        }
        /*Asset asset = (Asset) AssetDAOImpl.getInstance().findByQuery(new String[]
                {"_id"}, new String[]{ac.getAsset_id()}, Asset.class).get(0);*/
        @SuppressWarnings("unchecked")
        List<Permission> permissions = (List<Permission>) PermissionDAOImpl.getInstance()
                .findByQuery(new String[]{"asset_id"}, new String[]
                        {ac.getAsset_id()}, Permission.class);
        if (permissions != null) {
            for (Permission permission : permissions) {
                PerconstraintDAOImpl.getInstance().DeletePerconstraint(permission.getId());
                PermissionDAOImpl.getInstance().delete(permission);
            }
        }
        AssetDAOImpl.getInstance().delete(asset);
        AlbumContentDAOImpl.getInstance().deleteAlbumContentByContentId(fileId);
        return ac;
    }

    /**
     * 删除文件(已下载的)
     *
     * @param folderId 文件夹id
     * @param fileId   文件id
     */
    public static void deleteFile(String folderId, String fileId) {
        //ShareMomentEngine.clearSharePosition();
        ProgressHelp.removeProgress(fileId);    //文件保存进度如果存在的话
        String fileId_ = (String) ProgressHelp.getProgress("ap_" + folderId, "");
        if (TextUtils.equals(fileId_, fileId)) {
            //删除的文件id和续播的文件相等，则清除续播记录，对应ListFileActivity续播功能：againPlaying
            ProgressHelp.removeProgress("ap_" + folderId);
        }
        DownloadHelp.removeFileProgress(fileId); //如果存在的话
        AlbumContent ac = deleteFileDBDatas(fileId);
        if (ac == null) return;
        final String localPath = SZPathUtil.getFilePrefixPath() + "/" + folderId + "/" + fileId;
        final String ext = ac.getFileType().toLowerCase(Locale.getDefault());
        String filePath = localPath + "." + ext;
        FileUtil.deleteFileWithPath(filePath);
    }

    /**
     * 注销后，重新需要初始化的变量
     */
    public static void setInitData(Activity ac) {
        // 位置索引数据库表删除
        ////ClickIndexDBManager.Builder().deleteAll();
        ////DownData2DBManager.Builder().deleteAll();
        DownDataPatDBManager.Builder().deleteAll();
        //ShareMomentEngine.clearSharePosition();
        ProgressHelp.clearProgress();

        SZPathUtil.destoryFilePath();
        DRMDBHelper.destroyDBHelper();
        ClearKeyUtil.removeUserKey();
        ClearKeyUtil.removeWXKey();
        if (SZConfig.LoginConfig.type == DrmPat.LOGIN_QQ) {
            ClearKeyUtil.removeQQKey();
            Tencent tencent = Tencent.createInstance(SZConfig.QQ_APPID, ac.getApplicationContext());
            tencent.logout(ac);
        }
    }

}
