package com.wechat.ferry.handle;

import com.wechat.ferry.entity.dto.WxPpMsgDTO;
import com.wechat.ferry.entity.vo.request.WxPpWcfSendFileMsgReq;
import com.wechat.ferry.entity.vo.request.WxPpWcfSendImageMsgReq;
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
import org.springframework.util.CollectionUtils;

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
public class ZhiPuAiChatClient {

    public List<ChatMessage> messageQueue = new ArrayList<>();

    public static final ThreadPoolExecutor EXECUTORS = ThreadPoolUtils.createThreadPool("ZhiPuAiChatClient", 5, 10, 100,
                                                                                        60);

    private final ClientV4 client;

    public ZhiPuAiChatClient(String apiSecretKey) {
        messageQueue.add(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                                         "作为一名热心的群助手，以干练的风格解决群友的问题，回答格式采用日常交流的白话文方式," + "格式要得体，可以带一些小表情"));
        client = new ClientV4.Builder(apiSecretKey).networkConfig(60, 60, 60, 60, TimeUnit.SECONDS)
                                                   .build();
    }

    public String drawOnly(String content) {
        try {
            CreateImageRequest imageRequest = CreateImageRequest.builder().model("CogView-3-Flash").prompt(content)
                                                                .build();
            ImageApiResponse imageApiResponse = client.createImage(imageRequest);
            if (imageApiResponse.getData() != null && !CollectionUtils.isEmpty(imageApiResponse.getData().getData())) {
                String url = imageApiResponse.getData().getData().get(0).getUrl();
                log.info("生成url:{}", url);
//                return url;
                String imgName = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                String pathname = "file/" + imgName + ".png";
                File destFile = new File(pathname); // 指定保存图像的本地路径
                Files.copy(new URL(url).openStream(), destFile.toPath());
                return destFile.getAbsolutePath();
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return "";
    }

    public String chatOnly(String content) {
        ChatMessage userMsg = new ChatMessage(ChatMessageRole.USER.value(), content);
        String model = "glm-4-flash";

        messageQueue.add(userMsg);
        // [系统消息,[user,assist],user,assist,user,assist,user,assist]
        // 超过6条，删除前两条
        if (messageQueue.size() > 10) {
            messageQueue.remove(1);
            messageQueue.remove(1);
        }
        ChatCompletionRequest request = ChatCompletionRequest.builder().model(model).stream(Boolean.FALSE).invokeMethod(
                Constants.invokeMethod).messages(messageQueue).tools(getChatTools()).build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(request);
        for (Choice choice : invokeModelApiResp.getData().getChoices()) {
            ChatMessage aiMsg = choice.getMessage();
            messageQueue.add(aiMsg);
            return aiMsg.getContent().toString();
        }
        return "";
    }

    private static @NotNull List<ChatTool> getChatTools() {
        List<ChatTool> toolList = new ArrayList<>();
        ChatTool tool = new ChatTool();
        tool.setType("web_search");
        WebSearch webSearch = new WebSearch();
        webSearch.setEnable(true);
        tool.setWeb_search(webSearch);
        toolList.add(tool);
        return toolList;
    }

    public void videoOnly(WxPpMsgDTO dto, WeChatDllService weChatDllService) {
        EXECUTORS.execute(() -> {
            try {
                WxPpWcfSendTextMsgReq textMsgReq = new WxPpWcfSendTextMsgReq();
                textMsgReq.setMsgText("开始生成视频啦！请稍等一会！");
                textMsgReq.setRecipient(dto.getRoomId());
                weChatDllService.sendTextMsg(textMsgReq);
                String content = dto.getContent();
                VideosClientApiService videosClientApiService = new VideosClientApiService(
                        client.getConfig().getHttpClient(), Constants.BASE_URL);
                VideoCreateParams builder = VideoCreateParams.builder().model("CogVideoX-Flash").prompt(content)
                                                             .build();
                VideosClientApiService.VideoGenerationChain videoGenerationChain =
                        videosClientApiService.videoGenerations(builder);
                VideosResponse videosResponse = videoGenerationChain.apply(client);
                VideoObject data = videosResponse.getData();
                String id = data.getId();
                while ("PROCESSING".equals(data.getTaskStatus())) {
                    VideoCreateParams queryBuilder = VideoCreateParams.builder().id(id).build();
                    VideosClientApiService.VideoGenerationChain generationsResult =
                            videosClientApiService.videoGenerationsResult(queryBuilder);
                    videosResponse = generationsResult.apply(client);
                    data = videosResponse.getData();
                    TimeUnit.SECONDS.sleep(5);
                }
                if (data.getTaskStatus().equals("SUCCESS")) {
                    // SUCCESS
                    String url = data.getVideoResult().get(0).getUrl();
                    String imgName = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                    String pathname = "file/" + imgName + ".mp4";
                    File destFile = new File(pathname);
                    Files.copy(new URL(url).openStream(), destFile.toPath());
                    // 发送视频
                    WxPpWcfSendFileMsgReq fileMsgReq = new WxPpWcfSendFileMsgReq();
                    fileMsgReq.setRecipient(dto.getRoomId());
                    fileMsgReq.setResourcePath(pathname);
                    weChatDllService.sendFileMsg(fileMsgReq);
                } else {
                    textMsgReq = new WxPpWcfSendTextMsgReq();
                    textMsgReq.setMsgText("视频生成失败了！");
                    textMsgReq.setRecipient(dto.getRoomId());
                    weChatDllService.sendTextMsg(textMsgReq);
                }
            } catch (Exception e) {
                log.error("error", e);
            }
        });
    }
}
