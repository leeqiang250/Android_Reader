package cn.com.pyc.pbbonline.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.db.Shared;
import cn.com.pyc.pbbonline.widget.SmoothCheckBox;

import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.TimeUtil;

/**
 * 分享列表（首页面记录）
 */
public class ShareRecordAdapter extends BaseAdapter
{
	private Context ctx;
	private List<Shared> records;
	private SimpleDateFormat sdf;
	private LayoutInflater inflater;
	private boolean show_checkBox = false;
	private SparseBooleanArray mSelectState;				//存儲選中的狀態
	public static SparseArray<Shared> mSelectObjs; 			//存储删除的元素。

	private int selectColor;
	private int unSelectColor;
	private Drawable mDrawable;
	private Drawable mDisDrawable;

	public void release()
	{
		if (mSelectObjs != null)
		{
			mSelectObjs.clear();
			mSelectObjs = null;
		}
		mDrawable = null;
		mDisDrawable = null;
	}

	public ShareRecordAdapter(Context ctx, List<Shared> records)
	{
		super();
		this.ctx = ctx;
		this.records = records;

		if (mSelectObjs == null)
			mSelectObjs = new SparseArray<Shared>();
		mSelectObjs.clear();
		initCheckBoxState(this.records);
		sdf = new SimpleDateFormat(TimeUtil.DATE_FORMAT_STRING);
		inflater = LayoutInflater.from(ctx);
		this.selectColor = Color.parseColor("#212121");
		this.unSelectColor = Color.parseColor("#85000000");
		this.mDrawable = this.ctx.getResources().getDrawable(R.drawable.ic_sa_msg);
		this.mDisDrawable = this.ctx.getResources().getDrawable(R.drawable.ic_so_msg);
	}

	//初始化checkbox状态，默认false
	private void initCheckBoxState(List<Shared> records)
	{
		mSelectState = new SparseBooleanArray();
		mSelectState.clear();
		int size_ = records.size();
		for (int i = 0; i < size_; i++)
		{
			int key = records.get(i).getId();
			mSelectState.put(key, false);
		}
	}

	public void setShow_checkBox(boolean show_checkBox)
	{
		this.show_checkBox = show_checkBox;
	}

	public void setRecords(List<Shared> records)
	{
		this.records = records;
	}

	public List<Shared> getRecords()
	{
		return records;
	}

	@Override
	public int getCount()
	{
		return records != null ? records.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		return records.get(position);
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
			convertView = inflater.inflate(R.layout.pbbonline_item_share_records, null);
			holder = new ViewHolder();
			holder.tv_shareName = (TextView) convertView.findViewById(R.id.record_sharename);
			holder.tv_userName = (TextView) convertView.findViewById(R.id.record_username);
			holder.tv_time = (TextView) convertView.findViewById(R.id.record_time);
			holder.tv_state = (TextView) convertView.findViewById(R.id.record_state);
			holder.iv_dot = (ImageView) convertView.findViewById(R.id.record_iv_dot);
			holder.iv_record = (ImageView) convertView.findViewById(R.id.record_iv);
			holder.cb_select = (SmoothCheckBox) convertView.findViewById(R.id.record_checkbox);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		Shared s = records.get(position);
		holder.tv_userName.setText(s.getOwner());
		holder.tv_shareName.setText(s.getTheme());
		if (s.getTime() != 0)
		{
			String dateStr = sdf.format(new Date(s.getTime()));
			Date dstDate = TimeUtil.getDateFromDateString(dateStr, TimeUtil.DATE_FORMAT_STRING);
			holder.tv_time.setText(TimeUtil.getTimeString(ctx, dstDate));
		}
		else
		{
			holder.tv_time.setText(null);
		}
		setCheckBox(holder.cb_select, s);
		// 显示更新，如果被回收不显示
		setRedDot(holder.iv_dot, s.isWhetherNew() || s.isUpdate(), s.isRevoke());
		// 显示回收
		setTextState(holder, s.isRevoke());
		return convertView;
	}

	private void setTextState(ViewHolder holder, boolean isRevoke)
	{
		if (isRevoke)
		{
			//被收回
			holder.tv_state.setVisibility(View.VISIBLE);
			holder.tv_shareName.setTextColor(unSelectColor);
			holder.iv_record.setImageDrawable(mDisDrawable);
		}
		else
		{
			holder.tv_state.setVisibility(View.GONE);
			holder.tv_shareName.setTextColor(selectColor);
			holder.iv_record.setImageDrawable(mDrawable);
		}
	}

	private void setRedDot(ImageView dot, boolean isNew, boolean isRevoke)
	{
		if (isRevoke)
			isNew = false;
		dot.setVisibility(isNew ? View.VISIBLE : View.GONE);
	}

	private void setCheckBox(final SmoothCheckBox cb, final Shared s)
	{
		cb.setVisibility(show_checkBox ? View.VISIBLE : View.GONE);
		final int key = s.getId();
		cb.setChecked(mSelectState.get(key));

		cb.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (cb.isChecked())
				{
					mSelectState.delete(key);
					mSelectObjs.delete(key);
				}
				else
				{
					mSelectState.put(key, true);
					mSelectObjs.put(key, s);
					SZLog.i("select:" + s.getShareId() + "; " + s.getTheme());
				}
				cb.setChecked(!cb.isChecked(), true);
			}
		});
	}

	/**
	 * 全选或非全选
	 * 
	 * @param records
	 * @param selected
	 *            是否选中
	 */
	public void selectAll(List<Shared> records, boolean selected)
	{
		for (Shared shared : records)
		{
			int key = shared.getId();
			if (selected)
			{
				mSelectState.put(key, true);
				mSelectObjs.put(key, shared);
			}
			else
			{
				mSelectState.delete(key);
				mSelectObjs.clear();
			}
		}
		notifyDataSetChanged();
	}

	private static class ViewHolder
	{
		TextView tv_shareName;
		TextView tv_userName;
		TextView tv_time;
		ImageView iv_dot;
		ImageView iv_record;
		TextView tv_state;
		SmoothCheckBox cb_select;

	}

}
