package cn.com.pyc.pbbonline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.qlk.util.base.BaseActivity;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.mobilesdk.util.UIHelper.DialogCallBack;

import java.util.HashSet;
import java.util.Set;

import cn.com.pyc.base.BaseActivityGroup;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.event.IsEditRecordModeEvent;
import cn.com.pyc.pbbonline.bean.event.IsSierisModeEvent;
import cn.com.pyc.pbbonline.bean.event.LoginSuccessRefeshRecordEvent;
import cn.com.pyc.pbbonline.common.ShareMode;
import cn.com.pyc.pbbonline.db.Shared;
import cn.com.pyc.pbbonline.db.SharedDBManager;
import cn.com.pyc.pbbonline.model.JPushDataBean;
import cn.com.pyc.pbbonline.service.JpushViewService;
import cn.com.pyc.pbbonline.util.ClipboardUtil;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.pbbonline.widget.SlideTabHost;
import cn.com.pyc.pbbonline.widget.SlideTabHost.GenerateViewListener;
import cn.com.pyc.pbbonline.widget.SlideTabHost.OnTabChangedListener;
import cn.com.pyc.pbbonline.widget.SmoothCheckBox;
import cn.com.pyc.receive.FindFileActivity;
import cn.com.pyc.receive.ReceiveActivity;
import cn.com.pyc.widget.HighlightImageView;
import cn.com.pyc.widget.SegmentControl;
import cn.com.pyc.widget.SegmentControl.OnSegmentControlClickListener;
import de.greenrobot.event.EventBus;

/**
 * 新主页面。
 *
 * @author hudq
 */
@Deprecated
public class IndexPageHomeActivity extends BaseActivityGroup {
    private static final String TAG = "IndexHomeActy";
    private static final String SP_VIEW_TYPE = "sp_view_type";
    public static final String TK_SHORT_CODE = "tk_shortcode";
    public static final int RESULT_READ = 0x1234;
    private static final int TAB_1 = 0;
    private static final int TAB_2 = 1;

    protected static final int VIEW_TYPE_LIST = 0;        //default文件显示
    protected static final int VIEW_TYPE_SERIES = 1;    //系列显示
    protected int mViewType = VIEW_TYPE_LIST;
    private boolean isEditMode = false;
    private boolean showEditDel = false;
    private Drawable loginD;
    private Drawable unloginD;
//    private Drawable icAdd;

    private Set<Activity> activityStack;
    private SlideTabHost mTabHost;
    private HighlightImageView leftImg; //标题左图标
    private HighlightImageView rightImg; //标题右图标
    private SegmentControl segmentControl; //中间标题
    private PopupWindow popwin;//点击➕按钮弹出
    private TextView mSeriesSwitcherText;//系列显示&文件显示 切换
    private ImageView mDeleteImg;
//    private TextView tvTitle;

    private SmoothCheckBox mAllCheckBox;
    private View mBottomLayout; //底部编辑选项
    private AsyncTask<Void, Void, Boolean> initTask;
    private boolean isInitTaskRunning = false;
    private boolean isAcyCreate = false;
    private JPushDataBean pub;
    private SharedDBManager dbManager;
    private Handler mHandler = new Handler();

    public ImageView getDeleteImg() {
        return mDeleteImg;
    }

    public SmoothCheckBox getAllCheckBox() {
        return mAllCheckBox;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pbbonline_activity_group_indexhome);
        isAcyCreate = true;
        ViewHelp.showAppTintStatusBar(this);
        EventBus.getDefault().register(this);
        dbManager = new SharedDBManager();
        activityStack = new HashSet<Activity>();
//        loginD = getResources().getDrawable(R.drawable.icon_user_login);
//        unloginD = getResources().getDrawable(R.drawable.icon_user_unlogin);
//        icAdd = getResources().getDrawable(R.drawable.icon_add1);

        //用返回按钮代替原来用户人头按钮
        loginD = getResources().getDrawable(R.drawable.ic_back);
        unloginD = getResources().getDrawable(R.drawable.ic_back);

