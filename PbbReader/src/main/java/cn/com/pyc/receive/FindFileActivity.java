package cn.com.pyc.receive;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.qlk.util.media.scanner.QlkFileScannerActivity;

import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pcshare.DeviceScannerActivity;
import cn.com.pyc.sm.SmReaderActivity;

/**
 * 浏览文件
 */
public class FindFileActivity extends QlkFileScannerActivity {

//	@Override
//	protected String[] getSupportTypes()
//	{
//		return null;
//	}

    @Override
    protected void onFileClick(String path) {
        Intent intent = new Intent(this, SmReaderActivity.class);
        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, path);
        startActivity(intent);
    }


    @Override
    protected void initHeadView(ListView dirsListView) {
        View headView = View.inflate(this, R.layout.adapter_file_scanner, null);
        headView.findViewById(R.id.afs_lyt_datesize).setVisibility(View.GONE);
        (headView.findViewById(R.id.afs_imv_pic)).setBackgroundResource(R.drawable
                .format_folder);
        ((TextView) headView.findViewById(R.id.afs_txt_name)).setText("我的共享");

        dirsListView.addHeaderView(headView);
        headView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FindFileActivity.this, DeviceScannerActivity.class));
            }
        });
    }

}
