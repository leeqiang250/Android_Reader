package cn.com.pyc.pbbonline.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ValidDateUtil;

import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.ContentRight;
import com.sz.mobilesdk.util.MediaUtils;

/**
 * 专辑下文件列表
 * <p>
 * 已废弃
 */
public class AlbumList_Adapter extends BaseAdapter
{
	private Context context;
	private List<AlbumContent> mlist;
	private int curPosition = -1;
	private String contentId;

	private int selectColorId;
	private int unSelectNameColorId;
	private int unSelectTimeColorId;
	private int selectDrawableId;
	private int unSelectDrawableId;

	public void setCurrentPosition(int pos)
	{
		curPosition = pos;
	}

	public void setContentId(String contentId)
	{
		this.contentId = contentId;
	}

	public void setAlbumContentList(List<AlbumContent> mlist)
	{
		this.mlist = mlist;
	}

	public List<AlbumContent> getAlbumContentList()
	{
		return mlist;
	}

	public AlbumList_Adapter(Context context, List<AlbumContent> mlist)
	{
		this.context = context;
		this.mlist = mlist;

		this.selectColorId = context.getResources().getColor(R.color.blue_bar_color);
		this.unSelectNameColorId = context.getResources().getColor(R.color.black_bb);
		this.unSelectTimeColorId = context.getResources().getColor(R.color.gray);
		this.selectDrawableId = R.drawable.ic_validate_time_select;
		this.unSelectDrawableId = R.drawable.ic_validate_time_nor;
	}

	@Override
	public int getCount()
	{
		return mlist != null ? mlist.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		return mlist.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.pbbonline_item_menu_list, null);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.name_title);
			holder.txtTime = (TextView) convertView.findViewById(R.id.val_time);
			holder.txtType = (TextView) convertView.findViewById(R.id.val_type);
			holder.valIv = (ImageView) convertView.findViewById(R.id.val_iv);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		AlbumContent ac = mlist.get(position);
		if (position == curPosition || TextUtils.equals(contentId, ac.getContent_id()))
		{
			holder.txtTitle.setTextColor(selectColorId);
			holder.txtTime.setTextColor(selectColorId);
			holder.valIv.setImageResource(selectDrawableId);
		}
		else
		{
			holder.txtTitle.setTextColor(unSelectNameColorId);
			holder.txtTime.setTextColor(unSelectTimeColorId);
			holder.valIv.setImageResource(unSelectDrawableId);
		}
		holder.txtTitle.setText(ac.getName());
		List<ContentRight> rights = MediaUtils.getInstance().getMediaRight();
		if (rights != null && rights.size() > 0)
		{
			long availableTime = rights.get(position).availableTime;
			holder.txtTime.setText(ValidDateUtil.getValidTime(context, availableTime,
					rights.get(position).odd_datetime_end));
		}
		switch (ac.getFileType())
		{
			case Fields.PDF:
				holder.txtType.setText(Fields.PDF);
				break;
			case Fields.MP3:
				holder.txtType.setText("音乐");
				break;
			case Fields.MP4:
				holder.txtType.setText("视频");
				break;
			default:
				holder.txtType.setText(null);
				break;
		}
		return convertView;

	}

	private static class ViewHolder
	{
		TextView txtTitle;
		TextView txtTime;
		ImageView valIv;
		TextView txtType;
	}

	//	private void setValidateTime(ContentRight right, TextView txtTime)
	//	{
	//		long availableTime = Double.valueOf(right.availableTime).longValue();
	//		String available = FormatTimeutil.getLeftAvailableTime(context, availableTime);
	//		if ("00天00小时".equals(available))
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
	//			//剩余权限时间，有效期时间
	//			String odd_datetime_end = FormatTimeutil.getToOddEndTime(right.odd_datetime_end);
	//			txtTime.setText(context.getString(com.sz.mobilesdk.R.string.deadline_time,
	//					odd_datetime_end));
	//		}
	//	}

}
