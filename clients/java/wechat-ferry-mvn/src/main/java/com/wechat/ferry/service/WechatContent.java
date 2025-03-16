package com.wechat.ferry.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WechatContent {

    public static final Map<String, String> FILE_CONTENT = new ConcurrentHashMap<>();

    public static final List<String> GROUP_NAME = Arrays.asList("卖火柴", "每日吃瓜", "机器人测试群");

}
