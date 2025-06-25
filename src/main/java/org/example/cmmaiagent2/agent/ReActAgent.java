package org.example.cmmaiagent2.agent;

public abstract class ReActAgent extends BaseAgent{

    /**
     * 处理当前状态并觉得下一步行动
     * @return
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     * @return
     */
    public abstract String act();

    /**
     * 执行单个步骤:思考和行动
     * @return
     */
    @Override
    public String step() {
        try{
            boolean shouldAct =think();
            if(!shouldAct){
                return "思考完成-无需行动";
            }
            return act();
        }catch (Exception e){
            //记录异常日志
            e.printStackTrace();
            return "步骤执行失败:"+e.getMessage();
        }
    }
}
