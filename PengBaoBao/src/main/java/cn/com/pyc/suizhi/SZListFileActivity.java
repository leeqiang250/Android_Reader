package cn.com.pyc.suizhi;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.sz.mobilesdk.authentication.SZContent;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.database.practice.AlbumDAOImpl;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.widget.ToastShow;

import org.xutils.common.Callback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.pbbonline.util.SwipUtil;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.pbbonline.widget.PullListView;
import cn.com.pyc.suizhi.adapter.ListFileAdapter;
import cn.com.pyc.suizhi.adapter.ListFileLocalAdapter;
import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.bean.event.MusicCurrentPlayEvent;
import cn.com.pyc.suizhi.common.MusicMode;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.help.DRMDBHelper;
import cn.com.pyc.suizhi.help.DRMFileHelp;
import cn.com.pyc.suizhi.help.DownloadHelp;
import cn.com.pyc.suizhi.help.MusicHelp;
import cn.com.pyc.suizhi.help.ProgressHelp;
import cn.com.pyc.suizhi.manager.DownloadTaskManagerPat;
import cn.com.pyc.suizhi.manager.HttpEngine;
import cn.com.pyc.suizhi.manager.LrcEngine;
import cn.com.pyc.suizhi.manager.PromptMsgManager;
import cn.com.pyc.suizhi.manager.db.DownDataPatDBManager;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.model.FileVersionModel;
import cn.com.pyc.suizhi.model.FilesDataModel;
import cn.com.pyc.suizhi.model.ProductInfo;
import cn.com.pyc.suizhi.receiver.DownloadPatReceiver;
import cn.com.pyc.suizhi.service.DownloadPatService;
import cn.com.pyc.suizhi.util.SZAPIUtil;
import cn.com.pyc.suizhi.util.DRMUtil;
import cn.com.pyc.suizhi.util.OpenUIUtil;
import cn.com.pyc.utils.ClearKeyUtil;
import cn.com.pyc.utils.ViewUtil;
import cn.com.pyc.widget.HighlightImageView;
import de.greenrobot.event.EventBus;

/**
 * 隐藏批量操作的功能
 * desc:   文件列表（新）       <br/>
 * author: hudaqiang       <br/>
 * create at 2017/7/13 11:40
 */
public class SZListFileActivity extends ExtraBaseActivity implements View.OnClickListener {
    private static final String TAG = "lf";
    private static final String KEY_LOCAL_CONTENT = "local_contents";
    private static final int MSG_UPLOAD_ITEM = 0x301;
    private static final int MSG_LOAD_LOCAL = 0x303;
    private static final int MSG_CLEAR_ITEM = 0x305;
    private static final int MSG_CLEAR_LOCAL_ITEM = 0x307;

    HighlightImageView mBackImg;
    TextView mTitleTv;
    TextView mFunTv;
    TextView emptyTextView;
    View emptyView;
    PullListView mListView;
    SwipeMenuListView mLocalListView;

    private AlbumContentDAOImpl daoAcImpl;
    private MyOwnHandler handler = new MyOwnHandler(this);
    private volatile String myProductId;
    private String productName;
    private String category;
    private String productPic;
    private String productAuthor;
    private String productPicRatio;
    private String downloadFileId; //分享此刻需要下载的文件id;
    private boolean isValid;       //是否当前端有效

    private boolean onStop = false;
    private boolean isLoading = false;
    private boolean isMobileVNO = false;
    private int totalItemCount = 0;
    private int downloadItemCount = 0;
    private ListFileAdapter adapter;
    private ListFileLocalAdapter localAdapter;
    private Callback.Cancelable dataCancelable;
    private List<FileData> cacheDataList;   //加载的列表缓存数据

    private Set<String> sAllFileId = new LinkedHashSet<>(); // 存储文件id
    private Set<String> sDownloadSet = new LinkedHashSet<>(); // 存储已下载的文件id
    public static Set<String> sTaskIdSet; // 存储下载任务的id
    private LocalBroadcastManager mLocalBroadcastManager;


    private static class MyOwnHandler extends Handler {
        private WeakReference<SZListFileActivity> reference;

