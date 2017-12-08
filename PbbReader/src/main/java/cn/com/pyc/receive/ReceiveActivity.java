package cn.com.pyc.receive;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.alibaba.fastjson.JSON;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.qlk.util.base.BaseActivity;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.media.ISelection.ISelectListener;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.tool.Util.NetUtil;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.manager.ImageLoadHelp;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;

import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Stack;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.conn.SmRefresher;
import cn.com.pyc.db.SerisesDao;
import cn.com.pyc.db.sm.SmDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.model.SearchResultModel;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.event.IsSierisModeEvent;
import cn.com.pyc.update.UpdateTool;
import cn.com.pyc.user.UserCenterActivity;
import cn.com.pyc.web.BrowserActivity;
import cn.com.pyc.widget.PycAutoTextGray;
import cn.com.pyc.xcoder.XCoder;
import de.greenrobot.event.EventBus;

/**
 * 本类应用较广：PbbReader，鹏保宝的接收列表和发送列表
 * 此为原来接收文件的列表页
 * @author QiLiKing 2015-7-31 下午4:24:43
 */
@Deprecated
public class ReceiveActivity extends PbbBaseActivity {
    public static final String EXPAND_GROUP = "expand_group";
    public static final int RESULT_READ = 0x1234;

    private static final String TAG = "ReceiveActivity";
    private String tagWay;

    private enum TopBarMode {
        Normal, Search, Delete, SeriesID
    }

    /**
     * Use this to go back to previous step.<br>
     * It's top element is the current mode.
     */
    private final Stack<TopBarMode> mHistoryMode = new Stack<>();

    private static final String SP_VIEW_TYPE = "sp_view_type";
    /* Never change the value of the final type! They must be 0 and 1. */
    protected static final int VIEW_TYPE_LIST = 0;        //default
    protected static final int VIEW_TYPE_SERIES = 1;    //Sort by Series ID.
    protected int mViewType = VIEW_TYPE_LIST;

    private int mSid;
    private int setNew = -1;    //Change it to 1 when showing red circle, then  0 when pressed.

    /**
     * It is the data source of the "mSmAdapter", so we don't allow anyone to
     * change it's object it direct to.
     */
    private final ArrayList<String> mTotalPaths = new ArrayList<String>();
    public static final ArrayList<String> mSidPaths = new ArrayList<>();
    protected final ArrayList<String> mPaths = new ArrayList<>();
    public static final HashMap<String, SmInfo> mDatas = new HashMap<>();
    private final ArrayList<SmInfo> mSeriesInfos = new ArrayList<>();
    private boolean isFromUser = false;
    private SerisesDao mSerisesDao;
    private SmDao mSmDao;
    public static boolean reGetSidInfos = false;
    private boolean isOnSearch = false;
    private boolean isSearchRefeshing = false;
    private boolean isAddRecommend = false;
    private int currentPage = 1;
    private int totalPageNum = 1;
    private boolean isSearching = false;
    private String searchText;
    private List<View> mRecommendViews = new ArrayList<>();

    /* Top View */
    private ViewAnimator mTopView;
    private ImageButton mSettingBackButton;    //In PengBaoBao, it is back button; In PbbReader,
    // it is setting button.
    // private HighlightImageView mAddButton; //+按钮
    //	private ImageSwitcher mSwitchView;
    //	private ImageButton mSearchButton;
    //	private ImageButton mSidSearchButton;
    //  private TextView mSearchTxt;
    private PycAutoTextGray mSearchText;
    private TextView mDeleteNumText;
    private TextView mSeriesNameText;
    // private TextView mSeriesSwitcherText;
    // private PopupWindow popwin;
    private View mEmptyView;
    private View mRecommendView;
    private ExpandableListView mSmListView;
    private ReceiveAdapter mSmAdapter;
    private ListView mSeriesListView;
    private SeriesAdapter mSeriesAdapter;
    //private PullRefreshView mPullRefreshView;
    private SwipeToLoadLayout mPullRefreshView;
    private RelativeLayout mDelleteBottomView;
    // private RelativeLayout mSendTitle;//pbb中已发送界面的标题栏
    // private HighlightImageView mSendBack;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        //judgeHistory();
        //注册EventBus
        EventBus.getDefault().register(this);

