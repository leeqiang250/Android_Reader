package cn.com.pyc.model;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (获取用户信息)
 * @date 2016/12/6 11:33
 */
public class Userinfo_Model extends BaseModel_ {

    private userinfo_data Result;

    public userinfo_data getResult() {
        return Result;
    }

    public void setResult(userinfo_data result) {
        Result = result;
    }


    public static class userinfo_data{
        private String UserName;//用户名
        private String UserNick;//用户昵称
        private String PicUrl;//头像图片连接
        private String MobilePhone;//用户手机
        private String Email;//用户邮箱
        private String EmailStatus;//邮箱状态（0：未绑定，1：已绑定）
        private String BlackList;//用户自定义黑名单
        private String MobileStatus;//手机状态（0：未绑定，1：已绑定）
        private String QQNick;//用户QQ昵称
        private String isEnterpriseChild;//是否是子账号，0：不是子账号，!=0:子账号
        private String Password;//为了安全，密码采用加密传输。加密方式为对称加密，密钥“asdfjkle“，字母大写


        public String getUserName() {
            return UserName;
        }

        public void setUserName(String userName) {
            UserName = userName;
        }

        public String getUserNick() {
            return UserNick;
        }

        public void setUserNick(String userNick) {
            UserNick = userNick;
        }

        public String getPicUrl() {
            return PicUrl;
        }

        public void setPicUrl(String picUrl) {
            PicUrl = picUrl;
        }

        public String getMobilePhone() {
            return MobilePhone;
        }

        public void setMobilePhone(String mobilePhone) {
            MobilePhone = mobilePhone;
        }

        public String getEmail() {
            return Email;
        }

        public void setEmail(String email) {
            Email = email;
        }

        public String getEmailStatus() {
            return EmailStatus;
        }

        public void setEmailStatus(String emailStatus) {
            EmailStatus = emailStatus;
        }

        public String getBlackList() {
            return BlackList;
        }

        public void setBlackList(String blackList) {
            BlackList = blackList;
        }

        public String getMobileStatus() {
            return MobileStatus;
        }

        public void setMobileStatus(String mobileStatus) {
            MobileStatus = mobileStatus;
        }

        public String getQQNick() {
            return QQNick;
        }

        public void setQQNick(String QQNick) {
            this.QQNick = QQNick;
        }

        public String getIsEnterpriseChild() {
            return isEnterpriseChild;
        }

        public void setIsEnterpriseChild(String isEnterpriseChild) {
            this.isEnterpriseChild = isEnterpriseChild;
        }

        public String getPassword() {
            return Password;
        }

        public void setPassword(String password) {
            Password = password;
        }
    }
}
