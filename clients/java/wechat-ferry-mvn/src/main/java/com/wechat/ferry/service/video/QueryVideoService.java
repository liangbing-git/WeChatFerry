package com.wechat.ferry.service.video;

import com.wechat.ferry.service.video.entity.TokenResponse;
import com.wechat.ferry.service.video.entity.VideoResponse;
import com.wechat.ferry.service.video.entity.VideoResponseItem;
import com.wechat.ferry.utils.JsonTools;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class QueryVideoService {


    @Autowired
    @Qualifier("jsonRestTemplate")
    private RestTemplate restTemplate;


    public void setTestBean(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public String query(String keyword, String panType) {
        try {
            // 设置请求的URL
            String url = "http://qq.kkkob.com/v/api/getToken";

            // 创建HttpHeaders对象，设置请求头
            HttpHeaders headers = getTokenHeaders();

            // 创建HttpEntity对象，包含请求头和请求体（如果有）
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发送GET请求
            ResponseEntity<String> response = restTemplate.exchange(url,
                                                                           HttpMethod.GET,
                                                                           entity,
                                                                           String.class);
            TokenResponse tokenResponse = JsonTools.fromJson(response.getBody(), TokenResponse.class);
            if (tokenResponse == null || StringUtils.isBlank(tokenResponse.getToken())) {
                return "token获取失败";
            }
            String token = tokenResponse.getToken();
            List<String> list = Arrays.asList("http://qq.kkkob.com/v/api/search",
                                              "http://qq.kkkob.com/v/api/getDJ",
                                              "http://qq.kkkob.com/v/api/getJuzi",
                                              "http://qq.kkkob.com/v/api/getXiaoyu",
                                              "http://qq.kkkob.com/v/api/getSearchX");
            StringBuilder result = new StringBuilder();
            for (String apiUrl : list) {
                result.append(queryApi(token, keyword, apiUrl, panType));
            }
            if(result.isEmpty()){
                return "没有找到相关内容~";
            }
            return result.toString();
        } catch (RestClientException e) {
            log.error("异常了", e);
            return "报错咯~";
        }
    }


    private String queryApi(String token, String keyword, String url, String panType) {
        // 创建HttpHeaders对象，设置请求头
        HttpHeaders headers = getApiHeaders();

        // 创建表单数据
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("name", keyword);
        map.add("token", token); // 这里使用的是示例token，实际使用时应该替换为第一个请求获取到的token

        // 创建HttpEntity对象，包含请求头和请求体
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        // 发送POST请求
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,
                                                                requestEntity,
                                                                String.class);

        // 打印响应体
        log.info("response is {}", response.getBody());
        if (StringUtils.isBlank(response.getBody())) {
            return "";
        }
        VideoResponse videoResponse = JsonTools.fromJson(response.getBody(), VideoResponse.class);
        return findBaiduResult(videoResponse,panType);
    }

    private static String findBaiduResult(VideoResponse videoResponse, String panType) {
        List<VideoResponseItem> listInfo = videoResponse.getList();
        StringBuilder sb = new StringBuilder();
        for (VideoResponseItem videoResponseItem : listInfo) {
            String title = videoResponseItem.getQuestion();
            String answer = videoResponseItem.getAnswer();
            String[] linkArr = answer.replace("：", ":").replace("\n", "").split("链接:");
            for (String link : linkArr) {
                if (StringUtils.isNotBlank(link.trim()) && link.contains(panType)) {
//                    if (sb.isEmpty()) {
//                    }
                    sb.append(title.trim()).append("\n");
                    sb.append(link.trim()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    private static @NotNull HttpHeaders getTokenHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.setCacheControl("no-cache");
        headers.add("Connection", "keep-alive");
        headers.add("Pragma", "no-cache");
        headers.set("Referer", "http://qq.kkkob.com/apps/index.html?id=");
        headers.add("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131" +
                            ".0.0" + ".0 Safari/537.36 Edg/131.0" + ".0.0");
        headers.add("X-Requested-With", "XMLHttpRequest");
        return headers;
    }


    private static @NotNull HttpHeaders getApiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.setCacheControl("no-cache");
        headers.add("Connection", "keep-alive");
        headers.setContentType(new MediaType("application", "x-www-form-urlencoded", StandardCharsets.UTF_8));
        headers.set("Origin", "http://qq.kkkob.com");
        headers.set("Pragma", "no-cache");
        headers.set("Referer",
                    "http://qq.kkkob.com/apps/index.html?name=%E8%9C%80%E9%94%A6%E4%BA%BA%E5%AE%B6&token=bfh3gjat6ro");
        headers.add("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0" + ".0 Safari/537.36 Edg/131.0" + ".0.0");
        headers.add("X-Requested-With", "XMLHttpRequest");
        return headers;
    }


    public static void main(String[] args) {
        QueryVideoService queryVideoService = new QueryVideoService();
        queryVideoService.setTestBean(new RestTemplate());
        System.out.println(queryVideoService.query("蜘蛛侠", "xunlei"));
    }

}
