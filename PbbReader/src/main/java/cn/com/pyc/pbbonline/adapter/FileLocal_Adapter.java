package cn.com.pyc.pbbonline.adapter;

import java.util.List;

import com.qlk.util.tool.Util.ViewUtil;
import com.sz.mobilesdk.authentication.SZContent;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.view.widget.RoundProgressBar;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ValidDateUtil;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileLocal_Adapter extends BaseAdapter
{
	private PbbBaseActivity mContext;
	private List<AlbumContent> list;
	private String curFileId;

	private int selectColorId;
	private int unSelectNameColorId;
	private int unSelectTimeColorId;
	private Drawable selectDrawable;
	private Drawable unSelectDrawable;

	public void setCurFileId(String curFileId)
	{
		this.curFileId = curFileId;
	}

	public FileLocal_Adapter(PbbBaseActivity mContext, List<AlbumContent> list)
	{
		this.mContext = mContext;
		this.list = list;

		Resources resources = this.mContext.getResources();
		this.selectColorId = resources.getColor(R.color.blue_bar_color);
		this.unSelectNameColorId = resources.getColor(R.color.black_bb);
		this.unSelectTimeColorId = resources.getColor(R.color.gray);
		this.selectDrawable = resources.getDrawable(R.drawable.ic_validate_time_select);
		this.unSelectDrawable = resources.getDrawable(R.drawable.ic_validate_time_nor);

	}

	public List<AlbumContent> getList()
	{
		return list;
	}

	@Override
	public int getCount()
	{
		return list != null ? list.size() : 0;
	}

	@Override
	public AlbumContent getItem(int position)
	{
		return list.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.pbbonline_item_files_list, null);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.tv_file_name);
			holder.txtLable = (TextView) convertView.findViewById(R.id.tv_file_status_lable);
			holder.txtStatus = (TextView) convertView.findViewById(R.id.tv_file_status);
			holder.bar = (RoundProgressBar) convertView.findViewById(R.id.rpb_file);
			holder.txtSize = (TextView) convertView.findViewById(R.id.tv_file_size);
			holder.arrow = (ImageView) convertView.findViewById(R.id.iv_arrow_right);
			holder.ivTime = (ImageView) convertView.findViewById(R.id.iv_file_status);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		AlbumContent ac = list.get(position);
		ViewUtil.gone(holder.bar);
		ViewUtil.gone(holder.txtSize);
		ViewUtil.gone(holder.txtLable);
		ViewUtil.visible(holder.arrow);
		holder.txtTitle.setText(ac.getName());
		holder.txtStatus.setText(null);
		SZContent szcont = new SZContent(ac.getAsset_id());
		holder.txtStatus.setText(ValidDateUtil.getValidTime(mContext, szcont.getAvailbaleTime(),
				szcont.getOdd_datetime_end()));

		setPressSelector(ac.getContent_id(), holder);
		return convertView;
	}

	private void setPressSelector(String id, ViewHolder holder)
	{
		if (TextUtils.equals(id, curFileId))
		{
			holder.txtTitle.setTextColor(selectColorId);
			holder.txtStatus.setTextColor(selectColorId);
			holder.txtLable.setTextColor(selectColorId);
			holder.ivTime.setImageDrawable(selectDrawable);
		}
		else
		{
			holder.txtTitle.setTextColor(unSelectNameColorId);
			holder.txtStatus.setTextColor(unSelectTimeColorId);
			holder.txtLable.setTextColor(unSelectTimeColorId);
			holder.ivTime.setImageDrawable(unSelectDrawable);
		}
	}

	private static class ViewHolder
	{
		TextView txtTitle;
		TextView txtLable;
		TextView txtStatus;
		RoundProgressBar bar;
		TextView txtSize;
		ImageView arrow;
		ImageView ivTime;
	}

}
