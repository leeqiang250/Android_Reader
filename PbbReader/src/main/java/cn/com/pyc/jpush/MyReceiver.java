package cn.com.pyc.jpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbbonline.common.ShareMode;
import cn.com.pyc.pbbonline.db.manager.ClickIndexDBManager;
import cn.com.pyc.pbbonline.model.JPDataBean;
import cn.com.pyc.pbbonline.model.JPushDataBean;
import cn.com.pyc.pbbonline.service.JpushViewService;
import cn.com.pyc.pbbonline.util.DeleteFileUtil;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.ParserJPushBeanUtil;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.web.WebActivity.WebPage;
import cn.jpush.android.api.JPushInterface;

/**
 * 极光推送 广播接收者
 */
public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SZLog.v(TAG, printBundle(bundle));
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            SPUtil.save(Fields.FIELDS_JPUSH_REGISTERID, regId);
            Log.d(TAG, "接收Registration Id: " + regId);
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            String extra_message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            Log.d(TAG, "接收到推送下来的自定义消息: " + extra_message);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            //pbbonline
            String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
            String extra_alert = bundle.getString(JPushInterface.EXTRA_ALERT);
            //int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "接收到推送下来的通知: " + extra);
            JPushDataBean jb = ParserJPushBeanUtil.parserJPushJson(extra);
            //SZLog.e(TAG, "parser: " + jb.toString());
            jb.getData().setShare_prompt(extra_alert);

            JpushViewService.startJPushService(context, jb);
            //revokeShare(context, jb);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            String jsonPath = bundle.getString(JPushInterface.EXTRA_EXTRA);
            String extra_alert = bundle.getString(JPushInterface.EXTRA_ALERT);

            Log.d(TAG, "用户点击打开了通知: " + jsonPath);
            try {
                JSONObject jsonobj = new JSONObject(jsonPath);
                String realPath;
                if (jsonobj.has("url")) {
                    realPath = Constant.WebHost + jsonobj.get("url").toString();
                } else if (jsonobj.has("weburl")) {
                    realPath = jsonobj.get("weburl").toString();
                } else if (jsonobj.has("hdidurl")) {
                    if (jsonobj.get("hdidurl").toString().contains("?")) {
                        realPath = jsonobj.get("hdidurl").toString() + "&hdid="
                                + PhoneInfo.getDeviceID(context);
                    } else {
                        realPath = jsonobj.get("hdidurl").toString() + "?hdid="
                                + PhoneInfo.getDeviceID(context);
                    }
                } else {
                    //pbbonline:点击通知
                    JPushDataBean jb = ParserJPushBeanUtil.parserJPushJson(jsonPath);
                    jb.getData().setShare_prompt(extra_alert);
                    OpenPageUtil.openAppMainPage(context, jb);
                    return;
                }
                Intent i = new Intent(context, WebActivity.class);
                WebPage.Link.setUrl(realPath);
                i.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebPage.Link);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE,
                    false);
            Log.w(TAG, intent.getAction() + " connected state change to " + connected);
            if (connected) {
                com.sz.mobilesdk.common.Constant.initDeviceId(context);
            }
        } else {
            Log.d(TAG, "Unhandled intent - " + intent.getAction());

        }
    }

    private String printBundle(Bundle bundle) {
        if (!SZInitInterface.isDebugMode)
            return "";

        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    //send msg to DeviceScannerActivity
    //	private void processCustomMessage(Context context, Bundle bundle) {
    //		if (DeviceScannerActivity.isForeground) {
    //			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
    //			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
    //			Intent msgIntent = new Intent(DeviceScannerActivity.MESSAGE_RECEIVED_ACTION);
    //			msgIntent.putExtra(DeviceScannerActivity.KEY_MESSAGE, message);
    //			if (!ExampleUtil.isEmpty(extras)) {
    //				try {
    //					JSONObject extraJson = new JSONObject(extras);
    //					if (null != extraJson && extraJson.length() > 0) {
    //						msgIntent.putExtra(DeviceScannerActivity.KEY_EXTRAS, extras);
    //					}
    //				} catch (JSONException e) {
    //
    //				}
    //
    //			}
    //			context.sendBroadcast(msgIntent);
    //		}
    //	}

    private void revokeShare(Context context, JPushDataBean data) {
        String action = data.getAction();
        JPDataBean bean = data.getData();
        if (action == null)
            return;
        switch (action) {
            case ShareMode.REVOKEFILE:
                deleteFile(context, bean);
                break;
            case ShareMode.REVOKEFOLDER:
                deleteFolder(context, bean);
                break;

            default:
                break;
        }
    }

    /**
     * 删除收回的文件 (已下载的)
     *
     * @param bean
     */
    private void deleteFile(Context ctx, JPDataBean bean) {
        try {
            String shareId = bean.getShareID();
            List<String> filePaths = bean.getFilePath();
            for (String fp : filePaths) {
                if (fp != null) {
                    String[] paths = fp.split("/");
                    String myProId = paths[0];
                    String fileId = paths[1];

                    SZLog.v(TAG, "myProId: " + myProId);
                    SZLog.v(TAG, "fileId: " + fileId);

                    //DeleteFileUtil.deleteFile(ctx, shareId, myProId, fileId);

                    //只要文件有更新，删除对应的文件夹
                    if (!TextUtils.isEmpty(myProId)) {
                        DeleteFileUtil.deleteFolder(shareId, myProId);
                        ClickIndexDBManager.Builder().deleteByMyProId(myProId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除收回的文件夹(已下载的)
     *
     * @param bean
     */
    private void deleteFolder(Context ctx, JPDataBean bean) {
        String shareId = bean.getShareID();
        List<String> folderPaths = bean.getFolderPath();
        for (String myProId : folderPaths) {
            SZLog.v(TAG, "myProId: " + myProId);
            if (!TextUtils.isEmpty(myProId)) {
                DeleteFileUtil.deleteFolder(shareId, myProId);
                ClickIndexDBManager.Builder().deleteByMyProId(myProId);
            }
        }
    }

}
