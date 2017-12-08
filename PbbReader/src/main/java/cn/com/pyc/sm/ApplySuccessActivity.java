package cn.com.pyc.sm;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.qlk.util.global.GlobalDialog;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.event.SmsResultEvent;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.conn.SmResult;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.media.PycImage;
import cn.com.pyc.media.PycMusic;
import cn.com.pyc.media.PycPdf;
import cn.com.pyc.media.PycVideo;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.reader.image.ImageReaderActivity;
import cn.com.pyc.reader.music.MusicPlayerActivity;
import cn.com.pyc.reader.video.VideoPlayerActivity;
import cn.com.pyc.utils.Constant;
import de.greenrobot.event.EventBus;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (申请已经提交界面)
 * @date 2016/12/12 11:08
 */
public class ApplySuccessActivity extends PbbBaseActivity {
    private TextView txt_title, txt_showInfo, txt_solder_name;
    private Button btn_reapply;
    SmInfo smInfo;

    private Dialog dialog;

    //接收通知
    public void onEventMainThread(SmsResultEvent event) {
        if (dialog != null) {
            dialog.dismiss();
        }
        SmResult sr = event.getResult();
        if (sr.succeed()) {
            startActivity(GlobalIntentKeys.reUseIntent(ApplySuccessActivity.this,
                    ApplyRightsActivity.class));
            finish();
        } else {
            GlobalToast.toastShort(getApplicationContext(), "请重试");
        }
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        setContentView(R.layout.activity_apply_success);
        ViewHelp.showAppTintStatusBar(this);
        EventBus.getDefault().register(this);

        txt_showInfo = (TextView) findViewById(R.id.txt_showInfo);
        TextView txt = (TextView) findViewById(R.id.aas_txt_info);
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_showInfo = (TextView) findViewById(R.id.txt_showInfo);
        txt_solder_name = (TextView) findViewById(R.id.tv_solder_name);
        btn_reapply = (Button) findViewById(R.id.btn_reapply);
        smInfo = (SmInfo) getIntent().getSerializableExtra(
                GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
        txt_solder_name.setText(smInfo.getNick().toString());

        String title = "申请已提交";
        // 1，判断如果needreapply ＝ 1 则在申请完成页面要显示 重新申请 按钮 并且title内容改变
        if (smInfo.getNeedReApply() == Constant.C_TRUE) {
            title = "激活失败";
            btn_reapply.setVisibility(View.VISIBLE);
        }
        // 2，拿到showInfo 信息
        String showInfo = smInfo.getShowInfo().trim();
        // 3，拿到字体颜色信息
        if (smInfo.getShowDiff() == Constant.C_TRUE) {
            txt_showInfo.setTextColor(Color.RED);
        }

        txt_title.setText(title);
        txt_showInfo.setText(showInfo);

        btn_reapply.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Runnable netReApply = new Runnable() {
                    @Override
                    public void run() {
                        SmResult sr = new SmConnect(getApplicationContext()).getApplyInfo(smInfo);
                        EventBus.getDefault().post(new SmsResultEvent(sr));
                    }
                };
                dialog = GlobalDialog.showNetProgress(ApplySuccessActivity.this);
                GlobalTask.executeBackground(netReApply);
                //GlobalTask.executeNetTask(ApplySuccessActivity.this, netReApply);
            }
        });

		/*-
         * 卖家联系方式
		 */
        String email = smInfo.getEmail();
        String phone = smInfo.getPhone();
        String qq = smInfo.getQq();
        String str = "";
        if (!TextUtils.isEmpty(qq)) {
            str += "Q  Q：" + qq;
        }
        if (!TextUtils.isEmpty(phone)) {
            str += "\n手机：" + phone;
        }
        if (!TextUtils.isEmpty(email)) {
            str += "\n邮箱：" + email;
        }
        txt.setText(str);
    }

    private void autoRead() {
        Intent jumpIntent = getIntent();
        final String smPath = getIntent().getStringExtra(
                GlobalIntentKeys.BUNDLE_DATA_PATH);
        String path = smPath.substring(0, smPath.length() - 4);
        if (PycImage.isSameType1(path)) {
            jumpIntent.setClass(ApplySuccessActivity.this,
                    ImageReaderActivity.class);
        } else if (PycPdf.isSameType1(path)) {
            jumpIntent.setClass(ApplySuccessActivity.this, com.artifex.mupdfdemo.MuPDFActivity.class);
        } else if (PycVideo.isSameType1(path)) {
            jumpIntent.setClass(ApplySuccessActivity.this,
                    VideoPlayerActivity.class);
        } else if (PycMusic.isSameType1(path)) {
            jumpIntent.setClass(ApplySuccessActivity.this,
                    MusicPlayerActivity.class);
        } else {
            jumpIntent = null;
        }

        startActivity(jumpIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
