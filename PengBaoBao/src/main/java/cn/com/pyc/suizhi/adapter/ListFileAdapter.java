package cn.com.pyc.suizhi.adapter;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sz.mobilesdk.authentication.SZContent;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.SZLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ValidDateUtil;
import cn.com.pyc.suizhi.SZListFileActivity;
import cn.com.pyc.suizhi.bean.event.FileHandleEvent;
import cn.com.pyc.suizhi.help.DownloadHelp;
import cn.com.pyc.suizhi.manager.DownloadTaskManagerPat;
import cn.com.pyc.suizhi.manager.db.DownDataPat;
import cn.com.pyc.suizhi.manager.db.DownDataPatDBManager;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.widget.swipe.SimpleSwipeListener;
import cn.com.pyc.suizhi.widget.swipe.SwipeLayout;
import cn.com.pyc.utils.ViewUtil;
import cn.com.pyc.widget.HighlightImageView;
import de.greenrobot.event.EventBus;

public class ListFileAdapter extends BaseAdapter {
    private static final String TAG = "ListFileAdapter";
    private ExtraBaseActivity mContext;
    private List<FileData> mList;
    private ListView listView;
    private String contentId;

    private Random random = new Random();
    private ArrayList<SwipeLayout> alSwipeLayout = new ArrayList<>();
    private OnSwipeClickListener mListener;

    private boolean showCheckBox = false; //显示checkBox
    private boolean download = false;    //下载操作（true下载、false删除）
    private Map<String, Boolean> mSelectState = new HashMap<>(); //存儲選中的狀態
    private Map<String, FileData> sSelectFile = new HashMap<>(); //存储选中的元素。

    private int selectColorId;
    private int waitingColorId;
    private int unSelectNameColorId;
    private int unSelectTimeColorId;
    private int expiredColorId;


    private String waitingStr;
    private String connectStr;
    private String packagingStr;
    private String parseringStr;
    private String pauseDownloadStr;
    private String downloadedStr;
    private String unDownloadedStr;

    private DownDataPatDBManager mDBManager;
    private AlbumContentDAOImpl mDaoImpl;

