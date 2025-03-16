package com.wechat.ferry.strategy.msg.receive.impl;

import com.wechat.ferry.config.WeChatFerryProperties;
import com.wechat.ferry.entity.dto.WxPpMsgDTO;
import com.wechat.ferry.entity.vo.request.WxPpWcfSendImageMsgReq;
import com.wechat.ferry.enums.ReceiveMsgChannelEnum;
import com.wechat.ferry.enums.WcfMsgTypeEnum;
import com.wechat.ferry.handle.ZhiPuAiChatClient;
import com.wechat.ferry.service.BackMsgService;
import com.wechat.ferry.service.WeChatDllService;
import com.wechat.ferry.service.video.QueryVideoService;
import com.wechat.ferry.strategy.msg.receive.ReceiveMsgStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 策略实现类-接收消息-签到处理
 *
 * @author chandler
 * @date 2024-12-25 14:19
 */
@Slf4j
@Component
public class GroupMsgStrategyImpl implements ReceiveMsgStrategy {

    private static final Map<String, ZhiPuAiChatClient> GROUP_AI = new ConcurrentHashMap<>();

    private WeChatDllService weChatDllService;

    private QueryVideoService queryVideoService;

    public static final String SPACE = " ";

    public static final String AT_SELF = "@幻象" + SPACE;

    @Resource
    private BackMsgService backMsgService;

    @Resource
    private WeChatFerryProperties weChatFerryProperties;

    @Autowired
    public void setWeChatDllService(WeChatDllService weChatDllService) {
        this.weChatDllService = weChatDllService;
    }

    @Autowired
    public void setQueryVideoService(QueryVideoService queryVideoService) {
        this.queryVideoService = queryVideoService;
    }

    @Override
    public String getStrategyType() {
        return ReceiveMsgChannelEnum.GROUP_MSG.getCode();
    }

    @Override
    public String doHandle(WxPpMsgDTO dto) {
        // 不是群聊或者是自己发的消息则不处理
        if (!dto.getIsGroup()) {
            return "";
        }

        if (StringUtils.equals(dto.getType().toString(), WcfMsgTypeEnum.PICTURE.getCode())) {
            // TODO 如果是文件的话，先保存下来
            return "";
        }

        if (StringUtils.equals(dto.getType().toString(), WcfMsgTypeEnum.TEXT.getCode())) {
            // 文本没有艾特我，则不处理
            if (!dto.getContent().contains(AT_SELF)) {
                return "";
            }
            dto.setContent(dto.getContent().replace(AT_SELF, ""));


            // 搜剧模式
            if (dto.getContent().startsWith("百度搜") ||
                dto.getContent().startsWith("迅雷搜") ||
                dto.getContent().startsWith("搜")) {
                String panType = "baidu";
                if (dto.getContent().contains("迅雷")) {
                    panType = "xunlei";
                }
                String videoName = dto.getContent().replace("搜", "").replace("百度", "")
                                      .replace("迅雷", "");
                String videoInfo = queryVideoService.query(videoName, panType);
                backMsgService.sendTxtInfo(dto.getRoomId(), videoInfo);
                return "";
            }
            // 如果和文字，则调用AI对话
            ZhiPuAiChatClient zhiPuAiChatClient = GROUP_AI.computeIfAbsent(dto.getRoomId(),
                                                     k -> new ZhiPuAiChatClient(weChatFerryProperties.getZhiPuAiKey()));
            // content满足  画一张xxx开头，则打开画图模式
            if (isDraw(dto)) {
                String localPath = zhiPuAiChatClient.drawOnly(dto.getContent());
                WxPpWcfSendImageMsgReq imgMsgReq = new WxPpWcfSendImageMsgReq();
                imgMsgReq.setRecipient(dto.getRoomId());
                imgMsgReq.setResourcePath(localPath);
                weChatDllService.sendImageMsg(imgMsgReq);
            } else if (isVideo(dto)) {
                zhiPuAiChatClient.videoOnly(dto, weChatDllService);
            } else {
                String assistantMsg = zhiPuAiChatClient.chatOnly(dto.getContent());
                backMsgService.sendTxtInfo(dto.getRoomId(), assistantMsg);
            }
        }
        return "";
    }

    private boolean isVideo(WxPpMsgDTO dto) {
        boolean res = dto.getContent().startsWith("文生视频") || dto.getContent().startsWith("图生视频");
        if (res) {
            dto.setContent(dto.getContent().replace("文生视频", "").replace("图生视频", ""));
        }
        return res;
    }

    private static boolean isDraw(WxPpMsgDTO dto) {
        boolean res = dto.getContent().startsWith("画一张") ||
                      dto.getContent().startsWith("画一个") ||
                      dto.getContent().startsWith("画一幅");
        if (res) {
            dto.setContent(dto.getContent().replace("画一张", "").replace("画一个", "").replace("画一幅", ""));
        }
        return res;
    }
}
