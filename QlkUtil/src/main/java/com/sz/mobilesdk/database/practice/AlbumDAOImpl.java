package com.sz.mobilesdk.database.practice;

import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.dbBase.SZBaseDAOPracticeImpl;

import net.sqlcipher.Cursor;

public class AlbumDAOImpl extends SZBaseDAOPracticeImpl<Album> implements AlbumDAO {
    private static AlbumDAOImpl adi = null;

    public boolean cascadedDelete(String id) {
        return true;
    }

    public static AlbumDAOImpl getInstance() {
        if (adi == null) {
            adi = new AlbumDAOImpl();
        }
        return adi;
    }

//    private Album getAlbum(Cursor cursor) {
//        Album album = new Album();
//        album.setId(cursor.getString(cursor.getColumnIndex("_id")));
//        album.setCategory(cursor.getString(cursor.getColumnIndex("category")));
//        album.setItem_number(cursor.getString(cursor.getColumnIndex("item_number")));
//        album.setModify_time(cursor.getString(cursor.getColumnIndex("modify_time")));
//        album.setMyproduct_id(cursor.getString(cursor.getColumnIndex("myproduct_id")));
//        album.setName(cursor.getString(cursor.getColumnIndex("name")));
//        album.setPicture(cursor.getString(cursor.getColumnIndex("picture")));
//        album.setProduct_id(cursor.getString(cursor.getColumnIndex("product_id")));
//        album.setRight_id(cursor.getString(cursor.getColumnIndex("right_id")));
//        album.setUsername(cursor.getString(cursor.getColumnIndex("username")));
//        album.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
//        album.setPicture_ratio(cursor.getString(cursor.getColumnIndex("picture_ratio")));
//        album.setPublishDate(cursor.getString(cursor.getColumnIndex("publishDate")));
//        //album.setSave_Last_add_time(cursor.getString(cursor.getColumnIndex
//        // ("save_Last_add_time")));
//        //album.setSave_Last_modify_time(cursor.getString(cursor.getColumnIndex
//        // ("save_Last_modify_time")));
//        return album;
//    }

    /*
     * 根据myProId查询专辑
     *
     * @param myProId
     * @return
     */
//    public Album findAlbumByMyProId(String myProId) {
//        Album album = null;
//        Cursor cursor = null;
//        try {
//            cursor = dbHelper.rawQuery("SELECT * FROM Album WHERE myproduct_id=?",
//                    new String[]{myProId});
//            if (cursor.moveToNext()) {
//                album = getAlbum(cursor);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return album;
//    }

    /**
     * 根据MyProId查询专辑category
     *
     * @param myProId
     * @return
     */
    public String findAlbumCategoryByMyProId(String myProId) {
        String result = "";
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT category FROM Album WHERE myproduct_id=?",
                    new String[]{myProId});
            if (cursor.moveToNext()) {
                result = cursor.getString(cursor.getColumnIndex("category"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }



}
