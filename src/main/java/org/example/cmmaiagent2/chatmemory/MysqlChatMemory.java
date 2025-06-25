package org.example.cmmaiagent2.chatmemory;

import cn.hutool.core.lang.Snowflake;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import java.util.List;
@Slf4j
public class MysqlChatMemory implements ChatMemory {

    @Override
    public void add(String conversationId, List<Message> messages) {

    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        return null;
    }

    @Override
    public void clear(String conversationId) {

    }
}
