package cn.com.pyc.pbbonline;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.SecurityUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.mobilesdk.util.UIHelper.DialogCallBack;
import com.sz.view.dialog.LoadingBgDialog;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.adapter.ShareRecordAdapter;
import cn.com.pyc.pbbonline.bean.LoginBean;
import cn.com.pyc.pbbonline.bean.event.IsEditRecordModeEvent;
import cn.com.pyc.pbbonline.bean.event.LoginSuccessRefeshRecordEvent;
import cn.com.pyc.pbbonline.bean.event.RefreshShareRecordEvent;
import cn.com.pyc.pbbonline.bean.event.ShareRecordShowBgLoading;
import cn.com.pyc.pbbonline.common.IMusicConst;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.db.Shared;
import cn.com.pyc.pbbonline.db.SharedDBManager;
import cn.com.pyc.pbbonline.db.manager.ClickShareDBManger;
import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.pbbonline.model.SharesReceiveBean;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.SeparatedUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.pbbonline.widget.AppMsg;
import cn.com.pyc.pbbonline.widget.AppMsg.Style;
import cn.com.pyc.pbbonline.widget.PullListView;
import cn.com.pyc.pbbonline.widget.SmoothCheckBox;
import de.greenrobot.event.EventBus;

/**
 * 分享历史记录
 */
public class ShareRecordListActivity extends PbbBaseActivity
{
	private static final String TAG = "SharedRecordList";
	private static final int MSG_OPEN_INIT = 0x44;
	private static final int MSG_SINGLE_DELETE = 0x46;

	private boolean isEmpty = false;
	private boolean isLoading = false;
	private PullListView mListView;
	private View emptyView;
	private SharedDBManager dbManager;
	private ShareRecordAdapter adapter;
	private AsyncTask<Void, Void, List<Shared>> mHandleDataTask;
	private AsyncTask<Void, Integer, List<Shared>> mDeleteAllTask;
	private boolean isDelAlltaskRunning = false;
	private Cancelable mGetShareHttp;
	private AppMsg appMsg;
	private boolean isStop = false;
	private Handler handler = new MyHandler(this);

	//private LoadingBgDialog copyLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pbbonline_activity_sharerecord_list);
		mListView = (PullListView) findViewById(R.id.record_listview);
		emptyView = findViewById(R.id.empty_include);
		EventBus.getDefault().register(this);
		dbManager = new SharedDBManager();

