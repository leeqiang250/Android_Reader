package com.sz.mobilesdk.database.practice;

import android.content.ContentValues;

import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;
import com.sz.mobilesdk.util.SZLog;

import net.sqlcipher.Cursor;

import java.util.ArrayList;
import java.util.List;

public class AlbumContentDAOImpl extends SZBaseDAOPracticeImpl<AlbumContent> implements
        AlbumContentDAO {

    private static AlbumContentDAOImpl acdi = null;

    public boolean cascadedDelete(String id) {
        return true;
    }

    public static AlbumContentDAOImpl getInstance() {
        if (acdi == null) {
            acdi = new AlbumContentDAOImpl();
        }
        return acdi;
    }

    private AlbumContent getAlbumContentByCursor(Cursor cursor) {
        AlbumContent ac = new AlbumContent();
        // ac.setId(cursor.getString(cursor.getColumnIndex("_id")));
        ac.setMyProId(cursor.getString(cursor.getColumnIndex("myProId")));
        ac.setName(cursor.getString(cursor.getColumnIndex("name"))
                .replaceAll("\"", ""));
        ac.setContent_id(cursor.getString(
                cursor.getColumnIndex("content_id")).replaceAll("\"", ""));
        ac.setAlbum_id(cursor.getString(cursor.getColumnIndex("album_id")));
        ac.setModify_time(cursor.getString(cursor
                .getColumnIndex("modify_time")));
        ac.setAsset_id(cursor.getString(cursor.getColumnIndex("asset_id")));
        ac.setFileType(cursor.getString(cursor.getColumnIndex("fileType")));
        ac.setCollectionId(cursor.getString(cursor.getColumnIndex("collectionId")));
        ac.setCurrentItemId(cursor.getString(cursor.getColumnIndex("currentItemId")));
        ac.setLatestItemId(cursor.getString(cursor.getColumnIndex("latestItemId")));
        ac.setMusicLrcId(cursor.getString(cursor.getColumnIndex("musicLrcId")));
        ac.setContentSize(cursor.getLong(cursor.getColumnIndex("contentSize")));
        return ac;
    }

    /**
     * 根据collectionId判断是否存在文件
     *
     * @param collectionId
     * @return
     */
    public boolean existAlbumContentById(String collectionId) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(
                    "SELECT content_id FROM AlbumContent WHERE collectionId=?",
                    new String[]{collectionId});
            return cursor.moveToNext();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * 是否存在数据
     *
     * @return
     */
    public boolean existAlbumContent() {
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(
                    "SELECT content_id FROM AlbumContent", null);
            return cursor.moveToNext();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * 根据文件id,查询文件
     *
     * @param content_id (老版本的contentId可能带引号)
     * @return
     */
    public AlbumContent findAlbumContentByContentId(String content_id) {
        AlbumContent ac = null;
        Cursor cursor = dbHelper
                .rawQuery(
                        "SELECT * FROM AlbumContent WHERE content_id=? OR content_id=?",
                        new String[]{content_id, "\"" + content_id + "\""});
        if (cursor.moveToNext()) {
            ac = new AlbumContent();
            // ac.setId(cursor.getString(cursor.getColumnIndex("_id")));
            ac.setMyProId(cursor.getString(cursor.getColumnIndex("myProId")));
            ac.setName(cursor.getString(cursor.getColumnIndex("name")));
            ac.setContent_id(cursor.getString(cursor
                    .getColumnIndex("content_id")));
            ac.setAlbum_id(cursor.getString(cursor.getColumnIndex("album_id")));
            ac.setModify_time(cursor.getString(cursor
                    .getColumnIndex("modify_time")));
            ac.setAsset_id(cursor.getString(cursor.getColumnIndex("asset_id")));
            ac.setFileType(cursor.getString(cursor.getColumnIndex("fileType")));
        }
        cursor.close();
        return ac;
    }

    /**
     * 是否存在此id的数据
     *
     * @param content_id
     * @return
     */
    public boolean existAlbumContentByContentId(String content_id) {
        Cursor cursor = null;
        try {
            cursor = dbHelper
                    .rawQuery(
                            "SELECT * FROM AlbumContent WHERE content_id=? OR content_id=?",
                            new String[]{content_id, "\"" + content_id + "\""});
            if (cursor.moveToNext()) {
                SZLog.i("existAlbumContentByContentId", "exist albumContent: "
                        + cursor.getString(1));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    /**
     * 根据专辑id,查询所有文件
     *
     * @param myProId 即文件夹的id
     * @return
     */
    public List<AlbumContent> findAlbumContentByMyProId(String myProId) {
        List<AlbumContent> contents = new ArrayList<>();
        Cursor cursor = dbHelper.rawQuery(
                "SELECT * FROM AlbumContent WHERE myProId=?",
                new String[]{myProId});
        while (cursor.moveToNext()) {
            AlbumContent ac = new AlbumContent();
            // ac.setId(cursor.getString(cursor.getColumnIndex("_id")));
            ac.setMyProId(cursor.getString(cursor.getColumnIndex("myProId")));
            ac.setName(cursor.getString(cursor.getColumnIndex("name")));
            ac.setContent_id(cursor.getString(cursor
                    .getColumnIndex("content_id")));
            ac.setAlbum_id(cursor.getString(cursor.getColumnIndex("album_id")));
            ac.setModify_time(cursor.getString(cursor
                    .getColumnIndex("modify_time")));
            ac.setAsset_id(cursor.getString(cursor.getColumnIndex("asset_id")));
            ac.setFileType(cursor.getString(cursor.getColumnIndex("fileType")));
            contents.add(ac);
        }
        cursor.close();
        return contents;
    }

    /**
     * 更新albumContent内容
     *
     * @param ac
     * @return
     */
    public int updateAlbumContent(AlbumContent ac) {
        String content_id = ac.getContent_id();
        ContentValues values = new ContentValues();
        values.put("myProId", ac.getMyProId());
        // values.put("fileType", ac.getFileType());
        return dbHelper.update(AlbumContent.class.getSimpleName(), values,
                "content_id=?", new String[]{content_id});
    }

    /**
     * 删除对应id的文件
     *
     * @param contentId
     */
    public void deleteAlbumContentByContenId(String contentId) {
        int result = dbHelper.getDB().delete(
                AlbumContent.class.getSimpleName(),
                "content_id=? OR content_id=?",
                new String[]{contentId, "\"" + contentId + "\""});
        SZLog.w("deleteAlbumContentByContenId，result = " + result);
    }

    /**
     * 查询下载文件的id
     */
    public List<String> findContentIdByMyProId(String myProId) {
        List<String> contentIds = new ArrayList<>();
        Cursor cursor = dbHelper.rawQuery(
                "SELECT content_id FROM AlbumContent WHERE myProId=?",
                new String[]{myProId});
        while (cursor.moveToNext()) {
            contentIds
                    .add(cursor.getString(cursor.getColumnIndex("content_id")));
        }
        cursor.close();
        return contentIds;
    }

    /**
     * 根据content_id删除
     *
     * @param content_id
     */
    public void deleteAlbumContentByContentId(String content_id) {
        dbHelper.getDB().execSQL(
                "DELETE FROM AlbumContent WHERE content_id=? OR content_id=?",
                new String[]{content_id, "\"" + content_id + "\""});
    }

    /**
     * 根据文件的集合id,查询文件
     *
     * @param collectionId
     * @return
     */
    public AlbumContent findAlbumContentByCollectionId(String collectionId) {
        AlbumContent ac = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(
                    "SELECT * FROM AlbumContent WHERE collectionId=?",
                    new String[]{collectionId});
            if (cursor.moveToNext()) {
                ac = getAlbumContentByCursor(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ac;
    }

    /**
     * 更新collectionId,lrcId 等
     *
     * @return
     */
    public int updateAlbumContentByItemId(String itemId, String collectionId, String lrcId,
                                          String myProId, long contentSize) {
        ContentValues values = new ContentValues();
        values.put("collectionId", collectionId);
        values.put("musicLrcId", lrcId);
        values.put("myProId", myProId);
        values.put("contentSize", contentSize);
        return dbHelper.update(AlbumContent.class.getSimpleName(), values,
                "content_id=?", new String[]{itemId});
    }

    /**
     * 根据albumId删除AlbumContent
     *
     * @param CollectionId
     */
    public void deleteAlbumContentByCollectionId(String CollectionId) {
        dbHelper.DeleteAlbumContentByCollectionId(CollectionId);
    }


}
