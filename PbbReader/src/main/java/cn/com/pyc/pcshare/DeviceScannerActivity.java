package cn.com.pyc.pcshare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.qlk.util.base.BaseActivity;
import com.sz.accesspc.bean.DeviceInfo;
import com.sz.accesspc.intern.$$;
import com.sz.accesspc.intern.ScanDeviceEngine;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.dialog.InputEditDialog;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.List;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pcshare.adapter.DeviceListAdapter;
import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;

/**
 * 扫描主界面
 * Created by hudq on 2016/12/15.
 */
public class DeviceScannerActivity extends BaseActivity {

    private static final String TAG = "DeviceScannerUI";
    public static final String KEY_NAME = "s1";
    public static final String KEY_PWD = "s2";
    private static final int MSG_SUCCESS = 0x01;
    private static final int MSG_FAIL = 0x10;
    private ScanDeviceEngine engine;
    private ListView listview;
    private DeviceListAdapter adapter;
    private List<DeviceInfo> cacheData;
    private boolean isDestory = false;
    private MyHandler handler = new MyHandler(this);

    public static String sUserName;
    public static String sPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pc_activity_device_scanner);
        engine = new ScanDeviceEngine(this);
        ((TextView) findViewById(R.id.tv_local_ip)).setText("当前设备IP: " + engine.getDevAddress());
        listview = (ListView) findViewById(R.id.device_listview);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter == null) return;
                DeviceInfo o = adapter.getItem(position);
                sUserName = (String) SPUtil.get(KEY_NAME, "");
                sPassword = (String) SPUtil.get(KEY_PWD, "");
                if (TextUtils.isEmpty(sUserName) || TextUtils.isEmpty(sPassword)) {
                    showInputDialog(o);
                } else {
                    connectPC(o.getIp());
                }
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long
                    id) {
                UIHelper.showCommonDialog(DeviceScannerActivity.this, null,
                        "是否清除保存的账户密码？ \n清除后可重新输入", null,
                        new UIHelper.DialogCallBack() {
                            @Override
                            public void onConfirm() {
                                clearAccount();
                            }
                        });
                return true;
            }
        });

        searchDevices();
    }

    private void clearAccount() {
        SPUtil.remove(KEY_NAME);
        SPUtil.remove(KEY_PWD);
    }

    private void saveAccount(String name, String pwd) {
        SPUtil.save(KEY_NAME, (sUserName = name));
        SPUtil.save(KEY_PWD, (sPassword = pwd));
    }

    /**
     * 扫描设备
     */
    private void searchDevices() {
        showBgLoading(this, "搜索中...");
        engine.startScanning();
        engine.setScannerListener(new ScanDeviceEngine.ScannerListener() {

            @Override
            public void onScanFinished(List<DeviceInfo> result) {
                if (isDestory) {
                    if (engine != null)
                        engine.destory();
                    return;
                }
                if (result != null && !result.isEmpty()) {
                    cacheData = result;
                }
                if (result == null || result.isEmpty()) {
                    result = cacheData;
                }
                if (result == null || result.isEmpty()) {
                    showToastLong("没有搜索到远程连接设备！");
                } else {
                    adapter = new DeviceListAdapter(DeviceScannerActivity.this, result);
                    listview.setAdapter(adapter);
                }
                hideBgLoading();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestory = true;
    }

    private void showInputDialog(final DeviceInfo o) {

        InputEditDialog.InputDialogCallback callback = new InputEditDialog.InputDialogCallback() {
            @Override
            public void onConfirm(String name1, String pwd2) {
                saveAccount(name1, pwd2);
                connectPC(o.getIp());
            }
        };

        InputEditDialog dialog = new InputEditDialog.Builder()
                .setActivity(this)
                .setTitle("连接到：" + o.getName())
                .setCallback(callback)
                .create();
        dialog.show();
        //        int width = ((int) (Constant.screenWidth * 0.73));
//        Window dialogWindow = dialog.getWindow();
//        if (dialogWindow != null) {
//            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//            if (lp != null) {
//                lp.gravity = Gravity.CENTER;
//                lp.width = width;
//                lp.height = width * 2 / 3;
//                dialogWindow.setAttributes(lp);
//            }
//        }
    }

    /**
     * 连接PC
     *
     * @param ip IP
     */
    private void connectPC(String ip) {
        showBgLoading(this, "正在连接电脑");
        $$.execute(new UsRunnable(ip));
    }

    private class UsRunnable implements Runnable {
        private String ip;

        private UsRunnable(String ip) {
            this.ip = ip;
        }

        @Override
        public void run() {
            connect(ip);
        }
    }

    private void connect(String ip) {
        try {
            //SmbFile smbFile = new SmbFile("smb://" + ip);
            //smbFile.connect();
            //此处只是用来连接pc，查询是否成功。
            //用户名和密码鉴权,使用此种方法校验避免password出现特殊字符导致错误
            UniAddress domain = UniAddress.getByName(ip);
            //byte[] challenge = SmbSession.getChallenge(domain);
            //NtlmSsp.authenticate();
            SmbSession.logon(domain, new NtlmPasswordAuthentication(ip, sUserName, sPassword));
            //成功，跳转
            Message msg = Message.obtain();
            msg.obj = ip;
            msg.what = MSG_SUCCESS;
            handler.sendMessageDelayed(msg, 100);
        } catch (SmbException e) {
            e.printStackTrace();
            Log.e(TAG, "连接失败");
            handler.sendEmptyMessage(MSG_FAIL);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e(TAG, "UnknownHost");
            handler.sendEmptyMessage(MSG_FAIL);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//            Log.e(TAG, "MalformedURLException");
//        } catch (IOException e){
//            e.printStackTrace();
//            Log.e(TAG, "IOException");
        }
    }

    private void connectNOPWD(String ip) {

    }

    private static class MyHandler extends Handler {
        private WeakReference<DeviceScannerActivity> reference;

        private MyHandler(DeviceScannerActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (reference.get() == null) return;
            DeviceScannerActivity activity = reference.get();
            if (msg.what == MSG_SUCCESS) {
                String ip = (String) msg.obj;
                String path = "smb://" + ip;
                activity.startActivity(new Intent(activity, DirectoryHomeActivity.class)
                        .putExtra("path", path)
                        .putExtra("ip", ip));
            } else if (msg.what == MSG_FAIL) {
                activity.showToastLong("连接远程设备失败！");
                activity.clearAccount();
            }
            activity.hideBgLoading();
        }
    }
}
