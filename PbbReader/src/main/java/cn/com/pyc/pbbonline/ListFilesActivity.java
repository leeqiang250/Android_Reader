package cn.com.pyc.pbbonline;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.qlk.util.tool.Util.ViewUtil;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.manager.DownloadTaskManager2;
import com.sz.mobilesdk.manager.db.DownData2DBManager;
import com.sz.mobilesdk.models.FileData;
import com.sz.mobilesdk.receiver.DownloadReceiver2;
import com.sz.mobilesdk.service.DownloadService2;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.DownloadTaskUtil;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.adapter.FileList_Adapter;
import cn.com.pyc.pbbonline.adapter.FileLocal_Adapter;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.bean.event.DownloadFileCheckingEvent;
import cn.com.pyc.pbbonline.bean.event.MusicCircleEvent;
import cn.com.pyc.pbbonline.bean.event.RefreshDeviceNumEvent;
import cn.com.pyc.pbbonline.bean.event.RefreshShareInfoEvent;
import cn.com.pyc.pbbonline.common.Code;
import cn.com.pyc.pbbonline.common.IMusicConst;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.common.ShareMode;
import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.pbbonline.model.CheckResultBean;
import cn.com.pyc.pbbonline.model.FilesDataModel;
import cn.com.pyc.pbbonline.util.DeleteFileUtil;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.SendMsgShareUtil;
import cn.com.pyc.pbbonline.util.SwipUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.user.CheckLoginActivity;
import cn.com.pyc.user.LoginVerifyCodeActivity;
import de.greenrobot.event.EventBus;

/**
 * 文件列表，单集下载
 */
public class ListFilesActivity extends PbbBaseActivity implements OnClickListener
{
	private static final String TAG = ListFilesActivity.class.getSimpleName();
	private static final int MSG_LOAD_LOCALDATA = 0x110f;
	private static final int MSG_CLEAR_ITEMDATA = 0x112f;
	private static final int MSG_DOWNLOAD_FILE = 0x114f;
	private static final int CODE_VERIFY = 0x0ab1;
	private static final int CODE_LOGIN_AGAIN = 0x0ab3;
	private SwipeMenuListView mListView;
	private SwipeMenuListView mOfflineListView;
	private Button downloadAllBtn;
	private View emptyView;

	private String shareFolderId;
	private String shareFolderName;
	private String shareTheme;
	private String shareOwner;
	private String sharePublishDate;
	private boolean isLoading;
	private boolean isAllDownloading;
	private boolean isUIStart;
	private FileList_Adapter adapter;
	private FileLocal_Adapter offlineAdapter;
	private int totalItemCount = 0;
	private ArrayList<SZFile> mVideoFiles = new ArrayList<>();
	private ArrayList<SZFile> mMusicFiles = new ArrayList<>();
	private ArrayList<SZFile> mPdfFiles = new ArrayList<>();
	private List<FileData> tempDatas = new ArrayList<>();	//在线，已收回的文件对应的本地文件转化的集合
	private OwnerHandler handler = new OwnerHandler(this);
	private HandlerThread handlerThread;
	private ArrayList<String> delFileIds;
	private ExecutorService $$ = Executors.newFixedThreadPool(1);
	private static Set<String> sTaskIdSet; // 存储下载任务的id
	private static Set<String> sDownloadSet; // 存储已下载的文件id
	private static Map<String, DownloadCheckRunnable> sChecks;
	private static Map<String, Cancelable> sBindHttps;
	private static Map<String, Cancelable> sReceiveHttps;

	private DownloadReceiver2 receiver2 = new DownloadReceiver2()
	{
		@Override
		protected void updateProgress(FileData data, int progress, long currentSize)
		{
			adapter.updateItemView(ListFilesActivity.this, data);
			adapter.setItemDataState(data.getPosition(), data.getTaskState());
		}

		@Override
		protected void pathError(FileData data)
		{
			sTaskIdSet.remove(data.getFiles_id());
			DownloadTaskUtil.stopDownloadFile(getApplicationContext(), data.getFiles_id());
			adapter.updateItemView(ListFilesActivity.this, data);
			adapter.setItemDataState(data.getPosition(), data.getTaskState());
			showToast(getString(R.string.link_exception_error));
		}

		@Override
		protected void connectError(FileData data)
		{
			sTaskIdSet.remove(data.getFiles_id());
			DownloadTaskUtil.stopDownloadFile(getApplicationContext(), data.getFiles_id());
			adapter.updateItemView(ListFilesActivity.this, data);
			adapter.setItemDataState(data.getPosition(), data.getTaskState());
			showToast(getString(R.string.connect_file_server_error));
		}

		@Override
		protected void connecting(FileData data)
		{
			adapter.updateItemView(ListFilesActivity.this, data);
			adapter.setItemDataState(data.getPosition(), data.getTaskState());
		}

		@Override
		protected void parsering(FileData data)
		{
			adapter.updateItemView(ListFilesActivity.this, data);
			adapter.setItemDataState(data.getPosition(), data.getTaskState());
		}

		@Override
		protected void downloadFinished(FileData data)
		{
			sTaskIdSet.remove(data.getFiles_id());
			end(data.getFiles_id());
			adapter.updateItemView(ListFilesActivity.this, data);
			adapter.setItemDataState(data.getPosition(), data.getTaskState());
			showToast(getString(R.string.download_n_finished, data.getName()));
		}

		private void end(String fileId)
		{
			sDownloadSet.add(fileId);
			if (sDownloadSet.size() == totalItemCount)
				ViewUtil.gone(downloadAllBtn);
			AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(fileId);
			if (ac == null)
				return;

			separateFileByType(ac);
		}
	};