        private MyOwnHandler(SZListFileActivity context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (reference == null) return;
            SZListFileActivity activity = reference.get();
            if (activity == null) return;
            switch (msg.what) {
                case MSG_LOAD_LOCAL: {          //加载本地数据
                    Bundle bundle = msg.getData();
                    List<DrmFile> drmFiles = bundle.getParcelableArrayList(KEY_LOCAL_CONTENT);
                    if (drmFiles == null || drmFiles.isEmpty()) {
                        activity.loadFailedSet(activity.getString(R.string.offline_data_empty));
                    } else {
                        activity.localAdapter = new ListFileLocalAdapter(activity, drmFiles);
                        activity.mLocalListView.setAdapter(activity.localAdapter);
                    }
                    activity.hideLoading();
                }
                break;
                case MSG_CLEAR_ITEM: {          //清除网络下item数据缓存
                    String fileId = (String) msg.obj;
                    activity.clearItem(fileId);
                    activity.againPlaying(true); //检查续播文件id是否存在，不存在不显示续播按钮
                }
                break;
                case MSG_CLEAR_LOCAL_ITEM: {    //清除本地item数据缓存
                    int position = (int) msg.obj;
                    activity.clearLocalItem(position);
                    activity.againPlaying(true);
                }
                break;
                case MSG_UPLOAD_ITEM: {         //更新下载文件
                    FileData data = (FileData) msg.obj;
                    activity.hideBgLoading();
                    activity.downloadFile(data);
                    activity.againPlaying(true);
                }
                break;
            }
        }
    }

    private void clearItem(String fileId) {
        if (adapter != null) {
            adapter.setContentId(null);
            adapter.notifyDataSetChanged();
            if (downloadItemCount > 0) {
                if ((--downloadItemCount) == 0) {
                    //全部清除，则也删除专辑
                    AlbumDAOImpl.getInstance().deleteAlbumByMyProId(myProductId);
                }
                sDownloadSet.remove(fileId);
            }
        }
        hideBgLoading();
        ToastShow.getToast().showOk(this, getString(R.string.delete_localfile_success));
    }

    private void clearLocalItem(int position) {
        if (localAdapter != null && (position) != -1) {
            localAdapter.notifyDataSetChanged();
            if (localAdapter.getList().isEmpty()) {
                cn.com.pyc.suizhi.util.Util_.setEmptyViews(mLocalListView, emptyView, getString(R
                        .string
                        .connect_net_download));
                //缓存全部清理完，则清除专辑
                AlbumDAOImpl.getInstance().deleteAlbumByMyProId(myProductId);
                sendBroadcast(new Intent(DRMUtil.BROADCAST_CLEAR_DOWNLOADED_ALBUM));
            }
        }
        hideBgLoading();
        ToastShow.getToast().showOk(this, getString(R.string.delete_localfile_success));
    }

    // 清除Item本地资源文件
    private static class ClearItemDataThread implements Runnable {
        private WeakReference<SZListFileActivity> reference;
        private String folderId;
        private String fileId;
        private String collectionId;
        private String lrcId;
        private int position;

        private ClearItemDataThread(SZListFileActivity activity, String MyProId,
                                    String ItemId, String CollectionId,
                                    String LrcId, int Position) {
            reference = new WeakReference<>(activity);
            this.folderId = MyProId;
            this.fileId = ItemId;
            this.collectionId = CollectionId;
            this.lrcId = LrcId;
            this.position = Position;
        }

        @Override
        public void run() {
            if (reference.get() == null) return;
            SZListFileActivity activity = reference.get();
            activity.deleteFileData(folderId, fileId, collectionId, lrcId);
            if (MusicMode.STATUS != MusicMode.Status.STOP && MusicHelp.isSameMusic(fileId)) {
                MusicHelp.release(activity);
            }
            Message message = Message.obtain();
            //position=-1:网络下的删除本地; 反之离线下的删除本地
            message.what = (position == -1) ? MSG_CLEAR_ITEM : MSG_CLEAR_LOCAL_ITEM;
            message.obj = (position == -1) ? fileId : position;
            activity.handler.sendMessageDelayed(message, 300);
        }
    }

    private static class LoadLocalContentThread implements Runnable {
        private WeakReference<SZListFileActivity> reference;
        private String myProId;

