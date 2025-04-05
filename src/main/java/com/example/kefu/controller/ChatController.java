package com.example.kefu.controller;

import com.example.kefu.annotation.ApiLog;
import com.example.kefu.model.request.ChatRequest;
import com.example.kefu.model.EmbeddingRequest;
import com.example.kefu.model.EmbeddingResponse;
import com.example.kefu.model.request.VectorChatRequest;
import com.example.kefu.model.response.ApiResponse;
import com.example.kefu.model.response.ChatResponse;
import com.example.kefu.service.AliEmbeddingService;
import com.example.kefu.service.ElasticsearchService;
import com.example.kefu.service.QwenService;
import com.example.kefu.service.ChatHistoryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private QwenService qwenService;
    
    @Autowired
    private AliEmbeddingService aliEmbeddingService;
    
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ChatHistoryManager chatHistoryManager;

    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@RequestBody String question) {
        // 由于这是简单的聊天接口，没有tableId，所以使用固定的tableId
        String tableId = "default";
        
        // 获取聊天历史
        String chatHistory = chatHistoryManager.getChatHistory(tableId);
        
        // 构建提示词
        StringBuilder prompt = new StringBuilder();
        if (!chatHistory.isEmpty()) {
            prompt.append(chatHistory);
        }
        prompt.append(question);
        
        // 调用大模型获取回答
        String answer = qwenService.chat(prompt.toString());
        
        // 保存聊天记录
        chatHistoryManager.addChatRecord(tableId, question, answer);
        
        return ApiResponse.success(ChatResponse.of(answer));
    }

    /**
     * 回答用户问题
     *
     * @param request 问题请求，包含用户ID、店铺名称、产品名称和问题
     * @return 回答内容
     */
    @PostMapping("/answer")
    public ApiResponse<ChatResponse> answer(@RequestBody ChatRequest request) {
        try {
            // 1. 获取请求参数
            Integer userId = request.getUserId();
            String shopName = request.getShopName();
            String productName = request.getProductName();
            String question = request.getQuestion();
            String tableId = request.getTableId();
            
            // 如果tableId为空，则使用userId作为tableId
            if (tableId == null || tableId.trim().isEmpty()) {
                tableId = userId.toString();
            }

            // 参数校验
            if (userId == null || question == null || question.trim().isEmpty()) {
                return ApiResponse.error(400, "请提供有效的用户ID和问题");
            }

            // 2. 将问题转换为向量
            EmbeddingRequest embeddingRequest = new EmbeddingRequest();
            embeddingRequest.setInput(question);
            EmbeddingResponse embeddingResponse = aliEmbeddingService.getEmbedding(embeddingRequest);
            
            // 获取向量
            List<Float> embeddingList = embeddingResponse.getData().get(0).getEmbedding();
            float[] vector = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                vector[i] = embeddingList.get(i);
            }

            // 3. 执行向量搜索，指定返回字段（排除向量字段）
            List<String> includeFields = Arrays.asList("question", "answer", "productName", "shopName", "userId");
            Map<String, Object> searchResult = elasticsearchService.searchByVector(
                "qa_vectors",      // 索引名
                userId.longValue(), // 用户ID转为Long
                shopName,          // 店铺名称
                productName,       // 产品名称
                vector,            // 查询向量
                5,                 // 返回结果数量
                1.8f,              // 相似度阈值
                includeFields,     // 包含字段
                null               // 排除字段（不需要，因为已经指定了包含字段）
            );
            
            // 4. 提取相关文档
            List<Map<String, Object>> relevantDocs = elasticsearchService.extractRelevantDocuments(searchResult, 1.8);
            
            // 5. 构建提示词
            StringBuilder prompt = new StringBuilder();
            
            // 添加聊天历史
            String chatHistory = chatHistoryManager.getChatHistory(tableId);
            if (!chatHistory.isEmpty()) {
                prompt.append(chatHistory);
            }
            prompt.append("===").append(question).append("\n\n");

            prompt.append("下面是用户的当前问题：").append(question).append("\n\n");
            
            if (relevantDocs.isEmpty()) {
                // 如果没有找到相关文档，直接调用大模型
                String answer = qwenService.chat(prompt.toString());
                // 保存聊天记录
                chatHistoryManager.addChatRecord(tableId, question, answer);
                return ApiResponse.success(ChatResponse.of(answer));
            }
            
            prompt.append("以下是一些相关的问答信息，请参考这些信息来回答我的问题：\n\n");
            
            for (Map<String, Object> doc : relevantDocs) {
                prompt.append("问题：").append(doc.get("question")).append("\n");
                prompt.append("答案：").append(doc.get("answer")).append("\n\n");
            }
            
            prompt.append("请根据以上信息与历史记录，用专业、简洁的语言回答我的问题（只要信息足够就尽量简洁回答）。如果以上信息不足以回答我的问题，请自行回答。");
            
            // 6. 调用大模型获取回答
            String answer = qwenService.chat(prompt.toString());
            
            // 7. 保存聊天记录
            chatHistoryManager.addChatRecord(tableId, question, answer);
            
            return ApiResponse.success(ChatResponse.of(answer));
        } catch (Exception e) {
            log.error("向量搜索回答问题时发生错误", e);
            return ApiResponse.error(500, "处理您的问题时出现了错误，请稍后再试");
        }
    }

    /**
     * 混合搜索回答用户问题（结合文本搜索和向量搜索）
     *
     * @param request 问题请求，包含用户ID、店铺名称、产品名称和问题
     * @return 回答内容
     */
    @ApiLog("问答数量监控")
    @PostMapping("/hybrid-answer")
    public ApiResponse<ChatResponse> hybridAnswer(@RequestBody ChatRequest request) {
        try {
            // 1. 获取请求参数
            Integer userId = request.getUserId();
            String shopName = request.getShopName();
            String productName = request.getProductName();
            String question = request.getQuestion();
            String tableId = request.getTableId();
            
            // 如果tableId为空，则使用userId作为tableId
            if (tableId == null || tableId.trim().isEmpty()) {
                tableId = userId.toString();
            }

            // 参数校验
            if (userId == null || question == null || question.trim().isEmpty()) {
                return ApiResponse.error(400, "请提供有效的用户ID和问题");
            }

            // 2. 将问题转换为向量
            EmbeddingRequest embeddingRequest = new EmbeddingRequest();
            embeddingRequest.setInput(question);
            EmbeddingResponse embeddingResponse = aliEmbeddingService.getEmbedding(embeddingRequest);
            
            // 获取向量
            List<Float> embeddingList = embeddingResponse.getData().get(0).getEmbedding();
            float[] vector = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                vector[i] = embeddingList.get(i);
            }

            // 3. 执行混合搜索（结合文本搜索和向量搜索），指定返回字段
            List<String> includeFields = Arrays.asList("question", "answer", "productName", "shopName", "userId");
            Map<String, Object> searchResult = elasticsearchService.searchHybrid(
                "qa_vectors",                          // 索引名
                question,                              // 查询文本
                Arrays.asList("question", "answer"),   // 搜索字段
                vector,                                // 向量数组
                0.3f,                                  // 文本搜索权重
                0.7f,                                  // 向量搜索权重
                userId.longValue(),                    // 用户ID
                shopName,                              // 店铺名称
                productName,                           // 产品名称
                5,                                     // 返回结果数量
                includeFields,                         // 包含字段
                null                                   // 排除字段
            );
            
            // 4. 提取相关文档
            List<Map<String, Object>> relevantDocs = elasticsearchService.extractRelevantDocuments(searchResult, 1.8);
            
            // 5. 构建提示词
            StringBuilder prompt = new StringBuilder();
            
            // 添加聊天历史
            String chatHistory = chatHistoryManager.getChatHistory(tableId);
            if (!chatHistory.isEmpty()) {
                prompt.append(chatHistory);
            }
            
            prompt.append("我有一个问题：").append(question).append("\n\n");
            
            if (relevantDocs.isEmpty()) {
                // 如果没有找到相关文档，直接调用大模型
                String answer = qwenService.chat(prompt.toString());
                // 保存聊天记录
                chatHistoryManager.addChatRecord(tableId, question, answer);
                return ApiResponse.success(ChatResponse.of(answer));
            }
            
            prompt.append("以下是一些相关的问答信息，请参考这些信息来回答我的问题：\n\n");
            
            for (Map<String, Object> doc : relevantDocs) {
                prompt.append("问题：").append(doc.get("question")).append("\n");
                prompt.append("答案：").append(doc.get("answer")).append("\n\n");
            }
            
            prompt.append("请根据以上信息，用专业、简洁的语言回答我的问题,如果信息充足尽量减少礼貌用于和客套直接回答信息就行。" +
                    "如果以上信息不足以回答我的问题或者以上信息与用户问题无关。就不要参考上方数据回答，而是自己回答");
            
            // 6. 调用大模型获取回答
            String answer = qwenService.chat(prompt.toString());
            
            // 7. 保存聊天记录
            chatHistoryManager.addChatRecord(tableId, question, answer);
            
            return ApiResponse.success(ChatResponse.of(answer));
        } catch (Exception e) {
            log.error("混合搜索回答问题时发生错误", e);e.printStackTrace();
            return ApiResponse.error(500, "处理您的问题时出现了错误，请稍后再试");
        }
    }
    
    /**
     * 文本搜索回答用户问题（仅使用全文检索）
     *
     * @param request 问题请求，包含用户ID、店铺名称、产品名称和问题
     * @return 回答内容
     */
    @PostMapping("/text-answer")
    public ApiResponse<ChatResponse> textAnswer(@RequestBody ChatRequest request) {
        try {
            // 1. 获取请求参数
            Integer userId = request.getUserId();
            String shopName = request.getShopName();
            String productName = request.getProductName();
            String question = request.getQuestion();
            String tableId = request.getTableId();
            
            // 如果tableId为空，则使用userId作为tableId
            if (tableId == null || tableId.trim().isEmpty()) {
                tableId = userId.toString();
            }

            // 参数校验
            if (userId == null || question == null || question.trim().isEmpty()) {
                return ApiResponse.error(400, "请提供有效的用户ID和问题");
            }

            // 2. 执行文本搜索，指定返回字段
            List<String> includeFields = Arrays.asList("question", "answer", "productName", "shopName", "userId");
            Map<String, Object> searchResult = elasticsearchService.searchByText(
                "qa_vectors",                          // 索引名
                question,                              // 查询文本
                Arrays.asList("question", "answer"),   // 搜索字段
                userId.longValue(),                    // 用户ID
                shopName,                              // 店铺名称
                productName,                           // 产品名称
                5,                                     // 返回结果数量
                includeFields,                         // 包含字段
                null                                   // 排除字段
            );
            
            // 3. 提取相关文档
            List<Map<String, Object>> relevantDocs = elasticsearchService.extractRelevantDocuments(searchResult, 1.8);
            
            // 4. 构建提示词
            StringBuilder prompt = new StringBuilder();
            
            // 添加聊天历史
            String chatHistory = chatHistoryManager.getChatHistory(tableId);
            if (!chatHistory.isEmpty()) {
                prompt.append(chatHistory);
            }
            
            prompt.append("我有一个问题：").append(question).append("\n\n");
            
            if (relevantDocs.isEmpty()) {
                // 如果没有找到相关文档，直接调用大模型
                String answer = qwenService.chat(prompt.toString());
                // 保存聊天记录
                chatHistoryManager.addChatRecord(tableId, question, answer);
                return ApiResponse.success(ChatResponse.of(answer));
            }
            
            prompt.append("以下是一些相关的问答信息，请参考这些信息来回答我的问题：\n\n");
            
            for (Map<String, Object> doc : relevantDocs) {
                prompt.append("问题：").append(doc.get("question")).append("\n");
                prompt.append("答案：").append(doc.get("answer")).append("\n\n");
            }
            
            prompt.append("请根据以上信息，用专业、简洁的语言回答我的问题。如果以上信息不足以回答我的问题，请告诉我你需要更多信息。");
            
            // 5. 调用大模型获取回答
            String answer = qwenService.chat(prompt.toString());
            
            // 6. 保存聊天记录
            chatHistoryManager.addChatRecord(tableId, question, answer);
            
            return ApiResponse.success(ChatResponse.of(answer));
        } catch (Exception e) {
            log.error("文本搜索回答问题时发生错误", e);
            return ApiResponse.error(500, "处理您的问题时出现了错误，请稍后再试");
        }
    }

    /**
     * 上传文件并提取文本
     *
     * @param file 上传的文件
     * @return 提取的文本内容
     */
    @PostMapping("/upload")
    public ApiResponse<ChatResponse> uploadAndExtractText(@RequestParam("file") MultipartFile file) {
        try {
            String extractedText = qwenService.uploadAndExtractText(file);
            return ApiResponse.success(ChatResponse.of(extractedText));
        } catch (IOException e) {
            log.error("文件上传和文本提取失败", e);
            return ApiResponse.error(500, "文件处理失败: " + e.getMessage());
        }
    }

    /**
     * 使用指定向量字段进行搜索回答用户问题
     *
     * @param request 问题请求，包含用户ID、店铺名称、产品名称、问题和向量字段
     * @return 回答内容
     */
    @PostMapping("/vector-answer")
    public ApiResponse<ChatResponse> vectorAnswer(@RequestBody VectorChatRequest request) {
        try {
            // 1. 获取请求参数
            Integer userId = request.getUserId();
            String shopName = request.getShopName();
            String productName = request.getProductName();
            String question = request.getQuestion();
            String vectorField = request.getVectorField();
            String tableId = request.getTableId();
            
            // 如果tableId为空，则使用userId作为tableId
            if (tableId == null || tableId.trim().isEmpty()) {
                tableId = userId.toString();
            }

            // 参数校验
            if (userId == null || question == null || question.trim().isEmpty()) {
                return ApiResponse.error(400, "请提供有效的用户ID和问题");
            }
            
            // 如果未指定向量字段，默认使用问题向量
            if (vectorField == null || vectorField.trim().isEmpty()) {
                vectorField = "question_vector";
            }
            
            // 验证向量字段是否有效
            if (!vectorField.equals("question_vector") && !vectorField.equals("answer_vector")) {
                return ApiResponse.error(400, "向量字段必须是 'question_vector' 或 'answer_vector'");
            }

            // 2. 将问题转换为向量
            EmbeddingRequest embeddingRequest = new EmbeddingRequest();
            embeddingRequest.setInput(question);
            EmbeddingResponse embeddingResponse = aliEmbeddingService.getEmbedding(embeddingRequest);
            
            // 获取向量
            List<Float> embeddingList = embeddingResponse.getData().get(0).getEmbedding();
            float[] vector = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                vector[i] = embeddingList.get(i);
            }

            // 3. 执行向量搜索，指定返回字段（排除向量字段）
            List<String> includeFields = Arrays.asList("question", "answer", "productName", "shopName", "userId");
            Map<String, Object> searchResult = elasticsearchService.searchByVector(
                "qa_vectors",      // 索引名
                userId.longValue(), // 用户ID转为Long
                shopName,          // 店铺名称
                productName,       // 产品名称
                vector,            // 查询向量
                5,                 // 返回结果数量
                1.8f,              // 相似度阈值
                includeFields,     // 包含字段
                null,              // 排除字段（不需要，因为已经指定了包含字段）
                vectorField        // 向量字段
            );
            
            // 4. 提取相关文档
            List<Map<String, Object>> relevantDocs = elasticsearchService.extractRelevantDocuments(searchResult, 1.8);
            
            // 5. 构建提示词
            StringBuilder prompt = new StringBuilder();
            
            // 添加聊天历史
            String chatHistory = chatHistoryManager.getChatHistory(tableId);
            if (!chatHistory.isEmpty()) {
                prompt.append(chatHistory);
            }
            
            prompt.append("我有一个问题：").append(question).append("\n\n");
            
            if (relevantDocs.isEmpty()) {
                // 如果没有找到相关文档，直接调用大模型
                String answer = qwenService.chat(prompt.toString());
                // 保存聊天记录
                chatHistoryManager.addChatRecord(tableId, question, answer);
                return ApiResponse.success(ChatResponse.of(answer));
            }
            
            prompt.append("以下是一些相关的问答信息，请参考这些信息来回答我的问题：\n\n");
            
            for (Map<String, Object> doc : relevantDocs) {
                prompt.append("问题：").append(doc.get("question")).append("\n");
                prompt.append("答案：").append(doc.get("answer")).append("\n\n");
            }
            
            prompt.append("请根据以上信息，用专业、简洁的语言回答我的问题。如果以上信息不足以回答我的问题，请告诉我你需要更多信息。");
            
            // 6. 调用大模型获取回答
            String answer = qwenService.chat(prompt.toString());
            
            // 7. 保存聊天记录
            chatHistoryManager.addChatRecord(tableId, question, answer);
            
            return ApiResponse.success(ChatResponse.of(answer));
        } catch (Exception e) {
            log.error("向量搜索回答问题时发生错误", e);
            return ApiResponse.error(500, "处理您的问题时出现了错误，请稍后再试");
        }
    }

    /**
     * 混合搜索回答用户问题（结合文本搜索和向量搜索，支持指定向量字段）
     *
     * @param request 问题请求，包含用户ID、店铺名称、产品名称、问题和向量字段
     * @return 回答内容
     */
    @PostMapping("/hybrid-vector-answer")
    public ApiResponse<ChatResponse> hybridVectorAnswer(@RequestBody VectorChatRequest request) {
        try {
            // 1. 获取请求参数
            Integer userId = request.getUserId();
            String shopName = request.getShopName();
            String productName = request.getProductName();
            String question = request.getQuestion();
            String vectorField = request.getVectorField();
            String tableId = request.getTableId();
            
            // 如果tableId为空，则使用userId作为tableId
            if (tableId == null || tableId.trim().isEmpty()) {
                tableId = userId.toString();
            }

            // 参数校验
            if (userId == null || question == null || question.trim().isEmpty()) {
                return ApiResponse.error(400, "请提供有效的用户ID和问题");
            }
            
            // 如果未指定向量字段，默认使用问题向量
            if (vectorField == null || vectorField.trim().isEmpty()) {
                vectorField = "question_vector";
            }
            
            // 验证向量字段是否有效
            if (!vectorField.equals("question_vector") && !vectorField.equals("answer_vector")) {
                return ApiResponse.error(400, "向量字段必须是 'question_vector' 或 'answer_vector'");
            }

            // 2. 将问题转换为向量
            EmbeddingRequest embeddingRequest = new EmbeddingRequest();
            embeddingRequest.setInput(question);
            EmbeddingResponse embeddingResponse = aliEmbeddingService.getEmbedding(embeddingRequest);
            
            // 获取向量
            List<Float> embeddingList = embeddingResponse.getData().get(0).getEmbedding();
            float[] vector = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                vector[i] = embeddingList.get(i);
            }

            // 3. 执行混合搜索（结合文本搜索和向量搜索），指定返回字段
            List<String> includeFields = Arrays.asList("question", "answer", "productName", "shopName", "userId");
            Map<String, Object> searchResult = elasticsearchService.searchHybrid(
                "qa_vectors",                          // 索引名
                question,                              // 查询文本
                Arrays.asList("question", "answer"),   // 搜索字段
                vector,                                // 向量数组
                0.3f,                                  // 文本搜索权重
                0.7f,                                  // 向量搜索权重
                userId.longValue(),                    // 用户ID
                shopName,                              // 店铺名称
                productName,                           // 产品名称
                5,                                     // 返回结果数量
                includeFields,                         // 包含字段
                null,                                  // 排除字段
                vectorField                            // 向量字段
            );
            
            // 4. 提取相关文档
            List<Map<String, Object>> relevantDocs = elasticsearchService.extractRelevantDocuments(searchResult, 1.8);
            
            // 5. 构建提示词
            StringBuilder prompt = new StringBuilder();
            
            // 添加聊天历史
            String chatHistory = chatHistoryManager.getChatHistory(tableId);
            if (!chatHistory.isEmpty()) {
                prompt.append(chatHistory);
            }
            
            prompt.append("我有一个问题：").append(question).append("\n\n");
            
            if (relevantDocs.isEmpty()) {
                // 如果没有找到相关文档，直接调用大模型
                String answer = qwenService.chat(prompt.toString());
                // 保存聊天记录
                chatHistoryManager.addChatRecord(tableId, question, answer);
                return ApiResponse.success(ChatResponse.of(answer));
            }
            
            prompt.append("以下是一些相关的问答信息，请参考这些信息来回答我的问题：\n\n");
            
            for (Map<String, Object> doc : relevantDocs) {
                prompt.append("问题：").append(doc.get("question")).append("\n");
                prompt.append("答案：").append(doc.get("answer")).append("\n\n");
            }
            
            prompt.append("请根据以上信息，用专业、简洁的语言回答我的问题。如果以上信息不足以回答我的问题，请告诉我你需要更多信息。");
            
            // 6. 调用大模型获取回答
            String answer = qwenService.chat(prompt.toString());
            
            // 7. 保存聊天记录
            chatHistoryManager.addChatRecord(tableId, question, answer);
            
            return ApiResponse.success(ChatResponse.of(answer));
        } catch (Exception e) {
            log.error("混合搜索回答问题时发生错误", e);e.printStackTrace();
            return ApiResponse.error(500, "处理您的问题时出现了错误，请稍后再试");
        }
    }

    /**
     * 混合搜索回答用户问题（结合文本搜索和向量搜索）- GET方式
     * 
     * 与hybridAnswer功能相同，但使用GET请求方式获取参数
     *
     * @param userId 用户ID
     * @param shopName 店铺名称
     * @param productName 产品名称
     * @param question 用户问题
     * @param tableId 聊天窗口ID
     * @return 回答内容
     */
    @ApiLog("问答数量监控")
    @GetMapping("/hybrid-answer-get")
    public ApiResponse<ChatResponse> hybridAnswerGet(
            @RequestParam Integer userId,
            @RequestParam(required = false) String shopName,
            @RequestParam(required = false) String productName,
            @RequestParam String question,
            @RequestParam(required = false) String tableId) {
        try {
            // 如果tableId为空，则使用userId作为tableId
            if (tableId == null || tableId.trim().isEmpty()) {
                tableId = userId.toString();
            }

            // 参数校验
            if (userId == null || question == null || question.trim().isEmpty()) {
                return ApiResponse.error(400, "请提供有效的用户ID和问题");
            }

            // 2. 将问题转换为向量
            EmbeddingRequest embeddingRequest = new EmbeddingRequest();
            embeddingRequest.setInput(question);
            EmbeddingResponse embeddingResponse = aliEmbeddingService.getEmbedding(embeddingRequest);
            
            // 获取向量
            List<Float> embeddingList = embeddingResponse.getData().get(0).getEmbedding();
            float[] vector = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                vector[i] = embeddingList.get(i);
            }

            // 3. 执行混合搜索（结合文本搜索和向量搜索），指定返回字段
            List<String> includeFields = Arrays.asList("question", "answer", "productName", "shopName", "userId");
            Map<String, Object> searchResult = elasticsearchService.searchHybrid(
                "qa_vectors",                          // 索引名
                question,                              // 查询文本
                Arrays.asList("question", "answer"),   // 搜索字段
                vector,                                // 向量数组
                0.3f,                                  // 文本搜索权重
                0.7f,                                  // 向量搜索权重
                userId.longValue(),                    // 用户ID
                shopName,                              // 店铺名称
                productName,                           // 产品名称
                5,                                     // 返回结果数量
                includeFields,                         // 包含字段
                null                                   // 排除字段
            );
            
            // 4. 提取相关文档
            List<Map<String, Object>> relevantDocs = elasticsearchService.extractRelevantDocuments(searchResult, 1.8);
            
            // 5. 构建提示词
            StringBuilder prompt = new StringBuilder();
            
            // 添加聊天历史
            String chatHistory = chatHistoryManager.getChatHistory(tableId);
            if (!chatHistory.isEmpty()) {
                prompt.append(chatHistory);
            }
            
            prompt.append("我有一个问题：").append(question).append("\n\n");
            
            if (relevantDocs.isEmpty()) {
                // 如果没有找到相关文档，直接调用大模型
                String answer = qwenService.chat(prompt.toString());
                // 保存聊天记录
                chatHistoryManager.addChatRecord(tableId, question, answer);
                return ApiResponse.success(ChatResponse.of(answer));
            }
            
            prompt.append("以下是一些相关的问答信息，请参考这些信息来回答我的问题：\n\n");
            
            for (Map<String, Object> doc : relevantDocs) {
                prompt.append("问题：").append(doc.get("question")).append("\n");
                prompt.append("答案：").append(doc.get("answer")).append("\n\n");
            }
            
            prompt.append("请根据以上信息，用专业、简洁的语言回答我的问题,如果信息充足尽量减少礼貌用于和客套直接回答信息就行。" +
                    "如果以上信息不足以回答我的问题或者以上信息与用户问题无关。就不要参考上方数据回答，而是自己回答");
            
            // 6. 调用大模型获取回答
            String answer = qwenService.chat(prompt.toString());
            
            // 7. 保存聊天记录
            chatHistoryManager.addChatRecord(tableId, question, answer);
            
            return ApiResponse.success(ChatResponse.of(answer));
        } catch (Exception e) {
            log.error("GET方式混合搜索回答问题时发生错误", e);e.printStackTrace();
            return ApiResponse.error(500, "处理您的问题时出现了错误，请稍后再试");
        }
    }
} 
