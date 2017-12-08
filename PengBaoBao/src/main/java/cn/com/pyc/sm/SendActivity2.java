package cn.com.pyc.sm;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.media.ISelection.ISelectListener;
import com.qlk.util.tool.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbbonline.bean.event.RefreshModifyPowerEvent;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.widget.HighlightImageView;
import cn.com.pyc.widget.PycAutoTextGray;
import de.greenrobot.event.EventBus;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (新界面发送显示界面)
 * @date 2016/12/13 16:17
 */
public class SendActivity2 extends PbbBaseActivity implements View.OnClickListener {
    public static final String EXPAND_GROUP = "expand_group";
    private ViewAnimator mTopView;
    private ImageButton mSettingBackButton;
    private HighlightImageView mAddButton;
    private PycAutoTextGray mSearchText;
    private TextView mDeleteNumText;
    private TextView mSeriesNameText;
    private TextView mSeriesSwitcherText;
    protected ExpandableListView mSmExpandableListView;
    private ListView mSeriesListView;
    private SwipeToLoadLayout mPullRefreshView;
    private RelativeLayout mDelleteBottomView;
    private HighlightImageView mSendBack;
    private View mEmptyView;
    private SendAdapter mAdapter;
    protected static ArrayList<String> mPaths = new ArrayList<>();
    protected static HashMap<String, SmInfo> mDatas = new HashMap<>();
    private TextView tv_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init_view();
        init_listener();
        init_data();
        init_adapter();
        init_expand_group();
    }

    private void init_view() {
        EventBus.getDefault().register(this);//注册EventBus
        setContentView(R.layout.activity_receive_list);
        ViewHelp.showAppTintStatusBar(this);
        mTopView = (ViewAnimator) findViewById(R.id.arl_lyt_top);// 顶部标题栏
        mSettingBackButton = (ImageButton) findViewById(R.id.vst_imb_setting_back);// 后退键
        mAddButton = (HighlightImageView) findViewById(R.id.vst_imb_add);// 加好
        mSearchText = (PycAutoTextGray) findViewById(R.id.vsnv_edt_search);// 搜索框
        tv_search = (TextView) findViewById(R.id.searchTxt);
        mDeleteNumText = (TextView) findViewById(R.id.vsd_txt_num);// 长按选择后，显示已选择几个文件
        mSeriesNameText = (TextView) findViewById(R.id.vst_txt_series_name);// 搜索的名字

        //mSmExpandableListView = (ExpandableListView) findViewById(R.id.arl_lsv_files);//
        mSmExpandableListView = (ExpandableListView) findViewById(R.id.swipe_target);//
        mSeriesListView = (ListView) findViewById(R.id.arl_lsv_series);// listview 隐藏的。
        mPullRefreshView = (SwipeToLoadLayout) findViewById(R.id.arl_lyt_pull_refresh);// 下拉刷新控件
        mPullRefreshView.setLoadMoreEnabled(false);
        mDelleteBottomView = (RelativeLayout) findViewById(R.id.rl_dellete_bottom_veiw);// 选择删除框
        mSendBack = (HighlightImageView) findViewById(R.id.send_back_img);// 已发送的后退键
        mEmptyView = findViewById(R.id.arl_lyt_empty);// 空空如也
        ((TextView) findViewById(R.id.vst_txt_title)).setText("已发送");// 设定标题
        mSmExpandableListView.setVisibility(View.VISIBLE);
        mSeriesListView.setVisibility(View.GONE);
    }

    private void init_listener() {
        mSendBack.setOnClickListener(this);
        mSmExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long
                    id) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);// 振动器
                // 是否选中item。
                if (!mAdapter.isSelecting()) {
                    int pos = position;
                    // 判断长按的item不是第一行,也不是最后一行时。就把点击item的position设置为-1.
                    if (mAdapter.getExpandPos() != -1 && mAdapter.getExpandPos() < pos) {
                        pos--;
                    }
                    mAdapter.setSelectable(true);
                    mAdapter.setItemSelected(pos);
                    mDelleteBottomView.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                } else {
                    hide_delete_notify();
                }
                // 关闭键盘
                dismissKeyboard();
                // 关闭mPullRefresh的刷新。
                mPullRefreshView.setRefreshing(false);
                return true;
            }

        });

        mPullRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                monRefresh();
            }
        });

