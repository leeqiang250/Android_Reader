package cn.com.pyc.model;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (基础model)
 * @date 2016/12/6 11:29
 */
public class BaseModel_ {

    private String Status;//“1”:获得成功，”0”:获取失败

    private String Message;//错误原因


    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
