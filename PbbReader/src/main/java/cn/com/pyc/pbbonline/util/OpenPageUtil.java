package cn.com.pyc.pbbonline.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.zxing.client.android.CaptureActivity;
import com.sz.mobilesdk.database.bean.Album;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.pbbonline.IndexPageHomeActivity;
import cn.com.pyc.pbbonline.IntermediaryActivity;
import cn.com.pyc.pbbonline.ListAlbumActivity;
import cn.com.pyc.pbbonline.ListFilesActivity;
import cn.com.pyc.pbbonline.ShareDetailsPageActivity;
import cn.com.pyc.pbbonline.VideoActivity;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.common.IMusicConst;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.model.JPushDataBean;
import cn.com.pyc.pbbonline.service.MediaService;

/**
 * 管理打开其他de页面的工具类
 * 
 * @author hudq
 */
public class OpenPageUtil
{

	/**
	 * 开启页面
	 * 
	 * @param ctx
	 * @param cls
	 */
	public static void openActivity(Context ctx, Class<?> cls)
	{
		openActivity(ctx, cls, null);
	}

	/**
	 * 通过类名启动Activity，并且含有Bundle数据
	 * 
	 * @param clz
	 * @param bundle
	 *            传递的bundle数据
	 */
	public static void openActivity(Context ctx, Class<?> clz, Bundle bundle)
	{
		Intent intent = new Intent(ctx, clz);
		if (bundle != null)
			intent.putExtras(bundle);
		ctx.startActivity(intent);
	}

	/**
	 * 打开系统浏览器
	 * 
	 * @param context
	 * @param url
	 */
	public static void openBrowserOfSystem(Context context, String url)
	{
		if (TextUtils.isEmpty(url))
		{
			return;
		}
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(Intent.createChooser(intent, null));
	}

