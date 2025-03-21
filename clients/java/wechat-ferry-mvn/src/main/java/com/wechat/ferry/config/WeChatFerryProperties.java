package com.wechat.ferry.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 配置文件-WeChatFerry的配置文件
 *
 * @author chandler
 * @date 2024-09-21 21:35
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.ferry")
public class WeChatFerryProperties {

    /**
     * dll文件位置
     */
    private String dllPath;

    /**
     * socket端口
     */
    private Integer socketPort;

    /**
     * 联系人类型-官方杂号，禁止与其他分类重复(格式：代码|名称)
     * 使用时记得需要提取代码或者名称匹配
     */
    private List<String> contactsTypeMixed;

    /**
     * 联系人类型-公众号，禁止与其他分类重复(格式：代码|名称)
     * 使用时记得需要提取代码或者名称匹配
     */
    private List<String> contactsTypeOfficial;

    /**
     * 智谱清言AI-key
     */
    private String zhiPuAiKey;

    /**
     * 硅基流动AI-key
     */
    private String siliconFlowAiKey;

    /**
     * 管理员
     */
    private List<String> adminUsers;
    /**
     * 需要开启消息处理的群
     */
    private Set<String> openMsgGroups;

    /**
     * 接收消息回调开关
     */
    private Boolean receiveMsgCallbackSwitch = false;

    /**
     * 接收消息回调地址
     */
    private List<String> receiveMsgCallbackUrls;

    /**
     * 发送消息回调标识 1-关闭 2-全部回调 3-发送成功才回调
     */
    private String sendMsgCallbackFlag = "1";

    /**
     * 发送消息回调地址
     */
    private List<String> sendMsgCallbackUrls;

    /**
     * 调用第三方服务客户端成功状态码
     */
    private Map<String, String> thirdPartyOkCodes;

}
