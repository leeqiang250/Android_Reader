package cn.com.pyc.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.sz.mobilesdk.util.TimeUtil;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.main.adapter.SeriesFilesAdapter;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import de.greenrobot.event.EventBus;


/**
 * by 熊
 * <p>
 * 显示系列文件的列表界面
 */
public class SeriesListActivity extends ExtraBaseActivity {

    private ListView mListView;//系列文件列表
    private SeriesFilesAdapter adapter;
    private List<String> paths;//系列文件的路径集合
    private String seriesName;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_files);
        ViewHelp.showAppTintStatusBar(this);

        paths = getIntent().getStringArrayListExtra("pbb_series_paths");
        seriesName = getIntent().getStringExtra("pbb_series_name");
        if (paths == null) {
            finish();
            return;
        }

        mListView = (ListView) findViewById(R.id.swipe_target);
        SwipeToLoadLayout mSwipeLayout = (SwipeToLoadLayout) findViewById(R.id.lyt_pull_refresh);
        mSwipeLayout.setRefreshEnabled(false);
        mSwipeLayout.setLoadMoreEnabled(false);
        mSwipeLayout.setRefreshing(false);
        TextView tvTitle = (TextView) findViewById(R.id.title_tv);
        tvTitle.setText(seriesName);//标题显示系列名称
        findViewById(R.id.back_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //合并的数据的排序
        Collections.sort(paths, new Comparator<String>() {
            @Override
            public int compare(String t1, String t2) {
                String t2Time = TimeUtil.getDateStringFromMills(new File(t2)
                        .lastModified() + "", "yyyy-MM-dd HH:mm");
                String t1Time = TimeUtil.getDateStringFromMills(new File(t1)
                        .lastModified() + "", "yyyy-MM-dd HH:mm");
                if (TextUtils.isEmpty(t2Time) || TextUtils.isEmpty(t1Time)) {
                    return 0;
                }
                return t2Time.compareTo(t1Time);
            }
        });

        adapter = new SeriesFilesAdapter(this, paths);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String path = (String) mListView.getItemAtPosition(position);
                Intent intent = new Intent(SeriesListActivity.this, PbbFileDetailActivity.class);
                intent.putExtra("pbb_path", path);
                intent.putExtra("pbb_series_name", seriesName);
                startActivity(intent);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);

                String path = (String) mListView.getItemAtPosition(i);
                showDeleteDialog(SeriesListActivity.this, path);

                return true;
            }
        });
    }

    //删除对话框
    private void showDeleteDialog(Activity activity, final String path) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_delete, null);
        final Dialog dialog = new Dialog(activity, R.style.no_frame_small);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(v);
        dialog.show();
        TextView prompt = (TextView) v.findViewById(R.id.dd_txt_content);
        prompt.setText("确定要删除此项?");
        Button cancel = (Button) v.findViewById(R.id.dd_btn_sure);
        cancel.setText("取消");
        Button sure = (Button) v.findViewById(R.id.dd_btn_cancel);
        sure.setText("确定");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalData.Sm.instance(SeriesListActivity.this).delete(path);
                dialog.cancel();
                paths.remove(path);
                adapter.notifyDataSetChanged();
                dialog.cancel();
                //删除完，通知主界面列表刷新
                EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_HOME_TAB_2));
                if (paths.size() < 1) {
                    //删除完所有文件，结束当前页面
                    finish();
                }
            }
        });
    }


}
