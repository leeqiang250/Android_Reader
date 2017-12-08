package cn.com.pyc.plain.record;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.qlk.util.global.GlobalObserver;
import com.qlk.util.global.GlobalToast;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import cn.com.pyc.pbb.R;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.plain.record.PlayerBase.OnPlayerListener;
import cn.com.pyc.plain.record.PlayerBase.PlayerCmd;

/**
 * 后台服务
 * 
 * @author QiLiKing
 */
public class MusicRecordService extends Service implements Observer
{
	private static final int NOTIFICATION_ID = 0;

	public enum Command
	{
		InitRecord, RecordStartPause, AuditionStartPause, Pause, Release, StopBkg
	}

	public enum BackInfo
	{
		Volume, Progress, Error, StartPause
	}

	public static final String MUSIC_ACTION_RECORD = "cn.com.pyc.br.music.record";
	public static final String MUSIC_ACTION_AUDITION = "cn.com.pyc.br.music.audition";
	public static final String MUSIC_ACTION_PLAYER = "cn.com.pyc.br.music.player";

	private Record record;
	// private RecordMr record;
	private Audition audition;
	// private AuditionMp audition;
	private String filePath;
	private boolean isBackground = false;

	private NotificationManager manager;

	@Override
	public void onCreate()
	{
		super.onCreate();
		manager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
		GlobalObserver.getGOb().addObserver(this);
		// TelephonyManager tm = (TelephonyManager)
		// getSystemService(Context.TELEPHONY_SERVICE);
		// tm.listen(new PhoneStateListener()
		// {
		// @Override
		// public void onCallStateChanged(int state, String incomingNumber)
		// {
		// super.onCallStateChanged(state, incomingNumber);
		// if (state != TelephonyManager.CALL_STATE_IDLE)
		// {
		// boolean recordWork = record != null && record.isWorking();
		// boolean auditionWork = audition != null
		// && audition.isWorking();
		// if (recordWork)
		// {
		// record.executeCmd(PlayerCmd.Pause);
		// }
		// if (auditionWork)
		// {
		// audition.executeCmd(PlayerCmd.Pause);
		// }
		// }
		// }
		//
		// }, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null)
			return super.onStartCommand(intent, flags, startId);
		Command command = (Command) intent.getSerializableExtra(Command.class.getName());
		if (command == null)
			return super.onStartCommand(intent, flags, startId);
		switch (command)
		{
			case InitRecord:	// 注意，只能调用一次，且必须最先调用
				filePath = intent.getStringExtra(Command.InitRecord.name());
				record = new Record(this, filePath);
				audition = new Audition(this, filePath);
				record.setOnPlayerListener(recordListener);
				audition.setOnPlayerListener(auditionListener);
				break;

			case RecordStartPause:
				if (isEnoughMemory())
				{
					record.executeCmd(PlayerCmd.StartPause);
				}
				break;

			case AuditionStartPause:
				audition.executeCmd(PlayerCmd.StartPause);
				break;

			case Pause:
				if (record != null)
				{
					record.executeCmd(PlayerCmd.Pause);
				}
				if (audition != null)
				{
					audition.executeCmd(PlayerCmd.Pause);
				}
				break;

			case Release:
				stopSelf();
				break;

			case StopBkg:
				isBackground = false;
				((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
						.cancel(NOTIFICATION_ID);
				sendCurState();		// 如果因为内存不足而被终止，这时候activity是接收不到广播的，所以再确认一次
				break;

			default:
				break;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private boolean isEnoughMemory()
	{
		File file = new File(filePath);
		// 保留10M空间
		if (file.exists()
				&& file.length() > file.getParentFile().getUsableSpace() + 10 * 1024 * 1024)
		{
			lowMemoryOccured();
			return false;
		}
		return true;
	}

	// 如果在后台过程中，activity被清理，而且状态改变了，那么activity就接收不到信号了
	private void sendCurState()
	{
		boolean recordWork = record != null && record.isWorking();
		boolean auditionWork = audition != null && audition.isWorking();

		if (!(recordWork || auditionWork))
		{
			Intent intent = new Intent(MUSIC_ACTION_RECORD);
			intent.putExtra(BackInfo.StartPause.name(), false);
			sendBroadcast(intent);
			Intent intent2 = new Intent(MUSIC_ACTION_AUDITION);
			intent2.putExtra(BackInfo.StartPause.name(), false);
			sendBroadcast(intent2);
		}

		// if (!isEnoughMemory())
		// {
		// Intent intent = new Intent(MUSIC_ACTION_RECORD);
		// intent.putExtra(BackInfo.StartPause.name(), record.isWorking);
		// sendBroadcast(intent);
		// }
	}

	private void gotoBackground()
	{
		boolean recordWork = record != null && record.isWorking();
		boolean auditionWork = audition != null && audition.isWorking();

		Notification notification = new Notification();
		Builder builder = new Builder(this);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.ic_app_small);
		builder.setOngoing(true);
		if (recordWork || auditionWork)
		{
			builder.setContentTitle("鹏保宝正在录音");
			builder.setContentText("请谨慎使用\"一键清理\"功能");
			Intent intent = new Intent(this, MusicRecordActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					intent, 0);
			builder.setContentIntent(pendingIntent);
		}
		if (notification != null)
		{
			startForeground(NOTIFICATION_ID, notification);		// 启动后台
			manager.notify(NOTIFICATION_ID, notification);
		}
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		if (record != null && record.isWorking())
		{
			lowMemoryOccured();
		}
	}

	private void lowMemoryOccured()
	{
		if (isBackground && (record != null))
		{
			Notification notification = new Notification(
					R.drawable.ic_app_small, "空间不足，鹏保宝停止录音",
					System.currentTimeMillis());
			Intent intent = new Intent(this, MusicRecordActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// PendingIntent为一个特殊的Intent,通过getBroadcast或者getActivity或者getService得到.
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					intent, 0);
			notification.setLatestEventInfo(this, "鹏保宝录音已停止", "没有足够的空间了！",
					pendingIntent);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			startForeground(NOTIFICATION_ID, notification);		// 启动后台
			// 启动通知事件
			if (record.isWorking())
			{
				((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
						.notify(NOTIFICATION_ID, notification);
			}
		}
		else
		{
			GlobalToast.toastShort(getApplicationContext(), "没有足够的空间了！");
		}

		// 停止录音
		record.executeCmd(PlayerCmd.Pause);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		stopForeground(true);
		if (record != null)
		{
			record.executeCmd(PlayerCmd.Release);
		}
		if (audition != null)
		{
			audition.executeCmd(PlayerCmd.Release);
		}
		GlobalObserver.getGOb().deleteObserver(this);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.cancel(NOTIFICATION_ID);	// 服务业被杀死了，则需要清除notify
	}

	/*-****************************************
	 * Record
	 *****************************************/
	private OnPlayerListener recordListener = new OnPlayerListener()
	{
		private static final int VOLUME_PERIOD = 200;

		private long time;
		private float averageVolume;

		@Override
		public void onVolumeChanged(float volume)
		{
			averageVolume = (averageVolume + volume) / 2;
			if (System.currentTimeMillis() - time > VOLUME_PERIOD)
			{
				Intent intent = new Intent(MUSIC_ACTION_RECORD);
				intent.putExtra(BackInfo.Volume.name(), averageVolume);
				LocalBroadcastManager.getInstance(MusicRecordService.this)
						.sendBroadcast(intent);
				time = System.currentTimeMillis();
				averageVolume = 0;
			}
		}

		@Override
		public void onProgressChanged(int... curProgress)
		{
			Intent intent = new Intent(MUSIC_ACTION_RECORD);
			intent.putExtra(BackInfo.Progress.name(), curProgress[0]);
			LocalBroadcastManager.getInstance(MusicRecordService.this)
					.sendBroadcast(intent);

			isEnoughMemory();
		}

		@Override
		public void onError(String reason)
		{
			Intent intent = new Intent(MUSIC_ACTION_RECORD);
			intent.putExtra(BackInfo.Error.name(), reason);
			LocalBroadcastManager.getInstance(MusicRecordService.this)
					.sendBroadcast(intent);
			stopSelf();
		}

		@Override
		public void onStateChanged(boolean isWorking)
		{
			Intent intent = new Intent(MUSIC_ACTION_RECORD);
			intent.putExtra(BackInfo.StartPause.name(), isWorking);
			LocalBroadcastManager.getInstance(MusicRecordService.this)
					.sendBroadcast(intent);
		}
	};

	private OnPlayerListener auditionListener = new OnPlayerListener()
	{
		private static final int VOLUME_PERIOD = 200;

		private long time;
		private float averageVolume;

		@Override
		public void onVolumeChanged(float volume)
		{
			averageVolume = (averageVolume + volume) / 2;
			if (System.currentTimeMillis() - time > VOLUME_PERIOD)
			{
				Intent intent = new Intent(MUSIC_ACTION_AUDITION);
				intent.putExtra(BackInfo.Volume.name(), averageVolume);
				LocalBroadcastManager.getInstance(MusicRecordService.this)
						.sendBroadcast(intent);
				time = System.currentTimeMillis();
				averageVolume = 0;
			}
		}

		@Override
		public void onProgressChanged(int... curProgress)
		{
			Intent intent = new Intent(MUSIC_ACTION_AUDITION);
			intent.putExtra(BackInfo.Progress.name(), curProgress[0]);
			LocalBroadcastManager.getInstance(MusicRecordService.this)
					.sendBroadcast(intent);
		}

		@Override
		public void onError(String reason)
		{
			Intent intent = new Intent(MUSIC_ACTION_AUDITION);
			intent.putExtra(BackInfo.Error.name(), reason);
			LocalBroadcastManager.getInstance(MusicRecordService.this)
					.sendBroadcast(intent);
			stopSelf();
		}

		@Override
		public void onStateChanged(boolean isWorking)
		{
			Intent intent = new Intent(MUSIC_ACTION_AUDITION);
			intent.putExtra(BackInfo.StartPause.name(), isWorking);
			LocalBroadcastManager.getInstance(MusicRecordService.this)
					.sendBroadcast(intent);
		}
	};

	@Override
	public void update(Observable observable, Object data)
	{
		switch ((ObTag) data)
		{
			case PhoneOn:
				boolean recordWork = record != null && record.isWorking();
				boolean auditionWork = audition != null && audition.isWorking();
				if (recordWork)
				{
					record.executeCmd(PlayerCmd.Pause);
				}
				if (auditionWork)
				{
					audition.executeCmd(PlayerCmd.Pause);
				}
				break;

			case Home:
				gotoBackground();
				break;

			default:
				break;
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
