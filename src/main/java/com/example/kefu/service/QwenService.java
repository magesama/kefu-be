package com.example.kefu.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.example.kefu.model.EmbeddingRequest;
import com.example.kefu.model.EmbeddingResponse;
import com.example.kefu.model.request.ChatRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 千问大模型服务
 */
@Service
public class QwenService {

    @Autowired
    AliEmbeddingService aliEmbeddingService;

    @Autowired
    ElasticsearchService elasticsearchService;

    @Value("${aliyun.dashscope.api-key}")
    private String apiKey;

    private final Generation generation;


    public QwenService() {
        this.generation = new Generation();
    }

    /**
     * 处理Excel文件并生成问答对
     *
     * @param file Excel文件
     * @return 生成的问答对
     */
    public String processExcelAndGenerateQA(MultipartFile file) throws IOException {
        Map<String, String> productInfo = parseExcel(file);
        return getQAList(productInfo);
    }

    /**
     * 解析Excel文件
     *
     * @param file Excel文件
     * @return 产品信息映射
     */
    private Map<String, String> parseExcel(MultipartFile file) throws IOException {
        Map<String, String> productInfo = new HashMap<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 遍历行
            for (Row row : sheet) {
                // 跳过空行
                if (row.getCell(0) == null || row.getCell(1) == null) {
                    continue;
                }
                
                // 获取第一列（属性名）和第二列（属性值）
                String key = getCellValueAsString(row.getCell(0));
                String value = getCellValueAsString(row.getCell(1));
                
                // 如果key不为空，添加到map中
                if (!key.trim().isEmpty()) {
                    productInfo.put(key, value);
                }
            }
        }
        
        return productInfo;
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字，避免科学计数法
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.format("%d", (long) value);
                    } else {
                        return String.format("%.2f", value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 根据产品信息生成问答对
     *
     * @param productInfo 产品信息映射
     * @return 生成的问答对
     */
    public String getQAList(Map<String, String> productInfo) {
        try {
            // 构建产品信息文本
            StringBuilder productInfoText = new StringBuilder();
            productInfo.forEach((key, value) -> {
                productInfoText.append(key).append("：").append(value).append("\n");
            });

            // 系统提示语
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("你是一个专业的电商客服知识库建设专家。你的任务是：\n" +
                            "1. 分析用户提供的产品信息（包括但不限于：产品名称、价格、材质、颜色等）\n" +
                            "2. 提取所有关键信息点\n" +
                            "3. 基于这些信息，生成全面的问答对，包括：\n" +
                            "   - 基本信息问答（如：价格、材质、颜色、尺寸等）\n" +
                            "   - 产品特点和优势\n" +
                            "   - 使用场景和建议\n" +
                            "   - 清洗和保养方法\n" +
                            "   - 售后服务相关\n" +
                            "   - 常见疑虑解答\n" +
                            "4. 答案要专业、简洁、易懂，突出产品卖点\n" +
                            "请以固定的JSON格式返回，格式如下：\n" +
                            "{\n" +
                            "  \"qa_pairs\": [\n" +
                            "    {\"question\": \"问题1\", \"answer\": \"答案1\"},\n" +
                            "    {\"question\": \"问题2\", \"answer\": \"答案2\"}\n" +
                            "  ]\n" +
                            "}")
                    .build();

            // 用户问题（包含产品信息）
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content("请基于以下产品信息，生成详细的问答对：\n\n" + productInfoText.toString() + 
                            "\n请确保问答对完整覆盖所有产品信息，并补充可能的客户关注点。")
                    .build();

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen-plus")
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            // 调用模型
            GenerationResult result = generation.call(param);

            // 返回结果
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，生成问答对时出现错误，请稍后再试。";
        }
    }

    /**
     * 聊天接口
     *
     * @param question 用户问题
     * @return 回答内容
     */
    public String chat(String question) {
        try {
            // 系统提示语
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("你是一个专业的客服助手，请用简洁、专业的语言回答用户的问题。")
                    .build();

            // 用户问题
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(question)
                    .build();

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .model("qwen-plus") // 可选：qwen-turbo, qwen-plus, qwen-max
                    .apiKey(apiKey)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .enableSearch(true)
                    .build();

            // 调用模型
            GenerationResult result = generation.call(param);
            
            // 返回结果
            if (result != null && result.getOutput() != null && result.getOutput().getChoices() != null 
                && !result.getOutput().getChoices().isEmpty() 
                && result.getOutput().getChoices().get(0).getMessage() != null) {
                return result.getOutput().getChoices().get(0).getMessage().getContent();
            } else {
                return "抱歉，无法获取回答。";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，处理您的问题时出现了错误：" + e.getMessage();
        }
    }

    /**
     * 上传文件并提取文本内容
     *
     * @param file 上传的文件
     * @return 提取的文本内容
     * @throws IOException 如果文件处理失败
     */
    public String uploadAndExtractText(MultipartFile file) throws IOException {
        try {
            // 获取文件名和扩展名
            String fileName = file.getOriginalFilename();
            String fileExtension = "";
            if (fileName != null && fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            }
            
            // 根据文件类型处理
            String extractedText;
            if ("xlsx".equals(fileExtension) || "xls".equals(fileExtension)) {
                // Excel文件处理
                Map<String, String> productInfo = parseExcel(file);
                StringBuilder textBuilder = new StringBuilder();
                textBuilder.append("文件名: ").append(fileName).append("\n\n");
                textBuilder.append("提取的内容:\n");
                productInfo.forEach((key, value) -> {
                    textBuilder.append(key).append(": ").append(value).append("\n");
                });
                extractedText = textBuilder.toString();
            } else {
                // 其他文件类型，返回简单信息
                extractedText = "已上传文件: " + fileName + "\n\n" +
                        "文件类型: " + fileExtension + "\n\n" +
                        "文件大小: " + (file.getSize() / 1024) + " KB\n\n" +
                        "注意: 目前仅支持Excel文件的内容提取。";
            }
            
            return extractedText;
        } catch (Exception e) {
            throw new IOException("文件处理失败: " + e.getMessage(), e);
        }
    }

    public String answer(ChatRequest request) throws IOException {

//        把用户的问题通过向量方法变成向量，参考复用aliEmbeddingService.getEmbedding(embeddingRequest);
        EmbeddingRequest embeddingRequest = new EmbeddingRequest();
        embeddingRequest.setInput(request.getQuestion());
        EmbeddingResponse embeddingResponse = aliEmbeddingService.getEmbedding(embeddingRequest);

//        然后构建对es的查询条件，用户id精确匹配，店名和产品名精确匹配，在按照向量查询排序，

        List<Float> embeddingList = embeddingResponse.getData().get(0).getEmbedding();
        float[] vector = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            vector[i] = embeddingList.get(i);
        }

        List<Map<String, Object>> results = elasticsearchService.searchSimilarDocumentsInIndex(
                "a_vectors",
                vector,
                5);

//        然后把向量得分超过1.8的取前面五条的answer，


//        同问题一起拼接一个好的提示词给大模型也就是这里的chat方法去获取答案。


        return null;
    }
}