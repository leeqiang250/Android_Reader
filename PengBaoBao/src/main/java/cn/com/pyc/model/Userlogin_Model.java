package cn.com.pyc.model;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (获取用户登录返回值)
 * @date 2016/12/6 11:52
 */
public class Userlogin_Model extends BaseModel_ {

    private String Result;//用户密码（md5加密）


    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }
}
