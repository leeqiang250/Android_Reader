package cn.com.pyc.pbbonline;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.sz.mobilesdk.SZDBInterface;
import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.practice.AlbumDAOImpl;
import com.sz.mobilesdk.manager.db.DownData2DBManager;
import com.sz.mobilesdk.models.FolderInfo;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.mobilesdk.util.UIHelper.DialogCallBack;
import com.sz.view.widget.ToastShow;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.adapter.ShareDetailsOfflineAdapter;
import cn.com.pyc.pbbonline.adapter.ShareDetailsPageAdapter;
import cn.com.pyc.pbbonline.bean.event.RefreshDeviceNumEvent;
import cn.com.pyc.pbbonline.bean.event.RefreshShareInfoEvent;
import cn.com.pyc.pbbonline.bean.event.RefreshShareRecordEvent;
import cn.com.pyc.pbbonline.bean.event.SaveShareRecordEvent;
import cn.com.pyc.pbbonline.common.IMusicConst;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.common.ShareMode;
import cn.com.pyc.pbbonline.db.Shared;
import cn.com.pyc.pbbonline.db.SharedDBManager;
import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.pbbonline.model.DataBean;
import cn.com.pyc.pbbonline.model.FolderInfoModel;
import cn.com.pyc.pbbonline.util.DeleteFileUtil;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.SortNameUtil;
import cn.com.pyc.pbbonline.util.SwipUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.pbbonline.widget.ExpandableLayout;
import de.greenrobot.event.EventBus;

/**
 * 分享详情信息页
 * 
 * @author hudq
 */
public class ShareDetailsPageActivity extends PbbBaseActivity implements OnClickListener
{
	private static final String TAG = "ShareDetailsPage";
	public static final int CODE_VERIFY = 0x1a1;
	public static final int CODE_LOGIN_AGAIN = 0x1a3;

	private static final int MSG_DELETE = 0x10;
	private static final int MSG_OFFLINE = 0x12;
	private static final int MSG_DBDATA = 0x14;
	private Handler mHandler = new MyHandler(this);

	private TextView sharepeopleans;
	private TextView postscriptans;
	private TextView equipmentans;
	private TextView timelimitans;
	private SwipeMenuListView mListView;
	private SwipeMenuListView mOfflineListView;
	private ExpandableLayout app_detail_safety_info;
	private TextView title_tv;
	private View emptyView;
	private TextView emptyTextView;

