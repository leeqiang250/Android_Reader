package cn.com.pyc.suizhi.util;

import android.content.Context;

import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.bean.Perconattribute;
import com.sz.mobilesdk.database.bean.Perconstraint;
import com.sz.mobilesdk.database.bean.Permission;
import com.sz.mobilesdk.database.bean.Right;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.database.practice.AlbumDAOImpl;
import com.sz.mobilesdk.database.practice.RightDAOImpl;
import com.sz.mobilesdk.models.xml.OEX_Agreement;
import com.sz.mobilesdk.models.xml.OEX_Rights;
import com.sz.mobilesdk.models.xml.XML2JSON_Album;
import com.sz.mobilesdk.util.FileUtil;

import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LogerHelp;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.LogConfig;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.manager.ParserEngine;
import cn.com.pyc.suizhi.model.FileData;

/**
 * 解析文件
 */
public class ParserDRMUtil {

    private final String TAG = "ParserUtil";

    public static volatile ReentrantLock sLock;

    private ParserDRMUtil() {
    }

    private static class ParserDRMUtilInner {
        private static final ParserDRMUtil INSTANCE = new ParserDRMUtil();
    }

    public static ParserDRMUtil getInstance() {
        if (sLock == null) {
            sLock = new ReentrantLock(true);
        }
        return ParserDRMUtilInner.INSTANCE;
    }

    private void lock() {
        if (sLock != null) {
            sLock.lock();
        }
    }

    private void unlock() {
        if (sLock != null) {
            sLock.unlock();
            sLock = null;
        }
    }

    /**
     * 新的解析下载DRM文件（证书分离后）---------建议非UI线程执行调用；
     *
     * @param o
     */
    public List<cn.com.pyc.suizhi.manager.ParserEngine.CommonFile> parserDRMFileUnique(FileData
                                                                                               o)
            throws Exception {
        // 下载文件路径
        final String drmPath = new StringBuilder()
                .append(SZPathUtil.getDRMPrefixPath())
                .append(File.separator)
                .append(o.getItemId())
                .append(DrmPat._DRM).toString();
        // 解析后文件保存路径
        final String decodePath = new StringBuilder()
                .append(SZPathUtil.getFilePrefixPath())
                .append(File.separator)
                .append(o.getMyProId()).toString();
        FileUtil.createDirectory(drmPath);
        FileUtil.createDirectory(decodePath);
        return cn.com.pyc.suizhi.manager.ParserEngine.parserDRMFile2(drmPath, decodePath);
    }

    /**
     * 解析下载AlbumInfo文件（证书分离后）---------建议非UI线程执行调用；
     *
     * @param list
     * @return
     */
    public XML2JSON_Album parserAlbumInfoUnique(List<cn.com.pyc.suizhi.manager.ParserEngine
            .CommonFile> list) throws Exception {
        if (list == null || list.isEmpty())
            return null;
        XML2JSON_Album albumInfo = null;
        for (cn.com.pyc.suizhi.manager.ParserEngine.CommonFile c : list) {
            if (c != null) {
                if (c.filetype == cn.com.pyc.suizhi.manager.ParserEngine.CommonFile.FILETYPE
                        .ALBUMINFO) {
                    albumInfo = cn.com.pyc.suizhi.manager.ParserEngine.parserAlbumInfo2(new File
                            (c.filepath), list);
                }
            }
        }
        return albumInfo;
    }

