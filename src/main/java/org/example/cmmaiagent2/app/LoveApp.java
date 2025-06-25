package org.example.cmmaiagent2.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.cmmaiagent2.advisor.BanWordAdvisor;
import org.example.cmmaiagent2.advisor.MyLoggerAdvisor;
import org.example.cmmaiagent2.agent.model.AgentState;
import org.example.cmmaiagent2.chatmemory.FileBasedChatMemory;
import org.example.cmmaiagent2.rag.component.QueryRewriter;
import org.example.cmmaiagent2.rag.factory.LoveAppRagCustomAdvisorFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
@Slf4j
@Component
public class LoveApp {

    private static final String SYSTEM_PROMPT="我正在开发【恋爱大师】AI 对话应用，请你帮我编写设置给 AI 大模型的系统预设 Prompt 指令。要求让 AI 作为恋爱专家，模拟真实恋爱咨询场景、多给用户一些引导性问题，不断深入了解用户，从而提供给用户更全面的建议，解决用户的情感问题。";

    private final ChatClient chatClient;

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    /**
     * 初始化客户端
     * @param chatModel
     */
    public LoveApp(ChatModel chatModel){
        //初始化基于内存的对话记忆
//        ChatMemory chatMemory=new InMemoryChatMemory();
        //自定义基于文件的对话记忆
        String fileDir=System.getProperty("user.dir")+"/chatmemory";
        ChatMemory chatMemory2=new FileBasedChatMemory(fileDir);
//        ChatMemory chatMemory3=new MysqlChatMemory();
        chatClient=ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory2),
//                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志Advisor
                        new MyLoggerAdvisor(),
                        //自定义re-readAdvisor
//                        new ReReadingAdvisor()
                        new BanWordAdvisor()
                )
                .build();

    }

    /**
     * 基础对话(支出多轮对话记忆)
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message,String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    /**
     * 快速定义类（java21）
     * @param title
     * @param suggestions
     */
    public record LoveReport(String title, List<String> suggestions){

    }


    /**
     * Ai 恋爱报告功能(支持结构化输出)
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message,String chatId){
        LoveReport loveReport=chatClient
                .prompt()
                .system(SYSTEM_PROMPT+"每次对话后都要生成恋爱结果,标题为{用户名}的恋爱报告,内容为建议列表")
                .user(message)
                .advisors(spec->spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}",loveReport);
        return loveReport;
    }

    @Resource
    private VectorStore pgVectorVectorStore;
    /**
     * RAG知识库增强回答
     * @param messages
     * @param chatId
     * @return
     */
    public String doChatWithRag(String messages,String chatId){
        ChatResponse chatResponse=chatClient
                .prompt()
                .user(messages)
                .advisors(spec->spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                .advisors(new MyLoggerAdvisor())
                //应用知识库rag
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                //云知识库服务rag
                //.advisors(loveAppRagCloudAdvisor)
                //基于pgvector存储向量数据
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();
        String content=chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    @Resource
    private QueryRewriter queryRewriter;

    public String doChatWithQueryRewriter(String messages,String chatId){
        //查询重写
        String rewrittenMessage=queryRewriter.doQueryRewrite(messages);
        ChatResponse chatResponse=chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
                        loveAppVectorStore,"已婚"
                ))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    /**
     * 调用工具
     */
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTool(String messages,String chatId){
        ChatResponse response=chatClient
                .prompt()
                .user(messages)
                .advisors(spec->spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content=response.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    /**
     * 调用MCP服务
     */
    private  ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String messages,String chatId){
        ChatResponse chatResponse=chatClient
                .prompt()
                .user(messages)
                .advisors(spec->spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content=chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }


    /**
     * 返回Flux响应式对象
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message,String chatId){
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec->spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                .stream()
                .content();
    }


}
