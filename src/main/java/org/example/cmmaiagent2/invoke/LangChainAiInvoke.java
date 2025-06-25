package org.example.cmmaiagent2.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Value;

public class LangChainAiInvoke {

    private String aikey;
    public static void main(String[] args) {
        ChatLanguageModel qwenModel= QwenChatModel.builder()
                .apiKey("sk-7572d2f22b36416fa908ac7d4bb5f14b")
                .modelName("qwen-max")
                .build();
        String answer=qwenModel.chat("你好，请回复hello world");
        System.out.println(answer);
    }
}
