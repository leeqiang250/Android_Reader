package cn.com.pyc.plain.record;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.plain.record.MusicRecordService.Command;
import cn.com.pyc.sm.PayLimitConditionActivity;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.widget.WaveView;

/**
 * 录音界面
 * 
 * @author QiLiKing
 */
public class MusicRecordActivity extends ExtraBaseActivity implements OnClickListener
{
	private enum MusicRecordState
	{
		Prepare, RecordPause, Recording, AuditionPause, Auditioning
	}

	private static final int bkg_back = R.drawable.ic_back;
	private static final int bkg_back_disabled = R.drawable.back_disabled;
	private static final int bkg_complete = R.drawable.xml_record_complete;
	private static final int bkg_complete_disabled = R.drawable.record_complete_disabled;

	private static final int bkg_audition = R.drawable.xml_record_pause;
	private static final int bkg_audition_disabled = R.drawable.record_play_disabled;
	private static final int bkg_auditioning = R.drawable.xml_record_play;

	private static final int bkg_record = R.drawable.imb_record;
	private static final int bkg_record_disabled = R.drawable.record_disabled;
	private static final int bkg_recording = R.drawable.imb_recording;

	private TextView g_txtRecordDuration;
	private ImageView g_imvRecordAnim;
	private TextView g_txtAuditionDuration;
	private ImageView g_imvAuditionAnim;
	private View g_lytAuditionAnim;
	private ImageButton g_imbAudition;	// 试听
	private ImageButton g_imbRecord;	// 暂停录音
	private ImageButton g_imbComplete;
	private ImageButton g_imbBack;
	private WaveView g_vRecordWave;
	private WaveView g_vAuditionWave;
	private String filePath;
	private int auditionProgress;
	private int recordProgress;
	private String storeFold;

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_music_record);
		ViewHelp.showAppTintStatusBar(this);
		findViewAndSetListeners();
		setRecordState(MusicRecordState.Prepare);

		storeFold = Dirs.getCameraDir(Dirs.getMaxSpaceBoot(this));
		String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date())
				+ WAV;
		filePath = storeFold + File.separator + "pbb_" + name;
		publishCommand(Command.InitRecord);

	}

	@Override
	protected void onStart()
	{
		super.onStart();
		register();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		unregister();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		publishCommand(Command.StopBkg);
	}

	private void publishCommand(Command command)
	{
		Intent intent = new Intent(this, MusicRecordService.class);
		intent.putExtra(Command.class.getName(), command);
		switch (command)
		{
			case InitRecord:
				intent.putExtra(Command.InitRecord.name(), filePath);
				break;

			default:
				break;
		}

		startService(intent);
	}

	/*-********************************
	 * BroadcastReceiver
	 *********************************/

	private void register()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicRecordService.MUSIC_ACTION_RECORD);
		filter.addAction(MusicRecordService.MUSIC_ACTION_AUDITION);
		LocalBroadcastManager.getInstance(this).registerReceiver(recordAuditionReceiver, filter);
	}

	private void unregister()
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(recordAuditionReceiver);
	}

	private BroadcastReceiver recordAuditionReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equals(MusicRecordService.MUSIC_ACTION_RECORD))
			{
				String key = MusicRecordService.BackInfo.Volume.name();
				if (intent.hasExtra(key))
				{
					g_vRecordWave.updateWave(intent.getFloatExtra(key, 0), recordProgress);
				}

				key = MusicRecordService.BackInfo.Progress.name();
				if (intent.hasExtra(key))
				{
					recordProgress = intent.getIntExtra(key, 0);
					formatTime(g_txtRecordDuration, recordProgress);
				}

				key = MusicRecordService.BackInfo.Error.name();
				if (intent.hasExtra(key))
				{
					GlobalToast.toastShort(context, intent.getStringExtra(key));
					finish();
				}

				key = MusicRecordService.BackInfo.StartPause.name();
				if (intent.hasExtra(key))
				{
					if (intent.getBooleanExtra(key, false))
					{
						setRecordState(MusicRecordState.Recording);
					}
					else
					{
						setRecordState(MusicRecordState.RecordPause);
					}
				}
			}
			else if (action.equals(MusicRecordService.MUSIC_ACTION_AUDITION))
			{
				String key = MusicRecordService.BackInfo.Volume.name();
				if (intent.hasExtra(key))
				{
					g_vAuditionWave.updateWave(intent.getFloatExtra(key, 0), auditionProgress);
				}

				key = MusicRecordService.BackInfo.Progress.name();
				if (intent.hasExtra(key))
				{
					auditionProgress = intent.getIntExtra(key, 0);
					formatTime(g_txtAuditionDuration, auditionProgress);
				}

				key = MusicRecordService.BackInfo.Error.name();
				if (intent.hasExtra(key))
				{
					GlobalToast.toastShort(context, intent.getStringExtra(key));
					finish();
				}

				key = MusicRecordService.BackInfo.StartPause.name();
				if (intent.hasExtra(key))
				{
					if (intent.getBooleanExtra(key, false))
					{
						setRecordState(MusicRecordState.Auditioning);
					}
					else
					{
						setRecordState(MusicRecordState.AuditionPause);
					}
				}
			}
			else
			{
				// do nothing
			}
		}
	};

	@Override
	protected void findViewAndSetListeners()
	{
		g_txtRecordDuration = (TextView) findViewById(R.id.amr_txt_record_duration);
		g_imvRecordAnim = (ImageView) findViewById(R.id.amr_imv_record_anim);
		g_txtAuditionDuration = (TextView) findViewById(R.id.amr_txt_audition_duration);
		g_imvAuditionAnim = (ImageView) findViewById(R.id.amr_imv_audition_anim);
		g_lytAuditionAnim = findViewById(R.id.amr_lyt_audition_anim);
		g_imbAudition = (ImageButton) findViewById(R.id.amr_imb_audition);
		g_imbRecord = (ImageButton) findViewById(R.id.amr_imb_record);
		g_imbComplete = (ImageButton) findViewById(R.id.amr_imb_complete);
		g_imbBack = (ImageButton) findViewById(R.id.amr_imb_back);
		g_vRecordWave = (WaveView) findViewById(R.id.amr_view_wave_record);
		g_vAuditionWave = (WaveView) findViewById(R.id.amr_view_wave_audition);
		g_imbBack.setOnClickListener(this);		// 这个不能设置“onBackButtonClick”，否则showExitDialog出错
		// findViewById(R.id.amr_lyt_record_anim).setOnClickListener(this);
		// findViewById(R.id.amr_lyt_audition_anim).setOnClickListener(this);

		g_imbComplete.setOnClickListener(this);
		g_imbAudition.setOnClickListener(this);
		g_imbRecord.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.amr_imb_complete:
				showRenameDialog();
				break;

			case R.id.amr_imb_record:
			case R.id.amr_lyt_record_anim:
				publishCommand(Command.RecordStartPause);
				break;

			case R.id.amr_imb_audition:
			case R.id.amr_lyt_audition_anim:
				publishCommand(Command.AuditionStartPause);
				break;

			case R.id.amr_imb_back:
				if (new File(filePath).exists())
				{
					showExitDialog();
				}
				else
				{
					finish();
				}
				break;

			default:
				break;
		}
	}

	private void setRecordState(MusicRecordState state)
	{
		switch (state)
		{
			case Prepare:		// 准备阶段
				changeViewState(g_imbComplete, bkg_complete_disabled, false);		// 不能完成
				changeViewState(g_imbAudition, bkg_audition_disabled, false);		// 不能试听
				changeViewState(g_imbRecord, bkg_record, true);		// 可以录音
				changeViewState(g_imbBack, bkg_back, true);		// 可以返回
				g_vAuditionWave.setVisibility(View.GONE);
				g_vRecordWave.setVisibility(View.VISIBLE);
				break;

			case RecordPause:		// 录完后暂停
				changeViewState(g_imbComplete, bkg_complete, true);		// 可以完成
				changeViewState(g_imbAudition, bkg_audition, true);	// 可以试听
				changeViewState(g_imbRecord, bkg_record, true);		// 可以录音
				changeViewState(g_imbBack, bkg_back, true);		// 可以返回
				stopAnim(true, false);
				g_vAuditionWave.setVisibility(View.GONE);
				g_vRecordWave.setVisibility(View.VISIBLE);
				break;

			case Recording:		// 正在录音
				changeViewState(g_imbComplete, bkg_complete_disabled, false);		// 不能完成
				changeViewState(g_imbAudition, bkg_audition_disabled, false);		// 不能试听
				changeViewState(g_imbRecord, bkg_recording, true);		// 可以录音
				changeViewState(g_imbBack, bkg_back, false);		// 不能返回
				startRecordAnim();
				g_vAuditionWave.setVisibility(View.GONE);
				g_vRecordWave.setVisibility(View.VISIBLE);
				break;

			case AuditionPause:		// 试听暂停或者可以试听
				changeViewState(g_imbComplete, bkg_complete, true);		// 可以完成
				changeViewState(g_imbAudition, bkg_audition, true);		// 可以
				changeViewState(g_imbRecord, bkg_record, true);			// 可以录音
				changeViewState(g_imbBack, bkg_back, true);		// 可以返回
				// stopAnim(false, true);
				g_vAuditionWave.setVisibility(View.VISIBLE);
				g_vRecordWave.setVisibility(View.GONE);
				g_vAuditionWave.pause();
				break;

			case Auditioning:		// 试听中
				changeViewState(g_imbComplete, bkg_complete_disabled, false);		// 不能完成
				changeViewState(g_imbAudition, bkg_auditioning, true);		// 可以试听
				changeViewState(g_imbRecord, bkg_record_disabled, false);		// 不能录音
				changeViewState(g_imbBack, bkg_back, false);		// 不能返回
				// startAuditionAnim();
				g_vAuditionWave.setVisibility(View.VISIBLE);
				g_vRecordWave.setVisibility(View.GONE);
				break;

			default:
				break;
		}
	}

	/*-********************************
	 * 动画
	 *********************************/

	private void startRecordAnim()
	{
		Animation animation = g_imvRecordAnim.getAnimation();
		if (animation == null)
		{
			animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setFillAfter(true);
			animation.setDuration(2000);
			animation.setRepeatCount(Animation.INFINITE);
			// 旋转一次不停顿一下，需要以下两行代码
			LinearInterpolator lir = new LinearInterpolator();
			animation.setInterpolator(lir);
			g_imvRecordAnim.setVisibility(View.VISIBLE);
			g_imvRecordAnim.setAnimation(animation);
		}
		animation.startNow();
	}

	private void startAuditionAnim()
	{
		Animation animation = g_imvAuditionAnim.getAnimation();
		if (animation == null)
		{
			animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setFillAfter(true);
			animation.setDuration(2000);
			animation.setRepeatCount(Animation.INFINITE);
			// 旋转一次不停顿一下，需要以下两行代码
			LinearInterpolator lir = new LinearInterpolator();
			animation.setInterpolator(lir);
			g_imvAuditionAnim.setAnimation(animation);
		}
		g_lytAuditionAnim.setVisibility(View.VISIBLE);
		animation.startNow();
	}

	private void stopAnim(boolean isRecordStop, boolean isAuditionStop)
	{
		try
		{
			if (isRecordStop)
			{
				g_imvRecordAnim.getAnimation().cancel();
			}
			if (isAuditionStop)
			{
				g_imvAuditionAnim.getAnimation().cancel();
				g_lytAuditionAnim.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// 格式化显示的时间
	private void formatTime(TextView v, int progress)
	{
		progress /= 1000;
		int minute = progress / 60;
		int hour = minute / 60;
		int second = progress % 60;
		minute %= 60;
		String time = null;
		if (hour > 0)
		{
			time = String.format(Locale.CHINESE, "%02d:%02d:%02d", hour, minute, second);
			v.setText(time);
			if (v.getId() == R.id.amr_txt_record_duration)
			{
				v.setTextSize(30);
			}
			else if (v.getId() == R.id.amr_txt_audition_duration)
			{
				v.setTextSize(10);
			}
		}
		else
		{
			time = String.format(Locale.CHINESE, "%02d:%02d", minute, second);
			v.setText(time);
		}
	}

	private void changeViewState(View v, int bkg, boolean clickable)
	{
		v.setBackgroundResource(bkg);
		v.setClickable(clickable);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (new File(filePath).exists())
			{
				showExitDialog();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showExitDialog()
	{
		// 先暂停可能正在进行的任务
		publishCommand(Command.Pause);

		View v = LayoutInflater.from(this).inflate(R.layout.dialog_receive, null);
		final Dialog dialog = new Dialog(this, R.style.no_frame_small);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(v);
		dialog.show();
		TextView prompt = (TextView) v.findViewById(R.id.dd_txt_content);
		prompt.setText("是否保留录音文件?");
		Button check = (Button) v.findViewById(R.id.dd_btn_sure);
		check.setText("保留");
		Button exit = (Button) v.findViewById(R.id.dd_btn_cancel);
		exit.setText("删除");
		check.setOnClickListener(new android.view.View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				publishCommand(Command.Release); // 不能放在onDestroy里，否则就失去了后台的功能
				dialog.dismiss();
				finish();
			}
		});
		exit.setOnClickListener(new android.view.View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new File(filePath).delete();
				publishCommand(Command.Release); // 不能放在onDestroy里，否则就失去了后台的功能
				dialog.dismiss();
				finish();
			}
		});
	}

	private static final String WAV = ".wav";

	private void showRenameDialog()
	{
		View v = LayoutInflater.from(this).inflate(R.layout.dialog_rename, null);
		final Dialog dialog = new Dialog(this, R.style.no_frame_small);
		dialog.setContentView(v);
		final EditText edtName = (EditText) v.findViewById(R.id.dr_edt_name);
		edtName.setHint(FileUtil.getFileName(filePath).replaceAll(WAV, ""));
		dialog.show();
		v.findViewById(R.id.dr_btn_yes).setOnClickListener(new android.view.View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String name = edtName.getText().toString().trim();
				if (!name.equals(edtName.getHint().toString().trim()))
				{
					// 用户重命名了
					if (name.indexOf(".") != -1 || name.indexOf(File.separator) != -1
							|| name.indexOf("\\") != -1)
					{
						GlobalToast.toastShort(getApplicationContext(), "文件名无效！");
					}
					else
					{
						if (!TextUtils.isEmpty(name))
						{
							String newName = storeFold + File.separator + name + WAV;
							new File(filePath).renameTo(new File(newName));
							filePath = newName;
						}
						goTo();
						dialog.dismiss();
					}
				}
			}
		});
	}

	private void goTo()
	{
//		Intent intent = new Intent(MusicRecordActivity.this, ChooseSMwayActivity.class);
		Intent intent = new Intent(MusicRecordActivity.this, PayLimitConditionActivity.class);
		intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, filePath);
		intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
		startActivity(intent);
		GlobalToast.toastLong(this, "文件存储路径：" + filePath);
		publishCommand(Command.Release); // 不能放在onDestroy里，否则就失去了后台的功能

		GlobalData.Music.instance(this).add(0, filePath, false);

		finish();
	}

}
