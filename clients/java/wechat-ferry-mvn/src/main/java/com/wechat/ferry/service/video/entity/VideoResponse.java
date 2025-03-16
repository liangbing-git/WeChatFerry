package com.wechat.ferry.service.video.entity;

import lombok.Data;

import java.util.List;

@Data
public class VideoResponse {
    private List<VideoResponseItem> list;
    private Boolean us;
    private String msg;
    private Boolean cache;

}