	/**
	 * 分离文件，将数据库中AlbumContent转换成可视化对象SZFile;
	 * 
	 * @param ac
	 */
	private void separateFileByType(AlbumContent ac)
	{
		switch (ac.getFileType())
		{
			case Fields.PDF:
			{
				SZFile file = Util_.getSZFile(ac);
				if (!mPdfFiles.contains(file))
					mPdfFiles.add(file);
			}
				break;
			case Fields.MP3:
			{
				SZFile file = Util_.getSZFile(ac);
				if (!mMusicFiles.contains(file))
					mMusicFiles.add(file);
			}
				break;
			case Fields.MP4:
			{
				SZFile drmFile = Util_.getSZFile(ac);
				if (!mVideoFiles.contains(drmFile))
					mVideoFiles.add(drmFile);
			}
				break;
			default:
				break;
		}
	}

	private static class OwnerHandler extends Handler
	{
		private WeakReference<ListFilesActivity> reference;

		public OwnerHandler(ListFilesActivity context)
		{
			reference = new WeakReference<ListFilesActivity>(context);
		}

		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			final ListFilesActivity activity = reference.get();
			if (activity == null)
				return;
			switch (msg.what)
			{
				case MSG_LOAD_LOCALDATA:
				{
					Bundle bundle = msg.getData();
					@SuppressWarnings("unchecked")
					List<AlbumContent> acs = (List<AlbumContent>) bundle
							.getSerializable("local_albumContents");
					activity.hideLoading();
					if (acs == null || acs.isEmpty())
					{
						ViewHelp.setEmptyViews(activity.mOfflineListView, activity.emptyView,
								activity.getString(R.string.connect_net_download));
					}
					else
					{
						activity.offlineAdapter = new FileLocal_Adapter(activity, acs);
						activity.mOfflineListView.setAdapter(activity.offlineAdapter);
					}
				}
					break;
				case MSG_CLEAR_ITEMDATA:
				{
					if (activity.adapter != null)
					{
						activity.adapter.setFileId(null);
						activity.adapter.notifyDataSetChanged();
					}
					if (activity.offlineAdapter != null && ((int) msg.obj) != -1)
					{
						activity.offlineAdapter.getList().remove((int) msg.obj);
						activity.offlineAdapter.notifyDataSetChanged();
						if (activity.offlineAdapter.getList().isEmpty())
						{
							ViewHelp.setEmptyViews(activity.mOfflineListView, activity.emptyView,
									activity.getString(R.string.connect_net_download));
						}
					}
					activity.hideBgLoading();
					ToastShow.getToast().showOk(activity.getApplicationContext(),
							activity.getString(R.string.delete_localfile_success));
				}
					break;
				case MSG_DOWNLOAD_FILE:
				{
					CheckResultBean c = (CheckResultBean) msg.obj;
					FileData data = msg.getData().getParcelable("FileData");
					if (c != null)
					{
						activity.judgeCode(activity, c, data);
					}
					else
					{
						//验证失败，初始状态
						UIHelper.showToast(activity,
								activity.getString(R.string.load_server_failed));
						data.setTaskState(DownloadTaskManager2.INIT);
						EventBus.getDefault().post(new DownloadFileCheckingEvent(data));
					}
				}
					break;
				default:
					break;
			}
		}
	}

	//TODO:onCreate
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pbbonline_activity_files_list);
		initFlags();
		getExtras();
		initView();
		extracted();
	}

	private void initFlags()
	{
		isUIStart = true;
		if (getWindow() != null)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ViewHelp.showAppTintStatusBar(this);
		registerBroadcast();
		EventBus.getDefault().register(this);
		if (sTaskIdSet == null)
			sTaskIdSet = new LinkedHashSet<String>();
		if (sDownloadSet == null)
			sDownloadSet = new LinkedHashSet<String>();
		if (sChecks == null)
			sChecks = new LinkedHashMap<>();
		if (sBindHttps == null)
			sBindHttps = new LinkedHashMap<>();
		if (sReceiveHttps == null)
			sReceiveHttps = new LinkedHashMap<>();
	}

	private void getExtras()
	{
		Intent intent = getIntent();
		if (!intent.hasExtra("shareFolderId"))
		{
			throw new IllegalArgumentException("has extras,'shareFolderId' must be required.");
		}
		shareFolderId = intent.getStringExtra("shareFolderId"); 			//必传
		shareFolderName = intent.getStringExtra("shareFolderName");
		((TextView) findViewById(R.id.title_tv)).setText(shareFolderName);
		shareTheme = intent.getStringExtra("shareTheme");
		shareOwner = intent.getStringExtra("shareOwner");
		sharePublishDate = intent.getStringExtra("sharePublishDate");
		delFileIds = intent.getStringArrayListExtra("deleteFileIds");
	}

	/**
	 * 删除回收的文件，然后加载数据
	 */
	private void extracted()
	{
		// 离线delFileIds=null
		if (delFileIds != null && !delFileIds.isEmpty())
		{
			if (delFileIds.size() > 1)
				showLoading(this);
			handlerThread = new HandlerThread("del.revoke.file");
			handlerThread.start();
			final Handler handler = new Handler(handlerThread.getLooper(), new Handler.Callback()
			{
				@Override
				public boolean handleMessage(Message msg)
				{
					EventBus.getDefault().post(new RefreshShareInfoEvent());
					hideLoading();
					loadWorking();
					return true;
				}
			});
			handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					deleteRevokeFile(delFileIds);
					handler.sendEmptyMessage(0);
				}
			});
		}
		else
		{
			SZLog.v(TAG, "delIds is null or empty.");
			loadWorking();
		}
	}

	private void deleteRevokeFile(List<String> delFileIds)
	{
		//通过AlbumContent构造出一个FileData.
		SZLog.e(TAG, "del: " + delFileIds.toString());
		AlbumContentDAOImpl daoImpl = AlbumContentDAOImpl.getInstance();
		for (Iterator<String> iterator = delFileIds.iterator(); iterator.hasNext();)
		{
			AlbumContent ac = daoImpl.findAlbumContentByContentId(iterator.next());
			if (ac == null)
				continue;
			tempDatas.add(getFileDataByAc(ac));
			DeleteFileUtil.deleteFile((String) SPUtil.get(Fields.FIELDS_ID, ""), ac.getMyProId(),
					ac.getContent_id()); //删除本地文件
		}
	}

	/**
	 * 设置被收回的文件模型
	 * <p>
	 * file_id设置成“-1”
	 * 
	 * @param ac
	 * @return
	 */
	private FileData getFileDataByAc(AlbumContent ac)
	{
		FileData data = new FileData();
		data.setFiles_id("-1");
		data.setName(ac.getName());
		return data;
	}

	private void initView()
	{
		emptyView = findViewById(R.id.empty_include);
		mListView = (SwipeMenuListView) findViewById(R.id.files_listview);
		mOfflineListView = (SwipeMenuListView) findViewById(R.id.files_offline_listview);
		findViewById(R.id.back_img).setOnClickListener(this);
		downloadAllBtn = (Button) findViewById(R.id.downloadfile_all_btn);
		downloadAllBtn.setSelected(true);
		downloadAllBtn.setText(getString(R.string.downloaditem_all));
		downloadAllBtn.setOnClickListener(this);
	}

	private void loadWorking()
	{
		ViewUtil.gone(emptyView);
		if (CommonUtil.isNetConnect(this))
		{
			//在线
			ViewUtil.visible(mListView);
			ViewUtil.gone(mOfflineListView);
			SwipUtil.initSwipItem(mListView, netFileItemClickListener);
			hasOnlineListener();
			loadData();
		}
		else
		{
			//离线
			ViewUtil.gone(mListView);
			ViewUtil.visible(mOfflineListView);
			SwipUtil.initSwipItem(mOfflineListView, offFileItemClickListener);
			hasOfflineListener();
			loadOfflineData();
		}
	}

	private void hasOnlineListener()
	{
		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (adapter == null)
					return;
				FileData data = adapter.getItem(position);
				if (data == null)
					return;
				if ("-1".equals(data.getFiles_id())) //被收回的文件id设置-1
				{
					showToast(getString(R.string.downloaditem_lose_efficacy));
					return;
				}
				int taskState = data.getTaskState();
				SZLog.w(TAG, "taskState: " + taskState);
				if (taskState == DownloadTaskManager2.CHECKING)  		//正在验证
					return;
				if (taskState == DownloadTaskManager2.WAITING_CHECKING)	//等待验证
					return;
				if (taskState == DownloadTaskManager2.CONNECTING)		//正在连接
					return;
				if (taskState == DownloadTaskManager2.PARSERING)		//解析文件
					return;

				AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(
						data.getFiles_id());
				if (ac == null)
				{
					downloadFile(data);
					return;
				}
				adapter.setFileId(ac.getContent_id());
				adapter.notifyDataSetChanged();
				openPageUI(ac.getContent_id(), ac.getFileType());
			}
		});
	}

	private void hasOfflineListener()
	{
		mOfflineListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (offlineAdapter == null)
					return;
				AlbumContent ac = offlineAdapter.getItem(position);
				if (ac == null)
					return;

				offlineAdapter.setCurFileId(ac.getContent_id());
				offlineAdapter.notifyDataSetChanged();
				openPageUI(ac.getContent_id(), ac.getFileType());
			}
		});
	}

	// 离线数据
	private void loadOfflineData()
	{
		showLoading(this);
		ExecutorManager.getInstance().execute(new LoadLocalContentThread(this, shareFolderId));
	}

	private static class LoadLocalContentThread implements Runnable
	{
		private WeakReference<ListFilesActivity> reference;
		private String myProId;

		public LoadLocalContentThread(ListFilesActivity activity, String myProId)
		{
			reference = new WeakReference<ListFilesActivity>(activity);
			this.myProId = myProId;
		}

		@Override
		public void run()
		{
			if (reference == null)
				return;
			ListFilesActivity activity = reference.get();
			if (activity == null)
				return;
			List<AlbumContent> aContents = AlbumContentDAOImpl.getInstance()
					.findAlbumContentByMyProId(myProId);
			for (AlbumContent albumContent : aContents)
			{
				activity.separateFileByType(albumContent);
			}
			Message msg = Message.obtain();
			msg.what = MSG_LOAD_LOCALDATA;
			Bundle data = new Bundle();
			data.putSerializable("local_albumContents", (Serializable) aContents);
			msg.setData(data);
			activity.handler.sendMessageDelayed(msg, 300);
		}
	}

	private void registerBroadcast()
	{
		IntentFilter downloadFilter = new IntentFilter();
		downloadFilter.addAction(DownloadService2.ACTION_UPDATE);
		downloadFilter.addAction(DownloadService2.ACTION_DOWNLOAD_ERROR);
		downloadFilter.addAction(DownloadService2.ACTION_CONNECT_ERROR);
		downloadFilter.addAction(DownloadService2.ACTION_CONNECTING);
		downloadFilter.addAction(DownloadService2.ACTION_PARSERING);
		downloadFilter.addAction(DownloadService2.ACTION_FINISHED);
		registerReceiver(receiver2, downloadFilter);
	}

	private void loadData()
	{
		if (isLoading)
			return;

		isLoading = true;
		showLoading(this);
		Bundle bundle = new Bundle();
		bundle.putString("shareFolderId", shareFolderId);
		GlobalHttp.post(APIUtil.getShareFileUrl(), bundle, new Callback.CommonCallback<String>()
		{
			@Override
			public void onCancelled(CancelledException arg0)
			{
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				ViewHelp.setEmptyViews(mListView, emptyView, getString(R.string.load_server_failed));
				hideLoading();
			}

			@Override
			public void onFinished()
			{
				isLoading = false;
			}

			@Override
			public void onSuccess(String arg0)
			{
				SZLog.d(TAG, "getSuccess: " + arg0);
				parserData(arg0);
			}
		});
	}

	protected void parserData(String result)
	{
		FilesDataModel model = JSON.parseObject(result, FilesDataModel.class);
		if (model != null && model.isSuccess())
		{
			List<FileData> data = model.getData();
			totalItemCount = data.size();

			if (!tempDatas.isEmpty()) //添加收回的显示
				data.addAll(tempDatas);

			adapter = new FileList_Adapter(this, data);
			mListView.setAdapter(adapter);

			adapter.setListViewDate(mListView, sharePublishDate);
			setDownloadAllButton(data);
		}
		else
		{
			ViewHelp.setEmptyViews(mListView, emptyView, getString(R.string.load_data_empty));
		}
		hideLoading();
	}

	/**
	 * 加载数据成功，根据是否存在已下载数据，查询文件，显示全部按钮与否
	 * 
	 * @param datas
	 */
	private void setDownloadAllButton(List<FileData> datas)
	{
		int sum = 0, size = datas.size();
		for (int i = 0; i < size; i++)
		{
			FileData data = datas.get(i);
			AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(
					data.getFiles_id());
			if (ac == null)
				continue;
			sum++;
			sDownloadSet.add(data.getFiles_id());
			separateFileByType(ac);
		}
		downloadAllBtn.setVisibility((sum == totalItemCount || totalItemCount == 1) ? View.GONE
				: View.VISIBLE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		stopDownloadFile();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		isUIStart = true;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		isUIStart = false;
	}

	@Override
	public void onBackPressed()
	{
		backActy();
	}

	private void backActy()
	{
		setResult(Activity.RESULT_CANCELED, null);
		finish();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(receiver2);
		ExecutorManager.shutdownNow();
		EventBus.getDefault().unregister(this);
		cancelChecking();
		stopDownloadFile();
		release();
	}

	/**
	 * 停止下载和验证
	 */
	private void stopDownloadFile()
	{
		cancelBindHttps();
		cancelReceiveHttps();
		DownloadTaskUtil.stopDownloadFile(getApplicationContext());
		sTaskIdSet.clear();
	}

	private void cancelChecking()
	{
		if (sChecks == null || sChecks.isEmpty())
			return;
		Iterator<String> itor = sChecks.keySet().iterator();
		while(itor.hasNext())
		{
			DownloadCheckRunnable check = sChecks.get(itor.next());
			if (check != null && !check.isCancel)
			{
				SZLog.i(TAG, "cancel checking");
				check.isCancel = true;
			}
		}
		sChecks.clear();
	}

	private void cancelBindHttps()
	{
		if (sBindHttps == null || sBindHttps.isEmpty())
			return;
		Iterator<String> itor = sBindHttps.keySet().iterator();
		while(itor.hasNext())
		{
			SZLog.i(TAG, "cancel bind device");
			GlobalHttp.cancelHttp(sBindHttps.get(itor.next()));
		}
		sBindHttps.clear();
	}

	private void cancelReceiveHttps()
	{
		if (sReceiveHttps == null || sReceiveHttps.isEmpty())
			return;
		Iterator<String> itor = sReceiveHttps.keySet().iterator();
		while(itor.hasNext())
		{
			SZLog.i(TAG, "cancel receive share");
			GlobalHttp.cancelHttp(sReceiveHttps.get(itor.next()));
		}
		sReceiveHttps.clear();
	}

	@SuppressLint("NewApi")
	private void release()
	{
		if (sTaskIdSet != null)
			sTaskIdSet.clear();
		sTaskIdSet = null;
		if (sDownloadSet != null)
			sDownloadSet.clear();
		sDownloadSet = null;
		if (!tempDatas.isEmpty())
			tempDatas.clear();
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1 && handlerThread != null)
		{
			handlerThread.quitSafely();
			handlerThread = null;
		}
		sChecks = null;
		sBindHttps = null;
		sReceiveHttps = null;
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.back_img)
		{
			backActy();
		}
		else if (id == R.id.downloadfile_all_btn)
		{
			all();
		}
	}

	private void all()
	{
		if (CommonUtil.isFastDoubleClick(2000))
			return;

		if (isAllDownloading())
		{
			cancelChecking();
			stopDownloadFile();
			setDownloadAllButtonShow(R.string.downloaditem_all, false);
			return;
		}

		if (!sTaskIdSet.isEmpty())
		{
			ToastShow.getToast().showBusy(getApplicationContext(),
					getString(R.string.please_pause_n_download, sTaskIdSet.size()));
			return;
		}
		if (adapter == null)
			return;
		final List<FileData> targets = adapter.getList();
		if (targets == null || targets.isEmpty())
			return;

		// 如果下载不是wifi,则提示用户。
		if (CommonUtil.isWifi(this))
		{
			downloadFileAll(targets);
		}
		else
		{
			UIHelper.showCommonDialog(this, "", getString(R.string.download_tips),
					getString(R.string.download_ask_ok), new DialogCallBack()
					{
						@Override
						public void onConfirm()
						{
							downloadFileAll(targets);
						}
					});
		}
	}

	/**
	 * 设置全部下载按钮的显示
	 * 
	 * @param resId
	 * @param downloading
	 */
	private void setDownloadAllButtonShow(int resId, boolean downloading)
	{
		isAllDownloading = downloading;
		downloadAllBtn.setText(getString(resId));
		downloadAllBtn.setSelected(!isAllDownloading);
		downloadAllBtn.setTextColor(getResources().getColor(
				isAllDownloading ? R.color.gray : R.color.title_top_color));
	}

	private boolean isAllDownloading()
	{
		return isAllDownloading;
	}

	/**
	 * 全部下载
	 */
	private void downloadFileAll(List<FileData> targets)
	{
		final int size = targets.size();
		for (int i = 0; i < size; i++)
		{
			FileData fileData = targets.get(i);
			fileData.setPosition(i);
			AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(
					fileData.getFiles_id());
			if (ac != null)
				continue; // 存在已下载的文件

			//等待验证
			fileData.setTaskState(DownloadTaskManager2.WAITING_CHECKING);
			EventBus.getDefault().post(new DownloadFileCheckingEvent(fileData));
			//正在验证
			checking(fileData);
		}
		setDownloadAllButtonShow(R.string.downloaditem_cancel, true);
	}

	/**
	 * 正在验证
	 * 
	 * @param data
	 */
	private void checking(FileData data)
	{
		DownloadCheckRunnable runnable = new DownloadCheckRunnable(this, data);
		$$.execute(runnable);
		sChecks.put(data.getFiles_id(), runnable);
	}

	/**
	 * TODO: 验证下载
	 */
	private static class DownloadCheckRunnable implements Runnable
	{
		private WeakReference<Activity> r;
		private FileData data;
		private DownloadFileCheckingEvent defaultEvent;
		public boolean isCancel = false;

		public DownloadCheckRunnable(ListFilesActivity activity, FileData data)
		{
			r = new WeakReference<Activity>(activity);
			this.data = data;
			if (defaultEvent == null)
				defaultEvent = new DownloadFileCheckingEvent();
		}

		@Override
		public void run()
		{
			if (r.get() == null)
				return;
			ListFilesActivity a = (ListFilesActivity) r.get();

			data.setTaskState(isCancel ? DownloadTaskManager2.INIT : DownloadTaskManager2.CHECKING);
			defaultEvent.setData(data);
			EventBus.getDefault().post(defaultEvent);

			if (isCancel)
				return;
			CheckResultBean c = a.checkDownload(data);
			Message msg = Message.obtain(a.handler, MSG_DOWNLOAD_FILE, c);
			Bundle bundle = new Bundle();
			bundle.putParcelable("FileData", data);
			msg.setData(bundle);
			msg.getTarget().sendMessage(msg);
		}
	}

	/**
	 * 下载文件 TODO
	 * 
	 * @param o
	 */
	private void downloadFile(FileData o)
	{
		if (!CommonUtil.isNetConnect(getApplicationContext()))
		{
			showToast(getString(R.string.network_not_available));
			return;
		}
		String fileId = o.getFiles_id();
		if (sTaskIdSet.contains(fileId))
		{
			DownloadTaskUtil.stopDownloadFile(getApplicationContext(), fileId);
			o.setTaskState(DownloadTaskManager2.PAUSE);
			adapter.updateItemView(this, o);
			sTaskIdSet.remove(fileId);
			return;
		}
		//开始验证
		checking(o);
	}

	/**
	 * 开始下载了
	 * 
	 * @param o
	 */
	private void addDownloadTask(FileData o)
	{
		if (StringUtil.isEmptyOrNull(o.getFtpUrl()))
		{
			showToast(getString(R.string.downloaditem_exception));
			return;
		}

		sTaskIdSet.add(o.getFiles_id());
		o.setTaskState(DownloadTaskManager2.WAITING);
		adapter.updateItemView(this, o);
		DownloadTaskUtil.startDownloadFile(this, o);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode)
		{
			case CODE_VERIFY:
			case CODE_LOGIN_AGAIN:
			{
				if (data != null && data.getBooleanExtra("opt_flag", false))
				{
					//登录成功，刷新一下数据，主要是分享详情的头部信息DataBean
					EventBus.getDefault().post(new RefreshDeviceNumEvent());
				}
			}
				break;
			default:
				break;
		}
	}

	private void openPageUI(String contentId, String fileType)
	{
		switch (fileType)
		{
			case Fields.PDF:
			{
				SZLog.w(TAG, "PDF: " + mPdfFiles.size());
				OpenPageUtil.openPDFReader(this, contentId, mPdfFiles);
			}
				break;
			case Fields.MP4:
			{
				// 关闭音乐播放器广播
				SZLog.w(TAG, "MP4: " + mVideoFiles.size());
				EventBus.getDefault().post(new MusicCircleEvent(false));
				OpenPageUtil.stopMediaService(this);
				OpenPageUtil.openVideoPlayer(this, contentId, mVideoFiles);
			}
				break;
			case Fields.MP3:
			{
				//同一个文件，则不需要初始化。
				SZLog.w(TAG, "MP3: " + mMusicFiles.size());
				boolean isInit = !(TextUtils.equals(K.CURRENT_MUSIC_ID, contentId));
				SZLog.v("isInit: " + isInit, K.CURRENT_MUSIC_ID + "");
				OpenPageUtil.openMusicPlayer(this, shareFolderName, contentId, mMusicFiles, isInit);
			}
				break;
			default:
				showToast("文件异常，请重新下载");
				break;
		}
	}

	/**
	 * 校验下载,同步请求，非UI线程访问
	 * 
	 * @param o
	 *            {@code FileData}
	 */
	private CheckResultBean checkDownload(FileData o)
	{
		Bundle bundle = new Bundle();
		bundle.putString("fileId", o.getFiles_id());
		bundle.putString("shareFolderId", o.getSharefolder_id());
		bundle.putString("shareId", (String) SPUtil.get(Fields.FIELDS_ID, ""));
		bundle.putString("myToken", (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""));
		bundle.putString("receiveID", (String) SPUtil.get(Fields.FIELDS_RECEIVE_ID, ""));
		bundle.putString("deviceIdentifier", Constant.TOKEN);
		bundle.putString("deviceType", K.platform);
		String result = GlobalHttp.getSyncString(APIUtil.getDownloadCheckUrl(), bundle);
		if (result != null)
		{
			SZLog.d(TAG, "check success: " + result);
			CheckResultBean resultBean = JSON.parseObject(result, CheckResultBean.class);
			if (Code._SUCCESS.equals(resultBean.getCode()))
			{
				//已经登录成功，（后台已领取成功），发送消息保存记录
				//已经绑定成功过
				SendMsgShareUtil.sendMsg2SaveRecord(shareTheme, shareOwner);
			}
			else
			{
				//需要接收或绑定设备的分享，则取消其他的列表文件校验，置成初始状态
				cancelChecking();
				o.setTaskState(DownloadTaskManager2.INIT);
				EventBus.getDefault().post(new DownloadFileCheckingEvent(o));
			}
			return resultBean;
		}
		return null;
	}

	/**
	 * 校验绑定
	 * 
	 * @param atc
	 * @param oo
	 */
	private void checkBindDevice(final PbbBaseActivity atc, final FileData oo)
	{
		if (!isUIStart)
			return;
		UIHelper.showCommonDialog(atc, oo.getName(), atc.getString(R.string.ask_bind_device),
				atc.getString(R.string.bind), new DialogCallBack()
				{
					@Override
					public void onConfirm()
					{
						bindDevice(atc, oo);
					}
				});
	}

	private void bindDevice(final PbbBaseActivity atc, final FileData oo)
	{
		atc.showBgLoading(atc, atc.getString(R.string.binding));
		Bundle bundle = new Bundle();
		bundle.putString("fileId", oo.getFiles_id());
		bundle.putString("shareFolderId", oo.getSharefolder_id());
		bundle.putString("deviceIdentifier", Constant.TOKEN);
		bundle.putString("shareId", (String) SPUtil.get(Fields.FIELDS_ID, ""));
		bundle.putString("myToken", (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""));
		bundle.putString("receiveID", (String) SPUtil.get(Fields.FIELDS_RECEIVE_ID, ""));//绑定按身份创建的分享必传，绑定按设备创建的分享不必传
		bundle.putString("registrationid", (String) SPUtil.get(Fields.FIELDS_JPUSH_REGISTERID, ""));
		Cancelable mBindDeviceHttp = GlobalHttp.getOn(APIUtil.bindDevicesUrl(), bundle,
				new Callback.CommonCallback<String>()
				{
					@Override
					public void onCancelled(CancelledException arg0)
					{
					}

					@Override
					public void onError(Throwable arg0, boolean arg1)
					{
						UIHelper.showToast(atc, atc.getString(R.string.bind_device_fail));
						atc.hideBgLoading();
					}

					@Override
					public void onFinished()
					{
					}

					@Override
					public void onSuccess(String arg0)
					{
						SZLog.d(TAG, "bind:" + arg0);
						CheckResultBean c = JSON.parseObject(arg0, CheckResultBean.class);
						judgeCode(atc, c, oo);
						if (Code._SUCCESS.equals(c.getCode()))
						{
							//绑定成功，刷新绑定数
							EventBus.getDefault().post(new RefreshDeviceNumEvent());
							//按设备分享绑定成功，发送通知保存记录
							SendMsgShareUtil.sendMsg2SaveRecordByDevice(shareTheme, shareOwner);
						}
					}
				});
		sBindHttps.put(oo.getFiles_id(), mBindDeviceHttp);
	}

	/**
	 * 登录校验（按身份和人数分享时）
	 * 
	 * @param atc
	 */
	private void checkLogin(final PbbBaseActivity atc)
	{
		atc.hideBgLoading();
		if (ShareMode.SHARECOUNT.equals(ShareMode.Mode.value))
		{
			//按人数分享，未登录就直接去登录
			Intent intent = new Intent(atc, LoginVerifyCodeActivity.class);
			atc.startActivityForResult(intent, CODE_VERIFY);
			return;
		}
		Intent intent = new Intent(atc, CheckLoginActivity.class);
		atc.startActivityForResult(intent, CODE_VERIFY);
	}

	/**
	 * 登录失效，重新登录
	 * 
	 * @param atc
	 * @param msg
	 */
	private void loginAgain(PbbBaseActivity atc, String msg)
	{
		atc.hideBgLoading();
		Intent intent = new Intent(atc, LoginVerifyCodeActivity.class);
		intent.putExtra("phone_number", (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, ""));
		atc.startActivityForResult(intent, CODE_LOGIN_AGAIN);
		UIHelper.showToast(atc.getApplicationContext(), msg);
	}

	/**
	 * 领取分享 (按身份分享时) <br/>
	 * 1.未登录：验证手机号时领取 <br/>
	 * 2.已登录：校验下载中已领取<br/>
	 * 3.按身份分享指定其他手机号，先登录，后领取<br/>
	 * 
	 * @param atc
	 * @param o
	 */
	private void receiveShared(final PbbBaseActivity atc, final FileData o)
	{
		atc.showBgLoading(atc, atc.getString(R.string.receiving));
		String phone = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
		Cancelable mReceiveSharedHttp = Util_.receiveVerifyShared(phone,
				new Callback.CommonCallback<String>()
				{
					@Override
					public void onCancelled(CancelledException arg0)
					{
					}

					@Override
					public void onError(Throwable arg0, boolean arg1)
					{
						UIHelper.showToast(atc, atc.getString(R.string.receive_share_fail));
					}

					@Override
					public void onFinished()
					{
						atc.hideBgLoading();
					}

					@Override
					public void onSuccess(String arg0)
					{
						SZLog.d(TAG, "receive:" + arg0);
						CheckResultBean c = JSON.parseObject(arg0, CheckResultBean.class);
						judgeCode(atc, c, o);
						if (Code._SUCCESS.equals(c.getCode()))
						{
							//领取分享成功，提示绑定设备
							checkBindDevice(atc, o);
							SendMsgShareUtil.sendMsg2SaveRecordByUser(shareTheme, shareOwner);
							UIHelper.showToast(atc.getApplicationContext(),
									atc.getString(R.string.receive_share_success));
						}
					}
				});
		sReceiveHttps.put(o.getFiles_id(), mReceiveSharedHttp);
	}

	////////////////////////////////////////////////////
	//////////////////状态码Code.java/////////////////////
	////////////////////////////////////////////////////
	/**
	 * 状态码。 {@link Code}
	 * 
	 * @param atc
	 * @param c
	 *            返回数据模型
	 * @param o
	 *            下载信息模型
	 */
	private void judgeCode(PbbBaseActivity atc, CheckResultBean c, FileData o)
	{
		switch (c.getCode())
		{
			case Code._SUCCESS:
			{
				o.setFtpUrl(c.getFtpUrl());
				addDownloadTask(o);
			}
				break;
			case Code._8001:					//已登录，请先领取分享,接收分享操作
				receiveShared(atc, o);
				break;
			case Code._8002:					//请先绑定分享设备
				checkBindDevice(atc, o);
				break;
			case Code._8006:					//登录失效，请重新登录
				loginAgain(atc, c.getMsg());
				break;
			case Code._9007:					//您尚未登录
				checkLogin(atc);
				break;
			case Code._8007:					//正在打包
			case Code._9002:					//分享不存在
			case Code._9003:
			case Code._9004:
			case Code._9005:
			case Code._9006:
			case Code._9008:
			case Code._9010:
			case Code._9011:
			case Code._9012:
			case Code._9013:
			case Code._9014:
			case Code._9016:
			case Code._9017:
			case Code._9022:
			{
				atc.hideBgLoading();
				if (isUIStart)
					UIHelper.showSingleCommonDialog(atc, o.getName(), c.getMsg(),
							atc.getString(R.string.i_know), null);
				setDownloadAllButtonShow(R.string.downloaditem_all, false);
			}
				break;
			case Code._9001:
			case Code._9009:
			case Code._9015:
			case Code._9018:
			case Code._9019:
			case Code._9020:
			case Code._9021:
			{
				atc.hideBgLoading();
				ToastShow.getToast().showFail(atc, c.getMsg());
				setDownloadAllButtonShow(R.string.downloaditem_all, false);
			}
				break;
			default:
				UIHelper.showToast(atc.getApplicationContext(), "服务器数据校验错误");
				break;
		}
	}

	OnMenuItemClickListener netFileItemClickListener = new OnMenuItemClickListener()
	{
		@Override
		public void onMenuItemClick(final int position, SwipeMenu menu, int index)
		{
			if (adapter == null)
				return;
			if (index != 0)
				return;
			FileData data = adapter.getItem(position);
			if (data == null)
				return;
			AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(
					data.getFiles_id());
			if (ac == null) //无下载数据
				return;
			if (data.getTaskState() == DownloadTaskManager2.DOWNLOADING)
			{
				showToast(getString(R.string.downloading_please));
				return;
			}
			if (data.getTaskState() == DownloadTaskManager2.PARSERING)
			{
				showToast(getString(R.string.parsering_please));
				return;
			}
			removeItemData(data.getSharefolder_id(), data.getFiles_id(), -1);
		}
	};

	OnMenuItemClickListener offFileItemClickListener = new OnMenuItemClickListener()
	{
		@Override
		public void onMenuItemClick(int position, SwipeMenu menu, int index)
		{
			if (offlineAdapter == null)
				return;
			if (index != 0)
				return;
			AlbumContent ac = offlineAdapter.getItem(position);
			if (ac == null)
				return;
			removeItemData(ac.getMyProId(), ac.getContent_id(), position);
		}
	};

	protected void removeItemData(String folderId, String fileId, int position)
	{
		showBgLoading(this, getString(R.string.now_delete_item));
		if (K.playState != IMusicConst.OPTION_STOP)
			OpenPageUtil.stopMediaService(this);
		handler.removeMessages(MSG_CLEAR_ITEMDATA);
		ExecutorManager.getInstance().execute(
				new ClearItemDataThread(this, folderId, fileId, position));
	}

	//清除本地资源文件
	private static class ClearItemDataThread implements Runnable
	{
		private WeakReference<ListFilesActivity> reference;
		private String folderId;
		private String fileId;
		private int position;

		public ClearItemDataThread(ListFilesActivity activity, String folderId, String fileId,
				int position)
		{
			reference = new WeakReference<ListFilesActivity>(activity);
			this.folderId = folderId;
			this.fileId = fileId;
			this.position = position;
		}

		@Override
		public void run()
		{
			if (reference.get() == null)
				return;
			ListFilesActivity activity = reference.get();
			DeleteFileUtil.deleteFile((String) SPUtil.get(Fields.FIELDS_ID, ""), folderId, fileId);
			activity.deleteSZFile4List(fileId);
			DownData2DBManager.Builder().deleteByFileId(fileId);
			Message message = Message.obtain();
			message.what = MSG_CLEAR_ITEMDATA;
			message.obj = position;
			activity.handler.sendMessageDelayed(message, 400);
		}
	}

	/**
	 * 删除下载后添加到ArrayList中的SZFile文件
	 * 
	 * @param fileId
	 */
	private void deleteSZFile4List(String fileId)
	{
		ArrayList<SZFile> temps = null;
		//移除视频信息
		if (!mVideoFiles.isEmpty())
		{
			temps = new ArrayList<>(mVideoFiles);
			for (SZFile e : temps)
			{
				if (e.getContentId().equals(fileId))
				{
					mVideoFiles.remove(e);
				}
			}
		}
		//移除图书信息
		if (!mPdfFiles.isEmpty())
		{
			temps = new ArrayList<>(mPdfFiles);
			for (SZFile e : temps)
			{
				if (e.getContentId().equals(fileId))
				{
					mPdfFiles.remove(e);
				}
			}
		}
		//移除音乐信息
		if (!mMusicFiles.isEmpty())
		{
			temps = new ArrayList<>(mMusicFiles);
			for (SZFile e : temps)
			{
				if (e.getContentId().equals(fileId))
				{
					mMusicFiles.remove(e);
				}
			}
		}
	}

	/**
	 * 文件校验，更新ui
	 * 
	 * @param e
	 */
	public void onEventMainThread(DownloadFileCheckingEvent e)
	{
		FileData data = e.getData();
		if (data == null)
			return;
		int taskState = data.getTaskState();
		adapter.updateItemView(this, data);
		adapter.setItemDataState(data.getPosition(), taskState);
		if (taskState == DownloadTaskManager2.INIT)
		{
			setDownloadAllButtonShow(R.string.downloaditem_all, false);
		}
	}

}
