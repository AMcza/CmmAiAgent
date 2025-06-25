package org.example.cmmaiagent2.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


public class SpringAiInvoker implements CommandLineRunner {

    @Resource
    private ChatModel chatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage output=chatModel.call(new Prompt("你好，请回复hello world"))
                .getResult()
                .getOutput();

        System.out.println(output.getText());
    }
}
