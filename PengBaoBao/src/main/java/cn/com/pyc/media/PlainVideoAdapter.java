package cn.com.pyc.media;

import java.util.ArrayList;

import com.qlk.util.global.GlobalLruCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import cn.com.pyc.pbb.R;

public class PlainVideoAdapter extends MediaBaseAdapter
{

	public PlainVideoAdapter(Context context, ArrayList<String> paths)
	{
		super(context, paths);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHold vh;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.adapter_plain_video, parent, false);
			vh = new ViewHold();
			vh.pic = (ImageView) convertView.findViewById(R.id.apv_imv_video);
			vh.cbx = (CheckBox) convertView.findViewById(R.id.apv_cbx_check);
			vh.cbx.setOnCheckedChangeListener(onCheckedChangedListener);
			convertView.setTag(vh);
		}
		else
		{
			vh = (ViewHold) convertView.getTag();
		}

		String path = getItem(position);
		vh.pic.setImageBitmap(getBitmap(position));
		vh.cbx.setTag(path);
		vh.cbx.setChecked(mSelectPaths.contains(path));

		return convertView;
	}

	private class ViewHold
	{
		ImageView pic;
		CheckBox cbx;
	}

	@Override
	protected boolean isSupportThumbView()
	{
		return true;
	}

	@Override
	protected Runnable getThumbTask(final String path)
	{
		return new Runnable()
		{

			@Override
			public void run()
			{
				Bitmap bmp = GlobalLruCache.getGLC().get(path);
				if (bmp == null || bmp.isRecycled())
				{
					bmp = MyBitmapFactory.getVideoThumbnail(path, mThumbSize);
					if (bmp != null)
					{
						GlobalLruCache.getGLC().put(path, bmp);
					}
				}
			}
		};
	}

}
