package cn.com.pyc.suizhi.model;

import com.sz.mobilesdk.models.BaseModel;

import java.util.ArrayList;
import java.util.List;

public class FilesDataModel extends BaseModel
{

	private List<FileData> data;

	public void setData(List<FileData> data)
	{
		this.data = data;
	}

	public List<FileData> getData()
	{
		if (data == null) data = new ArrayList<>();
		return data;
	}

}
