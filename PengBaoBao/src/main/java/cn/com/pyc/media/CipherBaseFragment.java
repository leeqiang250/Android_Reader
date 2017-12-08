package cn.com.pyc.media;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewAnimator;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.qlk.util.base.BaseFragment;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.media.ISelection.ISelectListener;
import com.qlk.util.widget.PullRefreshView;
import com.qlk.util.widget.PullRefreshView.OnRefreshListener;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cn.com.pyc.pbb.R;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.plain.CameraTakerActivity;
import cn.com.pyc.reader.ExtraImageReaderActivity;
import cn.com.pyc.reader.ExtraVideoPlayerActivity;
import cn.com.pyc.sm.PayLimitConditionActivity;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.xcoder.XCodeView;
import cn.com.pyc.xcoder.XCodeView.XCodeType;

/**
 * It works for "TAG_CIPHER_IMAGE", "TAG_CIPHER_FILE" and "TAG_CIPHER_VIDEO". <br>
 * It's similar to "PlainBaseFragment".
 * 
 * @author QiLiKing 2015-8-18 上午11:32:45
 */
/**
 * 隐私空间的图片 隐私空间的文档 隐私空间的视频
 * @author 李巷阳
 * @date 2016-11-14 下午3:47:16
 * @version V1.0
 */
public class CipherBaseFragment extends BaseFragment implements OnClickListener {
	private GlobalData mType;
	private final ArrayList<String> mPaths = new ArrayList<>();
	private HashMap<String, String> mFileTimes = new HashMap<>();
	private MediaBaseAdapter mAdapter;
	private PullRefreshView mPullRefreshLayout;
	private AbsListView mAbsView;
	private View mEmptyLayout;
	private ViewStub mEmptyStub;
	private View mDateChosenLayout;
	private ViewAnimator mAnimator;
	private ToggleButton mToggleButton;
	private TextView mTitleView;
	private TextView mDateView;
	private TextView mChosenView;
	private ImageButton mCameraView;
	private ImageButton mDecryptView;
	private ImageButton mSmView;

	private boolean isOverflowOccured = false;

