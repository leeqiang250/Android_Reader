package cn.com.pyc.sm;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.qlk.util.global.GlobalObserver;
import com.qlk.util.tool.ColorText;
import com.qlk.util.tool.DataConvert;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SecurityUtil;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.sm.SmRule.FinishRule;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.web.WebActivity.WebPage;
import cn.com.pyc.xcoder.XCoder;

/**
 * @author 李巷阳
 * @Description: (制作完成, 发送界面)
 * @date 2016/12/2 11:48
 * 发送按钮
 */
public class MakeSmFileDoneActivity extends ExtraBaseActivity {
    private int SHARE_REQUST = 1010;
    private IWXAPI api;
    private static final String FreeRecord = "自由传播记录";
    private static final String PayRecord = "手动激活记录";
    private static final String FreeSpread = "请提醒Ta安装鹏保宝\n点击这里查询此文件状态";
    private static final String PaySpread = "你需要手动激活后Ta才能阅读\n激活时向鹏保宝支付费用\nTa只能在申请激活的设备上阅读";
    private Tencent mTencent;
    private String tokenString;

    private UserDao db = UserDao.getDB(this);
    private UserInfo userInfo;

    private Dialog dialog = null;
    private boolean dismiss;//是否出现提示框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sm_done);
        ViewHelp.showAppTintStatusBar(this);
        userInfo = db.getUserInfo();
        //wx49b46f184e65e4de微信API注册
        api = WXAPIFactory.createWXAPI(this, Constant.WEIXIN_ID,true);
        api.registerApp(Constant.WEIXIN_ID);

        //腾讯QQ
        mTencent = Tencent.createInstance(Constant.QQ_ID, this.getApplicationContext());
        initView();
    }

    private void initView() {
        // 分析条件信息
        final SmInfo info = (SmInfo) getIntent().getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);

        String text = "";
        switch (SmRule.getFinishRule(info)) {
            case FinishRule.FreeDateCount:
                text = "Ta " + getCount(info) + getSingle(info, true) + "\n" + getFreeDays(info);
                break;

            case FinishRule.FreeCount:
                text = "Ta " + getCount(info) + getSingle(info, true);
                break;

            case FinishRule.FreeDate:
                final String single = getSingle(info, false);
                if (TextUtils.isEmpty(single)) {
                    text = "Ta能看 " + info.getFreeLeftDays() + " 天";
                } else {
                    text = "Ta " + single + "\n" + getFreeDays(info);
                }
                break;

            case FinishRule.PayDateCount:
                text = "Ta " + getPayDays(info, true) + getCount(info);
//                Toast.makeText(MakeSmFileDoneActivity.this,"PayDateCount:"+ getPayDays(info, true) + getCount(info),Toast.LENGTH_LONG).show();
                break;

            case FinishRule.PayCount:
                text = "Ta " + getCount(info);
//                Toast.makeText(MakeSmFileDoneActivity.this,"PayCount:"+  getCount(info),Toast.LENGTH_LONG).show();
                break;
            case FinishRule.PayDate:
                text = "Ta " + getPayDays(info, false);
//                Toast.makeText(MakeSmFileDoneActivity.this,"PayDays:"+ getPayDays(info, false) ,Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }

        // 限制信息
        ColorText ct = new ColorText("0123456789无限-.", getResources().getColor(R.color.green));
        ((TextView) findViewById(R.id.asd_txt_limit)).setText(ct.getPartColor(text));

        // 文件名
        String fileName = info.getFileName().substring(0, info.getFileName().lastIndexOf("."));    // 去掉.pbb
        ((TextView) findViewById(R.id.apl_txt_name)).setText(fileName);

        // 链接地址
        TextView spread = (TextView) findViewById(R.id.asd_txt_spread);
        TextView record = (TextView) findViewById(R.id.asd_txt_record);
        spread.setText(info.isPayFile() ? PaySpread : FreeSpread);
        record.setText(info.isPayFile() ? PayRecord : FreeRecord);
        // 手动激活记录
        record.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebPage page = info.isPayFile() ? WebPage.PayRecord : WebPage.FreeRecord;
                startActivity(new Intent(MakeSmFileDoneActivity.this, WebActivity.class).putExtra(GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, page));
            }
        });

        // 发送按钮
        findViewById(R.id.asd_btn_send).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
