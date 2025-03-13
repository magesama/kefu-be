package com.example.kefu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kefu.entity.UserDocument;
import com.example.kefu.mapper.UserDocumentMapper;
import com.example.kefu.model.EmbeddingRequest;
import com.example.kefu.model.EmbeddingResponse;
import com.example.kefu.model.request.UserDocumentRequest;
import com.example.kefu.model.response.UserDocumentResponse;
import com.example.kefu.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 用户文档服务实现类
 */
@Slf4j
@Service
public class UserDocumentServiceImpl extends ServiceImpl<UserDocumentMapper, UserDocument> implements UserDocumentService {

    @Autowired
    private MinioService minioService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private AliEmbeddingService aliEmbeddingService;
    @Autowired
    private QwenService qwenService;

    @Value("${minio.document-bucket}")
    private String documentBucket;

    /**
     * 上传文档
     *
     * @param file 文件
     * @param request 文档请求对象
     * @return 文档ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadDocument(MultipartFile file, UserDocumentRequest request) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        try {
            // 生成文件存储路径
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String objectName = generateObjectName(request.getUserId(), fileExtension);
            
            // 上传文件到MinIO
            String fileUrl = minioService.uploadFile(file, documentBucket, objectName);
            
            // 保存文档信息到数据库
            UserDocument document = new UserDocument();
            document.setUserId(request.getUserId());
            document.setProductId(request.getProductId());
            document.setProductName(request.getProductName());
            document.setShopId(request.getShopId());
            document.setShopName(request.getShopName());
            document.setDocumentName(StringUtils.hasText(request.getDocumentName()) ? request.getDocumentName() : originalFilename);
            document.setDocumentType(fileExtension);
            document.setFileSize(file.getSize());
            document.setFileUrl(fileUrl);
            document.setDescription(request.getDescription());
            document.setIsDeleted(false);
            document.setCreateTime(LocalDateTime.now());
            document.setUpdateTime(LocalDateTime.now());
            
            // 保存到数据库
            save(document);

            try {
                // 读取Excel文件内容
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

                // 构建产品信息文本
                StringBuilder textBuilder = new StringBuilder();
                productInfo.forEach((key, value) -> {
                    textBuilder.append(key).append("：").append(value).append("\n");
                });
                String content = textBuilder.toString();

                // 调用大模型进行QA拆分
                String chatResult = qwenService.getQAList(productInfo);
                
                // 解析大模型返回的QA对
                List<Map<String, String>> qaList = parseQAFromChatResult(chatResult);
                
                // 处理每个QA对
                for (Map<String, String> qa : qaList) {
                    String question = qa.get("question");
                    String answer = qa.get("answer");
                    
                    // 构建ES文档
                    Map<String, Object> esDocument = new HashMap<>();
                    esDocument.put("userId", document.getUserId());
                    esDocument.put("productId", document.getProductId());
                    esDocument.put("productName", document.getProductName());
                    esDocument.put("shopId", document.getShopId());
                    esDocument.put("shopName", document.getShopName());
                    esDocument.put("documentId", document.getId());
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
                }

                return document.getId();
            } catch (Exception e) {
                // 如果处理失败，需要删除已上传的文件和数据库记录
                log.error("处理文档失败", e);e.printStackTrace();
                try {
                    // 删除MinIO中的文件
                    minioService.deleteFile(documentBucket, objectName);
                    // 删除数据库记录
                    removeById(document.getId());
                } catch (Exception ex) {
                    log.error("清理失败的文档记录时出错", ex);
                }
                throw new RuntimeException("处理文档失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("上传文档失败", e);
            throw new RuntimeException("上传文档失败: " + e.getMessage());
        }
    }

    /**
     * 更新文档信息
     *
     * @param id 文档ID
     * @param request 文档请求对象
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDocument(Long id, UserDocumentRequest request) {
        UserDocument document = getById(id);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        // 只更新文档名称和描述
        document.setDocumentName(request.getDocumentName());
        document.setDescription(request.getDescription());
        document.setUpdateTime(LocalDateTime.now());
        
        return updateById(document);
    }

    /**
     * 逻辑删除文档
     *
     * @param id 文档ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDocument(Long id) {
        // 使用MyBatis-Plus的逻辑删除功能
        return removeById(id);
    }

    /**
     * 获取文档详情
     *
     * @param id 文档ID
     * @return 文档响应对象
     */
    @Override
    public UserDocumentResponse getDocumentById(Long id) {
        UserDocument document = getById(id);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        return convertToResponse(document);
    }

    /**
     * 获取用户的文档列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 文档分页列表
     */
    @Override
    public Page<UserDocumentResponse> getUserDocuments(Long userId, int page, int size) {
        // 查询条件：用户ID匹配且未删除
        LambdaQueryWrapper<UserDocument> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDocument::getUserId, userId);
        