//        mPullRefreshView.setOnRefreshListener(new PullRefreshView.OnRefreshListener() {
//
//            @Override
//            public void onRefresh() {
//                monRefresh();
//            }
//        });

    }

    private void init_data() {
        mPaths = getPaths();
    }

    private void init_adapter() {
        mAdapter = new SendAdapter(this, mSmExpandableListView, mPaths, mDatas);
        mAdapter.setSelectListener(mSelectListener);
        mSmExpandableListView.setAdapter(mAdapter);
        // 只允许打开一个item
        mSmExpandableListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                int total = mSmExpandableListView.getCount();
                for (int i = 0; i < total; i++) {
                    if (i != groupPosition) {
                        mSmExpandableListView.collapseGroup(i);
                    }
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    public void onEventMainThread(RefreshModifyPowerEvent event) {
        String updatafile_path = event.getMpath();
        mAdapter.refreshSingal(updatafile_path, true);
    }

    /**
     * @Description: (是否打开指定的子item)
     * @author 李巷阳
     * @date 2016/12/14 15:02
     */
    private void init_expand_group() {
        int expand = this.getIntent().getIntExtra("expand_group", -1);
        if (expand >= 0 && expand < this.mAdapter.getGroupCount()) {
            this.mSmExpandableListView.expandGroup(expand);
            this.getIntent().removeExtra("expand_group");
        }
    }


    /**
     * @Description: (隐藏delete并刷新界面)
     * @author 李巷阳
     * @date 2016/12/14 10:52
     */
    private void hide_delete_notify() {
        mAdapter.setSelectable(false);
        mDelleteBottomView.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.send_back_img) {
            finish();
        }
    }

    // 搜索事件处理
    public void onSearchClick1(View v) {
        String tv_text = tv_search.getText().toString();
        if ("搜索".equals(tv_text)) {
            String search = mSearchText.getText().toString();
            if (TextUtils.isEmpty(search)) {
                GlobalToast.toastShort(this, "请输入搜索内容");
                return;
            }
            ((TextView) v).setText("取消");
            search(search);
        } else if ("取消".equals(tv_text)) {
            mSearchText.setText(null);
            tv_search.setText("搜索");
            mAdapter.setSearchText(null);
            mPaths = getPaths();
            mAdapter.setData(mPaths);
        }
        hide_delete_notify();
    }

    // 搜索
    private void search(String searchText) {
        ArrayList<String> searchPaths = new ArrayList<String>();
        ArrayList<String> paths = mPaths;
        for (String path : paths) {
            String name = Util.FileUtil.getFileName(path);
            name = name.substring(0, name.lastIndexOf("."));    //搜索".pyc".
            if (name.contains(searchText)) {
                searchPaths.add(path);
            }
        }
        if (searchPaths.isEmpty()) {
            GlobalToast.toastShort(this, "没有匹配项");
        }
        dismissKeyboard();
        mAdapter.setSearchText(searchText);
        mAdapter.setData(searchPaths);
    }


    private ISelectListener mSelectListener = new ISelectListener() {

        @Override
        public void onSelcetChanged(boolean overflow, int total, boolean allSelected) {
            mDeleteNumText.setText("已选择" + total + "个文件");
        }
    };

    // 获取所有的路径
    private ArrayList<String> getPaths() {
        ArrayList<String> al_path = GlobalData.Sm.instance(getApplication()).getCopyPaths(true);
        if (al_path != null && al_path.size() > 0) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            GlobalToast.toastShort(this, "本地未查询到已发送文件");
            mEmptyView.setVisibility(View.VISIBLE);
        }
        mPullRefreshView.setRefreshing(false);
        return al_path;
    }

    // 删除按钮触发
    public void onCompleteClick(View v) {
        deleteFiles(mAdapter.getSelected().toArray(new String[]{}));
    }

    // 删除取消按钮触发
    public void onCancelClick(View v) {
        hide_delete_notify();
    }


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
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaths.removeAll(GlobalData.Sm.instance(SendActivity2.this).delete(paths));
                mAdapter.setSelectable(false);
                mDelleteBottomView.setVisibility(View.GONE);
                mAdapter.setData(getPaths());
                mAdapter.setSearchText(null);
                mSearchText.setText(null);// 搜索框内容设置为null
                tv_search.setText("搜索"); // 搜索按钮设置
                dialog.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    // 刷新界面
    private void monRefresh() {
        if (UserDao.getDB(getApplication()).getUserInfo().isKeyNull()) {
            GlobalToast.toastShort(getApplication(), "请先登录");
            refreshUI();
            mPullRefreshView.setRefreshing(false);
            return;
        }
        mAdapter.setData(getPaths());
    }

    @Override
    public void update(Observable observable, Object data) {
        switch ((ObTag) data) {
            case Refresh:
                monRefresh();
                break;

            case ChangeLimit:
                monRefresh();
                break;

            default:
                break;
        }
    }

}
