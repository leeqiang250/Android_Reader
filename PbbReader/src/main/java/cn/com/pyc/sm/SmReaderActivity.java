package cn.com.pyc.sm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.tool.Util.IOUtil;
import com.qlk.util.tool.Util.NetUtil;
import com.qlk.util.tool.Util.ScreenUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.com.pyc.base.LOGConfig;
import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.conn.SmResult;
import cn.com.pyc.conn.SmResult.OpenFailure;
import cn.com.pyc.db.AdDao;
import cn.com.pyc.db.sm.ReceiveDao;
import cn.com.pyc.db.sm.SmDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LogerHelp;
import cn.com.pyc.media.MyBitmapFactory;
import cn.com.pyc.media.PycImage;
import cn.com.pyc.media.PycMusic;
import cn.com.pyc.media.PycPdf;
import cn.com.pyc.media.PycSm;
import cn.com.pyc.media.PycVideo;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.reader.image.ImageReaderActivity;
import cn.com.pyc.reader.music.MusicPlayerActivity;
import cn.com.pyc.reader.video.VideoPlayerActivity;
import cn.com.pyc.update.UpdateActivity;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.xcoder.XCoder;
import cn.com.pyc.xcoder.XCoderResult;

/**
 * 只用传入路径就可以了，其他的不用传
 *
 * @author QiLiKing 2014-12-10
 */

/**
 * @author 李巷阳
 * @Description: (点击阅读跳转此界面)
 * @date 2016/12/9 17:54
 */
public class SmReaderActivity extends PbbBaseActivity {

    private static final String TAG = "SmReaderUI";
    //	public static int twelfth_appear_count = 0;	// 第十二位为1出现的次数

    // 上传的广告规格，但显示的时候就不是了，会等比例缩放
    // private static final int AD_WIDTH = 640;
    // private static final int AD_HEIGHT = 960;
    private boolean isFromAutoRead = false;

    private static final int SHOW_AD = 0;
    private static final int SHOW_AD_FINISHD = 1;

    private boolean isAdFinished = false;
    private Intent jumpIntent;        // null的时候，直接finish即可
    private String errorReason;