		setListeners();
		getMyListeners();
		initUIShow(initData());	//加载本地
		refeshData(true);	//检查数据更新
	}

	/**
	 * 查询本地记录数据库
	 */
	private List<Shared> initData()
	{
		List<Shared> records = new ArrayList<Shared>();
		if (dbManager == null)
			return records;
		//无论是否登录，都要查询按设备分享方式的记录。
		records = dbManager.findByDevice();
		SZLog.d(TAG, "findByDevice：" + records.size());

		//查询登陆下的记录(1.存在登录账号的，2.二次分享账号可能为“”的)
		if (Util_.isLogin())
		{
			String account = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
			records.addAll(dbManager.findByAccount(account));
		}
		SZLog.d(TAG, "device+Account：" + records.size());
		Collections.sort(records, new Comparator<Shared>()
		{
			@Override
			public int compare(Shared lhs, Shared rhs)
			{
				//按时间排序
				return String.valueOf(rhs.getTime()).compareTo(String.valueOf(lhs.getTime()));
			}
		});
		return records;
	}

	/**
	 * 页面展示数据
	 * 
	 * @param records
	 */
	private void initUIShow(List<Shared> records)
	{
		if (records.isEmpty())
		{
			isEmpty = true;
			if (adapter != null && adapter.getRecords() != null)
			{	//删除所有数据，清空数据引用
				adapter.getRecords().clear();
				adapter.notifyDataSetChanged();
			}
			ViewHelp.setEmptyViews(mListView, emptyView, "暂无分享记录");
			hideBgLoading();
			return;
		}
		if (adapter == null)
		{
			adapter = new ShareRecordAdapter(this, records);
			mListView.setAdapter(adapter);
		}
		else
		{
			adapter.setRecords(records);
			adapter.notifyDataSetChanged();
		}
		isEmpty = false;
		hideBgLoading();
	}

	private void setListeners()
	{
		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (mListView.getState() == PullListView.REFRESHING)
					return;

				Object obj = mListView.getItemAtPosition(position);
				if (obj != null && obj instanceof Shared)
				{
					Shared record = (Shared) obj;
					openSharedDetail(record);
				}
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (mListView.getState() == PullListView.REFRESHING)
					return true;

				Object obj = mListView.getItemAtPosition(position);
				if (obj != null && obj instanceof Shared)
				{
					final Shared record = (Shared) obj;
					clearShared(record);
				}
				return true;
			}
		});

		mListView.setOnRefreshListener(new PullListView.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				refeshData(false);
				cancelAppMsg();
			}
		});

		mListView.setOnScrollListener(new AbsListView.OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
				// 滚动到最底部，隐藏底部的编辑栏，防止遮挡item显示
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE)
				{
					IndexPageHomeActivity atcy = (IndexPageHomeActivity) getParent();
					if (atcy == null)
						return;
					if (!atcy.isEditMode())
						return;
					if (mListView.getLastVisiblePosition() == (mListView.getCount() - 1))
					{
						atcy.hideBottom();
					}
					else
					{
						atcy.showBottom();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount)
			{
			}
		});
	}

	private void clearShared(final Shared record)
	{
		String content = getString(R.string.delete_share);
		UIHelper.showCommonDialog(this, record.getTheme(), content, getString(R.string.delete),
				new DialogCallBack()
				{
					@Override
					public void onConfirm()
					{
						showBgLoading(ShareRecordListActivity.this,
								getString(R.string.now_delete_item));
						ExecutorManager.getInstance().execute(
								new SingleDelThread(ShareRecordListActivity.this, record));
					}
				});
	}

	/**
	 * 先登录获取最新登录token,再加载数据
	 * 
	 * @param checkData
	 *            检查数据
	 */
	private void refeshData(final boolean checkData)
	{
		// 隐藏空空提示框
		if (emptyView.getVisibility() == View.VISIBLE)
			emptyView.setVisibility(View.GONE);
		// 用户未联网或未登陆,只显示本地数据。
		if (!CommonUtil.isNetConnect(this) || !Util_.isLogin() || isLoading)
		{
			handler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					initUIShow(initData());
					mListView.refreshComplete();
				}
			}, 1500);
			return;
		}
		isLoading = true;
		String password = (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, "");
		Bundle bundle = new Bundle();
		bundle.putString("username", (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, ""));
		bundle.putString("password", SecurityUtil.encryptBASE64(password));
		bundle.putString("mytoken", (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, "")); 	//无密码就校验mytoken
		bundle.putString("deviceIdentifier", Constant.TOKEN);
		bundle.putString("registrationid", (String) SPUtil.get(Fields.FIELDS_JPUSH_REGISTERID, ""));
		bundle.putString("source", (String) SPUtil.get(Fields.FIELDS_WEB_SOURCE, ""));	//网页启动app和扫码
		bundle.putString("shareId", (String) SPUtil.get(Fields.FIELDS_ID, ""));
		if (Util_.isWebBrowser())
		{
			bundle.putString("weixin", (String) SPUtil.get(Fields.FIELDS_WEB_WEIXIN, ""));
		}

		Callback.CommonCallback<String> callback = new Callback.CommonCallback<String>()
		{
			@Override
			public void onSuccess(String arg0)
			{
				SZLog.d(TAG, "login: " + arg0);
				LoginBean o = JSON.parseObject(arg0, LoginBean.class);
				if (o != null && o.isSuccess())
				{
					if ("true".equals(o.getFlag()))
					{
						SPUtil.save(Fields.FIELDS_LOGIN_TOKEN, o.getToken());
						getAllReceiveShares(o.getToken(), checkData);
						return;
					}
				}
				mListView.refreshComplete();
				hideBgLoading();
				if (!checkData)
					UIHelper.showToast(getApplicationContext(), "获取数据失败，请重新登录");
			}

			@Override
			public void onFinished()
			{
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				mListView.refreshComplete();
				hideBgLoading();
				if (!checkData)
					UIHelper.showToast(getApplicationContext(),
							getString(R.string.connect_server_failed));
			}

			@Override
			public void onCancelled(CancelledException arg0)
			{
			}
		};
		GlobalHttp.postOn(APIUtil.getLoginPath(), bundle, callback);
	}

	/**
	 * 获取所有分享
	 * 
	 * @param myToken
	 *            登录返回的token
	 * @param checkData
	 */
	private void getAllReceiveShares(String myToken, final boolean checkData)
	{
		Bundle bundle = new Bundle();
		bundle.putString("myToken", myToken);
		mGetShareHttp = GlobalHttp.get(APIUtil.getAllReceiveSharesUrl(), bundle,
				new Callback.CommonCallback<String>()
				{
					@Override
					public void onCancelled(CancelledException arg0)
					{
					}

					@Override
					public void onError(Throwable arg0, boolean arg1)
					{
						hideBgLoading();
						showToast(getString(R.string.load_server_failed));
					}

					@Override
					public void onFinished()
					{
						isLoading = false;
						mListView.refreshComplete();
					}

					@Override
					public void onSuccess(String arg0)
					{
						SZLog.d("refeshData:" + arg0);
						List<SharesReceiveBean> mSharesReceiveList = JSON.parseArray(arg0,
								SharesReceiveBean.class);
						if (mSharesReceiveList == null || mSharesReceiveList.isEmpty())
						{
							UIHelper.showToast(getApplicationContext(), "暂无数据更新！");
							////initUIShow(initData());
							hideBgLoading();
							return;
						}
						SZLog.e(TAG, "checkData: " + checkData);
						if (checkData) //检查数据是否有变化
						{
							List<Shared> allShareds = dbManager.findAll();
							if (allShareds.size() != mSharesReceiveList.size())
							{
								showAppMsg(getString(R.string.jpush_message_tips),
										appMsgClickListener);
							}
							return;
						}
						handlerRefeshData(mSharesReceiveList);
					}
				});
	}

	private void handlerRefeshData(final List<SharesReceiveBean> receives)
	{
		cancelDataTask();
		mHandleDataTask = new AsyncTask<Void, Void, List<Shared>>()
		{
			@Override
			protected List<Shared> doInBackground(Void... params)
			{
				if (isCancelled())
					return new ArrayList<Shared>();
				// 判断如果有新增数据，就添加到数据库。
				new SeparatedUtil().resolveShareReceive(receives);
				return initData();
			}

			@Override
			protected void onPostExecute(List<Shared> result)
			{
				super.onPostExecute(result);
				initUIShow(result);
			}
		};
		mHandleDataTask.execute();
	}

	private void cancelDataTask()
	{
		if (mHandleDataTask != null && mHandleDataTask.getStatus() == AsyncTask.Status.RUNNING)
		{
			mHandleDataTask.cancel(true);
			mHandleDataTask = null;
		}
	}

	/**
	 * 打开分享文件夹
	 * 
	 * @param record
	 */
	private void openSharedDetail(final Shared record)
	{
		if (record.isRevoke())
		{
			showToast(getString(R.string.shared_lose_efficacy));
			return;
		}
		if (TextUtils.isEmpty(record.getShareUrl()))
		{
			showToast(getString(R.string.shared_link_empty));
			return;
		}
		showBgLoading(this, getString(R.string.please_waiting));
		ExecutorManager.getInstance().execute(new OpenInitThread(this, record));
	}

	/**
	 * 显示msg提示
	 * 
	 * @param msg
	 * @param listener
	 */
	private void showAppMsg(String msg, View.OnClickListener listener)
	{
		if (isStop)
			return;

		if (appMsg == null)
		{
			Style style = new AppMsg.Style(AppMsg.LENGTH_LONG, R.color.confirm);
			appMsg = AppMsg.makeText(this, msg, style);
			appMsg.setLayoutGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
			appMsg.getView().findViewById(android.R.id.message).setOnClickListener(listener);
			appMsg.setAnimation(android.R.anim.fade_in, android.R.anim.fade_out);
		}
		appMsg.show();
	}

	/**
	 * 取消msg
	 */
	private void cancelAppMsg()
	{
		if (appMsg != null)
		{
			appMsg.cancel();
			appMsg = null;
		}
	}

	View.OnClickListener appMsgClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			cancelAppMsg();
			mListView.performRefresh();
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			UIHelper.showExitTips(this);
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
		SZLog.v("record", "onDestroy");
		isSelectBoxState(false);
		cancelAppMsg();
		release();
	}

	private void release()
	{
		if (adapter != null)
			adapter.release();
		EventBus.getDefault().unregister(this);
		GlobalHttp.cancelHttp(mGetShareHttp);
		cancelDelAllTask();
		cancelDataTask();
		handler.removeCallbacksAndMessages(null);
		handler = null;
	}

	private void getMyListeners()
	{
		if (getParent() == null)
			return;

		IndexPageHomeActivity atyGroup = (IndexPageHomeActivity) getParent();
		ImageView deleteImg = atyGroup.getDeleteImg();
		if (deleteImg == null)
			return;

		deleteImg.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				deleteSelectItem();
			}
		});
		SmoothCheckBox allCb = atyGroup.getAllCheckBox();
		if (allCb == null)
			return;

		allCb.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked)
			{
				SZLog.v("", "AllSelected: " + isChecked);
				isSelectBoxState(isChecked);
			}
		});
	}

	/**
	 * 是否选中
	 * 
	 * @param isChecked
	 *            是选中；反之
	 */
	private void isSelectBoxState(boolean isChecked)
	{
		if (adapter == null)
			return;
		if (adapter.getRecords().isEmpty())
			return;
		adapter.selectAll(adapter.getRecords(), isChecked);
	}

	private void deleteSelectItem()
	{
		final SparseArray<Shared> list = ShareRecordAdapter.mSelectObjs;
		if (list == null)
			return;
		int selectSize = list.size();
		SZLog.v("", "deleteSelected:" + selectSize);
		if (isEmpty)
			return;
		if (selectSize == 0)
		{
			showToast(getString(R.string.select_delete_item));
			return;
		}
		UIHelper.showCommonDialog(this, null, getString(R.string.ask_delete_n_item, selectSize),
				null, new UIHelper.DialogCallBack()
				{
					@Override
					public void onConfirm()
					{
						// 删除选中项！！
						deleteing(list);
					}
				});
	}

	/**
	 * 正在刪除ing~
	 * 
	 * @param list
	 */
	protected void deleteing(final SparseArray<Shared> list)
	{
		// 判断是否正在播放音乐
		if (K.playState != IMusicConst.OPTION_STOP)
			OpenPageUtil.stopMediaService(this);
		cancelDelAllTask();
		mDeleteAllTask = new AsyncTask<Void, Integer, List<Shared>>()
		{
			private LoadingBgDialog dialog;

			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				dialog = showBgLoading(ShareRecordListActivity.this,
						getString(R.string.now_delete_item));
				isDelAlltaskRunning = true;
			}

			@Override
			protected void onProgressUpdate(Integer... values)
			{
				if (dialog != null)
				{
					String content = getString(R.string.now_delete_n_item, values[0], list.size());
					dialog.setContentText(content);
				}
			}

			@Override
			protected List<Shared> doInBackground(Void... params)
			{
				if (isCancelled())
					return null;
				int length = list.size();
				for (int i = 0; i < length; i++)
				{
					if (!isDelAlltaskRunning)
						break;

					Shared shared = list.get(list.keyAt(i));
					if (shared != null)
					{
						publishProgress((i + 1));
						SZLog.w(TAG, "delete: " + (i + 1) + "; " + shared.getTheme());
						shared.setDelete(true);		// 分享记录标记为删除
						dbManager.updateDeleteFlag(shared);
						Util_.deleteCommonDataDB(SZInitInterface.getUserName(""), shared.getShareId());
						ClickShareDBManger.Builder().deleteClickByShareId(shared.getShareId());
					}
				}
				return initData();
			}

			@Override
			protected void onPostExecute(List<Shared> result)
			{
				super.onPostExecute(result);
				hideBgLoading();
				isDelAlltaskRunning = false;
				if (result == null)
					return;
				showToast(getString(R.string.delete_success));
				isEditMode(false);
				initUIShow(result);
			}

			@Override
			protected void onCancelled(List<Shared> result)
			{
				super.onCancelled(result);
				hideBgLoading();
				isDelAlltaskRunning = false;
			}
		};
		mDeleteAllTask.execute();
	}

	private void cancelDelAllTask()
	{
		if (mDeleteAllTask != null && mDeleteAllTask.getStatus() == AsyncTask.Status.RUNNING)
		{
			mDeleteAllTask.cancel(true);
			mDeleteAllTask = null;
		}
		isDelAlltaskRunning = false;
	}

	/**
	 * 接收ShareDetailsPage页面的通知，刷新界面数据
	 * 
	 * @param e
	 */
	public void onEventMainThread(RefreshShareRecordEvent e)
	{
		initUIShow(initData());
	}

	/**
	 * 接收container发送的编辑选项通知
	 * 
	 * @param e
	 */
	public void onEventMainThread(IsEditRecordModeEvent e)
	{
		boolean show_ = e.isShow();
		isEditMode(show_);
	}

	/**
	 * 显示加载框（show和hide合并成一个试试）
	 * 
	 * @param e
	 */
	public void onEventMainThread(ShareRecordShowBgLoading e)
	{
		if (e.isShowLoading())
			showBgLoading(this, getString(R.string.please_waiting));
		else
			hideBgLoading();
	}

	private void isEditMode(boolean show_)
	{
		if (adapter == null)
			return;
		adapter.setShow_checkBox(show_);
		adapter.notifyDataSetChanged();
		if (!show_)
		{
			isSelectBoxState(false);
			if (getParent() != null)
			{
				//推出全选状态，全选框复原，false。
				IndexPageHomeActivity atcy = (IndexPageHomeActivity) getParent();
				if (atcy.getAllCheckBox() != null && atcy.getAllCheckBox().isChecked())
				{
					atcy.getAllCheckBox().setChecked(false);
				}
				atcy.setEditMode(false);
				atcy.hideBottom();
			}
		}
	}

	/**
	 * 登陆或退出成功，重新加载数据
	 * 
	 * @param e
	 */
	public void onEventMainThread(LoginSuccessRefeshRecordEvent e)
	{
		if (adapter != null && adapter.getRecords() != null)
		{
			adapter.getRecords().clear();
		}

		initUIShow(initData());
		if (e.isLogin())
		{
			//登录成功，直接通过最新token获取所有分享
			showBgLoading(this, getString(R.string.getdata));
			getAllReceiveShares((String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""), false);
		}
		SZLog.v(TAG, "login or out success,refesh recordlist");
	}

	//打开分享
	private static class OpenInitThread implements Runnable
	{
		private WeakReference<ShareRecordListActivity> reference;
		private Shared shared;

		public OpenInitThread(ShareRecordListActivity activity, Shared shared)
		{
			reference = new WeakReference<ShareRecordListActivity>(activity);
			this.shared = shared;
		}

		@Override
		public void run()
		{
			if (reference == null || reference.get() == null)
				return;
			ShareRecordListActivity activity = reference.get();
			shared.setWhetherNew(false);
			shared.setUpdate(false);
			activity.dbManager.modifyNewSharedState(shared);
			ClickShareDBManger.Builder().saveClick(shared.getShareId(), true);
			Util_.initCommonDataDB(shared.getShareUrl(), false);
			Message message = Message.obtain();
			message.obj = shared.getTheme();
			message.what = MSG_OPEN_INIT;
			activity.handler.sendMessage(message);
		}
	}

	//长按，单个删除分享
	private static class SingleDelThread implements Runnable
	{
		private WeakReference<ShareRecordListActivity> reference;
		private Shared shared;

		public SingleDelThread(ShareRecordListActivity activity, Shared shared)
		{
			reference = new WeakReference<ShareRecordListActivity>(activity);
			this.shared = shared;
		}

		@Override
		public void run()
		{
			if (reference == null || reference.get() == null)
				return;
			ShareRecordListActivity activity = reference.get();
			shared.setDelete(true);		// 分享记录标记为删除
			activity.dbManager.updateDeleteFlag(shared);
			Util_.deleteCommonDataDB(SZInitInterface.getUserName(""), shared.getShareId());
			ClickShareDBManger.Builder().deleteClickByShareId(shared.getShareId());
			activity.handler.sendEmptyMessage(MSG_SINGLE_DELETE);
		}
	}

	private static class MyHandler extends Handler
	{
		private WeakReference<ShareRecordListActivity> reference;

		public MyHandler(ShareRecordListActivity acty)
		{
			reference = new WeakReference<ShareRecordListActivity>(acty);
		}

		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (reference == null || reference.get() == null)
				return;
			ShareRecordListActivity activity = reference.get();
			activity.hideBgLoading();
			switch (msg.what)
			{
				case MSG_OPEN_INIT:
				{
					// 开启分享详情页
					Intent intent = new Intent(activity, ShareDetailsPageActivity.class);
					intent.putExtra("ShareID", (String) SPUtil.get(Fields.FIELDS_ID, ""));
					intent.putExtra("ShareName", (String) msg.obj);
					activity.startActivity(intent);
					activity.adapter.notifyDataSetChanged();
				}
					break;
				case MSG_SINGLE_DELETE:
				{
					EventBus.getDefault().post(new RefreshShareRecordEvent());
					activity.showToast(activity.getString(R.string.delete_success));
				}
					break;
				default:
					break;
			}
		}
	}

	/**
	 * 迁移数据，复制文件：<br/>
	 * sdCard/Android/data/SZOnline--->：sdCard/Android/data/cn.com.pyc.pbb/
	 * SZOnline
	 */
	@Deprecated
	private void startCopyData()
	{
		if (Util_.checkCopyData(this))
		{
			final String targetPath = PathUtil.getSDCard() + "/" + PathUtil.getSZOffset();
			final String srcPath = PathUtil.getSDCard() + "/Android/data/SZOnline";
			final Context _Context = ShareRecordListActivity.this;
			handler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					OpenPageUtil.openIntermediaryPage(_Context, srcPath, targetPath);
				}
			}, 800);
		}
	}

}
