package com.qlk.util.tool;

import android.util.SparseArray;
import android.view.View;

public class ViewHolder
{
	/**
	 * 简化的ViewHolder获取view控件的方法。用在adapter中getView方法中初始化控件
	 * 
	 * @param view
	 *            convertView
	 * @param vId
	 *            view定义的id
	 * @return 控件view
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends View> T get(View view, int vId)
	{
		SparseArray<View> holder = (SparseArray<View>) view.getTag();
		if (null == holder)
		{
			holder = new SparseArray<View>();
			view.setTag(holder);
		}
		View itemView = holder.get(vId);
		if (null == itemView)
		{
			itemView = view.findViewById(vId);
			holder.put(vId, itemView);
		}
		return (T) itemView;
	}

}
