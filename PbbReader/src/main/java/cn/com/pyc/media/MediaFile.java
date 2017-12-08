package cn.com.pyc.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore.MediaColumns;

import com.qlk.util.event.PathsEvent;
import com.qlk.util.global.GlobalDialog;
import com.qlk.util.global.GlobalDialog.DialogInfo;
import com.qlk.util.global.GlobalObserver;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.media.QlkMedia;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cn.com.pyc.db.PathDao;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.utils.Dirs;
import de.greenrobot.event.EventBus;

public abstract class MediaFile extends QlkMedia
{
	public MediaFile(Context context)
	{
		super(context);
	}

	// .pyc文件夹下的文件（外发的或者加密的）
	protected final Vector<String> Ciphers = new Vector<String>();

	private int newNum;	// 加密完成后新增的文件数据。用在隐私空间中。

	/**
	 * 删除指定文件，带有提示框
	 * 
	 * @param activity
	 * @param paths
	 */
	public static void deleteFiles(final Activity activity, final String... paths)
	{
		String path = paths[0];
		String prompt = PycImage.isSameType1(path) ? "图片" : PycPdf.isSameType1(path) ? "文档"
				: PycVideo.isSameType1(path) ? "视频" : "文件";
		DialogInfo info = new DialogInfo();
		info.prompt = "是否要永久删除此" + prompt + "？";
		info.positiveBtnText = "删除";
		info.positiveTask = new Runnable()
		{
			@Override
			public void run()
			{
				GlobalData.ensure(activity, paths[0]).delete(paths);
			}
		};
		GlobalDialog.showConfirmDialog(activity, info);
	}

	/**
	 * @param increase
	 *            -1表示要清空
	 * @return 累加后的数目
	 */
	public int changeNewNum(int increase)
	{
		if (increase == -1)
		{
			newNum = 0;
		}
		else
		{
			newNum += increase;
		}
		return newNum;
	}

	/**
	 * 返回的是一个Copy
	 */
	public ArrayList<String> getCopyPaths(boolean isCipher)
	{
		return new ArrayList<>(isCipher ? Ciphers : SortedPaths);
	}

	public void add(int pos, String path, boolean isCipher)
	{
		if (isCipher)
		{
			Ciphers.add(pos, path);
		}
		else
		{
			SortedPaths.add(pos, path);
			insertToSysDBAsync(path);
		}
	}

	public void search(boolean isCipher)
	{
		if (isCipher)
		{
			searchCiphers();
		}
		else
		{
			search();
		}
	}

	/**
	 * 加解密
	 * 
	 * @param context
	 * @param mapPaths
	 */
	public void xCode(final Context context, final HashMap<String, String> mapPaths)
	{
		final PathDao pathDao = PathDao.getInstance(context);
		final ContentResolver resolver = context.getContentResolver();
		final ContentValues values = new ContentValues();
		Iterator<String> iterator = mapPaths.keySet().iterator();
		while(iterator.hasNext())
		{
			final String key = iterator.next();
			final String value = mapPaths.get(key);
			if (key.contains("/.pyc/"))
			{
				/* 解密 */
				SortedPaths.add(0, value);
				Ciphers.remove(key);
				GlobalTask.executeBackground(new Runnable()
				{
					@Override
					public void run()
					{
						pathDao.delete(key);
						values.put(MediaColumns.DATA, value);
						resolver.insert(getMediaUri(), values);
					}
				});
			}
			else
			{
				/* 加密 */
				SortedPaths.remove(key);
				Ciphers.add(0, value);
				GlobalTask.executeBackground(new Runnable()
				{
					@Override
					public void run()
					{
						pathDao.insert(key, value);
						resolver.delete(getMediaUri(), MediaColumns.DATA + "=?", new String[]
						{ key });
					}
				});
			}
		}

	}

	/*-*****************************************
	 * TODO 搜索密文
	 *******************************************/

	@SuppressLint("UseSparseArrays")
	private final HashMap<Long, ArrayList<String>> mSearchCiphers = new HashMap<Long, ArrayList<String>>();

	private boolean isSearching = false;

	private void searchCiphers()
	{
		// 如果钥匙为空，则没必要搜索
		if (isSearching || UserDao.getDB(mContext).getUserInfo().isKeyNull())
		{
			return;
		}

		isSearching = true;
		GlobalTask.executeBackground(new Runnable()
		{
			@Override
			public void run()
			{
				mSearchCiphers.clear();

				ArrayList<String> cardsPaths = Dirs.getCardsPaths();
				for (String boot : cardsPaths)
				{
					new File(Dirs.getUserDir(mContext, boot)).listFiles(filter);
				}
				HashMap<Long, ArrayList<String>> map = mSearchCiphers;
				ArrayList<String> paths = new ArrayList<String>();
				if (map.size() > 0)
				{
					Long[] times = map.keySet().toArray(new Long[] {});
					Arrays.sort(times);
					for (int i = times.length - 1; i >= 0; i--)
					{
						paths.addAll(map.get(times[i]));
					}
				}
				Ciphers.clear();
				Ciphers.addAll(paths);
				notifyRefresh();
				isSearching = false;
				mSearchCiphers.clear();
				EventBus.getDefault().post(new PathsEvent(PathsEvent.P_CLIPER));
			}
		});

	}

	private FileFilter filter = new FileFilter()
	{

		@Override
		public boolean accept(File file)
		{
			String path = file.getAbsolutePath();
			if (file.isFile())
			{
				if (isSameType2(path))
				{
					long time = file.lastModified();
					if (mSearchCiphers.get(time) == null)
					{
						mSearchCiphers.put(time, new ArrayList<String>());
					}
					mSearchCiphers.get(time).add(path);
				}
			}
			else
			{
				new File(path).listFiles(filter);
			}
			return false;
		}
	};

	@Override
	protected void onSearchFinished(boolean autoToast)
	{
		if (autoToast)
		{
			String str = this instanceof PycImage ? " 张图片" : this instanceof PycPdf ? " 个PDF文件"
					: this instanceof PycVideo ? " 个视频" : this instanceof PycMusic ? " 首音乐"
							: " 个文件";
			GlobalToast.toastInQueue(mContext, "搜索到 " + SortedPaths.size() + str);
		}
		notifyRefresh();
	}

	@Override
	public ArrayList<String> delete(String... delPaths)
	{
		LogerEngine.debug(mContext,"删除文件方法执行",null);
		ArrayList<String> del = super.delete(delPaths);
		if (!SortedPaths.removeAll(del))
		{
			Ciphers.removeAll(del);
		}

		GlobalObserver.getGOb().postNotifyObservers(ObTag.Delete);
		//GlobalToast.toastShort(mContext, "删除成功");

		PathDao dao = PathDao.getInstance(mContext);
		dao.delete(del);

		return del;
	}

	protected void notifyRefresh()
	{
		GlobalObserver.getGOb().postNotifyObservers(ObTag.Refresh);
	}
}
