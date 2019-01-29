package startup.mp.service;

import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.kefu.result.WxMpKfInfo;
import me.chanjar.weixin.mp.bean.kefu.result.WxMpKfOnlineList;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.constant.WxMpEventConstants;
import net.ptnetwork.config.WxMpConfig;
import net.ptnetwork.wxap.mp.handler.KfSessionHandler;
import net.ptnetwork.wxap.mp.handler.LogHandler;
import net.ptnetwork.wxap.mp.handler.MsgHandler;

/**
 * @author Noah
 * @description WeixinService
 * @created at 2018-11-27 11:50:10
 **/
@Service("weixinService")
public class WeixinService extends WxMpServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(WeixinService.class);

    private static final long ACCESS_TOKEN__EXPIRES = TimeUnit.HOURS.toMillis(2);

    public static final String XML_ROOT = "<xml></xml>";

    @Autowired
    protected LogHandler logHandler;

    @Autowired
    private MsgHandler msgHandler;

    @Autowired
    protected KfSessionHandler kfSessionHandler;

    @Autowired
    private WxMpConfig wxConfig;

    private WxMpMessageRouter router;

    @PostConstruct
    private void init() throws Exception {
        final WxMpInMemoryConfigStorage wxMpInMemoryConfigStorage = new WxMpInMemoryConfigStorage();
        wxMpInMemoryConfigStorage.setAppId(this.wxConfig.getAppid());
        wxMpInMemoryConfigStorage.setSecret(this.wxConfig.getAppSecret());
        wxMpInMemoryConfigStorage.setAesKey(this.wxConfig.getAesKey());
        wxMpInMemoryConfigStorage.setExpiresTime(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ACCESS_TOKEN__EXPIRES));
        super.setWxMpConfigStorage(wxMpInMemoryConfigStorage);
        String token = this.wxConfig.getToken();
        if (StringUtils.isBlank(token)){
            token = super.getAccessToken(true);
        }
        wxMpInMemoryConfigStorage.setAccessToken(token);
        this.refreshRouter();
    }

    public WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return this.router.route(message);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }

        return null;
    }

    public List<WxMpKfInfo> onlineKfList() {
        List<WxMpKfInfo> onlineList = new ArrayList<>();
        try {
            WxMpKfOnlineList kfOnlineList = this.getKefuService().kfOnlineList();
            if (kfOnlineList != null) {
                onlineList = kfOnlineList.getKfOnlineList() == null ? onlineList : kfOnlineList.getKfOnlineList();
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
        } finally {
            return onlineList;
        }
    }

    /**
     * 转发客服信息到客服系统
     *
     * @param openId
     * @param content 用户发送的信息
     * @return
     */
    public WxMpXmlOutMessage sendMsgToKf(String openId, String wxAccount, String content) {
        WxMpXmlMessage wxMpXmlMessage = WxMpXmlMessage.fromXml(XML_ROOT);
        wxMpXmlMessage.setFromUser(openId);
        wxMpXmlMessage.setToUser(wxAccount);
        wxMpXmlMessage.setContent(content);
        wxMpXmlMessage.setCreateTime(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        wxMpXmlMessage.setMsgType(WxConsts.XmlMsgType.TEXT);
        return route(wxMpXmlMessage);
    }

    private void refreshRouter() {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(this);
        newRouter.rule().handler(this.logHandler).next();
        newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(WxMpEventConstants.CustomerService.KF_CREATE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(WxMpEventConstants.CustomerService.KF_CLOSE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(WxMpEventConstants.CustomerService.KF_SWITCH_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).handler(this.msgHandler).end();
        this.router = newRouter;
    }

}