    /**
     * 解析证书 （证书分离后）---------建议非UI线程执行调用；
     *
     * @param o
     * @param xmlStream 字符串的证书信息
     * @return
     */
    public OEX_Rights parserRightUnique(FileData o, String xmlStream) {
        final String rightPath = new StringBuilder()
                .append(SZPathUtil.getFilePrefixPath())
                .append(File.separator)
                .append(o.getMyProId()).toString();
        FileUtil.createDirectory(rightPath);

        String fileName = rightPath + File.separator + o.getItemId() + DrmPat._XML;
        File file = FileUtil.writeTextToFile(fileName, xmlStream);
        return cn.com.pyc.suizhi.manager.ParserEngine.parserRight(file);
    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    /*
     * 解析下载的文件---------非UI线程执行调用；
     * <p>
     * 已废弃使用
     *
     * @param context
     * @param o       FileData
     */
//    @Deprecated
//    public void parserDRMFile(Context context, FileData o) {
//        context.sendBroadcast(new Intent(DownloadService.ACTION_PARSERING)
//                .putExtra("FileData", o));
//
//        DRMLog.d(TAG, "start parser file: " + o.getContent_name());
//        // 下载文件路径
//        final String drmPath = new StringBuilder()
//                .append(PathUtil.getDRMPrefixPath())
//                .append("/")
//                .append(o.getItemId())
//                .append(DrmPat._DRM).toString();
//        // 解析后文件保存路径
//        final String decodePath = new StringBuilder()
//                .append(PathUtil.getFilePrefixPath())
//                .append("/")
//                .append(o.getMyProId()).toString();
//        try {
//            // 创建解压目录
//            FileUtils.createDirectory(drmPath);
//            FileUtils.createDirectory(decodePath);
//
//            lock();
//            List<CommonFile> list = ParserEngine.parserDRMFile(drmPath, decodePath);
//            if (list == null) {
//                addLog(context, "List<CommonFile> maybe null, parserDRMFile failed.");
//                return;
//            }
//            XML2JSON_Album albumInfo = null;
//            OEX_Rights rights = null;
//            for (ParserEngine.CommonFile c : list) {
//                if (c != null) {
//                    if (c.filetype == CommonFile.FILETYPE.ALBUMINFO) {
//                        albumInfo = ParserEngine.parserAlbumInfo(new File(c.filepath), list);
//
//                    } else if (c.filetype == CommonFile.FILETYPE.RIGHT) {
//                        rights = parserRight(new File(c.filepath));
//                    }
//                } else {
//                    addLog(context, "parser file failed! in List<CommonFile>,the value maybe
// null" +
//                            ".");
//                }
//            }
//            // 将专辑信息和权限插入表中
//            if (albumInfo != null && rights != null) {
//                if (insertFileData(rights, albumInfo, list, o)) {
//                    DRMLog.d(TAG, "insert success");
//                    for (ParserEngine.CommonFile c : list) {
//                        CommonFile.FILETYPE type = c.filetype;
//                        if (type == CommonFile.FILETYPE.ALBUMINFO
//                                || type == CommonFile.FILETYPE.RIGHT) {
//                            FileUtils.deleteFileWithPath(c.filepath);
//                        }
//                    }
//                    FileUtils.deleteFileWithPath(drmPath);
//                } else {
//                    DRMLog.d(TAG, "insert failed");
//                    context.sendBroadcast(new Intent(DownloadService.ACTION_ERROR).putExtra
//                            ("FileData", o));
//
//                    addLog(context, "insertFile DB failed! \n " + list.toString() + "\n" + o
//                            .toString());
//                }
//            } else {
//                DRMLog.i(context.getString(R.string.msg_cipher_error));
//                addLog(context, "parser drm failed! albumInfo or rights is null.");
//            }
//            // 解析完成！
//            Intent intent = new Intent(DownloadService.ACTION_FINISHED);
//            intent.putExtra("FileData", o);
//            context.sendBroadcast(intent);
//            DRMLog.d(TAG, "end parser file: " + o.getItemId());
//        } catch (Exception e) {
//            e.printStackTrace();
//            context.sendBroadcast(new Intent(DownloadService.ACTION_ERROR).putExtra("FileData",
//                    o));
//
//            addLog(context, "parser error: " + e.getMessage());
//        } finally {
//            unlock();
//        }
//    }

    private void addLog(Context ctx, String content) {
        ExtraParams params = LogConfig.getBaseExtraParams();
        params.file_name = LogerHelp.getFileName();
        LogerEngine.error(ctx, content, params);
    }

    /**
     * 插入单个下载文件所对应的内容-----建议非UI线程执行调用；
     *
     * @param rights    证书信息
     * @param albumInfo AlbumInfo.xml
     * @param mcFiles   List Files
     * @param o         FileData
     * @return Boolean
     */
    public boolean insertFileData(OEX_Rights rights,
                                  XML2JSON_Album albumInfo,
                                  List<cn.com.pyc.suizhi.manager.ParserEngine.CommonFile> mcFiles,
                                  FileData o) {
        boolean flag = false;
        final int commonSize = mcFiles.size();
        final String collectionId = o.getCollectionId();
        boolean exist = AlbumContentDAOImpl.getInstance().existAlbumContentById(collectionId);
        if (!exist) {  //不存在插入DB；貌似不需要此判断，待验证。
            try {
                Right right = new Right();
                long currentTime = System.currentTimeMillis();
                right.setId(String.valueOf(currentTime));
                right.setPro_album_id("0");
                String str_uid = rights.getContextMap().get("uid");
                right.setRight_uid(str_uid != null ? str_uid : "");
                String str_ver = rights.getContextMap().get("version");
                right.setRight_version(str_ver != null ? str_ver : "");
                DateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale
                        .getDefault());
                String date = sDateFormat.format(new java.util.Date());
                right.setCreate_time(date);
                right.setAccount_id("1");
                right.setUsername(SZConstant.getAccountId());//使用accountId替换username
                // 4、 插入asset内容
                List<OEX_Agreement.OEX_Asset> rightAssets = rights.getAgreement().getAssets();
                LinkedList<Asset> assets = new LinkedList<Asset>();
                int a_count = rightAssets.size();
                for (int i = 0; i < a_count; i++) {
                    OEX_Agreement.OEX_Asset oex_asset = rightAssets.get(i);
                    Asset asset = new Asset();
                    asset.setId(String.valueOf(currentTime + i));
                    asset.setRight_id(right.getId());
                    asset.setAsset_uid(oex_asset.getOdd_uid());
                    asset.setCek_cipher_value(oex_asset.getCipheralue());
                    asset.setCek_encrypt_method(oex_asset.getEnc_algorithm());
                    asset.setCek_retrieval_key(oex_asset.getRetrieval_url());
                    asset.setDigest_method(oex_asset.getDigest_algorithm_key());
                    asset.setDigest_value(oex_asset.getDigest_algorithm_value());
                    asset.setCreate_time(date);
                    asset.setRight_version(right.getRight_version());
                    asset.setUsername(right.getUsername());
                    assets.add(asset);
                }
                // 3、 插入权限表及约束内容
                List<OEX_Agreement.OEX_Permission> rightPermissions = rights.getAgreement()
                        .getPermission();
                int n = 0, p_count = rightPermissions.size();
                for (int i = 0; i < p_count; i++) {
                    OEX_Agreement.OEX_Permission oex_permission = rightPermissions.get(i);
                    Permission permission = new Permission();
                    permission.setId(String.valueOf(currentTime + i));
                    permission.setExpired("0");   //标示意义：有效，未过期
                    int assetId = Integer.parseInt(oex_permission.getAssent_id().substring(5)) - 1;
                    //String assentId = assets.get(assetId).getId();
                    permission.setAsset_id(assets.get(assetId).getId());
                    permission.setCreate_time(date);
                    permission.setElement(String.valueOf(oex_permission.getType()));
                    List<Map<String, String>> attributes = oex_permission.getAttributes();
                    for (Map<String, String> map : attributes) {
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            n++;
                            Perconstraint perconstraint = new Perconstraint();
                            perconstraint.setId(String.valueOf(currentTime + n));
                            perconstraint.setElement(entry.getKey());
                            if ("datetime".equals(entry.getKey())) {
                                Perconattribute startPerconattribute = new Perconattribute();
                                Perconattribute endPerconattribute = new Perconattribute();
                                String start = rightPermissions.get(i).getStartTime();
                                String end = rightPermissions.get(i).getEndTime();
                                startPerconattribute.setElement("start");
                                startPerconattribute.setValue(start);
                                startPerconattribute.setCreate_time(date);
                                startPerconattribute.setPerconstraint_id(perconstraint.getId());
                                endPerconattribute.setElement("end");
                                endPerconattribute.setValue(end);
                                endPerconattribute.setCreate_time(date);
                                endPerconattribute.setPerconstraint_id(perconstraint.getId());
                                perconstraint.addPerconattributes(startPerconattribute);
                                perconstraint.addPerconattributes(endPerconattribute);
                            }
                            perconstraint.setPermission_id(permission.getId());
                            perconstraint.setValue(entry.getValue());
                            perconstraint.setCreate_time(date);
                            permission.addPerconstraint(perconstraint);
                        }
                    }
                    assets.get(assetId).addPermission(permission);
                }
                right.setAssets(assets);
                RightDAOImpl.getInstance().cascadedSave(right);

                //2、插入专辑， jsonObj变成局部变量
                Album saveAlbum = AlbumDAOImpl.getInstance().findAlbumByMyProId(o.getMyProId());
                if (saveAlbum == null) {
                    JSONObject albObject = albumInfo.getInfoObj();
                    Album album = new Album();
                    album.setId(String.valueOf(currentTime));
                    album.setName(albObject.optString("albumName"));
                    album.setRight_id(albObject.optString("rid"));
                    album.setProduct_id(albObject.optString("albumId"));
                    album.setModify_time(date);
                    album.setCategory(albObject.optString("albumCategory"));
                    album.setItem_number(String.valueOf(albumInfo.getContentList().size() / 2));
                    album.setUsername(SZConstant.getAccountId()); //使用accountId替换username
                    album.setPicture(albObject.optString("picture"));
                    album.setMyproduct_id(o.getMyProId());
                    album.setAuthor(o.getAuthors() == null ? "" : o.getAuthors());
                    album.setPicture_ratio(o.getPicture_ratio() == null ? "1" : o
                            .getPicture_ratio());
                    // 附加字段，默认""
                    album.setPublishDate("");
                    //album.setSave_Last_add_time("");
                    //album.setSave_Last_modify_time("");
                    AlbumDAOImpl.getInstance().save(album);
                    saveAlbum = album;  //没有查询到专辑数据，说明没有下载过文件
                }

                //1、插入专辑内的文件列表
                List<String> contents = albumInfo.getContentList();
                final int contentNum = contents.size();
                final int assetNum = assets.size();
                for (int i = 0; i < contentNum; i += 2) {
                    AlbumContent albumContent = new AlbumContent();
                    for (int j = 0; j < assetNum; j++) {
                        String content_id = contents.get(i).replace("\"", "");
                        String odd_uid = assets.get(j).getAsset_uid();
                        if (content_id.equals(odd_uid)) {
                            albumContent.setMyProId(saveAlbum.getMyproduct_id());
                            albumContent.setAlbum_id(saveAlbum.getId());
                            albumContent.setModify_time(date);
                            albumContent.setName(contents.get(i + 1).replaceAll("\"", ""));
                            albumContent.setAsset_id(assets.get(j).getId());
                            albumContent.setContent_id(content_id);
                            //新增字段，update 20170216
                            albumContent.setCollectionId(collectionId);
                            albumContent.setContentSize(o.getContent_size());
                            albumContent.setCurrentItemId(o.getCurrentItemId());
                            albumContent.setLatestItemId(o.getLatestItemId());
                            albumContent.setMusicLrcId(o.getMusicLyric_id() == null ? "" : o
                                    .getMusicLyric_id());
                            // 设置文件类型
                            for (int k = 0; k < commonSize; k++) {
                                ParserEngine.CommonFile file = mcFiles.get(k);
                                String fileName = file.filename;
                                String name = FileUtil.getNameFromFileName(fileName);
                                if (content_id.equals(name)) {
                                    String extName = String.valueOf(file.filetype);
                                    albumContent.setFileType(extName == null ? "" : extName);
                                }
                            }
                            //album.addAlbumContent(albumContent);
                            AlbumContentDAOImpl.getInstance().save(albumContent);
                        }
                    }
                }
                //saveAlbumContentHistory(o);
                //AlbumDAOImpl.getInstance().cascadedSave(album);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
                ExtraParams params = LogConfig.getBaseExtraParams();
                params.file_name = LogerHelp.getFileName();
                params.lines = LogerHelp.getLineNumber();
                LogerEngine.debug(SZApplication.getInstance(), "insert failed：[" + o.toString() +
                        "]" + e.getMessage(), params);
            }
        }
        return flag;
    }

    /*
     * 保存下载更新的文件记录
     *
     * @param o
     */
