package cn.com.pyc.sm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.qlk.util.event.PathsEvent;
import com.qlk.util.global.GlobalObserver;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util;
import com.qlk.util.tool.Util.FileUtil;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.HashMap;
import java.util.Random;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.conn.SmResult;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.bean.event.RefreshModifyPowerEvent;
import cn.com.pyc.update.UpdateActivity;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCodeView;
import cn.com.pyc.xcoder.XCodeView.XCodeType;
import cn.com.pyc.xcoder.XCodeView.XCodeViewListener;
import cn.com.pyc.xcoder.XCoder;
import cn.com.pyc.xcoder.XCoder.ReturnInfo;
import cn.com.pyc.xcoder.XCoderResult;
import de.greenrobot.event.EventBus;

/**
 * @author 李巷阳
 * @Description: (发送给Ta吧 界面)
 * @date 2016/11/30 15:11
 * <p>
 * 通过 Util.getPathFromIntent(this, intent, GlobalIntentKeys.BUNDLE_DATA_PATH) 获取要制作的文件的path
 */
public class MakeSmFileActivity extends ExtraBaseActivity {
    private static final int STATE_READY = 0;
    private static final int STATE_MAKE = 1;
    private static final int STATE_FINISH = 2;

    private UserDao db = UserDao.getDB(this);
    private UserInfo userInfo;
    private String tokenString;

