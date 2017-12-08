package cn.com.pyc.receive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.pbb.reader.R;

@Deprecated
public class SeriesAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private final ArrayList<SmInfo> mInfos;

	public SeriesAdapter(Context context, ArrayList<SmInfo> infos)
	{
		mInflater = LayoutInflater.from(context);
		mInfos = infos;
	}

	@Override
	public int getCount()
	{
		return mInfos.size();
	}

	@Override
	public Object getItem(int position)
	{
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder vh;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.adapter_series, parent, false);
			vh = new ViewHolder();
			vh.name = (TextView) convertView.findViewById(R.id.as_txt_name);
			vh.describe = (TextView) convertView.findViewById(R.id.as_txt_describe);
			convertView.setTag(vh);
		}
		else
		{
			vh = (ViewHolder) convertView.getTag();
		}

		if (getSeriesId(position) > 0)
		{
			vh.name.setText(getSeriesName(position));
			vh.describe.setVisibility(View.VISIBLE);
			vh.describe.setText(getDescribe(position));
			int pic = mInfos.get(position).isPayFile() ? R.drawable.icon_hand_file
					: R.drawable.icon_free_file;
			vh.describe.setCompoundDrawablesWithIntrinsicBounds(pic, 0, 0, 0);
		}
		else
		{
			vh.describe.setVisibility(View.GONE);
			vh.name.setText(getSeriesName(position) + "("
					+ mInfos.get(position).getSeriesReceiveNum() + "个)");
		}

		return convertView;
	}

	private final StringBuilder mStringBuilder = new StringBuilder();

	private CharSequence getDescribe(int position)
	{
		SmInfo info = mInfos.get(position);
		StringBuilder sb = mStringBuilder;
		sb.setLength(0);
		sb.append("作者：");
		sb.append(info.getNick());
		sb.append("\t\t包含");
		sb.append(info.getSeriseFilesNum());
		sb.append("个文件，已接收");
		sb.append(info.getSeriesReceiveNum());
		sb.append("个");
		return sb.toString();
	}

	class ViewHolder
	{
		TextView name;
		TextView describe;
	}

	public int getSeriesId(int position)
	{
		return mInfos.get(position).getSid();
	}

	public String getSeriesName(int position)
	{
		return mInfos.get(position).getSeriesName();
	}

}
