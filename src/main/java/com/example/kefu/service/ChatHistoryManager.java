package com.example.kefu.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天历史管理器
 * 用于存储和管理用户的聊天历史记录
 */
@Service
public class ChatHistoryManager {

    /**
     * 聊天记录项
     */
    private static class ChatItem {
        private String question;
        private String answer;
        private LocalDateTime timestamp;

        public ChatItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
            this.timestamp = LocalDateTime.now();
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    // 使用ConcurrentHashMap存储聊天历史，key为tableId，value为该tableId下的聊天记录列表
    private final Map<String, List<ChatItem>> chatHistory = new ConcurrentHashMap<>();
    
    // 聊天记录过期时间（小时）
    private static final int EXPIRATION_HOURS = 24;

    /**
     * 添加聊天记录
     *
     * @param tableId  聊天窗口ID
     * @param question 用户问题
     * @param answer   系统回答
     */
    public void addChatRecord(String tableId, String question, String answer) {
        if (tableId == null || tableId.isEmpty()) {
            return;
        }
        
        chatHistory.computeIfAbsent(tableId, k -> new ArrayList<>())
                .add(new ChatItem(question, answer));
    }

    /**
     * 获取指定tableId的聊天历史
     *
     * @param tableId 聊天窗口ID
     * @return 聊天历史字符串，格式为"问题：xxx
回答：xxx

"
     */
    public String getChatHistory(String tableId) {
        if (tableId == null || tableId.isEmpty() || !chatHistory.containsKey(tableId)) {
            return "";
        }
        
        List<ChatItem> history = chatHistory.get(tableId);
        if (history.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("以下是之前的对话历史： ");
        
        for (ChatItem item : history) {
            sb.append("问题：").append(item.getQuestion()).append("/n");
            sb.append("回答：").append(item.getAnswer()).append("/n");
        }
        
        return sb.toString();
    }

    /**
     * 清除指定tableId的聊天历史
     *
     * @param tableId 聊天窗口ID
     */
    public void clearChatHistory(String tableId) {
        if (tableId != null && !tableId.isEmpty()) {
            chatHistory.remove(tableId);
        }
    }

    /**
     * 定时清理过期的聊天记录
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredRecords() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(EXPIRATION_HOURS);
        
        chatHistory.forEach((tableId, records) -> {
            records.removeIf(item -> item.getTimestamp().isBefore(expirationTime));
            if (records.isEmpty()) {
                chatHistory.remove(tableId);
            }
        });
    }
}
