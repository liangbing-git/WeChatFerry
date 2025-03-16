package com.wechat.ferry.strategy.msg.receive.impl;

import com.wechat.ferry.config.WeChatFerryProperties;
import com.wechat.ferry.entity.dto.WxPpMsgDTO;
import com.wechat.ferry.enums.ReceiveMsgChannelEnum;
import com.wechat.ferry.service.BackMsgService;
import com.wechat.ferry.strategy.msg.receive.ReceiveMsgStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 策略实现类-接收消息-签到处理
 *
 * @author chandler
 * @date 2024-12-25 14:19
 */
@Slf4j
@Component
public class AdminMsgStrategyImpl implements ReceiveMsgStrategy {

    @Resource
    private WeChatFerryProperties weChatFerryProperties;

    @Resource
    private BackMsgService backMsgService;

    @Override
    public String getStrategyType() {
        return ReceiveMsgChannelEnum.ADMIN_MSG.getCode();
    }

    @Override
    public String doHandle(WxPpMsgDTO dto) {
        if (dto.getContent().contains("接入AI")) {
            String roomId = dto.getRoomId();
            weChatFerryProperties.getOpenMsgGroups().add(roomId);
            // 将roomId补充到yml中
            updateApplicationYml(roomId);
            backMsgService.sendTxtInfo(roomId, "已接入AI");
            return "end";
        }
        return "";
    }

    private void updateApplicationYml(String roomId) {
        String currentDir = System.getProperty("user.dir");
        String ymlFilePath = currentDir + File.separator + "application.yml";
        Path path = Paths.get(ymlFilePath);
        Map<String, Object> obj;
        // 创建DumperOptions并配置
        DumperOptions options = new DumperOptions();
        options.setIndent(2); // 设置缩进为2空格
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // 使用块样式
        Yaml yaml = new Yaml(options);
        try {
            try (InputStream in = Files.newInputStream(path)) {
                obj = yaml.load(in);
            }

            if (obj != null) {
                Map<String, Object> wechat = (Map<String, Object>) obj.get("wechat");
                if (wechat != null) {
                    Map<String, Object> ferry = (Map<String, Object>) wechat.get("ferry");
                    if (ferry != null) {
                        List<String> openMsgGroups = (List<String>) ferry.get("open-msg-groups");
                        if (openMsgGroups == null) {
                            openMsgGroups = new ArrayList<>();
                            ferry.put("open-msg-groups", openMsgGroups);
                        }
                        openMsgGroups.add(roomId);
                    }
                }
            }

            // 写回文件
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                yaml.dump(obj, writer);
            }

        } catch (IOException e) {
            log.error("更新application.yml失败", e);
        }
    }

}
