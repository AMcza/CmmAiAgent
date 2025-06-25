package org.example.cmmaiagent2.rag.method1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 加载本地md文件
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver){
        this.resourcePatternResolver=resourcePatternResolver;
    }

    /**
     * 适当添加元信息
     * @return
     */
    public List<Document> loadMarkdowns(){
        List<Document> allDocuments=new ArrayList<>();
        try{
            Resource[] resources= resourcePatternResolver.getResources("classpath:document/*.md");
            for(Resource resource:resources){
                String fileName=resource.getFilename();
                //提取文档标签
                String status=fileName.substring(fileName.length()-6,fileName.length()-4);
                MarkdownDocumentReaderConfig config= MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename",fileName)
                        .withAdditionalMetadata("status",status)
                        .build();
                MarkdownDocumentReader reader=new MarkdownDocumentReader(resource,config);
                allDocuments.addAll(reader.get());
            }
        }catch (IOException e){
            log.error("Markdown文档加载失败",e);
        }
        return allDocuments;
    }
}
