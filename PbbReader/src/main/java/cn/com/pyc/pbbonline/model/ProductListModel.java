package cn.com.pyc.pbbonline.model;

import com.sz.mobilesdk.models.BaseModel;

/**
 * 产品列表信息，（弃用）
 */
public class ProductListModel extends BaseModel
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

}
