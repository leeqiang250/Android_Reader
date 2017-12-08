package cn.com.pyc.reader.image;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.qlk.util.global.GlobalLruCache;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.tool.Util.ScreenUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.media.MyBitmapFactory;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.reader.ReaderBaseActivity;
import cn.com.pyc.xcoder.XCoder;

public class ImageReaderActivity extends ReaderBaseActivity
{
	private TextView mNumView;
	private TextView mTitleView;
	private ImageReaderViewPager mViewPager;
	private MyViewPagerAdapter mAdapter;

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_reader_image);
		LogerEngine.info(getApplication(),"查看图片",null);
		findViewAndSetListeners();

		initView();
	}

	@Override
	public void findViewAndSetListeners()
	{
		mNumView = (TextView) findViewById(R.id.ari_txt_num);
		mTitleView = (TextView) findViewById(R.id.ari_txt_title);
		mViewPager = (ImageReaderViewPager) findViewById(R.id.ari_vpg_image);

		findViewById(R.id.ari_imb_clockwise_rotation).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						((ImageZoomView) mViewPager.findViewWithTag(mCurPos))
								.rotate(90);
					}
				});
		findViewById(R.id.ari_imb_decrypt).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						decrypt(mCurPath);
					}
				});
		findViewById(R.id.ari_imb_delecte).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						delete(mCurPath);
					}
				});
		findViewById(R.id.ari_imb_send).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						send(mCurPath);
					}
				});
		findViewById(R.id.ari_imb_share).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						shareSmFile(mCurPath);
					}
				});
	}

	private void initView()
	{
		if (isCipher)
		{
			findViewById(R.id.ari_imb_decrypt).setVisibility(View.VISIBLE);
			findViewById(R.id.ari_imb_delecte).setVisibility(View.VISIBLE);
			findViewById(R.id.ari_imb_send).setVisibility(View.VISIBLE);
		}
		if (isFromSm)
		{
			showWaterView((TextView) findViewById(R.id.ari_txt_water));
		}
		else
		{
			findViewById(R.id.ari_imb_share).setVisibility(View.VISIBLE);
		}

		mAdapter = new MyViewPagerAdapter(this);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(mCurPos);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int arg0)
			{
				onCurrentPageChanged();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{
			}
		});
		onCurrentPageChanged();	// 首次进入刷新
	}

	/*-
	 * 刷新界面等操作
	 */
	private void onCurrentPageChanged()
	{
		mCurPos = mViewPager.getCurrentItem();
		mCurPath = mPaths.get(mCurPos);
		mNumView.setText((mCurPos + 1) + "/" + mPaths.size());
		mTitleView.setText(FileUtil.getFileName(mCurPath));

		mAdapter.loadPic();	// 加载图片
	}

	/*-*****************************
	 * TODO adapter
	 *******************************/

	private class MyViewPagerAdapter extends PagerAdapter
	{
		// 这里存入的GLC的路径会和缩略图冲突，故加上一个前缀
		private static final String LRU_DIR = "cache:";
		private Bitmap mDefaultBmp;
		private final int BMP_WIDTH;
		private final int BMP_HEIGHT;
		private final LinkedBlockingQueue<String> mWaitPaths = new LinkedBlockingQueue<>();
		private ImageThread mThread;
		private int mOldIndex = mCurPos;

		public MyViewPagerAdapter(Context context)
		{
			BMP_HEIGHT = ScreenUtil.getScreenHeight(context);
			BMP_WIDTH = ScreenUtil.getScreenWidth(context);

			mDefaultBmp = MyBitmapFactory.getResourcesThumbnail(context,
					R.drawable.media_default, BMP_WIDTH);
		}

		public void loadPic()
		{
			if (mThread == null || !mThread.isAlive())
			{
				mThread = new ImageThread();
				mThread.start();
			}
			mWaitPaths.clear();
			mWaitPaths.offer(mCurPath);
			String previous = mCurPath;
			String next = mCurPath;
			int cur = mCurPos;
			if (cur < mPaths.size() - 1)
			{
				next = mPaths.get(cur + 1);
			}
			if (cur > 0)
			{
				previous = mPaths.get(cur - 1);
			}
			/* 根据滑动方向选择加载顺序 */
			mWaitPaths.offer(mCurPos > mOldIndex ? next : previous);
			mWaitPaths.offer(mCurPos > mOldIndex ? previous : next);
			mOldIndex = mCurPos;

			notifyDataSetChanged();
		}

		@Override
		public int getItemPosition(Object object)
		{
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			container.removeView((ImageZoomView) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			ImageZoomView izv = new ImageZoomView(getApplicationContext());
			izv.setBitmap(getBitmap(position));
			izv.setTag(position);	// 图片旋转的时候用到
			container.addView(izv);
			return izv;
		}

		private Bitmap getBitmap(int position)
		{
			String path = mPaths.get(position);
			Bitmap bm = GlobalLruCache.getGLC().get(LRU_DIR + path);
			if (bm != null && !bm.isRecycled())
			{
				return bm;
			}
			else
			{
				return mDefaultBmp;
			}
		}

		@Override
		public int getCount()
		{
			return mPaths.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return arg0 == arg1;
		}

		private final Handler mHandler = new Handler(getMainLooper())
		{
			@Override
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);
				notifyDataSetChanged();
				if (isFromSm)
				{
					//加载完之后再countDown
					showLimitView((TextView) findViewById(R.id.ari_txt_countdown));
				}
			}
		};

		class ImageThread extends Thread
		{
			private XCoder mXCoder;

			@Override
			public void run()
			{
				super.run();
				while(true)
				{
					Bitmap bmp = null;
					try
					{
						String path = mWaitPaths.poll(1, TimeUnit.MINUTES);
						if (path == null)
						{
							break;
						}
						bmp = GlobalLruCache.getGLC().get(LRU_DIR + path);
						if (bmp != null && !bmp.isRecycled())
						{
							continue;
						}
						bmp = null;		// 如果recycle了，置空
						if (isFromSm)
						{
							byte[] data = XCoder.readSmImage(path,
									smInfo.getEncodeKey());
							if (data != null)
							{
								bmp = MyBitmapFactory.getBitmap(data,
										BMP_WIDTH, BMP_HEIGHT);
							}
						}
						else if (isCipher)
						{
							if (mXCoder == null)
							{
								mXCoder = new XCoder(getApplicationContext());
							}
							byte[] data = mXCoder.readCipherImage(path);
							if (data != null)
							{
								bmp = MyBitmapFactory.getBitmap(data,
										BMP_WIDTH, BMP_HEIGHT);
							}
						}
						else
						{
							bmp = MyBitmapFactory.getBitmap(path, BMP_WIDTH,
									BMP_HEIGHT);
						}

						if (bmp != null)
						{
							GlobalLruCache.getGLC().put(LRU_DIR + path, bmp);
							mHandler.sendEmptyMessage(0);
						}
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}

		}
	}

	@Override
	protected void afterDeXXX()
	{
		mPaths = GlobalData.Image.instance(this).getCopyPaths(isCipher);
		if (mPaths.size() == 0)
		{
			finish();
		}
		else
		{
			if (mCurPos > mPaths.size() - 1)		// 越界了
			{
				mCurPos = mPaths.size() - 1;
			}
			mCurPath = mPaths.get(mCurPos);
			mNumView.setText((mCurPos + 1) + "/" + mPaths.size());
			mAdapter.loadPic();
		}
	}

}
