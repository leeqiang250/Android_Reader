package cn.com.pyc.model;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (Token为客户端从授权服务器获取的access_token)
 * @date 2016/12/6 11:46
 */
public class ClientCredentials_Model {

    private String access_token;//访问资源服务器所需的令牌
    private String token_type;//令牌类型
    private String expires_in;//令牌有效期，单位：秒
    private String refresh_token;//用于刷新访问令牌的刷新凭证


    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
