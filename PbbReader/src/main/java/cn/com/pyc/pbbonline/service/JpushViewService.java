package cn.com.pyc.pbbonline.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.SPUtil;

import java.util.List;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.event.RefreshShareRecordEvent;
import cn.com.pyc.pbbonline.bean.event.ShareRecordShowBgLoading;
import cn.com.pyc.pbbonline.common.ShareMode;
import cn.com.pyc.pbbonline.db.Shared;
import cn.com.pyc.pbbonline.db.SharedDBManager;
import cn.com.pyc.pbbonline.db.manager.ClickShareDBManger;
import cn.com.pyc.pbbonline.model.JPDataBean;
import cn.com.pyc.pbbonline.model.JPushDataBean;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.widget.CustomTopNotification;
import cn.com.pyc.pbbonline.widget.CustomTopNotification.OnQuickNotifyListener;
import de.greenrobot.event.EventBus;

/**
 * 自定义通知
 * 
 * @author hudq
 */
public class JpushViewService extends Service
{
	private Context mContext;
	private SharedDBManager dbManager;
	private JPushDataBean pub;
	private CustomTopNotification notification;
	private AsyncTask<String, Void, Boolean> mOpenTask;
	private boolean isTaskRunning = false;

	/**
	 * 开启自定义通知栏
	 * 
	 * @param context
	 * @param jb
	 */
	public static final void startJPushService(Context context, JPushDataBean jb)
	{
		Intent service = new Intent(context, JpushViewService.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("PushUpdateBean", jb);
		service.putExtras(bundle);
		context.startService(service);
	}

	/**
	 * 关闭
	 * 
	 * @param context
	 */
	public static final void closeJPushService(Context context)
	{
		Intent service = new Intent(context, JpushViewService.class);
		context.stopService(service);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		mContext = this;
		dbManager = new SharedDBManager();
		notification = new CustomTopNotification.Builder().setContext(mContext)
				.setTime(System.currentTimeMillis()).setImgRes(R.drawable.app_logo).build();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null)
			return super.onStartCommand(intent, flags, startId);
		pub = (JPushDataBean) intent.getSerializableExtra("PushUpdateBean");
		saveDbManager(pub);
		setListener(pub);
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 消息保存处理
	 * 
	 * @param jPushDataBean
	 */
	private void saveDbManager(JPushDataBean jPushDataBean)
	{
		final JPDataBean bean = jPushDataBean.getData();
		String action = jPushDataBean.getAction();
		if (action == null || bean == null)
		{
			showToast("推送消息有误（msg error）");
			return;
		}
		if (notification == null || dbManager == null)
			return;
		notification.setContent(bean.getTheme());	//设置内容
		switch (action)
		{
			case ShareMode.NEWSHARE:		// 新增分享
				savePushNewShare(bean);
				notification.setTitle(getString(R.string.jpush_notify_new));
				showNotification(null);
				break;
			case ShareMode.REVOKESHARE:		// 收回分享
				updateShareRevoke(bean);
				notification.setTitle(getString(R.string.jpush_notify_revoke));
				showNotification(bean.getShareID());
				break;
			case ShareMode.ADDFFILE:		// 追加文件
			case ShareMode.REVOKEFILE:		// 收回文件
			case ShareMode.REVOKEFOLDER:	// 收回文件夹
				saveOrUpdataRevoke(bean);
				notification.setTitle(getString(R.string.jpush_notify_update));
				showNotification(bean.getShareID());
				break;
			default:
				break;
		}
	}

	/**
	 * 分享没有被删除，显示通知
	 * 
	 * @param shareId
	 */
	private void showNotification(String shareId)
	{
		// 如果app位于后台，则不弹出
		if (!CommonUtil.isAppForward2(mContext))
			return;

		if (shareId == null)
		{
			notification.show();
			return;
		}
		Shared shared = dbManager.findByShareId(shareId);
		if (shared != null && !shared.isDelete())
		{
			notification.show();
		}
	}

	private void setListener(final JPushDataBean bean)
	{
		if (notification == null)
			return;

		notification.setOnQuickNotifyListener(new OnQuickNotifyListener()
		{
			@Override
			public void onClick()
			{
				openPage(bean);
			}
		});
	}

	/**
	 * 推送 收回分享<br/>
	 * {"action":"RevokeShare","data":
	 * "{\"num\":\"4ba59d5b-cb36-406c-86c2-ad24d29268b1\",
	 * \"shareID\":\"a8eb2167-f7e5-43aa-aa52-a36c2e4f2c71\"}"
	 * }
	 */
	private void updateShareRevoke(JPDataBean bean)
	{
		String shareId = bean.getShareID();
		Shared o = dbManager.findByShareId(shareId);
		if (o == null)
			return;

		o.setRevoke(true);
		o.setTime(0);
		boolean is_success = dbManager.updateRevokeShared(o);
		if (is_success)
			EventBus.getDefault().post(new RefreshShareRecordEvent());

	}

	/**
	 * 推送 新增分享 保存
	 */
	private void savePushNewShare(JPDataBean data)
	{
		String shareId = data.getShareID();
		if (dbManager.findByShareId(shareId) != null)
		{
			//推送的分享，如果已经有保存.
			//以后：推送的同一个分享，如果已经删除了，应该替换保存。
			return;
		}

		Shared o = new Shared(shareId, data.getTheme(), data.getOwner());
		o.setShareUrl(data.getUrl());
		o.setTime(ConvertToUtil.toLong(data.getCreate_time(), System.currentTimeMillis()));//没有领取时间的情况下取分享的创建时间
		o.setShareMode(data.getShare_mode());
		//按设备，不需要考虑账户,默认guestpbb;按身份，人数，账户即为登录手机号
		boolean device = (ShareMode.SHAREDEVICE.equals(data.getShare_mode()));
		String account = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
		o.setAccountName(device ? SZInitInterface.getUserName("") : account);
		o.setWhetherNew(true);
		boolean save_ = dbManager.saveShared(o);
		if (save_)
		{
			//通知页面刷新。
			EventBus.getDefault().post(new RefreshShareRecordEvent());
		}
	}

	/**
	 * 收回/追加文件，收回文件夹
	 */
	private void saveOrUpdataRevoke(JPDataBean bean)
	{
		String shareId = bean.getShareID();
		Shared o = dbManager.findByShareId(shareId);
		if (o == null)
			return;
		if (!o.isUpdate())
		{
			o.setUpdate(true);
			boolean is_success = dbManager.updateShared(o);
			if (is_success)
				EventBus.getDefault().post(new RefreshShareRecordEvent());
		}
	}

	public void showToast(String text)
	{
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}

	private void openPage(JPushDataBean bean)
	{
		if (ShareMode.REVOKESHARE.equals(bean.getAction()))
		{
			showToast(getString(R.string.shared_lose_efficacy));
			return; //分享被收回
		}
		//开启页面
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = manager.getRunningTasks(10);
		if (runningTasks == null)
			return;
		ComponentName component = runningTasks.get(0).topActivity;
		Log.v("", "topActivity is " + component.getClassName());
		Shared record = dbManager.findByShareId(bean.getData().getShareID());
		if (record == null)
			return;
		if (isTopAtcy(component.getClassName()))
		{
			openSharedDetail(record);						//在首页面，即打开详情页
		}
		else
		{
			OpenPageUtil.openAppMainPage(mContext, pub);	//其他即打开首页面
			EventBus.getDefault().post(new RefreshShareRecordEvent());
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		cancelTask();
		if (notification != null)
			notification.removeSoure();
	}

	//	private void openIndexPage()
	//	{
	//		Bundle bundle = new Bundle();
	//		bundle.putSerializable("PushUpdateBean", pub);
	//		Intent intent = new Intent(mContext, IndexPageHomeActivity.class);
	//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	//		intent.putExtras(bundle);
	//		mContext.startActivity(intent);
	//	}

	private void openSharedDetail(final Shared record)
	{
		if (record.isRevoke())
		{
			return;
		}
		if (TextUtils.isEmpty(record.getShareUrl()))
		{
			return;
		}
		cancelTask();
		mOpenTask = new AsyncTask<String, Void, Boolean>()
		{
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				isTaskRunning = true;
				EventBus.getDefault().postSticky(new ShareRecordShowBgLoading(true));
			}

			@Override
			protected Boolean doInBackground(String... params)
			{
				if (isCancelled())
					return false;
				if (!isTaskRunning)
					return false;
				record.setWhetherNew(false);
				record.setUpdate(false);
				dbManager.modifyNewSharedState(record);
				ClickShareDBManger.Builder().saveClick(record.getShareId(), true);
				return Util_.initCommonDataDB(params[0], false);
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				super.onPostExecute(result);
				EventBus.getDefault().postSticky(new ShareRecordShowBgLoading(false));
				if (!result)
					return;
				String shareID = (String) SPUtil.get(Fields.FIELDS_ID, "");
				OpenPageUtil.openSharedDetaiPage(mContext, shareID);

				EventBus.getDefault().post(new RefreshShareRecordEvent());
			}
		};
		mOpenTask.execute(record.getShareUrl());
	}

	private void cancelTask()
	{
		if (null != mOpenTask)
		{
			mOpenTask.cancel(true);
			mOpenTask = null;
		}
		isTaskRunning = false;
	}

	//在首页
	private boolean isTopAtcy(String atcyName)
	{
		return ("cn.com.pyc.pbbonline.IndexPageHomeActivity".equals(atcyName))
				|| "cn.com.pyc.pbbonline.ShareRecordListActivity".equals(atcyName)
				|| "cn.com.pyc.receive.ReceiveActivity".equals(atcyName);
	}
}
