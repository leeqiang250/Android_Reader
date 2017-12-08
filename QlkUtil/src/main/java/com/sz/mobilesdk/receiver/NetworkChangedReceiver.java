package com.sz.mobilesdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SZLog;

/**
 * Created by hudq on 2017/1/10.
 */

public class NetworkChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean result = CommonUtil.isNetConnect(context);
        SZLog.w("NetworkChanged: " + result);
        if (result) {
            Constant.initDeviceId(context);
        }
    }
}
