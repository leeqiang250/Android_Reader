package cn.com.pyc.suizhi.help;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;

import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.util.SZLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.model.FileData;

/**
 * 数据加载管理帮助类
 * <p>
 * Created by hudq on 2017/4/6.
 */

public class LoadDataHelp {

    private String myProductId;
    private String myProductUrl;
    private String curFileId;
    private List<FileData> dataList;
    private ExecHandler mHandler;


    public void load(OnLoadDataListener listener) {
        if (mHandler != null && mHandler.hasMessages(1000)) {
            mHandler.removeMessages(1000);
            mHandler = null;
        }
        mHandler = new ExecHandler(this, listener);
        ExecutorManager.getInstance().execute(new LoadDataRunnable(myProductId, myProductUrl,
                curFileId, dataList));
    }

    private LoadDataHelp(String myProductId,
                         String myProductUrl,
                         String curFileId,
                         List<FileData> dataList) {
        this.myProductId = myProductId;
        this.myProductUrl = myProductUrl;
        this.curFileId = curFileId;
        this.dataList = dataList;
    }

    public static class Builder {
        private String myProductId;
        private String myProductUrl;
        private String curFileId;
        private List<FileData> dataList;

        public Builder setMyProductId(String myProductId) {
            this.myProductId = myProductId;
            return this;
        }

        public Builder setMyProductUrl(String myProductUrl) {
            if (myProductUrl == null) myProductUrl = "";
            this.myProductUrl = myProductUrl;
            return this;
        }

        public Builder setCurrentFileId(String curFileId) {
            this.curFileId = curFileId;
            return this;
        }

        public Builder setDateList(List<FileData> dataList) {
            this.dataList = dataList;
            return this;
        }

        public LoadDataHelp init() {
            return new LoadDataHelp(myProductId, myProductUrl, curFileId, dataList);
        }

    }

    private static class ExecHandler extends Handler {
        private WeakReference<LoadDataHelp> reference;
        private OnLoadDataListener mListener;

        private ExecHandler(LoadDataHelp activity, OnLoadDataListener listener) {
            reference = new WeakReference<LoadDataHelp>(activity);
            mListener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            LoadDataHelp ref = reference.get();
            if (ref == null) return;
            if (msg.what != 1000)
                throw new IllegalArgumentException("msg 'what' error.");

            Bundle data = msg.getData();
            List<DrmFile> drmFiles = data.getParcelableArrayList("ldh.content");
            int pos = data.getInt("ldh.pos");
            if (mListener != null && drmFiles != null) {
                SZLog.i("count = " + drmFiles.size() + "; " + drmFiles.toString());
                mListener.onLoadSuccess(drmFiles, pos);
            }
        }
    }

    private class LoadDataRunnable implements Runnable {
        private String myProductId;
        private String myProductUrl;
        private String curFileId;
        private List<FileData> dataList;

        private LoadDataRunnable(String myProductId, String myProductUrl,
                                 String curFileId, List<FileData> dataList) {
            this.myProductId = myProductId;
            this.myProductUrl = myProductUrl;
            this.curFileId = curFileId;
            this.dataList = dataList;
        }

        @Override
        public void run() {
            List<AlbumContent> contents = AlbumContentDAOImpl.getInstance()
                    .findAlbumContentByMyProId(myProductId);

            if (dataList != null) {
                contents = sortOutPutData(dataList, contents);
            }

            List<DrmFile> mDrmFiles = DRMFileHelp.convert2DrmFileList(contents, myProductUrl);
            int mCurrentPos = DRMFileHelp.getStartPosition(curFileId, mDrmFiles);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("ldh.content", (ArrayList<? extends Parcelable>)
                    mDrmFiles);
            bundle.putInt("ldh.pos", mCurrentPos);
            Message msg = mHandler.obtainMessage(1000);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    /**
     * 将查询的数据按照指定的列表顺序输出
     *
     * @param srcData   源数据，即列表
     * @param inPutData 输入数据
     */
    public static List<AlbumContent> sortOutPutData(List<FileData> srcData,
                                                    List<AlbumContent> inPutData) {
        int srcSize = srcData.size(), outPutSize = inPutData.size();
        List<AlbumContent> targets = new ArrayList<>();
        for (int i = 0; i < srcSize; i++) {
            String fileId = srcData.get(i).getItemId();
            for (int j = 0; j < outPutSize; j++) {
                AlbumContent ac = inPutData.get(j);
                if (fileId != null && TextUtils.equals(fileId, ac.getContent_id())) {
                    targets.add(ac);
                }
            }
        }
        return targets;
    }


    public interface OnLoadDataListener {
        void onLoadSuccess(List<DrmFile> drmFiles, int currentPosition);
    }


}
