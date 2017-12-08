package cn.com.pyc.pbbonline.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.widget.ProgressButton;

import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.util.TimeUtil;

/**
 * 离线数据adapter
 */
public class ShareDetailsOfflineAdapter extends BaseAdapter
{

	private Context context;
	private List<Album> albums;
	String OPENText;			//打开

	public ShareDetailsOfflineAdapter(Context context, List<Album> albums)
	{
		this.context = context;
		this.albums = albums;

		OPENText = context.getString(R.string.look);
	}

	public List<Album> getAlbums()
	{
		return albums;
	}

	@Override
	public int getCount()
	{
		return albums != null ? albums.size() : 0;
	}

	@Override
	public Album getItem(int position)
	{
		return albums.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = View
					.inflate(context, R.layout.pbbonline_item_share_details_offline, null);
			holder = new ViewHolder();
			holder.album_name = (TextView) convertView.findViewById(R.id.album_name_offline);
			holder.album_time = (TextView) convertView.findViewById(R.id.album_time_offline);
			holder.album_size = (TextView) convertView.findViewById(R.id.album_size_offline);
			holder.open_btn = (ProgressButton) convertView.findViewById(R.id.album_openbtn_offline);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final Album a = albums.get(position);
		holder.album_name.setText(a.getName());
		holder.album_time.setText(TimeUtil.getDateStringFromMills(a.getPublishDate()));
		holder.album_size.setText(context.getString(R.string.file_count, a.getItem_number()));
		holder.open_btn.setState(ProgressButton.NORMAL);
		holder.open_btn.setCurrentText(OPENText);
		holder.open_btn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				////OpenPageUtil.openMultipleMedia(context, a);
				OpenPageUtil.openFileListPage(context, a.getMyproduct_id(), a.getName());
			}
		});
		return convertView;
	}

	static class ViewHolder
	{
		TextView album_name;
		TextView album_time;
		TextView album_size;
		ProgressButton open_btn;
	}

}