        segmentControl = (SegmentControl) findViewById(R.id.index_segment_control);
        leftImg = ((HighlightImageView) findViewById(R.id.index_leftimg));
        leftImg.setImageDrawable(Util_.isLogin() ? loginD : unloginD);
        rightImg = (HighlightImageView) findViewById(R.id.index_rightimg);
//        rightImg.setImageDrawable(icAdd);
//        tvTitle = (TextView) findViewById(R.id.title_tv);
//        tvTitle.setText("阅读文件");
        mBottomLayout = findViewById(R.id.rel_index_edit);
        mAllCheckBox = (SmoothCheckBox) findViewById(R.id.allselect_checkbox);
        //mAllCheckBox.setChecked(false); //默认未选
        mDeleteImg = (ImageView) findViewById(R.id.delete_select_imge);
        mTabHost = (SlideTabHost) findViewById(R.id.slideTabHost);
        mViewType = (int) PbbSP.getGSP(this).getValue(SP_VIEW_TYPE, 0);

        int lableIndex = (int) SPUtil.get(Fields.FIELDS_PBB_TAB, TAB_1);
        boolean hasOnline = (lableIndex == TAB_2);//new SharedDBManager().existData();
        segmentControl.setSelectedIndex(lableIndex);
        showEditDel = hasOnline;
        initTabsView(hasOnline);
        setListener();
        getJpushDataJumpDetailPage();
        InitBrowserData();
        clipboardEyes();