//                com.tencent.mm/.ui.account.SimpleLoginUI
                intent.setType("*/*");//
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + getIntent().getStringExtra(GlobalIntentKeys.BUNDLE_DATA_PATH)));
                startActivityForResult(intent,SHARE_REQUST);

                //统计发送文件
                getUserToken(userInfo,R.id.asd_btn_send);
            }
        });

        findViewById(R.id.asd_txt_reader).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MakeSmFileDoneActivity.this,WebActivity.class).putExtra(GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.Reader));
            }
        });

        //发送链接
        findViewById(R.id.btn_sendurl).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPouUpWindow(view, dialog);
                //发送链接统计
                getUserToken(userInfo,R.id.btn_sendurl);
            }
        });


        findViewById(R.id.btn_qq_send).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle params = new Bundle();
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                params.putString(QQShare.SHARE_TO_QQ_TITLE, info.getFileName());
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  "要分享的摘要");
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "http://www.qq.com/news/1.html");
                mTencent.shareToQQ(MakeSmFileDoneActivity.this, params, new BaseUiListener()); //com.tencent.mobileqq/.activity.JumpActivity
            }
        });

    }
    private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void method(String tokenString,int id) {
        int type = -1;
        if (id == R.id.asd_btn_send){
            type = 1;
        }else if (id == R.id.btn_sendurl){
            type = 2;
        }
        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("appid", "28");
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            bundle.putString("appversion",versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        bundle.putString("logtype", "Info");
        bundle.putString("logtitle", type + "");
        TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String szImei = TelephonyMgr.getDeviceId();
        bundle.putString("devhdid", szImei);


        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization","Bearer " + tokenString);

        headers.put("Content-Type", "application/x-www-form-urlencoded");
//        headers.put("Content-Type", "application/json");

        GlobalHttp.post(Constant.UserSourceHost+"api/v1/pbblog",bundle ,headers, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String s) {
               /* try {
                    JSONObject object = new JSONObject(s);
                    String status = object.getString("Status");
                    Toast.makeText(MakeSmFileDoneActivity.this,"发送文件",Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
            @Override
            public void onError(Throwable throwable, boolean b) {
            }
            @Override
            public void onCancelled(CancelledException e) {
            }
            @Override
            public void onFinished() {
            }
        });
    }
    private void getUserToken(final UserInfo userInfo, final int id) {

        String userTokenUrl = Constant.UserTokenHost;

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "password");
        bundle.putString("username", userInfo.getUserName());
        if (!TextUtils.isEmpty(userInfo.getPsd())) {
            bundle.putString("password", userInfo.getPsd());
        } else {
            bundle.putString(
                    "password",
                    "n|"
                            + XCoder.getHttpEncryptText(userInfo
                            .getUserName()));
        }

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(
                "Authorization",
                "Basic "
                        + SecurityUtil.encryptBASE64(PhoneInfo.testID
                        + ":" + PhoneInfo.testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.post(userTokenUrl, bundle, headers,
                new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String arg0) {
                        // 解析Json
                        try {
                            JSONObject object = new JSONObject(arg0);
                            tokenString = (String) object
                                    .get("access_token");

                            method(tokenString,id);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinished() {
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        hideLoading();
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                    }
                });
    }
    private void showPouUpWindow(View view, final Dialog dialog) {
        View contentView = View.inflate(getApplicationContext(),
                R.layout.pop_am, null);
        TextView weixin = (TextView) contentView.findViewById(R.id.weixin);
        TextView qq = (TextView) contentView.findViewById(R.id.qq);
        TextView btn_copy = (TextView) contentView.findViewById(R.id.btn_copy);


        // 指定宽高wrap_content
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;


        // 参1 弹出框的布局view 参2 宽度 参3高度
        final PopupWindow popupWindow = new PopupWindow(contentView, width, height);
        qq.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Bundle params = new Bundle();

                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                params.putString(QQShare.SHARE_TO_QQ_TITLE, "如何打开.pbb文件");
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  "点我~点我~点我!");
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "http://www.pyc.com.cn/sj/applink.aspx");

                mTencent.shareToQQ(MakeSmFileDoneActivity.this, params, new BaseUiListener()); //com.tencent.mobileqq/.activity.JumpActivity
                popupWindow.dismiss();
                if (dialog != null) {
                    dialog.cancel();
                }
            }
        });
        weixin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = "http://www.pyc.com.cn/sj/applink.aspx";
                WXMediaMessage msg = new WXMediaMessage(webpage);
                msg.title = "如何打开.pbb文件";
                msg.description = "点我~点我~点我!";
                Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                msg.thumbData = bmpToByteArray(thumb,true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneSession;
                api.sendReq(req);
                popupWindow.dismiss();
                if (dialog != null) {
                    dialog.cancel();
                }
            }
        });

        btn_copy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText("http://www.pyc.com.cn/sj/applink.aspx");

                Toast.makeText(MakeSmFileDoneActivity.this,"APP链接复制成功",Toast.LENGTH_SHORT).show();

                popupWindow.dismiss();
                if (dialog != null) {
                    dialog.cancel();
                }
            }
        });
        popupWindow.setFocusable(true);// 设置有焦点
        // 设置背景颜色 如果要点击其它区域或者返回键消失 就必须设置 如果不想要颜色 设置透明
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupWindow.setAnimationStyle(android.R.anim.fade_in);
        // 抛锚 参1 显示在指定的view的下面
//         popupWindow.showAsDropDown(view);
        // 参1 锚点 参2 x轴偏移量 参3 y轴偏移量
        popupWindow.showAsDropDown(view, 50, -40);
        // 参1 传activity里的任意一个view 参2 位置 默认上下居中
        // popupWindow.showAtLocation(tv, Gravity.RIGHT | Gravity.TOP, 0, 50);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
    private class BaseUiListener implements IUiListener {

        protected void doComplete(JSONObject values) {
        }

        @Override
        public void onComplete(Object o) {

        }

        @Override
        public void onError(UiError e) {
//            showResult("onError:", "code:" + e.errorCode + ", msg:"
//                    + e.errorMessage + ", detail:" + e.errorDetail);
        }
        @Override
        public void onCancel() {
//            showResult("onCancel", "");
        }
    }
    //qq分享回调
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        dismiss = (Boolean) SPUtil.get("dismiss", true);
        if (!dismiss){
            return;
        }

        if (null != mTencent&&SHARE_REQUST == requestCode){

            mTencent.onActivityResult(requestCode, resultCode, data);
            //弹出对话框
            showDialogSend2();
        }
    }

    private void showDialogSend2() {
        if (dialog != null) {
            dialog.show();
            return;
        }
        dialog = new Dialog(MakeSmFileDoneActivity.this, R.style.share_url);
        View v = LayoutInflater.from(MakeSmFileDoneActivity.this).inflate(R.layout.dialog_sendurl,
                null);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(v);
        dialog.show();
        TextView check = (TextView) v.findViewById(R.id.btn_sure);
        TextView exit = (TextView) v.findViewById(R.id.btn_cancel);
        check.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPouUpWindow(v,dialog);
                //发送链接统计
                getUserToken(userInfo,R.id.btn_sendurl);
            }
        });
        exit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //以后都不在出现这个提示框
                dismiss = false;

                SPUtil.save("dismiss",dismiss);
                dialog.cancel();
            }
        });
    }

    private String getCount(SmInfo info) {

        if (info.getOpenCount() == 0){
            return "能看 无限 次  ";
        }
        return "能看 " + info.getOpenCount() + " 次  ";
    }

    private String getSingle(SmInfo info, boolean needCommon) {
        String str = "";
        if (info.getSingleOpenTime() > 0) {
            if (needCommon) {
                str += ",";
            }
            str += "每次能看 " + DataConvert.toTime(info.getSingleOpenTime());
        }
        return str;
    }

    private String getFreeDays(SmInfo info) {
        long leftDays = info.getFreeLeftDays();
        String freeDays = leftDays > 1 ? "自 " + convertDateType(info.getStartTime()) + "起，有效期 " + leftDays + " 天" : convertDateType(info.getStartTime()) + "当天有效";
        return freeDays;
    }

    private static String convertDateType(String s) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd", Locale.CHINA);
            Date date = sdf.parse(s);

            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年mm月dd日", Locale.CHINA);
            String result = sdf2.format(date);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getPayDays(SmInfo info, boolean hasCount) {
        String str = null;
        if (hasCount) {

            //向服务器传 0 ，代表 无限天
            if (info.getDays()== 0){
                str =  "无限 天内";
            }
            if (info.getDays() > 0) {
                str = info.getDays() + " 天内";
            }
            if (info.getYears() > 0) {
                str = info.getYears() + " 年内";
            }
        } else {
            //向服务器传 0 ，代表 无限天
            if (info.getDays() == 0){
                str = "能看 无限 天";
            }
            if (info.getDays() > 0) {
                str = "能看 " + info.getDays() + " 天";
            }
            if (info.getYears() > 0) {
                str = "能看 " + info.getYears() + " 年";
            }
        }
        return str;
    }

    @Override
    public void finish() {
        super.finish();
        GlobalObserver.getGOb().postNotifyObservers(ObTag.Make);
        Intent intent = new Intent(this, SendActivity2.class);
        intent.putExtra(SendActivity2.EXPAND_GROUP, 0);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
