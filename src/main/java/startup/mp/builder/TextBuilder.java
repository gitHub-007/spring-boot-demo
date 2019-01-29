package startup.mp.builder;

import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import net.ptnetwork.wxap.mp.service.WeixinService;

/**
 * @author Binary Wang
 */
public class TextBuilder extends AbstractBuilder {

  @Override
  public WxMpXmlOutMessage build(String content, WxMpXmlMessage wxMessage,
                                 WeixinService service) {
    WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content(content)
      .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
      .build();
    return m;
  }

}