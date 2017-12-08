//package cn.com.pyc.pbbonline.bean;
//
//import java.util.Locale;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
///**
// * *************Parcelable序列化操作,定义变量的顺序不可更改！
// * 文件信息
// * 
// * @author hudq
// */
//public class DrmFile implements Parcelable
//{
//
//	private String filePath;
//	private String publicKey;
//	private String privateKey;
//
//	private String format; // 格式-可根据filePath获得
//	private String title;
//	private String asset_id;
//	private String odd_datetime_end;
//	private String validity; // 有效期信息
//
//	private String myProId;
//	private String contentId;
//	private boolean checkOpen;
//
//	// 明文
//	public DrmFile(String filePath)
//	{
//		this.filePath = filePath;
//	}
//
//	// 密文
//	public DrmFile(String filePath, String publicKey, String privateKey)
//	{
//		this.filePath = filePath;
//		this.publicKey = publicKey;
//		this.privateKey = privateKey;
//	}
//
//	// public boolean isCipherFile()
//	// {
//	// return canDecrypt();
//	// }
//
//	// public boolean canEncrypt()
//	// {
//	// return !TextUtils.isEmpty(publicKey) && !isCipherFile(); //
//	// 公钥是针对用户的；不能对密文解密
//	// }
//
//	// public boolean canDecrypt()
//	// {
//	// return !TextUtils.isEmpty(privateKey); // 私钥是与文件一一对应的
//	// }
//
//	/*-************************
//	 * Getter and Setter
//	 *************************/
//
//	public String getFilePath()
//	{
//		return filePath;
//	}
//
//	public void setFilePath(String filePath)
//	{
//		this.filePath = filePath;
//	}
//
//	public String getPublicKey()
//	{
//		return publicKey;
//	}
//
//	public void setPublicKey(String publicKey)
//	{
//		this.publicKey = publicKey;
//	}
//
//	public String getPrivateKey()
//	{
//		return privateKey;
//	}
//
//	public void setPrivateKey(String privateKey)
//	{
//		this.privateKey = privateKey;
//	}
//
//	public String getFormat()
//	{
//		return filePath.substring(filePath.lastIndexOf(".") + 1).toUpperCase(Locale.ENGLISH);
//	}
//
//	public void setFormat(String format)
//	{
//		this.format = format;
//	}
//	public String getTitle()
//	{
//		return title;
//	}
//
//	public void setTitle(String title)
//	{
//		this.title = title;
//	}
//
//	public String getAsset_id()
//	{
//		return asset_id;
//	}
//
//	public void setAsset_id(String asset_id)
//	{
//		this.asset_id = asset_id;
//	}
//
//	public String getOdd_datetime_end()
//	{
//		return odd_datetime_end;
//	}
//
//	public void setOdd_datetime_end(String odd_datetime_end)
//	{
//		this.odd_datetime_end = odd_datetime_end;
//	}
//
//	public String getValidity()
//	{
//		return validity;
//	}
//
//	public void setValidity(String validity)
//	{
//		this.validity = validity;
//	}
//
//	public String getMyProId()
//	{
//		return myProId;
//	}
//
//	public void setMyProId(String myProId)
//	{
//		this.myProId = myProId;
//	}
//
//	public String getContentId()
//	{
//		return contentId;
//	}
//
//	public void setContentId(String contentId)
//	{
//		this.contentId = contentId;
//	}
//
//	public void setCheckOpen(boolean checkOpen)
//	{
//		this.checkOpen = checkOpen;
//	}
//
//	public boolean isCheckOpen()
//	{
//		return checkOpen;
//	}
//
//	@Override
//	public int describeContents()
//	{
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags)
//	{
//		dest.writeString(this.filePath);
//		dest.writeString(this.publicKey);
//		dest.writeString(this.privateKey);
//
//		dest.writeString(this.format);
//		dest.writeString(this.title);
//		dest.writeString(this.asset_id);
//		dest.writeString(this.odd_datetime_end);
//		dest.writeString(this.validity);
//
//		dest.writeString(this.myProId);
//		dest.writeString(this.contentId);
//		dest.writeInt(this.checkOpen ? 1 : 0);
//	}
//
//	public final static Parcelable.Creator<DrmFile> CREATOR = new Creator<DrmFile>()
//	{
//
//		@Override
//		public DrmFile[] newArray(int size)
//		{
//			return new DrmFile[size];
//		}
//
//		@Override
//		public DrmFile createFromParcel(Parcel source)
//		{
//			String filePath = source.readString();
//			String publicKey = source.readString();
//			String privateKey = source.readString();
//			DrmFile df = new DrmFile(filePath, publicKey, privateKey);
//
//			df.setFormat(source.readString());
//			df.setTitle(source.readString());
//			df.setAsset_id(source.readString());
//			df.setOdd_datetime_end(source.readString());
//			df.setValidity(source.readString());
//
//			df.setMyProId(source.readString());
//			df.setContentId(source.readString());
//			df.setCheckOpen(source.readInt() == 1 ? true : false);
//			return df;
//		}
//	};
//
//	@Override
//	public String toString()
//	{
//		return "DrmFile [filePath=" + filePath + ", publicKey=" + publicKey + ", privateKey="
//				+ privateKey + ", format=" + format + ", title=" + title + ", asset_id=" + asset_id
//				+ ", odd_datetime_end=" + odd_datetime_end + ", validity=" + validity
//				+ ", myProId=" + myProId + ", contentId=" + contentId + ", checkOpen=" + checkOpen
//				+ "]";
//	}
//	
//}
