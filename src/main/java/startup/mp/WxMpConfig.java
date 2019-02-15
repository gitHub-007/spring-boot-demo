package startup.mp;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * @author Noah
 * @description WxMpConfig
 * @created at 2018-11-28 16:11:22
 **/
@Configuration
public class WxMpConfig {

    private String token;

    @Value("${appId}")
    private String appid;

    @Value("${appSecret}")
    private String appSecret;

    private String aesKey;

    public String getToken() {
        return token;
    }

    public String getAppid() {
        return this.appid;
    }

    public String getAppSecret() {
        return this.appSecret;
    }

    public String getAesKey() {
        return this.aesKey;
    }

}
