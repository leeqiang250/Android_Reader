package com.artifex.mupdfdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFReaderView.OnChangedListener;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util.AnimationUtil;
import com.qlk.util.tool.Util.AnimationUtil.Location;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.tool.Util.ViewUtil;
import com.qlk.util.tool._SysoXXX;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.Executor;

import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pcshare.help.PCFileHelp;
import cn.com.pyc.reader.PlayFile;
import cn.com.pyc.reader.ReaderBaseActivity;
import cn.com.pyc.widget.PycAutoText;
import cn.com.pyc.widget.PycEditText;
import cn.com.pyc.xcoder.XCoder;

class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}

public class MuPDFActivity extends ReaderBaseActivity implements FilePicker.FilePickerSupport {
    private MuPDFCore core;
    private MuPDFReaderView mDocView;
    private SearchTask mSearchTask;
    private String PageStoreName;
    private View mTopLayout;

    private boolean showMaskView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_read_pdf);
        super.onCreate(savedInstanceState);
        findViewAndSetListeners();

        String name = FileUtil.getFileName(mCurPath);


       /* Log.i("------PBB_Path:",mCurPath);///storage/emulated/0/Download/文档1.pdf.pbb
        mCurPath = "/storage/emulated/0/Download/文档3.pdf";
        Log.i("------PBB_Path1:",mCurPath);

        isFromSm = false;*/

        if (isFromSm) {
            name = name.substring(0, name.length() - 4);
        }
        ((TextView) findViewById(R.id.arp_txt_title)).setText(name);

        initViews();
    }

    @Override
    public void findViewAndSetListeners() {
        mTopLayout = findViewById(R.id.arp_lyt_top);
        g_lytSearch = findViewById(R.id.arp_lyt_search);
        g_lytSearchOrder = findViewById(R.id.arp_lyt_search_order);
        g_edtSearch = (PycAutoText) findViewById(R.id.arp_edt_search);
        g_edtSearch.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                search(g_edtSearch.getText().toString().trim(), 1);
            }
        });
        g_edtSearch.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    search(g_edtSearch.getText().toString().trim(), 1);
                }
                return false;
            }
        });

        g_txtComplete = (TextView) findViewById(R.id.arp_txt_complete);
        g_txtComplete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                search(g_edtSearch.getText().toString().trim(), 1);
            }
        });

        findViewById(R.id.arp_imb_search_previous).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                search(g_edtSearch.getText().toString().trim(), -1);
            }
        });
        findViewById(R.id.arp_imb_search_next).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                search(g_edtSearch.getText().toString().trim(), 1);
            }
        });
        findViewById(R.id.arp_imb_search).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                changeSearchMode();
            }
        });
        findViewById(R.id.arp_imb_outline).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showOutline();
            }
        });

        g_edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetSearchView(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        g_lytJump = findViewById(R.id.arp_lyt_jump);
        g_edtJump = (PycEditText) findViewById(R.id.arp_edt_jump);
        findViewById(R.id.arp_imb_jump).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeJumpMode();
            }
        });
        findViewById(R.id.arp_txt_jump).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(g_edtJump.getText().toString().trim());
            }
        });
    }

    // @Override
    // protected void onShowReaderViewFinished(ReturnInfo info)
    // {
    // super.onShowReaderViewFinished(info);
    // initViews(info.desBuffer, info.desPath);
    // }

    private void showControlView(final boolean show, boolean useDelay) {
        if (useDelay) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    AnimationUtil.translate(mTopLayout, true, show, Location.Top);
                }
            }, 3000);
        } else {
            AnimationUtil.translate(mTopLayout, true, show, Location.Top);
        }
    }

    private void initViews() {
        try {
            PlayFile playFile = null;
            if (isFromSm && smInfo != null) {
//				String path1 = Environment.getExternalStorageDirectory()+File.separator+"123.pdf
// .pbb";
//				try
//				{
//					FileOutputStream fileOutputStream = new FileOutputStream(path1);
//					FileInputStream fileInputStream = new FileInputStream(mCurPath);
//					fileInputStream.skip(smInfo.getOffset());
//					byte[] data = new byte[2<<20];
//					int read = 0;
//					while((read = fileInputStream.read(data))!=-1)
//					{
//						fileOutputStream.write(data,0,read);
//					}
//					mCurPath = path1;
//					fileInputStream.close();
//					fileOutputStream.close();
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}

                playFile = new PlayFile(mCurPath, XCoder.wrapEncodeKey(smInfo.getEncodeKey()),
                        smInfo.getCodeLen());

                //System.out.println("EncodeKey:"+smInfo.getEncodeKey());
                //System.out.println("CodeLen"+smInfo.getCodeLen());

                playFile.setOffset(smInfo.getOffset());

                //System.out.println("len=" + smInfo.getCodeLen());
                //System.out.println("pdf的偏移量：" + smInfo.getOffset());
                //System.out.println("mCurPath:::"+mCurPath);

                //_SysoXXX.message("len=" + smInfo.getCodeLen());
                //_SysoXXX.message("pdf的偏移量：" + smInfo.getOffset());
                _SysoXXX.array(XCoder.wrapEncodeKey(smInfo.getEncodeKey()), "key111");

                showLimitView((TextView) findViewById(R.id.arp_txt_countdown));
                //TODO:水印
                //showWaterView((TextView) findViewById(R.id.arp_txt_water));
                showMaskView = true;
            } else if (isCipher) {
                playFile = new XCoder(this).getPlayFileInfo(mCurPath);
            } else {
                playFile = new PlayFile(mCurPath);
            }
            //System.out.println("\\\\\\\\\\\\"+Arrays.toString(playFile.getKey()));
            //byte[] a = {54,49,(byte) 255,(byte) 196,(byte) 149,(byte) 182,(byte) 199,(byte) 166,
            //(byte) 130,116,66,87,73,(byte) 210,73,30};



            core = new MuPDFCore(this, mCurPath, playFile.getKey(), playFile.getCodeLen(),
                    playFile.getOffset());

           /* byte buffer[] = null;

//            InputStream is = getContentResolver().openInputStream(Uri.parse(mCurPath));
            InputStream is = new FileInputStream(mCurPath);
            int len = is.available();
            buffer = new byte[len];
            is.read(buffer, 0, len);


            core = new MuPDFCore(this,buffer,null);
            is.close();*/

        } catch (Exception e) {
            e.printStackTrace();
            core = null;
        }

        if (core == null) {
            GlobalToast.toastShort(this, "文件打开失败");
            finish();
            return;
        }

        findViewById(R.id.arp_imb_outline)
                .setVisibility(core.hasOutline() ? View.VISIBLE : View.GONE);

        mDocView = new MuPDFReaderView(this);
        mDocView.setOnChangedListener(changedListener);
        MuPDFPageAdapter adapter = new MuPDFPageAdapter(this, this, core);
        mDocView.setAdapter(adapter);
        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.arp_lyt_render);
        final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams
                .MATCH_PARENT);
        frameLayout.addView(mDocView, 0, lp);
        //TODO 添加水印
        if (showMaskView) {
            adapter.setPageSizeListener(new MuPDFPageAdapter.PageSizeListener() {
                @Override
                public void pageSize(PointF size) {
                    View maskView = initWaterMaskView(size.x, size.y);
                    frameLayout.addView(maskView, 1, lp);
                }
            });
        }

        PageStoreName = "page_" + FileUtil.getFileName(mCurPath);
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        goToPage(sp.getInt(PageStoreName, 0));

        showControlView(false, true);
    }

	/*-**************************************************
     * TODO 搜索
	 ****************************************************/

    private View g_lytSearch;
    private View g_lytSearchOrder;
    private PycAutoText g_edtSearch;
    private TextView g_txtComplete;
    private boolean inSearchMode = false;

    private void changeSearchMode() {
        inSearchMode = !inSearchMode;
        g_lytSearch.setVisibility(inSearchMode ? View.VISIBLE : View.GONE);
        Animation scale = AnimationUtil.scale(g_lytSearch, false, inSearchMode, Location.Right,
                new Pair<Float, Float>(0.92f, 0.5f));
        Animation alpha = AnimationUtil.alpha(g_lytSearch, false, inSearchMode);
        AnimationUtil.group(g_lytSearch, inSearchMode, scale, alpha);
        if (inSearchMode) {
            setAutoAdapter();
            g_edtSearch.requestFocus();
            showKeyboard();
            resetSearchView(false);
        } else {
            g_edtSearch.clearFocus();
            g_edtSearch.setText("");
            dismissKeyboard();
            resetSearchView(false);
        }
    }

    private void setAutoAdapter() {
        Set<String> autos = PbbSP.getGSP(this).getSharedPreferences()
                .getStringSet(PbbSP.SP_AUTO_SEARCH, null);
        if (autos != null) {
            g_edtSearch.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, autos.toArray(new String[]{})));
        }
    }

    private void search(String searchText, int direction) {
        if (TextUtils.isEmpty(searchText)) {
            GlobalToast.toastShort(this, "请输入搜索内容");
        } else {
            if (mSearchTask == null) {
                mSearchTask = new SearchTask(this, core) {
                    @Override
                    protected void onTextFound(SearchTaskResult result) {
                        SearchTaskResult.set(result);
                        mDocView.setDisplayedViewIndex(result.pageNumber);
                        mDocView.resetupChildren();
                        resetSearchView(true);
                    }
                };
            }

            dismissKeyboard();
            int displayPage = mDocView.getDisplayedViewIndex();
            SearchTaskResult r = SearchTaskResult.get();
            int searchPage = r != null ? r.pageNumber : -1;
            mSearchTask.go(searchText, direction, displayPage, searchPage);

            PbbSP.setAutoSearchText(this, searchText);
        }
    }

    private void resetSearchView(boolean isSearching) {
        if (isSearching) {
            g_txtComplete.setVisibility(View.GONE);
            g_lytSearchOrder.setVisibility(View.VISIBLE);
        } else {
            g_lytSearchOrder.setVisibility(View.GONE);
            g_txtComplete.setVisibility(View.VISIBLE);
            SearchTaskResult.set(null);
            mDocView.resetupChildren();
        }
    }

	/*-******************************************
     *TODO Jump
	 ********************************************/

    private View g_lytJump;
    private PycEditText g_edtJump;
    private boolean inJumpMode = false;

    private void changeJumpMode() {
        inJumpMode = !inJumpMode;
        g_lytJump.setVisibility(inJumpMode ? View.VISIBLE : View.GONE);
        Animation scale = AnimationUtil.scale(g_lytJump, false, inJumpMode, Location.Right,
                new Pair<Float, Float>(0.70f, 0.5f));
        Animation alpha = AnimationUtil.alpha(g_lytJump, false, inJumpMode);
        AnimationUtil.group(g_lytJump, inJumpMode, scale, alpha);
        if (inJumpMode) {
            g_edtJump.requestFocus();
            g_edtJump.setHint("请输入跳转页数：1～" + core.countPages());
            showKeyboard();
        } else {
            g_edtJump.clearFocus();
            g_edtJump.setText("");
            dismissKeyboard();
        }
    }

    private void jump(String jump) {
        if (TextUtils.isEmpty(jump)) {
            GlobalToast.toastShort(this, "请输入跳转页数");
        } else {
            final int total = core.countPages();
            int pageInt = Integer.valueOf(jump);
            if (pageInt < 1 || pageInt > total) {
                GlobalToast.toastShort(getApplicationContext(), "选择范围：1~" + total);
                g_edtJump.setText("");
            } else {
                changeJumpMode();
                goToPage(pageInt - 1);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (inSearchMode) {
                changeSearchMode();
                return true;
            }
            if (inJumpMode) {
                changeJumpMode();
                return true;
            }
            //TODO：删除下载的共享临时文件
            deletePDFByPCTemp();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void deletePDFByPCTemp() {
        if (!mCurPath.contains(PCFileHelp.getPCShareOffset())) return;
        //if (core == null) return;
        //if (mDocView == null) return;
        //int curPage = mDocView.getDisplayedViewIndex();
        //int readPage = (int) (core.countPages() * 0.9);
        //阅读超过了90%的页数
        //if (curPage >= readPage) {
        String fileName = com.sz.mobilesdk.util.FileUtil.getNameFromFilePath(mCurPath);
        PCFileHelp.deleteFile(fileName);
        //}
    }

    @Override
    public void onBackButtonClick(View v) {
        super.onBackButtonClick(v);
        deletePDFByPCTemp();
    }

    private Toast toast = null;

    private OnChangedListener changedListener = new OnChangedListener() {
        @Override
        public void onPageChanged(int curPage) {
            if (toast == null) {
                toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 100);
            }
            toast.setText(String.format("%d/%d", curPage + 1, core.countPages()));
            toast.show();
            MuPDFActivity.this.curPage = curPage;
        }

        @Override
        public void onTapMainArea() {
            showControlView(!ViewUtil.isShown(mTopLayout), false);
        }
    };

    private void goToPage(int page) {
        curPage = page;
        mDocView.setDisplayedViewIndex(page);
    }

    private int curPage;

    private void showOutline() {
        OutlineItem[] outline = core.getOutline();
        if (outline != null) {
            OutlineActivity.mItems = outline;
            Intent intent = new Intent(MuPDFActivity.this, OutlineActivity.class);
            intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PROGRESS, curPage);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent intent) {
        super.onActivityResult(request, result, intent);
        if (result == RESULT_OK) {
            int page = intent.getIntExtra(GlobalIntentKeys.BUNDLE_DATA_PROGRESS, -1);
            if (page != -1) {
                goToPage(page);
            }
        }
    }

    private void savePage() {
        if (mDocView != null) {
            SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putInt(PageStoreName, mDocView.getDisplayedViewIndex());
            edit.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        savePage();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSearchTask != null)
            mSearchTask.stop();

        savePage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDocView != null) {
            mDocView.applyToChildren(new ReaderView.ViewMapper() {
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (core != null) {
            core.onDestroy();
        }
    }

    // mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
    // mPageSlider.setProgress(index * mPageSliderRes);

    // public void showKeyboard()
    // {
    // InputMethodManager imm = (InputMethodManager)
    // getSystemService(Context.INPUT_METHOD_SERVICE);
    // if (imm != null)
    // imm.showSoftInput(mSearchText, 0);
    // }
    //
    // private void hideKeyboard()
    // {
    // InputMethodManager imm = (InputMethodManager)
    // getSystemService(Context.INPUT_METHOD_SERVICE);
    // if (imm != null)
    // imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    // }

    @Override
    public boolean onSearchRequested() {
        // if (mButtonsVisible && mTopBarMode == TopBarMode.Search)
        // {
        // hideButtons();
        // }
        // else
        // {
        // showButtons();
        // searchModeOn();
        // }
        return super.onSearchRequested();
    }

    @Override
    protected void onStop() {
        if (core != null) {
            core.stopAlerts();
        }

        super.onStop();
    }

    @Override
    public void performPickFor(FilePicker picker) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ReaderView.HORIZONTAL_SCROLLING = newConfig.orientation == Configuration
                .ORIENTATION_PORTRAIT;
        //		float scale = 1.0f;
        //		if (!ReaderView.HORIZONTAL_SCROLLING)
        //		{
        //			DisplayMetrics dm = ScreenUtil.getScreenRect(this);
        //			int screenWidth = Math.max(dm.widthPixels, dm.heightPixels);
        //			float pageWidth = core.getPageWidth();
        //			_SysoXXX.message("screenWidth:"+screenWidth);
        //			_SysoXXX.message("pageWidth:"+pageWidth);
        //			scale = 1.0f * screenWidth / pageWidth;
        //		}
        //		mDocView.setScale(scale);
        mDocView.setDisplayedViewIndex(curPage);
        //		_SysoXXX.message("scale:" + scale);
    }
}
