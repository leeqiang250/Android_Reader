package cn.com.pyc.update;

import android.content.Context;

import com.qlk.util.global.GlobalObserver;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util.IOUtil;
import com.qlk.util.tool.Util.NetUtil;
import com.qlk.util.tool._SysoXXX;
import com.sz.mobilesdk.util.CommonUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.com.pyc.global.ObTag;

/**
 * apk在线更新工具类
 */
public class UpdateTool {
    private static String Apk_Down_Url;
    private static String Apk_Version;
    //	private static String Help_Version;
    private static int artical;        //Our new article.
    private static int recommend;    //New recommend.

    public static void checkUpdateInfo(final Context context, final boolean showDialog) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                //_SysoXXX.message("111");
                if (NetUtil.isNetInUse(context)) {
                    if (check(context)) {
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.Update);
                    } else {
                        if (showDialog) {
                            GlobalToast.toastShort(context, "获取信息失败");
                        }
                    }
                } else {
                    GlobalToast.toastShort(context, "没有可用网络");
                }
            }
        };
        if (showDialog) {
            GlobalTask.executeNormalTask(context, task);
        } else {
            GlobalTask.executeBackground(task);
        }
    }

    //	public static void checkNewInfo(final Context context)
    //	{
    //		GlobalTask.executeBackground(new Runnable()
    //		{
    //			@Override
    //			public void run()
    //			{
    //				String uuid = PhoneInfo.getUUID(context);
    //				String info = NetUtil.connectHttpNet(context,
    //						String.format(Locale.US, Constant.URL_RECOMMEND, uuid));
    //				if (!TextUtils.isEmpty(info) && info.matches("[0-9]*"))
    //				{
    //					recommend = Integer.parseInt(info);
    //				}
    //				info = NetUtil.connectHttpNet(context,
    //						String.format(Locale.US, Constant.URL_ARTICAL, uuid));
    //				if (!TextUtils.isEmpty(info) && info.matches("[0-9]*"))
    //				{
    //					artical = Integer.parseInt(info);
    //				}
    //
    //				GlobalObserver.getGOb().postNotifyObservers(ObTag.Update);
    //			}
    //		});
    //	}

    private static boolean check(Context context) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            //20170609修改：合并后，使用的是reader的更新链接
            URL url = new URL(context.getResources().getString(cn.com.pyc.pbb.reader.R.string
                    .app_update_url));
            conn = (HttpURLConnection) url.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            //			Help_Version = reader.readLine().trim();
            reader.readLine();    //Pass help version.
            Apk_Version = reader.readLine().trim();
            Apk_Down_Url = reader.readLine().trim();
            _SysoXXX.message(Apk_Down_Url + " ; " + Apk_Version);
            return true;
        } catch (Exception e) {
            //_SysoXXX.message("222");
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            IOUtil.close(reader, null);
        }
    }

    public static boolean isApkNew(Context context) {
        //		return Apk_Version != null
        //				&& !context.getResources().getString(R.string.app_version).equals
        // (Apk_Version);

        //		String regEx="[^0-9]";
        //		Pattern p = Pattern.compile(regEx);
        //		Matcher m = p.matcher(context.getResources().getString(R.string.app_version));
        //
        //		String s1 = m.replaceAll("").trim().substring(0, 5);
        //		Matcher m1 = p.matcher(Apk_Version);
        //		String s2 = m1.replaceAll("").trim().substring(0, 5);
        //
        //		System.out.println(s1 +" | " +s2);
        //		GlobalToast.toastShort(context, s1 +" | " +s2);
        //
        //		if (Integer.parseInt(s1) < Integer.parseInt(s2))
        //		{
        //			return true;
        //		}
        //		return false;
        System.out.println(Apk_Version +"----------|||||||||||||-----------");
        String curVersion = CommonUtil.getAppVersionName(context);
//        return Apk_Version != null && context.getResources().getString(cn.com.pyc.pbb.reader.R
//                .string.app_version).compareTo(Apk_Version) < 0;
        return Apk_Version != null && curVersion.compareTo(Apk_Version) < 0;

//		if (null == Apk_Version)
//			return false;
//
//		String netVersion = Apk_Version;
//		String localVersion = context.getResources().getString(R.string.app_version);
//
//		String net = netVersion.substring(0, netVersion.lastIndexOf("."));
//		String local = localVersion.substring(0, localVersion.lastIndexOf("."));
//
//		String localss[] = local.split("\\.");
//		String netss[] = net.split("\\.");
//
//		System.out.println("v[\\.]:" + localss[0]);
//
//		int locala = Integer.parseInt(localss[0]);
//		int localb = Integer.parseInt(localss[1]);
//		int localc = Integer.parseInt(localss[2]);
//
//		int neta = Integer.parseInt(netss[0]);
//		int netb = Integer.parseInt(netss[1]);
//		int netc = Integer.parseInt(netss[2]);
//
//		if (locala < neta)
//		{
//			return true;
//		}
//		else if (locala == neta)
//		{
//			if (localb < netb)
//			{
//				return true;
//			}
//			else if (localb == netb)
//			{
//				if (localc < netc)
//				{
//					return true;
//				}
//				else if (localc == netc)
//				{
//					return false;
//				}
//				else
//				{
//					return false;
//				}
//			}
//			else
//			{
//				return false;
//			}
//		}
//		else
//		{
//			return false;
//		}

        //		for (int i = 0; i < localss.length; i++)
        //		{
        //			if (Integer.parseInt(localss[i]) < Integer.parseInt(netss[i]))
        //			{
        //				return true;
        //			}else if (Integer.parseInt(localss[i]) > Integer.parseInt(netss[i]))
        //			{
        //				return false;
        //			}else {
        //				if (i==2)
        //				{
        //					return false;
        //				}
        //				continue;
        //			}
        //		}

    }

    //	public static boolean isHelpNew(Context context)
    //	{
    //		String memHelp = (String) PbbSP.getGSP(context).getValue(PbbSP.SP_HELP_VERSION, "");
    //		return !memHelp.equals(Help_Version);	// 第一次时"".equals(null)
    //	}
    //
    //	public static void changeHelpVersion(Context context)
    //	{
    //		if (!TextUtils.isEmpty(Help_Version))	// 只有help合法时才更新
    //		{
    //			PbbSP.getGSP(context).putValue(PbbSP.SP_HELP_VERSION, Help_Version);
    //		}
    //	}

    public static String getApkDownUrl() {
        return Apk_Down_Url;
    }

    static String getApk_Version() {
        return Apk_Version;
    }

    public static int getArtical() {
        return artical;
    }

    public static int getRecommend() {
        return recommend;
    }

    public static boolean isAnyNew(Context context) {
        return artical > 0 || recommend > 0 || isApkNew(context);
    }

    /**
     * Clear and refresh.
     */
    public static void clearRecommend() {
        recommend = 0;
        GlobalObserver.getGOb().postNotifyObservers(ObTag.Update);
    }

    /**
     * Clear and refresh.
     */
    public static void clearArtical() {
        artical = 0;
        GlobalObserver.getGOb().postNotifyObservers(ObTag.Update);
    }
}