        //新需求取消蒙板提示功能
        //ExampleUtil.showGuide2(this);
    }

    // 获取极光推送过来的数据
    private void getJpushDataJumpDetailPage() {
        pub = (JPushDataBean) getIntent().getSerializableExtra("PushUpdateBean");
        if (pub == null || pub.getData() == null)
            return;
        if (ShareMode.REVOKESHARE.equals(pub.getAction()) && mTabHost != null)    //分享被收回
        {
            mTabHost.setCurrentTabIndex(TAB_2);
            showToast(getString(R.string.shared_lose_s_efficacy, pub.getData().getTheme()));
            return;
        }
        if (ShareMode.NEWSHARE.equals(pub.getAction()))                            //新分享
        {
            String url = pub.getData().getUrl();
            parserContentUrl(url, false);
            return;
        }
        if (dbManager == null)
            dbManager = new SharedDBManager();
        Shared record = dbManager.findByShareId(pub.getData().getShareID());    //分享更新
        if (record != null)
            parserContentUrl(record.getShareUrl(), false);
    }

    // 如果此界面已经在开启状态，则推送不走oncreate,而是走onNewIntent()方法。
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        getJpushDataJumpDetailPage();
        InitBrowserData();
        clipboardEyes();
    }

    /**
     * 获取浏览器或微信传送过来的数据
     */
    private void InitBrowserData() {
        Uri uri = getIntent().getData();
        if (uri != null) {
            String dataString = uri.toString();
            parserContentUrl(dataString, true);
        }
    }

    private void initTabsView(boolean showShared) {
        final Context context = IndexPageHomeActivity.this;
        mTabHost.addTabAndContentGenerateListener(!showShared, new GenerateViewListener() {
            @Override
            public View generateView() {
                Intent intent = new Intent(context, ReceiveActivity.class);
                //传递参数，标示是从按钮我要阅读点击跳转
                intent.putExtra(BaseActivity.TAG_WAY, BaseActivity.PBB_READER);
                //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return getLocalActivityManager().startActivity("1_acy", intent).getDecorView();
            }
        });

        //20170527：合并后，不添加online页面
//        mTabHost.addTabAndContentGenerateListener(showShared, new GenerateViewListener() {
//            @Override
//            public View generateView() {
//                Intent intent = new Intent(context, ShareRecordListActivity.class);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                return getLocalActivityManager().startActivity("2_acy", intent).getDecorView();
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SZLog.v(TAG, "onDestroy");
        isAcyCreate = false;
        SPUtil.save(Fields.FIELDS_PBB_TAB, mTabHost.getCurrentTabIndex()); //记录退出app时切换的标签
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        cancelTask();
        destoryActivitys();
    }

    private void destoryActivitys() {
        for (Activity a : activityStack) {
            if (a != null && !a.isFinishing()) {
                a.finish();
                activityStack.remove(a);
            }
        }
        activityStack.clear();
        activityStack = null;
        JpushViewService.closeJPushService(getApplicationContext());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Activity ac = getCurrentActivity();
            if (ac != null) {
                if (isEditMode()) {
                    hideBottomEdit();    //正在编辑状态，则先退出编辑
                    return true;
                }
                return ac.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setListener() {
        leftImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //原来用户信息按钮，现在是返回按钮
                finish();
//                OpenPageUtil.openActivity(IndexPageHomeActivity.this, UserCenterActivity.class);
//                overridePendingTransition(R.anim.trans_x_in, R.anim.trans_x_out);

            }
        });
        rightImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击➕按钮
                onAddClick(showEditDel);
            }
        });
        mTabHost.setOnTabChangedListener(new OnTabChangedListener() {
            @Override
            public void onTabChanged(int newTabIndex, boolean isFirstTime) {
                activityStack.add(getCurrentActivity());
                segmentControl.setSelectedIndex(newTabIndex);
                switch (newTabIndex) {
                    case TAB_1:
                        tab1();
                        break;
                    case TAB_2:
                        tab2();
                        break;
                    default:
                        break;
                }
            }
        });
        segmentControl.setOnSegmentControlClickListener(new OnSegmentControlClickListener() {
            @Override
            public void onSegmentControlClick(int index) {
                mTabHost.setCurrentTabIndex(index);
            }
        });
    }

    //tab1:
    private void tab1() {
        //切换到第一个页面,如果编辑状态，则隐藏
        showEditDel = false;
        if (isEditMode)
            hideBottom();
    }

    //tab2:
    private void tab2() {
        //切换到第2个页面，如果编辑状态，则显示
        showEditDel = true;
        if (isEditMode)
            showBottom();
    }

    //加号按钮 弹出popwindow
    public void onAddClick(boolean showEditDel) {
        if (popwin == null) {
            final View addPopView = getLayoutInflater().inflate(R.layout.xml_add_popwindow, null);
            mSeriesSwitcherText = (TextView) addPopView.findViewById(R.id.xap_txt_serise);

            View editLayout = addPopView.findViewById(R.id.xap_edit);
            View serisModeLayout = addPopView.findViewById(R.id.xap_ll_sieris_show);
            View openFoldVLayout = addPopView.findViewById(R.id.xap_open_folder);
            View editLine = addPopView.findViewById(R.id.xap_edit_line);
            View serisModeLine = addPopView.findViewById(R.id.xap_serisModeLine);
            View openFoldLine = addPopView.findViewById(R.id.xap_open_folder_line);

            editLayout.setVisibility(showEditDel ? View.VISIBLE : View.GONE);
            editLine.setVisibility(showEditDel ? View.VISIBLE : View.GONE);
            serisModeLayout.setVisibility(!showEditDel ? View.VISIBLE : View.GONE);
            openFoldVLayout.setVisibility(!showEditDel ? View.VISIBLE : View.GONE);
//            serisModeLine.setVisibility(!showEditDel ? View.VISIBLE : View.GONE);
            openFoldLine.setVisibility(!showEditDel ? View.VISIBLE : View.GONE);

            popwin = new PopupWindow(addPopView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            //popwin.setAnimationStyle(R.style.add_popwindow_anim);
            popwin.setAnimationStyle(android.R.style.Animation_Dialog);
            popwin.setOutsideTouchable(true);
            popwin.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popwin.setFocusable(true);
            popwin.setOnDismissListener(new PopDismissLintener());

            if (mViewType == 0) {
                mSeriesSwitcherText.setText("系列显示");
                //mSeriesSwitcherText.setCompoundDrawablesWithIntrinsicBounds(R.drawable
                //.icon_sieris,0, 0, 0);
            } else if (mViewType == 1) {
                mSeriesSwitcherText.setText("文件显示");
                //mSeriesSwitcherText.setCompoundDrawablesWithIntrinsicBounds(
                //R.drawable.icon_file_view, 0, 0, 0);
            }

            popwin.showAtLocation(rightImg, Gravity.LEFT | Gravity.TOP,
                    rightImg.getLeft() - (rightImg.getWidth() * 34 / 11),
                    rightImg.getBottom() + rightImg.getHeight());
            backgroudAlpha(0.5f);

            addPopView.findViewById(R.id.xap_ll_sweep).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIHelper.showToast(getApplicationContext(), "进入扫一扫");
                    OpenPageUtil.openZXingCode(IndexPageHomeActivity.this);
                    closePop();
                }
            });

            addPopView.findViewById(R.id.xap_ll_sieris_show).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchViewType();
                            EventBus.getDefault().post(new IsSierisModeEvent(mViewType));
                            closePop();
                        }
                    });

            addPopView.findViewById(R.id.xap_open_folder).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    closePop();
                    OpenPageUtil.openActivity(IndexPageHomeActivity.this, FindFileActivity.class);
                }
            });

            final TextView xapTxtEdit = (TextView) addPopView.findViewById(R.id.xap_txt_edit);
            xapTxtEdit.setText(isEditMode ? "退出编辑" : "编 辑");
            editLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closePop();
                    if (isEditMode) {
                        hideBottomEdit();
                    } else {
                        showBottomEdit();
                    }
                }
            });

