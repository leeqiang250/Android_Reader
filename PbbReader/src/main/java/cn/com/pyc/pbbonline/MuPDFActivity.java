package cn.com.pyc.pbbonline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
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
import com.qlk.util.tool.Util.AnimationUtil;
import com.qlk.util.tool.Util.AnimationUtil.Location;
import com.qlk.util.tool.Util.ViewUtil;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.Bookmark;
import com.sz.mobilesdk.database.practice.BookmarkDAOImpl;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
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

import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LogerHelp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.bean.event.MuPDFBookMarkDelEvent;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.widget.HighlightImageView;
import cn.com.pyc.widget.MarqueeText;
import de.greenrobot.event.EventBus;

/**
 * pdf阅读界面
 */
public class MuPDFActivity extends Activity implements View.OnClickListener
{
	private static final String TAG = "MuPDFUI";
	// 竖屏和横屏时缩放倍数
	// public static final float sScaleVertical = 1.0f;
	// public static final float sScaleHorizontal = 1.1002f;// 2.3224998f;
	private static final int OUTLINE_REQUEST = 10;
	private static final int RELOAD_DATA_REQUEST = 12;
	private MuPDFCore core;
	private List<OutlineItem> outlines;
	private String mFileName;
	private MuPDFReaderView mDocView;
	private View mButtonsView;
	private boolean mButtonsVisible;
	private TextView mPageCurrentView; // 当前页数
	private TextView mPageTotalView; // 总页数
	private SeekBar mPageSlider;
	private HighlightImageView mPdfFileInfoButton;
	private ViewAnimator mTopBarSwitcher;
	private TextView tvFloatPage;
	private View bottomBarMain;

	private boolean mStartTouchSeekBar = false;
	private boolean mReflow = false;
	private int mPageSliderRes;
	private TopBarMode mTopBarMode = TopBarMode.Main;
	private PopupWindow pwInfo;
	/** 最外层父类layout */
	private RelativeLayout mParentLayout;
	private RelativeLayout mupdf_situation;
	private RelativeLayout mTitleBar;
	private ImageView makebook;
	private int page_few;
	private String content_id;
	private PdfPageSharedPreference preference;
	private ToastShow tos = ToastShow.getToast();

