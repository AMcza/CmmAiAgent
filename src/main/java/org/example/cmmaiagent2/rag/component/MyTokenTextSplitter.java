package org.example.cmmaiagent2.rag.component;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义基于token的切词器
 */
@Component
public class MyTokenTextSplitter {

    public List<Document> splitDocuments(List<Document> documents){
        TokenTextSplitter splitter=new TokenTextSplitter();
        return splitter.apply(documents);
    }

    /**
     * tokenTextSplitter的参数:1.每个文本块目标大小 2.每个文本块的最小大小 3.包含的chunk的最小长度 4.是否在块中保留分隔符
     * @param documents
     * @return
     */
    public List<Document> splitCostomized(List<Document> documents){
        TokenTextSplitter splitter=new TokenTextSplitter(200,100,10,5000,true);
        return splitter.apply(documents);
    }
}
