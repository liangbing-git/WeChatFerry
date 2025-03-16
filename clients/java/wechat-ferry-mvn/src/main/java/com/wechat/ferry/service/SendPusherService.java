package com.wechat.ferry.service;


import com.google.gson.reflect.TypeToken;
import com.wechat.ferry.utils.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Deprecated
public class SendPusherService {

    private final String token = "1099062139";

    @Autowired
    private RestTemplate restTemplate;


    /**
     * curl --location 'http://localhost:3001/webhook/msg/v2?token=[YOUR_PERSONAL_TOKEN]' \
     * --header 'Content-Type: application/json' \
     * --data '{
     * "to": "testUser",
     * "data": { "content": "你好👋" }
     * }'
     */
    public void sendMsg(String name, String content) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("to", name);
        HashMap<String, String> data = new HashMap<>();
        payload.put("data", data);
        data.put("content", content);
        sendWxMsg(name, payload);
    }

    /**
     * curl --location 'http://localhost:3001/webhook/msg/v2?token=[YOUR_PERSONAL_TOKEN]' \
     * --header 'Content-Type: application/json' \
     * --data '{
     * "to": "testGroup",
     * "isRoom": true,
     * "data": { "type": "fileUrl" , "content": "https://download.samplelib.com/jpeg/sample-clouds-400x300.jpg" },
     * }'
     */
    public void sendGroup(String name, String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", name);
        payload.put("isRoom", true);
        HashMap<String, String> data = new HashMap<>();
        payload.put("data", data);
        data.put("content", content);
        data.put("type", "text");
        sendWxMsg(name, payload);
    }

    private void sendWxMsg(String name, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(JsonTools.toJson(payload), headers);
        ResponseEntity<String> response = restTemplate.exchange("http://wxBotWebhook:3001/webhook/msg/v2?token=1099062139", HttpMethod.POST, entity, String.class);
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = JsonTools.fromJson(response.getBody(), mapType);
        log.info("发送给{}消息 响应 {}", name, map);
    }


    public boolean needReLogin() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("http://wxBotWebhook:3001/healthz?token=1099062139", String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "unHealthy".equals(response.getBody());
            }
        } catch (Exception e) {
            log.error("检查失败 {}", e.getMessage());
            return false;
        }
        return false;
    }

    public String getLoginUrl() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("http://wxBotWebhook:3001/login?token=1099062139", String.class);
            MediaType contentType = response.getHeaders().getContentType();
            if (MediaType.APPLICATION_JSON.equals(contentType)) {
                return response.getBody();
            } else if (MediaType.TEXT_HTML.equals(contentType)) {
                return response.getBody();
            } else {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("检查失败", e);
            return e.getMessage();
        }
    }

    public void sendToWxPusher() {
        try {
            // 目标URL
            String url = "http://wxpusher.zjiecode.com/api/send/message";
            // 要发送的数据
            Map<String, Object> data = new HashMap<>();
            data.put("appToken", "AT_4ZJFpKjkHzLpeEFiZGXn1drit8LOAS8S");
            data.put("content", getLoginUrl());
            data.put("summary", "微信掉了，请重新登陆");
            data.put("contentType", 2);
            data.put("uids", new String[]{"UID_7XybaMkzw83Ouod9IqDGHBOcRqzn","UID_dtoUZ5eRLD3lfuDqFizGa381EpLk"});
//            data.put("url", loginUrl);
            data.put("verifyPay", "false");
            data.put("verifyPayType", 0);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 创建请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(data, headers);

            // 发送POST请求
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            log.info("发送完成 {}", response.getBody());
        } catch (RestClientException e) {
            log.error("发送pusher失败", e);
        }
    }

}
