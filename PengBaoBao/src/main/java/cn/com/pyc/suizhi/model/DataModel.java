package cn.com.pyc.suizhi.model;

/**
 * Created by hudq on 2017/3/31.
 */

public class DataModel extends com.sz.mobilesdk.models.BaseModel {

    private Data data;

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        private String url;             //下载url
        private String fileInfo;        //加密证书

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(String fileInfo) {
            this.fileInfo = fileInfo;
        }
    }
}
