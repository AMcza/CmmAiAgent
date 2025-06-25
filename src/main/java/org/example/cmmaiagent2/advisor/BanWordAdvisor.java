package org.example.cmmaiagent2.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
public class BanWordAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {


    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        //请求文本
        String requestText = advisedRequest.userText();
        if(!isBanWord(requestText)){
            throw new RuntimeException("文本中包含敏感词");
        }
        return advisedRequest;
    }


    private boolean isBanWord(String requestText){
        String[] sensitiveWords = {
                "敏感词1", "阳光", "彩虹", "敏感词2", "梦想", "宇宙", "蝴蝶",
                "敏感词3", "星辰", "海洋", "风筝", "火山", "钻石", "月光",
                "沙漠", "钢琴", "银河", "枫叶", "瀑布", "孔雀", "水晶",
                "极光", "向日葵", "金字塔", "小提琴", "珊瑚", "银杏",
                "龙卷风", "红宝石", "夜莺", "橄榄枝", "紫水晶", "白鸽",
                "指南针", "黑天鹅", "绿松石", "红玫瑰", "蓝鲸", "黄鹂",
                "蒲公英", "红枫", "蓝宝石", "白天鹅", "黑珍珠", "绿洲",
                "紫罗兰", "白杨", "黑钻石", "绿翡翠", "紫藤", "白云",
                "黑洞", "绿地", "紫薇", "白雪", "黑夜", "绿灯", "紫霞",
                "白露", "黑马", "绿茶", "紫菜", "白酒", "黑板", "绿荫",
                "紫外线", "白糖", "黑土", "绿化", "紫外线", "白菜", "黑猫",
                "绿豆", "紫砂", "白兔", "黑熊", "绿草", "紫米", "白鹤",
                "黑莓", "绿灯", "紫檀", "白蚁", "黑麦", "绿茶", "紫苏",
                "白兰", "黑豆", "绿萝", "紫荆", "白果", "黑米", "绿竹"
        };

        String regex = "(?:^|[^\\p{L}])(" + String.join("|", sensitiveWords) + ")(?:$|[^\\p{L}])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(requestText);

        if (matcher.find()) {
            log.warn("文本中包含敏感词: {}", matcher.group(1));
            return false;
        } else {
            return true;
        }
    }
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 2;
    }


    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }
}