	private SZFile mCurrentPdfFile;
	private List<SZFile> pdfFiles;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getWindow() != null)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		preference = new PdfPageSharedPreference(this);
		getParams();
		initCore(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	/**
	 * 获取传递过来的值
	 */
	private void getParams()
	{
		Intent intent = getIntent();
		String contentId = intent.getStringExtra("contentId");
		pdfFiles = intent.getParcelableArrayListExtra("pdfFiles");
		if (pdfFiles == null || contentId == null)
		{
			tos.showFail(getApplicationContext(), "文件出错了(0.0)");
			finish();
			return;
		}
		//计算打开的位置。
		int startPos = Util_.getStartIndex(contentId, pdfFiles);
		SZLog.e(TAG, "open pdf curPos = " + startPos);
		mCurrentPdfFile = pdfFiles.get(startPos);
		content_id = mCurrentPdfFile.getContentId();
		mFileName = content_id;
		SZLog.d(TAG, "content_Id:" + contentId);
		SZLog.d(TAG, "content_id:" + content_id);
	}

	private void makeButtonsView()
	{
		mButtonsView = getLayoutInflater().inflate(R.layout.pbbonline_activity_pdf_buttons, null);
		mupdf_situation = (RelativeLayout) mButtonsView.findViewById(R.id.mupdf_situation);
		TextView pdfCount = (TextView) mButtonsView.findViewById(R.id.pdf_text_count);
		pdfCount.setVisibility(View.VISIBLE);
		pdfCount.setText(pdfFiles.size() + "");
		MarqueeText pdfName = (MarqueeText) mButtonsView.findViewById(R.id.pdf_name);// PDF页面顶部名称
		pdfName.setText(mCurrentPdfFile.getName());

		makebook = (ImageView) mButtonsView.findViewById(R.id.pdf_makebook);
		TextView mOutlineText = (TextView) mButtonsView.findViewById(R.id.pdf_outlineText);
		HighlightImageView mPdfListButton = (HighlightImageView) mButtonsView
				.findViewById(R.id.pdf_list_Button);
		//		BadgeView badgeView = new BadgeView(this);
		//		badgeView.setHideOnNull(false);
		//		badgeView.setTargetView(mPdfListButton);
		//		badgeView.setBadgeCount(pdfFiles.size());
		//		badgeView.setBackground(10, Color.parseColor("#ffffff"));
		//		badgeView.setTextColor(Color.parseColor("#bb000000"));
		//		badgeView.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);

		mPdfFileInfoButton = (HighlightImageView) mButtonsView.findViewById(R.id.pdf_info_Button);
		mTopBarSwitcher = (ViewAnimator) mButtonsView.findViewById(R.id.switcher);
		mPageCurrentView = (TextView) mButtonsView.findViewById(R.id.currentPage_pdf_txt);
		mPageTotalView = (TextView) mButtonsView.findViewById(R.id.totalPage_pdf_txt);
		mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
		HighlightImageView mTitleBack = (HighlightImageView) mButtonsView
				.findViewById(R.id.pdf_back);
		mTitleBar = (RelativeLayout) mButtonsView.findViewById(R.id.rel_titlebar);
		tvFloatPage = (TextView) mButtonsView.findViewById(R.id.tv_float_page);
		bottomBarMain = mButtonsView.findViewById(R.id.bottomBar0Main);

		makebook.setOnClickListener(this);
		mTitleBack.setOnClickListener(this);
		mTitleBar.setOnClickListener(this);
		mTopBarSwitcher.setOnClickListener(this);
		mOutlineText.setOnClickListener(this);
		mPdfListButton.setOnClickListener(this);
		mPdfFileInfoButton.setOnClickListener(this);

		outlines = getOutlines();
	}

	private List<OutlineItem> getOutlines()
	{
		OutlineItem[] outline = core.getOutline();
		List<OutlineItem> outlineList = null;
		if (outline != null && outline.length > 0)
		{
			outlineList = Arrays.asList(outline);
			// 当前页数
			getOutlinePos(outlineList);
		}
		return outlineList;
	}

	private void getOutlinePos(List<OutlineItem> outlines)
	{
		if (outlines == null)
			return;
		for (int i = 0; i < outlines.size(); i++)
		{
			int page = outlines.get(i).page;
			if (page == page_few)
			{
				K.OUTLINE_POSITION = i;
				break;
			}
			if (i == (outlines.size() - 1))
			{
				K.OUTLINE_POSITION = i + 1;
				break;
			}
		}
	}

	/**
	 * core initial
	 * 
	 * @param savedInstanceState
	 */
	private void initCore(Bundle savedInstanceState)
	{
		if (core == null)
		{
			core = (MuPDFCore) getLastNonConfigurationInstance();
			if (savedInstanceState != null && savedInstanceState.containsKey("FileName"))
			{
				mFileName = savedInstanceState.getString("FileName");
			}
		}
		if (core == null)
		{
			try
			{
				parserIntent(savedInstanceState);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			createPdfUI(savedInstanceState);
		}
	}

	private void parserIntent(Bundle savedInstanceState)
	{
		String filePath = getFilePath();
		core = openPdfFile(filePath);
		if (core != null && core.needsPassword())
		{
			requestPassword(savedInstanceState);
			return;
		}
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(filePath);
			byte buffer[] = new byte[inputStream.available()];
			inputStream.read(buffer, 0, inputStream.available());
			core = openPdfBuffer(buffer);
			if (core != null && core.needsPassword())
			{
				tos.showBusy(getApplicationContext(), getString(R.string.need_password));
				requestPassword(savedInstanceState);
				return;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void requestPassword(Bundle savedInstanceState)
	{
		String key = getCliperKey();
		if (mCurrentPdfFile.isCheckOpen() && core.authenticatePassword(key))
		{
			createPdfUI(savedInstanceState);
		}
		else
		{
			makeButtonsView();
			mParentLayout = new RelativeLayout(this);
			mParentLayout.addView(mButtonsView);
			setContentView(mParentLayout);
			tos.show(getApplicationContext(), ToastShow.IMG_BUSY,
					getString(R.string.read_miss_private_key), Gravity.CENTER);
		}
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.pdf_back)
		{
			finish();
		}
		else if (id == R.id.pdf_outlineText)
		{
			openOutlinesWindow(); // 目录/书签
		}
		else if (id == R.id.pdf_list_Button)
		{
			openFileList(); // 文件列表
		}
		else if (id == R.id.pdf_info_Button)
		{
			showInfo(); // 图书信息
		}
		else if (id == R.id.pdf_makebook)
		{
			makeBookMark(); // 添加书签
		}
	}

	private void openFileList()
	{
		if (pwInfo != null && pwInfo.isShowing())
		{
			pwInfo.dismiss();
			pwInfo = null;
		}
		Intent it = new Intent(this, ListSZFileActivity.class);
		it.putExtra(K.JUMP_FLAG, K.UI_PDF);// 标识从pdf界面跳转
		it.putParcelableArrayListExtra(ListSZFileActivity.FILE_FLAGS,
				(ArrayList<? extends Parcelable>) pdfFiles);
		it.putExtra("title_name", mCurrentPdfFile.getName());
		it.putExtra("cur_contentId", content_id);
		startActivityForResult(it, RELOAD_DATA_REQUEST);
	}

	/**
	 * 设置书签
	 */
	private void makeBookMark()
	{
		if (hasPermit())
		{
			Bookmark bookmark = BookmarkDAOImpl.getInstance()
					.findBookmarkById(content_id, page_few);
			String currentdate = TimeUtil.getCurrentTime();
			if (bookmark == null)
			{
				String content = "";
				TextWord[][] texwords = core.textLines(page_few);
				for (int x = 0; x < texwords.length; x++)
				{
					TextWord[] textword = texwords[x];
					for (int z = 0; z < textword.length; z++)
					{
						TextWord tw = textword[z];
						String w = tw.getW();
						content += w;
					}
				}
				content = content.replace(" ", "").trim();
				if ("".equals(content))
				{
					content = "[图片]";
				}
				else
				{
					final int maxLetters = 61;
					int originalLength = content.length();
					content = content.substring(0, originalLength < maxLetters ? originalLength
							: maxLetters);
					content = originalLength < maxLetters ? content : content + "...";
				}
				Bookmark bm = new Bookmark();
				bm.setId(System.currentTimeMillis() + "");
				bm.setContent_ids(content_id);
				bm.setContent(content);
				bm.setTime(currentdate);
				bm.setPagefew(page_few + "");
				BookmarkDAOImpl.getInstance().save(bm);
				makebook.setSelected(true);
				showToast(getString(R.string.add_bookmarks_success));
			}
			else
			{
				Bookmark bm = new Bookmark();
				bm.setId(bookmark.getId());
				bm.setContent_ids(bookmark.getContent_ids());
				bm.setContent(bookmark.getContent());
				bm.setTime(currentdate);
				bm.setPagefew(bookmark.getPagefew());
				BookmarkDAOImpl.getInstance().update(bm);
				makebook.setSelected(true);
				showToast(getString(R.string.bookmarks_exist));
			}
		}
		else
		{
			showToast(getString(R.string.bookmarks_er_because_unkey));
		}
	}

	/**
	 * 打开目录主页
	 */
	private void openOutlinesWindow()
	{
		if (pwInfo != null && pwInfo.isShowing())
		{
			pwInfo.dismiss();
			pwInfo = null;
		}
		if (!hasPermit())
		{
			tos.showBusy(getApplicationContext(), getString(R.string.read_miss_private_key));
			return;
		}
		getOutlinePos(outlines);
		Intent intent = new Intent(this, MuPDFOutlineHomeActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("outline_list", (Serializable) outlines);
		bundle.putString("title_name", mCurrentPdfFile.getName());
		bundle.putString("content_id", content_id);
		intent.putExtras(bundle);
		startActivityForResult(intent, OUTLINE_REQUEST);
	}

	/**
	 * 图书信息
	 */
	public void showInfo()
	{
		if (pwInfo == null)
		{
			showInfo(true);
		}
		else
		{
			showInfo(!pwInfo.isShowing());
		}
	}

	public void showInfo(boolean show)
	{
		if (show)
		{
			View infoView = getLayoutInflater().inflate(R.layout.pbbonline_dialog_pdf_infor, null);
			((TextView) infoView.findViewById(R.id.dvi_txt_geshi)).setText(Fields.PDF);
			((TextView) infoView.findViewById(R.id.dvi_txt_yeshu)).setText("" + core.countPages());
			if (pwInfo == null)
			{
				pwInfo = new PopupWindow(infoView, ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				//pwInfo.setAnimationStyle(R.style.PopupWindow_info_anim);
				pwInfo.setAnimationStyle(android.R.style.Animation_Dialog);
			}
			pwInfo.showAtLocation(mPdfFileInfoButton, Gravity.LEFT | Gravity.BOTTOM,
					(mPdfFileInfoButton.getLeft() + mPdfFileInfoButton.getWidth() / 14),
					(mPdfFileInfoButton.getBottom() + 10));
		}
		else
		{
			if (pwInfo != null)
			{
				pwInfo.dismiss();
				pwInfo = null;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode)
		{
			case OUTLINE_REQUEST:
				if (data != null)
				{
					mDocView.setDisplayedViewIndex(data.getIntExtra("page", 1));
				}
				break;

			case RELOAD_DATA_REQUEST:
			{
				if (data != null)
				{
					MuPDFActivity.this.finish();
					String contentId = data.getStringExtra("contentId");
					List<SZFile> files = data.getParcelableArrayListExtra("pdfFiles");
					OpenPageUtil.openPDFReader(this, contentId, files);
				}
			}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null)
		{
			outState.putString("FileName", mFileName);
			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded Other info
			// is needed only for screen-orientation change,
			// so it can go in the bundle
			preference.putPageInt("page" + mFileName, mDocView.getDisplayedViewIndex());
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);

		if (mReflow)
			outState.putBoolean("ReflowMode", true);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		tos.cancel();
		if (mFileName != null && mDocView != null)
			preference.putPageInt("page" + mFileName, mDocView.getDisplayedViewIndex());
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		release();
		EventBus.getDefault().unregister(this);
		if (pwInfo != null && pwInfo.isShowing())
		{
			pwInfo.dismiss();
			pwInfo = null;
		}
	}

	private void release()
	{
		if (mDocView != null)
		{
			mDocView.applyToChildren(new ReaderView.ViewMapper()
			{

				public void applyToView(View view)
				{
					((MuPDFView) view).releaseBitmaps();
				}
			});
		}
		if (core != null)
		{
			core.onDestroy();
			core = null;
		}
	}

	private MuPDFCore openPdfFile(String path)
	{
		Util_.checkFileExist(this, path, new UIHelper.DialogCallBack()
		{
			@Override
			public void onConfirm()
			{
				finish();
			}
		});

		SZLog.i("Trying to open: " + path);
		try
		{
			//TODO:打开pdf初始化方法。
			core = new MuPDFCore(this, path, null, 0, 0);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		}
		catch (Exception e)
		{
			ExtraParams ep = new ExtraParams();
			ep.account_name = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
			String password = (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, "");
			ep.account_password= AESUtil.encrypt(password);
			ep.lines = LogerHelp.getLineNumber();
			LogerEngine.debug(getApplicationContext(), "MuPDFCore解析失败" + Log.getStackTraceString(e), true, ep);
			e.printStackTrace();
			return null;
		}
		catch (java.lang.OutOfMemoryError e)
		{
			// out of memory is not an Exception, so we catch it separately.
			e.printStackTrace();
			return null;
		}
		return core;
	}

	private MuPDFCore openPdfBuffer(byte buffer[])
	{
		SZLog.i("Trying to open byte buffer");
		try
		{
			core = new MuPDFCore(this, buffer, null);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return core;
	}

	public void createPdfUI(Bundle savedInstanceState)
	{
		if (core == null)
			return;

		// Set up the page slider
		int smax = Math.max(core.countPages() - 1, 1);
		mPageSliderRes = ((10 + smax - 1) / smax) * 2;
		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(this)
		{
			@Override
			protected void onMoveToChild(int i)
			{
				if (core == null)
					return;
				page_few = i;
				// 当前页初始化
				mPageCurrentView.setText(getString(R.string.page_n, (i + 1)));
				mPageSlider.setProgress(i * mPageSliderRes);
				// 书签
				Bookmark bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(content_id,
						page_few);
				makebook.setSelected(bookmark != null);
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea()
			{
				if (!mButtonsVisible)
				{
					showButtons();
				}
				else
				{
					if (mTopBarMode == TopBarMode.Main)
						hideButtons();
				}
			}

			@Override
			protected void onDocMotion()
			{
				hideButtons();
			}

			@Override
			protected void onHit(Hit item)
			{
				super.onHit(item);
				switch (mTopBarMode)
				{
					case Annot:
						if (item == Hit.Annotation)
						{
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
						if (pageView != null)
							pageView.deselectAnnotation();
						break;
				}
			}
		};
		MuPDFPageAdapter adapter = new MuPDFPageAdapter(this, null, core);
		mDocView.setAdapter(adapter);

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// 总页数初始化
		mPageTotalView.setText(getString(R.string.page_total, core.countPages()));
		mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
		int height = bottomBarMain.getHeight();
		height = height > 0 ? height : CommonUtil.dip2px(this, 100f);
		RelativeLayout.LayoutParams params = (LayoutParams) tvFloatPage.getLayoutParams();
		params.bottomMargin = (int) (height * 1.2f);
		tvFloatPage.setLayoutParams(params);
		ViewUtil.visible(tvFloatPage);

		// Activate the seekbar
		mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				if (!hasPermit())
					return;
				mStartTouchSeekBar = false;
				mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2)
						/ mPageSliderRes);
				// 隐藏悬浮页数textview
				tvFloatPage.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						tvFloatPage.startAnimation(AnimationUtils.loadAnimation(MuPDFActivity.this,
								android.R.anim.fade_out));
						ViewUtil.gone(tvFloatPage);
					}
				}, 600);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				if (!hasPermit())
					return;
				mStartTouchSeekBar = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (!hasPermit())
					return;
				updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
				// 滑动时view不可见了，则设置可见
				if (mStartTouchSeekBar && tvFloatPage.getVisibility() == View.GONE)
				{
					tvFloatPage.clearAnimation();
					ViewUtil.visible(tvFloatPage);
				}
			}
		});

		// Reenstate last state if it was recorded
		int page = preference.getPageInt("page" + mFileName, 0);
		SZLog.d(TAG, "filePage = " + page);
		mDocView.setDisplayedViewIndex(page);

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		if (savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false))
			reflowModeSet(true);

		// Stick the document view and the buttons overlay into a parent view
		mParentLayout = new RelativeLayout(this);
		////if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		////	mParentLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		FrameLayout fLayout = new FrameLayout(this);
		fLayout.setBackgroundColor(Color.parseColor("#ECECEC"));
		// ////// fLayout.addView(mButtonsView);
		if (hasPermit())
		{
			mupdf_situation.setBackgroundDrawable(null);
			mPageSlider.setEnabled(true);
			fLayout.addView(mDocView);
		}
		else
		{
			mupdf_situation.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8B8989")));
			mPageSlider.setEnabled(false);
			mPageCurrentView.setText(getString(R.string.page_n, 0));
			mPageTotalView.setText(getString(R.string.page_total, 0));
			mPageSlider.setMax(0);
			mPageSlider.setProgress(0);
			ViewUtil.gone(tvFloatPage);
			tos.show(getApplicationContext(), ToastShow.IMG_BUSY,
					getString(R.string.read_miss_private_key), Gravity.CENTER);
		}
		fLayout.addView(mButtonsView); // update 2015-12-03
		mParentLayout.addView(fLayout);
		setContentView(mParentLayout);
		ViewHelp.showAppTintStatusBar(this, R.color.video_bkg_lightdark);
	}

	/**
	 * 当前文件是否有权限
	 * 
	 * @return
	 */
	private boolean hasPermit()
	{
		return mCurrentPdfFile.isCheckOpen();
	}

	private String getFilePath()
	{
		return mCurrentPdfFile.getFilePath();
	}

	private String getCliperKey()
	{
		return mCurrentPdfFile.getCek_cipher_value();
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	private void reflowModeSet(boolean reflow)
	{
		mReflow = reflow;
		mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(
				this, null, core));
		mDocView.refresh(mReflow);
	}

	private void showButtons()
	{
		if (core == null)
			return;
		if (!mButtonsVisible)
		{
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();

			updatePageNumView(index);

			ViewUtil.visible(tvFloatPage);

			AnimationUtil.translate(mTitleBar, true, true, Location.Top);
			AnimationUtil.translate(mTopBarSwitcher, true, true, Location.Bottom);

			// mTitleBar.setVisibility(View.VISIBLE);
			// mTopBarSwitcher.setVisibility(View.VISIBLE);
		}
	}

	private void hideButtons()
	{
		if (mButtonsVisible)
		{
			mButtonsVisible = false;
			////getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			AnimationUtil.translate(mTitleBar, true, false, Location.Top);
			AnimationUtil.translate(mTopBarSwitcher, true, false, Location.Bottom);
			ViewUtil.gone(tvFloatPage);

			// mTitleBar.setVisibility(View.INVISIBLE);
			// mTopBarSwitcher.setVisibility(View.INVISIBLE);

			if (pwInfo != null)
			{
				showInfo(false);
			}
		}
	}

	/**
	 * 更新当前页数
	 * 
	 * @param index
	 */
	private void updatePageNumView(int index)
	{
		if (core == null)
			return;

		mPageCurrentView.setText(getString(R.string.page_n, (index + 1)));
		tvFloatPage.setText(getString(R.string.page_n, (index + 1)));
		// 总页数
		// mPageTotalView.setText(getString(R.string.page_total,
		// core.countPages()));
	}

	private void showToast(String str)
	{
		Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		finish();
	}

	/** The core rendering instance */
	enum TopBarMode
	{
		Main, Search, Annot, Delete, More, Accept
	}

	/**
	 * 保存pdf页码的SharedPreferences
	 * 
	 * @author hudq
	 */
	public static class PdfPageSharedPreference
	{

		private SharedPreferences.Editor editor;
		private SharedPreferences prefs;
		private String PREFS_NAME = "File_Page_Preferences";

		public PdfPageSharedPreference(Context context)
		{
			prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			editor = prefs.edit();
		}

		/**
		 * 清除页数保存
		 */
		public void clearPdfPageData()
		{
			editor.clear().commit();
		}

		/**
		 * 保存键值
		 * 
		 * @param key
		 * @param value
		 */
		public boolean putPageInt(String key, int value)
		{
			editor.putInt(key, value);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
			{
				editor.apply();
				return true;
			}
			return editor.commit();
		}

		/**
		 * 获取数值
		 * 
		 * @param key
		 * @param defValue
		 * @return
		 */
		public int getPageInt(String key, int defValue)
		{
			return prefs.getInt(key, defValue);
		}
	}

	/**
	 * 接收删除书签的事件
	 * 
	 * @param event
	 */
	public void onEventMainThread(MuPDFBookMarkDelEvent event)
	{
		// 删除书签
		SZLog.d(TAG, "delete bookmark");
		Bookmark bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(content_id, page_few);
		makebook.setSelected(bookmark != null);
	}
}