//    private void saveAlbumContentHistory(FileData o) {
//        AlbumContentHistory a = new AlbumContentHistory();
//        a.setItemId(o.getItemId());
//        a.setCollectionId(o.getCollectionId());
//        a.setContent_name(o.getContent_name());
//        a.setContent_size(o.getContent_size());
//        a.setContent_format(o.getContent_format());
//        a.setPage_num(o.getPage_num());
//        a.setLength(o.getLength());
//        a.setVersion(o.getVersion());
//        a.setVersionInfo(StringUtil.isEmptyOrNull(o.getVersionInfo()) ? "" : o.getVersionInfo());
//        a.setPlay_progress(o.getPlay_progress());
//        AlbumContentHistoryDAOImpl.getInstance().save(a);
//    }

    /*
     * 解析下载的专辑---------非UI线程执行调用；
     *
     * 已废弃使用！
     *
     * @param context
     * @param myProId
     * @param position
     * @param o
     */
//    @Deprecated
//    public void parserDRM(Context context, final String myProId, final int position, ProductInfo
//            o) {
//        DRMLog.d(TAG, "start parser: " + o.getProductName());
//        // 下载文件路径
//        final String drmPath = new StringBuilder().append(PathUtil.getDRMPrefixPath()).append("/")
//                .append(myProId).append(DrmPat._DRM).toString();
//        // 解析后文件保存路径
//        final String decodePath = new StringBuilder().append(PathUtil.getFilePrefixPath()).append
//                ("/")
//                .append(myProId).toString();
//
//        try {
//            // 创建解压目录
//            FileUtils.createDirectory(drmPath);
//            FileUtils.createDirectory(decodePath);
//
//            lock();
//            List<CommonFile> list = ParserEngine.parserDRMFile(drmPath, decodePath);
//            XML2JSON_Album albumInfo = null;
//            OEX_Rights rights = null;
//            for (CommonFile c : list) {
//                if (c.filetype == CommonFile.FILETYPE.ALBUMINFO) {
//                    albumInfo = ParserEngine.parserAlbumInfo(new File(c.filepath), list);
//
//                } else if (c.filetype == CommonFile.FILETYPE.RIGHT) {
//                    rights = parserRight(new File(c.filepath));
//                }
//            }
//            String author = o.getAuthors();
//            String picture_ratio = o.getPicture_ratio();
//            String saveLastaddtime = o.getLast_add_time();
//            String saveLastmodifytime = o.getLast_modify_time();
//
//            // 将专辑信息和权限插入表中
//            if (albumInfo != null && rights != null) {
//                if (!insertDRMData(rights, albumInfo, list, myProId, author, picture_ratio,
//                        saveLastaddtime, saveLastmodifytime)) {
//                    // 插入失败
//                    DRMLog.i("insert data failed");
//                } else {
//                    // 插入成功
//                    DRMLog.i("insert success");
//                    for (CommonFile c : list) {
//                        if (c.filetype == CommonFile.FILETYPE.ALBUMINFO) {
//                            FileUtils.deleteFileWithPath(c.filepath);
//                        } else if (c.filetype == CommonFile.FILETYPE.RIGHT) {
//                            FileUtils.deleteFileWithPath(c.filepath);
//                        }
//                    }
//                    FileUtils.deleteFileWithPath(drmPath);
//                }
//            }
//            Intent intent = new Intent(DRMUtil.BROADCAST_PARSER_OVER_RELOAD);
//            intent.putExtra("position", position);
//            context.sendBroadcast(intent);
//            DRMLog.d(TAG, "end parser drm : " + myProId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            unlock();
//        }
//    }

    /*
     * 插入整个drm专辑所对应的内容
     *
     * 已废弃使用！
     *
     * @param rights
     * @param albumInfo
     * @param mcFiles
     * @param myProId
     * @param author
     * @param picture_ratio
     * @param saveLastaddtime
     * @param saveLastmodifytime
     * @return
     */
