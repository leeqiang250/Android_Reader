package cn.com.pyc.pcshare;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.qlk.util.base.BaseActivity;
import com.sz.accesspc.intern.$$;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.view.dialog.ProgressBarDialog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pcshare.help.PCFileHelp;
import cn.com.pyc.receive.ReceiveActivity;
import cn.com.pyc.sm.SmReaderActivity;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * Created by hudq on 2016/12/15.
 */
public class DirectoryHomeActivity extends BaseActivity implements DirectoryFragment
        .FileClickListener {

    private static final String TAG = "DirectoryHomeUI";

    private String currentIP;
    private String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pc_activity_directory_home);
        currentIP = getIntent().getStringExtra("ip");
        currentPath = getIntent().getStringExtra("path");

        if (getWindow() != null)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getFragmentManager().beginTransaction()
                .add(R.id.container, DirectoryFragment.getInstance(currentIP, currentPath))
                .commit();
    }


    private void addFragmentToBackStack(String ip, String path) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.replace(R.id.container, DirectoryFragment.getInstance(ip, path))
                //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFileClicked(final SmbFile file) {
        if (file == null) return;
        String canonicalPath = file.getCanonicalPath();
        Log.d(TAG, "canonicalPath: " + canonicalPath);
        try {
            if (file.isHidden()) return;
            if (file.isDirectory()) {
                addFragmentToBackStack(currentIP, canonicalPath);
                currentPath = canonicalPath;
            } else if (file.isFile()) {
                downloadRunning = false;
                final String filePath = PathUtil.getSDCard() + PCFileHelp.getPCShareOffset() + file
                        .getName();
                if (new File(filePath).exists()) {
                    startPlay(this, filePath);
                } else {
                    proDialog = new ProgressBarDialog(this);
                    proDialog.setTitle("请稍候...");
                    String fileName = file.getName();
                    String endName = fileName.endsWith(".pbb") ? fileName.substring(0,
                            fileName.length() - 4) : fileName;
                    proDialog.setMessage("文件正在迁移至目录:\nSDCard" + PCFileHelp.getPCShareOffset() +
                            endName);
                    proDialog.setMax(100);
                    proDialog.setTotalSize(file.length());
                    proDialog.show();
                    proDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            downloadRunning = false;
                            try {
                                File localFile = new File(filePath);
                                if (localFile.length() < file.length()) {
                                    SZLog.d("close dialog", "clear~" + localFile.length());
                                    localFile.delete();
                                }
                            } catch (SmbException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    $$.execute(new MyRunnable(file, filePath));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private class MyRunnable implements Runnable {
        private SmbFile file;
        private String filePath;

        private MyRunnable(SmbFile file, String filePath) {
            this.file = file;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            save2Local(file, filePath);
        }
    }

    private void save2Local(SmbFile file, String filePath) {
        String dir = PathUtil.getSDCard() + PCFileHelp.getPCShareOffset();
        new File(dir).mkdirs();
        File localFile = new File(filePath);
        InputStream in = null;
        OutputStream out = null;
        downloadRunning = true;
        try {
            long fileSize = file.length();
            int length = -1;
            long currentSize = 0;
            in = new BufferedInputStream(new SmbFileInputStream(file));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[2048];
            while (downloadRunning && (length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
                currentSize += length;
                int progress = (int) (currentSize * 100 / fileSize);
                progress = (progress == 0) ? 1 : progress;
                //mHandler.removeMessages(MSG_PROGRESS);
                Message msg = mHandler.obtainMessage(MSG_PROGRESS, progress, -1);
                msg.obj = currentSize;
                mHandler.sendMessage(msg);
            }
            if (currentSize >= fileSize) {
                if (mHandler.hasMessages(MSG_PROGRESS))
                    mHandler.removeMessages(MSG_PROGRESS);
                Message msg = mHandler.obtainMessage(MSG_COMPLETE, localFile.getAbsolutePath());
                mHandler.sendMessageDelayed(msg, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean downloadRunning = false;
    private ProgressBarDialog proDialog;
    private Handler mHandler = new ExecHandler(this);
    private static final int MSG_PROGRESS = 0x100;
    private static final int MSG_COMPLETE = 0x102;

    private static class ExecHandler extends Handler {
        private WeakReference<DirectoryHomeActivity> reference;

        private ExecHandler(DirectoryHomeActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DirectoryHomeActivity activity = reference.get();
            if (activity == null) return;
            if (activity.isFinishing()) return;
            if (msg.what == DirectoryHomeActivity.MSG_PROGRESS) {
                int progress = msg.arg1;
                long currentSize = (long) msg.obj;
                if (activity.proDialog != null) {
                    activity.proDialog.setProgress(progress);
                    activity.proDialog.setCurrentSize(currentSize);
                }
            } else if (msg.what == DirectoryHomeActivity.MSG_COMPLETE) {
                if (activity.proDialog != null) {
                    activity.proDialog.setProgress(100);
                    activity.proDialog.dismiss();
                }
                activity.downloadRunning = false;
                String filePath = ((String) msg.obj);
                activity.startPlay(activity, filePath);
            }
        }
    }

    private void startPlay(Activity context, String filePath) {
        Intent intent = new Intent(context, SmReaderActivity.class);
        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, filePath);
        context.startActivityForResult(intent, ReceiveActivity.RESULT_READ);
    }
}