        setContentView(R.layout.activity_receive_list);
        findViewAndSetListeners();
        initUI();
    }

	/*
     * 判断是否有扫码记录
	 */
    //	private void judgeHistory()
    //	{
    //		ScanHistory o = ScanHistoryDBManager.Builder().findFirstData();
    //		if (o == null)
    //		{
    //			SZLog.v("", "history empty");
    //		}
    //		else
    //		{
    //			OpenPageUtil.openActivity(this, ListHistoryActivity.class);
    //			finish();
    //		}
    //	}

    public void onFolderClick(View v) {
        startActivity(new Intent(this, FindFileActivity.class));
    }

    /**
     * Subclass should override this method to indicate what (ReceiveList or
     * SendList) you are.
     */
    protected boolean isReceive() {
        return true;
    }

    protected String getMyTitle() {
        return getResources().getString(R.string.app_name);
    }

    protected ReceiveAdapter getAdapter() {
        return new ReceiveAdapter(this, mPaths, mDatas);
    }

    private ArrayList<String> getPaths() {
        return GlobalData.Sm.instance(this).getCopyPaths(!isReceive());
    }

    protected void search() {
        GlobalData.Sm.instance(this).search(!isReceive());
    }

    @Override
    protected void initUI() {
        mHistoryMode.push(TopBarMode.Normal);
        mSerisesDao = SerisesDao.getInstance();
        mSmDao = SmDao.getInstance(this, isReceive());
        mViewType = (int) PbbSP.getGSP(this).getValue(SP_VIEW_TYPE, 0);
        ((TextView) findViewById(R.id.vst_txt_title)).setText(getMyTitle());
        //		mSwitchView.setVisibility(isReceive() ? View.VISIBLE : View.GONE);
        //		mSwitchView.setDisplayedChild(mViewType);
        //		mSearchButton.setVisibility(mViewType == VIEW_TYPE_LIST ? View.VISIBLE : View
        // .INVISIBLE);

        mSmAdapter = getAdapter();
        mSmAdapter.setSelectListener(mSelectListener);
        mSmListView.setAdapter(mSmAdapter);
        mSmListView.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                int total = mSmListView.getCount();
                for (int i = 0; i < total; i++) {
                    if (i != groupPosition) {
                        mSmListView.collapseGroup(i);
                    }
                }
            }
        });
        RelativeLayout mSendTitle = (RelativeLayout) findViewById(R.id.rl_send_title);
        mSendTitle.setVisibility(mSmAdapter.isReceive() ? View.GONE : View.VISIBLE);

        mSeriesAdapter = new SeriesAdapter(this, mSeriesInfos);
        mSeriesListView.setAdapter(mSeriesAdapter);

		/* The project is PbbReader. */
        //点击MainUI的我要阅读后，传递参数判断，不使用isReaderProject()
        this.tagWay = getIntent().getStringExtra(BaseActivity.TAG_WAY);
        if (isReaderProject()) {
        //if (BaseActivity.PBB_READER.equals(getIntent().getStringExtra(BaseActivity.TAG_WAY))) {
            GlobalData.Sm.instance(this).searchFromSysDB();
            if (NetUtil.isNetInUse(this)) {
                UpdateTool.checkUpdateInfo(ReceiveActivity.this, false);
                //				UpdateTool.checkNewInfo(this);
            }
        } else {
            mSettingBackButton.setBackgroundResource(R.drawable.ic_back);
            reGetDatasAsync();
        }
    }

    protected boolean isReaderProject() {
        return BaseActivity.PBB_READER.equals(tagWay);
    }

    @Override
    public void findViewAndSetListeners() {
        mTopView = (ViewAnimator) findViewById(R.id.arl_lyt_top);
        mSettingBackButton = (ImageButton) findViewById(R.id.vst_imb_setting_back);
        //		mSwitchView = (ImageSwitcher) findViewById(R.id.vst_lyt_switch_view);
        //		mSearchButton = (ImageButton) findViewById(R.id.vst_imb_search);
        // 		mSearchTxt = (TextView) findViewById(R.id.searchTxt);
        //		mSidSearchButton = (ImageButton) findViewById(R.id.vst_imb_sid_search);
        // mAddButton = (HighlightImageView) findViewById(R.id.vst_imb_add);
        mSearchText = (PycAutoTextGray) findViewById(R.id.vsnv_edt_search);
        mDeleteNumText = (TextView) findViewById(R.id.vsd_txt_num);
        mSeriesNameText = (TextView) findViewById(R.id.vst_txt_series_name);

        //mSmListView = (ExpandableListView) findViewById(R.id.arl_lsv_files);
        mSmListView = (ExpandableListView) findViewById(R.id.swipe_target);
        mSeriesListView = (ListView) findViewById(R.id.arl_lsv_series);
        //mPullRefreshView = (PullRefreshView) findViewById(R.id.arl_lyt_pull_refresh);
        mPullRefreshView = (SwipeToLoadLayout) findViewById(R.id.arl_lyt_pull_refresh);
        mPullRefreshView.setRefreshEnabled(true);
        mPullRefreshView.setLoadMoreEnabled(false);
        mDelleteBottomView = (RelativeLayout) findViewById(R.id.rl_dellete_bottom_veiw);
        //mSendBack = (HighlightImageView) findViewById(R.id.send_back_img);
        findViewById(R.id.send_back_img).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        mEmptyView = findViewById(R.id.arl_lyt_empty);

        //		mSwitchView.setOnClickListener(new OnClickListener()
        //		{
        //
        //			@Override
        //			public void onClick(View v)
        //			{
        //				switchViewType();
        //			}
        //		});

        mSettingBackButton = (ImageButton) findViewById(R.id.vst_imb_setting_back);
        mSettingBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReaderProject()) {
                    setNew = 0;
                    mSettingBackButton.setBackgroundResource(R.drawable.xml_setting_normal);
                    //					startActivity(new Intent(ReceiveActivity.this,
                    // SettingActivity.class));
                    startActivity(new Intent(ReceiveActivity.this, UserCenterActivity.class));

                } else {
                    //					if (!back())
                    //					{
                    //						onBackPressed();
                    //					}
                }
            }
        });

        mSmListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long
                    id) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
                if (!mSmAdapter.isSelecting()) {
                    changeToMode(TopBarMode.Delete);
                    mDelleteBottomView.setVisibility(View.VISIBLE);
                    int pos = position;
                    if (mSmAdapter.getExpandPos() != -1 && mSmAdapter.getExpandPos() < pos) {
                        pos--;
                    }
                    mSmAdapter.setItemSelected(pos);
                } else {
                    backMode();    //Cancel the delete mode and back to previous one.
                    mDelleteBottomView.setVisibility(View.GONE);
                }
                refreshUI();
                return true;
            }
        });