	/** 系统分享 */
	public static void sharedMore(Context ctx, String text)
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, text);
		ctx.startActivity(Intent.createChooser(intent, "选择分享"));
	}

	/** 打开系统短信界面 */
	public static void sharedSendMsg(Context ctx, String text)
	{
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		// sendIntent.setData(Uri.parse("smsto:"));
		sendIntent.setType("vnd.android-dir/mms-sms");
		sendIntent.putExtra("sms_body", text);
		ctx.startActivity(Intent.createChooser(sendIntent, null));
	}

	/** 打开网络设置界面 */
	public static void openWifiSetting(Context ctx)
	{
		Intent wifiSettingsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
		ctx.startActivity(wifiSettingsIntent);
	}

	/**
	 * 扫一扫
	 * 
	 * @param activity
	 */
	public static void openZXingCode(Activity activity)
	{
		Intent scanerIntent = new Intent(activity, CaptureActivity.class);
		activity.startActivityForResult(scanerIntent, CaptureActivity.REQUEST_CODE_SCAN);
	}

	/**
	 * 进入主下载页面
	 * 
	 * @param atc
	 * @param url
	 * @param tName
	 * @param isReceiveUI
	 */
	//	public static void openDownloadMain(Context atc, String tName, boolean isReceive)
	//	{
	//		Intent intent = new Intent(atc, DownloadMainActivity.class);
	//		intent.putExtra("title_name", tName);
	//		intent.putExtra("is_receive", isReceive);
	//		atc.startActivity(intent);
	//	}

	/**
	 * 打开视频播放
	 * 
	 * @param ctx
	 * @param contentId
	 *            当前文件id
	 * @param videoFiles
	 */
	public static void openVideoPlayer(Context ctx, String contentId, List<SZFile> videoFiles)
	{
		Intent intent = new Intent(ctx, VideoActivity.class);
		intent.putExtra("contentId", contentId);
		intent.putParcelableArrayListExtra("videoFiles",
				(ArrayList<? extends Parcelable>) videoFiles);
		ctx.startActivity(intent);
	}

	/**
	 * 打开pdf
	 * 
	 * @param ctx
	 * @param contentId
	 *            当前文件id
	 * @param pdfFiles
	 */
	public static void openPDFReader(Context ctx, String contentId, List<SZFile> pdfFiles)
	{
		Intent intent = new Intent(ctx, cn.com.pyc.pbbonline.MuPDFActivity.class);
		intent.putExtra("contentId", contentId);
		intent.putParcelableArrayListExtra("pdfFiles", (ArrayList<? extends Parcelable>) pdfFiles);
		ctx.startActivity(intent);
	}

	/**
	 * 打开音乐
	 * 
	 * @param ctx
	 * @param folderName
	 * @param contentId
	 *            当前文件id
	 * @param musicFiles
	 * @param isinit
	 */
	public static void openMusicPlayer(Context ctx, String folderName, String contentId,
			List<SZFile> musicFiles, boolean isinit)
	{
		Intent intent = new Intent(ctx, cn.com.pyc.pbbonline.MusicHomeActivity.class);
		intent.putExtra("folderName", folderName);
		intent.putExtra("contentId", contentId);
		intent.putExtra("is_init", isinit);
		intent.putParcelableArrayListExtra("musicFiles",
				(ArrayList<? extends Parcelable>) musicFiles);
		ctx.startActivity(intent);
	}

	/**
	 * 打开媒体服务
	 * 
	 * @param ctx
	 * @param szFile
	 * @param option
	 * @param process
	 * @param files
	 */
	public static void startMediaService(Context ctx, SZFile szFile, int option, int process,
			List<SZFile> files)
	{
		Intent intent = new Intent(ctx, MediaService.class);
		intent.putExtra("music_file", szFile);
		intent.putExtra("option", option);
		intent.putExtra("process", process);
		intent.putParcelableArrayListExtra("list_files", (ArrayList<? extends Parcelable>) files);
		ctx.startService(intent);
	}

	/**
	 * 停止媒体服务
	 * 
	 * @param ctx
	 */
	public static void stopMediaService(Context ctx)
	{
		Intent intent = new Intent(ctx, MediaService.class);
		intent.putExtra("option", IMusicConst.OPTION_STOP);
		ctx.startService(intent);
	}

	/**
	 * 打开对应的资源文件
	 * 
	 * @param a
	 */
	public static void openMultipleMedia(Context ctx, Album a)
	{
		if (a.getCategory() == null)
			return;
		Bundle bundle = new Bundle();
		bundle.putSerializable("Album", a);
		bundle.putInt(K.JUMP_FLAG, K.UI_MAIN);
		openActivity(ctx, ListAlbumActivity.class, bundle);
	}

	/**
	 * 打开分享详情页面
	 * 
	 * @param ctx
	 * @param sharedId
	 */
	public static void openSharedDetaiPage(Context ctx, String sharedId)
	{
		Intent intent = new Intent(ctx, ShareDetailsPageActivity.class);
		intent.putExtra("ShareID", sharedId);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}

	/**
	 * 打开app，进入主页面
	 * 
	 * @param context
	 */
	public static void openApp(Context context)
	{
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClass(context, IndexPageHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		context.startActivity(intent);
	}

	/**
	 * 通过通知消息,打开主页面
	 * 
	 * @param context
	 * @param o
	 */
	public static void openAppMainPage(Context context, JPushDataBean o)
	{
		Bundle bundle = new Bundle();
		bundle.putSerializable("PushUpdateBean", o);
		Intent intentNotification = new Intent(Intent.ACTION_MAIN);
		intentNotification.putExtras(bundle);
		intentNotification.addCategory(Intent.CATEGORY_LAUNCHER);
		intentNotification.setClass(context, IndexPageHomeActivity.class);
		intentNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		context.startActivity(intentNotification);
	}

	/**
	 * 打开单击文件列表
	 * 
	 * @param context
	 * @param folderId
	 *            文件夹id，必传
	 * @param folderName
	 *            文件夹名，必传
	 * @param folderPublishDate
	 * @param theme
	 * @param owner
	 * @param delFileIds
	 *            删除的文件id集合
	 */
	public static void openFileListPage(Context context, String folderId, String folderName,
			String folderPublishDate, String theme, String owner, List<String> delFileIds)
	{
		Bundle bundle = new Bundle();
		bundle.putString("shareFolderId", folderId);
		bundle.putString("shareFolderName", folderName);
		bundle.putString("sharePublishDate", folderPublishDate);
		bundle.putString("shareTheme", theme);
		bundle.putString("shareOwner", owner);
		bundle.putStringArrayList("deleteFileIds", (ArrayList<String>) delFileIds);
		openActivity(context, ListFilesActivity.class, bundle);
	}

	/**
	 * 打开单击文件列表（离线）
	 * 
	 * @param context
	 * @param folderId
	 *            文件夹id，必传
	 * @param folderName
	 *            文件夹名，必传
	 */
	public static void openFileListPage(Context context, String folderId, String folderName)
	{
		openFileListPage(context, folderId, folderName, "", "", "", null);
	}

	/**
	 * 打开中介迁移数据的显示界面
	 * 
	 * @param context
	 * @param srcPath
	 * @param destPath
	 */
	public static void openIntermediaryPage(Context context, String srcPath, String destPath)
	{
		Intent intent = new Intent(context, IntermediaryActivity.class);
		intent.putExtra("srcPath", srcPath);
		intent.putExtra("destPath", destPath);
		context.startActivity(intent);
	}
}
