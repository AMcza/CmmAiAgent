package org.example.cmmaiagent2.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import java.io.IOException;

/**
 * 网页抓取工具
 */
public class WebScrapingTool {
    @Tool(description = "Scrape information from a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try{
            Document doc=Jsoup.connect(url).get();
            return doc.html();
        }catch (IOException o){
            return "Error: "+o.getMessage();
        }
    }
}
