package cn.com.pyc.pbbonline.bean;

import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 可视化文件信息（包括文件信息和权限）
 * 
 * @author hudq
 */
public class SZFile implements Parcelable
{
	//private String album_id;
	//private String album_pic;
	private String name;					//文件名称
	private String filePath;				//文件本地路径
	private String cek_cipher_value;		//文件秘钥

	private String myProId;					//专辑文件夹id
	private String contentId;				//文件id
	private String assetId;					//文件约束id
	private String format; 					//格式-可根据filePath获得,或设置

	private boolean checkOpen;				//文件是否有打开权限
	private String odd_datetime_end;		//权限截止日期
	private String validity_time;			//权限有效时间

	public SZFile(String name, String path, String privateKey)
	{
		this.name = name;
		this.filePath = path;
		this.cek_cipher_value = privateKey;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		//dest.writeString(this.album_id);
		//dest.writeString(this.album_pic);
		dest.writeString(this.name);
		dest.writeString(this.filePath);
		dest.writeString(this.cek_cipher_value);

		dest.writeString(this.myProId);
		dest.writeString(this.contentId);
		dest.writeString(this.assetId);
		dest.writeString(this.format);

		dest.writeInt(this.checkOpen ? 1 : 0);
		dest.writeString(this.odd_datetime_end);
		dest.writeString(this.validity_time);

	}

	public final static Parcelable.Creator<SZFile> CREATOR = new Creator<SZFile>()
	{

		@Override
		public SZFile[] newArray(int size)
		{
			return new SZFile[size];
		}

		@Override
		public SZFile createFromParcel(Parcel source)
		{
			String name = source.readString();
			String path = source.readString();
			String key = source.readString();

			SZFile file = new SZFile(name, path, key);

			//file.setAlbum_id(source.readString());
			//file.setAlbum_pic(source.readString());

			file.setMyProId(source.readString());
			file.setContentId(source.readString());
			file.setAssetId(source.readString());
			file.setFormat(source.readString());

			file.setCheckOpen(source.readInt() == 1 ? true : false);
			file.setOdd_datetime_end(source.readString());
			file.setValidity_time(source.readString());

			return file;
		}
	};

	//	public String getAlbum_id()
	//	{
	//		return album_id;
	//	}
	//
	//	public void setAlbum_id(String album_id)
	//	{
	//		this.album_id = album_id;
	//	}

	//	public void setAlbum_pic(String album_pic)
	//	{
	//		this.album_pic = album_pic;
	//	}
	//
	//	public String getAlbum_pic()
	//	{
	//		return album_pic;
	//	}

	public String getMyProId()
	{
		return myProId;
	}

	public void setMyProId(String myProId)
	{
		this.myProId = myProId;
	}

	public String getContentId()
	{
		return contentId;
	}

	public void setContentId(String contentId)
	{
		this.contentId = contentId;
	}

	public String getName()
	{
		return name;
	}

	//	public void setName(String name)
	//	{
	//		this.name = name;
	//	}

	public String getAssetId()
	{
		return assetId;
	}

	public void setAssetId(String assetId)
	{
		this.assetId = assetId;
	}

	public String getFilePath()
	{
		return filePath;
	}

	//	public void setFilePath(String filePath)
	//	{
	//		this.filePath = filePath;
	//	}

	public boolean isCheckOpen()
	{
		return checkOpen;
	}

	public void setCheckOpen(boolean checkOpen)
	{
		this.checkOpen = checkOpen;
	}

	public String getCek_cipher_value()
	{
		return cek_cipher_value;
	}

	//	public void setCek_cipher_value(String cek_cipher_value)
	//	{
	//		this.cek_cipher_value = cek_cipher_value;
	//	}

	public String getOdd_datetime_end()
	{
		return odd_datetime_end;
	}

	public void setOdd_datetime_end(String odd_datetime_end)
	{
		this.odd_datetime_end = odd_datetime_end;
	}

	public String getValidity_time()
	{
		return validity_time;
	}

	public void setValidity_time(String validity_time)
	{
		this.validity_time = validity_time;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

	//eg: sdcard/cn.com.pyc.pbb/asdadasdsads-sdas12afdsf4-dsd12dfsf.mp4
	public String getFormat()
	{
		return filePath.substring(filePath.lastIndexOf(".") + 1).toUpperCase(Locale.getDefault());
	}

	@Override
	public String toString()
	{
		return "SZFile [myProId=" + myProId + ", contentId=" + contentId + ", name=" + name
				+ ", filePath=" + filePath + ", assetId=" + assetId + ", format=" + format
				+ ", checkOpen=" + checkOpen + ", cek_cipher_value=" + cek_cipher_value
				+ ", odd_datetime_end=" + odd_datetime_end + ", validity_time=" + validity_time
				+ "]";
	}

}