    private ReadSmTask readSmTask;
    private Bitmap adBitmap = null;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        isFromAutoRead = getIntent().getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_AUTO_READ, false);
        //_SysoXXX.message("isFromAutoRead    " + isFromAutoRead);

        setFinishOnTouchOutside(false); // 点击框以外地方不消失
        setContentView(R.layout.activity_reader_sm);
        String smPath = Util.getPathFromIntent(this, getIntent(), GlobalIntentKeys
                .BUNDLE_DATA_PATH);
        SZLog.i(TAG, "smPath: " + smPath);
        if (smPath == null) {
            GlobalToast.toastShort(this, "文件路径解析错误");
            finish();
            return;
        }
        if (smPath.contains(Dirs.DIR_PRIVACY))    // 第三方软件打开（不是微信）
        {
            // 需求：制作者打开自己制作的文件时，跳转到发送列表指定条目（上面的判断就表示操作者是制作人本身）
            GlobalToast.toastShort(this, "您是制作者，不能打开此文件！");
            finish();
            return;
        }

        if (PycSm.isSameType1(smPath)) {
            if (!GlobalData.Sm.instance(this).getCopyPaths(false).contains(smPath)) {
                //可能是来自文件管理器的
                GlobalData.Sm.instance(this).insertToSysDBAsync(smPath);
                GlobalData.Sm.instance(this).add(0, smPath, true);
            }
            cancelReadTask();
            if (readSmTask == null) {
                readSmTask = new ReadSmTask();

                //阅读文件的标识
                SPUtil.save("read",readSmTask.toString());
            }
            readSmTask.execute(smPath);
        } else {
            // 比如用户在复制文件时可能会出现/.../a.jpg(1).pbb，这类文件鹏保宝不处理
            Toast.makeText(this, "不支持文件格式" + FileUtil.getFileName(smPath), Toast.LENGTH_LONG)
                    .show();
            finish();
        }
    }

    private void cancelReadTask() {
        if (readSmTask == null) return;

        if (readSmTask.getStatus() == AsyncTask.Status.RUNNING) {
            readSmTask.cancel(true);
            readSmTask = null;
        }
    }

    private class ReadSmTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            if (isCancelled()) return "-1";

            final String smPath = params[0];
            XCoderResult xr = XCoder.analysisSmFile(smPath);
            if (xr.succeed()) {
                final SmInfo smInfo = xr.getSmInfo();
                if (!isFromAutoRead) {
                    prepairShowAD(smInfo);
                } else {
                    isAdFinished = true;
                }

                // 是否从SecurityCodeActivity跳过来的
                if (getIntent().getBooleanExtra(SecurityCodeActivity.FROM_SECURITY, false)) {
                    String securityCode = getIntent().getStringExtra(SecurityCodeActivity
                            .SECURITY_CODE);
                    String msgId = getIntent().getStringExtra(SecurityCodeActivity.MSG_ID);
                    smInfo.setSecurityCode(securityCode);
                    smInfo.setMsgId(msgId);
                }
                SmResult sr = new SmConnect(getApplicationContext()).openFile(smInfo, true, false);
                if (sr.valid()) {
                    jumpIntent = new Intent();
                    jumpIntent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, true);
                    jumpIntent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, smPath);
                    jumpIntent.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO, smInfo);
                    if (sr.canOpen()) {
                        setResult(RESULT_OK, jumpIntent);
                        String path = smPath.substring(0, smPath.length() - 4);
                        if (PycImage.isSameType1(path)) {
                            jumpIntent.setClass(SmReaderActivity.this, ImageReaderActivity.class);
                        } else if (PycPdf.isSameType1(path)) {
                            jumpIntent.setClass(SmReaderActivity.this, com.artifex.mupdfdemo
                                    .MuPDFActivity.class);
                        } else if (PycVideo.isSameType1(path)) {
                            jumpIntent.setClass(SmReaderActivity.this, VideoPlayerActivity.class);
                        } else if (PycMusic.isSameType1(path)) {
                            jumpIntent.setClass(SmReaderActivity.this, MusicPlayerActivity.class);
                        } else {
                            jumpIntent = null;
                        }
                    } else {
                        OpenFailure failure = sr.whyOpenFailed();
                        //_SysoXXX.message("failure----------：" + failure.name());
                        switch (failure) {
                            // 需要离线验证
                            case NeedVerify:
                                jumpIntent.setClass(SmReaderActivity.this, VerifyOfflineActivity
                                        .class);
                                break;
                            // 需要验证手机
                            case NeedPhone:
                                jumpIntent.setClass(SmReaderActivity.this, SecurityCodeActivity
                                        .class);
                                break;
                            // 离线文件移动到其他设备上了
                            case DeviceChanged:
                                jumpIntent.setClass(SmReaderActivity.this, DeviceChangedActivity
                                        .class);
                                break;
                            // 没有写权限
                            case NeedApply:
                                jumpIntent.setClass(SmReaderActivity.this, ApplyRightsActivity
                                        .class);
                                break;
                            case AutoActive:
                                if (!isFromAutoRead) {
                                    jumpIntent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_AUTO_READ,
                                            true);
                                    jumpIntent.setClass(SmReaderActivity.this, SmReaderActivity
                                            .class);
                                } else {
                                    jumpIntent.setClass(SmReaderActivity.this,
                                            ApplySuccessActivity.class);
                                }
                                //_SysoXXX.message("自动激活－－－－－" + isFromAutoRead);
                                break;
                            case WaitForActive:
                                jumpIntent.setClass(SmReaderActivity.this, ApplySuccessActivity
                                        .class);
                                break;

                            case NeedUpdate:
                                jumpIntent.setClass(SmReaderActivity.this, UpdateActivity.class);
                                break;

                            default:
                                jumpIntent = null;
                                break;
                        }
                        errorReason = sr.getFailureReason();
                    }
                } else {
                    errorReason = sr.getFailureReason();
                }
            } else {
                isAdFinished = true;    // 此时没有执行prepairShowAD()，就不会改变isAdFinished
                errorReason = xr.getFailureReason();

                ExtraParams ep = LOGConfig.getBaseExtraParams();
                ep.file_name = LogerHelp.getFileName();
                ep.lines = LogerHelp.getLineNumber();
                LogerEngine.debug(getApplication(), "解析文件失败：" + errorReason, ep);
            }

            tryToJump();    //某三星平板，第二次（自动激活）进入时，死活不会执行onPostExecute，故放在这里

            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //if("-1".equals(result)) return;

            //System.out.println("ReaderTask onPostExecute");
            //tryToJump();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            GlobalToast.toastShort(this, "正在努力进行中，请勿取消!");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void tryToJump() {
        if (isAdFinished) {
            //System.out.println("开始跳转tryToJump");
            if (!TextUtils.isEmpty(errorReason)) {
                if (jumpIntent == null) {
                    GlobalToast.toastShort(this, errorReason);
                    finish();
                }
            }
            if (jumpIntent != null) {
                if (jumpIntent.getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_AUTO_READ, false)) {
                    //_SysoXXX.message("启动自动激活");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(jumpIntent);
                            finish();
                        }
                    }, 2000);
                } else {
                    //_SysoXXX.message("启动Actiivty...");
                    startActivity(jumpIntent);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelReadTask();
        handler.removeCallbacksAndMessages(null);
        if (adBitmap != null) {
            if (!adBitmap.isRecycled()) adBitmap.recycle();
            adBitmap = null;
        }
        //		if (getIntent().getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_AUTO_READ, false))
        //		{
        //			twelfth_appear_count = 0;	// 第二次进入该类时，为true。那么在退出时要清零，以便下次正常打开
        //		}
    }

	/*-******************************************
     * 广告
	 *******************************************/

    private String AD_URL = Constant.WebHost + "/myspace/deployadvertshow.aspx?fid=";

    private void prepairShowAD(SmInfo smInfo) {
        SmInfo infoTemp = new SmInfo();
        infoTemp.setFid(smInfo.getFid());
        SmDao.getInstance(this, true).query(infoTemp);
        String picName = new AdDao().query(infoTemp.getUid());
        // if (smInfo.isOfflineFile())
        // {
        // showAD(picName);
        // return;
        // }

        if (!NetUtil.isNetInUse(getApplicationContext())) {
            // isAdFinished = true; // 允许退出（前提是它之后一定会调用tryToJump()）
            showAD(picName);
            return;
        }

        isAdFinished = false;
        HttpURLConnection conn = null;
        try {
            AD_URL += smInfo.getFid();
            URL url = new URL(AD_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                // 这里的硬编码全是按照html解析的，网页变，这里也得变
                if (line.contains("imgicon")) {
                    String[] lines = line.split(" ");
                    for (String lineTemp : lines) {
                        if (lineTemp.startsWith("src")) {
                            String imgUrl = lineTemp.substring(5, lineTemp.length() - 1);
                            String netName = FileUtil.getFileName(imgUrl);
                            if (netName.equals("pbbad.jpg")) {
                                break;    // 展示默认广告
                            }

                            // 检测是否需要更新
                            String uid = netName.split("_")[0];
                            if (uid.equals(infoTemp.getUid()) && netName.equals(picName)) {
                                // 不需要更新
                                showAD(picName);
                                return;    // 展示正常广告
                            } else {
                                // 更新
                                String picPath = Dirs.getAdDir() + File.separator + netName;
                                // 注意，是netName了
                                if (downlaodAdPic(getApplicationContext(), imgUrl, picPath)) {
                                    new AdDao().updateOrInsert(uid, netName);
                                    ReceiveDao.getInstance(this).updateOrInsertUid(smInfo.getFid
                                            (), uid);
                                    showAD(netName);
                                    return;    // 展示正常广告
                                }
                            }
                            break;    // 展示默认广告
                        }
                    }
                }
            }
            // 联网获取图片失败，显示默认
            showAD(picName);
        } catch (Exception e) {
            e.printStackTrace();
            showAD(picName);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void showAD(String picName) {
        //Bitmap bm = null;
        final DisplayMetrics dm = ScreenUtil.getScreenRect(this);
        // 有卖家广告记录
        if (!TextUtils.isEmpty(picName)) {
            String picPath = Dirs.getAdDir() + File.separator + picName;
            if (new File(picPath).exists())    // 卖家广告存在
            {
                adBitmap = MyBitmapFactory.getBitmap(picPath, dm.widthPixels, dm.heightPixels);
            }
        }

        if (adBitmap == null) {
            adBitmap = MyBitmapFactory.getBitmap(this, R.drawable.imv_ad_default, dm.widthPixels,
                    dm.heightPixels);
        }
        Message msg = Message.obtain();
        msg.what = SHOW_AD;
        msg.obj = adBitmap;
        handler.sendMessage(msg);
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_AD:
                    AdView ad = (AdView) findViewById(R.id.ars_imv_ad);
                    ad.setVisibility(View.VISIBLE);
                    ad.setBitmap((Bitmap) msg.obj);
                    sendEmptyMessageDelayed(SHOW_AD_FINISHD, 3000);
                    break;

                case SHOW_AD_FINISHD:
                    //System.out.println("广告加在结束，跳转！！");
                    isAdFinished = true;
                    tryToJump();
                    break;

                default:
                    break;
            }
        }
    };

    private boolean downlaodAdPic(Context context, String srcUrl, String to) {
        HttpURLConnection conn = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(srcUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            InputStream is = conn.getInputStream();
            fos = new FileOutputStream(to);
            byte[] buf = new byte[10 * 1024];
            int real;
            while ((real = is.read(buf)) > 0) {
                fos.write(buf, 0, real);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            FileUtil.deleteFile(to);
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            IOUtil.close(null, fos);
        }
    }

}
