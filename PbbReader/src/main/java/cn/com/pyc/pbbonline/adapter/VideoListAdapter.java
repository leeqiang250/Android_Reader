package cn.com.pyc.pbbonline.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.util.ValidDateUtil;

public class VideoListAdapter extends BaseAdapter
{
	private Context context;
	private List<SZFile> drmFiles;
	private int curPos;

	private int selectColorId;
	private int unSelectColorId;
	private int selectDrawableId;
	private int unSelectDrawableId;

	public VideoListAdapter(Context context, List<SZFile> drmFiles)
	{
		this.context = context;
		this.drmFiles = drmFiles;

		this.selectColorId = context.getResources().getColor(R.color.blue_bar_color);
		this.unSelectColorId = context.getResources().getColor(R.color.content_text_color_white);
		this.selectDrawableId = R.drawable.ic_validate_time_select;
		this.unSelectDrawableId = R.drawable.ic_validate_time_nor;
	}

	@Override
	public int getCount()
	{
		return drmFiles != null ? drmFiles.size() : 0;
	}

	@Override
	public SZFile getItem(int position)
	{

		return drmFiles.get(position);
	}

	@Override
	public long getItemId(int position)
	{

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.pbbonline_item_video_list, null);
			holder.videoName = (TextView) convertView.findViewById(R.id.alt_txt_title);
			holder.videoTime = (TextView) convertView.findViewById(R.id.alt_txt_validity);
			holder.videoIcon = (ImageView) convertView.findViewById(R.id.alt_img_validate);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		SZFile df = drmFiles.get(position);
		holder.videoName.setText(df.getName());
		holder.videoTime.setText(ValidDateUtil.getValidTime(context, df.getValidity_time(),
				df.getOdd_datetime_end()));
		
		if (position == curPos)
		{
			holder.videoName.setTextColor(selectColorId);
			holder.videoTime.setTextColor(selectColorId);
			holder.videoIcon.setImageResource(selectDrawableId);
		}
		else
		{
			holder.videoName.setTextColor(unSelectColorId);
			holder.videoTime.setTextColor(unSelectColorId);
			holder.videoIcon.setImageResource(unSelectDrawableId);
		}
		return convertView;
	}

	/**
	 * 当前位置
	 * 
	 * @param pos
	 */
	public void setCurPosition(int position)
	{
		curPos = position;
		notifyDataSetChanged();
	}

	static class ViewHolder
	{
		TextView videoName;
		TextView videoTime;
		ImageView videoIcon;
	}

	//	private void setValidate(DrmFile drmFile, TextView txtTime)
	//	{
	//		String available = drmFile.getValidity();
	//		if ("00天00小时".equalsIgnoreCase(available))
	//		{
	//			// 已经过期
	//			txtTime.setText(context.getString(com.sz.mobilesdk.R.string.over_time));
	//		}
	//		else if (context.getString(com.sz.mobilesdk.R.string.forever_time).equals(available))
	//		{
	//			// 永久
	//			txtTime.setText(available);
	//		}
	//		else
	//		{
	//			//剩余权限时间
	//			txtTime.setText(context.getString(com.sz.mobilesdk.R.string.deadline_time,
	//					drmFile.getOdd_datetime_end()));
	//		}
	//	}

}
