package org.example.cmmaiagent2.rag.component;

import jakarta.annotation.Resource;
import org.example.cmmaiagent2.rag.method1.LoveAppDocumentLoader;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyKeywordEnricher {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 基于Ai生成元信息,自动解析关键词添加到元信息
     * @param documents
     * @return
     */
    public List<Document> enrichDocuments(List<Document> documents){
        KeywordMetadataEnricher enricher=new KeywordMetadataEnricher(this.dashscopeChatModel,5);
        return enricher.apply(documents);
    }

}
