//package cn.com.pyc.suizhi.model;
//
//import com.sz.mobilesdk.models.BaseModel;
//
//import cn.com.pyc.bean.SmInfo;
//
///**
// * Created by 熊大 on 2017/8/22.
// */
//
//public class ReaderAndSuizhiModel extends BaseModel {
//
//    private int fileType ;          //item左侧图标类型：1：reader（显示固定图标）   2：随知（显示文件对应的图标）
//
//    private String fileName;        //文件名 或者 系列名
//
//    private String fileAuthor;      //作者 或者 机构名
//
//    private String fileDate;        //文件日期
//
//    private String icoUrl ;         //左侧图标对应的url
//
//    private SmInfo info;
//
//    private ProductInfo productInfo;
//
//    public int getFileType() {
//        return fileType;
//    }
//
//    public void setFileType(int fileType) {
//        this.fileType = fileType;
//    }
//
//    public String getFileAuthor() {
//        return fileAuthor;
//    }
//
//    public void setFileAuthor(String fileAuthor) {
//        this.fileAuthor = fileAuthor;
//    }
//
//    public String getFileDate() {
//        return fileDate;
//    }
//
//    public void setFileDate(String fileDate) {
//        this.fileDate = fileDate;
//    }
//
//    public String getIcoUrl() {
//        return icoUrl;
//    }
//
//    public void setIcoUrl(String icoUrl) {
//        this.icoUrl = icoUrl;
//    }
//
//    public SmInfo getInfo() {
//        return info;
//    }
//
//    public void setInfo(SmInfo info) {
//        this.info = info;
//    }
//
//    public String getFileName() {
//        return fileName;
//    }
//
//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }
//
//    public ProductInfo getProductInfo() {
//        return productInfo;
//    }
//
//    public void setProductInfo(ProductInfo productInfo) {
//        this.productInfo = productInfo;
//    }
//
//    @Override
//    public String toString() {
//        return "ReaderAndSuizhiModel{" +
//                "fileType=" + fileType +
//                ", fileName='" + fileName + '\'' +
//                ", fileAuthor='" + fileAuthor + '\'' +
//                ", fileDate='" + fileDate + '\'' +
//                ", icoUrl='" + icoUrl + '\'' +
//                ", info=" + info +
//                ", productInfo=" + productInfo +
//                '}';
//    }
//}
