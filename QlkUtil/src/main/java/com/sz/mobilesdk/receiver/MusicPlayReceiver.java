package com.sz.mobilesdk.receiver;

import com.sz.mobilesdk.common.BroadCastAction;
import com.sz.mobilesdk.common.Fields;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class MusicPlayReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent == null || intent.getAction() == null) return;

		int currentPos = intent.getIntExtra("m.currentPosition", -1);
		int duration = intent.getIntExtra("m.duration", -1);

		switch (intent.getAction())
		{
			case BroadCastAction.ACTION_MUSIC_PROGRESS:
				playProgress(currentPos, duration);
				break;
			case BroadCastAction.ACTION_MUSIC_OBTAIN_TIME:
				obtainTime(currentPos, duration);
				break;
			case BroadCastAction.ACTION_MUSIC_STATUSBAR:
			{
				int buttonId = intent
						.getIntExtra(Fields.NOTIFY_BUTTONID_TAG, 0);
				onStatusBar(buttonId);
			}
				break;
			default:
				break;
		}
	}

	protected abstract void playProgress(int current, int duration);

	protected abstract void obtainTime(int current, int duration);

	protected abstract void onStatusBar(int buttonId);

}
