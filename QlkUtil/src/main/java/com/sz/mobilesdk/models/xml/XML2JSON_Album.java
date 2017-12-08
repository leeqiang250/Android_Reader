package com.sz.mobilesdk.models.xml;

import java.util.List;

import org.json.JSONObject;

/**
 * Created by simaben on 2014/6/9. drm描述文件，内容为json格式的数据
 */
public class XML2JSON_Album {

    private List<String> contentList;
    private List<String> infoList;
    private JSONObject infoObj;

    public JSONObject getInfoObj() {
        return infoObj;
    }

    public void setInfoObj(JSONObject infoObj) {
        this.infoObj = infoObj;
    }

    public List<String> getContentList() {
        return contentList;
    }

    public void setContentList(List<String> contentList) {
        this.contentList = contentList;
    }

    public List<String> getInfoList() {
        return infoList;
    }

    public void setInfoList(List<String> infoList) {
        this.infoList = infoList;
    }

}
