package cn.com.pyc.pbbonline.model;

import java.util.ArrayList;
import java.util.List;

import com.sz.mobilesdk.models.BaseModel;
import com.sz.mobilesdk.models.FileData;

/**
 * 文件列表信息
 */
public class FilesDataModel extends BaseModel
{
	private List<FileData> data;

	public void setData(List<FileData> data)
	{
		this.data = data;
	}

	public List<FileData> getData()
	{
		if (data == null)
			data = new ArrayList<>();
		return data;
	}

}
