package cn.com.pyc.suizhi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.qlk.util.tool.Util;
import com.sz.mobilesdk.manager.ImageLoadHelp;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SZLog;

import org.xutils.common.Callback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.db.sm.SmDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.main.PbbFileDetailActivity;
import cn.com.pyc.model.SearchResultModel;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.suizhi.adapter.SearchFileAdapter;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.manager.HttpEngine;
import cn.com.pyc.suizhi.util.OpenUIUtil;
import cn.com.pyc.suizhi.util.SZAPIUtil;
import cn.com.pyc.widget.SearchEditText;
import cn.com.pyc.xcoder.XCoder;

/**
 * 新的搜索页面
 */
public class SZSearchActivity extends ExtraBaseActivity implements View.OnClickListener {

    private static final String TAG = "SZSearch";
    private static final int MSG_SM_END = 100;
    private SwipeToLoadLayout mSwipeToLoadLayout;
    private ListView mListView;
    private TextView mEmptyTextTip;
    private TextView mCancelText;
    private SearchEditText mSearchEditText;
    private List<View> mRecommendViewList = new ArrayList<>();

    private SearchFileAdapter mAdapter;
    private InputMethodManager imManager;
    private SmDao mSmDao;
    private ExecHandler mHandler = new ExecHandler(this);

    private String keyWords;
    private boolean isAddRecommend = false;
    private boolean isSearch = false;
    private boolean isLoading = false;
    private int currentPage = 1;   //默认第一页
    private int totalPageNum = 1;  //默认共一页
    private List<String> paths = new ArrayList<>();
    private List<SmInfo> mSmInfos = new ArrayList<>();

    private static class ExecHandler extends Handler {
        private WeakReference<SZSearchActivity> reference;

        private ExecHandler(SZSearchActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SZSearchActivity activity = reference.get();
            if (activity == null) return;
            if (msg.what == MSG_SM_END) {
                activity.hideLoading();
                activity.hideBgLoading();
                activity.isSearch = false;
                if (activity.mSmInfos == null || activity.mSmInfos.isEmpty()) {
                    activity.mEmptyTextTip.setVisibility(View.VISIBLE);
                    activity.mEmptyTextTip.setText("本地暂无文件");
                    return;
                }
                activity.mEmptyTextTip.setVisibility(View.GONE);
                if (activity.mAdapter == null) {
                    activity.mAdapter = new SearchFileAdapter(activity, activity.mSmInfos);
                    activity.mListView.setAdapter(activity.mAdapter);
                } else {
                    activity.mAdapter.setSmInfos(activity.mSmInfos);
                    activity.mAdapter.notifyDataSetChanged();
                }
                activity.recommend(activity.keyWords);
            }
        }
    }

    private static class SmInfoPathRunnable implements Runnable {

        private WeakReference<SZSearchActivity> mReference;
        private List<String> mPaths;

        private SmInfoPathRunnable(SZSearchActivity activity, List<String> paths) {
            mReference = new WeakReference<>(activity);
            mPaths = paths;
        }

        @Override
        public void run() {
            if (mReference.get() == null) return;
            SZSearchActivity activity = mReference.get();
            SmDao smDao = activity.mSmDao;
            activity.mSmInfos.clear();
            for (String path : mPaths) {
                SmInfo info = XCoder.analysisSmFile(path).getSmInfo();
                smDao.query(info);
                activity.mSmInfos.add(info);
            }
            activity.mHandler.sendEmptyMessage(MSG_SM_END);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_new);
        init();
        initLayout();
        loadPath();
    }

    private void init() {
        ViewHelp.showAppTintStatusBar(this);
        mSmDao = SmDao.getInstance(this, true);
        imManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        paths = GlobalData.Sm.instance(this).getCopyPaths(false);
    }


