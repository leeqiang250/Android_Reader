package com.qlk.util.global;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * 图片缓存类
 * 
 * @author QiLiKing 2015-6-30 下午3:59:19
 */
public class GlobalLruCache extends LruCache<String, Bitmap>
{
	private static GlobalLruCache glc;

	/**
	 * 单例-缓存大小为最大内存的1/8
	 * 
	 * @return
	 */
	public static GlobalLruCache getGLC()
	{
		if (glc == null)
		{
			glc = new GlobalLruCache((int) (Runtime.getRuntime().maxMemory() / 8));
		}
		return glc;
	}

	private GlobalLruCache(int maxSize)
	{
		super(maxSize);
	}

	@Override
	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue)
	{
		super.entryRemoved(evicted, key, oldValue, newValue);
	}

	@Override
	protected int sizeOf(String key, Bitmap value)
	{
		return value.getByteCount();		//这句才是缓存的精髓
	}

}
