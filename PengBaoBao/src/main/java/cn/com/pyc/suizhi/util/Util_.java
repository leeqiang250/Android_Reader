package cn.com.pyc.suizhi.util;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.UIHelper;

import java.util.List;

import cn.com.pyc.pbb.R;
import cn.com.pyc.suizhi.model.FileData;

/**
 * Created by hudaqiang on 2017/8/24.
 */

public class Util_ {

//    /**
//     * 检查登录
//     *
//     * @return
//     */
//    public static boolean checkLogin() {
//        String accountId = (String) SPUtil.get(KeyHelp.KEY_ACCOUNT_ID, "");
//        String token = (String) SPUtil.get(KeyHelp.KEY_SUPER_TOKEN, "");
//        return !TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token);
//    }

    /**
     * 获取位置
     *
     * @param contentId
     * @param files
     * @return
     */
    public static int getContentIndex(String contentId, List<AlbumContent> files) {
        int startPos = 0;
        if (contentId == null) return startPos;
        contentId = contentId.replaceAll("\"", "");
        for (int i = 0, size = files.size(); i < size; i++) {
            AlbumContent ac = files.get(i);
            if (contentId.equals(ac.getContent_id())) {
                SZLog.d("fileName[" + i + "]: " + ac.getName());
                startPos = i;
                break;
            }
        }
        return startPos;
    }

    public static int getFileIndex(String itemId, List<FileData> files) {
        int startPos = -1;
        if (itemId == null) return startPos;
        int size = files.size();
        for (int i = 0; i < size; i++) {
            FileData data = files.get(i);
            if (itemId.equals(data.getItemId())) {
                startPos = i;
                break;
            }
        }
        return startPos;
    }

    /**
     * 检查文件是否存在本地
     *
     * @param ac
     * @param filePath
     * @param callback
     */
    public static boolean checkFileExist(Context ac, String filePath,
                                         UIHelper.DialogCallBack callback) {
        boolean exist = true;
        if (!FileUtil.checkFilePathExists(filePath)) {
            exist = false;
            SZLog.d("文件不存在");
            UIHelper.showSingleCommonDialog(ac, null,
                    ac.getString(R.string.fail_to_open_file),
                    ac.getString(R.string.close), callback);
        }
        return exist;
    }

    /**
     * 设置listview的EmptyView，在加载数据为空或没有数据的时候使用
     *
     * @param lv
     * @param emptyView
     * @param tips
     */
    public static void setEmptyViews(ListView lv, View emptyView, String tips) {
        if (lv == null)
            return;

        TextView tipTextView = ((TextView) emptyView.findViewById(R.id.vep_txt_bulletin));
        tipTextView.setTextColor(Color.parseColor("#666666"));
        tipTextView.setText(tips);
        lv.setEmptyView(emptyView);
    }


    /*
     * 重复登录了，token验证失败/登录已过期
     */
//    @Deprecated
//    public static void repeatLogin(Activity context) {
//        if (MusicMode.STATUS != MusicMode.Status.STOP) {
//            MusicHelp.release(context);
//        }
//        DRMDBHelper.setInitData(context);
//        Bundle bundle = new Bundle();
//        bundle.putBoolean(KeyHelp.KEY_REPEAT_LOGIN, true);
//        OpenPageUtil.openActivity(context, LoginActivity.class, bundle);
//        context.finish();
//    }
}
