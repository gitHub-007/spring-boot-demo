package startup.mp.handler;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import me.chanjar.weixin.common.session.WxSession;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.result.WxMpKfInfo;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import startup.mp.builder.TextBuilder;
import startup.mp.service.WeixinService;

/**
 * @author Noah
 * @description MsgHandler
 * @created at 2018-11-38 15:27:23
 **/
@Component
public class MsgHandler extends AbstractHandler {

    private static final String KF_ACCOUNT_SESSION_NAME = "KfAccount";

    /**
     * 文本消息的逻辑在此处修改
     *
     * @param wxMessage
     * @param context
     * @param wxMpService
     * @param sessionManager
     * @return
     */
    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService,
                                    WxSessionManager sessionManager) {

        logger.warn("wxMessage.getToUser()={},wxMessage.getFromUser()={}", wxMessage.getToUser(),
                    wxMessage.getFromUser());
        Objects.requireNonNull(wxMessage.getToUser());
        Objects.requireNonNull(wxMessage.getFromUser());
        WeixinService wxService = (WeixinService) wxMpService;
        WxSession wxSession = sessionManager.getSession(wxMessage.getToUser(), true);
        String KfAccount = (String) wxSession.getAttribute(KF_ACCOUNT_SESSION_NAME);
        Set<String> kfAccounts =
                wxService.onlineKfList().stream().map(kf -> kf.getAccount()).collect(Collectors.toSet());
        //判断之前的客服是否在线,否则重新指定新的客服
        if (!kfAccounts.contains(KfAccount)) {
            Optional<WxMpKfInfo> wxMpKfInfo =
                    wxService.onlineKfList().stream().min(Comparator.comparing(WxMpKfInfo::getAcceptedCase));
            if (wxMpKfInfo.isPresent()) {
                KfAccount = wxMpKfInfo.get().getAccount();
                wxSession.setAttribute(KF_ACCOUNT_SESSION_NAME, KfAccount);
            }
        }
        logger.warn("KfAccount={}", KfAccount);
        WxMpXmlOutMessage wxMpXmlOutMessage;
        //如果有客服在线就转发，否则直接回复用户
        if (!StringUtils.isBlank(KfAccount)) {
            wxMpXmlOutMessage =
                    WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser()).kfAccount(KfAccount).build();
        } else {
            String content = "抱歉,当前客服不在线,请您稍后咨询！";
            wxMpXmlOutMessage = new TextBuilder().build(content, wxMessage, wxService);
        }
        logger.warn("wxMpXmlOutMessage={}", wxMpXmlOutMessage);
        return wxMpXmlOutMessage;

    }
}
