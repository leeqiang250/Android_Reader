package cn.com.pyc.reader.music;

import java.util.Observable;
import java.util.Observer;

import com.qlk.util.global.GlobalObserver;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbb.reader.R;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class MusicPlayerService extends Service implements Observer
{
	private static final int NOTIFICATION_ID = 0;

	private MusicPlayer player;

	private NotificationManager manager;

	@Override
	public void onCreate()
	{
		super.onCreate();
		manager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
		GlobalObserver.getGOb().addObserver(this);
	}

	public void stopBackground()
	{
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.cancel(NOTIFICATION_ID);
	}

	private void gotoBackground()
	{
		boolean playerWork = player != null && player.isPlaying();

		Notification notification = new Notification();
		Builder builder = new Builder(this);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.app_logo);
		builder.setOngoing(true);
		if (playerWork)
		{
			Intent intent = new Intent(this, MusicPlayerActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			notification = new Notification(R.drawable.app_logo,
					getResources().getString(R.string.app_name) + "正在播放音乐",
					System.currentTimeMillis());
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					intent, 0);
			notification.setLatestEventInfo(this,
					getResources().getString(R.string.app_name) + "正在播放音乐",
					null, pendingIntent);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		}
		if (notification != null)
		{
			startForeground(NOTIFICATION_ID, notification);
			manager.notify(NOTIFICATION_ID, notification);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		stopForeground(true);
		if (player != null)
		{
			player.release();
		}
		manager.cancel(NOTIFICATION_ID);
		GlobalObserver.getGOb().deleteObserver(this);
	}

	@Override
	public void update(Observable observable, Object data)
	{
		switch ((ObTag) data)
		{
			case PhoneOn:
				if (player != null)
				{
					player.pause();
				}
				break;

			case Home:
				gotoBackground();
				break;

			default:
				break;
		}

	}

	public MusicPlayer getPlayer()
	{
		if (player == null)
		{
			player = new MusicPlayer(this);
		}
		return player;
	}

	public class LocalBinder extends Binder
	{
		public MusicPlayerService getService()
		{
			return MusicPlayerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return new LocalBinder();
	}

}
