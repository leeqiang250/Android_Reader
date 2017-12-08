package cn.com.pyc.global;

import android.content.Context;

import cn.com.pyc.media.MediaFile;
import cn.com.pyc.media.PycImage;
import cn.com.pyc.media.PycMusic;
import cn.com.pyc.media.PycPdf;
import cn.com.pyc.media.PycSm;
import cn.com.pyc.media.PycVideo;

/**
 * 本程序中用到的刷新等路径数据皆存储在这里面
 * 
 * @author QiLiKing 2014-12-5
 */
public enum GlobalData
{
	Image, Video, Music, Pdf, Sm, All;

	private MediaFile mediaFile;

	public MediaFile instance(Context context)
	{
		if (mediaFile == null)
		{
			switch (this)
			{
				case Image:
					mediaFile = new PycImage(context);
					break;

				case Video:
					mediaFile = new PycVideo(context);
					break;

				case Music:
					mediaFile = new PycMusic(context);
					break;

				case Pdf:
					mediaFile = new PycPdf(context);
					break;

				case Sm:
					mediaFile = new PycSm(context);
					break;

				default:
					break;
			}
		}
		return mediaFile;
	}

	/**
	 * @param path
	 * @return null 没有找到匹配项
	 */
	public static MediaFile ensure(Context context, String path)
	{
		if (PycImage.isSameType1(path))
		{
			return Image.instance(context);
		}
		else if (PycVideo.isSameType1(path))
		{
			return Video.instance(context);
		}
		else if (PycMusic.isSameType1(path))
		{
			return Music.instance(context);
		}
		else if (PycPdf.isSameType1(path))
		{
			return Pdf.instance(context);
		}
		else if (PycSm.isSameType1(path))
		{
			return Sm.instance(context);
		}
		else
		{
			return null;
		}
	}

	/**
	 * 这一步会不会起很多个线程啊，造成系统卡顿
	 * 
	 * @param context
	 * @param isCipher
	 */
	public static void searchTotal(Context context, boolean isCipher)
	{
		Image.instance(context).search(isCipher);
		Video.instance(context).search(isCipher);
		Pdf.instance(context).search(isCipher);
		Music.instance(context).search(isCipher);
		Sm.instance(context).search(isCipher);
	}

	// 初始化每个多媒体的查询类。
	// 每个多媒体的类型，都继承MediaFile类。
	// 而父类MediaFile类，继承QlkMedia类
	// 我们查询调用QlkMedia中的searchFromSysDB方法获取。
	public static void searchPlainsFromSysDB(Context context)
	{
		Image.instance(context).searchFromSysDB();
		Video.instance(context).searchFromSysDB();
		Pdf.instance(context).searchFromSysDB();
		Music.instance(context).searchFromSysDB();
		Sm.instance(context).searchFromSysDB();
	}

	public static int getTotalCount(Context context, boolean isCipher)
	{
		return Image.instance(context).getCopyPaths(isCipher).size()
				+ Video.instance(context).getCopyPaths(isCipher).size()
				+ Pdf.instance(context).getCopyPaths(isCipher).size()
				+ Music.instance(context).getCopyPaths(isCipher).size();
	}
}
