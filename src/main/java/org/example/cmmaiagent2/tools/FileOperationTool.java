package org.example.cmmaiagent2.tools;

import cn.hutool.core.io.FileUtil;
import org.example.cmmaiagent2.constant.FileContent;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作
 */
public class FileOperationTool {

    private final String FILE_DIR= FileContent.FILE_SAVE_DIR+"/file";
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read")String fileName){
        String filePath=FILE_DIR+"/"+fileName;
        try{
            return FileUtil.readUtf8String(filePath);
        }catch (Exception e){
            return "Error reading file:"+e.getMessage();
        }
    }
    @Tool(description= "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write")String fileName,
                            @ToolParam(description = "Content to write to the file")String content){
        String filePath=FILE_DIR+"/"+fileName;
        try{
            //创建目录
            FileUtil.mkdir(filePath);
            FileUtil.writeUtf8String(content,filePath);
            return "File written successfully to"+filePath;
        }catch (Exception e){
            return "Error writing file:"+e.getMessage();
        }
    }
}
