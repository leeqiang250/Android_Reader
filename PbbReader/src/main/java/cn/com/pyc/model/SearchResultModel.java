package cn.com.pyc.model;

import com.sz.mobilesdk.models.BaseModel;

import java.util.ArrayList;
import java.util.List;


public class SearchResultModel extends BaseModel
{

	private SearchInfo data;

	public void setData(SearchInfo data)
	{
		this.data = data;
	}

	public SearchInfo getData()
	{
		return data;
	}

	public static class SearchInfo
	{
		private MyProduct myProducts;
		private SearchProduct searchProducts;
		private RecommendProduct recommendProducts;
		private String accessLogId;

		public MyProduct getMyProducts()
		{
			return myProducts;
		}

		public void setMyProducts(MyProduct myProducts)
		{
			this.myProducts = myProducts;
		}

		public SearchProduct getSearchProducts()
		{
			return searchProducts;
		}

		public void setSearchProducts(SearchProduct searchProducts)
		{
			this.searchProducts = searchProducts;
		}

		public RecommendProduct getRecommendProducts()
		{
			return recommendProducts;
		}

		public void setRecommendProducts(RecommendProduct recommendProducts)
		{
			this.recommendProducts = recommendProducts;
		}

		public void setAccessLogId(String accessLogId) {
			this.accessLogId = accessLogId;
		}

		public String getAccessLogId() {
			return accessLogId;
		}
	}

	/**
	 * 推荐结果
	 * 
	 * @author hudq
	 * 
	 */
	public static class RecommendProduct
	{
		private int totalPageNum;
		private List<SearchResult> items;

		public int getTotalPageNum()
		{
			return totalPageNum;
		}

		public void setTotalPageNum(int totalPageNum)
		{
			this.totalPageNum = totalPageNum;
		}

		public List<SearchResult> getItems()
		{
			if (items == null) return new ArrayList<>();
			return setMarkSource(items, 64);
		}

		public void setItems(List<SearchResult> items)
		{
			this.items = items;
		}
	}

	/**
	 * 搜索结果
	 * 
	 * @author hudq
	 * 
	 */
	public static class SearchProduct
	{
		private int totalPageNum;
		private List<SearchResult> items;

		public int getTotalPageNum()
		{
			return totalPageNum;
		}

		public void setTotalPageNum(int totalPageNum)
		{
			this.totalPageNum = totalPageNum;
		}

		public List<SearchResult> getItems()
		{
			if (items == null) return new ArrayList<>();
			return setMarkSource(items, 32);
		}

		public void setItems(List<SearchResult> items)
		{
			this.items = items;
		}
	}

	/**
	 * 我的购买产品
	 * 
	 * @author hudq
	 * 
	 */
	public static class MyProduct
	{
		// private int pageSize;
		// private int finishNum;
		// private int startNum;
		private int totalPageNum;
		private List<SearchResult> items;

		public int getTotalPageNum()
		{
			return totalPageNum;
		}

		public void setTotalPageNum(int totalPageNum)
		{
			this.totalPageNum = totalPageNum;
		}

		public List<SearchResult> getItems()
		{
			if (items == null) return new ArrayList<>();
			return setMarkSource(items, -1);
		}

		public void setItems(List<SearchResult> items)
		{
			this.items = items;
		}
	}

	/**
	 * 搜索结果
	 * 
	 * @author hudq
	 * 
	 */
	public static class SearchResult
	{
		private String authors;
		private String picture_url;
		private String picture_ratio;
		private String preview;
		private String myProId;
		private String proId;
		private String productName;
		private String category;
		private String sellerName;
		private int source; // -1 购买的； 32 搜索结果；64 推荐

		public void setSource(int source)
		{
			this.source = source;
		}

		public int getSource()
		{
			return source;
		}

		public String getAuthors()
		{
			return authors;
		}

		public void setAuthors(String authors)
		{
			this.authors = authors;
		}

		public String getPicture_url()
		{
			return picture_url;
		}

		public void setPicture_url(String picture_url)
		{
			this.picture_url = picture_url;
		}

		public String getPicture_ratio()
		{
			return picture_ratio;
		}

		public void setPicture_ratio(String picture_ratio)
		{
			this.picture_ratio = picture_ratio;
		}

		public String getPreview()
		{
			return preview;
		}

		public void setPreview(String preview)
		{
			this.preview = preview;
		}

		public String getMyProId()
		{
			return myProId;
		}

		public void setMyProId(String myProId)
		{
			this.myProId = myProId;
		}

		public String getProId()
		{
			return proId;
		}

		public void setProId(String proId)
		{
			this.proId = proId;
		}

		public String getProductName()
		{
			return productName;
		}

		public void setProductName(String productName)
		{
			this.productName = productName;
		}

		public void setCategory(String category)
		{
			this.category = category;
		}

		public String getCategory()
		{
			return category;
		}

		public void setSellerName(String sellerName) {
			this.sellerName = sellerName;
		}

		public String getSellerName() {
			return sellerName;
		}
	}

	/**
	 * 设置对应的搜索来源
	 * 
	 * @param targetItems
	 * @param source
	 * 
	 *  DrmPat.BUYED
	 *  DrmPat.SEARCHED
	 *  DrmPat.RECOMMONED
	 * @return
	 */
	private static List<SearchResult> setMarkSource(
			List<SearchResult> targetItems, int source)
	{
		List<SearchResult> results = new ArrayList<SearchResult>();
		for (SearchResult s : targetItems)
		{
			s.setSource(source);
			results.add(s);
		}
		targetItems.clear();
		targetItems = null;
		return results;
	}

}
