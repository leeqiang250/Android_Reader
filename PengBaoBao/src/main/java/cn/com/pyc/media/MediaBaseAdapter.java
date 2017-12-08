package cn.com.pyc.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.qlk.util.global.GlobalLruCache;
import com.qlk.util.media.ISelection;
import com.qlk.util.tool.Util.ScreenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.utils.Dirs;

/**
 * 适用于本程序的Video、Music、Pdf、Image的Adapter <br>
 * 调用类退出时最好调用下本类的exit()方法
 * 
 * @author QiLiKing 2015-7-30 上午11:04:00
 */

public abstract class MediaBaseAdapter extends BaseAdapter implements ISelection
{
	private final ArrayList<String> mPaths;	// 数据源
	protected final Context mContext;
	protected final LayoutInflater mInflater;
	private Bitmap mDefaultBitmap;	// 默认显示的图片
	protected int mThumbSize;
	private BlockingQueue<String> mThumbTasks;
	private ThumbThread mThread1, mThread2;
	private int mScrollState;

	public MediaBaseAdapter(Context context, ArrayList<String> paths)
	{
		mContext = context;
		mPaths = paths;
		mInflater = LayoutInflater.from(context);
		if (isSupportThumbView())
		{
			mThumbSize = ScreenUtil.getScreenWidth(context) / 3;
			mDefaultBitmap = MyBitmapFactory.getResourcesThumbnail(context,
					R.drawable.media_default, mThumbSize);
			mThumbTasks = new LinkedBlockingQueue<>();
		}
		setSelectListener(null);
	}

	public void changeScrollState(AbsListView view, int state)
	{
		mScrollState = state;
		if (state == OnScrollListener.SCROLL_STATE_IDLE)
		{
			refresh(view);
		}
	}

	/**
	 * 是否支持缩略图浏览，如果是则需要重写getThumbTask
	 * 
	 * @return
	 */
	protected abstract boolean isSupportThumbView();

	/**
	 * 各个子类实现，如果不需要缩略图，则不用重写
	 * <p>
	 * 实现：需要将取到的缩略图存储于GLC中
	 * <p>
	 * 操作之前再次检查GLC中是否有缓存了
	 * 
	 * @param path
	 * @return
	 */
	protected Runnable getThumbTask(String path)
	{
		return null;
	}

	/**
	 * 结束线程
	 */
	public void exit()
	{
		if (mThread1 != null && mThread1.isAlive())
		{
			mThread1.interrupt();
		}
		if (mThread2 != null && mThread2.isAlive())
		{
			mThread2.interrupt();
		}
	}

	/**
	 * 只有这个方法才会加载缩略图
	 * 
	 *            要刷新的第一个item
	 *            从第一个item开始刷新total个
	 */
	public void refresh(AbsListView view)
	{
		if (!isSupportThumbView())
		{
			notifyDataSetChanged();
			return;
		}

		// 主要提防删除
		int first = view.getFirstVisiblePosition();
		int last = view.getLastVisiblePosition();
		if (last > mPaths.size() - 1)
		{
			last = mPaths.size() - 1;
		}

		/* 是否有缩略图需要更新 */
		mThumbTasks.clear();		// 清空已不可见的加载任务
		for (int i = first; i <= last; i++)
		{
			final String path = mPaths.get(i);
			Bitmap bm = GlobalLruCache.getGLC().get(path);
			if (bm == null || bm.isRecycled())
			{
				startThread();		// 1分钟超时，thread就被终止了
				mThumbTasks.offer(path);
			}
		}

		/*-
		 * 以上操作只有在新加入缩略图时才刷新，但如果是某些图片被删除则也需要更新
		 */
		notifyDataSetChanged();
	}

	private void startThread()
	{
		if (mThread1 == null || !mThread1.isAlive())
		{
			mThread1 = new ThumbThread();
			mThread1.start();
		}
		if (mThread2 == null || !mThread2.isAlive())
		{
			mThread2 = new ThumbThread();
			mThread2.start();
		}
	}

