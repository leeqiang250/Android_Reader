package cn.com.pyc.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.qlk.util.event.PathsEvent;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.TimeUtil;
import com.sz.mobilesdk.util.UIHelper;

import org.xutils.common.Callback;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.RZListBean;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.bean.event.SeriesPbbFileEvent;
import cn.com.pyc.db.SerisesDao;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.db.sm.SmDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.main.adapter.DeleteFilesAdapter;
import cn.com.pyc.main.adapter.ReaderFilesAdapter;
import cn.com.pyc.media.MediaActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.plain.record.MusicRecordActivity;
import cn.com.pyc.receive.FindFileActivity;
import cn.com.pyc.sm.PayLimitConditionActivity;
import cn.com.pyc.sm.SendActivity2;
import cn.com.pyc.suizhi.SZSearchActivity;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.manager.HttpEngine;
import cn.com.pyc.suizhi.model.ProductInfo;
import cn.com.pyc.suizhi.model.ProductListModel;
import cn.com.pyc.suizhi.util.OpenUIUtil;
import cn.com.pyc.suizhi.util.SZAPIUtil;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.UserInfoActivity;
import cn.com.pyc.utils.ClearKeyUtil;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.utils.LoginControll;
import cn.com.pyc.utils.ViewUtil;
import de.greenrobot.event.EventBus;

/**
 * Created by 熊大 on 2017/8/21.
 * <p>
 * update by hudq on 2017.10.26
 * <p>
 * 显示加密和阅读页面
 */
public class CodeAndReadActivity2 extends ExtraBaseActivity implements View.OnClickListener {

    private static final String TAG = "CAR";
    private static final String DATE_FORMATER = "yyyy-MM-dd HH:mm";
    private static final int MSG_SM_END = 200;
    private static final int MSG_SM_FAILED = 100;

    private RelativeLayout rlCodeView;                  //加密Tab下的视图
    private RelativeLayout rlReadView;                  //阅读Tab下的视图
    //private LinearLayout llTabCode;                     //点击 加密Tab控件
    //private LinearLayout llTabRead;                     //点击 阅读Tab控件

    private LinearLayout llSendCounts;                  //已发送数目布局
    private TextView tvSendCount;                       //已发送数目显示
    private SwipeToLoadLayout mSwipeLoadLayout;         //下拉刷新视图
    private ListView mListView;                         //列表
    private ImageView menuText;                          //“+”菜单按钮
    private View emptyView;                             //无文件时候 显示空白视图
    private TextView emptyText;                         //无文件时候 显示空白提示文字
    private PopupWindow popupWindow;                    //点击➕按钮弹出
    private FrameLayout frameLayout;                    //混合数据列表布局（编辑删除时隐藏）
    private ListView deleteList;                        //点'编辑'时显示的本地列表（删除功能只针对本地文件）
    private RelativeLayout ll_delete;                   //删除界面
    private RelativeLayout rl_search;                   //搜索界面

    private boolean isReadTabShow;                      //判断显示Tab: true加密 or read
    private String g_strMediaPath;                      //拍照图片的文件路径
    private SmDao mSmDao;                               //文件信息存储dao
    private SerisesDao mSerisesDao;                     //系列文件信息存储dao
    private List<RZListBean> mSeriesContent = new ArrayList<>();//系列对应的RZListBean集合
    private List<String> mPaths = new ArrayList<>();    //本地文件的路径集合
    private ExecHandler mHandler = new ExecHandler(this);
    private int currentPage = 1;
    private int totalPageNum = 1;
    private ReaderFilesAdapter readerFilesAdapter;      //文件列表数据adapter
    private DeleteFilesAdapter deleteFilesAdapter;      //删除的adapter
    private boolean workByUser = false;                 //区分第一次进人是否是来自用户的操作

    private static class SmInfoPathRunnable implements Runnable {
        private WeakReference<CodeAndReadActivity2> mReference;

