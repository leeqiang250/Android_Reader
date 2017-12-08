package com.qlk.util.media.scanner;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qlk.util.R;
import com.qlk.util.base.BaseActivity;
import com.qlk.util.global.GlobalDialog;
import com.qlk.util.global.GlobalDialog.DialogInfo;
import com.qlk.util.global.GlobalDialog.DialogInfo.DialogSize;
import com.qlk.util.media.QlkDirs;
import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.widget.HorizontalListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public abstract class QlkFileScannerActivity extends BaseActivity {
    public static final String SCAN_DIR = "scan_dir";
    private static final String GO_BACK_FLAG = "...";

    private HorizontalListView guideListView;
    private GuideAdapter guideAdapter;
    private ListView dirsListView;
    private DirsAdapter dirsAdapter;

    private String g_strCurDir;
    private ArrayList<String> diskBoot;
    private final ArrayList<String> guides = new ArrayList<String>();
    private final ArrayList<String> names = new ArrayList<String>();
    private final Scanner scanner = new Scanner();

    private final Stack<Integer> touchHistory = new Stack<Integer>();

    // protected abstract String[] getSupportTypes();

    protected abstract void onFileClick(String path);

    protected abstract void initHeadView(ListView dirsListView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        g_strCurDir = getIntent().getStringExtra(SCAN_DIR);
        diskBoot = QlkDirs.getCardsPaths();

        setContentView(R.layout.activity_file_scanner);
        findViewAndSetListeners();

        guideAdapter = new GuideAdapter();
        guideListView.setAdapter(guideAdapter);

        //TODO:添加头部View
        initHeadView(dirsListView);
        dirsAdapter = new DirsAdapter();
        dirsListView.setAdapter(dirsAdapter);

        refresh();
    }

    private void refresh() {
        names.clear();
        guides.clear();
        guides.add("我的手机");
        if (TextUtils.isEmpty(g_strCurDir)) {
            touchHistory.clear(); // 只要path为空，它就应该清空
            for (String boot : diskBoot) {
                names.add(QlkDirs.getBootName(boot));
            }
        } else {
            String dir = g_strCurDir;
            for (String boot : diskBoot) {
                if (dir.startsWith(boot)) {
                    guides.add(QlkDirs.getBootName(boot));
                    dir = dir.replaceFirst(boot + "/?", "").trim();
                    if (!TextUtils.isEmpty(dir)) {
                        guides.addAll(Arrays.asList(dir.split(File.separator)));
                    }
                    break;
                }
            }
            names.add(GO_BACK_FLAG); // 返回上一级
            names.addAll(scanner.scan(g_strCurDir));
        }
        guideAdapter.notifyDataSetChanged();
        dirsAdapter.notifyDataSetChanged();
    }

    @Override
    public void findViewAndSetListeners() {
        guideListView = (HorizontalListView) findViewById(R.id.afs_lsv_guider);
        dirsListView = (ListView) findViewById(R.id.afs_lsv_dirs);

        guideListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    g_strCurDir = null; // 返回手机根目录
                    refresh();
                } else {
                    if (guides.size() > 2) {
                        goBack(guides.size() - 1 - position);
                    }
                }
            }
        });
        dirsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                int position = pos - 1;
                String name = names.get(position);
                String path = g_strCurDir;
                if (QlkDirs.getBootPath(name) != null) {
                    path = QlkDirs.getBootPath(name);
                } else {
                    if (position == 0) {
                        goBack();
                        return; // 注意此时应该return
                    } else {
                        path += File.separator + name;
                    }
                }
                if (path == null) return;
                // 判断点击应该去哪里
                if (new File(path).isDirectory()) {
                    g_strCurDir = path; // 只记录文件夹路径
                    touchHistory.push(dirsListView.getFirstVisiblePosition()); // 记录上一版ListView的显示位置
                    refresh();
                } else {
                    onFileClick(path);
                }
            }
        });

        dirsListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long
                    id) {
                int position = pos - 1;
                String name = names.get(position);
                String path = g_strCurDir + File.separator + name;
                File file = new File(path);
                // 处在“我的手机”界面；点击“...”；点击了文件夹夹（全部按单击处理）
                if (QlkDirs.getBootPath(name) != null || position == 0 || file.isDirectory()) {
                    return false; // 将事件传给onClick
                }

                // 长按文件，出dialog
                showLongClickDialog(path);
                return true; // 不再往下传
            }
        });
    }

    private void showLongClickDialog(final String path) {
        View v = getLayoutInflater().inflate(R.layout.dialog_listview, null);
        ListView listView = (ListView) v.findViewById(R.id.qlkListView);
        listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout
                .adapter_text1,
                new String[]{"打开", "发送", "删除", "取消"}));
        final Dialog dialog = GlobalDialog.showSelfDialog(QlkFileScannerActivity.this, v,
                DialogSize.Mini);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intentOpen = OpenFile.openFile(path);
                        if (intentOpen != null) {
                            startActivity(intentOpen);
                        }
                        break;

                    case 1:
                        Intent intentShare = new Intent();
                        intentShare.setAction(Intent.ACTION_SEND);
                        intentShare.setType("*/*");
                        intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
                        // value必须是Parcelable
                        startActivity(intentShare);
                        break;

                    case 2:
                        DialogInfo info = new DialogInfo();
                        info.title = "删除该文件？";
                        info.prompt = "位置：" + path + "\r\n大小："
                                + DataConvert.toSize(QlkFileScannerActivity.this, new File(path)
                                .length());
                        info.positiveBtnText = "删除";
                        info.positiveTask = new Runnable() {

                            @Override
                            public void run() {
                                FileUtil.deleteFile(path);
                                refresh();
                            }
                        };
                        GlobalDialog.showConfirmDialog(QlkFileScannerActivity.this, info);
                        break;

                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
    }

    /**
     * @return false 不能返回，到尽头了
     */
    private boolean goBack() {
        return goBack(1);
    }

    /**
     * @param steps 连续返回几次
     * @return false 不能返回，到尽头了
     */
    private boolean goBack(int steps) {
        if (TextUtils.isEmpty(g_strCurDir)) {
            return false;
        }
        for (String boot : diskBoot) {
            if (boot.equals(g_strCurDir)) {
                g_strCurDir = null;
                refresh();
                return true;
            }
        }
        if (steps <= 0) {
            return true;
        }
        int selection = 0;
        for (int i = 0; i < steps; i++) {
            g_strCurDir = g_strCurDir.substring(0, g_strCurDir.lastIndexOf(File.separator));
            if (!touchHistory.empty()) {
                // 按理这个判断是不应该加的，但现在设成static了，就有可能被系统回收，故检查之
                selection = touchHistory.pop();
            }
        }
        refresh();
        if (steps == 1) {
            dirsListView.setSelection(selection); // 单步操作可以，但多的话就得handler（但是会闪一下，效果很不好）
        } else {
            final int s = selection;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    dirsListView.setSelection(s);
                }
            });
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && goBack()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class GuideAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return guides.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(QlkFileScannerActivity.this, R.layout
                        .adapter_file_guider, null);
            }
            ((TextView) convertView.findViewById(R.id.afg_txt_name)).setText(guides.get(position));
            return convertView;
        }
    }

    private class DirsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return names.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.adapter_file_scanner, null);
            }
            ImageView pic = (ImageView) convertView.findViewById(R.id.afs_imv_pic);
            TextView name = ((TextView) convertView.findViewById(R.id.afs_txt_name));
            // 默认gone，使name居中
            convertView.findViewById(R.id.afs_lyt_datesize).setVisibility(View.GONE);
            name.setText(names.get(position));
            if (TextUtils.isEmpty(g_strCurDir)) {
                pic.setBackgroundResource(R.drawable.folder); // 应该是”内部存储“和”扩展存储卡“的相应图标
            } else {
                String path = g_strCurDir + File.separator + names.get(position);
                File file = new File(path);
                if ((path.endsWith(GO_BACK_FLAG) && position == 0)) {
                    pic.setBackgroundResource(R.drawable.folder); // 应该是返回上一级的图标
                } else if (file.isDirectory()) {
                    pic.setBackgroundResource(R.drawable.folder);
                } else {
                    // pic.setBackgroundResource(QlkMedia.isSameType2(path,
                    // getSupportTypes()) ? R.drawable.pdf
                    // : R.drawable.other_file);
                    pic.setBackgroundResource(R.drawable.other_file);
                    convertView.findViewById(R.id.afs_lyt_datesize).setVisibility(View.VISIBLE);
                    ((TextView) convertView.findViewById(R.id.afs_txt_date))
                            .setText(DataConvert.toDate(file.lastModified()));
                    ((TextView) convertView.findViewById(R.id.afs_txt_size))
                            .setText(DataConvert.toSize(getApplicationContext(), file.length()));
                }
            }
            return convertView;
        }
    }

}
