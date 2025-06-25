package org.example.cmmaiagent2.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.example.cmmaiagent2.agent.model.AgentState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {
    //可用的工具
    private final ToolCallback[] availableTools;

    //保存了工具调用的信息响应
    private ChatResponse toolCallChatResponse;


    //工具调用管理者
    private final ToolCallingManager toolCallingManager;

    //禁用内置的工具调用机制
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools=availableTools;
        this.toolCallingManager=ToolCallingManager.builder().build();
        //禁用SpringAi的工具调用
        this.chatOptions= DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    @Override
    public boolean think() {
        if(getNextStepPrompt()!=null && getNextStepPrompt().isEmpty()){
            UserMessage userMessage=new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList=getMessageList();
        Prompt prompt=new Prompt(messageList,chatOptions);
        try{
            //获取工具选项的响应
            ChatResponse chatResponse=getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            //记录响应,用于Act
            this.toolCallChatResponse=chatResponse;
            /////自动助手信息到上下文中
            AssistantMessage assistantMessage=chatResponse.getResult().getOutput();
            //输出提示信息
            String result=assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList=assistantMessage.getToolCalls();
            log.info(getName()+"的思考"+result);
            log.info(getName()+"的工具调用"+toolCallList.size()+"个工具来使用");
            String toolCallInfo=toolCallList.stream()
                    .map(toolCall->String.format("工具名称:%s,参数：%s",toolCall.name(),toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if(toolCallList.isEmpty()){
                //只有不调用工具时,才记录助手信息
                getMessageList().add(assistantMessage);
                return false;
            }else{
                //需要迪奥哟工具时,无需记录助手信息
                return true;
            }
        }catch (Exception e){
            log.error(getName()+"思考过程遇到了问题:"+e.getMessage());
            getMessageList().add(
                    new AssistantMessage("思考过程遇到了问题:"+e.getMessage())
            );
            return false;
        }
    }

    @Override
    public String act() {
        if(!toolCallChatResponse.hasToolCalls()){
            return "没有工具调用";
        }
        //调用工具
        Prompt prompt=new Prompt(getMessageList(),chatOptions);
        ToolExecutionResult toolExecutionResult=toolCallingManager.executeToolCalls(prompt,toolCallChatResponse);
        //记录消息上下文,conversationHistory
        setMessageList(toolExecutionResult.conversationHistory());
        //返回工具调用的结果
        ToolResponseMessage toolResponseMessage=(ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results=toolResponseMessage.getResponses().stream()
                .map(response->"工具"+response.name()+"完成了它的任务!结果:"+response.responseData())
                .collect(Collectors.joining("\n"));

        //判断是否调用了终止工具
        boolean terminateToolCalled=toolResponseMessage.getResponses().stream()
                        .anyMatch(response->"doTerminate".equals(response.name()));
        if(terminateToolCalled){
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;
    }
}