	@Override
	protected boolean isObserverEnabled() {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("my_fragment", this.getClass().getName());
		init_data();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_cipher_base, container, false);
		init_view(v);
		init_listener();
		initUI();
		refreshUI();
		return v;
	}
	/**   
	* @author 李巷阳
	* @date 2016-11-14 下午4:54:02 
	*/
	private void init_view(View v) {
		mTitleView = (TextView) v.findViewById(R.id.fcb_txt_title);
		mToggleButton = (ToggleButton) v.findViewById(R.id.fcb_tbn_operate);
		mDateChosenLayout = v.findViewById(R.id.fcb_lyt_date_chosen);
		mDateView = (TextView) mDateChosenLayout.findViewById(R.id.idc_txt_date);
		mChosenView = (TextView) mDateChosenLayout.findViewById(R.id.idc_txt_chosen);
		mPullRefreshLayout = (PullRefreshView) v.findViewById(R.id.pull_down_refresh);
		int id = mType.equals(GlobalData.Image) ? R.id.fcb_grv_data : R.id.fcb_lsv_data;
		mAbsView = (AbsListView) v.findViewById(id);
		mEmptyStub = (ViewStub) v.findViewById(R.id.fcb_lyt_empty);
		mAnimator = (ViewAnimator) v.findViewById(R.id.fcb_lyt_bottom);
		mCameraView = (ImageButton) mAnimator.findViewById(R.id.fcb_imb_camera);
		mSmView = (ImageButton) mAnimator.findViewById(R.id.fcb_imb_sm);
		mDecryptView = (ImageButton) mAnimator.findViewById(R.id.fcb_imb_decrypt);
	}
	/**   
	* @author 李巷阳
	* @date 2016-11-14 下午4:54:04 
	*/
	private void init_listener() {
		mToggleButton.setOnClickListener(this);
		mCameraView.setOnClickListener(this);
		mSmView.setOnClickListener(this);
		mDecryptView.setOnClickListener(this);
		mAnimator.findViewById(R.id.fcb_imb_encrypt).setOnClickListener(this);
		mAnimator.findViewById(R.id.fcb_imb_transmit).setOnClickListener(this);
		// 下拉刷新
		mPullRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				mType.instance(getActivity()).search(true);
			}
		});
		// listview的item点击
		mAbsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mAdapter.isSelecting()) {
					mAdapter.setItemSelected(position);
				} else {
					goToReadView(position);
				}
			}
		});
		// listview 滑动事件
		mAbsView.setOnScrollListener(new OnScrollListener() {
			private int scrollState = SCROLL_STATE_IDLE;
			private boolean isFirstScroll = true;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				this.scrollState = scrollState;
				if (scrollState == SCROLL_STATE_IDLE) {
					refreshFileTime();
				}
				// The adapter will refresh thumbs when user press and scroll.
				mAdapter.changeScrollState(view, scrollState);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
					refreshFileTime();
				}
				if (isFirstScroll && visibleItemCount > 0) {
					isFirstScroll = false;
					mAdapter.refresh(view);
				}
			}
		});
		// listview的长按item事件，显示发送和解锁按钮等。
		mAbsView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
				if (mAdapter.isSelecting()) {
					// 关闭框
					mAdapter.setSelectable(false);
				} else {
					// 打开选择框和发送按钮
					mAdapter.setSelectable(true);
					mAdapter.setItemSelected(position);
				}
				return true;
			}
		});
	}

	/**   
	* @author 李巷阳
	* @date 2016-11-14 下午4:51:50 
	*/
	private void init_data() {
		String type = getArguments().getString(GlobalIntentKeys.BUNDLE_DATA_TYPE);
		switch (type) {
		case Pbb_Fields.TAG_CIPHER_IMAGE:
			mType = GlobalData.Image;
			break;
		case Pbb_Fields.TAG_CIPHER_FILE:
			mType = GlobalData.Pdf;
			break;
		case Pbb_Fields.TAG_CIPHER_VIDEO:
			mType = GlobalData.Video;
			break;
		default:
			break;
		}
	}

	@Override
	protected void initUI() {
		mAbsView.setVisibility(View.VISIBLE);
		mAdapter = getAdapter();
		mAbsView.setAdapter(mAdapter);
		mAdapter.setSelectListener(new ISelectListener() {
			@Override
			public void onSelcetChanged(boolean overflow, int total, boolean allSelected) {
				if (overflow) {
					if (!isOverflowOccured) {
						isOverflowOccured = true;
						GlobalToast.toastShort(getActivity(), "所选文件超出可用空间");
						changeDecryptBtnClickable(false);
						changeSmBtnClickable(false);
					}
				} else {
					if (isOverflowOccured) {
						isOverflowOccured = false;
						changeDecryptBtnClickable(true);
						changeSmBtnClickable(true);
					}
				}

				if (total > 0) {
					mChosenView.setVisibility(View.VISIBLE);
					mChosenView.setText("已选择" + total + "个");
					mAnimator.setDisplayedChild(1);
					showDialog();
				} else {
					mChosenView.setVisibility(View.GONE);
					mAnimator.setDisplayedChild(0);
				}

				mToggleButton.setChecked(mAdapter.isSelecting());
			}
		});
	}

	/**
	 * 刷新，数据源添加到path集合中
	 * @author 李巷阳
	 * @date 2016-11-14 下午5:51:12
	 */
	public void refreshUI() {
		mPullRefreshLayout.setRefreshComplete();
		mPaths.clear();
		mPaths.addAll(mType.instance(getActivity()).getCopyPaths(true));
		// 如果为空则现在数据为空提示,否则正常显示
		if (mPaths.isEmpty()) {
			mToggleButton.setVisibility(View.GONE);
			mDateChosenLayout.setVisibility(View.INVISIBLE);
			if (mEmptyLayout == null) {
				mEmptyLayout = mEmptyStub.inflate();
				initZero((TextView) mEmptyLayout.findViewById(R.id.vep_txt_prompt));
			}
			mEmptyLayout.setVisibility(View.VISIBLE);
		} else {
			mToggleButton.setVisibility(View.VISIBLE);
			mDateChosenLayout.setVisibility(View.VISIBLE);
			if (mEmptyLayout != null) {
				mEmptyLayout.setVisibility(View.GONE);
			}
			refreshFileTime();
		}
		mAnimator.setDisplayedChild(0);
		mAdapter.setSelectable(false);
		mAdapter.refresh(mAbsView);
		// 设置title栏
		mTitleView.setText(getMyTitle());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 操作，取消
		case R.id.fcb_tbn_operate:
			mAdapter.setSelectable(mToggleButton.isChecked());
			break;
		// 图片
		case R.id.fcb_imb_camera:
			Intent intent = new Intent(getActivity(), CameraTakerActivity.class);
			intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, false);
			intent.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_MEDIA_TYPE, mType);
			startActivity(intent);
			break;
		// 文件夹
		case R.id.fcb_imb_encrypt:
			goToEncrypt();
			break;
		// 解密钥匙
		case R.id.fcb_imb_decrypt:
			new XCodeView(getActivity()).startXCode(XCodeType.Decrypt, null, true, getCheckedPaths().toArray(new String[] {}));
			break;
		// 发送
		case R.id.fcb_imb_sm:
			sendSm();
			break;
		// 传送到电脑
		case R.id.fcb_imb_transmit:
			new ExtraDelSndFile().sendFiles(getActivity(), getCheckedPaths(), mType);
			break;

		default:
			break;
		}
	}

	private void sendSm() {
		ArrayList<String> pathSend = getCheckedPaths();
		if (pathSend.size() > 1) {
			GlobalToast.toastShort(getActivity(), "每次只能发送一个安全文件");
			return;
		}
//		Intent intent = new Intent(getActivity(), ChooseSMwayActivity.class);
		Intent intent = new Intent(getActivity(), PayLimitConditionActivity.class);
		intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, pathSend.get(0));
		startActivity(intent);
	}

	private ArrayList<String> getCheckedPaths() {
		return new ArrayList<>(mAdapter.getSelected());
	}

	// 点击那个“文件夹”的按钮
	private void goToEncrypt() {
		switch (mType) {
		case Image:
			changeFragment(Pbb_Fields.TAG_PLAIN_IMAGE_SORT);
			break;
		case Pdf:
			changeFragment(Pbb_Fields.TAG_PLAIN_FILE);
			break;
		case Video:
			changeFragment(Pbb_Fields.TAG_PLAIN_VIDEO);
			break;

		default:
			break;
		}
	}

	private void goToReadView(int position) {
		Class<?> clas = null;
		switch (mType) {
		case Image:
			clas = ExtraImageReaderActivity.class;
			break;
		case Video:
			clas = ExtraVideoPlayerActivity.class;
			break;
		case Pdf:
			clas = MuPDFActivity.class;
			break;

		default:
			break;
		}
		Intent intent = new Intent(getActivity(), clas);
		intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, mPaths.get(position));
		intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATHS, mPaths);
		intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, true);
		startActivity(intent);
	}

	// 蒙板
	private void showDialog() {
		if ((boolean) PbbSP.getGSP(getActivity()).getValue(PbbSP.SP_GUIDE_DECRYPT_LAYOUT_FIRST_SHOW, true)) {
			View view = new View(getActivity());
			view.setBackgroundResource(R.drawable.guide_2);

			final Dialog dialog = new Dialog(getActivity(), R.style.no_bkg_pyc);
			dialog.setContentView(view);
			dialog.show();
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});
			PbbSP.getGSP(getActivity()).putValue(PbbSP.SP_GUIDE_DECRYPT_LAYOUT_FIRST_SHOW, false);
		}
	}

	private void changeDecryptBtnClickable(boolean clickable) {
		mDecryptView.setClickable(clickable);
		mDecryptView.setBackgroundResource(clickable ? R.drawable.xml_decrypt : R.drawable.decrypt_disabled);
	}

	private void changeSmBtnClickable(boolean clickable) {
		mSmView.setClickable(clickable);
		mSmView.setBackgroundResource(clickable ? R.drawable.xml_makesm : R.drawable.sm_disabled);
	}

	private MediaBaseAdapter getAdapter() {
		if (mType.equals(GlobalData.Image)) {
			return new CipherImageAdapter(getActivity(), mPaths);
		} else {
			return new MediaListAdapter(getActivity(), mPaths);
		}
	}
	/**
	* @Description: (获取第一张图片的时间,设置到item顶端) 
	* @author 李巷阳
	* @date 2016-11-14 下午5:43:54
	 */
	private void refreshFileTime() {
		if (mPaths.size() > 0) {
			String firstFilePath = mPaths.get(mAbsView.getFirstVisiblePosition());
			String firstFileTime = mFileTimes.get(firstFilePath);
			if (firstFileTime == null) {
				firstFileTime = DateFormat.getDateInstance().format(new Date(new File(firstFilePath).lastModified()));
				mFileTimes.put(firstFilePath, firstFileTime);
			}

			mDateView.setText(firstFileTime);
		}
	}

	private CharSequence getMyTitle() {
		String title = mType.equals(GlobalData.Image) ? "图片" : mType.equals(GlobalData.Pdf) ? "文档" : "视频";
		return "已加密" + title + "(" + mPaths.size() + ")";
	}

	/*-
	 * 当没有文件时，界面上显示的文字信息
	 */
	private void initZero(TextView zero) {
		if (mType.equals(GlobalData.Pdf)) {
			Drawable s = getResources().getDrawable(R.drawable.scancode_small);
			s.setBounds(0, 0, s.getIntrinsicWidth(), s.getIntrinsicHeight());

			SpannableString spannable = new SpannableString("通过s对文档进行加密");

			ImageSpan span1 = new ImageSpan(s);
			spannable.setSpan(span1, 2, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			zero.setText(spannable);
			mCameraView.setVisibility(View.GONE);
		} else {
			Drawable s = getResources().getDrawable(R.drawable.scancode_small);
			s.setBounds(0, 0, s.getIntrinsicWidth(), s.getIntrinsicHeight());

			Drawable t = getResources().getDrawable(R.drawable.takephoto_small);
			t.setBounds(0, 0, t.getIntrinsicWidth(), t.getIntrinsicHeight());

			SpannableString spannable = new SpannableString("通过s或者t对图片进行加密");

			ImageSpan span1 = new ImageSpan(s);
			spannable.setSpan(span1, 2, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

			ImageSpan span2 = new ImageSpan(t);
			spannable.setSpan(span2, 5, 6, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

			zero.setText(spannable);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mAdapter.exit(); // Create in "onCreateView" and exit in
							// "onDestroyView".
	}

}
