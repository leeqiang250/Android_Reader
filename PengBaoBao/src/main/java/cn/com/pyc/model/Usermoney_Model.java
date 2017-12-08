package cn.com.pyc.model;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2016/12/16 18:28
 */
public class Usermoney_Model extends BaseModel_ {

    private Usermoney_data Result;

    public Usermoney_data getResult() {
        return Result;
    }

    public void setResult(Usermoney_data result) {
        Result = result;
    }
    public class Usermoney_data {

        private String money;
        private String freemoney;

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public String getFreemoney() {
            return freemoney;
        }

        public void setFreemoney(String freemoney) {
            this.freemoney = freemoney;
        }
    }
}
