package cn.com.pyc.suizhi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import cn.com.pyc.suizhi.manager.DownloadTaskManagerPat;

public class FileData implements Parcelable {

    private String itemId;
    private String content_name;
    private String content_format;
    private long content_size;
    private int page_num;
    private double length;
    private String version;
    private String versionInfo;
    private long play_progress;
    private String musicLyric_id;
    private boolean overdue; //是否已过期

    //add:20170216
    private String collectionId;
    private String currentItemId;
    private String latestItemId;
    private transient List<FileVersionModel> itemList;

    // 专辑信息
    private String myProId;
    private String picture_ratio;
    private String authors;

    // 下载相关
    private int taskState = DownloadTaskManagerPat.INIT; // 任务状态，初始化
    private int progress; // 下载保存进度


    public String getCurrentItemId() {
        return currentItemId;
    }

    public void setCurrentItemId(String currentItemId) {
        this.currentItemId = currentItemId;
    }

    public String getLatestItemId() {
        return latestItemId;
    }

    public void setLatestItemId(String latestItemId) {
        this.latestItemId = latestItemId;
    }

    public List<FileVersionModel> getItemList() {
        return itemList;
    }

    public void setItemList(List<FileVersionModel> itemList) {
        this.itemList = itemList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.itemId);
        dest.writeString(this.collectionId);
        dest.writeString(this.currentItemId);
        dest.writeString(this.latestItemId);
        dest.writeString(this.content_name);
        dest.writeString(this.content_format);
        dest.writeLong(this.content_size);
        dest.writeInt(this.page_num);
        dest.writeDouble(this.length);
        dest.writeString(this.version);
        dest.writeString(this.versionInfo);
        dest.writeLong(this.play_progress);
        dest.writeString(this.musicLyric_id);
        dest.writeByte(this.overdue ? (byte) 1 : (byte) 0);

        dest.writeString(this.myProId);
        dest.writeString(this.picture_ratio);
        dest.writeString(this.authors);

        dest.writeInt(this.taskState);
        dest.writeInt(this.progress);
    }

    public final static Parcelable.Creator<FileData> CREATOR = new Creator<FileData>() {

        @Override
        public FileData[] newArray(int size) {
            return new FileData[size];
        }

        @Override
        public FileData createFromParcel(Parcel source) {
            FileData data = new FileData();
            data.setItemId(source.readString());
            data.setCollectionId(source.readString());
            data.setLatestItemId(source.readString());
            data.setCurrentItemId(source.readString());

            data.setContent_name(source.readString());
            data.setContent_format(source.readString());
            data.setContent_size(source.readLong());
            data.setPage_num(source.readInt());
            data.setLength(source.readDouble());
            data.setVersion(source.readString());
            data.setVersionInfo(source.readString());
            data.setPlay_progress(source.readLong());
            data.setMusicLyric_id(source.readString());
            data.setOverdue(source.readByte() == 1);

            data.setMyProId(source.readString());
            data.setPicture_ratio(source.readString());
            data.setAuthors(source.readString());

            data.setTaskState(source.readInt());
            data.setProgress(source.readInt());
            return data;
        }
    };


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getContent_name() {
        return content_name;
    }

    public void setContent_name(String content_name) {
        this.content_name = content_name;
    }

    public String getContent_format() {
        return content_format;
    }

    public void setContent_format(String content_format) {
        this.content_format = content_format;
    }

    public long getContent_size() {
        return content_size;
    }

    public void setContent_size(long content_size) {
        this.content_size = content_size;
    }

    public int getPage_num() {
        return page_num;
    }

    public void setPage_num(int page_num) {
        this.page_num = page_num;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getMyProId() {
        return myProId;
    }

    public void setMyProId(String myProId) {
        this.myProId = myProId;
    }

    public String getPicture_ratio() {
        return picture_ratio;
    }

    public void setPicture_ratio(String picture_ratio) {
        this.picture_ratio = picture_ratio;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public int getTaskState() {
        return taskState;
    }

    public void setTaskState(int taskState) {
        this.taskState = taskState;
    }

//    public int getPosition() {
//        return position;
//    }
//
//    public void setPosition(int position) {
//        this.position = position;
//    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setPlay_progress(long play_progress) {
        this.play_progress = play_progress;
    }

    public long getPlay_progress() {
        return play_progress;
    }

    public void setMusicLyric_id(String musicLyric_id) {
        this.musicLyric_id = musicLyric_id;
    }

    public String getMusicLyric_id() {
        return musicLyric_id == null ? "" : musicLyric_id;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public boolean isOverdue() {
        return overdue;
    }

    @Override
    public String toString() {
        return "FileData{" +
                "itemId='" + itemId + '\'' +
                ", content_name='" + content_name + '\'' +
                ", content_format='" + content_format + '\'' +
                ", content_size=" + content_size +
                ", page_num=" + page_num +
                ", length=" + length +
                ", version='" + version + '\'' +
                ", versionInfo='" + versionInfo + '\'' +
                ", play_progress=" + play_progress + '\'' +
                ", musicLyric_id=" + musicLyric_id + '\'' +
                ", collectionId='" + collectionId + '\'' +
                ", currentItemId='" + currentItemId + '\'' +
                ", latestItemId='" + latestItemId + '\'' +
                '}';
    }
}
