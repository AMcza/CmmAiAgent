package org.example.cmmaiagent2.agent;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.cmmaiagent2.agent.model.AgentState;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public abstract class BaseAgent {
    //核心属性
    private String name;

    //提示
    private String systemPrompt;
    private String nextStepPrompt;
    //状态
    private AgentState state=AgentState.IDLE;

    //执行控制
    private int maxSteps=10;
    private int currentStep=0;

    //LLM
    private ChatClient chatClient;

    //memory
    private List<Message> messageList=new ArrayList<>();

    public String run(String userPrompt){
        if(this.state!=AgentState.IDLE){
            throw new RuntimeException("Cannot run agent from state:"+this.state);
        }
        if(StringUtils.isBlank(userPrompt)){
            throw new RuntimeException("Cannot run agent with empty prompt");
        }
        //更改状态
        state=AgentState.RUNNING;

        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));

        //保存结果列表
        List<String> results=new ArrayList<>();
        try{
            //执行
            for(int i=0;i<maxSteps && state!=AgentState.FINISHED;i++){
                int stepNumber=i+1;
                currentStep=stepNumber;
                log.info("Executing step "+stepNumber+"/"+maxSteps);
                //单步执行
                String stepResult=step();
                String result="Step "+stepNumber+" result: "+stepResult;
                results.add(result);
            }
            //检测是否超出步骤限制
            if(currentStep>=maxSteps){
                state=AgentState.FINISHED;
                results.add("Terminated:Reached max Step:("+maxSteps+")");
            }
            return String.join("\n",results);
        }catch (Exception e){
            state=AgentState.ERROR;
            log.error("Error executing agent",e);
            return "执行错误"+e.getMessage();
        }finally {
            this.cleanup();
        }
    }

    /**
     * 执行单个步骤(交给子类实现)
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){}


    public SseEmitter runStream(String userPrompt){
        //创建SseEmitter对象
        SseEmitter emitter=new SseEmitter(300000L);
        //创建线程异步处理
        CompletableFuture.runAsync(()->{
            try{
                if(this.state!= AgentState.IDLE){
                    emitter.send("错误:无法从状态运行代理:"+this.state);
                    emitter.complete();
                    return;
                }
                if(StringUtil.isBlank(userPrompt)){
                    emitter.send("错误:不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }
                //更改状态
                state=AgentState.RUNNING;
                //记录消息上下文
                messageList.add(new UserMessage(userPrompt));


                try{
                    for(int i=0;i<maxSteps && state!=AgentState.FINISHED;i++){
                        int stepNumber=i+1;
                        currentStep=stepNumber;
                        log.info("Executing step"+stepNumber+"/"+maxSteps);

                        //单步执行
                        String stepResult=step();
                        String result="Step "+stepNumber+" result: "+stepResult;

                        //发送每一步的结果
                        emitter.send(result);
                    }
                    //检测是否超出步骤现在
                    if(currentStep>=maxSteps){
                        state=AgentState.FINISHED;
                        emitter.send("执行结束:达到最大步骤:("+maxSteps+")");
                    }
                    //正常完成
                    emitter.complete();
                }catch (Exception e){
                    state=AgentState.ERROR;
                    log.error("执行智能体失败:",e);
                    try{
                        emitter.send("执行错误:"+e.getMessage());
                    }catch (Exception ex){
                        emitter.completeWithError(ex);
                    }
                }finally {
                    this.cleanup();
                }
            }catch (Exception e){
                emitter.completeWithError(e);
            }
        });

        //设置超时和完成回调
        emitter.onTimeout(()->{
            this.state=AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(()->{
            if(this.state==AgentState.RUNNING){
                this.state=AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }
}
