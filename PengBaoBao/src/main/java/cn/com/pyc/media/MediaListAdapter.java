package cn.com.pyc.media;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool._SysoXXX;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.pyc.pbb.R;

public class MediaListAdapter extends MediaBaseAdapter
{
	private static final HashMap<String, Integer> PICS = new HashMap<>();

	static
	{
		PICS.put(".pdf", R.drawable.pdf);
		PICS.put(".3gp", R.drawable.video_3gp);
		PICS.put(".mp4", R.drawable.video_mp4);
		PICS.put(".mp3", R.drawable.music);
		PICS.put(".wav", R.drawable.music);
	}

	public MediaListAdapter(Context context, ArrayList<String> paths)
	{
		super(context, paths);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHold vh;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.adapter_media_list, parent, false);
			vh = new ViewHold();
			vh.pic = (ImageView) convertView.findViewById(R.id.aml_imv_pic);
			vh.name = (TextView) convertView.findViewById(R.id.aml_txt_name);
			vh.time = (TextView) convertView.findViewById(R.id.aml_txt_time);
			vh.size = (TextView) convertView.findViewById(R.id.aml_txt_size);
			vh.cbx = (CheckBox) convertView.findViewById(R.id.aml_cbx_check);
			vh.cbx.setOnCheckedChangeListener(onCheckedChangedListener);
			convertView.setTag(vh);
		}
		else
		{
			vh = (ViewHold) convertView.getTag();
		}

		String path = getItem(position);
		File file = new File(path);
		vh.name.setText(file.getName());
		vh.time.setText(DataConvert.toDate(file.lastModified()));
		vh.size.setText(DataConvert.toSize(mContext, file.length()));
		String suffix = path.substring(path.lastIndexOf(".")).toLowerCase(Locale.US);
		Integer pic = PICS.get(suffix);
		vh.pic.setBackgroundResource(pic == null ? R.drawable.video_3gp : pic);
		_SysoXXX.message("isSelectable:"+isSelectable);
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
		TextView name;
		TextView time;
		TextView size;
		CheckBox cbx;
	}

	@Override
	protected boolean isSupportThumbView()
	{
		return false;
	}

}
