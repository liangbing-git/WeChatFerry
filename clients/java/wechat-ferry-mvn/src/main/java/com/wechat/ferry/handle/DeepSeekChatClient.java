package com.wechat.ferry.handle;

import com.wechat.ferry.entity.dto.WxPpMsgDTO;
import com.wechat.ferry.entity.vo.request.WxPpWcfSendFileMsgReq;
import com.wechat.ferry.entity.vo.request.WxPpWcfSendTextMsgReq;
import com.wechat.ferry.service.WeChatDllService;
import com.wechat.ferry.service.WechatContent;
import com.wechat.ferry.utils.ThreadPoolUtils;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.core.response.HttpxBinaryResponseContent;
import com.zhipu.oapi.service.v4.api.VideosClientApiService;
import com.zhipu.oapi.service.v4.file.FileApiResponse;
import com.zhipu.oapi.service.v4.file.UploadFileRequest;
import com.zhipu.oapi.service.v4.image.CreateImageRequest;
import com.zhipu.oapi.service.v4.image.ImageApiResponse;
import com.zhipu.oapi.service.v4.model.*;
import com.zhipu.oapi.service.v4.videos.VideoCreateParams;
import com.zhipu.oapi.service.v4.videos.VideoObject;
import com.zhipu.oapi.service.v4.videos.VideosResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DeepSeekChatClient {

    public List<ChatMessage> messageQueue = new ArrayList<>();

    public final String apiKey;

    public static final RestTemplate restTemplate = new RestTemplate();

    public DeepSeekChatClient(String apiKey) {
        this.apiKey = apiKey;
        messageQueue.add(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                                         "作为一名热心的群助手，以干练的风格解决群友的问题，回答格式采用日常交流的白话文方式," + "格式要得体，可以带一些小表情"));
    }

    public void sendMessage(String model, String message) {

        // 构建请求 URL
        final String url = "https://api.siliconflow.cn/v1/chat/completions";

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+apiKey);
        headers.add("Content-Type", "application/json");

        // 构建请求体
        String requestBody = String.format("{\"model\": \"%s\",\"messages\": [{\"role\": \"user\",\"content\": \"%s\"}],\"stream\": false,\"max_tokens\": 512,\"stop\": [\"null\"],\"temperature\": 0.7,\"top_p\": 0.7,\"top_k\": 50,\"frequency_penalty\": 0.5,\"n\": 1,\"response_format\": {\"type\": \"text\"}}", model, message);

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 执行请求
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            System.out.println("Response Status Code: " + responseEntity.getStatusCode());
            System.out.println("Response Body: " + responseEntity.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
