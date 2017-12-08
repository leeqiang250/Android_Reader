package cn.com.pyc.suizhi.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 播放模型 （可作为全局播放模型使用，包括图书，音乐，视频）
 * <p>
 *
 * @author hudq
 */
public class DrmFile implements Parcelable {

    private String fileId;                          // 文件id
    private String lyricId;                         // 歌词id，书籍和视频则为空
    private String fileName;                        // 标题名称
    private String fileFormat;                      // 文件格式,扩展名
    private long fileSize;                          // 文件大小
    private String filePath;                        // 文件路径
    private String privateKey;                      // 私钥
    private String publicKey;                       // 公钥，无就为空

    private String validityTime;                    // 有效期信息，eg:00天00小时
    private String endDatetime;                     // 有效结束日期
    private boolean checkOpen;                      // 是否有权限打开
    private boolean inEffective;                    // 是否未生效

    private String assetId;                         // 权限约束文件Id
    private String myProductId;                     // 专辑id
    private String productUrl;                      // 专辑图片url
    private String collectionId;                    // 文件所属集合的id


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileId);
        dest.writeString(this.lyricId);
        dest.writeString(this.fileName);
        dest.writeString(this.fileFormat);
        dest.writeLong(this.fileSize);
        dest.writeString(this.filePath);
        dest.writeString(this.privateKey);
        dest.writeString(this.publicKey);
        dest.writeString(this.validityTime);
        dest.writeString(this.endDatetime);
        dest.writeByte(this.checkOpen ? (byte) 1 : (byte) 0);
        dest.writeByte(this.inEffective ? (byte) 1 : (byte) 0);
        dest.writeString(this.assetId);
        dest.writeString(this.myProductId);
        dest.writeString(this.productUrl);
        dest.writeString(this.collectionId);
    }

    public DrmFile() {
    }

    protected DrmFile(Parcel in) {
        this.fileId = in.readString();
        this.lyricId = in.readString();
        this.fileName = in.readString();
        this.fileFormat = in.readString();
        this.fileSize = in.readLong();
        this.filePath = in.readString();
        this.privateKey = in.readString();
        this.publicKey = in.readString();
        this.validityTime = in.readString();
        this.endDatetime = in.readString();
        this.checkOpen = in.readByte() != 0;
        this.inEffective = in.readByte() != 0;
        this.assetId = in.readString();
        this.myProductId = in.readString();
        this.productUrl = in.readString();
        this.collectionId = in.readString();
    }

    public static final Creator<DrmFile> CREATOR = new Creator<DrmFile>() {
        @Override
        public DrmFile createFromParcel(Parcel source) {
            return new DrmFile(source);
        }

        @Override
        public DrmFile[] newArray(int size) {
            return new DrmFile[size];
        }
    };

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setLyricId(String lyricId) {
        this.lyricId = lyricId;
    }

    public String getLyricId() {
        return lyricId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getValidityTime() {
        return validityTime;
    }

    public void setValidityTime(String validityTime) {
        this.validityTime = validityTime;
    }

    public String getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(String endDatetime) {
        this.endDatetime = endDatetime;
    }

    public boolean isCheckOpen() {
        return checkOpen;
    }

    public void setCheckOpen(boolean checkOpen) {
        this.checkOpen = checkOpen;
    }

    public boolean isInEffective() {
        return inEffective;
    }

    public void setInEffective(boolean inEffective) {
        this.inEffective = inEffective;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getMyProductId() {
        return myProductId;
    }

    public void setMyProductId(String myProductId) {
        this.myProductId = myProductId;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }


    @Override
    public String toString() {
        return "DrmFile{" +
                "fileId='" + fileId + '\'' +
                "lyricId='" + lyricId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileFormat='" + fileFormat + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", filePath='" + filePath + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", validityTime='" + validityTime + '\'' +
                ", endDatetime='" + endDatetime + '\'' +
                ", checkOpen=" + checkOpen +
                ", inEffective=" + inEffective +
                ", assetId='" + assetId + '\'' +
                ", myProductId='" + myProductId + '\'' +
                ", productUrl='" + productUrl + '\'' +
                ", collectionId='" + collectionId + '\'' +
                '}';
    }
}
