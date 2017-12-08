package cn.com.pyc.sm;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qlk.util.global.GlobalDialog;
import com.qlk.util.global.GlobalObserver;
import com.qlk.util.global.GlobalTask;
import com.sz.mobilesdk.util.SZLog;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.event.SmsResultEvent;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.conn.SmResult;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.widget.WidgetTool;
import de.greenrobot.event.EventBus;

public class ApplyConfirmActivity extends PbbBaseActivity implements OnClickListener {
    private SmInfo smInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_confirm);
        findViewById(R.id.aac_btn_apply).setOnClickListener(this);
        EventBus.getDefault().register(this);

        smInfo = (SmInfo) getIntent().getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
        SZLog.d("ApplyConfirm", smInfo.toString());
        // QQ
        if (!TextUtils.isEmpty(smInfo.getQqBuyer())) {
            TextView qq = (TextView) findViewById(R.id.aac_txt_qq);
            qq.setVisibility(View.VISIBLE);
            qq.setText("Q Q：" + smInfo.getQqBuyer());
        }

        // phone
        if (!TextUtils.isEmpty(smInfo.getPhoneBuyer())) {
            TextView phone = (TextView) findViewById(R.id.aac_txt_phone);
            phone.setVisibility(View.VISIBLE);
            phone.setText("手机：" + smInfo.getPhoneBuyer());
        }

        // email
        if (!TextUtils.isEmpty(smInfo.getEmailBuyer())) {
            TextView email = (TextView) findViewById(R.id.aac_txt_email);
            email.setVisibility(View.VISIBLE);
            email.setText("邮箱：" + smInfo.getEmailBuyer());
        }
        // value1
        if (smInfo.getSelfMust() > 0) {
            TextView key1 = (TextView) findViewById(R.id.aac_txt_key1);
            final TextView value1 = (TextView) findViewById(R.id.aac_txt_value1);
            key1.setVisibility(View.VISIBLE);
            value1.setVisibility(View.VISIBLE);
            key1.setText(smInfo.getSelfDefineKey1() + "：");
            value1.setText(smInfo.getSelfDefineValue1());
            if (smInfo.isSelfDefineSecret1()) {
                ImageButton flashlight1 = (ImageButton) findViewById(R.id.aac_imb_flashlight1);
                flashlight1.setVisibility(View.VISIBLE);
                WidgetTool.changVisible(value1, false);
                flashlight1.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        v.performClick();    // 同时调用v的onClickListener
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                WidgetTool.changVisible(value1, true);
                                break;

                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP:
                                WidgetTool.changVisible(value1, false);
                                break;

                            default:
                                break;
                        }
                        return false;
                    }
                });
            }
        }

        // value2
        if (smInfo.getSelfMust() > 1) {
            TextView key2 = (TextView) findViewById(R.id.aac_txt_key2);
            final TextView value2 = (TextView) findViewById(R.id.aac_txt_value2);
            key2.setVisibility(View.VISIBLE);
            value2.setVisibility(View.VISIBLE);
            key2.setText(smInfo.getSelfDefineKey2() + "：");
            value2.setText(smInfo.getSelfDefineValue2());
            if (smInfo.isSelfDefineSecret2()) {
                ImageButton flashlight2 = (ImageButton) findViewById(R.id.aac_imb_flashlight2);
                flashlight2.setVisibility(View.VISIBLE);
                WidgetTool.changVisible(value2, false);
                flashlight2.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        v.performClick();    // 同时调用v的onClickListener
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                WidgetTool.changVisible(value2, true);
                                break;

                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP:
                                WidgetTool.changVisible(value2, false);
                                break;

                            default:
                                break;
                        }
                        return false;
                    }
                });
            }
        }
    }

    private Dialog dialog;

    public void onEventMainThread(SmsResultEvent event) {
        if (dialog != null) {
            dialog.dismiss();
        }
        SmResult sr = event.getResult();
        // 如果没有申请个，第一位是1，第七位是0
        // 如果已经申请过啦，仍然申请时，第一位会是0，第七位是1（值为64）
        if (sr.succeed() || sr.seventh()) {
            Intent intent = getIntent();
            if (sr.twelfth()) {
//						intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_AUTO_READ, true);
//					}
//					if (intent.getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_AUTO_READ, false))
//					{
                intent.setClass(ApplyConfirmActivity.this, SmReaderActivity.class);
            } else {
                intent.setClass(ApplyConfirmActivity.this, ApplySuccessActivity.class);
            }
            startActivity(intent);
            GlobalObserver.getGOb().postNotifyObservers(ObTag.Apply);    //
            // 因为可以返回到ApplyRightsActivity，所以此时它还没有finish
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        dialog = GlobalDialog.showNetProgress(this);
        GlobalTask.executeBackground(new Runnable() {
            @Override
            public void run() {
                SmResult sr = new SmConnect(getApplicationContext()).applyActivate(smInfo,
                        true);
                LogerEngine.info(getApplication(), "ApplyConfirm:申请激活", null);
                EventBus.getDefault().post(new SmsResultEvent(sr));
            }
        });
//        GlobalTask.executeNetTask(this, new Runnable() {
//            @Override
//            public void run() {
//                SmResult sr = new SmConnect(getApplicationContext()).applyActivate(smInfo,
//                        true);
//                LogerEngine.info(getApplication(), "申请激活", null);
//                // 如果没有申请个，第一位是1，第七位是0
//                // 如果已经申请过啦，仍然申请时，第一位会是0，第七位是1（值为64）
//                if (sr.succeed() || sr.seventh()) {
//                    Intent intent = getIntent();
//                    if (sr.twelfth()) {
////						intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_AUTO_READ, true);
////					}
////					if (intent.getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_AUTO_READ, false))
////					{
//                        intent.setClass(ApplyConfirmActivity.this, SmReaderActivity.class);
//                    } else {
//                        intent.setClass(ApplyConfirmActivity.this, ApplySuccessActivity.class);
//                    }
//                    startActivity(intent);
//                    GlobalObserver.getGOb().postNotifyObservers(ObTag.Apply);    //
//                    // 因为可以返回到ApplyRightsActivity，所以此时它还没有finish
//                    finish();
//
//                }
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
