package cn.com.pyc.update;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qlk.util.base.BaseApplication;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.tool.Util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.utils.Dirs;

/**
 * @author 熊 apk在线更新
 * @Description: (正在下载)
 * @date 2016/12/12 11:08
 */
public class UpdateActivity extends PbbBaseActivity {
    private TextView g_txtPercent;
    private TextView g_txtSize;
    private ProgressBar g_pbProgress;
    private boolean cancelDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false); // 点击框以外地方不消失

        setContentView(R.layout.activity_update);

        g_pbProgress = (ProgressBar) findViewById(R.id.au_pb_progress);
        g_txtPercent = (TextView) findViewById(R.id.au_txt_percent);
        g_txtSize = (TextView) findViewById(R.id.au_txt_size);
        findViewById(R.id.au_btn_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDownload = true;
            }
        });

        String apkPath = Dirs.getPrivacyDir(Dirs.getDefaultBoot());
        File apkFile = new File(apkPath);
        if (!apkFile.exists())
            apkFile.mkdirs();
        if (FileUtil.fileCanExecute(apkPath)) {
            new DownloadApk().execute();
        } else {
            GlobalToast.toastShort(this, "目标路径无效，无法更新安装包！");
        }
    }

    private class DownloadApk extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            FileOutputStream fos = null;
            HttpURLConnection conn = null;
            String localApkPath = Dirs.getPrivacyDir(Dirs.getDefaultBoot()) + File.separator
                    + UpdateTool.getApk_Version() + ".apk";
            try {
                URL url = new URL(UpdateTool.getApkDownUrl());
                conn = (HttpURLConnection) url.openConnection();
                final int total = conn.getContentLength();
                if (isAlreadyExist(total, localApkPath)) {
                    GlobalToast.toastShort(UpdateActivity.this, "安装包已存在");
                    return localApkPath;
                }

                InputStream is = conn.getInputStream();
                fos = new FileOutputStream(localApkPath);
                byte[] buf = new byte[10 * 1024];
                int real;
                int already = 0;
                while ((real = is.read(buf)) > 0) {
                    if (cancelDownload) {
                        FileUtil.deleteFile(localApkPath);
                        localApkPath = null;
                        break;
                    }
                    fos.write(buf, 0, real);
                    already += real;
                    publishProgress(already, total);
                }
                is.close();    // 应该不用关闭，conn关闭时它就自动关了
            } catch (Exception e) {
                e.printStackTrace();
                localApkPath = null;
            } finally {
                IOUtil.close(null, fos);

                if (conn != null) {
                    conn.disconnect();
                }
            }
            System.out.println("|||" + localApkPath);
            return localApkPath;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                System.out.println("安装------------------------");
                installApk(result);
            }

            finish();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            g_pbProgress.setMax(values[1]);
            g_txtSize.setText(DataConvert.toSize(UpdateActivity.this, values[1]));
            g_pbProgress.setProgress(values[0]);
            g_txtPercent.setText(DataConvert.toSize(UpdateActivity.this, values[0]));
        }
    }

    /**
     * 只要文件大小和网络的大小一样，就判断为一个文件
     *
     * @param netLen
     * @param to
     * @return
     */
    private static boolean isAlreadyExist(int netLen, String to) {
        File file = new File(to);
        if (file.exists()) {
            return file.length() == netLen;
        }
        return false;
    }

    private void installApk(String apkPath) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("file://" + apkPath),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        GlobalToast.toastShort(this, "准备安装程序");
        ((BaseApplication) getApplication()).safeExit();
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        if (data.equals(ObTag.SdCardOff)) {
            String to = getIntent().getStringExtra(GlobalIntentKeys.BUNDLE_DATA_PATH);
            if (!FileUtil.fileCanExecute(to)) {
                GlobalToast.toastShort(this, "存储卡不可用，取消升级");
                cancelDownload = true;
            }
        }
    }

}
