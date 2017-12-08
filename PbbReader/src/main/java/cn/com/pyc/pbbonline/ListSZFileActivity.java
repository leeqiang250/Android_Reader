package cn.com.pyc.pbbonline;

import java.util.ArrayList;
import java.util.List;

import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.SZLog;
import com.sz.view.widget.AVLoadingIndicatorView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.bean.event.ListAlbumSelectEvent;
import cn.com.pyc.pbbonline.bean.event.MusicListFileSelectEvent;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.common.IMusicConst;
import cn.com.pyc.pbbonline.listener.OnBackGestureListener;
import cn.com.pyc.pbbonline.util.ValidDateUtil;
import cn.com.pyc.pbbonline.util.ViewHelp;
import de.greenrobot.event.EventBus;

/**
 * 列表，文件分离后的列表。
 */
public class ListSZFileActivity extends PbbBaseActivity
{
	public static final String FILE_FLAGS = "list_szfiles";
	private static final String TAG = "ListSZFile";
	private GestureDetector mGestureDetector;
	private TextView tvTitle;
	private MyAdapter adapter;
	private List<SZFile> files;
	private int jumpWay;
	private boolean isDestory;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pbbonline_activity_list_szfile);
		isDestory = false;
		Intent intent = getIntent();
		jumpWay = intent.getIntExtra(K.JUMP_FLAG, 0);
		files = intent.getParcelableArrayListExtra(FILE_FLAGS);
		String cur_contentId = intent.getStringExtra("cur_contentId");
		String name = intent.getStringExtra("title_name");
		SZLog.d(TAG, "jumpWay = " + jumpWay);
		SZLog.d(TAG, "cur_contentId = " + cur_contentId);

		tvTitle = ((TextView) findViewById(R.id.menu_title_tv));
		tvTitle.setText(name);
		ListView mListView = (ListView) findViewById(R.id.listview_menu_list);
		int titleColorId = -1;
		if (jumpWay == K.UI_MUSIC)
		{
			EventBus.getDefault().register(this);
			titleColorId = R.color.touming;
			findViewById(R.id.menu_title_line).setVisibility(View.VISIBLE);
			View backBg = findViewById(R.id.llayout_menu_list);
			if (MusicHomeActivity.bgId == 0)
				MusicHomeActivity.bgId = R.drawable.music_bg;
			backBg.setBackgroundDrawable(getResources().getDrawable(MusicHomeActivity.bgId));
		}
		else
		{
			titleColorId = R.color.title_top_color;
			View titleBg = findViewById(R.id.rlayout_menu_title);
			titleBg.setBackgroundColor(getResources().getColor(titleColorId));
		}
		ViewHelp.showAppTintStatusBar(this, titleColorId);
		adapter = new MyAdapter(files, jumpWay);
		adapter.setContentId(cur_contentId);
		mListView.setAdapter(adapter);
		setListener(mListView);
	}

	private void setListener(ListView mListView)
	{
		findViewById(R.id.menu_back_img).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				back();
			}
		});
		mGestureDetector = new GestureDetector(this, new OnBackGestureListener()
		{

			@Override
			public void onFlingRight()
			{
				back();
			}
		});
		mListView.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				mGestureDetector.onTouchEvent(event);
				return false;
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (adapter == null)
					return;

				SZFile file = adapter.getItem(position);
				String contentId = file.getContentId();
				adapter.setContentId(contentId);
				adapter.notifyDataSetChanged();

				String alloc = "";
				Intent data = new Intent();
				data.putExtra("contentId", contentId);

				if (jumpWay == K.UI_PDF)
				{
					alloc = Fields.PDF;
					data.putParcelableArrayListExtra("pdfFiles",
							(ArrayList<? extends Parcelable>) files);
				}
				else if (jumpWay == K.UI_MUSIC)
				{
					alloc = Fields.MP3;
					data.putParcelableArrayListExtra("musicFiles",
							(ArrayList<? extends Parcelable>) files);
				}
				//发送通知：选择文件后，改变选中项。
				EventBus.getDefault().post(new ListAlbumSelectEvent(contentId, alloc));
				setResult(Activity.RESULT_OK, data);
				finish();
				if (jumpWay == K.UI_PDF)
					overridePendingTransition(R.anim.trans_x_in, R.anim.trans_x_out);
			}
		});
	}

	private class MyAdapter extends BaseAdapter
	{
		private String contentId;
		private List<SZFile> files;

		private int selectColorId;
		private int unSelectNameColorId;
		private int unSelectTimeColorId;
		private int selectDrawableId;
		private int unSelectDrawableId;

		private int jumpWay;
		private Drawable line;

		public void setContentId(String contentId)
		{
			this.contentId = contentId;
		}

		public MyAdapter(List<SZFile> files, int jumpWay)
		{
			this.files = files;
			this.jumpWay = jumpWay;

			this.selectColorId = getResources().getColor(R.color.blue_bar_color);
			this.unSelectNameColorId = getResources().getColor(
					(this.jumpWay == K.UI_MUSIC) ? R.color.white_ea : R.color.black_bb);
			this.unSelectTimeColorId = getResources().getColor(R.color.gray);
			this.selectDrawableId = R.drawable.ic_validate_time_select;
			this.unSelectDrawableId = R.drawable.ic_validate_time_nor;

			line = (this.jumpWay == K.UI_MUSIC) ? getResources().getDrawable(R.drawable.title_line)
					: new ColorDrawable(Color.parseColor("#D7D7D7"));
		}

		@Override
		public int getCount()
		{
			return files != null ? files.size() : 0;
		}

		@Override
		public SZFile getItem(int position)
		{
			return files.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;
			if (convertView == null)
			{
				holder = new ViewHolder();
				convertView = View.inflate(getApplicationContext(),
						R.layout.pbbonline_item_menu_list, null);
				holder.txtTitle = (TextView) convertView.findViewById(R.id.name_title);
				holder.txtTime = (TextView) convertView.findViewById(R.id.val_time);
				holder.txtType = (TextView) convertView.findViewById(R.id.val_type);
				holder.valIv = (ImageView) convertView.findViewById(R.id.val_iv);
				holder.itemLine = convertView.findViewById(R.id.view_bot_line);
				holder.avLoading = (AVLoadingIndicatorView) convertView
						.findViewById(R.id.avloading_volume);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			SZFile szFile = files.get(position);
			String contentId = szFile.getContentId();
			holder.itemLine.setBackgroundDrawable(line);
			if (TextUtils.equals(this.contentId, contentId))
			{
				boolean isPlay = (K.playState == IMusicConst.OPTION_PLAY || K.playState == IMusicConst.OPTION_CONTINUE);
				boolean isShow = (this.jumpWay == K.UI_MUSIC && isPlay);
				holder.avLoading.setVisibility(isShow ? View.VISIBLE : View.GONE);
				holder.txtTitle.setTextColor(selectColorId);
				holder.txtTime.setTextColor(selectColorId);
				holder.valIv.setImageResource(selectDrawableId);
			}
			else
			{
				holder.avLoading.setVisibility(View.GONE);
				holder.txtTitle.setTextColor(unSelectNameColorId);
				holder.txtTime.setTextColor(unSelectTimeColorId);
				holder.valIv.setImageResource(unSelectDrawableId);
			}
			holder.txtTitle.setText(szFile.getName());
			holder.txtType.setText(null);
			holder.txtTime.setText(ValidDateUtil.getValidTime(getApplicationContext(),
					szFile.getValidity_time(), szFile.getOdd_datetime_end()));
			return convertView;
		}
	}

	private static class ViewHolder
	{
		TextView txtTitle;
		TextView txtTime;
		ImageView valIv;
		TextView txtType;
		View itemLine;
		AVLoadingIndicatorView avLoading;
	}

	private void back()
	{
		setResult(Activity.RESULT_CANCELED, null);
		finish();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		back();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		isDestory = true;
		if (jumpWay == K.UI_MUSIC)
			EventBus.getDefault().unregister(this);
	}

	/**
	 * 当前界面可见，歌曲播放完毕后，切换歌曲。通知从mediaservice发送。
	 * 
	 * @param event
	 */
	public void onEventMainThread(MusicListFileSelectEvent event)
	{
		if (isDestory)
			return;
		if (jumpWay != K.UI_MUSIC)
			return;
		if (adapter == null)
			return;
		tvTitle.setText(event.getName());
		adapter.setContentId(event.getContentId());
		adapter.notifyDataSetChanged();
	}
}
