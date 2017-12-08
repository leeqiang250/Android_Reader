package cn.com.pyc.suizhi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.artifex.mupdfdemo.Hit;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFReflowAdapter;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.OutlineItem;
import com.artifex.mupdfdemo.ReaderView;
import com.artifex.mupdfdemo.TextWord;
import com.qlk.util.tool.Util;
import com.sz.help.KeyHelp;
import com.sz.mobilesdk.authentication.SZContent;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.Bookmark;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.database.practice.BookmarkDAOImpl;
import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.TimeUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.widget.ToastShow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LogerHelp;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.MuPDFOutlineHomeActivity;
import cn.com.pyc.pbbonline.bean.event.MuPDFBookMarkDelEvent;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.LogConfig;
import cn.com.pyc.suizhi.help.LoadDataHelp;
import cn.com.pyc.suizhi.help.ProgressHelp;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.util.DRMUtil;
import cn.com.pyc.suizhi.util.OpenUIUtil;
import cn.com.pyc.suizhi.util.SZPathUtil;
import cn.com.pyc.suizhi.util.Util_;
import cn.com.pyc.widget.HighlightImageView;
import cn.com.pyc.widget.MarqueeTextView;
import de.greenrobot.event.EventBus;

/**
 * Created by hudaqiang on 2017/8/24.
 */

public class SZPDFActivity extends ExtraBaseActivity implements View.OnClickListener {

    private static final String TAG = "MuPDFActivity";
    private static final int OUTLINE_REQUEST = 101;
    private static final int CODE_RELOAD_REQUEST = 102;
    private MuPDFCore core;
    private String mFileIdName;
    private MuPDFReaderView mDocView;
    private View mButtonsView;
    private boolean mButtonsVisible;
    private TextView mPageCurrentView; // 当前页数
    private TextView mPageTotalView; // 总页数
    private SeekBar mPageSlider;
    private HighlightImageView mPdfFileInfoButton;
    private ViewAnimator mTopBarSwitcher;
    //private View floatView;
    //private TextView tvFloatPage;
    private boolean mReflow = false;
    private int mPageSliderRes;
    private TopBarMode mTopBarMode = TopBarMode.Main;
    private PopupWindow pwInfo;
    /**
     * 最外层父类layout
     */
    private RelativeLayout mParentLayout;
    private RelativeLayout mupdf_situation;
    private RelativeLayout mTitleBar;
    private ImageView makebook;
    //private String asset_id;
    private int page_few;
    private Bundle savedInstanceState;
    private ToastShow ts = ToastShow.getToast();

    private String cur_fileId;
    private List<AlbumContent> contents;
    private volatile int mCurrentIndex;
    private SZContent szCont;
    private String myProId;
    private String productName;
    private String fileName;
    private List<FileData> dataList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        EventBus.getDefault().register(this);

        getValue();
        initCore(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
    }

    protected void getValue() {
        Intent intent = getIntent();
        myProId = intent.getStringExtra(KeyHelp.KEY_MYPRO_ID);
        productName = intent.getStringExtra(KeyHelp.KEY_PRO_NAME);
        cur_fileId = intent.getStringExtra(KeyHelp.KEY_FILE_ID);
        dataList = intent.getParcelableArrayListExtra(KeyHelp.KEY_FILE_LIST); //列表数据

        contents = AlbumContentDAOImpl.getInstance().findAlbumContentByMyProId(myProId);
        if (dataList != null) {
            contents = LoadDataHelp.sortOutPutData(dataList, contents);
        }
        SZLog.d(TAG, "count = " + contents.size() + "; " + contents.toString());
        ////contents = (List<AlbumContent>) intent.getSerializableExtra("save_albumContent");
        mCurrentIndex = Util_.getContentIndex(cur_fileId, contents);
        fileName = contents.get(mCurrentIndex).getName();
        String asset_id = contents.get(mCurrentIndex).getAsset_id();
        szCont = new SZContent(asset_id);
        SZLog.d(TAG, "fileName=" + fileName);

        Log.e(TAG, "sz pdf play...");
    }

    /**
     * 接收删除书签的事件
     *
     * @param event
     */
    public void onEventMainThread(MuPDFBookMarkDelEvent event) {
        //Bookmark bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(asset_id, page_few);
        Bookmark bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(cur_fileId, page_few);
        makebook.setSelected(bookmark != null);
    }