        private LoadLocalContentThread(SZListFileActivity activity, String myProId) {
            reference = new WeakReference<>(activity);
            this.myProId = myProId;
        }

        @Override
        public void run() {
            if (reference == null) return;
            SZListFileActivity activity = reference.get();
            if (activity == null) return;
            List<AlbumContent> as = activity.daoAcImpl.findAlbumContentByMyProId(myProId);
            List<DrmFile> drmFiles = DRMFileHelp.convert2DrmFileList(as, activity.productPic);
            Message msg = Message.obtain();
            msg.what = MSG_LOAD_LOCAL;
            Bundle data = new Bundle();
            data.putParcelableArrayList(KEY_LOCAL_CONTENT, (ArrayList<? extends Parcelable>)
                    drmFiles);
            msg.setData(data);
            activity.handler.sendMessageDelayed(msg, 400);
        }
    }

    /**
     * 文件的更新下载
     */
    private static class UploadFileDataThread implements Runnable {
        private WeakReference<SZListFileActivity> reference;
        private FileData data;

        private UploadFileDataThread(SZListFileActivity activity, FileData data) {
            reference = new WeakReference<>(activity);
            this.data = data;
        }

        @Override
        public void run() {
            if (reference.get() == null) return;
            SZListFileActivity activity = reference.get();
            AlbumContent ac = activity.daoAcImpl.findAlbumContentByCollectionId(data
                    .getCollectionId());
            if (ac != null) {      // 删除相关
                activity.deleteFileData(ac.getMyProId(), ac.getContent_id(),
                        ac.getCollectionId(), ac.getMusicLrcId());
            }
            //重新更新下载
            Message message = Message.obtain();
            message.obj = data;
            message.what = MSG_UPLOAD_ITEM;
            activity.handler.sendMessage(message);
        }
    }

