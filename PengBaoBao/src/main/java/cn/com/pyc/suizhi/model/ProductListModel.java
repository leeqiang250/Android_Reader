package cn.com.pyc.suizhi.model;

import java.util.List;

public class ProductListModel extends com.sz.mobilesdk.models.BaseModel
{

	private ProductListInfo data;

	public void setData(ProductListInfo data)
	{
		this.data = data;
	}

	public ProductListInfo getData()
	{
		return data;
	}

	public static class ProductListInfo
	{
		private int currentPageNum;
		private int pageSize;
		private int totalNum;
		private int totalPageNum;
		private List<ProductInfo> items;

		public int getCurrentPageNum()
		{
			return currentPageNum;
		}

		public void setCurrentPageNum(int currentPageNum)
		{
			this.currentPageNum = currentPageNum;
		}

		public int getPageSize()
		{
			return pageSize;
		}

		public void setPageSize(int pageSize)
		{
			this.pageSize = pageSize;
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

		public List<ProductInfo> getItems()
		{
			return items;
		}

		public void setItems(List<ProductInfo> items)
		{
			this.items = items;
		}
	}

}
