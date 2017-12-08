package cn.com.pyc.receive;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.qlk.util.global.GlobalToast;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ViewHelp;

/**
 * 此为原来系列文件列表
 */
@Deprecated
public class ReceiveSeriesListActivity extends PbbBaseActivity
{
	public static final int RESULT_READ = 0x1236;
	private ReceiveAdapter mAdapter;
	private int sid;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_series_list);
		ViewHelp.showAppTintStatusBar(this);
		
		sid = getIntent().getIntExtra("sid", -1);
		((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra("title"));
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.arl_lsv_files);
		mAdapter = new ReceiveAdapter(this, ReceiveActivity.mSidPaths, ReceiveActivity.mDatas);
		listView.setAdapter(mAdapter);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		boolean readSuc = requestCode == RESULT_READ && resultCode == RESULT_OK && data != null;
		if (readSuc)
		{
			SmInfo info = (SmInfo) data
					.getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
			if (info.getSid() != sid)
			{
				ReceiveActivity.reGetSidInfos = true;
				GlobalToast.toastLong(this, "文件已迁移至分组：" + info.getSeriesName());
			}
		}
	}

}
