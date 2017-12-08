package cn.com.pyc.bean;

import java.util.List;
import java.util.Map;

import cn.com.pyc.suizhi.model.ProductInfo;

public class RZListBean {

    //1 pbb文件；2 系列；3 随知列表数据
    public static class Source {
        public static final byte S_PBB = 1;
        public static final byte S_SERIES = 2;
        public static final byte S_SZ = 3;
    }

    private int source; //来源：1 pbb文件；2 系列；3 随知列表数据
    private String name; //名称
    private String time; //时间
    private String owner; //所有者

    private ProductInfo mProductInfo;   // source=3时对应的bean;
    private String mFilePath;           // source=1时对应的path;source=2则取集合中第一个path；
    private Map<String, List<String>> mSeriesMap;//source=2,存放系列文件的path集合


    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ProductInfo getProductInfo() {
        if (source == Source.S_SZ && mProductInfo == null) {
            throw new IllegalArgumentException("赋值错误，mProductInfo is null.");
        }
        return mProductInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        mProductInfo = productInfo;
    }

    public String getFilePath() {
        if (source == Source.S_PBB && mFilePath == null) {
            throw new IllegalArgumentException("赋值错误，mSmInfo is null.");
        }
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public Map<String, List<String>> getSeriesMap() {
        if (source == Source.S_SERIES && mSeriesMap == null) {
            throw new IllegalArgumentException("赋值错误，mSmInfo is null.");
        }
        return mSeriesMap;
    }

    public void setSeriesMap(Map<String, List<String>> seriesMap) {
        mSeriesMap = seriesMap;
    }


    @Override
    public String toString() {
        return "RZListBean{" +
                "source=" + source +
                ", mFilePath=" + mFilePath +
                ", name='" + name + '\'' +
                ", time='" + time + '\'' +
                ", owner='" + owner + '\'' +
                ", mProductInfo=" + (mProductInfo != null ? mProductInfo.toString() : "空") +
                '}';
    }
}
