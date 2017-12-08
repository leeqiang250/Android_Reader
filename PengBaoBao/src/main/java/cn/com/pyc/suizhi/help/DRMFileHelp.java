package cn.com.pyc.suizhi.help;

import com.sz.mobilesdk.authentication.SZContent;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.SZLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.util.SZPathUtil;


/**
 * Created by hudq on 2017/3/24.
 */

public class DRMFileHelp {

    /**
     * 获取播放的位置
     *
     * @param currentFileId
     * @param files
     * @return
     */
    public static int getStartPosition(String currentFileId, List<DrmFile> files) {
        if (files == null) {
            return -1;
        }
        int startPos = 0, size = files.size();
        for (int i = 0; i < size; i++) {
            DrmFile file = files.get(i);
            if (currentFileId.equals(file.getFileId())) {
                SZLog.v("","fileName[" + i + "]: " + file.getFileName());
                startPos = i;
                break;
            }
        }
        return startPos;
    }

    /**
     * 获取播放DrmFile文件(已包括权限)
     *
     * @param ac
     */
    public static DrmFile convert2DrmFile(AlbumContent ac) {
        return convert2DrmFile(ac, "");
    }

    /**
     * 获取播放DrmFile文件(已包括权限)
     *
     * @param ac
     * @param productUrl
     * @return
     */
    public static DrmFile convert2DrmFile(AlbumContent ac, String productUrl) {
        String myProId = ac.getMyProId();
        String contentId = ac.getContent_id();
        String extName = ac.getFileType();
        StringBuilder builder = new StringBuilder();
        String filePath = builder.append(SZPathUtil.getFilePrefixPath())
                .append("/")
                .append(myProId)
                .append("/")
                .append(contentId)
                .append(".")
                .append(extName.toLowerCase(Locale.getDefault())).toString();
        // String[] _pid1 = { "_id" };
        // String[] _pidvalue1 = { ac.getAsset_id() };
        // Asset asset = (Asset) AssetDAOImpl.getInstance()
        // .findByQuery(_pid1, _pidvalue1, Asset.class).get(0);
        String assetId = ac.getAsset_id();
        SZContent szCont = new SZContent(assetId);
        String validityTime = FormatterUtil.getLeftAvailableTime(szCont.getAvailbaleTime());
        String odd_datetime_end = FormatterUtil.getToOddEndTime(szCont.getOdd_datetime_end());

        boolean checkOpen = szCont.checkOpen();
        String privateKey = checkOpen ? szCont.getCek_cipher_value() : "";
        DrmFile drmFile = new DrmFile();
        drmFile.setFileId(ac.getContent_id());
        drmFile.setLyricId(ac.getMusicLrcId());
        drmFile.setFileName(ac.getName());
        drmFile.setFilePath(filePath);
        drmFile.setFileFormat(extName);
        drmFile.setFileSize(ac.getContentSize());
        drmFile.setPrivateKey(privateKey);

        drmFile.setValidityTime(validityTime);
        drmFile.setEndDatetime(odd_datetime_end);
        drmFile.setCheckOpen(checkOpen);
        drmFile.setInEffective(szCont.isInEffective());

        drmFile.setAssetId(assetId);
        drmFile.setMyProductId(myProId);
        drmFile.setProductUrl(productUrl);
        drmFile.setCollectionId(ac.getCollectionId());
        return drmFile;
    }

    public static List<DrmFile> convert2DrmFileList(List<AlbumContent> contents) {
        return convert2DrmFileList(contents, "");
    }

    /**
     * 获取DrmFile文件集合
     *
     * @param contents
     * @param productUrl
     * @return
     */
    public static List<DrmFile> convert2DrmFileList(List<AlbumContent> contents, String
            productUrl) {
        List<DrmFile> files = new ArrayList<>();
        for (AlbumContent content : contents) {
            files.add(convert2DrmFile(content, productUrl));
        }
        return files;
    }
}
