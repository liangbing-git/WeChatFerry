package com.wechat.ferry.service;

import com.wechat.ferry.entity.vo.request.WxPpWcfSendTextMsgReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class BackMsgService {

    @Resource
    private WeChatDllService weChatDllService;

    public void sendTxtInfo(String recipient, String assistantMsg) {
        WxPpWcfSendTextMsgReq textMsgReq = new WxPpWcfSendTextMsgReq();
        textMsgReq.setMsgText(assistantMsg);
        textMsgReq.setRecipient(recipient);
        weChatDllService.sendTextMsg(textMsgReq);
    }
}
