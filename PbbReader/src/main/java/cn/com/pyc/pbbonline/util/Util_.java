package cn.com.pyc.pbbonline.util;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.sz.mobilesdk.SZDBInterface;
import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.authentication.SZContent;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.mobilesdk.util.UIHelper;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.bean.event.RefreshShareRecordEvent;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.common.ShareMode;
import cn.com.pyc.pbbonline.db.Shared;
import cn.com.pyc.pbbonline.db.SharedDBManager;
import de.greenrobot.event.EventBus;

/**
 * 本app常用或公用的方法函数
 * 
 * @author hudq
 */
public class Util_
{

	private static final String TAG = "Util_";

	/**
	 * 是否已经登录
	 * 
	 * @return
	 */
	public static boolean isLogin()
	{
		String loginName = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, ""); //登录手机号
		String loginToken = (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""); //登录token
		return !TextUtils.isEmpty(loginName) && !TextUtils.isEmpty(loginToken);
	}

	/**
	 * 是否已经保存过第三方网页跳转参数（网页打开app跳转）
	 * 
	 * @return
	 */
	public static boolean isWebBrowser()
	{
		String source = (String) SPUtil.get(Fields.FIELDS_WEB_SOURCE, "");
		String weixin = (String) SPUtil.get(Fields.FIELDS_WEB_WEIXIN, "");
		return !TextUtils.isEmpty(source) && !K.SCANCODE.equals(source)
				&& !TextUtils.isEmpty(weixin);
	}

	/**
	 * 清除保存过第三方网页跳转参数<br/>
	 * 用账号正常流程登录，清除保存的值
	 */
	public static void clearWebBrowserParams()
	{
		SPUtil.remove(Fields.FIELDS_WEB_SOURCE);
		SPUtil.remove(Fields.FIELDS_WEB_WEIXIN);
	}

	/**
	 * 检验手机号是否是被分享者
	 * 
	 * @param phone
	 * @param callback
	 */
	public static Cancelable receiveVerifyShared(String phone,
			Callback.CommonCallback<String> callback)
	{
		Bundle bundle = new Bundle();
		bundle.putString("phone", phone);
		bundle.putString("shareId", (String) SPUtil.get(Fields.FIELDS_ID, ""));
		bundle.putString("deviceIdentifier", Constant.TOKEN);
		bundle.putString("myToken", (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""));
		return GlobalHttp.get(APIUtil.receiverByPhoneUrl(), bundle, callback);
	}

	/**
	 * 初始化数据和数据库。<br/>
	 * 打开分享时，必须先初始化数据库和相关的保存值 <br/>
	 * <br/>
	 * 一般建议放在子线程中执行。
	 * 
	 * @param shareUrl
	 *            分享链接
	 * @param isScanning
	 *            是否是扫一扫时，进行初始化
	 * @return {@see Boolean}
	 */
	public static boolean initCommonDataDB(String shareUrl, boolean isScanning)
	{
		SZLog.v("sharedUrl", "" + shareUrl);
		long startT = System.currentTimeMillis();
		String ShareID = StringUtil.getStringByResult(shareUrl, "shareId=");
		if (shareUrl.contains("id=") || shareUrl.contains("ID="))
		{
			ShareID = StringUtil.getStringByResult(shareUrl, "id=");
		}
		if (TextUtils.isEmpty(ShareID))											//shareId不可为空
			return false;
		if (isScanning)
		{
			//扫码进入，已删除的分享需要再次显示
			SharedDBManager dbManager = new SharedDBManager();
			Shared mSave = dbManager.findByShareId(ShareID);
			if (mSave != null && mSave.isDelete())
			{
				mSave.setDelete(false);
				dbManager.updateDeleteFlag(mSave);
				EventBus.getDefault().post(new RefreshShareRecordEvent());
			}
		}
		SPUtil.save(Fields.FIELDS_WEB_SOURCE, isScanning ? K.SCANCODE : "");	//APP直接扫码跳转注册或登录，source="appQrcode"
		SPUtil.remove(Fields.FIELDS_WEB_WEIXIN);
		SPUtil.remove(Fields.FIELDS_RECEIVE_ID);
		final String receiveIdStr = "receiveId=";
		if (shareUrl.contains(receiveIdStr) || shareUrl.contains("receiveID=")
				|| shareUrl.contains("ReceiveID="))
		{
			String ReceiveID = StringUtil.getStringByResult(shareUrl, receiveIdStr);
			SPUtil.save(Fields.FIELDS_RECEIVE_ID, ReceiveID);
		}
		String ShareType = StringUtil.getStringByResult(shareUrl, "shareType=");
		ShareMode.Mode.value = TextUtils.isEmpty(ShareType) ? ShareMode.SHAREDEVICE : ShareType;

		//1.解析所需字段值；userName默认为guestpbb。文件权限校验时会使用账号校验
		String UserName = Fields.GUEST_PBB;
		SPUtil.save(Fields.FIELDS_SCAN_URL, shareUrl);		// 保存分享的url
		SPUtil.save(Fields.FIELDS_ID, ShareID);				// 保存分享的shareId
		SZInitInterface.saveUserName(UserName);				// 保存分享获取的用户名，默认为guestpbb

		//2.创建相关
		SZDBInterface.destoryDBHelper();
		SZInitInterface.destoryFilePath();
		SZInitInterface.createFilePath(UserName + Fields._LINE + ShareID);
		boolean createDb = SZDBInterface.createDB();
		SZLog.w("init", "handle time is " + (System.currentTimeMillis() - startT) + " ms");
		return createDb;
	}

	/**
	 * 使用第三方扫码后，打开app时使用！ <br/>
	 * 打开reader后，解析数值初始化数据和数据库。<br/>
	 * <br/>
	 * 一般建议放在子线程中执行。
	 * 
	 * @param browserUrl
	 *            browser返回的url <br/>
	 *            eg：pbbreader://type==wx&&qrurl==http://192.168.85.74:80/
	 *            PBBOnline/client/content/getShareInfo?shareId=0028cc3a-6676-4
	 *            ac1-972a-aad94cce8051
	 *            &shareType=shareuser&&source==webQrcodeApp&&weixin==undefined
	 * @return {@see Boolean}
	 */
	public static boolean initBrowserDataDB(String browserUrl)
	{
		SZLog.v("browserUrl", "" + browserUrl);
		long startT = System.currentTimeMillis();

		//1.解析所需字段值；userName默认为guestpbb。文件权限校验时会使用账号校验
		String UserName = Fields.GUEST_PBB;
		String ShareID = StringUtil.getStringByResult(browserUrl, "shareId=");
		if (TextUtils.isEmpty(ShareID))				//shareId不可为空
			return false;
		////String type = StringUtil.getStringByResult(browserUrl, "type==");
		String qrurl = StringUtil.getStringByResult(browserUrl, "qrurl==");
		String source = StringUtil.getStringByResult(browserUrl, "source==");
		String weixin = StringUtil.getStringByResult(browserUrl, "weixin==");
		SPUtil.save(Fields.FIELDS_WEB_SOURCE, source);
		SPUtil.save(Fields.FIELDS_WEB_WEIXIN, weixin);

		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(qrurl);
		urlBuilder.append("&userName=" + UserName);
		urlBuilder.append("&systemType=PBBONLINE");

		SPUtil.remove(Fields.FIELDS_RECEIVE_ID);
		String ReceiveID = StringUtil.getStringByResult(browserUrl, "receiveId=");
		if (!TextUtils.isEmpty(ReceiveID))
		{
			urlBuilder.append("&receiveId=" + ReceiveID);
			SPUtil.save(Fields.FIELDS_RECEIVE_ID, ReceiveID);
		}
		String ShareType = StringUtil.getStringByResult(browserUrl, "shareType=");
		ShareMode.Mode.value = TextUtils.isEmpty(ShareType) ? ShareMode.SHAREDEVICE : ShareType;
		urlBuilder.append("&shareType=" + ShareType);

		SZLog.v("ShareUrl", urlBuilder.toString());
		SPUtil.save(Fields.FIELDS_SCAN_URL, urlBuilder.toString());		// 保存分享的url
		SPUtil.save(Fields.FIELDS_ID, ShareID);							// 保存分享的shareId
		SZInitInterface.saveUserName(UserName);							// 保存分享获取的用户名，默认为guestpbb

		//2.创建相关
		SZDBInterface.destoryDBHelper();
		SZInitInterface.destoryFilePath();
		SZInitInterface.createFilePath(UserName + Fields._LINE + ShareID);
		boolean createDb = SZDBInterface.createDB();
		SZLog.w("init", "parser time is " + (System.currentTimeMillis() - startT) + " ms");
		return createDb;
	}

	/**
	 * 删除对应sharId的本地数据和数据库中的数据<br/>
	 * <br/>
	 * 一般建议放在后台线程中执行。
	 * 
	 * @param userName
	 * @param shareId
	 */
	public static void deleteCommonDataDB(String userName, String shareId)
	{
		// 清空数据库中的表数据
		SZDBInterface.destoryDBHelper();
		SZInitInterface.saveUserName(userName);
		SPUtil.save(Fields.FIELDS_ID, shareId);
		SZDBInterface.createDB();
		SZDBInterface.deleteAllTableData();								// 删除数据表
		SZDBInterface.dropDatabase(userName + Fields._LINE + shareId);		// 删除数据库
		
		String _path = DirsUtil.getSavePath(userName, shareId);				
		FileUtil.delAllFile(_path);										// 删除下载文件，如果有下载的话
	}

	/**
	 * 获取当前文件对应的位置
	 * 
	 * @param curFileId
	 * @param szFiles
	 * @return
	 */
	public static int getStartIndex(String curFileId, List<SZFile> szFiles)
	{
		//计算播放位置。
		int startPos = 0, size = szFiles.size();
		for (int i = 0; i < size; i++)
		{
			SZFile szFile = szFiles.get(i);
			SZLog.w(TAG, "contentId : " + szFile.getContentId());
			if (curFileId.equals(szFile.getContentId()))
			{
				startPos = i;
				break;
			}
		}
		return startPos;
	}

	/**
	 * 获取文件信息，包含约束信息
	 * 
	 * @param ac
	 * @return
	 */
	public static SZFile getSZFile(AlbumContent ac)
	{
		SZInitInterface.checkFilePath();
		String path = new StringBuilder()
				.append(PathUtil.DEF_SAVE_FILE_PATH)
				.append(File.separator)
				.append(ac.getMyProId())
				.append(File.separator)
				.append(ac.getContent_id())
				.append(".")
				.append(ac.getFileType().toLowerCase(Locale.getDefault())).toString();
		//String[] _pid = { "_id" };
		//String[] _pidValue = { ac.getAsset_id() };
		//Asset asset = (Asset) AssetDAOImpl.getInstance().findByQuery(_pid, _pidValue, Asset.class)
		//		.get(0);

		SZContent szcont = new SZContent(ac.getAsset_id());
		String validityTime = FormatterUtil.getLeftAvailableTime(szcont.getAvailbaleTime());
		String odd_datetime_end = FormatterUtil.getToOddEndTime(szcont.getOdd_datetime_end());
		String key = szcont.checkOpen() ? szcont.getCek_cipher_value() : "";
		//Album a = AlbumDAOImpl.getInstance().findAlbumByMyproId(myProId);

		SZFile file = new SZFile(ac.getName(), path, key);
		//file.setAlbum_id(ac.getAlbum_id());
		//file.setAlbum_pic(a != null ? a.getPicture() : "");
		file.setMyProId(ac.getMyProId());
		file.setContentId(ac.getContent_id());
		file.setAssetId(ac.getAsset_id());
		file.setCheckOpen(szcont.checkOpen());
		file.setOdd_datetime_end(odd_datetime_end);
		file.setValidity_time(validityTime);
		file.setFormat(ac.getFileType());
		return file;
	}
	
	/**
	 * 分集下载文件后，保存的专辑有重复，但myProId唯一
	 * 
	 * @param albums
	 * @return
	 */
	public static List<Album> wipeRepeatAlbumData(List<Album> albums)
	{
		List<Album> list = new ArrayList<Album>();
		String id = null;
		for (Album album : albums)
		{
			if (!TextUtils.equals(id, album.getMyproduct_id()))
			{
				list.add(album);
			}
			id = album.getMyproduct_id();
		}
		return list;
	}

	/**
	 * 检查文件是否存在本地
	 * 
	 * @param ac
	 * @param filePath
	 * @param callback
	 */
	public static boolean checkFileExist(Context ac, String filePath,
			UIHelper.DialogCallBack callback)
	{
		boolean exist = true;
		if (!FileUtil.checkFilePathExists(filePath))
		{
			exist = false;
			SZLog.e(TAG, "文件不存在：" + filePath);
			UIHelper.showSingleCommonDialog(ac, "", ac.getString(R.string.fail_to_open_file),
					ac.getString(R.string.close), callback);
		}
		return exist;
	}
	
	/**
	 * 是否需要迁移复制数据到指定目录下
	 * 
	 * @param context
	 * @return 
	 * 			true需要迁移复制，false反之
	 */
	public static final boolean checkCopyData(Context context)
	{
		if ((boolean) SPUtil.get("online.copy", false))
			return false;
		if ("cn.com.pyc.pbb".equals(context.getPackageName()))
		{
			final String targetPath = PathUtil.getSDCard() + "/" + PathUtil.getSZOffset();
			final String srcPath = PathUtil.getSDCard() + "/Android/data/SZOnline";
			if (new File(targetPath).exists())
			{
				SZLog.e(TAG, "目标文件夹已存在: " + targetPath);
				return false;
			}
			if (!new File(srcPath).exists())
			{
				SZLog.e(TAG, "源文件夹不存在: " + srcPath);
				return false;
			}
			return true;
		}
		return false;
	}

}
