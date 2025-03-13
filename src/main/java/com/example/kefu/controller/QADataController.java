package com.example.kefu.controller;

import com.example.kefu.model.EmbeddingRequest;
import com.example.kefu.model.EmbeddingResponse;
import com.example.kefu.model.request.QADataRequest;
import com.example.kefu.model.response.ApiResponse;
import com.example.kefu.service.AliEmbeddingService;
import com.example.kefu.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QA数据控制器
 * 提供直接上传QA数据到Elasticsearch的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/qa-data")
public class QADataController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private AliEmbeddingService aliEmbeddingService;

    /**
     * 上传QA数据
     *
     * @param request QA数据请求对象，包含用户ID、店铺信息、产品信息和QA列表
     * @return 成功上传的QA数量
     */
    @PostMapping("/upload")
    public ApiResponse<Integer> uploadQAData(@Valid @RequestBody QADataRequest request) {
        try {
            // 获取请求参数
            Long userId = request.getUserId();
            Long shopId = request.getShopId();
            String shopName = request.getShopName();
            Long productId = request.getProductId();
            String productName = request.getProductName();
            List<Map<String, String>> qaList = request.getQaList();

            // 参数校验
            if (userId == null) {
                return ApiResponse.error(400, "用户ID不能为空");
            }
            
            if (qaList == null || qaList.isEmpty()) {
                return ApiResponse.error(400, "QA列表不能为空");
            }

            // 处理每个QA对
            int successCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (Map<String, String> qa : qaList) {
                String question = qa.get("question");
                String answer = qa.get("answer");
                
                // 参数校验
                if (question == null || question.trim().isEmpty()) {
                    errorMessages.add("问题不能为空");
                    continue;
                }
                
                if (answer == null || answer.trim().isEmpty()) {
                    errorMessages.add("答案不能为空");
                    continue;
                }
                
                try {
                    // 构建ES文档
                    Map<String, Object> esDocument = new HashMap<>();
                    esDocument.put("userId", userId);
                    esDocument.put("shopId", shopId);
                    esDocument.put("shopName", shopName);
                    esDocument.put("productId", productId);
                    esDocument.put("productName", productName);
                    esDocument.put("question", question);
                    esDocument.put("answer", answer);
                    
                    // 为问题生成向量
                    EmbeddingRequest questionEmbeddingRequest = new EmbeddingRequest();
                    questionEmbeddingRequest.setInput(question);
                    EmbeddingResponse questionEmbeddingResponse = aliEmbeddingService.getEmbedding(questionEmbeddingRequest);
                    
                    // 转换问题向量格式
                    List<Float> questionEmbeddingList = questionEmbeddingResponse.getData().get(0).getEmbedding();
                    float[] questionVector = new float[questionEmbeddingList.size()];
                    for (int i = 0; i < questionEmbeddingList.size(); i++) {
                        questionVector[i] = questionEmbeddingList.get(i);
                    }
                    esDocument.put("question_vector", questionVector);
                    
                    // 为答案生成向量
                    EmbeddingRequest answerEmbeddingRequest = new EmbeddingRequest();
                    answerEmbeddingRequest.setInput(answer);
                    EmbeddingResponse answerEmbeddingResponse = aliEmbeddingService.getEmbedding(answerEmbeddingRequest);
                    
                    // 转换答案向量格式
                    List<Float> answerEmbeddingList = answerEmbeddingResponse.getData().get(0).getEmbedding();
                    float[] answerVector = new float[answerEmbeddingList.size()];
                    for (int i = 0; i < answerEmbeddingList.size(); i++) {
                        answerVector[i] = answerEmbeddingList.get(i);
                    }
                    esDocument.put("answer_vector", answerVector);

                    // 添加时间戳
                    esDocument.put("createTime", LocalDateTime.now());
                    esDocument.put("updateTime", LocalDateTime.now());

                    // 存储到ES索引
                    elasticsearchService.saveDocumentToIndex("qa_vectors", esDocument);
                    successCount++;
                } catch (Exception e) {
                    log.error("处理QA对失败: question={}, answer={}", question, answer, e);
                    errorMessages.add("处理QA对失败: " + e.getMessage());
                }
            }

            // 返回结果
            if (successCount == 0 && !errorMessages.isEmpty()) {
                return ApiResponse.error(500, "所有QA对处理失败: " + String.join("; ", errorMessages));
            } else if (successCount < qaList.size()) {
                ApiResponse<Integer> response = new ApiResponse<>(0, "部分QA对处理成功，成功数量: " + successCount + ", 失败原因: " + String.join("; ", errorMessages), successCount);
                return response;
            } else {
                return ApiResponse.success(successCount);
            }
        } catch (Exception e) {
            log.error("上传QA数据失败", e);
            return ApiResponse.error(500, "上传QA数据失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传QA数据
     *
     * @param requestList QA数据请求对象列表，每个对象包含用户ID、店铺信息、产品信息和QA列表
     * @return 成功上传的QA数量
     */
    @PostMapping("/batch-upload")
    public ApiResponse<Integer> batchUploadQAData(@Valid @RequestBody List<QADataRequest> requestList) {
        try {
            if (requestList == null || requestList.isEmpty()) {
                return ApiResponse.error(400, "请求列表不能为空");
            }

            int totalSuccessCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (QADataRequest request : requestList) {
                try {
                    ApiResponse<Integer> response = uploadQAData(request);
                    if (response.getCode() == 0) {
                        totalSuccessCount += response.getData();
                    } else {
                        errorMessages.add(response.getMessage());
                    }
                } catch (Exception e) {
                    log.error("处理QA数据请求失败", e);
                    errorMessages.add("处理QA数据请求失败: " + e.getMessage());
                }
            }

            // 返回结果
            if (totalSuccessCount == 0 && !errorMessages.isEmpty()) {
                return ApiResponse.error(500, "所有QA数据处理失败: " + String.join("; ", errorMessages));
            } else if (!errorMessages.isEmpty()) {
                ApiResponse<Integer> response = new ApiResponse<>(0, "部分QA数据处理成功，成功数量: " + totalSuccessCount + ", 失败原因: " + String.join("; ", errorMessages), totalSuccessCount);
                return response;
            } else {
                return ApiResponse.success(totalSuccessCount);
            }
        } catch (Exception e) {
            log.error("批量上传QA数据失败", e);
            return ApiResponse.error(500, "批量上传QA数据失败: " + e.getMessage());
        }
    }
} 