//    @Deprecated
//    private boolean insertDRMData(OEX_Rights rights, XML2JSON_Album albumInfo, List<CommonFile>
//            mcFiles, String myProId, String author, String picture_ratio, String saveLastaddtime,
//                                  String saveLastmodifytime) {
//        DRMUtil.isInsertDRMData = true;
//        final List<CommonFile> mCommonFiles = mcFiles;
//        String Album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProId);// 获取专辑ID
//        if (Album_Id == null && DRMUtil.isInsertDRMData) {
//            try {
//                DRMUtil.isInsertDRMData = false;
//                Right right = new Right();
//                long currentTime = System.currentTimeMillis();
//                right.setId(String.valueOf(currentTime));
//                right.setPro_album_id("0");
//                right.setRight_uid(rights.getContextMap().get("uid"));
//                right.setRight_version(rights.getContextMap().get("version"));
//                DateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale
//                        .getDefault());
//                String date = sDateFormat.format(new java.util.Date());
//                right.setCreate_time(date);
//                right.setAccount_id("1");
//                right.setUsername(Constant.getUserName());
//                // 4、 插入asset内容
//                List<OEX_Asset> rightAssets = rights.getAgreement().getAssets();
//                LinkedList<Asset> assets = new LinkedList<Asset>();
//                for (int i = 0, count = rightAssets.size(); i < count; i++) {
//                    Asset asset = new Asset();
//                    asset.setId(String.valueOf(currentTime + i));
//                    asset.setAsset_uid(rightAssets.get(i).getOdd_uid());
//                    asset.setRight_id(right.getId());
//                    asset.setCek_cipher_value(rightAssets.get(i).getCipheralue());
//                    asset.setCek_encrypt_method(rightAssets.get(i).getEnc_algorithm());
//                    asset.setCek_retrieval_key(rightAssets.get(i).getRetrieval_url());
//                    asset.setDigest_method(rightAssets.get(i).getDigest_algorithm_key());
//                    asset.setDigest_value(rightAssets.get(i).getDigest_algorithm_value());
//                    asset.setCreate_time(date);
//                    asset.setRight_version(right.getRight_version());
//                    asset.setUsername(right.getUsername());
//                    assets.add(asset);
//                }
//                int n = 0;
//                // 3、 插入权限表及约束内容
//                List<OEX_Permission> rightPermissions = rights.getAgreement().getPermission();
//                for (int i = 0, count = rightPermissions.size(); i < count; i++) {
//                    Permission permission = new Permission();
//                    permission.setId(String.valueOf(currentTime + i));
//                    int assetId = Integer.parseInt(rightPermissions.get(i).getAssent_id()
//                            .substring(5)) - 1;
//                    String assentId = assets.get(assetId).getId();
//                    permission.setAsset_id(assentId);
//                    permission.setCreate_time(date);
//                    permission.setElement(String.valueOf(rightPermissions.get(i).getType()));
//                    List<Map<String, String>> attributes = rightPermissions.get(i)
// .getAttributes();
//                    for (Map<String, String> map : attributes) {
//                        for (Map.Entry<String, String> entry : map.entrySet()) {
//                            n++;
//                            Perconstraint perconstraint = new Perconstraint();
//                            perconstraint.setId(String.valueOf(currentTime + n));
//                            perconstraint.setElement(entry.getKey());
//                            if ("datetime".equals(entry.getKey())) {
//                                Perconattribute startPerconattribute = new Perconattribute();
//                                Perconattribute endPerconattribute = new Perconattribute();
//                                String start = rightPermissions.get(i).getStartTime();
//                                String end = rightPermissions.get(i).getEndTime();
//                                // DRMLog.e(TAG, "satrt-time: " + start);
//                                // DRMLog.e(TAG, "end-time: " + end);
//                                startPerconattribute.setElement("start");
//                                startPerconattribute.setValue(start);
//                                startPerconattribute.setCreate_time(date);
//                                startPerconattribute.setPerconstraint_id(perconstraint.getId());
//                                endPerconattribute.setElement("end");
//                                endPerconattribute.setValue(end);
//                                endPerconattribute.setCreate_time(date);
//                                endPerconattribute.setPerconstraint_id(perconstraint.getId());
//                                perconstraint.addPerconattributes(startPerconattribute);
//                                perconstraint.addPerconattributes(endPerconattribute);
//                            }
//                            perconstraint.setPermission_id(permission.getId());
//                            perconstraint.setValue(entry.getValue());
//                            perconstraint.setCreate_time(date);
//                            permission.addPerconstraint(perconstraint);
//                        }
//                    }
//                    assets.get(assetId).addPermission(permission);
//                }
//                right.setAssets(assets);
//                RightDAOImpl.getInstance().cascadedSave(right);
//
//                // jsonObj变成局部变量
//                JSONObject albObject = albumInfo.getInfoObj();
//                Album album = new Album();
//                album.setId(String.valueOf(currentTime));
//                album.setName(albObject.getString("albumName"));
//                album.setRight_id(albObject.getString("rid"));
//                album.setProduct_id(albObject.getString("albumId"));
//                album.setModify_time(date);
//                album.setCategory(albObject.getString("albumCategory"));
//                album.setItem_number(String.valueOf(albumInfo.getContentList().size() / 2));
//                album.setUsername(Constant.getUserName());
//                album.setPicture(albObject.getString("picture"));
//                album.setMyproduct_id(myProId);
//                album.setAuthor(author == null ? "DRM" : author);
//                album.setPicture_ratio(picture_ratio == null ? "1" : picture_ratio);
//                album.setPublishDate("");
//                album.setSave_Last_add_time(saveLastaddtime == null ? "" : saveLastaddtime);
//                album.setSave_Last_modify_time(saveLastmodifytime == null ? "" :
//                        saveLastmodifytime);
//                List<String> contents = albumInfo.getContentList();
//                for (int i = 0, count = contents.size(); i < count; i += 2) {
//                    AlbumContent albumContent = new AlbumContent();
//                    for (int j = 0; j < assets.size(); j++) {
//                        String Content_id = contents.get(i).replace("\"", "");
//                        String odduid = assets.get(j).getAsset_uid();
//                        if (Content_id.equals(odduid)) {
//                            albumContent.setMyProId(album.getMyproduct_id());
//                            albumContent.setAlbum_id(album.getId());
//                            albumContent.setModify_time(date);
//                            albumContent.setName(contents.get(i + 1).replaceAll("\"", ""));
//                            albumContent.setAsset_id(assets.get(j).getId());
//                            albumContent.setContent_id(Content_id);
//                            // 设置文件类型
//                            for (int k = 0, size = mCommonFiles.size(); k < size; k++) {
//                                CommonFile file = mCommonFiles.get(k);
//                                String fileName = file.filename;
//                                String name = FileUtils.getNameFromFileName(fileName);
//                                if (Content_id.equals(name)) {
//                                    String extName = String.valueOf(file.filetype);
//                                    DRMLog.v("ext:" + extName);
//                                    albumContent.setFileType(extName);
//                                }
//                            }
//                            album.addAlbumContent(albumContent);
//                        }
//                    }
//                }
//                DRMLog.d("insert", "album: " + album.toString());
//                AlbumDAOImpl.getInstance().cascadedSave(album);
//                DRMUtil.isInsertDRMData = true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                DRMUtil.isInsertDRMData = false;
//            }
//        }
//        return DRMUtil.isInsertDRMData;
//    }

}
