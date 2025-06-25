package org.example.cmmaiagent2.rag.factory;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

public class LoveAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance(){
        PromptTemplate emptyContextPromptTemplate =new PromptTemplate(
                """
                    你应该输出下面的内容:
                    抱歉,我只能回答恋爱相关的问题，别的没办法帮到您哦。
                """
        );
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