	private String tk_shortCode;
	private String shareId;
	private String theme;
	private String owner;
	private ShareDetailsPageAdapter adapter;
	private ShareDetailsOfflineAdapter mOfflineAdapter;
	private Cancelable mGetShareInfoHttp;
	private boolean isStart;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pbbonline_activity_sharedetails_page);
		ViewHelp.showAppTintStatusBar(this);
		init();
		LoadData();
	}


	private void init()
	{
		Intent intent = getIntent();
		shareId = intent.getStringExtra("ShareID");
		String shareName = intent.getStringExtra("ShareName");
		tk_shortCode = intent.getStringExtra(IndexPageHomeActivity.TK_SHORT_CODE);

		sharepeopleans = (TextView) findViewById(R.id.sharepeopleans);
		postscriptans = (TextView) findViewById(R.id.postscriptans);
		equipmentans = (TextView) findViewById(R.id.equipmentans);
		timelimitans = (TextView) findViewById(R.id.timelimitans);
		mListView = (SwipeMenuListView) findViewById(R.id.lv_folder);
		app_detail_safety_info = (ExpandableLayout) findViewById(R.id.app_detail_safety_info);
		title_tv = (TextView) findViewById(R.id.title_tv);
		emptyView = findViewById(R.id.arl_lyt_empty);
		emptyTextView = (TextView) findViewById(R.id.vep_txt_prompt);
		mOfflineListView = (SwipeMenuListView) findViewById(R.id.lv_offline_folder);

		EventBus.getDefault().register(this);
		findViewById(R.id.back_img).setOnClickListener(this);
		if (!TextUtils.isEmpty(shareName))
			title_tv.setText(shareName);
		isStart = true;
	}

	/**
	 * 初始化数据
	 */
	private void LoadData()
	{
			// 联网
		if (CommonUtil.isNetConnect(this))
		{
			mListView.setVisibility(View.VISIBLE);
			mOfflineListView.setVisibility(View.GONE);
			SwipUtil.initSwipItem(mListView, netMenuItemClickListener);
			hasNetListener();
			getData(false);
		}
		else
		{
			// 离线
			mListView.setVisibility(View.GONE);
			mOfflineListView.setVisibility(View.VISIBLE);
			SwipUtil.initSwipItem(mOfflineListView, offMenuItemClickListener);
			hasOfflineListener();
			loadOfflineData();
		}
	}

	/**
	 * 加载本地已下载的数据
	 */
	private void loadOfflineData()
	{
		showLoading(this);
		ExecutorManager.getInstance().execute(new OfflineThread(this));
	}

	private static class OfflineThread implements Runnable
	{
		private WeakReference<ShareDetailsPageActivity> reference;

		public OfflineThread(ShareDetailsPageActivity activity)
		{
			reference = new WeakReference<ShareDetailsPageActivity>(activity);
		}

		@Override
		public void run()
		{
			if (reference == null || reference.get() == null)
				return;
			ShareDetailsPageActivity activity = reference.get();

			if (activity.mOfflineAdapter != null)
			{
				activity.mOfflineAdapter.getAlbums().clear();
				activity.mOfflineAdapter = null;
			}
			List<Album> albumlist = AlbumDAOImpl.getInstance().findAll(Album.class, "DESC");
			List<Album> albumList_ = SortNameUtil.sortAlbumItems(Util_
					.wipeRepeatAlbumData(albumlist));
			Message msg = Message.obtain();
			msg.what = MSG_OFFLINE;
			Bundle bundle = new Bundle();
			bundle.putSerializable("offline_data", (Serializable) albumList_);
			msg.setData(bundle);
			activity.mHandler.sendMessageDelayed(msg, 300);
		}
	}


	//传参
	private Bundle createParams(String receiveID)
	{
		Bundle bundle = new Bundle();
		bundle.putString("myToken", (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""));
		bundle.putString("shareId", shareId);
		if (!TextUtils.isEmpty(receiveID))
		{
			bundle.putString("receiveId", receiveID);
		}
		if (hasTkcode4Clip())
		{
			bundle.remove("shareId");
			bundle.putString("code", tk_shortCode);
		}
		return bundle;
	}

	//访问路径url
	private String createActionUrl(String receiveID)
	{
		String actionUrl = APIUtil.getShareInfoUrl();
		if (!TextUtils.isEmpty(receiveID))
		{
			actionUrl = APIUtil.getShareInfo2Url();
		}
		if (hasTkcode4Clip())
		{
			actionUrl = APIUtil.getShareInfoByCodeUrl();
		}
		return actionUrl;
	}

	//存在剪贴板里的短码
	private boolean hasTkcode4Clip()
	{
		return !TextUtils.isEmpty(tk_shortCode);
	}

	/**
	 * 加载数据
	 * 
	 * @param isRefeshNum
	 *            是否只刷新number数
	 */
	private void getData(final boolean isRefeshNum)
	{
		if (!isRefeshNum && isStart)
			showLoading(this);
		String receiveID = (String) SPUtil.get(Fields.FIELDS_RECEIVE_ID, "");
		Bundle bundle = createParams(receiveID);
		String url = createActionUrl(receiveID);
		mGetShareInfoHttp = GlobalHttp.getOn(url, bundle, new Callback.CommonCallback<String>()
		{
			@Override
			public void onCancelled(CancelledException arg0)
			{
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				SZLog.d(TAG, "onError: " + arg0.getMessage());
				getDataFailed(getString(R.string.load_server_failed));
			}

			@Override
			public void onFinished()
			{
				hideLoading();
			}

			@Override
			public void onSuccess(String arg0)
			{
				SZLog.d(TAG, "onSuccess: " + arg0);
				FolderInfoModel o = JSON.parseObject(arg0, FolderInfoModel.class);
				if (o != null && o.isSuccess())
				{
					getDataSuccess(o, isRefeshNum);
				}
				else
				{
					String tips = getString(R.string.load_data_empty);
					if (hasTkcode4Clip())
					{
						tips = getString(R.string.shared_invalid_by_tkcode);
					}
					getDataFailed(tips);
				}
			}
		});
	}

	/**
	 * @param a
	 * @param isRefeshNum
	 *            true只刷新数量，反之加载ui数据<br/>
	 *            刷新ui的num数量。
	 */
	private void getDataSuccess(FolderInfoModel a, boolean isRefeshNum)
	{
		DataBean data = a.getData();
		if (data == null)
		{
			//分享时效或被收回
			getDataFailed(getString(R.string.shared_lose_efficacy));
			return;
		}
		int limmit = 0;
		switch (data.getShare_mode())
		{
			case ShareMode.SHAREDEVICE:
				limmit = data.getMax_device_num() - data.getReceive_device_num();
				break;
			case ShareMode.SHAREUSER:
			case ShareMode.SHARECOUNT:
				limmit = data.getLimit_num() - data.getReceive_device_num();
				break;
			default:
				break;
		}
		if (isRefeshNum)
		{
			//只刷新绑定设备数量
			Log.v(TAG, "only refesh device num");
			equipmentans.setText(getString(R.string.shared_surplus_n_table, limmit));
			return;
		}
		String deadTime = null;
		if (data.isUnlimmit())
		{
			deadTime = getString(R.string.effective_longtime);
		}
		else if (data.isDayRange())
		{
			deadTime = data.getShare_time().replace("/", "至");
		}
		else
		{
			deadTime = getString(R.string.first_bind_n_day, data.getShare_time());
		}
		timelimitans.setText(deadTime);
		equipmentans.setText(getString(R.string.shared_surplus_n_table, limmit));
		postscriptans.setText(TextUtils.isEmpty(data.getMessage()) ? "无" : data.getMessage());
		title_tv.setText(data.getTheme());
		sharepeopleans.setText(data.getOwner());
		this.theme = data.getTheme();
		this.owner = data.getOwner();
		List<FolderInfo> itembean_list = a.getPageInfo().getItems();
		if (itembean_list == null || itembean_list.isEmpty())
		{
			mListView.setEmptyView(emptyView);
			emptyTextView.setText("暂无内容");
			return;
		}
		final String shareUrl = data.getUrl();
		if (shareUrl != null
				&& (shareUrl.contains("receiveId=") || shareUrl.contains("receiveID=") || shareUrl
						.contains("ReceiveID=")))
		{
			String receiveId = StringUtil.getStringByResult(shareUrl, "receiveId=");
			SPUtil.save(Fields.FIELDS_RECEIVE_ID, receiveId);
		}
		initDataByResult_TkCode(itembean_list, data);
	}

	private void getDataFailed(String str)
	{
		mListView.setEmptyView(emptyView);
		emptyTextView.setText(str);
	}

	private void initAdapter(List<FolderInfo> infos, DataBean data)
	{
		adapter = new ShareDetailsPageAdapter(this, infos);
		mListView.setAdapter(adapter);

		adapter.setListView(mListView);
		adapter.setTheme(data.getTheme());
		adapter.setOwner(data.getOwner());
	}

	/**
	 * 剪贴板存在短码，加载数据然后创建相关数据库；不存在就默认加载
	 * 
	 * @param infos
	 * @param bean
	 */
	private void initDataByResult_TkCode(final List<FolderInfo> infos, final DataBean bean)
	{
		if (!hasTkcode4Clip())
		{
			Log.v(TAG, "load folder success");
			app_detail_safety_info.setVisibility(View.VISIBLE);
			initAdapter(infos, bean);
			return;
		}
		Log.v(TAG, "create data by tk_shortcode");
		showBgLoading(this, getString(R.string.loading_waiting));
		Thread tk = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				String ShareID = bean.getMyshare_id();
				ShareMode.Mode.value = bean.getShare_mode();
				SPUtil.save(Fields.FIELDS_SCAN_URL, bean.getUrl());		// 保存分享的url
				SPUtil.save(Fields.FIELDS_ID, ShareID);					// 保存分享的shareId
				SZInitInterface.saveUserName(Fields.GUEST_PBB);			// 保存分享获取的用户名，默认为guestpbb

				SZDBInterface.destoryDBHelper();
				SZInitInterface.destoryFilePath();
				String name_ = SZInitInterface.getUserName("") + Fields._LINE + ShareID;
				SZInitInterface.createFilePath(name_);
				SZDBInterface.createDB();

				Message message = Message.obtain();
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("info_list", (ArrayList<FolderInfo>) infos);
				bundle.putSerializable("data_bean", bean);
				message.setData(bundle);
				message.what = MSG_DBDATA;
				mHandler.sendMessage(message);
			}
		});
		ExecutorManager.getInstance().execute(tk);
	}

	OnMenuItemClickListener netMenuItemClickListener = new OnMenuItemClickListener()
	{
		@Override
		public void onMenuItemClick(int position, SwipeMenu menu, int index)
		{
			if (adapter == null)
				return;
			if (index != 0)
				return;
			FolderInfo o = adapter.getItem(position);
			// 如果有下载状态，需要判断“正在下载”和“下载完成开始解析”不可删除！
			checkRemove(o.getProductName(), o.getMyProId(), null);
		}
	};
	OnMenuItemClickListener offMenuItemClickListener = new OnMenuItemClickListener()
	{
		@Override
		public void onMenuItemClick(int position, SwipeMenu menu, int index)
		{
			if (mOfflineAdapter == null)
				return;
			if (index != 0)
				return;
			Album a = mOfflineAdapter.getItem(position);
			checkRemove(a.getName(), a.getMyproduct_id(), a);
		}
	};

	private void checkRemove(String name, final String myProId, final Album a)
	{
		String content = getString(R.string.delete_localfile);
		UIHelper.showCommonDialog(this, name, content, getString(R.string.delete),
				new DialogCallBack()
				{
					@Override
					public void onConfirm()
					{
						removeItem(myProId, a);
					}
				});
	}

	private void removeItem(final String myProId, final Album a)
	{
		showBgLoading(this);
		// 判断是否正在播放音乐
		if (K.playState != IMusicConst.OPTION_STOP)
			OpenPageUtil.stopMediaService(this);

		Thread mClear = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				String id__ = (String) SPUtil.get(Fields.FIELDS_ID, "");
				DeleteFileUtil.deleteFolder(id__, myProId);
				DownData2DBManager.Builder().deleteByFolderId(myProId);
				Message message = Message.obtain();
				message.what = MSG_DELETE;
				if (a != null)
					message.obj = a;
				mHandler.sendMessageDelayed(message, 300);
			}
		});
		mClear.setPriority(Thread.NORM_PRIORITY - 1);
		ExecutorManager.getInstance().execute(mClear);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.back_img)
		{
			finish();
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		isStart = true;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		isStart = false;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		GlobalHttp.cancelHttp(mGetShareInfoHttp);
		ExecutorManager.shutdownNow();
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onBackPressed()
	{
		//finishUI();
		finish();
	}

	//	private void finishUI()
	//	{
	//		DownloadTaskUtil.stopAllDownloadTask(this);
	//		finish();
	//	}

	//update:20160928
	/*
	 * 存在文件更新，接收通知 :<br/>
	 * 1.删除此前下载的专辑，2.删除权限，3.重新下载<br/>
	 * @param event
	 */
	//	public void onEventMainThread(UpdateFolderEvent event)
	//	{
	//		DownloadInfo o = event.getO();
	//		String myProId = event.getMyProId();
	//		int position = o.getPosition();
	//
	//		if (K.playState != IMusicConst.OPTION_STOP)
	//			OpenPageUtil.stopMediaService(this);
	//		ClickIndexDBManager.Builder().deleteByMyProId(myProId);
	//		String _filePath = DirsUtil.getSaveFilePath(SZInitInterface.getUserName(""),
	//				(String) SPUtil.get(Fields.FIELDS_ID, ""), myProId);
	//		FileUtil.delAllFile(_filePath);
	//		SZAlbumInterface.deleteAlbumAttachInfo(myProId);
	//		updateDownload(o);
	//
	//		SZLog.d(TAG, "update album position is " + position + "，download again");
	//	}

	//update:20160928
	/*
	 * 更新下载
	 * @param o
	 * 下载产品的信息object
	 */
	//	private void updateDownload(DownloadInfo o)
	//	{
	//		if (adapter == null)
	//		{
	//			showToast(getString(R.string.getdata_failed));
	//			return;
	//		}
	//		adapter.download(this, o);
	//	}

	//update:20160928
	/*
	 * 接收通知：开始下载
	 * @param e
	 */
	//	public void onEventMainThread(DownloadFolderEvent e)
	//	{
	//		if (adapter == null)
	//		{
	//			showToast(getString(R.string.getdata_failed));
	//			return;
	//		}
	//		DownloadInfo o = e.getO();
	//		int position = o.getPosition();
	//		DownloadTaskUtil.startDownloadTask(this, o, position);
	//		o.setTaskState(DownloadTaskManager.WAITING);
	//		adapter.updateItemViewWhenDownload(this, position, o);
	//	}

	//update:20160928
	/*
	 * 下载监听
	 */
	//	private DownloadReceiver receiver = new DownloadReceiver()
	//	{
	//		PbbBaseActivity context = ShareDetailsPageActivity.this;
	//
	//		@Override
	//		protected void updateProgress(int position, int progress, long currentSize, long totalSize,
	//				boolean isLastSaveProgress)
	//		{
	//			// 正在下载， 更新进度
	//			DownloadInfo o = adapter.getInfos().get(position);
	//			o.setProgress(progress);
	//			o.setCurrentSize(currentSize);
	//			if (o.getTotalSize() == 0)
	//				o.setTotalSize(totalSize);
	//			// 最后一次进度只是用来显示
	//			if (isLastSaveProgress)
	//			{
	//				o.setTaskState(DownloadTaskManager.PAUSE);
	//				adapter.updateItemViewWhenDownload(context, position, o);
	//			}
	//			else
	//			{
	//				o.setTaskState(DownloadTaskManager.DOWNLOADING);
	//				adapter.updateProgress(context, position, o);
	//			}
	//		}
	//
	//		@Override
	//		protected void pathError(int position, String myProId)
	//		{
	//			DownloadInfo o = adapter.getInfos().get(position);
	//			o.setTaskState(DownloadTaskManager.DOWNLOAD_ERROR);
	//			adapter.updateItemViewWhenDownload(context, position, o);
	//		}
	//
	//		@Override
	//		protected void connectError(int position, String myProId)
	//		{
	//			DownloadInfo o = adapter.getInfos().get(position);
	//			o.setTaskState(DownloadTaskManager.CONNECT_ERROR);
	//			adapter.updateItemViewWhenDownload(context, position, o);
	//		}
	//
	//		@Override
	//		protected void connecting(int position)
	//		{
	//			DownloadInfo o = adapter.getInfos().get(position);
	//			o.setTaskState(DownloadTaskManager.CONNECTING);
	//			adapter.updateItemViewWhenDownload(context, position, o);
	//		}
	//
	//		@Override
	//		protected void downloadFinish(int position, String myProId, DownloadInfo downloadInfo)
	//		{
	//			SZLog.i("download file at " + position + "，id : " + myProId);
	//			DownloadInfo o = adapter.getInfos().get(position);
	//			o.setTaskState(DownloadTaskManager.COMPLETE);
	//			adapter.updateItemViewWhenDownload(context, position, o);
	//			DownloadTaskUtil.startParserFileService(context, position, myProId, o);
	//		}
	//
	//		@Override
	//		protected void parserFinish(int position)
	//		{
	//			DownloadInfo o = adapter.getInfos().get(position);
	//			o.setTaskState(DownloadTaskManager.INIT);
	//			adapter.updateItemViewParserOver(context, position, o);
	//			DownloadReceiver.sTaskIdSet.remove(o.getMyProId());
	//			showToast(getString(R.string.download_n_finished, o.getProductName()));
	//		}
	//	};

	private static class MyHandler extends Handler
	{
		private WeakReference<ShareDetailsPageActivity> reference;

		public MyHandler(ShareDetailsPageActivity acty)
		{
			reference = new WeakReference<ShareDetailsPageActivity>(acty);
		}

		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (reference == null || reference.get() == null)
				return;
			ShareDetailsPageActivity activity = reference.get();
			switch (msg.what)
			{
				case MSG_DELETE:
				{
					activity.hideBgLoading();
					if (activity.adapter != null)
						activity.adapter.notifyDataSetChanged();
					else if (activity.mOfflineAdapter != null)
					{
						Album a_ = (Album) msg.obj;
						activity.mOfflineAdapter.getAlbums().remove(a_);
						activity.mOfflineAdapter.notifyDataSetChanged();
						if (activity.mOfflineAdapter.getAlbums().isEmpty())
						{
							activity.mOfflineListView.setEmptyView(activity.emptyView);
							activity.emptyTextView.setText(activity
									.getString(R.string.connect_net_download));
						}
					}
					ToastShow.getToast().showOk(activity,
							activity.getString(R.string.delete_localfile_success));
				}
					break;
				case MSG_OFFLINE:
				{
					Bundle bundle = msg.getData();
					@SuppressWarnings("unchecked")
					List<Album> albums = (List<Album>) bundle.getSerializable("offline_data");
					activity.hideLoading();
					if (albums == null || albums.isEmpty())
					{
						activity.mOfflineListView.setEmptyView(activity.emptyView);
						activity.emptyTextView.setText(activity
								.getString(R.string.connect_net_download));
						ToastShow.getToast().show(activity, ToastShow.IMG_FAIL,
								activity.getString(R.string.network_check_available),
								Gravity.CENTER);
						return;
					}
					activity.mOfflineAdapter = new ShareDetailsOfflineAdapter(activity, albums);
					activity.mOfflineListView.setAdapter(activity.mOfflineAdapter);
				}
					break;
				case MSG_DBDATA:
				{
					activity.hideBgLoading();
					activity.app_detail_safety_info.setVisibility(View.VISIBLE);
					Bundle bundle = msg.getData();
					List<FolderInfo> infos = bundle.getParcelableArrayList("info_list");
					DataBean bean = (DataBean) bundle.getSerializable("data_bean");
					activity.initAdapter(infos, bean);
				}
					break;
				default:
					break;
			}
		}
	}

	//update:20160928
	//	@Override
	//	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	//	{
	//		super.onActivityResult(requestCode, resultCode, data);
	//		if (resultCode != Activity.RESULT_OK)
	//			return;
	//		switch (requestCode)
	//		{
	//			case CODE_VERIFY:
	//			case CODE_LOGIN_AGAIN:
	//			{
	//				if (data == null)
	//					return;
	//				if (data.getBooleanExtra("opt_flag", false))
	//				{
	//					getData(true);	//登录成功，加载数据，点击按钮下载！
	//				}
	//			}
	//				break;
	//			default:
	//				break;
	//		}
	//	}

	/**
	 * 网络连接时，点击item监听事件
	 */
	private void hasNetListener()
	{
		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Object obj = mListView.getItemAtPosition(position);
				if (obj != null && obj instanceof FolderInfo)
				{
					FolderInfo o = (FolderInfo) obj;
					////Album album = AlbumDAOImpl.getInstance().findAlbumByMyproId(o.getMyProId());
					////if (album != null)
					////	return;

					OpenPageUtil.openFileListPage(ShareDetailsPageActivity.this, o.getMyProId(),
							o.getProductName(), o.getPublishDate(), theme, owner, o.getDelFileIds());
				}
			}
		});
	}

	/**
	 * 离线时，点击item事件监听
	 */
	private void hasOfflineListener()
	{
		mOfflineListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Object obj = mOfflineListView.getItemAtPosition(position);
				if (obj != null && obj instanceof Album)
				{
					Album a = (Album) obj;
					OpenPageUtil.openFileListPage(ShareDetailsPageActivity.this,
							a.getMyproduct_id(), a.getName());
				}
			}
		});
	}

	/**
	 * 接收绑定成功或接收成功通知，保存分享记录；后台线程执行操作
	 * 
	 * @param event
	 */
	public void onEventBackgroundThread(SaveShareRecordEvent event)
	{
		String shareID = (String) SPUtil.get(Fields.FIELDS_ID, "");
		SharedDBManager dbManager = new SharedDBManager();
		Shared oShared = dbManager.findByShareId(shareID);
		if (oShared != null)
		{
			return;
		}
		Shared o = new Shared(shareID, event.getTheme(), event.getOwner());
		o.setShareUrl((String) SPUtil.get(Fields.FIELDS_SCAN_URL, ""));
		o.setTime(System.currentTimeMillis());
		o.setShareMode(ShareMode.Mode.value);
		//按设备，不需要考虑账户,默认guestpbb;按身份，人数，账户即为登录手机号,二次分享可能账号为""
		boolean device = (ShareMode.SHAREDEVICE.equals(o.getShareMode()));
		String account = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
		o.setAccountName(device ? SZInitInterface.getUserName("") : account);
		o.setWhetherNew(false);
		boolean save_ = dbManager.saveShared(o);

		Log.v(TAG, "receive save record msg");
		if (save_)
		{
			//通知recordList页面刷新。
			Log.d(TAG, "send msg reload recordlist");
			EventBus.getDefault().post(new RefreshShareRecordEvent());
		}
	}

	/**
	 * 绑定设备成功，接收通知刷新数量
	 * 
	 * @param e
	 */
	public void onEventMainThread(RefreshDeviceNumEvent e)
	{
		getData(true);
	}

	/**
	 * 删除收回的文件成功，刷新，从 ListFileUI发送通知
	 * 
	 * @param e
	 */
	public void onEventMainThread(RefreshShareInfoEvent e)
	{
		getData(false);
	}

}