    public void setOnSwipeClickListener(OnSwipeClickListener listener) {
        mListener = listener;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    public ListFileAdapter(ExtraBaseActivity context, List<FileData> list) {
        mContext = context;
        mList = list;

        mDaoImpl = AlbumContentDAOImpl.getInstance();
        mDBManager = DownDataPatDBManager.Builder();
//        initCheckBoxState(mList);

        Resources resources = mContext.getResources();
        waitingColorId = resources.getColor(R.color.title_top_color);
        selectColorId = resources.getColor(R.color.brilliant_blue);
        unSelectNameColorId = resources.getColor(R.color.black_bb);
        unSelectTimeColorId = resources.getColor(R.color.gray);
        expiredColorId = resources.getColor(R.color.red);
        //grayLineColorId = resources.getColor(R.color.line_color);
        //blueLineColorId = resources.getColor(R.color.brilliant_tint_blue);
        //this.selectDrawable = resources.getDrawable(R.drawable.ic_validate_time_select);
        //this.unSelectDrawable = resources.getDrawable(R.drawable.ic_validate_time_nor);

        waitingStr = resources.getString(R.string.Waiting);
        connectStr = resources.getString(R.string.Connecting);
        packagingStr = resources.getString(R.string.Packaging);
        parseringStr = resources.getString(R.string.Parsering);
        pauseDownloadStr = resources.getString(R.string.downloaditem_pause);
        downloadedStr = resources.getString(R.string.downloaditem_download);
        unDownloadedStr = resources.getString(R.string.downloaditem_undownload);
    }

    public List<FileData> getList() {
        return mList;
    }

    public void setList(List<FileData> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public FileData getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FileData fileData = mList.get(position);
        if (convertView == null)
            convertView = View.inflate(mContext, R.layout.item_list_file, null);
        convertView.setTag(fileData.getItemId());
        ViewHolder holder = new ViewHolder(convertView);
        //名称和大小
        holder.progressBar.setSecondaryProgress(0);
        holder.nameText.setText(fileData.getContent_name());
        holder.sizeText.setText(FormatterUtil.formatSize(fileData.getContent_size()));
        AlbumContent content = null;
        if (fileData.isOverdue()) { //文件已过期
            holder.timeText.setTextColor(expiredColorId);
            holder.timeText.setText(mContext.getString(R.string.over_time));
        } else {
            unSelectorItem(holder);
            content = mDaoImpl.findAlbumContentByCollectionId(fileData.getCollectionId());
            setItemView(fileData, content, holder);
        }
//        setCheckBox(holder, fileData, content != null);
        if (fileData.getContent_size() == 0) //出现错误数据，不显示下载按钮,一般是服务器数据出错
            ViewUtil.hideWidget(holder.downloadImage);
        initSwipeClickListener(holder, position);
        return convertView;
    }

    //设置item view显示
    private void setItemView(FileData fileData, AlbumContent content, ViewHolder holder) {
        if (content == null) {  //没有下载
            SZLog.d(TAG, "Net data name is " + fileData.getContent_name());
            ViewUtil.showWidget(holder.downloadImage);
            ViewUtil.hideWidget(holder.timeLayout);
            ViewUtil.hideWidget(holder.downloadStatusText);
            ViewUtil.hideWidget(holder.updateBtn);
            holder.progressBar.setSecondaryProgress(0);
            initState(fileData, holder);
            //downloadFileByClick(fileData, holder.downloadImage);
        } else { //本地已下载
            SZLog.d(TAG, "Local data name is " + content.getName());
            ViewUtil.inVisibleWidget(holder.downloadImage);
            ViewUtil.hideWidget(holder.downloadStatusText);
            ViewUtil.showWidget(holder.timeLayout);
            holder.progressBar.setSecondaryProgress(100);

            initTime(content.getAsset_id(), holder.timeText);
            setItemSelector(content.getContent_id(), holder);
            checkFileUpdate(content.getContent_id(), fileData, holder);
        }
    }

    //下载文件
//    private void downloadFileByClick(final FileData data, View downloadView) {
//        downloadView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (data.isOverdue()) {
//                    mContext.showToast(mContext.getString(R.string.file_expired_2));
//                    return;
//                }
//                int taskState = data.getTaskState();
//                if (taskState == DownloadTaskManagerPat.CONNECTING) return;
//                if (taskState == DownloadTaskManagerPat.PARSERING) return;
//                if (taskState == DownloadTaskManagerPat.PACKAGING) {
//                    mContext.showToast(mContext.getString(R.string.downloaditem_packaging));
//                    return;
//                }
//                EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type.FILE_DOWNLOAD,
// data));
//            }
//        });
//    }

    //设置权限时间
    private void initTime(String assetId, TextView timeText) {
        SZContent szCont = new SZContent(assetId);
        if (szCont.isInEffective()) {
            timeText.setText(mContext.getString(R.string.file_ineffective));
        } else {
            timeText.setText(ValidDateUtil.getValidTime(mContext, szCont
                    .getAvailbaleTime(), szCont.getOdd_datetime_end()));
        }
    }

    //初始化下载各种状态显示
    private void initState(FileData o, ViewHolder holder) {
        //下载退出后再进入文件列表，可能该列表中对应文件刚好下载完正处在解析状态
        if (!TextUtils.isEmpty(DownloadHelp.findParserId(o.getItemId()))) {
            //o.setTaskState(DownloadTaskManagerPat.PARSERING);
            //updateItemState(o.getItemId(), DownloadTaskManagerPat.PARSERING);
        }
        //下载过程退出后再进入文件列表，可能该列表中对应文件正好也在下载
//        int tempProgress = DownloadHelp.findFileProgress(o.getItemId());
//        if (tempProgress > 0 && o.getTaskState() != DownloadTaskManagerPat.DOWNLOADING) {
//            o.setTaskState(DownloadTaskManagerPat.DOWNLOADING);
//            o.setProgress(tempProgress);
//            holder.progressBar.setProgress(tempProgress);
//            holder.checkBox.setChecked(true);
//            EventBus.getDefault().post(new FileProgressStateEvent(o.getItemId()));
//        }
        // 下载后删除数据，不执行finished()；滚动列表过程中。
        if (o.getTaskState() != DownloadTaskManagerPat.FINISHED) {
            changeView(o, holder);
        }
        //存在下载记录，并且正在连接状态
        DownDataPat data = mDBManager.findByFileId(o.getItemId());
        if (data == null) {
            holder.progressBar.setProgress(0);
        } else {
            holder.progressBar.setProgress(data.getProgress());
        }
    }


    private void setItemSelector(String contentId, ViewHolder holder) {
        if (TextUtils.equals(this.contentId, contentId)) {
            holder.nameText.setTextColor(selectColorId);
        } else {
            unSelectorItem(holder);
        }
    }

    private void unSelectorItem(ViewHolder holder) {
        holder.timeText.setTextColor(unSelectTimeColorId);
        holder.nameText.setTextColor(unSelectNameColorId);
    }

    // 检查已下载的文件是否有更新(保存的id和最新的id不同就提示更新)
    private void checkFileUpdate(String dbContentId, final FileData o, final ViewHolder holder) {
        String newItemId = o.getLatestItemId();
        SZLog.d(TAG, "dbContentId  = " + dbContentId);
        SZLog.d(TAG, "latestItemId = " + newItemId);
        ViewUtil.hideWidget(holder.updateBtn);
        if (TextUtils.equals(newItemId, dbContentId))
            return;
        ViewUtil.hideWidget(holder.downloadImage);
        ViewUtil.showWidget(holder.updateBtn);
        holder.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SZListFileActivity.sTaskIdSet != null) {
                    int taskSize = SZListFileActivity.sTaskIdSet.size();
                    if (taskSize >= Constant.sTaskCount) {
                        mContext.showToast(mContext.getString(R.string
                                .please_waiting_n_update_download, taskSize));
                        return;
                    }
                }
                EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type.FILE_UPDATE, o));
                ViewUtil.hideWidget(holder.updateBtn);
            }
        });
    }

    /**
     * 下载任务开始，单个item状态变化更新
     *
     * @param o FileData
     */
    public void updateItemView(FileData o) {
        View itemView = listView.findViewWithTag(o.getItemId());
        if (itemView == null) {
            return;
        }
        SZLog.d(TAG, "updateItemView : " + o.getContent_name());

        itemView.setTag(o.getItemId());
        ViewHolder holder = new ViewHolder(itemView);

        changeView(o, holder);
        updateItemState(o);
    }

    /**
     * 设置item对象状态
     */
    private void updateItemState(FileData data) {
        if (mList == null) return;
        String itemId = data.getItemId();
        int location = cn.com.pyc.suizhi.util.Util_.getFileIndex(itemId, mList);
        if (location != -1) {
            FileData o = mList.get(location);
            int taskState = data.getTaskState();
            if (o.getTaskState() == taskState) {
                return;
            }
            o.setTaskState(taskState);
        }
    }

    /**
     * 修改view状态
     */
    private void changeView(FileData o, ViewHolder holder) {
        int taskState = o.getTaskState();
        switch (taskState) {
            case DownloadTaskManagerPat.INIT:
                init(o, holder);
                break;
            case DownloadTaskManagerPat.WAITING:
                waiting(o, holder);
                break;
            case DownloadTaskManagerPat.CONNECTING:
                connecting(o, holder);
                break;
            case DownloadTaskManagerPat.PAUSE:
                pause(o, holder);
                break;
            case DownloadTaskManagerPat.PARSERING:
                parsering(o, holder);
                break;
            case DownloadTaskManagerPat.DOWNLOAD_ERROR:
                downloadError(o, holder);
                break;
            case DownloadTaskManagerPat.DOWNLOADING:
                downloading(o, holder);
                break;
            case DownloadTaskManagerPat.FINISHED:
                finished(o, holder);
                break;
            case DownloadTaskManagerPat.PACKAGING:
                packaging(o, holder);
                break;
            default:
                Log.e(TAG, "download task state error.");
                break;
        }
    }

    /**
     * 开始状态
     */
    private void init(FileData o, ViewHolder holder) {
        ViewUtil.showWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.hideWidget(holder.downloadStatusText);
        holder.progressBar.setProgress(0);
        holder.downloadStatusText.setTag(null);
        holder.sizeText.setText(FormatterUtil.formatSize(o.getContent_size()));
    }

    /**
     * 等待中
     */
    private void waiting(FileData o, ViewHolder holder) {
        ViewUtil.inVisibleWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        holder.downloadStatusText.setTextColor(waitingColorId);
        holder.downloadStatusText.setText(waitingStr);
        holder.progressBar.setProgress(0);
        //holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size()));
    }

    /**
     * 连接中
     */
    private void connecting(FileData o, ViewHolder holder) {
        ViewUtil.inVisibleWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        holder.downloadStatusText.setTag(null);
        holder.downloadStatusText.setTextColor(unSelectTimeColorId);
        holder.downloadStatusText.setText(connectStr);
        holder.progressBar.setProgress(0);
        //holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size()));
    }

    /**
     * 正在打包
     */
    private void packaging(FileData o, ViewHolder holder) {
        ViewUtil.inVisibleWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        holder.downloadStatusText.setTextColor(unSelectTimeColorId);
        holder.downloadStatusText.setText(packagingStr);
        holder.progressBar.setProgress(0);
//        if (holder.checkBox.isChecked()) {
//            sSelectFile.remove(o.getItemId());
//            holder.checkBox.setChecked(false);
//        }
        //相当于回到初始状态，文件不选择
//        EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type.FILE_UN_SELECT, o));
    }

    /**
     * 下载
     */
    private void downloading(final FileData o, final ViewHolder holder) {
        ViewUtil.inVisibleWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        ViewUtil.showWidget(holder.progressBar);
//        if (!holder.checkBox.isChecked()) {
//            //正在下载的，如果没有勾选，设置勾选
//            sSelectFile.put(o.getItemId(), o);
//            holder.checkBox.setChecked(true);
//        }
        int progress = o.getProgress();
        holder.progressBar.setProgress(progress);
        long currentSize = (o.getContent_size() * progress) / 100;
        holder.sizeText.setText(FormatterUtil.formatSize2(currentSize) + "/" + FormatterUtil
                .formatSize(o.getContent_size()));
        holder.downloadStatusText.setTextColor(selectColorId);
        holder.downloadStatusText.setText(pauseDownloadStr);
        if (holder.downloadStatusText.getTag() == null) {
            holder.downloadStatusText.setTag(o.getItemId());
            holder.downloadStatusText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //holder.checkBox.setChecked(false);
                    //下载---》 暂停
                    EventBus.getDefault().post(new FileHandleEvent(
                            BaseEvent.Type.FILE_CANCEL, o));
                }
            });
        }
    }

    /**
     * 暂停中
     */
    private void pause(FileData o, ViewHolder holder) {
        ViewUtil.showWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.inVisibleWidget(holder.downloadStatusText);
        holder.progressBar.setProgress(o.getProgress());
        holder.downloadStatusText.setOnClickListener(null);
        holder.downloadStatusText.setTag(null);
        holder.sizeText.setText(FormatterUtil.formatSize(o.getContent_size()));
    }

    /**
     * 解析文件
     */
    private void parsering(FileData o, ViewHolder holder) {
        ViewUtil.hideWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        holder.downloadStatusText.setTextColor(unSelectTimeColorId);
        holder.downloadStatusText.setText(parseringStr);
        int progress = 98 + random.nextInt(3);
        int curProgress = holder.progressBar.getProgress();
        if (curProgress < progress) {
            curProgress = progress;
        }
        holder.progressBar.setProgress(curProgress);
        holder.downloadStatusText.setOnClickListener(null);
        holder.downloadStatusText.setTag(null);
        holder.sizeText.setText(FormatterUtil.formatSize(o.getContent_size()));
    }

    /**
     * 下载异常，ftpPath: shutdown,404,-1
     */
    private void downloadError(FileData o, ViewHolder holder) {
        ViewUtil.showWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.hideWidget(holder.downloadStatusText);
//        if (holder.checkBox.isChecked()) {
//            sSelectFile.remove(o.getItemId());
//            holder.checkBox.setChecked(false);
//        }
        holder.progressBar.setProgress(0);
        holder.downloadStatusText.setOnClickListener(null);
        holder.downloadStatusText.setTag(null);
        holder.sizeText.setText(FormatterUtil.formatSize(o.getContent_size()));
        //相当于回到初始状态，文件不选择
//        EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type.FILE_UN_SELECT, o));
    }

    /**
     * 下载完成
     */
    private void finished(FileData o, ViewHolder holder) {
        // 查询存储的数值
        AlbumContent content = mDaoImpl.findAlbumContentByCollectionId(o.getCollectionId());
        if (content == null) return;
        ViewUtil.hideWidget(holder.downloadStatusText);
        ViewUtil.hideWidget(holder.downloadImage);
//        if (holder.checkBox.isChecked()) {
//            sSelectFile.remove(o.getItemId());
//            holder.checkBox.setChecked(false);
//        }
//        ViewUtil.hideWidget(holder.checkBox);
        ViewUtil.showWidget(holder.timeLayout);
        SZContent szContent = new SZContent(content.getAsset_id());
        holder.timeText.setText(ValidDateUtil.getValidTime(mContext,
                szContent.getAvailbaleTime(), szContent.getOdd_datetime_end()));
        holder.nameText.setText(TextUtils.isEmpty(content.getName()) ? o.getContent_name() :
                content.getName());
        holder.progressBar.setSecondaryProgress(100); //第二进度当做蓝色线条
        holder.progressBar.setProgress(0);
        holder.sizeText.setText(FormatterUtil.formatSize(o.getContent_size())); //更新size
    }

    static class ViewHolder {
        SwipeLayout swipeLayout;
        View delLayout;
        TextView nameText;
        TextView downloadStatusText;
        HighlightImageView downloadImage;
        TextView timeText;
        TextView sizeText;
        ProgressBar progressBar;
        View timeLayout;
        Button updateBtn;

        private ViewHolder(View convertView) {
            swipeLayout = ((SwipeLayout) convertView.findViewById(R.id.item_lf_swipe));
            delLayout = convertView.findViewById(R.id.item_lf_del_layout);
            timeLayout = convertView.findViewById(R.id.item_lf_time_layout);
            nameText = ((TextView) convertView.findViewById(R.id.item_lf_name_tv));
            downloadStatusText = ((TextView) convertView.findViewById(R.id.item_lf_status_tv));
            downloadImage = ((HighlightImageView) convertView.findViewById(R.id
                    .item_lf_download_img));
            timeText = ((TextView) convertView.findViewById(R.id.item_lf_time_tv));
            sizeText = ((TextView) convertView.findViewById(R.id.item_lf_size_tv));
            progressBar = ((ProgressBar) convertView.findViewById(R.id.item_lf_progress));
            updateBtn = ((Button) convertView.findViewById(R.id.item_lf_update_btn));
        }
    }

    /**
     * 初始化左滑控件事件。
     */
    private void initSwipeClickListener(final ViewHolder holder, final int position) {
        if (SZListFileActivity.isLetv()) {
            holder.swipeLayout.setSwipeEnabled(false);
            return;
        }
        holder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                if (alSwipeLayout.size() > 0) {
                    ArrayList<SwipeLayout> sl_al = new ArrayList<>(alSwipeLayout);
                    for (SwipeLayout sw : sl_al) {
                        if (sw != null && !layout.equals(sw)) {
                            sw.close();
                            alSwipeLayout.remove(sw);
                        }
                    }
                }
                if (!alSwipeLayout.contains(holder.swipeLayout)) {
                    alSwipeLayout.add(holder.swipeLayout);
                }
            }
        });
        holder.delLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(v, position);
                }
                holder.swipeLayout.close();
            }
        });
    }

    // 左滑删除控件回调事件。
    public interface OnSwipeClickListener {
        void onClick(View view, int position);
    }

}
