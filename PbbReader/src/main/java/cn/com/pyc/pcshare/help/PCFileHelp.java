package cn.com.pyc.pcshare.help;

import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SZLog;

import java.io.File;

/**
 * Created by hudq on 2017/2/10.
 */

public class PCFileHelp {

    public static String getPCShareOffset() {
        return "/PBBReader/PCShare/";
    }

    public static boolean deleteFile(String fileName) {
        String filePath = PathUtil.getSDCard() + getPCShareOffset() + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            SZLog.v("删除PCShare临时文件", filePath);
            return file.delete();
        }
        return false;
    }
}
