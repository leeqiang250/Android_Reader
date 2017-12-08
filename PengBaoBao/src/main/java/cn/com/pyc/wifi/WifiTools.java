package cn.com.pyc.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class WifiTools
{
	public static boolean isWifiInUse(Context context)
	{
		WifiManager wifiMgr = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
		{
			ConnectivityManager connManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiInfo = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return wifiInfo.isConnected();
		}
		else
		{
			return false;
		}
	}
}