        private SmInfoPathRunnable(CodeAndReadActivity2 activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            if (mReference.get() == null) return;
            CodeAndReadActivity2 activity = mReference.get();
            //查询数据库中的smInfo；
            List<SmInfo> smInfos = activity.mSmDao.getSmInfos();
            if (smInfos.isEmpty()) {
                activity.mHandler.sendEmptyMessage(MSG_SM_END);
            } else {
                Map<String, List<String>> mSeriesPathMap = new ConcurrentHashMap<>();
                for (SmInfo smInfo : smInfos) {
                    activity.mSerisesDao.query(smInfo);
                    Log.d(TAG, "db smPath: " + smInfo.getFilePath() + " ; series name: " + smInfo
                            .getSeriesName());
                    String saveSeriesName = smInfo.getSeriesName();//保存的系列名
                    String saveFilePath = smInfo.getFilePath();//保存的文件路径
                    //保存系列名不为空,并且扫描的路径中包含此文件的路径，则该文件就是系列文件
                    if (!TextUtils.isEmpty(saveSeriesName)
                            && activity.mPaths.contains(saveFilePath)) {
                        activity.mPaths.remove(saveFilePath); //移除该文件的路径
                        if (mSeriesPathMap.containsKey(saveSeriesName)) {
                            mSeriesPathMap.get(saveSeriesName).add(saveFilePath);
                        } else {
                            RZListBean bean = new RZListBean();
                            bean.setSource(RZListBean.Source.S_SERIES);
                            bean.setName(saveSeriesName);
                            bean.setOwner(smInfo.getNick());
                            bean.setFilePath(saveFilePath);
                            bean.setTime(TimeUtil.getDateStringFromMills(new File(saveFilePath)
                                    .lastModified() + "", DATE_FORMATER));
                            List<String> filePaths = new ArrayList<>();
                            filePaths.add(saveFilePath);

                            mSeriesPathMap.put(saveSeriesName, filePaths);
                            bean.setSeriesMap(mSeriesPathMap);
                            activity.mSeriesContent.add(bean);
                        }
                    }
                }
                activity.mHandler.sendEmptyMessage(MSG_SM_END);
            }
        }
    }

    private static class ExecHandler extends Handler {
        private WeakReference<CodeAndReadActivity2> reference;

        private ExecHandler(CodeAndReadActivity2 activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CodeAndReadActivity2 activity = reference.get();
            if (activity == null) return;
            if (msg.what == MSG_SM_END) {
                //开始加载随知的列表(需要判断是否存在登录的token)
                if (LoginControll.checkLogin())
                    activity.getSZProductList();
                else {
                    activity.mergeData(activity.mPaths, null);
                }
            } else if (msg.what == MSG_SM_FAILED) {
                activity.mSwipeLoadLayout.setRefreshing(false);
                activity.mSwipeLoadLayout.setLoadingMore(false);
            }
        }
    }

    //扫描path完毕，接收通知
    public void onEventMainThread(PathsEvent event) {
        int type = event.getType();
        mPaths = GlobalData.Sm.instance(this).getCopyPaths(false);
        Log.e(TAG, "record(size): " + mPaths.size());
        if (type == PathsEvent.P_PATH) {
            if (mPaths != null && !mPaths.isEmpty()) {
                //开始加载本地列表和随知的列表(需要判断是否存在登录的token)
                ExecutorManager.getInstance().execute(new SmInfoPathRunnable(this));
            } else {
                showEmptyData();
            }
        } else if (type == PathsEvent.P_CLIPER) {
            int sendCount = GlobalData.Sm.instance(this).getCopyPaths(true).size();
            llSendCounts.setVisibility(sendCount > 0 ? View.VISIBLE : View.GONE);
            tvSendCount.setText(sendCount + "");
        }
    }

