package cn.com.pyc.pbbonline.model;

import com.sz.mobilesdk.models.BaseModel;

/**
 * 分享文件夹详情
 */
public class FolderInfoModel extends BaseModel
{
	private DataBean data;
	private PageInfoBean pageInfo;

	public DataBean getData()
	{
		return data;
	}

	public void setData(DataBean data)
	{
		this.data = data;
	}

	public PageInfoBean getPageInfo()
	{
		return pageInfo;
	}

	public void setPageInfo(PageInfoBean pageInfo)
	{
		this.pageInfo = pageInfo;
	}

}
