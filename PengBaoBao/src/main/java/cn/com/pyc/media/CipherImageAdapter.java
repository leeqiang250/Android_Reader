package cn.com.pyc.media;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import cn.com.pyc.pbb.R;
import cn.com.pyc.xcoder.XCoder;

import com.qlk.util.global.GlobalLruCache;

class CipherImageAdapter extends MediaBaseAdapter
{
	private final XCoder mXCoder;

	public CipherImageAdapter(Context context, ArrayList<String> paths)
	{
		super(context, paths);
		mXCoder = new XCoder(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHold vh;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.adapter_cipher_image, parent, false);
			vh = new ViewHold();
			vh.pic = (ImageView) convertView.findViewById(R.id.aci_imv_image);
			vh.cbx = (CheckBox) convertView.findViewById(R.id.aci_cbx_check);
			vh.cbx.setOnCheckedChangeListener(onCheckedChangedListener);
			convertView.setTag(vh);
		}
		else
		{
			vh = (ViewHold) convertView.getTag();
		}

		String path = getItem(position);
		vh.pic.setImageBitmap(getBitmap(position));
		if (isSelectable)
		{
			vh.cbx.setVisibility(View.VISIBLE);
			vh.cbx.setTag(path);
			vh.cbx.setChecked(mSelectPaths.contains(path));
		}
		else
		{
			vh.cbx.setVisibility(View.GONE);
		}

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
					byte[] data = mXCoder.readCipherImage(path);
					if (data != null)
					{
						bmp = MyBitmapFactory.getImageThumbnail(data, mThumbSize);
						if (bmp != null)
						{
							GlobalLruCache.getGLC().put(path, bmp);
						}
					}
				}
			}
		};
	}

}