    private void makeButtonsView() {
        mButtonsView = LayoutInflater.from(this).inflate(R.layout.activity_pdf_buttons, null);
        mupdf_situation = (RelativeLayout) mButtonsView.findViewById(R.id.mupdf_situation);
        TextView amc_text_count = (TextView) mButtonsView.findViewById(R.id.amc_text_count);
        final int mMediaCount = contents.size();
        amc_text_count.setVisibility(View.VISIBLE);
        amc_text_count.setText(mMediaCount + "");
        // PDF页面顶部名称
        ((MarqueeTextView) mButtonsView.findViewById(R.id.pdf_title)).setText(contents.get
                (mCurrentIndex).getName());
        makebook = (ImageView) mButtonsView.findViewById(R.id.pdf_makebook);
        mPdfFileInfoButton = (HighlightImageView) mButtonsView.findViewById(R.id.pdf_info_button);
        mTopBarSwitcher = (ViewAnimator) mButtonsView.findViewById(R.id.switcher);
        mPageCurrentView = (TextView) mButtonsView.findViewById(R.id.currentPage_pdf_txt);
        mPageTotalView = (TextView) mButtonsView.findViewById(R.id.totalPage_pdf_txt);
        mTitleBar = (RelativeLayout) mButtonsView.findViewById(R.id.rel_titlebar);
        mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.sbfPageSlider);
//        floatView = mButtonsView.findViewById(R.id.share_layout);
//        tvFloatPage = (TextView) mButtonsView.findViewById(R.id.share_text_pos);

//        mButtonsView.findViewById(R.id.share_image_btn).setOnClickListener(this);
        mButtonsView.findViewById(R.id.pdf_back).setOnClickListener(this);
        mButtonsView.findViewById(R.id.pdf_list_button).setOnClickListener(this);
        mButtonsView.findViewById(R.id.pdf_outline_text).setOnClickListener(this);
        makebook.setOnClickListener(this);
        mTitleBar.setOnClickListener(this);
        mTopBarSwitcher.setOnClickListener(this);
        mPdfFileInfoButton.setOnClickListener(this);
    }

    /**
     * core initial
     *
     * @param savedInstanceState
     */
    private void initCore(Bundle savedInstanceState) {
        if (core == null) {
            core = (MuPDFCore) getLastNonConfigurationInstance();
            if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
                mFileIdName = savedInstanceState.getString("FileName");
            }
        }
        if (core == null) {
            try {
                parserIntent(savedInstanceState);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            createPdfUI(savedInstanceState);
        }
    }

    private void parserIntent(Bundle savedInstanceState) {
        String filePath = getFilePath();
        core = openPdfFile(filePath);
        if (core != null && core.needsPassword()) {
            requestPassword(savedInstanceState);
            return;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte buffer[] = new byte[inputStream.available()];
            inputStream.read(buffer, 0, inputStream.available());
            core = openPdfBuffer(buffer);
            if (core != null && core.needsPassword()) {
                ts.showBusy(this, getString(R.string.need_password));
                requestPassword(savedInstanceState);
                //return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void requestPassword(Bundle savedInstanceState) {
//        String[] _pid1 = {"_id"};
//        String[] _pidvalue1 = {asset_id};
//        Asset asset = (Asset) AssetDAOImpl.getInstance()
//                .findByQuery(_pid1, _pidvalue1, Asset.class).get(0);
        if (core.authenticatePassword(szCont.getCek_cipher_value())) {
            createPdfUI(savedInstanceState);
        } else {
            makeButtonsView();
            mupdf_situation.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8B8989")));
            mPageSlider.setEnabled(false);
            mParentLayout = new RelativeLayout(this);
            mParentLayout.addView(mButtonsView);
            setContentView(mParentLayout);
            ts.show(this, ToastShow.IMG_FAIL, getString(R.string.file_read_verify_error), Gravity
                    .CENTER);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pdf_back:
                finish();
                break;
            case R.id.pdf_outline_text:
                showOutLinesWindow(); // 目录/书签
                break;
            case R.id.pdf_list_button:
                openAlbumList(); // 专辑列表
                break;
            case R.id.pdf_info_button:
                showInfo(); // 图书信息
                break;
            case R.id.pdf_makebook:
                makeBookMark(); // 添加书签
                break;
//            case R.id.share_image_btn: //分享此刻
//                sharedMoment(String.valueOf(mDocView.getDisplayedViewIndex()));
//                break;
            default:
                break;
        }
    }

    private void openAlbumList() {
        if (pwInfo != null && pwInfo.isShowing()) {
            pwInfo.dismiss();
            pwInfo = null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(KeyHelp.KEY_MYPRO_ID, myProId);
        bundle.putString(KeyHelp.KEY_PRO_NAME, productName);
        bundle.putString(KeyHelp.KEY_FILE_ID, contents.get(mCurrentIndex).getContent_id());
        bundle.putString(KeyHelp.KEY_PRO_CATEGORY, DrmPat.BOOK);
        bundle.putSerializable(KeyHelp.KEY_SAVE_CONTENT, (ArrayList<AlbumContent>) this.contents);
        //bundle.putString(DRMUtil.JUMP_FLAG, DRMUtil.BY_MUPDF_UI);// 标识从pdf主界面跳转
        Intent i = new Intent(this, SZListAlbumContentActivity.class);
        i.putExtras(bundle);
        startActivityForResult(i, CODE_RELOAD_REQUEST);
    }

    /**
     * 设置书签
     */
    private void makeBookMark() {
        if (hasPermit()) {
            Bookmark bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(cur_fileId,
                    page_few);
            String currentTime = TimeUtil.getCurrentTime();
            if (bookmark == null) {
                String content = "";
                TextWord[][] textwords = core.textLines(page_few);
                for (int x = 0; x < textwords.length; x++) {
                    TextWord[] textword = textwords[x];
                    for (int z = 0; z < textword.length; z++) {
                        TextWord tw = textword[z];
                        String w = tw.getW();
                        content += w;
                    }
                }
                content = content.replace(" ", "").trim();
                if ("".equals(content)) {
                    content = "[图片]";
                } else {
                    final int maxLetters = 61;
                    int originalLength = content.length();
                    content = content.substring(0,
                            originalLength < maxLetters ? originalLength : maxLetters);
                    content = originalLength < maxLetters ? content : content + "...";
                }
                Bookmark bm = new Bookmark();
                bm.setId(System.currentTimeMillis() + "");
                bm.setContent_ids(cur_fileId);
                bm.setContent(content);
                bm.setTime(currentTime);
                bm.setPagefew(page_few + "");
                BookmarkDAOImpl.getInstance().save(bm);
                makebook.setSelected(true);
                showToast(getString(R.string.add_bookmarks_success));
            } else {
                Bookmark bm = new Bookmark();
                bm.setId(bookmark.getId());
                bm.setContent_ids(bookmark.getContent_ids());
                bm.setContent(bookmark.getContent());
                bm.setTime(currentTime);
                bm.setPagefew(bookmark.getPagefew());
                BookmarkDAOImpl.getInstance().update(bm);
                makebook.setSelected(true);
                showToast(getString(R.string.bookmarks_exist));
            }
        } else {
            showToast(getString(R.string.bookmarks_er_because_unkey));
        }
    }

    /**
     * 打开目录主页
     */
    private void showOutLinesWindow() {
        if (pwInfo != null && pwInfo.isShowing()) {
            pwInfo.dismiss();
            pwInfo = null;
        }
        if (!hasPermit()) {
            ts.showBusy(getApplicationContext(), getString(R.string.file_read_miss_authority));
            return;
        }

        OutlineItem[] outlines = core.getOutline();
        List<OutlineItem> outlineList = null;
        if (outlines != null && outlines.length > 0) {
            outlineList = Arrays.asList(outlines);
            String pageString = mPageCurrentView.getText().toString().trim();
            int pageCurrent = ConvertToUtil.toInt(pageString.substring(1, pageString.length() -
                    1));
            for (int i = 0; i < outlines.length; i++) {
                SZLog.d(TAG, outlines[i].toString());
                int page = outlines[i].page;
                if (page >= pageCurrent) {
                    DRMUtil.OUTLINE_POSITION = i;
                    break;
                }
                if (i == (outlines.length - 1)) {
                    DRMUtil.OUTLINE_POSITION = i + 1;
                    break;
                }
            }
        }
        //此处使用PBBReader下pbbonline包下的MuPDFOutlineHomeActivity
        Intent in = new Intent(SZPDFActivity.this, MuPDFOutlineHomeActivity.class);
        Bundle bundle = new Bundle();
        if (outlineList != null) {
            bundle.putSerializable("outline_list", (Serializable) outlineList);
        }
        bundle.putString("title_name", fileName);
        bundle.putString("content_id", cur_fileId);
        //bundle.putString("asset_content_id", asset_id);
        in.putExtras(bundle);
        SZPDFActivity.this.startActivityForResult(in, OUTLINE_REQUEST);
    }

    /**
     * 图书信息
     */
    public void showInfo() {
        if (pwInfo == null) {
            showInfo(true);
        } else {
            showInfo(!pwInfo.isShowing());
        }
    }

    public void showInfo(boolean show) {
        if (show) {
            View infoView = getLayoutInflater().inflate(R.layout.pbbonline_dialog_pdf_infor, null);
            ((TextView) infoView.findViewById(R.id.dvi_txt_geshi)).setText(DrmPat.PDF);
            ((TextView) infoView.findViewById(R.id.dvi_txt_yeshu)).setText(String.valueOf(core
                    .countPages()));
            if (pwInfo == null) {
                pwInfo = new PopupWindow(infoView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                // pwInfo.setAnimationStyle(R.style.PopupWindow_info_anim);
                pwInfo.setAnimationStyle(android.R.style.Animation_Dialog);
            }
            pwInfo.showAtLocation(mPdfFileInfoButton, Gravity.LEFT | Gravity.BOTTOM,
                    (mPdfFileInfoButton.getLeft() + mPdfFileInfoButton.getWidth() / 14),
                    (mPdfFileInfoButton.getBottom() + 10));
        } else {
            if (pwInfo != null) {
                pwInfo.dismiss();
                pwInfo = null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case OUTLINE_REQUEST:
                if (data != null) {
                    mDocView.setDisplayedViewIndex(data.getIntExtra("page", 1));
                }
                break;
            case CODE_RELOAD_REQUEST: {
                if (data != null) {
                    SZPDFActivity.this.finish();
                    Bundle bundle = data.getExtras();
                    OpenUIUtil.openActivity(this, SZPDFActivity.class, bundle);
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFileIdName != null && mDocView != null) {
            outState.putString("FileName", mFileIdName);
            // Store current page in the prefs against the file name,
            // so that we can pick it up each time the file is loaded Other info
            // is needed only for screen-orientation change,
            // so it can go in the bundle
            //preference.putPageInt("page" + mFileIdName, mDocView.getDisplayedViewIndex());
            ProgressHelp.saveProgress(mFileIdName, mDocView.getDisplayedViewIndex());
        }

        if (!mButtonsVisible) outState.putBoolean("ButtonsHidden", true);

        if (mReflow) outState.putBoolean("ReflowMode", true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ts.cancel();
        if (mFileIdName != null && mDocView != null)
            ProgressHelp.saveProgress(mFileIdName, mDocView.getDisplayedViewIndex());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        releaseResources();
    }

    private void releaseResources() {
        if (pwInfo != null && pwInfo.isShowing()) {
            pwInfo.dismiss();
            pwInfo = null;
        }
        if (mParentLayout != null) {
            mParentLayout.removeAllViews();
        }
        if (mDocView != null) {
            mDocView.applyToChildren(new ReaderView.ViewMapper() {
                @Override
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (core != null) {
            core.onDestroy();
            core = null;
        }
    }

    private MuPDFCore openPdfFile(String path) {
        Util_.checkFileExist(this, path, new UIHelper.DialogCallBack() {
            @Override
            public void onConfirm() {
                SZPDFActivity.this.finish();
            }
        });

        // int lastSlashPos = path.lastIndexOf('/');
        mFileIdName = contents.get(mCurrentIndex).getContent_id();// new
        // String(lastSlashPos
        // == -1 ?
        // path
        // :
        // path.substring(lastSlashPos
        // + 1));
        SZLog.i("Trying to open " + path);
        try {
            // core = new MuPDFCore(this, path);
            core = new MuPDFCore(this, path, null, 0, 0);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            e.printStackTrace();
            //添加日志
            ExtraParams extraParams = LogConfig.getBaseExtraParams();
            extraParams.file_name = LogerHelp.getFileName();
            extraParams.lines = LogerHelp.getLineNumber();
            LogerEngine.debug(this, e.getMessage(), extraParams);
        } catch (java.lang.OutOfMemoryError e) {
            // out of memory is not an Exception, so we catch it separately.
            e.printStackTrace();
        }
        return core;
    }

    private MuPDFCore openPdfBuffer(byte buffer[]) {
        SZLog.i("Trying to open byte buffer");
        try {
            core = new MuPDFCore(this, buffer, null);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return core;
    }

    public void createPdfUI(Bundle savedInstanceState) {
        if (core == null) return;

        // Now create the UI.
        // First create the document view
        mDocView = new MuPDFReaderView(this) {
            @Override
            protected void onMoveToChild(int i) {
                if (core == null) return;
                // mPageNumberView.setText(String.format("%d / %d", i + 1,
                // core.countPages()));
                page_few = i;
                // 当前页初始化
                mPageCurrentView.setText(getString(R.string.page_n, (i + 1)));
                // 总页数初始化
                mPageTotalView.setText(getString(R.string.page_total,
                        core.countPages()));

                mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
                mPageSlider.setProgress(i * mPageSliderRes);
                // 书签
                Bookmark bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(cur_fileId,
                        page_few);
                makebook.setSelected(bookmark != null);
                super.onMoveToChild(i);
            }

            @Override
            protected void onTapMainDocArea() {
                if (!mButtonsVisible) {
                    showButtons();
                } else {
                    if (mTopBarMode == TopBarMode.Main) hideButtons();
                }
            }

            @Override
            protected void onDocMotion() {
                hideButtons();
            }

            @Override
            protected void onHit(Hit item) {
                super.onHit(item);
                switch (mTopBarMode) {
                    case Annot:
                        if (item == Hit.Annotation) {
                            showButtons();
                            mTopBarMode = TopBarMode.Delete;
                            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        }
                        break;
                    case Delete:
                        mTopBarMode = TopBarMode.Annot;
                        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        // fall through
                    default:
                        // Not in annotation editing mode, but the pageview will
                        // still select and highlight hit annotations, so
                        // deselect just in case.
                        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                        if (pageView != null) pageView.deselectAnnotation();
                        break;
                }
            }
        };
        // MuPDFPageAdapter adapter = new MuPDFPageAdapter(this, core);
        MuPDFPageAdapter adapter = new MuPDFPageAdapter(this, null, core);
        mDocView.setAdapter(adapter);

        // Make the buttons overlay, and store all its
        // controls in variables
        makeButtonsView();

        // Set up the page slider
        int smax = Math.max(core.countPages() - 1, 1);
        mPageSliderRes = ((10 + smax - 1) / smax) * 2;

        mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                if (!hasPermit()) return;
                updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!hasPermit()) return;
//                floatView.clearAnimation();
//                ViewUtil.showWidget(floatView);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!hasPermit()) return;
                mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2)
                        / mPageSliderRes);
//                ViewUtil.hideWidgetAnimator(floatView);
            }
        });

        //去分享此刻开始播放（存在的话）
//        String sharePosition = ShareMomentEngine.getSharePosition(DrmPat.BOOK);
//        if (!TextUtils.isEmpty(sharePosition)) {
//            mDocView.setDisplayedViewIndex(ConvertToUtil.toInt(sharePosition));
//        } else {
        //上次打开文件位置
        // int page = preference.getPageInt("page" + mFileIdName, 0);
        int page = (int) ProgressHelp.getProgress(mFileIdName, 0);
        //分享此刻不存在，跳转上次文件打开位置
        mDocView.setDisplayedViewIndex(page);
//        }

        if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
            showButtons();

        if (savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false))
            reflowModeSet(true);

        // Stick the document view and the buttons overlay into a parent view
        mParentLayout = new RelativeLayout(this);
        // mParentLayout
        // .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        FrameLayout fLayout = new FrameLayout(this);
        fLayout.setBackgroundColor(Color.parseColor("#ECECEC"));
        // ////// fLayout.addView(mButtonsView);
        if (hasPermit()) {
            mupdf_situation.setBackgroundDrawable(null);
            mPageSlider.setEnabled(true);
            fLayout.addView(mDocView);
        } else {
            mupdf_situation.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8B8989")));
            mPageSlider.setEnabled(false);
            //ViewUtil.hideWidget(tvFloatPage);
            mPageCurrentView.setText(getString(R.string.page_n, 0));
            mPageTotalView.setText(getString(R.string.page_total, 0));
            mPageSlider.setMax(0);
            mPageSlider.setProgress(0);
            ts.show(this, ToastShow.IMG_BUSY, getString(R.string.file_read_miss_authority), Gravity
                    .CENTER);
        }
        fLayout.addView(mButtonsView); // update 2015-12-03
        mParentLayout.addView(fLayout);
        setContentView(mParentLayout);
        UIHelper.showTintStatusBar(this, getResources().getColor(R.color.video_bkg_lightdark));
    }

    /*
     * 分享此刻PDF
     */
//    private void sharedMoment(String sharePosition) {
//        if (!CommonUtil.isNetConnect(this)) {
//            return;
//        }
//        ViewUtil.hideWidget(floatView);
//        ShareMomentEngine engine = new ShareMomentEngine.Intern()
//                .setProId(ShareMomentEngine.getSelectProId())
//                .setMyProId(this.myProId)
//                .setItemId(this.cur_fileId)
//                .setCategory(DrmPat.BOOK)
//                .setSharePosition(sharePosition)
//                .launch();
//        engine.work(this);
//    }

    /**
     * 当前文件是否有权限
     */
    private boolean hasPermit() {
        return szCont.checkOpen();
    }

    private String getFilePath() {
        String fileId = contents.get(mCurrentIndex).getContent_id();
        return SZPathUtil.getFilePrefixPath() + "/" + myProId + "/" + fileId
                + DrmPat._PDF;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        MuPDFCore myCore = core;
        core = null;
        return myCore;
    }

    private void reflowModeSet(boolean reflow) {
        mReflow = reflow;
        mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core)
                : new MuPDFPageAdapter(this, null, core));

        mDocView.refresh(mReflow);
    }

    private void showButtons() {
        if (core == null) return;
        if (!mButtonsVisible) {
            mButtonsVisible = true;
            // Update page number text and slider
            int index = mDocView.getDisplayedViewIndex();

            updatePageNumView(index);
            //ViewUtil.showWidget(tvFloatPage);

            Util.AnimationUtil.translate(mTitleBar, true, true, Util.AnimationUtil.Location.Top);
            Util.AnimationUtil.translate(mTopBarSwitcher, true, true, Util.AnimationUtil.Location
                    .Bottom);

            // mTitleBar.setVisibility(View.VISIBLE);
            // mTopBarSwitcher.setVisibility(View.VISIBLE);
        }
    }

    private void hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false;
            // getWindow().addFlags(
            // WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            Util.AnimationUtil.translate(mTitleBar, true, false, Util.AnimationUtil.Location.Top);
            Util.AnimationUtil.translate(mTopBarSwitcher, true, false, Util.AnimationUtil
                    .Location.Bottom);
            //ViewUtil.hideWidget(tvFloatPage);

            // mTitleBar.setVisibility(View.INVISIBLE);
            // mTopBarSwitcher.setVisibility(View.INVISIBLE);

            if (pwInfo != null) {
                showInfo(false);
            }
        }
    }

    /**
     * 更新当前页数
     *
     * @param index
     */
    private void updatePageNumView(int index) {
        //if (core == null) return;
        mPageCurrentView.setText(getString(R.string.page_n, (index + 1)));
//        tvFloatPage.setText(getString(R.string.page_n, (index + 1)));
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * The core rendering instance
     */
    enum TopBarMode {
        Main, Search, Annot, Delete, More, Accept
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) return;
        if (intent.getBooleanExtra(KeyHelp.KEY_FROM_CHECK, false)) {
            releaseResources();
            getValue();
            initCore(savedInstanceState);
        }
    }
}
