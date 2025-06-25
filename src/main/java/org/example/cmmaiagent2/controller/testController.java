package org.example.cmmaiagent2.controller;

import jakarta.annotation.Resource;
import org.example.cmmaiagent2.app.LoveApp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class testController {


    @Resource
    LoveApp loveApp;

    @GetMapping("/test")
    public String test(){
        String chatId= UUID.randomUUID().toString();
        //第一轮
        String message="你好，我是一名原神玩家，我想让另一半更爱我,白蚁";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        return loveReport.toString();
    }
}
