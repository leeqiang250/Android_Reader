package cn.com.pyc.pbbonline;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;

import java.io.File;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.event.CopyOnlineDataEvent;
import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.pbbonline.util.DirsUtil;
import cn.com.pyc.pbbonline.widget.ProgressButton;
import de.greenrobot.event.EventBus;

/**
 * 中间页面（迁移数据显示进度时使用）
 */
public class IntermediaryActivity extends PbbBaseActivity
{
	private static final String TAG = "IntermediaryUI";
	private String srcPath;
	private String destPath;
	private int tempOrder = 0;
	private ProgressButton pbProgress;
	private TextView tvIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pbbonline_acitivity_intermediary);
		init();
		initView();
		copyData();
	}

	private void initView()
	{
		pbProgress = (ProgressButton) findViewById(R.id.intermediary_progress_btn);
		tvIndex = (TextView) findViewById(R.id.intermediary_progress_text);
		pbProgress.setState(ProgressButton.DOWNLOADING);
	}

	private void init()
	{
		srcPath = getIntent().getStringExtra("srcPath");
		destPath = getIntent().getStringExtra("destPath");
		SZLog.d(TAG, "srcPath: " + srcPath);
		SZLog.d(TAG, "destPath: " + destPath);
		EventBus.getDefault().register(this);
		if (getWindow() != null)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void copyData()
	{
		final File srcFile = new File(srcPath);
		final File desFile = new File(destPath);
		desFile.mkdirs();
		SZInitInterface.destoryFilePath();

		Log.i(TAG, "copying...");
		Runnable command = new CopyOnlineThread(srcFile, desFile);
		ExecutorManager.getInstance().execute(command);
	}

	private static class CopyOnlineThread implements Runnable
	{
		private File srcFile, desFile;

		public CopyOnlineThread(File srcFile, File desFile)
		{
			this.srcFile = srcFile;
			this.desFile = desFile;
		}

		@Override
		public void run()
		{
			DirsUtil.copy(srcFile, desFile);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	/**
	 * 迁移数据更新进度
	 * 
	 * @param e
	 */
	public void onEventMainThread(CopyOnlineDataEvent e)
	{
		if (e == null)
			return;
		final int order = e.getOrder(), progress = e.getProgress();
		if (order == -1 && progress == -1) //数据复制完毕
		{
			tvIndex.setText("更新完毕,正在退出...");
			pbProgress.setProgressText("", 100);
			SPUtil.save("online.copy", true);
			toMainPage();
			return;
		}
		if (order > tempOrder)
		{
			tvIndex.setText("正在更新第" + order + "条数据...");
			pbProgress.setProgressText("", 0);
			tempOrder = order;
		}
		pbProgress.setProgressText("", e.getProgress());
	}

	private void toMainPage()
	{
		tvIndex.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				IntermediaryActivity.this.finish();
			}
		}, 1000);
	}

}
