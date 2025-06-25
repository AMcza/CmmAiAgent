package org.example.cmmaiagent2.controller;

import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.example.cmmaiagent2.agent.CmmManus;
import org.example.cmmaiagent2.app.LoveApp;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(@Param("message") String message){
        CmmManus cmmManus=new CmmManus(allTools,dashscopeChatModel);
        return cmmManus.runStream(message);
    }

    /**
     * 同步接口
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(@Param("message")String message,@Param("chatId")String chatId){
        return loveApp.doChat(message,chatId);
    }

    /**
     * SSE接口
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(@Param("message")String message,@Param("chatId")String chatId){
        return loveApp.doChatByStream(message,chatId);
    }


    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(@Param("message") String message, @Param("chatId") String chatId){
        //创建一个超时时间较长的SseEmitter
        SseEmitter emitter = new SseEmitter(180000L);//3分钟
        //获取Flux数据流并直接订阅
        loveApp.doChatByStream(message,chatId)
                .subscribe(
                        //处理每条消息
                        chunk->{
                            try{
                                emitter.send(chunk);
                            }catch (IOException e){
                                emitter.completeWithError(e);
                            }
                        },
                        //处理错误
                        emitter::completeWithError,
                        //处理完成
                        emitter::complete
                );
        return emitter;
    }
}
