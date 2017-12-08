package cn.com.pyc.suizhi.manager.db;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

/**
 * 下载文件数据管理,断点下载: DownDataPat数据表
 */
public class DownDataPatDBManager {
    private DbManager dbManager;

    public static DownDataPatDBManager Builder() {
        return new DownDataPatDBManager();
    }

    private DownDataPatDBManager() {
        dbManager = x.getDb(DbConfig.daoConfig());
        DbConfig.createTableIfNotExist(dbManager, DownDataPat.class);
    }

    /**
     * 删除所有记录
     */
    public void deleteAll() {
        try {
            if (dbManager != null) {
                dbManager.delete(DownDataPat.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存或更新记录
     *
     * @param data
     */
    public void saveOrUpdate(DownDataPat data) {
        try {
            //dbManager.replace(data); // 没有就插入，有就更新
            dbManager.saveOrUpdate(data);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据fileId查询记录
     *
     * @param fileId
     * @return
     */
    public DownDataPat findByFileId(String fileId) {
        DownDataPat data = null;
        try {
            data = dbManager.selector(DownDataPat.class)
                    .where(WhereBuilder.b("fileId", "=", fileId)).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 存在数据
     *
     * @param fileId
     * @return
     */
    public boolean existData(String fileId) {
        return findByFileId(fileId) != null;
    }

    /**
     * 通过fileId删除记录
     *
     * @param fileId
     * @return
     */
    public int deleteByFileId(String fileId) {
        int result = -1;
        try {
            result = dbManager.delete(DownDataPat.class,
                    WhereBuilder.b("fileId", "=", fileId));
        } catch (DbException e) {
            e.printStackTrace();
        }
        return result;
    }

}