//            addPopView.findViewById(R.id.xap_about).setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                    closePop();
//                    OpenPageUtil.openActivity(IndexPageHomeActivity.this, AboutActivity.class);
//                }
//            });
        } else {
            closePop();
        }
    }

    private void closePop() {
        if (popwin != null) {
            popwin.dismiss();
            popwin = null;
        }
        backgroudAlpha(1f);
    }

    /* 设置背景透明度 */
    protected void backgroudAlpha(float bgAlpha) {
        if (getWindow() == null)
            return;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0 - 1.0
        getWindow().setAttributes(lp);
    }

    /**
     * 进入编辑状态
     */
    private void showBottomEdit() {
        isEditMode = true;
        EventBus.getDefault().post(new IsEditRecordModeEvent(true));
        showBottom();
    }

    public void showBottom() {
        mBottomLayout.clearAnimation();
        if (mBottomLayout.getVisibility() == View.GONE) {
            Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.d_open);
            mBottomLayout.startAnimation(a);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 退出编辑状态
     */
    private void hideBottomEdit() {
        isEditMode = false;
        EventBus.getDefault().post(new IsEditRecordModeEvent(false));
        hideBottom();
    }

    public void hideBottom() {
        mBottomLayout.clearAnimation();
        if (mBottomLayout.getVisibility() == View.VISIBLE) {
            Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.d_close);
            mBottomLayout.startAnimation(a);
            mBottomLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //		 boolean readSuc = requestCode == RESULT_READ && resultCode == RESULT_OK && data
        // != null;
        //				if (readSuc && mHistoryMode.peek() == TopBarMode.SeriesID)
        //				{
        //					SmInfo info = (SmInfo) data
        //							.getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
        //					if (info.getSid() != mSid)
        //					{
        //						GlobalToast.toastLong(this, "文件已迁移至分组：" + info.getSeriesName());
        //					}
        //				}
        //PBBOnline
        boolean isScanResult = (resultCode == RESULT_OK
                && requestCode == CaptureActivity.REQUEST_CODE_SCAN && data != null);
        if (isScanResult) {
            Bundle bundle = data.getExtras();
            String mDataSource = bundle.getString(CaptureActivity.DECODED_CONTENT_KEY);
            SZLog.v("URL", "" + mDataSource);
            parserContentUrl(mDataSource, false);
        }
    }

    /**
     * 地址解析
     *
     * @param DataSource
     * @param is4Browser 是否从第三方浏览器来源
     */
    private void parserContentUrl(final String DataSource, final boolean is4Browser) {
        if (TextUtils.isEmpty(DataSource)) {
            UIHelper.showToast(getApplicationContext(), "识别内容为空！");
            return;
        }
        if (!is4Browser) {
            if (!DataSource.startsWith("http://") && !DataSource.startsWith("https://")) {
                showMsgDialog(this, DataSource);
                return;
            }
        }
        cancelTask();
        initTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                isInitTaskRunning = true;
                showBgLoading(IndexPageHomeActivity.this, getString(R.string.please_waiting));
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                if (isCancelled())
                    return false;
                if (!isInitTaskRunning)
                    return false;
                //创建数据库
                return is4Browser ? Util_.initBrowserDataDB(DataSource) : Util_.initCommonDataDB(
                        DataSource, true);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                hideBgLoading();
                isInitTaskRunning = false;
                if (!result) {
                    if (isAcyCreate)
                        showMsgDialog(IndexPageHomeActivity.this, DataSource);
                    return;
                }
                String shareID = (String) SPUtil.get(Fields.FIELDS_ID, "");
                OpenPageUtil.openSharedDetaiPage(IndexPageHomeActivity.this, shareID);
            }

            @Override
            protected void onCancelled(Boolean result) {
                super.onCancelled(result);
                isInitTaskRunning = false;
                hideBgLoading();
            }
        };
        initTask.execute();
    }

    // 取消任务
    private void cancelTask() {
        if (initTask != null
                && (!initTask.isCancelled() || initTask.getStatus() == AsyncTask.Status.RUNNING)) {
            initTask.cancel(true);
            initTask = null;
        }
        isInitTaskRunning = false;
    }

    class PopDismissLintener implements PopupWindow.OnDismissListener {
        @Override
        public void onDismiss() {
            backgroudAlpha(1f);
            popwin = null;
        }
    }

    private void switchViewType() {
        mViewType ^= 1;
        PbbSP.getGSP(IndexPageHomeActivity.this).putValue(SP_VIEW_TYPE, mViewType);
    }

    /**
     * 非自定义登录的url，显示结果对话框
     *
     * @param result
     */
    public static void showMsgDialog(final Context ctx, final String result) {
        UIHelper.showCommonDialog(ctx, "扫描结果", result, "复制", new DialogCallBack() {
            @Override
            public void onConfirm() {
                CommonUtil.copyText(ctx.getApplicationContext(), result);
                UIHelper.showToast(ctx.getApplicationContext(), "内容已复制到剪贴板");
            }
        });
    }

    /**
     * 接收登录或退出的通知
     *
     * @param e
     */
    public void onEventMainThread(LoginSuccessRefeshRecordEvent e) {
        if (mTabHost == null || leftImg == null)
            return;
        SZLog.v(TAG, "login: " + e.isLogin() + ",change left image");
        mTabHost.setCurrentTabIndex(TAB_2);
        leftImg.setImageDrawable(e.isLogin() ? loginD : unloginD);
    }

    /**
     * 剪贴板监视；eg短码：TKcpFhp
     */
    private void clipboardEyes() {
        final String preCode = "TK";
        String clipContent = ClipboardUtil.eyesClipboard(this);
        if (TextUtils.isEmpty(clipContent))                    //剪贴板空
            return;
        if (clipContent.length() < preCode.length())           //短码格式错误
            return;

        String prefix = clipContent.substring(0, 2);
        if (!preCode.equalsIgnoreCase(prefix))                //非TK开头
            return;

        clipContent = clipContent.substring(preCode.length());
        if (TextUtils.isEmpty(clipContent))                    //短码为空
            return;

        final String tk_code = clipContent;
        final Context ctx = IndexPageHomeActivity.this;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ctx, ShareDetailsPageActivity.class);
                intent.putExtra(TK_SHORT_CODE, tk_code);
                ctx.startActivity(intent);
                ClipboardUtil.clearClipboard(getApplicationContext());
            }
        }, 700);
    }
}