	private final Handler mRefreshHandler = new Handler(Looper.getMainLooper())
	{

		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			notifyDataSetChanged();
		}

	};

	class ThumbThread extends Thread
	{

		@Override
		public void run()
		{
			super.run();
			while(true)
			{
				try
				{
					String path = mThumbTasks.poll(1, TimeUnit.MINUTES);
					if (path == null)
					{
						break;	// 1分钟还没有新任务就退出线程，节省资源
					}
					getThumbTask(path).run();
					mRefreshHandler.sendEmptyMessage(0);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

	}

	// private class ThumbTask implements Runnable
	// {
	// private ReturnInfo info;
	//
	// /**
	// * desPath和desBuffer置空表示明文阅读，有一个不为空表示密文阅读
	// *
	// * @param srcPath
	// * @param desPath
	// * @param desBuffer
	// */
	// public ThumbTask(ReturnInfo info)
	// {
	// this.info = info;
	// }
	//
	// @Override
	// public void run()
	// {
	// // desBuffer或者desPath不为空时肯定是密文图片的缩略图
	// Bitmap thumb = null;
	// if (info.desBuffer != null)
	// {
	// thumb = MyBitmapFactory.getImageThumbnail(info.desBuffer,
	// mThumbSize);
	// }
	// else if (info.desPath != null)
	// {
	// thumb = MyBitmapFactory.getImageThumbnail(info.desPath,
	// mThumbSize);
	// }
	// else
	// {
	// if (type.equals(GlobalData.Video))
	// {
	// thumb = MyBitmapFactory.getVideoThumbnail(info.srcPath,
	// mThumbSize);
	// }
	// else
	// {
	// thumb = MyBitmapFactory.getImageThumbnail(info.srcPath,
	// mThumbSize);
	// }
	// }
	//
	// // 有的图片不能生成缩略图，put的话会出错
	// if (thumb != null)
	// {
	// GlobalLruCache.getGLC().put(info.srcPath, thumb);
	// PbbBaseActivity.UIHandler.post(refreshRunnable);
	// }
	// }
	// }

	// 因缩略图是异步加载，所以在未得出缩略图之前要显示默认图片
	protected Bitmap getBitmap(int position)
	{
		String path = mPaths.get(position);
		Bitmap bm = GlobalLruCache.getGLC().get(path);
		if (bm == null || bm.isRecycled())
		{
			bm = mDefaultBitmap;

			if (mScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
			{
				startThread();		// 1分钟超时，thread就被终止了
				mThumbTasks.offer(path);
			}
		}
		return bm;
	}

	@Override
	public int getCount()
	{
		return mPaths.size();
	}

	@Override
	public String getItem(int position)
	{
		return mPaths.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	/*-*********************************************
	 * TODO Select
	 ***********************************************/

	private ISelectListener mSelectListener;
	protected final HashSet<String> mSelectPaths = new HashSet<>();
	/** Used by subclass. */
	protected boolean isSelectable = false;

	@Override
	public void setSelectListener(ISelectListener listener)
	{
		mSelectListener = listener;
		if (mSelectListener == null)
		{
			mSelectListener = new ISelectListener()
			{
				@Override
				public void onSelcetChanged(boolean overflow, int total, boolean allSelected)
				{
					/* Do not care the listener's value is null when using. */
				}
			};
		}
	}

	@Override
	public void setSelectable(boolean selectable)
	{
		this.isSelectable = selectable;
		/* In select mode, user may call setItemSelect(). */
		if (!selectable)
		{
			mSelectPaths.clear();
		}

		notifyDataSetChanged();
		mSelectListener.onSelcetChanged(false, mSelectPaths.size(),
				mSelectPaths.size() == mPaths.size());
	}

	@Override
	public void clearSelected()
	{
		mSelectPaths.clear();
		notifyDataSetChanged();
		mSelectListener.onSelcetChanged(false, mSelectPaths.size(),
				mSelectPaths.size() == mPaths.size());
	}

	@Override
	public boolean isSelecting()
	{
		return isSelectable;
	}

	@Override
	public void setItemSelected(int position)
	{
		if (position < 0)
		{
			return;
		}
		String path = getItem(position);
		if (mSelectPaths.contains(path))
		{
			mSelectPaths.remove(path);
		}
		else
		{
			mSelectPaths.add(path);
		}

		notifyDataSetChanged();
		mSelectListener.onSelcetChanged(isSpaceOverflow(mSelectPaths), mSelectPaths.size(),
				mSelectPaths.size() == mPaths.size());
	}

	@Override
	public Collection<String> getSelected()
	{
		return mSelectPaths;
	}

	private boolean isSpaceOverflow(Collection<String> paths)
	{
		long needDiskSpace = 0;
		for (String path : paths)
		{
			needDiskSpace += new File(path).length();
		}
		ArrayList<String> cardsPaths = Dirs.getCardsPaths();
		for (String dir : cardsPaths)
		{
			if (new File(dir).getUsableSpace() > needDiskSpace)
			{
				return false;
			}
		}
		return true;
	}

	/** Used by subclass. */
	protected OnCheckedChangeListener onCheckedChangedListener = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			String path = (String) buttonView.getTag();
			if (isChecked)
			{
				mSelectPaths.add(path);
			}
			else
			{
				mSelectPaths.remove(path);
			}
			mSelectListener.onSelcetChanged(isSpaceOverflow(mSelectPaths), mSelectPaths.size(),
					mSelectPaths.size() == mPaths.size());
		}
	};

}