    //数据path为空，可能是扫描出空，也可能是删除全部后为空。
    private void showEmptyData() {
        if (readerFilesAdapter != null) {
            mSwipeLoadLayout.setRefreshing(false);
            List<RZListBean> mListBean = readerFilesAdapter.getCommonBeanList();
            if (mListBean == null || mListBean.isEmpty()) return;
            List<RZListBean> mList = new ArrayList<>(mListBean);
            for (RZListBean bean : mListBean) {
                if (bean.getSource() != RZListBean.Source.S_SZ) {
                    mList.remove(bean);//过滤掉存在随知的文件的情况
                }
            }
            if (mList.isEmpty()) {
                readerFilesAdapter.getCommonBeanList().clear();
                readerFilesAdapter.notifyDataSetChanged();
                readerFilesAdapter = null;
                showEmptyView();
                return;
            }
            //随知文件，继续显示
            readerFilesAdapter.setCommonBeanList(mList);
            readerFilesAdapter.notifyDataSetChanged();
        } else {
            if (LoginControll.checkLogin())
                getSZProductList();
            else {
                mSwipeLoadLayout.setRefreshing(false);
                showEmptyView();
            }
        }
    }

    //接收到切换到tab2的通知
    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UI_HOME_TAB_2
                || event.getType() == BaseEvent.Type.UI_HOME_TAB_3) {
            if (event.getType() == BaseEvent.Type.UI_HOME_TAB_2) {
                refreshTopTabUI(true);
                workByUser = false;
            }
            mSwipeLoadLayout.setRefreshing(true);
        }
    }

    //接收文件迁移到系列的通知，通知来自PbbFileDetailActivity.
    //该文件必定是系列文件,存在系列才会发送通知。
    public void onEventMainThread(SeriesPbbFileEvent event) {
        SmInfo smInfo = event.getSmInfo();
        checkAddSeries(smInfo);
    }

    //添加到系列
    private void checkAddSeries(SmInfo smInfo) {
        if (readerFilesAdapter == null) return;
        List<RZListBean> mDatas = readerFilesAdapter.getCommonBeanList();
        if (mDatas == null || mDatas.isEmpty()) return;
        String curFilePath = smInfo.getFilePath();
        String curSeriesName = smInfo.getSeriesName();
        String curNickName = smInfo.getNick();
        Log.i(TAG, "cur file: " + curFilePath + "; series name: " + curSeriesName);
        List<RZListBean> mContents = new ArrayList<>();
        mContents.clear();
        boolean hasSeries = false;
        for (RZListBean data : mDatas) {
            if (TextUtils.equals(data.getName(), curSeriesName)) {
                hasSeries = true; //已经存在系列名了。
                break;
            }
        }
        for (RZListBean data : mDatas) {
            if (TextUtils.equals(data.getName(), curSeriesName)) {
                data.getSeriesMap().get(curSeriesName).add(curFilePath);
                mContents.add(data);
            } else if (TextUtils.equals(data.getFilePath(), curFilePath)) {
                if (!hasSeries) {
                    Map<String, List<String>> mSeriesPathMap = new ConcurrentHashMap<>();
                    RZListBean o = new RZListBean();
                    o.setSource(RZListBean.Source.S_SERIES);
                    o.setName(curSeriesName);
                    o.setOwner(curNickName);
                    o.setFilePath(curFilePath);
                    o.setTime(TimeUtil.getDateStringFromMills(new File(curFilePath)
                            .lastModified() + "", DATE_FORMATER));
                    List<String> filePaths = new ArrayList<>();
                    filePaths.add(curFilePath);
                    mSeriesPathMap.put(curSeriesName, filePaths);
                    o.setSeriesMap(mSeriesPathMap);
                    mContents.add(o);
                }
            } else {
                mContents.add(data);
            }
        }
        sortData(mContents);
        readerFilesAdapter.getCommonBeanList().clear();
        readerFilesAdapter.setCommonBeanList(mContents);
        readerFilesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_and_read2);
        initConfig();
        initView();
    }

    private void initConfig() {
        EventBus.getDefault().register(this);
        mSeriesContent.clear();
        mSmDao = SmDao.getInstance(this, true);
        mSerisesDao = SerisesDao.getInstance();
        boolean hasPBBFile = mSmDao.query(); //存在文件数据;
        if (LoginControll.checkLogin()) {
            boolean hasSZFile = AlbumContentDAOImpl.getInstance().existAlbumContent();
            Log.d(TAG, "hasSZFile: " + hasSZFile);
            isReadTabShow = hasPBBFile || hasSZFile;
        } else {
            isReadTabShow = hasPBBFile;
        }
        Log.d(TAG, "hasPBBFile: " + hasPBBFile);
    }

    private void initData() {
        if (workByUser) {
            GlobalData.Sm.instance(this).search(); //搜索本地文件paths
        } else {
            workByUser = true; //第一次进来，加载缓存，之后刷新重新扫描本地sd卡
            GlobalData.Sm.instance(this).searchFromSysDBNew();
        }
        GlobalData.searchTotal(this, true);    // 搜索存储卡文件夹获取已经发送,已经接收,隐私空间数量。
        GlobalData.searchPlainsFromSysDB(this); // 查询本地数据库获取各个多媒体文件的数量
    }

    private void initView() {
        menuText = (ImageView) findViewById(R.id.tv_menu_add);
        rlCodeView = (RelativeLayout) findViewById(R.id.rl_code_view);
        rlReadView = (RelativeLayout) findViewById(R.id.rl_readView);

        View llTabCode = findViewById(R.id.ll_top_tab_code);
        View llTabRead = findViewById(R.id.ll_top_tab_read);

        View rlSendCounts = findViewById(R.id.rl_send_counts);//已制作发送文件数目
        TextView llTakePhoto = (TextView) findViewById(R.id.ll_take_photo); //拍照按钮
        TextView llTakeVideo = (TextView) findViewById(R.id.ll_take_video);//录像
        TextView llTakeAudio = (TextView) findViewById(R.id.ll_take_audio);//录音
        TextView llTakeReview = (TextView) findViewById(R.id.ll_take_review);//浏览

        llSendCounts = (LinearLayout) findViewById(R.id.ll_send_counts);
        tvSendCount = (TextView) findViewById(R.id.tv_send_counts);
        frameLayout = (FrameLayout) findViewById(R.id.frame_list);
        deleteList = (ListView) findViewById(R.id.list_delete);
        rl_search = (RelativeLayout) findViewById(R.id.ll_add_search);
        ll_delete = (RelativeLayout) findViewById(R.id.ll_delete);
        TextView tv_cancel = (TextView) findViewById(R.id.tv_cancle);
        TextView tv_selected_all = (TextView) findViewById(R.id.tv_select_all);
        TextView tv_delete = (TextView) findViewById(R.id.tv_delete);
        ImageView imvSearch = (ImageView) findViewById(R.id.imv_ico_search);
        mSwipeLoadLayout = (SwipeToLoadLayout) findViewById(R.id.lyt_pull_refresh);
        mSwipeLoadLayout.setRefreshEnabled(true);
        mSwipeLoadLayout.setLoadMoreEnabled(false);
        mSwipeLoadLayout.setRefreshing(true);

        mListView = (ListView) findViewById(R.id.swipe_target);
        emptyView = findViewById(R.id.vep_lyt_empty);
        emptyText = (TextView) emptyView.findViewById(R.id.vep_txt_prompt);
        menuText.setOnClickListener(this);

        llTabCode.setOnClickListener(this);
        llTabRead.setOnClickListener(this);
        rlSendCounts.setOnClickListener(this);
        llTakePhoto.setOnClickListener(this);
        llTakeVideo.setOnClickListener(this);
        llTakeAudio.setOnClickListener(this);
        llTakeReview.setOnClickListener(this);
        imvSearch.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        tv_selected_all.setOnClickListener(this);


        //控制加密和阅读视图切换
        refreshTopTabUI(isReadTabShow);

        //下拉刷新动作
        mSwipeLoadLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                mPaths.clear();
                mSeriesContent.clear();
                ViewUtil.hideWidget(emptyView);
                ViewUtil.showWidget(mListView);
                initData();
            }
        });
        mSwipeLoadLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (LoginControll.checkLogin()) {
                    if (currentPage < totalPageNum) {
                        currentPage++;
                        getSZProductList();
                    } else {
                        mSwipeLoadLayout.setLoadingMore(false);
                        mSwipeLoadLayout.setLoadMoreEnabled(false);
                        showToast(getString(cn.com.pyc.pbb.reader.R.string.the_last_page));
                    }
                }
            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mSwipeLoadLayout.isRefreshing() || mSwipeLoadLayout.isLoadingMore()) {
                    showToast("正在更新数据,请稍候");
                    return;
                }
                RZListBean bean = (RZListBean) mListView.getItemAtPosition(position);
                if (bean == null) return;
                int source = bean.getSource();
                if (source == RZListBean.Source.S_SZ) {
                    ProductInfo productInfo = bean.getProductInfo();
                    OpenUIUtil.openFileListPage(CodeAndReadActivity2.this, productInfo);
                } else if (source == RZListBean.Source.S_PBB) {
                    String filePath = bean.getFilePath();
                    Intent intent = new Intent(CodeAndReadActivity2.this, PbbFileDetailActivity
                            .class);
                    intent.putExtra("pbb_path", filePath);
                    startActivity(intent);
                } else if (source == RZListBean.Source.S_SERIES) {
                    Map<String, List<String>> mapInfo = bean.getSeriesMap();
                    ArrayList<String> paths = (ArrayList<String>) mapInfo.get(bean.getName());
                    Intent intent = new Intent(CodeAndReadActivity2.this, SeriesListActivity.class);
                    intent.putStringArrayListExtra("pbb_series_paths", paths);
                    intent.putExtra("pbb_series_name", bean.getName());
                    startActivity(intent);
                } else {
                    Log.e(TAG, "source error!!!");
                }
            }
        });
    }


    //加号按钮 弹出popwindow
    private void onMenuClick() {
        if (popupWindow == null) {
            final View addPopView = getLayoutInflater().inflate(R.layout.xml_add_popwindow2, null);
            popupWindow = new PopupWindow(addPopView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setFocusable(true);
            popupWindow.setOnDismissListener(new PopDismissListener());
            popupWindow.showAsDropDown(menuText, -menuText.getLeft() * 5 / 16, 0);
            backgroundAlpha(0.5f);

            //浏览文件
            addPopView.findViewById(cn.com.pyc.pbb.reader.R.id.xap_open_folder)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            closePop();
                            OpenPageUtil.openActivity(CodeAndReadActivity2.this, FindFileActivity
                                    .class);
                        }
                    });

            //编辑
            addPopView.findViewById(cn.com.pyc.pbb.reader.R.id.xap_edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closePop();
                    //剔除随知文件，只留本地pbb文件
                    if (readerFilesAdapter != null) {
                        List<RZListBean> mListBean = readerFilesAdapter.getCommonBeanList();
                        if (mListBean == null || mListBean.isEmpty()) return;
                        List<RZListBean> mList = new ArrayList<>(mListBean);
                        for (RZListBean bean : mListBean) {
                            if (bean.getSource() == RZListBean.Source.S_SZ) {
                                mList.remove(bean);
                            }
                        }
                        if (mList.isEmpty()) {
                            showToast("本地没有pbb文件");
                            return;
                        }
                        showDeleteList(true);
                        deleteFilesAdapter = new DeleteFilesAdapter(CodeAndReadActivity2.this,
                                mList);
                        deleteList.setAdapter(deleteFilesAdapter);
                    }
                }
            });
        } else {
            closePop();
        }
    }


    private class PopDismissListener implements PopupWindow.OnDismissListener {
        @Override
        public void onDismiss() {
            closePop();
        }
    }

    /* 设置背景透明度 */
    private void backgroundAlpha(float bgAlpha) {
        if (getWindow() == null) return;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0 - 1.0
        getWindow().setAttributes(lp);
    }

    private void closePop() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
        backgroundAlpha(1f);
    }

    //加载随知数据的产品列表
    private void getSZProductList() {
        Bundle bundle = new Bundle();
        bundle.putString("category", "0");
        bundle.putInt("page", currentPage);
        bundle.putString("token", SZConstant.getToken());
        bundle.putString("username", SZConstant.getName());
        bundle.putString("application_name", DrmPat.APP_FULLNAME);
        bundle.putString("app_version", CommonUtil.getAppVersionName(this));
        HttpEngine.post(SZAPIUtil.getProductsListUrl(), bundle,
                new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "requestJSON:" + s);
                        ProductListModel o = JSON.parseObject(s, ProductListModel.class);
                        if (o.isSuccess()) {
                            getDataSuccess(o);
                        } else {
                            if (o.getMsg() != null && o.getMsg().contains("验证")) {
                                showToast("登录已失效！");
                                // 登录token已过期，只显示本地
                                ClearKeyUtil.removeKey();
                                mergeData(mPaths, null);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable, boolean b) {
                        // 加载随知数据失败，只显示本地
                        mergeData(mPaths, null);
                    }

                    @Override
                    public void onCancelled(CancelledException e) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
    }

    private void getDataSuccess(ProductListModel o) {
        if (o.getData() == null) {
            mergeData(mPaths, null);
            return;
        }
        ProductListModel.ProductListInfo oo = o.getData();
        List<ProductInfo> mProductInfos = oo.getItems();
        totalPageNum = oo.getTotalPageNum();

        if (mProductInfos == null || mProductInfos.isEmpty()) {
            mergeData(mPaths, null);
            return;
        }
        Log.d(TAG, "currentPage = " + currentPage);
        Log.d(TAG, "totalPageNum = " + totalPageNum);
        Log.d(TAG, "productCount = " + mProductInfos.size());
        mSwipeLoadLayout.setLoadingMore(currentPage < totalPageNum);
        mSwipeLoadLayout.setLoadMoreEnabled(currentPage < totalPageNum);

        //开始合并两个集合
        mergeData(mPaths, mProductInfos);
    }


    private void mergeData(List<String> paths, List<ProductInfo> mProductInfos) {
        List<RZListBean> contents = new ArrayList<>();
        contents.clear();
        //随知数据
        if (mProductInfos != null && !mProductInfos.isEmpty()) {
            for (ProductInfo productInfo : mProductInfos) {
                RZListBean bean = new RZListBean();
                bean.setSource(RZListBean.Source.S_SZ); //此处来源是随知数据
                bean.setProductInfo(productInfo);
                bean.setName(productInfo.getProductName());
                bean.setOwner(productInfo.getStoreName());
                bean.setTime(productInfo.getProduct_buy_time());
                contents.add(bean);
            }
        }
        if (currentPage == 1) {
            //本地数据
            if (paths != null && !paths.isEmpty()) {
                for (String p : paths) {
                    RZListBean bean = new RZListBean();
                    bean.setSource(RZListBean.Source.S_PBB); //此处来源是PBB数据
                    bean.setFilePath(p);
                    String fileName_ = Util.FileUtil.getFileName(p);
                    bean.setName(fileName_.substring(0, fileName_.length() - 4));
                    bean.setOwner("");
                    bean.setTime(TimeUtil.getDateStringFromMills(new File(p).lastModified() + "",
                            DATE_FORMATER));
                    contents.add(bean);
                }
            }
            //系列显示
            if (mSeriesContent != null && !mSeriesContent.isEmpty()) {
                contents.addAll(mSeriesContent);
            }
            //刷新，从第一页加载，并且清空已有的数据
            if (readerFilesAdapter != null && readerFilesAdapter.getCommonBeanList() != null) {
                readerFilesAdapter.getCommonBeanList().clear();
                //readerFilesAdapter.notifyDataSetChanged();
                readerFilesAdapter = null;
            }
        }

        if (readerFilesAdapter == null) {
            if (contents.isEmpty()) {
                showEmptyView();
            } else {
                sortData(contents);
                //显示ui
                readerFilesAdapter = new ReaderFilesAdapter(this, contents);
                mListView.setAdapter(readerFilesAdapter);
            }
        } else {
            List<RZListBean> commonBeanList = readerFilesAdapter.getCommonBeanList();
            commonBeanList.addAll(contents);
            readerFilesAdapter.setCommonBeanList(commonBeanList);
            readerFilesAdapter.notifyDataSetChanged();
        }

        mSwipeLoadLayout.setRefreshing(false);
        mSwipeLoadLayout.setLoadingMore(false);
    }

    private void showEmptyView() {
        ViewUtil.showWidget(emptyView);
        emptyText.setText("暂无可阅读的文件\n请下拉刷新文件或点击右上角'+'浏览文件");
        ViewUtil.hideWidget(mListView);
    }

    //合并的数据的排序
    private void sortData(List<RZListBean> contents) {
        Collections.sort(contents, new Comparator<RZListBean>() {
            @Override
            public int compare(RZListBean t1, RZListBean t2) {
                if (t1 == null || t2 == null || t2.getTime() == null) {
                    return 0;
                }
                return t2.getTime().compareTo(t1.getTime());
            }
        });
    }


    //设置页面的展示
    private void refreshTopTabUI(boolean showRead) {
        if (showRead) {
            rlCodeView.setVisibility(View.GONE);
            rlReadView.setVisibility(View.VISIBLE);
//            llTabCode.setBackgroundResource(R.color.green_light);
//            llTabRead.setBackgroundResource(R.color.green);
        } else {
            rlCodeView.setVisibility(View.VISIBLE);
            rlReadView.setVisibility(View.GONE);
//            llTabCode.setBackgroundResource(R.color.green);
//            llTabRead.setBackgroundResource(R.color.green_light);
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ll_top_tab_code) {
            refreshTopTabUI(false);
        } else if (id == R.id.ll_top_tab_read) {
            refreshTopTabUI(true);
        } else if (id == R.id.rl_send_counts) {
            startActivity(new Intent(this, SendActivity2.class));
        } else if (id == R.id.ll_take_photo) {
            takePhoto();
        } else if (id == R.id.ll_take_video) {
            takeVideo();
        } else if (id == R.id.ll_take_audio) {
            startActivity(new Intent(this, MusicRecordActivity.class));
        } else if (id == R.id.ll_take_review) {
            query_local_file();
        } else {
            if (mSwipeLoadLayout.isRefreshing() || mSwipeLoadLayout.isLoadingMore())
                return;
            switch (view.getId()) {
                case R.id.imv_ico_search:
                    startActivity(new Intent(this, SZSearchActivity.class));
                    break;
                case R.id.tv_menu_add:
                    onMenuClick();
                    break;
                case R.id.tv_delete:
                    showDeleteMulDialog(this);
                    break;
                case R.id.tv_cancle:
                    workByUser = false;
                    showDeleteList(false);
                    if (deleteFilesAdapter == null) return;
                    deleteFilesAdapter.selectAll(deleteFilesAdapter.getCommonBeanList(), false);
                    mSwipeLoadLayout.setRefreshing(true);//取消后重新刷新，避免不显示随之文件的问题
                    break;
                case R.id.tv_select_all:
                    if (deleteFilesAdapter == null) return;
                    deleteFilesAdapter.selectAll(deleteFilesAdapter.getCommonBeanList(), true);
                    break;
                default:
                    break;
            }
        }
    }

    //显示多项文件删除提示窗
    private void showDeleteMulDialog(Activity activity) {
        if (deleteFilesAdapter == null) return;
        final Set<String> selectPaths = deleteFilesAdapter.getSelectPath();
        Log.v(TAG, "select file count: " + selectPaths.size());
        if (selectPaths.isEmpty()) {
            showToast("请先选择要删除的文件~");
            return;
        }
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_delete, null);
        final Dialog dialog = new Dialog(activity, R.style.no_frame_small);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(v);
        dialog.show();
        TextView prompt = (TextView) v.findViewById(R.id.dd_txt_content);
        prompt.setText("确定要删除这些文件?");
        Button cancel = (Button) v.findViewById(R.id.dd_btn_sure);
        cancel.setText("取消");
        Button sure = (Button) v.findViewById(R.id.dd_btn_cancel);
        sure.setText("确定");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFiles(selectPaths);
                showDeleteList(false);
                workByUser = false;
                selectPaths.clear();
                mSwipeLoadLayout.setRefreshing(true);
                GlobalToast.toastShort(CodeAndReadActivity2.this, "删除成功");
                dialog.cancel();
            }
        });
    }

    //是否显示删除列表
    private void showDeleteList(boolean delete) {
        if (delete) {
            frameLayout.setVisibility(View.GONE);
            deleteList.setVisibility(View.VISIBLE);
            ll_delete.setVisibility(View.VISIBLE);
            rl_search.setVisibility(View.INVISIBLE);
        } else {
            frameLayout.setVisibility(View.VISIBLE);
            deleteList.setVisibility(View.GONE);
            ll_delete.setVisibility(View.GONE);
            rl_search.setVisibility(View.VISIBLE);
        }
    }

    private void deleteFiles(Set<String> deleteList) {
        if (deleteList == null || deleteList.isEmpty()) return;
        for (String path : deleteList) {
            GlobalData.Sm.instance(this).delete(path);
        }
    }

    private void takePhoto() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date()) +
                ".jpg";
        g_strMediaPath = Dirs.getCameraDir(Dirs.getDefaultBoot()) + "/pbb_" + name;
        Uri uri = Uri.parse("file://" + g_strMediaPath);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 0);
    }

    private void takeVideo() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date()) +
                ".mp4";
        g_strMediaPath = Dirs.getCameraDir(Dirs.getDefaultBoot()) + "/pbb_" + name;
        Uri uri = Uri.parse("file://" + g_strMediaPath);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, 1);
    }

    private void query_local_file() {
        // 判断是否需要验证true:不需要;false:需要.
        // 不需要直接跳转MediaActivity
        if (isVerification()) {
            OpenMediaActivity();
        } else {
            Query_local_file_Verification();
        }
    }

    private void OpenMediaActivity() {
        Intent intent = new Intent(this, MediaActivity.class);
        // Pbb_Fields.TAG_PLAIN_TOTAL 显示本地数量列表.
        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields.TAG_PLAIN_TOTAL);
        // GlobalIntentKeys.BUNDLE_FLAG_CIPHER 密文环境.
        intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
        intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, true);
        startActivity(intent);
    }

    private void Query_local_file_Verification() {
        // 提示用户验证身体，以及绑定
        final Dialog dialog = new Dialog(this, R.style.no_frame_small);
        View v = getLayoutInflater().inflate(R.layout.dialog_click_limit, null);
        dialog.setContentView(v);
        dialog.show();
        // 用户不想验证,则跳转MediaActivity。
        v.findViewById(R.id.dcl_btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                OpenMediaActivity();
            }
        });
        // 用户同意验证，跳转个人中心去绑定验证。
        v.findViewById(R.id.dcl_btn_goto_check).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.cancel();
                startActivity(new Intent(CodeAndReadActivity2.this, UserInfoActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 如果还有其他类型的request，则再细分
            Intent intent = new Intent(this, PayLimitConditionActivity.class);
            intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, g_strMediaPath);
            intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
            startActivity(intent);
        }
    }

    private boolean isVerification() {
        UserInfo userInfo = UserDao.getDB(this).getUserInfo();
        return userInfo.isEmailBinded() || userInfo.isPhoneBinded()
                || userInfo.isQqBinded() || GlobalData.getTotalCount(this, true) < 11;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        workByUser = false;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        UIHelper.showExitTips(this);
    }

}
