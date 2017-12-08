package cn.com.pyc.reader;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.tool.DataConvert;
import com.sz.view.widget.WaterMaskView;

import java.util.ArrayList;
import java.util.Observable;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.media.MediaFile;


/**
 * 管辖范围：pdf、video、image、music 本类管理密文的加解密，倒计时控制以及水印信息的显示等
 */
public abstract class ReaderBaseActivity extends PbbBaseActivity {
    protected boolean isFromSm;
    protected boolean isCipher;

    protected SmInfo smInfo;        // 外发播放时有效
    protected String mCurPath;        // 当前播放路径
    protected int mCurPos;        // 在g_lstPaths中的位置（0～g_lstPaths.size()-1）
    protected ArrayList<String> mPaths = new ArrayList<String>();        // 所有待播放资源路径

    private CountDownTimer limitTimer;    // 倒计时，外发时有效

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE); // 禁止截屏

        // 初始化
        final Intent intent = getIntent();
        isFromSm = intent.getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, false);
        isCipher = intent.getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
        smInfo = (SmInfo) intent.getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
        mCurPath = intent.getStringExtra(GlobalIntentKeys.BUNDLE_DATA_PATH);
        ArrayList<String> paths = intent
                .getStringArrayListExtra(GlobalIntentKeys.BUNDLE_DATA_PATHS);
        if (paths == null || paths.size() == 0)    // 有时不需要它，但为了统一，加上
        {
            mPaths.add(mCurPath);
        } else {
            mPaths.addAll(paths);
        }
        mCurPos = mPaths.indexOf(mCurPath);
    }

    /**
     * 由子类调用
     *
     * @param waterView
     */
    @Deprecated
    protected void showWaterView(TextView waterView) {
        String strWater = "";
        SmInfo info = smInfo;
        if (!TextUtils.isEmpty(info.getQqBuyer())) {
//			strWater += "Q  Q：" + info.getQqBuyer() + " ";
            strWater += "" + info.getQqBuyer() + " ";
        }
        if (!TextUtils.isEmpty(info.getPhoneBuyer())) {
//			strWater += "手机：" + info.getPhoneBuyer() + " ";
            strWater += "" + info.getPhoneBuyer() + " ";
        }
        if (!TextUtils.isEmpty(info.getEmailBuyer())) {
//			strWater += "邮箱：" + info.getEmailBuyer() + " ";
            strWater += "" + info.getEmailBuyer() + " ";
        }

        if (!TextUtils.isEmpty(info.getSelfDefineValue1()) && !info.isSelfDefineSecret1()) {
//			strWater += info.getSelfDefineKey1() + "：" + info.getSelfDefineValue1() + "\n";
            strWater += "" + info.getSelfDefineValue1() + " ";
        }
        if (!TextUtils.isEmpty(info.getSelfDefineValue2()) && !info.isSelfDefineSecret2()) {
//			strWater += info.getSelfDefineKey2() + "：" + info.getSelfDefineValue2();
            strWater += "" + info.getSelfDefineValue2();
        }
        if (!TextUtils.isEmpty(strWater)) {
            waterView.setVisibility(View.VISIBLE);
            waterView.setText(strWater);
        }
    }

    protected View initWaterMaskView(float pageSizeX, float pageSizeY) {
        String strWater = "";
        SmInfo info = smInfo;
        if (!TextUtils.isEmpty(info.getQqBuyer())) {
            strWater += "" + info.getQqBuyer() + " ";
        }
        if (!TextUtils.isEmpty(info.getPhoneBuyer())) {
            strWater += "" + info.getPhoneBuyer() + " ";
        }
        if (!TextUtils.isEmpty(info.getEmailBuyer())) {
            strWater += "" + info.getEmailBuyer() + " ";
        }

        if (!TextUtils.isEmpty(info.getSelfDefineValue1()) && !info.isSelfDefineSecret1()) {
            strWater += "" + info.getSelfDefineValue1() + " ";
        }
        if (!TextUtils.isEmpty(info.getSelfDefineValue2()) && !info.isSelfDefineSecret2()) {
            strWater += "" + info.getSelfDefineValue2();
        }
        WaterMaskView maskView = new WaterMaskView(this);
        maskView.setMaxSize(pageSizeX, pageSizeY);
        maskView.setContentText(strWater);
        return maskView;
    }

    // 倒计时
    protected void showLimitView(final TextView limitView) {
        int limit = smInfo.getSingleOpenTime();
        if (limit <= 0) {
            return;
        }
        limitView.setVisibility(View.VISIBLE);
        limitView.setText(DataConvert.toTime(limit));
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        limitTimer = new CountDownTimer(limit * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                limitView.setText(DataConvert.toTime(millisUntilFinished / 1000));
                if (millisUntilFinished < 10 * 1000) {
                    limitView.setBackgroundResource(cn.com.pyc.pbb.reader.R.drawable.countdown_red);
                    vibrator.vibrate(50);
                }
            }

            public void onFinish() {
                finish();
                onTimerFinished();
            }
        }.start();
    }

    protected void onTimerFinished() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (limitTimer != null) {
            limitTimer.cancel();
        }
        if (isFromSm && smInfo.getPlayId() > 0) {
            GlobalTask.executeBackground(new Runnable() {
                @Override
                public void run() {
                    // 统计播放时间
                    new SmConnect(ReaderBaseActivity.this).sendPlayTime(smInfo);
                }
            });
        }
    }

    /**
     * 只有密文才可删除
     *
     * @param path
     */
    protected void delete(final String path) {
        MediaFile.deleteFiles(this, path);
    }

    protected void send(String path) {
    }

    protected void shareSmFile(String path) {
    }

    protected void decrypt(String path) {
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        switch ((ObTag) data) {
            case Make:
                finish();
                break;

            case Delete:
            case Decrypt:
                afterDeXXX();
                break;

            default:
                break;
        }
    }

    protected void afterDeXXX() {
    }

}
