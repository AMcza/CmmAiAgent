package org.example.cmmaiagent2;

import jakarta.annotation.Resource;
import org.example.cmmaiagent2.agent.CmmManus;
import org.example.cmmaiagent2.app.LoveApp;
import org.example.cmmaiagent2.rag.component.MultiQueryExpanderDemo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
class CmmAiAgent2ApplicationTests {
    @Resource
    private LoveApp loveApp;
    @Resource
    VectorStore pgVectorVectorStore;


    @Test
    void test(){
        List<Document> documents=List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2"))
        );

        //添加文档
        pgVectorVectorStore.add(documents);
        //相似度查询
        List<Document> result=pgVectorVectorStore.similaritySearch(SearchRequest
                .builder()
                .query("鳞癌")
                .topK(5)
                .build());
        Assertions.assertNotNull(result);
    }

    @Test
    void doChatWithReport(){
        String chatId= UUID.randomUUID().toString();
        //第一轮
        String message="你好，我是一名原神玩家，我想让另一半更爱我,白蚁";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }
    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithQueryRewriter() {
        String chatId = UUID.randomUUID().toString();
        String message = "我想谈恋爱";
        String answer =  loveApp.doChatWithQueryRewriter(message, chatId);
        Assertions.assertNotNull(answer);
    }
    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;
    @Test
    void expand(){
        List<Query> queries=multiQueryExpanderDemo.expand("我想谈恋爱");
        Assertions.assertNotNull(queries);
    }


    @Test
    void doChatWithMcp(){
        //测试图片搜索MCP
        String message="我想找一张书法图片";
        String chatId= UUID.randomUUID().toString();
        String answer=loveApp.doChatWithMcp(message,chatId);
        Assertions.assertNotNull(answer);
    }

    @Resource
    private CmmManus cmmManus;
    @Test
    void run(){
        String userPrompt= """
                    我是一名原神玩家，我最近找到一个女朋友，我准备和我女朋友这周前去约会,帮我指定一份约会计划,并以pdf输出
                """;
        String answer=cmmManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }

}
