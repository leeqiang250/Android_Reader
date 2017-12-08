package cn.com.pyc.pbbonline;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.adapter.AlbumList_Adapter;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.bean.event.ListAlbumSelectEvent;
import cn.com.pyc.pbbonline.bean.event.MusicCircleEvent;
import cn.com.pyc.pbbonline.bean.event.MusicListFileSelectEvent;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.db.manager.ClickIndexDBManager;
import cn.com.pyc.pbbonline.listener.OnBackGestureListener;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.SeparatedUtil;
import cn.com.pyc.pbbonline.util.ViewHelp;

import com.sz.mobilesdk.SZAlbumInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.UIHelper;

import de.greenrobot.event.EventBus;

/**
 * 专辑列表 （pdf，音乐，视频）
 * <p>
 * 已废弃
 */
public class ListAlbumActivity extends PbbBaseActivity
{

	private Album album;
	private int jumpWay;
	private GestureDetector mGestureDetector;
	private ListView mListView;
	private AlbumList_Adapter adapter;

	private String myProductId;
	private int lastPosition;
	private ClickIndexDBManager sDb;
	private boolean isStop;

	//List<DrmFile> drmFiles;
	List<SZFile> videoFiles;
	List<SZFile> pdfFiles;
	List<SZFile> musicFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pbbonline_activity_album_list);
		ViewHelp.showAppTintStatusBar(this);
		sDb = ClickIndexDBManager.Builder();
		getValue();
		initView();
		loadData();
		EventBus.getDefault().register(this);
	}

	protected void initView()
	{
		((TextView) findViewById(R.id.title_tv)).setText(album.getName());
		mListView = (ListView) findViewById(R.id.album_listview);
		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Object obj = mListView.getItemAtPosition(position);
				if (obj != null && obj instanceof AlbumContent)
				{
					adapter.setContentId(null);
					adapter.setCurrentPosition(position);
					adapter.notifyDataSetChanged();

					AlbumContent ac = (AlbumContent) obj;
					SZLog.d("文件类型", "" + ac.getFileType());
					openUI(position, ac);
				}
			}
		});
		mGestureDetector = new GestureDetector(this, new OnBackGestureListener()
		{

			@Override
			public void onFlingRight()
			{
				flingRightBack();
			}
		});

		/**
		 * 此处让listview执行手势事件,避免和activity的OnTouchEvent()冲突
		 */
		mListView.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				mGestureDetector.onTouchEvent(event);
				return false;
			}
		});
		findViewById(R.id.back_img).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				flingRightBack();
			}
		});
	}

	protected void loadData()
	{
		List<AlbumContent> contents = SZAlbumInterface.getAlbumContentList();
		separated(contents);
		if (adapter == null)
			adapter = new AlbumList_Adapter(this, contents);
		adapter.setCurrentPosition(lastPosition);
		mListView.setAdapter(adapter);
		// 定位到当前选择阅读的位置
		mListView.setSelection(lastPosition);
	}

	/**
	 * 分离文件
	 * 
	 * @param contents
	 */
	private void separated(List<AlbumContent> contents)
	{
		SeparatedUtil sep = new SeparatedUtil();
		videoFiles = sep.getSZFiles(contents, myProductId, Fields.MP4);
		pdfFiles = sep.getSZFiles(contents, myProductId, Fields.PDF);
		musicFiles = sep.getSZFiles(contents, myProductId, Fields.MP3);
	}

	protected void getValue()
	{
		Intent intent = getIntent();
		album = (Album) intent.getSerializableExtra("Album");
		jumpWay = intent.getIntExtra(K.JUMP_FLAG, 0);
		myProductId = album.getMyproduct_id();
		// 第一次初始化专辑中的数据
		SZAlbumInterface.initMedias(album.getId());

		//最后一次保存的位置。
		lastPosition = sDb.findIndexByMyProId(myProductId);
	}

	/**
	 * 通过扩展名判断打开哪一个页面
	 * 
	 * @param pos
	 * @param ac
	 */
	private void openUI(int pos, AlbumContent ac)
	{
		//必定从主界面跳转。remove code.
		if (jumpWay != K.UI_MAIN)
			return;

		String contentId = ac.getContent_id();
		String fileType = ac.getFileType();
		switch (fileType)
		{
			case Fields.PDF:
			{
				boolean flag = sDb.saveDb(pos, myProductId, contentId, Fields.PDF);
				if (flag && pdfFiles != null)
				{
					//打开PDF界面
					OpenPageUtil.openPDFReader(this, contentId, pdfFiles);
				}
			}
				break;

			case Fields.MP3:
			{
				boolean flag = sDb.saveDb(pos, myProductId, contentId, Fields.MP3);
				if (flag && musicFiles != null)
				{
					//同一个文件，则不需要初始化。
					boolean isInit = !(TextUtils.equals(K.CURRENT_MUSIC_ID, contentId));
					SZLog.v("isInit: " + isInit, K.CURRENT_MUSIC_ID + "");
					OpenPageUtil.openMusicPlayer(this, album.getName(), contentId, musicFiles,
							isInit);
				}
			}
				break;

			case Fields.MP4:
			{
				boolean flag = sDb.saveDb(pos, myProductId, contentId, Fields.MP4);
				if (flag && videoFiles != null)
				{
					// 关闭音乐播放器广播
					EventBus.getDefault().post(new MusicCircleEvent(false));
					OpenPageUtil.stopMediaService(this);
					OpenPageUtil.openVideoPlayer(this, contentId, videoFiles);
				}
			}
				break;
			default:
				showToast("不支持的文件类型(" + fileType + ")");
				break;
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		flingRightBack();
	}

	/**
	 * 右滑返回
	 */
	private void flingRightBack()
	{
		setResult(Activity.RESULT_CANCELED, null);
		finish();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		isStop = false;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		isStop = true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	private int getCurrentPos(String contentId)
	{
		List<AlbumContent> contents = SZAlbumInterface.getAlbumContentList();
		if (contents == null || contents.isEmpty())
			return 0;
		int alloc_pos = 0;
		for (int i = 0; i < contents.size(); i++)
		{
			AlbumContent ac = contents.get(i);
			if (contentId.equals(ac.getContent_id()))
			{
				alloc_pos = i;
				break;
			}
		}
		return alloc_pos;
	}

	/**
	 * 接收选择item文件的第订阅事件，事件通知从ListSZFileActivity的item点击发送
	 * 
	 * @param event
	 */
	public void onEventMainThread(ListAlbumSelectEvent event)
	{
		if (adapter == null || myProductId == null || sDb == null)
			return;

		String contentId = event.getContentId();
		String fileType = event.getFileType();
		adapter.setCurrentPosition(-1);
		adapter.setContentId(contentId);
		adapter.notifyDataSetChanged();

		//存储位置
		int alloc_pos = getCurrentPos(contentId);
		sDb.saveDb(alloc_pos, myProductId, contentId, fileType);
	}

	/**
	 * 当前界面可见，歌曲播放完毕后，切换歌曲。通知从mediaservice发送。
	 * 
	 * @param event
	 */
	public void onEventMainThread(MusicListFileSelectEvent event)
	{
		String contentId = event.getContentId();
		//根据歌曲contentId,获取当前歌曲文件在列表中的索引
		int alloc_pos = getCurrentPos(contentId);
		if (sDb == null || myProductId == null)
			return;
		//保存播放文件的位置索引。
		sDb.saveDb(alloc_pos, myProductId, contentId, Fields.MP3);

		if (isStop || adapter == null)
			return;
		adapter.setCurrentPosition(-1);
		adapter.setContentId(contentId);
		adapter.notifyDataSetChanged();

	}
}