    private void initLayout() {
        mSearchEditText = ((SearchEditText) findViewById(R.id.search_edittext));
        mCancelText = ((TextView) findViewById(R.id.search_canceltext));
        mSearchEditText.requestFocus();
        mSearchEditText.setCancelTextView(mCancelText, true);
        mSwipeToLoadLayout = ((SwipeToLoadLayout) findViewById(R.id.search_swipe_layout));
        mListView = ((ListView) findViewById(R.id.swipe_target));
        mEmptyTextTip = ((TextView) findViewById(R.id.search_empty_tip));

        mCancelText.setOnClickListener(this);
        mSearchEditText.setOnSearchClickListener(new SearchEditText.OnSearchClickListener() {
            @Override
            public void onSearchClick(View view) {
                keyWords = mSearchEditText.getText().toString().trim();
                if (TextUtils.isEmpty(keyWords)) {
                    showToast(getString(R.string.search_input_keyword));
                    return;
                }
                if (mAdapter != null) {
                    mAdapter.getSmInfos().clear();
                    mAdapter.notifyDataSetChanged();
                    mAdapter = null;
                }
                currentPage = 1;
                search(keyWords);
            }
        });

        mSwipeToLoadLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (currentPage < totalPageNum) {
                    currentPage++;
                    recommend(keyWords);
                } else {
                    mSwipeToLoadLayout.setLoadingMore(false);
                    mSwipeToLoadLayout.setLoadMoreEnabled(false);
                    showToast(getString(cn.com.pyc.pbb.reader.R.string.the_last_page));
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SmInfo smInfo = (SmInfo) mListView.getItemAtPosition(position);
                if (smInfo == null) return;
                Intent intent = new Intent(SZSearchActivity.this, PbbFileDetailActivity.class);
                //intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, readerFilesAdapter
                // .getFilePath(position));
                intent.putExtra("pbb_smInfo", smInfo);
                startActivity(intent);
            }
        });
    }

    private void loadPath() {
        showLoading(this);
        ExecutorManager.getInstance().execute(new SmInfoPathRunnable(this, paths));
    }

    private void search(String keyWord) {
        if (isSearch) return;
        isSearch = true;
        showBgLoading(this);
        ArrayList<String> searchPaths = new ArrayList<>();
        searchPaths.clear();
        ArrayList<String> paths = new ArrayList<>(this.paths);
        for (String path : paths) {
            String name = Util.FileUtil.getFileName(path);
            //Don't consider the string like ".pbb" or ".pyc".
            name = name.substring(0, name.lastIndexOf("."));
//            if (name.contains(keyWord)) {
//                searchPaths.add(path);
//            }
            //不区分大小写匹配字符串
            Pattern pattern = Pattern.compile(keyWord, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                searchPaths.add(path);
            }
        }
        //没有搜索到匹配结果！
        if (searchPaths.isEmpty()) {
            isSearch = false;
            hideBgLoading();
            mEmptyTextTip.setVisibility(View.VISIBLE);
            mEmptyTextTip.setText(getString(R.string.search_empty));
            recommend(keyWord);
        } else {
            ExecutorManager.getInstance().execute(new SmInfoPathRunnable(this, searchPaths));
        }
    }

    //猜您喜欢 推荐产品
    private void recommend(String keyWords) {
        if (TextUtils.isEmpty(keyWords)) return;
        if (!CommonUtil.isNetConnect(this)) return;
        if (isLoading) return;
        isLoading = true;
        Bundle bundle = new Bundle();
        bundle.putString("keyword", keyWords);
        bundle.putString("username", SZConstant.getName());
        bundle.putString("token", SZConstant.getToken());
        bundle.putInt("page", currentPage);
        bundle.putString("IMEI", com.sz.mobilesdk.common.Constant.TOKEN);
        bundle.putString("application_name", DrmPat.APP_FULLNAME);
        HttpEngine.post(SZAPIUtil.getProductSearch(), bundle, new
                Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "recommend：" + s);
                        parserData(s);
                    }

                    @Override
                    public void onError(Throwable throwable, boolean b) {
                        SZLog.e(TAG, throwable.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException e) {
                    }

                    @Override
                    public void onFinished() {
                        isLoading = false;
                        mSwipeToLoadLayout.setLoadingMore(false);
                    }
                });
    }

    private void parserData(String arg0) {
        SearchResultModel model = JSON.parseObject(arg0, SearchResultModel.class);
        if (model.isSuccess()) {
            SearchResultModel.SearchInfo info = model.getData();
            if (info == null) {
                return;
            }
            //SearchResultModel.MyProduct myProduct = info.getMyProducts();
            SearchResultModel.SearchProduct searchProduct = info.getSearchProducts();
            SearchResultModel.RecommendProduct recommendProduct = info.getRecommendProducts();

            //if (myProduct == null || searchProduct == null || recommendProduct == null) {
            if (searchProduct == null || recommendProduct == null) {
                //showTipsText(getString(R.string.search_empty));
                return;
            }
            //数据内容集合
            List<SearchResultModel.SearchResult> contents = new ArrayList<>();
            contents.clear();

            //List<SearchResultModel.SearchResult> myList = myProduct.getItems();
            List<SearchResultModel.SearchResult> searchList = searchProduct.getItems();
            List<SearchResultModel.SearchResult> recommondList = recommendProduct.getItems();
            //boolean myEmpty = (myList == null || myList.isEmpty());
            boolean recommondEmpty = (recommondList == null || recommondList.isEmpty());
            boolean searchEmpty = (searchList == null || searchList.isEmpty());
            if (searchEmpty) { //搜索结果为空
                if (!recommondEmpty) {
                    totalPageNum = recommendProduct.getTotalPageNum();
                    contents.addAll(recommondList);
                }
            } else {
                totalPageNum = searchProduct.getTotalPageNum();
                contents.addAll(searchList);
            }
            //if (!myEmpty) contents.addAll(myList);
            if (contents.isEmpty()) return;

            Log.i(TAG, "currentPage = " + currentPage);
            Log.i(TAG, "totalPageNum = " + totalPageNum);
            Log.i(TAG, "contentSize = " + contents.size());
            mSwipeToLoadLayout.setLoadingMore(currentPage < totalPageNum);
            mSwipeToLoadLayout.setLoadMoreEnabled(currentPage < totalPageNum);
            //搜索后重新adapter数据！
            if (currentPage == 1) {
                clearRecommendView();
            }
            initRecommend4Search(info.getAccessLogId(), contents);
        }
    }


    private void clearRecommendView() {
        if (mRecommendViewList == null || mRecommendViewList.isEmpty()) return;
        for (View view : mRecommendViewList) {
            if (view != null) {
                mListView.removeFooterView(view);
            }
        }
        isAddRecommend = false;
    }

    //猜您喜欢
    private void initRecommend4Search(final String logId, List<SearchResultModel.SearchResult>
            contents) {
        View mLabelView = View.inflate(this, cn.com.pyc.pbb.reader.R.layout.view_recommend_lable,
                null);
        View mRecommendView = View.inflate(this, cn.com.pyc.pbb.reader.R.layout
                        .view_recommend_search,
                null);
        mListView.removeFooterView(mRecommendView);
        if (!isAddRecommend) {
            mListView.addFooterView(mLabelView);
            mRecommendViewList.add(mLabelView);
            isAddRecommend = true;
        }
        mListView.addFooterView(mRecommendView);
        mRecommendViewList.add(mRecommendView);

        LinearLayout groupView = (LinearLayout) mRecommendView.findViewById(cn.com.pyc.pbb.reader
                .R.id.container_recommend_search);
        groupView.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < contents.size(); i++) {
            final SearchResultModel.SearchResult result = contents.get(i);
            View itemView = inflater.inflate(cn.com.pyc.pbb.reader.R.layout.view_item_recommend,
                    groupView, false);
            ImageView img = (ImageView) itemView.findViewById(cn.com.pyc.pbb.reader.R.id
                    .recommend_img);
            ImageLoadHelp.loadImage(img, result.getPicture_url());
            TextView name = (TextView) itemView.findViewById(cn.com.pyc.pbb.reader.R.id
                    .recommend_name);
            name.setText(result.getProductName());
            TextView owner = (TextView) itemView.findViewById(cn.com.pyc.pbb.reader.R.id
                    .recommend_ower);
            owner.setText(result.getSellerName());

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String url = SZAPIUtil.getProductDetailUrl(result.getProId(), logId);
                    //BrowserActivity.openAppBrowser(SZSearchActivity.this, url);
                    OpenUIUtil.openWebViewOfApp2Buy(SZSearchActivity.this, url);
                }
            });
            groupView.addView(itemView);
        }
        //如果为空，使用addFooterView,再设置一下adapter,
        if (mSmInfos == null || mSmInfos.isEmpty()) {
            mAdapter = new SearchFileAdapter(this, mSmInfos);
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.search_canceltext) {
            mSearchEditText.setText(null);
            hideKeyboard(mCancelText);
            finishUI();
        }
    }

    private void hideKeyboard(View v) {
        if (imManager != null && imManager.isActive()) {
            imManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    private void finishUI() {
//        HttpEngine.cancelHttp(searchCancelable);
//        if (mSwipeToLoadLayout.isLoadingMore())
//            mSwipeToLoadLayout.setLoadingMore(false);
//        if (tempMyLists != null) tempMyLists.clear();
//        tempMyLists = null;
        clearRecommendView();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
