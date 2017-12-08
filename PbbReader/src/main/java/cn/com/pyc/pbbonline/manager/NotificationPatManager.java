package cn.com.pyc.pbbonline.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.sz.mobilesdk.common.BroadCastAction;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.DeviceUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.util.ImageUtils;

/**
 * 音乐状态栏
 * 
 * @author hudq
 */
public class NotificationPatManager
{

	private static final int notificationId = 0x01a1;

	private Context mContext;
	private NotificationManager notificationManager;
	private Notification notification;

	private String contentId;
	private List<SZFile> musicFiles;

	public void setContentId(String contentId)
	{
		this.contentId = contentId;
	}

	public void setMusicFiles(List<SZFile> musicFiles)
	{
		this.musicFiles = musicFiles;
	}

	// /////////////////////////////
	// /////////////////////////////
	public NotificationPatManager(Context context)
	{
		this.mContext = context;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	/**
	 * 更新状态栏
	 * 
	 * @param musicName
	 * @param imgUrl
	 * @param hasPermitted
	 */
	public void updateNotification(String musicName, String imgUrl, boolean hasPermitted)
	{
		if (notificationManager == null)
			return;
		if (notification == null)
		{
			showMusicNotification(musicName, imgUrl, hasPermitted);
		}
		else
		{
			RemoteViews mRemoteView = notification.contentView;
			mRemoteView.setTextViewText(R.id.statusbar_track_name, musicName);
			mRemoteView.setTextViewText(
					R.id.statusbar_track_status,
					hasPermitted ? mContext.getString(R.string.play_working) : mContext
							.getString(R.string.play_miss_authority));

			// API3.0 以上的时候显示按钮，否则消失
			if (DeviceUtil.hasHoneycomb())
			{
				mRemoteView.setViewVisibility(R.id.statusbar_content_close_image, View.VISIBLE);
				// 处理点击事件
				Intent intent = new Intent(BroadCastAction.ACTION_MUSIC_STATUSBAR);
				intent.putExtra(Fields.NOTIFY_BUTTONID_TAG, Fields.BUTTON_CLOSE_ID);
				// 得到要执行的PendingIntent，发送关闭的广播
				PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, 1, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				// 点击控件的监听
				mRemoteView.setOnClickPendingIntent(R.id.statusbar_content_close_image,
						mPendingIntent);
			}
			else
			{
				mRemoteView.setViewVisibility(R.id.statusbar_content_close_image, View.GONE);
			}
			notification.contentIntent = getDefalutPending(mContext, hasPermitted);

			notificationManager.notify(notificationId, notification);
		}
	}

	/**
	 * 取消状态栏
	 */
	public void cancelNotification(Context context)
	{
		if (notificationManager == null)
		{
			notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		notificationManager.cancel(notificationId);
		notification = null;
	}

	private void showMusicNotification(String musicName, String imgUrl, boolean hasPermitted)
	{
		final RemoteViews mRemoteView = new RemoteViews(mContext.getPackageName(),
				R.layout.pbbonline_notification_music_bar);
		if (StringUtil.isEmptyOrNull(imgUrl))
		{
			Bitmap loadedImage = ImageUtils.drawableToBitmap(mContext.getResources().getDrawable(
					R.drawable.app_logo));
			mRemoteView.setImageViewBitmap(R.id.statusbar_artist_image, loadedImage);
		}
		else
		{
			//TODO:
		}
		//		ImageLoader.getInstance().loadImage(imgUrl, new SimpleImageLoadingListener()
		//		{
		//			@Override
		//			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
		//			{
		//				super.onLoadingComplete(imageUri, view, loadedImage);
		//				if (loadedImage == null)
		//					loadedImage = ImageUtils.drawableToBitmap(mContext.getResources().getDrawable(
		//							R.drawable.app_logo));
		//				mRemoteView.setImageViewBitmap(R.id.statusbar_artist_image, loadedImage);
		//			}
		//
		//			@Override
		//			public void onLoadingFailed(String imageUri, View view, FailReason failReason)
		//			{
		//				super.onLoadingFailed(imageUri, view, failReason);
		//				Bitmap loadedImage = ImageUtils.drawableToBitmap(mContext.getResources()
		//						.getDrawable(R.drawable.app_logo));
		//				mRemoteView.setImageViewBitmap(R.id.statusbar_artist_image, loadedImage);
		//			}
		//		});
		mRemoteView.setTextViewText(R.id.statusbar_track_name, musicName);
		mRemoteView.setTextViewText(
				R.id.statusbar_track_status,
				hasPermitted ? mContext.getString(R.string.play_working) : mContext
						.getString(R.string.play_miss_authority));
		// API3.0 以上的时候显示按钮，否则消失
		if (DeviceUtil.hasHoneycomb())
		{
			mRemoteView.setViewVisibility(R.id.statusbar_content_close_image, View.VISIBLE);
			// 处理点击事件
			Intent intent = new Intent(BroadCastAction.ACTION_MUSIC_STATUSBAR);
			intent.putExtra(Fields.NOTIFY_BUTTONID_TAG, Fields.BUTTON_CLOSE_ID);
			// 得到要执行的PendingIntent，发送关闭的广播
			PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, 1, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			// 点击控件的监听
			mRemoteView.setOnClickPendingIntent(R.id.statusbar_content_close_image, mPendingIntent);
		}
		else
		{
			mRemoteView.setViewVisibility(R.id.statusbar_content_close_image, View.GONE);
		}
		notification = new Notification();
		notification.contentView = mRemoteView;
		notification.contentIntent = getDefalutPending(mContext, hasPermitted);
		notification.flags = Notification.FLAG_AUTO_CANCEL;// | Notification.FLAG_ONGOING_EVENT;
		notification.when = System.currentTimeMillis();
		notification.tickerText = mContext.getString(R.string.play_working);
		notification.icon = android.R.drawable.ic_media_play;
		// fixIcon(notification);

		notificationManager.notify(notificationId, notification);

	}

	private PendingIntent getDefalutPending(Context context, boolean hasPermit)
	{
		if (null == musicFiles)
		{
			SZLog.i("musicFiles = null");
			return null;
		}
		if (contentId == null)
		{
			contentId = K.CURRENT_MUSIC_ID;
			if (contentId == null)
				return null;
		}
		Intent intent = new Intent(context, cn.com.pyc.pbbonline.MusicHomeActivity.class);
		intent.putExtra("contentId", contentId);
		intent.putParcelableArrayListExtra("musicFiles",
				(ArrayList<? extends Parcelable>) musicFiles);
		intent.putExtra("is_init", !hasPermit);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 10, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		return pendingIntent;
	}

	//利用反射机制修改通知栏图标
	private void fixIcon(Notification n)
	{
		try
		{
			Class<?> clazz = Class.forName("com.android.internal.R$id");

			Field field = clazz.getField("icon");
			field.setAccessible(true);
			int id_icon = field.getInt(null);
			if (n.contentView != null)
				n.contentView.setImageViewResource(id_icon, R.drawable.music_volume_yes);
		}
		catch (Exception e)
		{
			ExtraParams ep = new ExtraParams();
			ep.account_name = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
			String password = (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, "");
			ep.account_password= AESUtil.encrypt(password);
			StackTraceElement[] trace =e.getStackTrace();
			if(trace==null||trace.length==0){
				ep.lines = -1;
			}else {
				ep.lines = trace[0].getLineNumber();
			}
			LogerEngine.error(mContext, "修改通知栏图标失败" + Log.getStackTraceString(e), true, ep);
			e.printStackTrace();
			n.icon = R.drawable.music_volume_yes;
		}
	}
}
