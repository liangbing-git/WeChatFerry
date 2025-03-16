package com.wechat.ferry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * 配置类-protobuf
 *
 * @author chandler
 * @date 2024-09-26 21:35
 */
@Configuration
public class ProtobufConfig {

    /**
     * protobuf 序列化
     */
    @Bean
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    /**
     * protobuf 反序列化
     */
    @Bean
    RestTemplate restTemplate(ProtobufHttpMessageConverter protobufHttpMessageConverter) {
        return new RestTemplate(Collections.singletonList(protobufHttpMessageConverter));
    }

    @Bean("jsonRestTemplate")
    RestTemplate jsonRestTemplate() {
        return new RestTemplate();
    }

}
