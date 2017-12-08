package cn.com.pyc.words;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.pyc.pbb.reader.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SlideAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private Context context;
//	private List<MessageItem> mMessageItems;
	private List<Map<String, Object>> listItems;
	private boolean isLongState;
	private HashMap<Integer, Boolean> checkedItemMap = new HashMap<Integer, Boolean>();
	private Drawable longStateItemCheckedBg, longStateItemNormalBg;

	public SlideAdapter(Context context, List<Map<String, Object>> listItems,
			LayoutInflater mInflater) {
		super();
		this.context = context;
		this.mInflater = mInflater;
		this.listItems = listItems;

//		longStateItemCheckedBg = context.getResources().getDrawable(
//				R.drawable.xlistview_check_bg);
//		longStateItemNormalBg = context.getResources().getDrawable(
//				R.drawable.xlistview_bg);
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView,
			ViewGroup parent) {
		TextView tv_title;
		TextView tv_time;
		ViewHolder holder;
		SlidingDeleteSlideView slideView = (SlidingDeleteSlideView) convertView;

		
			View itemView = mInflater.inflate(R.layout.adapter_mywords, null);
			tv_title = (TextView) itemView.findViewById(R.id.tv_mywords_title);
			tv_time = (TextView) itemView.findViewById(R.id.tv_mywords_time);
			
//			tv_title.setText(listItems.get(position).get("Title").title)
			slideView.setContentView(itemView);

	
		return slideView;
	}

	private static class ViewHolder {
		
	}

	public interface OnDeleteListener {
		public void onDelete(View view, int position);
	}

	public void setIsLongState(boolean isLongState) {
		this.isLongState = isLongState;
	}

	public void setCheckItemMap(HashMap<Integer, Boolean> checkedItemMap) {
		this.checkedItemMap = checkedItemMap;
	}

}
