package cn.com.pyc.pbbonline.model;

import java.util.List;

import com.sz.mobilesdk.models.FolderInfo;

import cn.com.pyc.pbbonline.util.SortNameUtil;

/**
 * 文件夹信息
 */
public class PageInfoBean
{

	private int pageSize;
	private int currentPageNum;
	private int totalNum;
	private int totalPageNum;
	private List<FolderInfo> items;

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	public int getCurrentPageNum()
	{
		return currentPageNum;
	}

	public void setCurrentPageNum(int currentPageNum)
	{
		this.currentPageNum = currentPageNum;
	}

	public int getTotalNum()
	{
		return totalNum;
	}

	public void setTotalNum(int totalNum)
	{
		this.totalNum = totalNum;
	}

	public int getTotalPageNum()
	{
		return totalPageNum;
	}

	public void setTotalPageNum(int totalPageNum)
	{
		this.totalPageNum = totalPageNum;
	}

	public List<FolderInfo> getItems()
	{
		return items;
	}

	public void setItems(List<FolderInfo> items)
	{
		this.items = SortNameUtil.sortFolderInfo(items);
	}

}
