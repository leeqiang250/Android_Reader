package com.sz.mobilesdk.util;

import com.sz.mobilesdk.authentication.SZContent;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.bean.ContentRight;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.database.practice.AssetDAOImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 已废弃使用
 * <p>
 * 
 */
@Deprecated
public class MediaUtils
{
	private static final String TAG = MediaUtils.class.getSimpleName();

	// pdf,music,video
	private List<ContentRight> mediaRight = new ArrayList<ContentRight>();
	private List<AlbumContent> mediaList = new ArrayList<AlbumContent>();
	private List<Asset> assetList = new ArrayList<Asset>();

	private static volatile MediaUtils instance = new MediaUtils();

	private MediaUtils()
	{
	}

	public static MediaUtils getInstance()
	{
		return instance;
	}

	/**
	 * 
	 * @param albumID
	 */
	@SuppressWarnings("unchecked")
	public void initMedia(String albumID)
	{
		mediaList.clear();// 子专辑的集合。
		mediaRight.clear();// 子专辑权限集合。
		assetList.clear();// 子专辑解密集合。
		String[] pid = { "album_id" };
		String[] pidvalue = { albumID };
		mediaList = (List<AlbumContent>) AlbumContentDAOImpl.getInstance()
				.findByQuery(pid, pidvalue, AlbumContent.class);
		for (int i = 0; i < mediaList.size(); i++)
		{
			String[] _pid = { "_id" };
			String[] _pidvalue = { mediaList.get(i).getAsset_id() };
			Asset tmp = (Asset) AssetDAOImpl.getInstance()
					.findByQuery(_pid, _pidvalue, Asset.class).get(0);
			assetList.add(tmp);
			SZContent szcont = new SZContent(mediaList.get(i).getAsset_id());
			ContentRight right = new ContentRight();
			right.order = i;// 子专辑的标识
			right.permitted = szcont.checkOpen();// 是否鉴权
			right.availableTime = szcont.getAvailbaleTime();// 获取剩余时间
			right.odd_datetime_end = szcont.getOdd_datetime_end();
			mediaRight.add(right);

			SZLog.d(TAG, "availableTime: " + right.availableTime);
			SZLog.d(TAG, "permitted: " + right.permitted);
			SZLog.d(TAG, "odd_datetime_end: " + right.odd_datetime_end);
		}
	}

	// @SuppressWarnings("unchecked")
	// public void initPdf(Context context, String albumID)
	// {
	// pdfmediaList.clear();// 子专辑的集合。
	// pdfmediaRight.clear();// 子专辑权限集合。
	// pdfassetList.clear();// 子专辑解密集合。
	// String[] pid = { "album_id" };
	// String[] pidvalue = { albumID };
	// pdfmediaList = (List<AlbumContent>)
	// AlbumContentDAOImpl.getInstance().findByQuery(pid, pidvalue,
	// AlbumContent.class);
	// for (int i = 0; i < pdfmediaList.size(); i++)
	// {
	// String[] _pid1 = { "_id" };
	// String[] _pidvalue1 = { pdfmediaList.get(i).getAsset_id() };
	// Asset tmp = (Asset) AssetDAOImpl.getInstance().findByQuery(_pid1,
	// _pidvalue1, Asset.class).get(0);
	// pdfassetList.add(tmp);
	// SZContent szcont = new SZContent(pdfmediaList.get(i).getAsset_id());
	// ContentRight right = new ContentRight();
	// right.order = i;// 子专辑的标识
	// right.permitted = szcont.checkOpen();// 是否鉴权
	// right.availableTime = szcont.getAvailbaleTime();// 获取剩余时间
	// right.odd_datetime_end = szcont.getOdd_datetime_end();
	// pdfmediaRight.add(right);
	//
	// SZLog.d(TAG, "availableTime: " + right.availableTime);
	// SZLog.d(TAG, "permitted: " + right.permitted);
	// }
	// }

	public List<AlbumContent> getMediaList()
	{
		return mediaList;
	}

	public List<ContentRight> getMediaRight()
	{
		return mediaRight;
	}

	public List<Asset> getAssetList()
	{
		return assetList;
	}

	public void setAssetList(List<Asset> assetList)
	{
		this.assetList = assetList;
	}

	public void addMusicRight(ContentRight right)
	{
		this.mediaRight.add(right);
	}

	// ////////////////////////////////////////////
	// ////////////////////////////////////////////
	// ////////////////////////////////////////////

	// public List<ContentRight> getPdfmediaRight()
	// {
	// return pdfmediaRight;
	// }
	//
	// public void setPdfmediaRight(List<ContentRight> pdfmediaRight)
	// {
	// this.pdfmediaRight = pdfmediaRight;
	// }
	//
	// public List<AlbumContent> getPdfmediaList()
	// {
	// return pdfmediaList;
	// }
	//
	// public void setPdfmediaList(List<AlbumContent> pdfmediaList)
	// {
	// this.pdfmediaList = pdfmediaList;
	// }
	//
	// public List<Asset> getPdfassetList()
	// {
	// return pdfassetList;
	// }
	//
	// public void setPdfassetList(List<Asset> pdfassetList)
	// {
	// this.pdfassetList = pdfassetList;
	// }

}