    private SmInfo smInfo;
    private boolean isPayMode;
    private boolean isChangeLimit;
    private String g_strPath;    // 原文件
    private String g_strSmPath;    // 制作后的文件
    private View g_lytTop;
    private XCodeView codeView;
    private String s;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setFinishOnTouchOutside(false); // 点击框以外地方不消失
        setContentView(R.layout.activity_sm_make);
        userInfo = db.getUserInfo();
        init_value();
        init_view();

    }

    private void init_value() {
        isPayMode = getIntent().getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_PAY_MODE, false);
        isChangeLimit = getIntent().getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_CHANGE_LIMIT,
                false);
        smInfo = (SmInfo) getIntent().getSerializableExtra(GlobalIntentKeys
                .BUNDLE_OBJECT_SM_INFO);// 获取制作文件的条件
        g_strPath = Util.getPathFromIntent(this, getIntent(), GlobalIntentKeys.BUNDLE_DATA_PATH);
        // 获取制作文件的path

        UserInfo userInfo = UserDao.getDB(this).getUserInfo();// 获取user信息
        smInfo.setUserName(userInfo.getUserName());// 获取用户名
        smInfo.setNick(userInfo.getNick());// 获取用户昵称
        smInfo.setFileName(FileUtil.getFileName(g_strPath) + ".pbb");// 获取制作完成后的文件名
        smInfo.setPayFile(isPayMode ? Constant.C_TRUE : Constant.C_FALSE);
        // 制作
        if (isChangeLimit) {
            ((TextView) findViewById(R.id.asm_txt_prompt)).setText("正在修改限制条件");
            changeLimit(g_strPath);
        } else {
            handler.sendEmptyMessage(STATE_READY);    // 保持风格一致
        }
    }

    private void init_view() {
        g_lytTop = findViewById(R.id.asm_lyt_top);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STATE_READY:
                    makeState1();
                    break;
                // 开始制作
                case STATE_MAKE:
                    makeState2();
                    // 正在制作的提示框
                    setContentView(codeView.getShowView());
                    g_lytTop.setVisibility(View.INVISIBLE);
                    break;

                case STATE_FINISH:
                    makeState3();
                    setContentView(R.layout.activity_sm_make);
                    break;

                default:
                    break;
            }
        }

    };

    private void makeState1() {
        GlobalTask.executeBackground(new Runnable() {

            @Override
            public void run() {
                getUserToken(smInfo);
//                SmResult sr = new SmConnect(MakeSmFileActivity.this).makeFile(smInfo,
// isPayMode);    // sr1
//                if (sr.succeed()) {
//                    // 设定编码key,产生一个16位随机数
//                    smInfo.setEncodeKey(createRandomKey_16());
//                    // 设定SessionKey,产生一个16位随机数
//                    smInfo.setSessionKey(createRandomKey_16());
//                    // 设定fid
//                    smInfo.setFid(sr.getSmInfo().getFid());
//                    // 其实只有制作payFile时才有效
//                    smInfo.setOrderNo(sr.getSmInfo().getOrderNo());
//
//                    handler.sendEmptyMessage(STATE_MAKE);
//                } else {
//                    if (sr.whyOpenFailed().equals(SmResult.OpenFailure.NeedUpdate)) {
//                        startActivity(new Intent(MakeSmFileActivity.this, UpdateActivity.class));
//                    }
//                    GlobalToast.toastShort(MakeSmFileActivity.this, sr.getFailureReason());
//                    finish();
//                }
            }

//            smInfo.setVersionStr(PhoneInfo.getVersionStr(mContext));
//            smInfo.setAppType(PhoneInfo.appType);		// 服务器以此区分客户端，填错的话则返回的数据中会出现乱码。下同！
//            smInfo.setVersion(PhoneInfo.version);
//            smInfo.setFileVersion(PhoneInfo.fileVersion);

            private void getUserToken(final SmInfo smInfo) {
                String userTokenUrl = Constant.UserTokenHost;
                smInfo.setVersionStr(PhoneInfo.getVersionStr(MakeSmFileActivity.this));
                smInfo.setAppType(PhoneInfo.appType);        // 服务器以此区分客户端，填错的话则返回的数据中会出现乱码。下同！
                smInfo.setVersion(PhoneInfo.version);
                smInfo.setFileVersion(PhoneInfo.fileVersion);

                // 请求参数
                Bundle bundle = new Bundle();
                bundle.putString("grant_type", "password");
                bundle.putString("username", userInfo.getUserName());
                if (!TextUtils.isEmpty(userInfo.getPsd())) {
                    bundle.putString("password", userInfo.getPsd());
                } else {
                    bundle.putString("password",
                            "n|" + XCoder.getHttpEncryptText(userInfo.getUserName()));
                }

                // 请求头
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",
                        "Basic " + SecurityUtil.encryptBASE64(PhoneInfo.testID
                                + ":" + PhoneInfo.testPSD));
                headers.put("Content-Type", "application/x-www-form-urlencoded");

                GlobalHttp.post(userTokenUrl, bundle, headers,
                        new Callback.CommonCallback<String>() {

                            @Override
                            public void onSuccess(String arg0) {
                                // 解析Json
                                try {
                                    JSONObject object = new JSONObject(arg0);
                                    tokenString = (String) object.get("access_token");

                                    makeFile(smInfo);

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

            private void makeFile(final SmInfo smInfo) {
                String makeFileUrl = Constant.UserSourceHost + "api/v1/filemake";
                // 请求参数
                Bundle bundle = new Bundle();
                bundle.putString("AppType", "28");
                bundle.putString("AppVersion", PhoneInfo.getVersionStr(MakeSmFileActivity.this));
                bundle.putInt("MakeType", 1);
                bundle.putString("Filename", smInfo.getFileName());
                final byte[] randomKey_16 = createRandomKey_16();
                bundle.putString("EncryptKey", XCoder.getHttpEncryptText(SecurityUtil
                        .byteArrayToHexString(randomKey_16)));

                final byte[] randomKey_161 = createRandomKey_16();
                bundle.putString("SessionKey", XCoder.getHttpEncryptText(SecurityUtil
                        .byteArrayToHexString(randomKey_161)));
                bundle.putInt("FileVersion", 2);
                bundle.putInt("SeeNum", smInfo.getOpenCount());
                bundle.putInt("SeeDay", smInfo.getDays());

                // 请求头
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + tokenString);

                headers.put("Content-Type", "application/x-www-form-urlencoded");
                //headers.put("Content-Type", "application/json");

                GlobalHttp.post(makeFileUrl, bundle, headers,
                        new Callback.CommonCallback<String>() {

                            @Override
                            public void onSuccess(String arg0) {
                                // 解析Json
                                try {
                                    JSONObject object = new JSONObject(arg0);
                                    String status = (String) object.get("Status");

                                    if (status.equals("1")) {
                                        String result = object.get("Result").toString();
                                        JSONObject jsonObject = new JSONObject(result);
                                        int fid = (int) jsonObject.get("Fid");

                                        smInfo.setFid(fid);
                                        smInfo.setEncodeKey(randomKey_16);
                                        smInfo.setSessionKey(randomKey_161);

                                        handler.sendEmptyMessage(STATE_MAKE);
                                    } else if (status.equals("0")) {
                                        GlobalToast.toastShort(getApplicationContext(), (String)
                                                object.get("Message"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFinished() {
                            }

                            @Override
                            public void onError(Throwable arg0, boolean arg1) {
                                finish();
                            }

                            @Override
                            public void onCancelled(CancelledException arg0) {
                            }
                        });
            }
        });

    }

    /*private void makeState1() {
        GlobalTask.executeBackground(new Runnable() {

            @Override
            public void run() {
                SmResult sr = new SmConnect(MakeSmFileActivity.this).makeFile(smInfo, isPayMode);
                    // sr1
                if (sr.succeed()) {
                    // 设定编码key,产生一个16位随机数
                    smInfo.setEncodeKey(createRandomKey_16());
                    // 设定SessionKey,产生一个16位随机数
                    smInfo.setSessionKey(createRandomKey_16());
                    // 设定fid
                    smInfo.setFid(sr.getSmInfo().getFid());
                    // 其实只有制作payFile时才有效
                    smInfo.setOrderNo(sr.getSmInfo().getOrderNo());

                    handler.sendEmptyMessage(STATE_MAKE);
                } else {
                    if (sr.whyOpenFailed().equals(SmResult.OpenFailure.NeedUpdate)) {
                        startActivity(new Intent(MakeSmFileActivity.this, UpdateActivity.class));
                    }
                    GlobalToast.toastShort(MakeSmFileActivity.this, sr.getFailureReason());
                    finish();
                }
            }
        });

    }*/

    private void makeState2() {
        XCodeType type = isPayMode ? XCodeType.MakePay : XCodeType.MakeFree;
        codeView = new XCodeView(MakeSmFileActivity.this);
        codeView.startXCode(type, smInfo, true, g_strPath);
        codeView.setXCodeViewListener(new XCodeViewListener() {

            @Override
            public void onFinished(ReturnInfo info) {
                g_strSmPath = info.desPath;
                handler.sendEmptyMessage(STATE_FINISH);
            }

            @Override
            public void onError(int error) {
                finish();
            }
        });
    }

    private void makeState3() {
        GlobalTask.executeBackground(new Runnable() {

            @Override
            public void run() {
                // 制作完成,跳转MakeSmFileDoneActivity.class
                GlobalData.Sm.instance(MakeSmFileActivity.this).add(0, g_strSmPath, true);
                EventBus.getDefault().post(new PathsEvent(PathsEvent.P_CLIPER));
                GlobalObserver.getGOb().postNotifyObservers(ObTag.Refresh);
                Intent intent = GlobalIntentKeys.reUseIntent(MakeSmFileActivity.this,
                        MakeSmFileDoneActivity.class);
                intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, g_strSmPath);
                startActivity(intent);
               /* SmResult sr = new SmConnect(MakeSmFileActivity.this).uploadHash(smInfo, true);
                if (sr.succeed()) {
                    if (WXTool.isFromWxMake) {
                        backToWX(g_strSmPath);
                    } else {
                        // 制作完成,跳转MakeSmFileDoneActivity.class
                        GlobalData.Sm.instance(MakeSmFileActivity.this).add(0, g_strSmPath, true);
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.Refresh);
                        Intent intent = GlobalIntentKeys.reUseIntent(MakeSmFileActivity.this,
                        MakeSmFileDoneActivity.class);
                        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, g_strSmPath);
                        startActivity(intent);
                    }
                } else {
                    FileUtil.deleteFile(g_strSmPath); // 提交失败则删除文件
                }*/
                finish();
            }
        });
    }

    // 产生一个16位随机数
    private static byte[] createRandomKey_16() {
        byte[] b = new byte[16];
        for (int i = 0; i < 16; i++) {
            b[i] = (byte) new Random().nextInt(120);
        }
        return b;
    }

    private void changeLimit(final String path) {
        Runnable netTask = new Runnable() {
            @Override
            public void run() {
                XCoderResult xr = XCoder.analysisSmFile(path);
                if (xr.succeed()) {
                    smInfo.setFid(xr.getSmInfo().getFid());
                    SmResult sr = new SmConnect(MakeSmFileActivity.this).modifyLimit(smInfo,
                            true, true);
                    // 注意这里，需要版本升级
                    if (!sr.succeed() && sr.whyOpenFailed().equals(SmResult.OpenFailure
                            .NeedUpdate)) {
                        startActivity(new Intent(MakeSmFileActivity.this, UpdateActivity.class));
                    }
                }
                EventBus.getDefault().post(new RefreshModifyPowerEvent(path));
                finish();
            }
        };

        GlobalTask.executeBackground(netTask);
    }

    /*-
     * 微信版本升级后，原来可行的方案就不行了
     * 现在的这个方案仍有问题，需探索新的
     */
//    private void backToWX(String sucPath) {
        // // final WXAppExtendObject object = new WXAppExtendObject();
        // //
        // // object.fileData = new byte[30];
        // // object.extInfo = Dirs.getFileName(sucPath);
        // // final WXMediaMessage msg = new WXMediaMessage();
        // // msg.title = WXTool.WX_TITLE;
        // // msg.description = "鹏保宝秘制的限时阅读文件，能看多久由我控制。";
        // // msg.mediaObject = object;
        // //
        // // GetMessageFromWX.Resp resp = new GetMessageFromWX.Resp();
        // // resp.transaction = new
        // // GetMessageFromWX.Req(WXTool.wxBundle).transaction;
        // // resp.message = msg;
        // //
        // // IWXAPI api = WXAPIFactory.createWXAPI(MakeSmFileActivity.this,
        // // WXTool.WX_APP_ID);
        // // api.sendResp(resp);
        // // ((PycApplication) getApplication()).exit();
        //
        // WXFileObject object = new WXFileObject();
        // object.filePath = sucPath;
        // final WXMediaMessage msg = new WXMediaMessage();
        // msg.title = Dirs.getFileName(sucPath);
        // msg.description = "鹏保宝秘制的限时阅读文件，能看多久由我控制。";
        // msg.mediaObject = object;
        // final Bitmap bmp = BitmapFactory.decodeResource(getResources(),
        // R.drawable.app_pbb_logo);
        // msg.thumbData = bmpToByteArray(bmp, true);
        //
        // // GetMessageFromWX.Resp resp = new
        // // GetMessageFromWX.Resp(WXTool.wxBundle);
        // // resp.transaction = new
        // // GetMessageFromWX.Req(WXTool.wxBundle).transaction;
        // // resp.message = msg;
        // // IWXAPI api = WXAPIFactory.createWXAPI(MakeSmFileActivity.this,
        // // WXTool.WX_APP_ID);
        // // api.sendResp(resp);
        // SendMessageToWX.Req req = new SendMessageToWX.Req();
        // req.transaction = new
        // GetMessageFromWX.Req(WXTool.wxBundle).transaction;
        // req.message = msg;
        // IWXAPI api = WXAPIFactory.createWXAPI(MakeSmFileActivity.this,
        // WXTool.WX_APP_ID);
        // api.sendReq(req);
        // ((PycApplication) getApplication()).exit();
//    }

//    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        bmp.compress(CompressFormat.PNG, 100, output);
//        if (needRecycle) {
//            bmp.recycle();
//        }
//
//        byte[] result = output.toByteArray();
//        try {
//            output.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            GlobalToast.toastShort(this, isChangeLimit ? "正在修改，请勿取消！" : "正在制作，请勿取消！");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
