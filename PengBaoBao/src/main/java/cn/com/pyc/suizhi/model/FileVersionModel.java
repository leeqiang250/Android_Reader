package cn.com.pyc.suizhi.model;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2017/2/15 17:24
 */
public class FileVersionModel {

    private String itemId;
    private String content_name;
    private long content_size;
    private String content_format;
    private int page_num;
    private double length;
    private String version;
    private String versionInfo;
    private long play_progress;
    private String musicLyric_id;


    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getContent_name() {
        return content_name;
    }

    public void setContent_name(String content_name) {
        this.content_name = content_name;
    }

    public long getContent_size() {
        return content_size;
    }

    public void setContent_size(long content_size) {
        this.content_size = content_size;
    }

    public String getContent_format() {
        return content_format;
    }

    public void setContent_format(String content_format) {
        this.content_format = content_format;
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
        return musicLyric_id;
    }

    @Override
    public String toString() {
        return "FileVersionModel{" +
                "itemId='" + itemId + '\'' +
                ", content_name='" + content_name + '\'' +
                ", content_size=" + content_size +
                ", content_format='" + content_format + '\'' +
                ", page_num=" + page_num +
                ", length=" + length +
                ", version='" + version + '\'' +
                ", versionInfo='" + versionInfo + '\'' +
                ", play_progress=" + play_progress + '\'' +
                ", musicLyric_id=" + musicLyric_id +
                '}';
    }
}