        // 分页查询
        Page<UserDocument> documentPage = page(new Page<>(page, size), queryWrapper);
        
        // 转换为响应对象
        Page<UserDocumentResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(documentPage, responsePage, "records");
        
        List<UserDocumentResponse> responseList = new ArrayList<>();
        for (UserDocument document : documentPage.getRecords()) {
            responseList.add(convertToResponse(document));
        }
        responsePage.setRecords(responseList);
        
        return responsePage;
    }

    /**
     * 获取产品的文档列表
     *
     * @param productId 产品ID
     * @param page 页码
     * @param size 每页大小
     * @return 文档分页列表
     */
    @Override
    public Page<UserDocumentResponse> getProductDocuments(Long productId, int page, int size) {
        // 查询条件：产品ID匹配且未删除
        LambdaQueryWrapper<UserDocument> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDocument::getProductId, productId);
        
        // 分页查询
        Page<UserDocument> documentPage = page(new Page<>(page, size), queryWrapper);
        
        // 转换为响应对象
        Page<UserDocumentResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(documentPage, responsePage, "records");
        
        List<UserDocumentResponse> responseList = new ArrayList<>();
        for (UserDocument document : documentPage.getRecords()) {
            responseList.add(convertToResponse(document));
        }
        responsePage.setRecords(responseList);
        
        return responsePage;
    }

    /**
     * 下载文档
     *
     * @param id 文档ID
     * @param response HTTP响应对象
     */
    @Override
    public void downloadDocument(Long id, HttpServletResponse response) {
        UserDocument document = getById(id);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        try {
            // 从文件URL中提取桶名称和对象名称
            String[] bucketAndObject = minioService.extractBucketAndObjectFromUrl(document.getFileUrl());
            String objectName = bucketAndObject[1];
            
            // 从MinIO下载文件
            InputStream inputStream = minioService.downloadFile(documentBucket, objectName);
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            String encodedFileName = URLEncoder.encode(document.getDocumentName(), "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
            
            // 写入响应
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.flush();
            inputStream.close();
        } catch (IOException e) {
            log.error("下载文档失败", e);
            throw new RuntimeException("下载文档失败: " + e.getMessage());
        }
    }

    /**
     * 将实体对象转换为响应对象
     *
     * @param document 文档实体
     * @return 文档响应对象
     */
    private UserDocumentResponse convertToResponse(UserDocument document) {
        UserDocumentResponse response = new UserDocumentResponse();
        BeanUtils.copyProperties(document, response);
        return response;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 生成对象名称
     *
     * @param userId 用户ID
     * @param fileExtension 文件扩展名
     * @return 对象名称
     */
    private String generateObjectName(Long userId, String fileExtension) {
        // 格式：userId/yyyy/MM/dd/UUID.extension
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return userId + "/" + datePath + "/" + uuid + "." + fileExtension;
    }

    /**
     * 解析大模型返回的QA结果
     * @param chatResult 大模型返回的结果，格式为：
     * {
     *   "qa_pairs": [
     *     {"question": "问题1", "answer": "答案1"},
     *     {"question": "问题2", "answer": "答案2"}
     *   ]
     * }
     * @return QA对列表
     */
    private List<Map<String, String>> parseQAFromChatResult(String chatResult) {
        List<Map<String, String>> qaList = new ArrayList<>();
        try {
            // 处理输入字符串，移除可能存在的markdown代码块标记
            String jsonStr = chatResult;
            if (jsonStr.startsWith("```")) {
                // 移除开头的```json或```
                jsonStr = jsonStr.substring(jsonStr.indexOf('\n'));
                // 移除结尾的```
                int endIndex = jsonStr.lastIndexOf("```");
                if (endIndex >= 0) {
                    jsonStr = jsonStr.substring(0, endIndex);
                }
                // 去除首尾空白字符
                jsonStr = jsonStr.trim();
            }

            ObjectMapper mapper = new ObjectMapper();
            // 首先解析外层的JSON对象
            Map<String, Object> rootNode = mapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            
            // 获取qa_pairs数组
            List<Map<String, String>> qaPairs = (List<Map<String, String>>) rootNode.get("qa_pairs");
            
            if (qaPairs != null) {
                qaList.addAll(qaPairs);
            } else {
                log.warn("QA解析结果为空，原始数据: {}", jsonStr);
            }
        } catch (Exception e) {
            log.error("解析QA对失败，原始数据: {}", chatResult, e);
            throw new RuntimeException("解析QA对失败: " + e.getMessage());
        }
        return qaList;
    }

    /**
     * 获取单元格的值作为字符串
     * @param cell 单元格
     * @return 单元格的值作为字符串
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
} 