    private DownloadPatReceiver receiver = new DownloadPatReceiver() {
        private Context mContext = SZListFileActivity.this;

        @Override
        protected void updateProgress(FileData data, int progress,
                                      long currentSize, boolean isLastProgress) {
            if (adapter == null || sAllFileId == null) return;
            if (data.getTaskState() == DownloadTaskManagerPat.DOWNLOADING
                    && sAllFileId.contains(data.getItemId())) {
                //存在已下载的
                addTaskId(data.getItemId()); //进入UI,下载加入对应的taskId;
            }
            adapter.updateItemView(data);
        }

        @Override
        protected void parsering(FileData data) {
            if (adapter == null) return;
            //////removeTaskId(data.getItemId());
            adapter.updateItemView(data);
        }

        @Override
        protected void connecting(FileData data) {
            if (adapter == null) return;
            adapter.updateItemView(data);
        }

        @Override
        protected void downloadError(FileData data) {
            if (adapter == null) return;
            removeTaskId(data.getItemId());
            adapter.updateItemView(data);

            DownloadHelp.stopDownload(mContext, data.getItemId());
            showToast(getString(R.string.connect_server_error));
        }

        @Override
        protected void downloadFinished(FileData data) {
            if (adapter == null) return;
            removeTaskId(data.getItemId());
            adapter.updateItemView(data);

            endDownloadProcess(data);
            showToast(getString(R.string.download_n_data_complete, data.getContent_name()));
            checkSharePosFile(data);
        }

        @Override
        protected void packaging(final FileData data) {
            if (adapter == null) return;
            adapter.updateItemView(data);
            showToast(getString(R.string.downloaditem_packaging));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeTaskId(data.getItemId());
                    data.setTaskState(DownloadTaskManagerPat.INIT);
                    adapter.updateItemView(data);
                }
            }, 2000);
        }

        @Override
        protected void requestError(FileData data, String code) {
            if (adapter == null) return;
            removeTaskId(data.getItemId());
            adapter.updateItemView(data);
            DownloadHelp.stopDownload(mContext, data.getItemId());
            PromptMsgManager.showToast(mContext, code);
        }
    };


    //TODO: onCreate UI Start...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(cn.com.pyc.pbb.R.layout.activity_list_file);
        initConfigure();
        getValue();
        initView();
        loadData();
        registerBroadcast();
    }

    private void registerBroadcast() {
        IntentFilter downloadFilter = new IntentFilter();
        downloadFilter.addAction(DownloadPatService.ACTION_FINISHED);
        downloadFilter.addAction(DownloadPatService.ACTION_UPDATE);
        downloadFilter.addAction(DownloadPatService.ACTION_ERROR);
        downloadFilter.addAction(DownloadPatService.ACTION_CONNECTING);
        downloadFilter.addAction(DownloadPatService.ACTION_PARSERING);
        downloadFilter.addAction(DownloadPatService.ACTION_PACKAGING);
        downloadFilter.addAction(DownloadPatService.ACTION_REQUEST_ERROR);
        mLocalBroadcastManager.registerReceiver(receiver, downloadFilter);
    }

    private void initConfigure() {
        onStop = false;
        EventBus.getDefault().register(this);
        ViewHelp.showAppTintStatusBar(this);
        daoAcImpl = AlbumContentDAOImpl.getInstance();
        if (sTaskIdSet == null) sTaskIdSet = new LinkedHashSet<>();
        sTaskIdSet.clear();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    private void getValue() {
        Intent intent = getIntent();
        ProductInfo o = intent.getParcelableExtra("ProductInfo");
        if (intent.hasExtra("download_fileId")) {
            downloadFileId = intent.getStringExtra("download_fileId");
        }
        if (o != null) {
            isValid = o.isValid();
            myProductId = o.getMyProId();
            productName = o.getProductName();
            productPic = o.getPicture_url();
            category = o.getCategory();
            productAuthor = o.getAuthors();
            productPicRatio = o.getPicture_ratio();
            Log.e(TAG, o.toString());
        }
        checkMyProId(myProductId);// 检查缺省值
        checkCategory(myProductId);// 检查专辑类型
    }

    private void checkMyProId(String myProId) {
        if (TextUtils.isEmpty(myProId)) {
            UIHelper.showSingleCommonDialog(this, null, "参数缺失(id可能为空或出现错误)",
                    "返回", new UIHelper.DialogCallBack() {
                        @Override
                        public void onConfirm() {
                            finish();
                        }
                    });
        }
    }

    /**
     * 初始化专辑类型category
     * <p>
     * VIDEO;MUSIC;BOOK
     */
    private String checkCategory(String myProId) {
        if (TextUtils.isEmpty(category)) {
            category = AlbumDAOImpl.getInstance().findAlbumCategoryByMyProId(myProId);
        }
        return category;
    }

    protected void initView() {
        mBackImg = (HighlightImageView) findViewById(R.id.alf_back_img);
        mTitleTv = (TextView) findViewById(R.id.alf_title_tv);
        mFunTv = (TextView) findViewById(R.id.alf_fun_tv);
        emptyView = findViewById(R.id.empty_include);
        emptyTextView = (TextView) emptyView.findViewById(R.id.vep_txt_prompt);
        mListView = (PullListView) findViewById(R.id.alf_files_listview);
        mLocalListView = (SwipeMenuListView) findViewById(R.id.alf_local_files_listview);

        mTitleTv.setText(productName);
        mBackImg.setOnClickListener(this);
//        mBatchDownloadTv.setOnClickListener(this);
//        mBatchDeleteTv.setOnClickListener(this);
//        mBatchTv.setOnClickListener(this);


        mLocalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (localAdapter == null) return;
                DrmFile ac = localAdapter.getItem(position);
                if (ac == null) return;
                localAdapter.setContentId(ac.getFileId());
                localAdapter.notifyDataSetChanged();
                // 根据不同的类型，以及子专辑item，跳转不同的播放器。
                OpenUIUtil.openPage(SZListFileActivity.this,
                        category,
                        myProductId,
                        productName,
                        productPic,
                        ac.getFileId(), ac.getLyricId(), null);
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //卖家禁用当前端时，已下载的文件给出相应提示
                if (!isValid) {
                    UIHelper.showSingleCommonDialog(SZListFileActivity.this, "",
                            getString(R.string.file_limit_device), getString(R.string.i_known),
                            null);
                    return;
                }
                FileData data = (FileData) mListView.getItemAtPosition(position);
                if (data == null) return;
                if (data.isOverdue()) {
                    showToast(getString(R.string.file_expired_2));
                    return;
                }
                int taskState = data.getTaskState();
                if (taskState == DownloadTaskManagerPat.CONNECTING) return;
                if (taskState == DownloadTaskManagerPat.PARSERING) return;
                if (taskState == DownloadTaskManagerPat.PACKAGING) {
                    showToast(getString(R.string.downloaditem_packaging));
                    return;
                }
                AlbumContent ac = daoAcImpl.findAlbumContentByCollectionId(data.getCollectionId());
                if (ac == null) {
                    //下载
                    downloadFile(data);
                } else {
                    SZContent szContent = new SZContent(ac.getAsset_id());
                    if (!szContent.checkOpen()) {
                        showToast(getString(R.string.file_expired));
                        return;
                    }
                    if (szContent.isInEffective()) {
                        showToast(getString(R.string.file_ineffective));
                        return;
                    }
                    adapter.setContentId(data.getItemId());
                    adapter.notifyDataSetChanged();
                    OpenUIUtil.openPage(SZListFileActivity.this,
                            category,
                            myProductId,
                            productName,
                            productPic,
                            data.getItemId(),
                            data.getMusicLyric_id(),
                            cacheDataList);
                }
            }
        });
        mListView.setOnRefreshListener(new PullListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (sTaskIdSet != null && !sTaskIdSet.isEmpty()) {
                    showToast(getString(R.string.please_waiting_now_downloading));
                    mListView.refreshComplete();
                    return;
                }

                loadNetData(false);
            }
        });
    }

    protected void loadData() {
        ViewUtil.hideWidget(emptyView);
        if (CommonUtil.isNetConnect(this)) {
            loadNetData(true);
        } else {
            SwipeMenuListView.OnMenuItemClickListener offFileItemClickListener = new
                    SwipeMenuListView
                            .OnMenuItemClickListener() {
                        @Override
                        public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                            if (localAdapter == null) return;
                            if (index != 0) return;
                            DrmFile file = localAdapter.getItem(position);
                            if (file == null) return;
                            removeItemData(file.getMyProductId(), file.getFileId(), file
                                    .getCollectionId(), file.getLyricId(), position);
                        }
                    };

            ViewUtil.showWidget(mLocalListView);
            mListView.refreshComplete();
            ViewUtil.hideWidget(mListView);
            SwipUtil.initSwipItem(mLocalListView, offFileItemClickListener);
            showLoading(this);
            ExecutorManager.getInstance().execute(new LoadLocalContentThread(this, myProductId));
        }
    }

    private void loadNetData(boolean showLoading) {
        if (isLoading) {
            return;
        }
        isLoading = true;
        if (showLoading) showLoading(this);
        ViewUtil.showWidget(mListView);
        ViewUtil.hideWidget(mLocalListView);
        Bundle params = new Bundle();
        params.putString("username", SZConstant.getName());
        params.putString("token", SZConstant.getToken());
        params.putString("category", category);
        params.putString("myProductId", myProductId);
        dataCancelable = HttpEngine.post(SZAPIUtil.getItemVersionList(),
                params, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        SZLog.v("", "itemVersion: " + s);
                        FilesDataModel model = JSON.parseObject(s, FilesDataModel.class);
                        parserData(model);
                    }

                    @Override
                    public void onError(Throwable throwable, boolean b) {
                        Log.e(TAG, throwable.getMessage());
                        loadFailedSet(getResources().getString(R.string.connect_server_failed));
                    }

                    @Override
                    public void onCancelled(CancelledException e) {
                    }

                    @Override
                    public void onFinished() {
                        isLoading = false;
                        mListView.refreshComplete();
                        hideLoading();
                    }
                });
    }

    private void parserData(FilesDataModel model) {
        List<FileData> mDataList = new ArrayList<>();
        if (model.isSuccess()) {
            List<FileData> fileDataList = model.getData();
            int size = fileDataList.size();
            for (int x = 0; x < size; x++) {
                FileData fileData = fileDataList.get(x);
                List<FileVersionModel> versionModelList = fileData.getItemList();
                String latestItemId = fileData.getLatestItemId();//最新的Id;
                int count = versionModelList.size();
                for (int z = 0; z < count; z++) {
                    FileVersionModel versionModel = versionModelList.get(z);
                    String itemId = versionModel.getItemId();
                    if (TextUtils.equals(latestItemId, itemId)) {
                        convertModel(fileData, versionModel);
                        break;
                    }
                }
                sAllFileId.add(fileData.getItemId());
                mDataList.add(fileData);
            }
            loadSuccess(mDataList);
        } else {
            if (model.getMsg().contains("验证")) {
                ClearKeyUtil.removeKey();
                loadFailedSet("登录状态已过期，请重新登录");
                //cn.com.pyc.suizhi.util.Util_.repeatLogin(this);
                return;
            }
            loadFailedSet(getString(R.string.getdata_failed));
        }
    }

    private FileData convertModel(FileData target, FileVersionModel src) {
        target.setContent_name(src.getContent_name());
        target.setContent_size(src.getContent_size());
        target.setContent_format(src.getContent_format());
        target.setPage_num(src.getPage_num());
        target.setLength(src.getLength());
        target.setVersion(src.getVersion());
        target.setVersionInfo(src.getVersionInfo());
        target.setItemId(src.getItemId());
        target.setPlay_progress(src.getPlay_progress());
        target.setMusicLyric_id(src.getMusicLyric_id());
        target.setMyProId(myProductId);
        target.setPicture_ratio(productPicRatio);
        target.setAuthors(productAuthor);
        return target;
    }


    private void loadFailedSet(String msg) {
        emptyTextView.setText(msg);
        mListView.setEmptyView(emptyView);
    }

    private void loadSuccess(List<FileData> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            emptyTextView.setText(getString(R.string.load_data_null));
            mListView.setEmptyView(emptyView);
            return;
        }
        cacheDataList = itemList;
        updateAlbumContentForCollectionId(itemList);

        totalItemCount = itemList.size();
        adapter = new ListFileAdapter(this, itemList);
        adapter.setListView(mListView);
        mListView.setAdapter(adapter);
        initOnSwipeClickListener();
        notifyItemView(MusicHelp.getCurrentPlayId());

        againPlaying(true);
        downloadShareFile(downloadFileId, itemList);
    }

    //先通过itemId查询到记录AlbumContent;
    //查询该itemId对应的collectionId;
    //将对应的collectionId更新到AlbumContent;
    private void updateAlbumContentForCollectionId(List<FileData> datas) {
        for (FileData data : datas) {
            List<FileVersionModel> models = data.getItemList();
            for (FileVersionModel model : models) {
                AlbumContent saveAc = daoAcImpl.findAlbumContentByContentId(model.getItemId());
                if (saveAc != null) {
                    //存在记录
                    if (StringUtil.isEmptyOrNull(saveAc.getCollectionId())
                            || StringUtil.isEmptyOrNull(saveAc.getMusicLrcId())
                            || StringUtil.isEmptyOrNull(saveAc.getMyProId())
                            || saveAc.getContentSize() == 0L) {
                        String collectionId = data.getCollectionId();
                        String lrcId = data.getMusicLyric_id();
                        long contentSize = data.getContent_size();
                        int result = daoAcImpl.updateAlbumContentByItemId(saveAc.getContent_id(),
                                collectionId, lrcId, myProductId, contentSize);
                        SZLog.e(TAG, "更新AlbumContent[" + saveAc.getName() + "] Column: " + result);
                    }
                }
            }
        }
    }

    /**
     * 乐视品牌手机
     */
    public static boolean isLetv() {
        return "Letv".equalsIgnoreCase(Build.BRAND) || Build.BRAND.startsWith("Letv");
    }

    // 初始化左滑删除事件回调
    private void initOnSwipeClickListener() {
        if (adapter == null) return;
        if (isLetv()) {  //乐视手机侧滑异常，采用长按事件
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                               long id) {
                    final int index = position;
                    UIHelper.showCommonDialog(SZListFileActivity.this, null, "确定要删除该文件吗?", "删除",
                            new UIHelper.DialogCallBack() {
                                @Override
                                public void onConfirm() {
                                    clearFileData(index);
                                }
                            });
                    return true;
                }
            });
            return;
        }
        adapter.setOnSwipeClickListener(new ListFileAdapter.OnSwipeClickListener() {
            @Override
            public void onClick(View view, int position) {
                clearFileData(position);
            }
        });
    }

    private void clearFileData(int position) {
        FileData data = adapter.getItem(position);
        if (data == null) return;
        //AlbumContent ac = daoAcImpl.findAlbumContentByCollectionId(data.getCollectionId
        // ());
        //if (ac == null) return;
        boolean hasSaveData = daoAcImpl.existAlbumContentById(data.getCollectionId());
        if (!hasSaveData) {
            DownDataPatDBManager.Builder().deleteByFileId(data.getItemId());
            return;
        }
        if (data.getTaskState() == DownloadTaskManagerPat.DOWNLOADING) return;
        if (data.getTaskState() == DownloadTaskManagerPat.PARSERING) {
            showToast(getString(R.string.please_waiting_now_parser));
            return;
        }
        removeItemData(data.getMyProId(), data.getItemId(), data.getCollectionId(), data
                .getMusicLyric_id(), -1);
    }

    /**
     * 删除数据
     *
     * @param MyProId      我购买的专辑id
     * @param ItemId       文件id
     * @param CollectionId 文件集合id
     * @param LrcId        歌词id
     * @param Position     网络状态：position=-1,离线本地：position=position
     */
    protected void removeItemData(String MyProId, String ItemId, String CollectionId,
                                  String LrcId, int Position) {
        handler.removeCallbacksAndMessages(null);
        showBgLoading(this, getString(R.string.now_delete_item));
        ExecutorManager.getInstance().execute(new ClearItemDataThread(this, MyProId, ItemId,
                CollectionId, LrcId, Position));
    }


    private void notifyItemView(String fileId) {
        if (adapter == null || TextUtils.isEmpty(fileId)) return;
        adapter.setContentId(fileId);
        adapter.notifyDataSetChanged();
    }

    /**
     * 续播
     *
     * @param check 是否检查存在续播文件id
     */
    private void againPlaying(boolean check) {
        //判断对应myProId保存的文件id是否存在
        String fileId = (String) ProgressHelp.getProgress("ap_" + myProductId, "");
        if (check) {
            if (!TextUtils.isEmpty(fileId)) {
                mFunTv.setVisibility(View.VISIBLE);
                mFunTv.setOnClickListener(this);
            } else {
                mFunTv.setVisibility(View.INVISIBLE);
            }
            return;
        }
        ProgressHelp.removeProgress("ap_" + myProductId); //移除，只需取到值fileId就可以；
        FileData fileData = null;
        for (FileData data : cacheDataList) {
            if (TextUtils.equals(data.getItemId(), fileId)) {
                fileData = data;
                break;
            }
        }
        String tempId = (fileData != null) ? fileData.getItemId() : fileId;
        notifyItemView(tempId);
        OpenUIUtil.openPage(SZListFileActivity.this,
                category,
                myProductId,
                productName,
                productPic,
                tempId,
                fileData != null ? fileData.getMusicLyric_id() : "",
                cacheDataList);
    }

    /**
     * 下载分享此刻的文件（有权限前提下）
     */
    private void downloadShareFile(String downloadFileId, List<FileData> itemList) {
        if (TextUtils.isEmpty(downloadFileId)) return;
        int count = itemList.size();
        for (int i = 0; i < count; i++) {
            FileData data = itemList.get(i);
            if (downloadFileId.equals(data.getItemId())) {
                data.setMyProId(myProductId);
                data.setAuthors(productAuthor);
                data.setPicture_ratio(productPicRatio);
                downloadFile(data);
                break;
            }
        }
    }

    /**
     * 下载完成，检测分享此刻的文件并提示打开
     */
    private void checkSharePosFile(final FileData data) {
        if (TextUtils.isEmpty(downloadFileId)) return;
        UIHelper.showCommonDialog(this, "",
                getString(R.string.share_moment_download_complete),
                getString(R.string.open),
                new UIHelper.DialogCallBack() {
                    @Override
                    public void onConfirm() {
                        if (adapter != null) {
                            adapter.setContentId(data.getItemId());
                            adapter.notifyDataSetChanged();
                        }
                        OpenUIUtil.openPage(SZListFileActivity.this,
                                category,
                                myProductId,
                                productName,
                                productPic,
                                data.getItemId(),
                                data.getMusicLyric_id(),
                                cacheDataList);
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (onStop) { //页面从不可见到显示，更新item选中和续播按钮显示
            notifyItemView(MusicHelp.getCurrentPlayId());
            againPlaying(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        onStop = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        if (sTaskIdSet != null) sTaskIdSet.clear();
        if (sDownloadSet != null) sDownloadSet.clear();
        if (sAllFileId != null) sAllFileId.clear();
        handler.removeCallbacksAndMessages(null);
        mLocalBroadcastManager.unregisterReceiver(receiver);
        //DownloadHelp.stopAllDownload(this);
        EventBus.getDefault().unregister(this);
        HttpEngine.cancelHttp(dataCancelable);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        ///////if (CommonUtil.isFastDoubleClick(600)) return;
        int id = v.getId();
        if (id == R.id.alf_back_img) {
            finish();
        } else if (id == R.id.alf_fun_tv) {
            //续播
            againPlaying(false);
        }
    }

    //下载过程退出后再进入文件列表，可能该列表中对应文件正好也在下载(ListFileAdapter发送通知)
//    @Deprecated
//    public void onEventMainThread(FileProgressStateEvent event) {
//        //setDownloadAllButtonShow(R.string.download_cancel, true);
//        //进入UI,下载加入对应的taskId;
//        addTaskId(event.getFileId());
//    }

    // 播放音乐服务，发送当前的id
    public void onEventMainThread(MusicCurrentPlayEvent event) {
        notifyItemView(event.getFileId());
    }

    /**
     * 更新文件，删除相关数据
     * <p>
     * folderId 文件夹id,
     * itemId   文件id（文件有更新，会有历史id列表）,
     * collectionId   文件集合id,
     * lrcId   音乐歌词id
     */
    private void deleteFileData(String folderId, String itemId,
                                String collectionId, String lrcId) {
        DownDataPatDBManager.Builder().deleteByFileId(itemId);
        DRMDBHelper.deleteFile(folderId, itemId);
        daoAcImpl.deleteAlbumContentByCollectionId(collectionId);
        LrcEngine.deleteLyric(folderId, lrcId);
    }

    private synchronized void addTaskId(String fileId) {
        if (sTaskIdSet != null) {
            sTaskIdSet.add(fileId);
        }
    }

    private synchronized void removeTaskId(String fileId) {
        if (sTaskIdSet != null) {
            sTaskIdSet.remove(fileId);
        }
    }

//    private synchronized void clearTaskId() {
//        if (sTaskIdSet != null) {
//            sTaskIdSet.clear();
//        }
//    }

    private synchronized boolean containsTaskId(String fileId) {
        return sTaskIdSet != null && sTaskIdSet.contains(fileId);
    }

    /**
     * 整个下载流程结束，文件解析完毕并入库
     */
    private void endDownloadProcess(FileData data) {
        sDownloadSet.add(data.getItemId());
        downloadItemCount = sDownloadSet.size();
        SZLog.e(TAG, "已下载个数：" + downloadItemCount + ", 总数：" + totalItemCount);
        checkCategory(myProductId);
    }

    private void addTask(FileData o) {
        addTaskId(o.getItemId());
        o.setTaskState(DownloadTaskManagerPat.WAITING);
        adapter.updateItemView(o);
        DownloadHelp.startDownload(this, o);

        //存在下载任务，按钮显示取消
        //setDownloadAllButtonShow(R.string.download_cancel, true);
    }

    private void removeTask(FileData o) {
        removeTaskId(o.getItemId());
        o.setTaskState(DownloadTaskManagerPat.PAUSE);
        adapter.updateItemView(o);
        DownloadHelp.stopDownload(this, o.getItemId());
    }

    /**
     * 下载
     */
    private void downloadFile(final FileData o) {
        if (o == null) return;
        if (containsTaskId(o.getItemId())) {
            removeTask(o);
            return;
        }
        // 提示一次信息
        if (!CommonUtil.isWifi(this) && !isMobileVNO) {
            UIHelper.showCommonDialog(this, "", getString(R.string.download_tips),
                    getString(R.string.download_ask_ok), new UIHelper.DialogCallBack() {
                        public void onConfirm() {
                            isMobileVNO = true;
                            addTask(o);
                        }
                    });
        } else {
            addTask(o);
        }
    }

}
