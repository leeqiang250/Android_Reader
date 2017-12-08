package com.qlk.util.media;

import java.util.Collection;

public interface ISelection
{
	void setSelectListener(ISelectListener listener);

	void setSelectable(boolean selectable);

	void clearSelected();

	boolean isSelecting();

	/**
	 * 此方法不会改变selectable的值
	 * 
	 * @param position
	 */
	void setItemSelected(int position);

	Collection<String> getSelected();

	public interface ISelectListener
	{
		/**
		 * 选择数目变化
		 * 
		 * @param overflow
		 *            是否超出可用存储
		 * @param total
		 *            选择的总数目
		 * @param allSelected
		 *            是否被全部选中
		 */
		void onSelcetChanged(boolean overflow, int total, boolean allSelected);
	}
}
