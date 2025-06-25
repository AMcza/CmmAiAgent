package org.example.cmmaiagent2.rag.method1;

import jakarta.annotation.Resource;
import org.example.cmmaiagent2.rag.component.MyKeywordEnricher;
import org.example.cmmaiagent2.rag.component.MyTokenTextSplitter;
import org.example.cmmaiagent2.rag.method1.LoveAppDocumentLoader;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 加载数据到向量存储数据库
 */
@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel){
        SimpleVectorStore simpleVectorStore=SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        //加载文档
        List<Document> documents=loveAppDocumentLoader.loadMarkdowns();

        //自主切分
        List<Document> splitDocuments=myTokenTextSplitter.splitCostomized(documents);
        simpleVectorStore.add(splitDocuments);

        //自动补充关键词元信息
//        List<Document> enrichedDocuments=myKeywordEnricher.enrichDocuments(splitDocuments);
//        simpleVectorStore.add(enrichedDocuments);


        return simpleVectorStore;
    }
}