//        mPullRefreshView.setOnRefreshListener(new OnRefreshListener() {
//
//            @Override
//            public void onRefresh() {
//                isFromUser = true;
//                search();
//            }
//        });

        mPullRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isSearchRefeshing) {
                    isFromUser = true;
                    search();
                    isSearchRefeshing = true;
                }
            }
        });
        mPullRefreshView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (currentPage < totalPageNum) {
                    currentPage++;
                    getRecommandSearchData(searchText, currentPage);
                } else {
                    mPullRefreshView.setLoadingMore(false);
                    mPullRefreshView.setLoadMoreEnabled(false);
                    showToast(getString(R.string.the_last_page));
                }
            }
        });

        mSeriesListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //				changeToMode(TopBarMode.SeriesID);
                int oldSid = mSid;
                mSid = mSeriesAdapter.getSeriesId(position);
                mSeriesNameText.setText(mSeriesAdapter.getSeriesName(position));
                if (oldSid != mSid || reGetSidInfos) {
                    reGetSidInfos = false;
                    SmInfo info = new SmInfo();
                    info.setSid(mSid);
                    ArrayList<String> paths = calculateSidPaths(info);
                    mSidPaths.clear();
                    mSidPaths.addAll(paths);
                }
                startActivity(new Intent(getApplicationContext(), ReceiveSeriesListActivity.class)
                        .putExtra("title", mSeriesAdapter.getSeriesName(position)).putExtra("sid",
                                mSid));
                //				refreshUI();
            }
        });

    }

