package cn.com.pyc.suizhi.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import cn.com.pyc.suizhi.common.DrmPat;


/**
 * 专辑信息 <br\>
 * 实现Parcelable接口, 序列化过程：必须按成员变量声明的顺序进行封装  <br\>
 * <br\>
 */
public class ProductInfo implements Parcelable {

    private String authors;
    private String category;
    private String myProId;
    private String picture_ratio;
    private String picture_url;
    private String productName;
    private String proId;

    private String publishDate;
    private boolean valid;  //在当前端是否可用

    private String product_buy_time;//产品的购买时间
    private String product_creater_id;//用于拼店铺地址
    private String product_creater_name;//发布者姓名
    private String storeName;//店铺名称
    private String grade;//等级


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.authors);
        dest.writeString(this.category);
        dest.writeString(this.myProId);
        dest.writeString(this.picture_ratio);
        dest.writeString(this.picture_url);
        dest.writeString(this.productName);
        dest.writeString(this.proId);

        dest.writeString(this.publishDate);
        dest.writeByte(this.valid ? (byte) 1 : (byte) 0);

        dest.writeString(this.product_buy_time);
        dest.writeString(this.product_creater_id);
        dest.writeString(this.storeName);
        dest.writeString(this.grade);

    }

    public final static Parcelable.Creator<ProductInfo> CREATOR = new Creator<ProductInfo>() {

        @Override
        public ProductInfo[] newArray(int size) {

            return new ProductInfo[size];
        }

        @Override
        public ProductInfo createFromParcel(Parcel source) {
            ProductInfo o = new ProductInfo();
            o.setAuthors(source.readString());
            o.setCategory(source.readString());
            o.setMyProId(source.readString());
            o.setPicture_ratio(source.readString());
            o.setPicture_url(source.readString());
            o.setProductName(source.readString());
            o.setProId(source.readString());

            o.setPublishDate(source.readString());
            o.setValid(source.readByte() == 1);

            o.setProduct_buy_time(source.readString());
            o.setProduct_creater_id(source.readString());
            o.setStoreName(source.readString());
            o.setGrade(source.readString());

            return o;
        }
    };

    /**
     * 类别：MUSIC; VIDEO; BOOK
     *
     * @return
     */
    public String getCategory() {
        return getAlbumCategory(this.category);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 中文类别：音乐；视频；图书
     *
     * @return
     */
    public String getCategoryName() {
        return category;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
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

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProId() {
        return proId;
    }

    public void setProId(String proId) {
        this.proId = proId;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getProduct_buy_time() {
        return product_buy_time;
    }

    public void setProduct_buy_time(String product_buy_time) {
        this.product_buy_time = product_buy_time;
    }

    public String getProduct_creater_id() {
        return product_creater_id;
    }

    public void setProduct_creater_id(String product_creater_id) {
        this.product_creater_id = product_creater_id;
    }

    public String getProduct_creater_name() {
        return product_creater_name;
    }

    public void setProduct_creater_name(String product_creater_name) {
        this.product_creater_name = product_creater_name;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public String toString() {
        return "ProductInfo{" +
                "authors='" + authors + '\'' +
                ", category='" + category + '\'' +
                ", myProId='" + myProId + '\'' +
                ", picture_ratio='" + picture_ratio + '\'' +
                ", picture_url='" + picture_url + '\'' +
                ", productName='" + productName + '\'' +
                ", proId='" + proId + '\'' +
                ", publishDate='" + publishDate + '\'' +
                ", valid=" + valid +
                ", product_creater_id='" + product_creater_id + '\'' +
                ", product_creater_name='" + product_creater_name + '\'' +
                ", storeName='" + storeName + '\'' +
                ", grade='" + grade + '\'' +
                '}';
    }

    /**
     * 返回的种类可能是中文类别
     *
     * @param category
     * @return
     */
    public static String getAlbumCategory(String category) {
        String nameCategory = "";
        if (TextUtils.isEmpty(category)) return nameCategory;
        switch (category) {
            case DrmPat.MUSIC:
            case "音乐":
                nameCategory = DrmPat.MUSIC;
                break;
            case DrmPat.BOOK:
            case "图书":
                nameCategory = DrmPat.BOOK;
                break;
            case DrmPat.VIDEO:
            case "视频":
                nameCategory = DrmPat.VIDEO;
                break;
            default:
                break;
        }
        return nameCategory;
    }
}