//    private void switchViewType() {
//        mViewType ^= 1;
//        refreshUI();
//        PbbSP.getGSP(ReceiveActivity.this).putValue(SP_VIEW_TYPE, mViewType);
//    }

    /**
     * Refresh according to the cache data. Not get from database or net.
     */
    @Override
    protected void refreshUI() {
        dismissKeyboard();

        isAddRecommend = false;
        currentPage = 1;
        isOnSearch = false;
        mSearchText.setText(null);
        //((TextView) findViewById(R.id.complete)).setText("搜索");
        ((TextView) findViewById(R.id.searchTxt)).setText("搜索");

        //mPullRefreshView.setRefreshComplete();

        final TopBarMode curMode = mHistoryMode.peek();
        mTopView.setDisplayedChild(curMode.ordinal());
        //		mSwitchView.setDisplayedChild(mViewType);

		/* The only view which is different with the most. */
        if (curMode == TopBarMode.Normal && mViewType == VIEW_TYPE_SERIES) {
            //			mSearchButton.setVisibility(View.INVISIBLE);
            mSmListView.setVisibility(View.GONE);
            mSeriesListView.setVisibility(View.VISIBLE);
            showEmptyView(mSeriesAdapter.isEmpty());
            //mSeriesAdapter.notifyDataSetChanged();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSeriesAdapter.notifyDataSetChanged();
                }
            });
        } else {
            if (curMode == TopBarMode.Normal) {
                mPaths.clear();
                mPaths.addAll(mTotalPaths);
            } else if (curMode == TopBarMode.SeriesID) {
                mPaths.clear();
                mPaths.addAll(mSidPaths);
            }

            mSmAdapter.setSelectable(curMode == TopBarMode.Delete);
            mSmListView.setVisibility(View.VISIBLE);
            mSeriesListView.setVisibility(View.GONE);
            showEmptyView(mSmAdapter.isEmpty());
            mSmAdapter.setSearchText(null);
            //mSmAdapter.notifyDataSetChanged();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSmAdapter.notifyDataSetChanged();
                }
            });
            //			mSearchButton.setVisibility(mSmAdapter.isEmpty() ? View.INVISIBLE : View
            // .VISIBLE);
            //			mSidSearchButton.setVisibility(mSmAdapter.isEmpty() ? View.INVISIBLE :
            // View.VISIBLE);

            int expand = getIntent().getIntExtra(EXPAND_GROUP, -1);
            if (expand >= 0 && expand < mSmAdapter.getGroupCount()) {
                mSmListView.expandGroup(expand);
                getIntent().removeExtra(EXPAND_GROUP);
            }

			/* If empty,auto back. */
            if (!isRefreshing && mSmAdapter.isEmpty()
                    && (curMode == TopBarMode.Search || curMode == TopBarMode.SeriesID)) {
                backMode();
                refreshUI();
            }
        }
    }

    /**
     * Clear all data and get again.<br>
     * Run in non-UI thread.
     */
    private void reGetDatasAsync() {
        GlobalTask.executeOrderTask(new Runnable() {
            @Override
            public void run() {
                isRefreshing = true;
                isLoading = true;
                /* Change UI first,then refresh data. */
                postRefreshUI();

                TopBarMode curMode = mHistoryMode.peek();

				/* get total data */
                ArrayList<String> paths = getPaths();
                mTotalPaths.clear();
                mTotalPaths.addAll(paths);
                getDatasFromDB();    //Get the latest info without connecting net and refresh
                // nothing.
                /* Make sure that the current view matches it. */
                if (curMode == TopBarMode.Normal && mViewType == VIEW_TYPE_LIST) {
                    isLoading = false;
                    postRefreshUI();
                }
                /* get series group */
                ArrayList<SmInfo> seriesInfos = mSerisesDao.getSeriesInfos();
                ArrayList<SmInfo> delSids = new ArrayList<>();
                for (SmInfo info : seriesInfos) {
                    int size = calculateSidPaths(info).size();
                    info.setSeriesReceiveNum(size);
                    if (size == 0)    //Delete by user not in our project.
                    {
                        mSerisesDao.delete(info);
                        delSids.add(info);
                    }
                }
                seriesInfos.removeAll(delSids);
                SmInfo info = new SmInfo();
                info.setSid(SmInfo.NO_SID);
                int zeroSize = calculateSidPaths(info).size();
                if (zeroSize > 0) {
                    SmInfo zeroInfo = new SmInfo();
                    zeroInfo.setSid(SmInfo.NO_SID);
                    zeroInfo.setSeriesName("未分组文件");
                    zeroInfo.setSeriesReceiveNum(zeroSize);
                    seriesInfos.add(zeroInfo);
                }
                mSeriesInfos.clear();
                mSeriesInfos.addAll(seriesInfos);
                /* Make sure that the current view matches it. */
                if (curMode == TopBarMode.Normal && mViewType == VIEW_TYPE_SERIES) {
                    isLoading = false;
                    postRefreshUI();
                }

				/* get sid data */
                SmInfo info2 = new SmInfo();
                info2.setSid(mSid);
                paths = calculateSidPaths(info2);
                mSidPaths.clear();
                mSidPaths.addAll(paths);
                /* Make sure that the current view matches it. */
                if (curMode == TopBarMode.SeriesID) {
                    isLoading = false;
                    postRefreshUI();
                }

                if (isFromUser) {
                    refresh(mDatas);
                    isFromUser = false;
                }
                isRefreshing = false;
            }
        });
    }

    private void showEmptyView(boolean empty) {
        /* Should not show empty view when loading. */
        mEmptyView.setVisibility(!empty || isLoading ? View.GONE : View.VISIBLE);
    }

    /*-*******************************
     * TODO Refresh
     ********************************/
    private boolean isRefreshing = false;
    private boolean isLoading = true;

    private void getDatasFromDB() {
        ArrayList<String> paths = mTotalPaths;
        HashMap<String, SmInfo> datas = mDatas;
        SmDao smDao = mSmDao;
        for (String path : paths) {
            SmInfo info = datas.get(path);    //Get the cache info.
            if (info == null || info.getFid() < 0) {
                info = XCoder.analysisSmFile(path).getSmInfo();
                datas.put(path, info);    //replace
            }
            smDao.query(info);
        }
    }

    /**
     * Connect net to refresh data.
     */
    private void refresh(HashMap<String, SmInfo> needRefresInfos) {
        if (needRefresInfos.size() <= 0) {
            return;
        }

        if (!NetUtil.isNetInUse(this)) {
            GlobalToast.toastShort(this, "没有可用网络");
            return;
        }

        SmConnect smConnect = new SmConnect(this);
        SmRefresher refresher = new SmRefresher();
        ArrayList<String> sendIds = refresher.getSendFids(needRefresInfos);
        for (String ids : sendIds) {
            // Every ten items in a group.
            byte[] result = smConnect.getFileInfos(ids.getBytes(), isReceive(), false);
            if (result != null) {
                HashMap<String, SmInfo> sucInfos = refresher.analysisReceiveData(result,
                        needRefresInfos);
                mSmDao.updateOrInsert(sucInfos.values());
                //UIHandler.post(new Runnable() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSmAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private void changeToMode(TopBarMode toMode) {

        if (toMode == TopBarMode.Normal) {
            mHistoryMode.clear();
            mSmAdapter.setSearchText("");
        }
        mHistoryMode.push(toMode);
    }

    private void backMode() {
        TopBarMode rmMode = mHistoryMode.pop();
        if (rmMode == TopBarMode.Search) {
            /*
             * The search mode can live with delete mode, so we can't clear it
			 * in the method of "refreshUI".
			 */
            mSmAdapter.setSearchText("");
        }

		/* If empty,auto back. */
        TopBarMode curMode = mHistoryMode.peek();
        if (mSmAdapter.isEmpty()
                && (curMode == TopBarMode.Search || curMode == TopBarMode.SeriesID)) {
            backMode();
        }
    }

    //	@Override
    //	public void onBackPressed()
    //	{
    //		TopBarMode curMode = mHistoryMode.peek();
    //		if (curMode==TopBarMode.Delete)
    //		{
    //			mDelleteBottomView.setVisibility(View.GONE);
    //			backMode();
    //		}else {
    //			super.onBackPressed();
    //		}
    //	}

    private ArrayList<String> calculateSidPaths(SmInfo seriesInfo) {
        ArrayList<String> sidPaths = new ArrayList<>();
        HashMap<String, SmInfo> datas = mDatas;
        ArrayList<String> paths = mTotalPaths;
        for (String path : paths) {
            SmInfo info = datas.get(path);
            if (info != null && info.getSid() == seriesInfo.getSid()) {
                sidPaths.add(path);

                seriesInfo.setPayFile(info.isPayFile() ? 1 : 0);
            }
        }
        return sidPaths;
    }

    @Deprecated
    public void onSearchClick(View v) {
        if (!mSmAdapter.isEmpty()) {
            TopBarMode oldMode = mHistoryMode.peek();
            changeToMode(TopBarMode.Search);
            mTopView.setDisplayedChild(TopBarMode.Search.ordinal());
            mSearchText.requestFocus();
            mSearchText.setHint(oldMode == TopBarMode.SeriesID ? "搜索该系列" : "搜索全部文件");
            showKeyboard();
        }
    }

    // 取消删除
    public void onCancelClick(View v) {
        mDelleteBottomView.setVisibility(View.GONE);
        backMode();
        refreshUI();
    }

    //原先搜索界面(search+delete)。以防需求改回，代码暂未删除，只是原先界面隐藏
    @Deprecated
    public void onCompleteClick(View v) {
        TopBarMode curMode = mHistoryMode.peek();
        switch (curMode) {
            case Search:
                search(mSearchText.getText().toString());
                break;
            case Delete:
                deleteFiles(mSmAdapter.getSelected().toArray(new String[]{}));
                break;
            default:
                break;
        }
    }

    //新需求 搜索条目显示在标题之下，ListView之上。 点击 “搜索” 时的逻辑
    public void onSearchClick1(View v) {
        clearRecommendView();
        isOnSearch = !isOnSearch;
        if (isOnSearch) {
            searchText = mSearchText.getText().toString().trim();
            if (TextUtils.isEmpty(searchText)) {
                GlobalToast.toastShort(this, "请输入搜索内容");
                isOnSearch = false;
                return;
            }
            ((TextView) v).setText("取消");
            showEmptyView(false);
            search(searchText);
        } else {
            refreshUI();
            mSearchText.setText(null);
            ((TextView) v).setText("搜索");
            showEmptyView(mSmAdapter != null && mSmAdapter.isEmpty());
        }
    }

    private void clearRecommendView() {
        if (mRecommendViews == null) return;
        for (View view : mRecommendViews) {
            if (view != null) {
                mSmListView.removeFooterView(view);
            }
        }
    }

	/*-**************************************
     * TODO Delete and Search
	 ****************************************/

    private void deleteFiles(final String... paths) {
        if (paths.length == 0) {
            GlobalToast.toastShort(this, "请选择删除文件");
            return;
        }

        View v = getLayoutInflater().inflate(R.layout.dialog_delete, null);

        final Dialog dialog = new Dialog(this, R.style.no_frame_small);
        dialog.setContentView(v);
        dialog.show();

        TextView t = (TextView) v.findViewById(R.id.dd_txt_content);
        Button b1 = (Button) v.findViewById(R.id.dd_btn_sure);
        Button b2 = (Button) v.findViewById(R.id.dd_btn_cancel);

        t.setText("是否删除选中项?");
        b1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPaths.removeAll(GlobalData.Sm.instance(ReceiveActivity.this).delete(paths));
                backMode();
                reGetDatasAsync();
                mDelleteBottomView.setVisibility(View.GONE);
                dialog.dismiss();
            }
        });
        b2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

		/*
         * DialogInfo info = new DialogInfo();
		 * info.prompt = "是否删除选中项？";
		 * info.positiveTask = new Runnable()
		 * {
		 * @Override
		 * public void run()
		 * {
		 * GlobalTask.executeDialog(new Runnable()
		 * {
		 * @Override
		 * public void run()
		 * {
		 * mPaths.removeAll(GlobalData.Sm.instance(ReceiveActivity.this).delete(
		 * paths));
		 * backMode();
		 * reGetDatasAsync();
		 * }
		 * }, GlobalDialog.showProgressDialog(ReceiveActivity.this,
		 * "正在删除所选文件..."));
		 * }
		 * };
		 * GlobalDialog.showConfirmDialog(this, info);
		 */
    }

    private ISelectListener mSelectListener = new ISelectListener() {

        @Override
        public void onSelcetChanged(boolean overflow, int total, boolean allSelected) {
            mDeleteNumText.setText("已选择" + total + "个文件");
        }
    };

    //	private void setAutoAdapter()
    //	{
    //		Set<String> autos = PbbSP.getGSP(this).getSharedPreferences()
    //				.getStringSet(PbbSP.SP_AUTO_SEARCH, null);
    //		if (autos != null)
    //		{
    //			g_edtSearch.setAdapter(new ArrayAdapter<String>(this,
    //					android.R.layout.simple_dropdown_item_1line, autos.toArray(new String[]
    // {})));
    //		}
    //	}

    private void search(String searchText) {
        if (TextUtils.isEmpty(searchText)) {
            GlobalToast.toastShort(this, "请输入搜索内容");
        } else {
            //			PbbSP.setAutoSearchText(this, searchText);
            ArrayList<String> searchPaths = new ArrayList<String>();
            ArrayList<String> paths = new ArrayList<>(mPaths);
            for (String path : paths) {
                String name = FileUtil.getFileName(path);
                name = name.substring(0, name.lastIndexOf("."));    //Don't consider the string
                // like ".pbb" or ".pyc".
                if (name.contains(searchText)) {
                    searchPaths.add(path);
                }
            }

            if (searchPaths.isEmpty()) {
                GlobalToast.toastLong(this, "没有搜索到匹配结果！");
            }
            //			else
            //			{
            //				changeToMode(TopBarMode.Search);
            dismissKeyboard();
            mSmAdapter.setSearchText(searchText);
            mPaths.clear();
            mPaths.addAll(searchPaths);
            mSmAdapter.notifyDataSetChanged();

            //TODO:搜索扩展
            getRecommandSearchData(searchText, currentPage);


            //				refreshUI();
            //mSmAdapter.notifyDataSetChanged();
            //				mSeriesAdapter.notifyDataSetChanged();
            //			}
        }
    }


    /*
     * 获取 *猜你喜欢* 数据
     */
    private void getRecommandSearchData(String searchText, int page) {
        if (isSearching) return;
        isSearching = true;
        Bundle bundle = new Bundle();
        bundle.putString("keyword", searchText);
        bundle.putString("username", (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, ""));
        bundle.putString("token", (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, ""));
        bundle.putInt("page", page);
        bundle.putString("IMEI", com.sz.mobilesdk.common.Constant.TOKEN);
        bundle.putString("application_name", "Reader for Android");
        GlobalHttp.post(APIUtil.getRecommandSearchUrl(), bundle,
                new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String arg0) {
                        SZLog.d("onSuccess:", arg0);
                        paserData(arg0);
                    }

                    @Override
                    public void onFinished() {
                        isSearching = false;
                        mPullRefreshView.setLoadingMore(false);
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        SZLog.e("onError", arg0.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                    }
                });
    }

    private void paserData(String arg0) {
        SearchResultModel model = JSON.parseObject(arg0, SearchResultModel.class);
        if (model.isSuccess()) {
            SearchResultModel.SearchInfo info = model.getData();
            if (info == null) {
                //showTipsText(getString(R.string.load_server_failed));
                return;
            }
            //SearchResultModel.MyProduct myProduct = info.getMyProducts();
            SearchResultModel.SearchProduct searchProduct = info.getSearchProducts();
            //SearchResultModel.RecommendProduct recommendProduct = info.getRecommendProducts();

            //if (myProduct == null || searchProduct == null || recommendProduct == null) {
            if (searchProduct == null) {
                //showTipsText(getString(R.string.search_empty));
                return;
            }
            List<SearchResultModel.SearchResult> contents = new ArrayList<>();
            contents.clear();
            //List<SearchResultModel.SearchResult> myList = myProduct.getItems();
            List<SearchResultModel.SearchResult> searchList = searchProduct.getItems();
            //List<SearchResultModel.SearchResult> recommondList = recommendProduct.getItems();
            //boolean myEmpty = (myList == null || myList.isEmpty());
            boolean searchEmpty = (searchList == null || searchList.isEmpty());
            //boolean recommondEmpty = (recommondList == null || recommondList.isEmpty());
            totalPageNum = searchProduct.getTotalPageNum();
            //if (searchEmpty) totalPageNum = recommendProduct.getTotalPageNum();
            //if (!myEmpty) contents.addAll(myList);
            if (!searchEmpty) contents.addAll(searchList);
            //if (!recommondEmpty) contents.addAll(recommondList);
            SZLog.d(TAG, "search：" + searchList.size());
            if (contents.isEmpty()) return;

            SZLog.i("currentPage = " + currentPage);
            SZLog.i("totalPageNum = " + totalPageNum);
            SZLog.i("contentSize = " + contents.size());
            mPullRefreshView.setLoadingMore(currentPage < totalPageNum);
            mPullRefreshView.setLoadMoreEnabled(currentPage < totalPageNum);
            //搜索后重新adapter数据！
            initRecommend4Search(info.getAccessLogId(), contents);
        }
    }


    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        switch ((ObTag) data) {
            case Refresh: {
                reGetDatasAsync();
                mPullRefreshView.setRefreshing(false);
                isSearchRefeshing = false;
            }
            break;

            case Update:
                if (isReaderProject()) {
                    if (setNew == -1 && UpdateTool.isAnyNew(this)) {
                        mSettingBackButton.setBackgroundResource(R.drawable.xml_setting_new);
                        setNew = 1;
                    } else {
                        mSettingBackButton.setBackgroundResource(R.drawable.xml_setting_normal);
                    }
                }
                break;

            default:
                break;
        }
    }

    //	@Override
    //	protected ExitMode getExitMode()
    //	{
    //		if (isReaderProject())
    //		{
    //			return ExitMode.DoubleClick;
    //		}
    //		else
    //		{
    //			return super.getExitMode();
    //		}
    //	}
    //
    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //if (keyCode == KeyEvent.KEYCODE_BACK && back())
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (back()) {
                mDelleteBottomView.setVisibility(View.GONE);
            } else {
                if (this.isReceive()) {
                    UIHelper.showExitTips(this);
                } else {
                    finish();
                }
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }*/

    private boolean back() {
        if (mHistoryMode.peek() != TopBarMode.Normal) {
            backMode();
            refreshUI();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean readSuc = requestCode == RESULT_READ && resultCode == RESULT_OK && data != null;
        if (readSuc && mHistoryMode.peek() == TopBarMode.SeriesID) {
            SmInfo info = (SmInfo) data
                    .getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
            if (info.getSid() != mSid) {
                GlobalToast.toastLong(this, "文件已迁移至分组：" + info.getSeriesName());
            }
        }

        //PBBOnline
        //		boolean isScanResult = (resultCode == RESULT_OK
        //				&& requestCode == CaptureActivity.REQUEST_CODE_SCAN && data != null);
        //		if (isScanResult)
        //		{
        //			Bundle bundle = data.getExtras();
        //			String content = bundle.getString(CaptureActivity.DECODED_CONTENT_KEY);
        //			SZLog.e("", "scan: " + content);
        //			paserContentUrl(ReceiveActivity.this, content, true);
        //		}

        //Intent intent = new Intent(ReceiveActivity.this, ShareDetailsPageActivity.class);
        //startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SZLog.v("receive", "onDestroy");
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(IsSierisModeEvent event) {
        mViewType = event.getViewType();
        refreshUI();
    }

    /**
     * 猜您喜欢
     */
    private void initRecommend4Search(final String logId, List<SearchResultModel.SearchResult>
            contents) {

        View mLableView = View.inflate(this, R.layout.view_recommend_lable, null);
        mRecommendView = View.inflate(this, R.layout.view_recommend_search, null);
        mSmListView.removeFooterView(mRecommendView);
        if (!isAddRecommend) {
            mSmListView.addFooterView(mLableView);
            mRecommendViews.add(mLableView);
            isAddRecommend = true;
        }
        mSmListView.addFooterView(mRecommendView);
        mRecommendViews.add(mRecommendView);

        LinearLayout groupView = (LinearLayout) mRecommendView.findViewById(R.id
                .container_recommend_search);
        groupView.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < contents.size(); i++) {
            final SearchResultModel.SearchResult result = contents.get(i);
            View itemView = inflater.inflate(R.layout.view_item_recommend, groupView, false);
            ImageView img = (ImageView) itemView.findViewById(R.id.recommend_img);
            //ImageLoader.getInstance().displayImage(result.getPicture_url(), img);
            ImageLoadHelp.loadImage(img,result.getPicture_url());
            TextView name = (TextView) itemView.findViewById(R.id.recommend_name);
            name.setText(result.getProductName());
            TextView ower = (TextView) itemView.findViewById(R.id.recommend_ower);
            ower.setText(result.getAuthors());

            itemView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String url = APIUtil.getProDetailsUrl(result.getProId(), logId);
                    BrowserActivity.openAppBrowser(ReceiveActivity.this, url);
                }
            });
            groupView.addView(itemView);
        }
        initSmsListView();
    }

    private void initSmsListView() {
        mSmAdapter = getAdapter();
        if (!TextUtils.isEmpty(searchText)) mSmAdapter.setSearchText(searchText);
        mSmAdapter.setSelectListener(mSelectListener);
        mSmListView.setAdapter(mSmAdapter);
        mSmListView.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                int total = mSmListView.getCount();
                for (int i = 0; i < total; i++) {
                    if (i != groupPosition) {
                        mSmListView.collapseGroup(i);
                    }
                }
            }
        });
    }
}
