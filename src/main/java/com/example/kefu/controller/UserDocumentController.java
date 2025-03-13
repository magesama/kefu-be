package com.example.kefu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.kefu.model.request.UserDocumentRequest;
import com.example.kefu.model.response.ApiResponse;
import com.example.kefu.model.response.UserDocumentResponse;
import com.example.kefu.service.UserDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 用户文档控制器
 */
@RestController
@RequestMapping("/api/documents")
public class UserDocumentController {

    @Autowired
    private UserDocumentService userDocumentService;

    /**
     * 上传文档
     *
     * @param file 文件
     * @param userId 用户ID
     * @param productId 产品ID
     * @param productName 产品名称
     * @param shopId 店铺ID
     * @param shopName 店铺名称
     * @param documentName 文档名称
     * @param description 文档描述
     * @return 文档ID
     */
    @PostMapping("/upload")
    public ApiResponse<Long> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "productId", required = false, defaultValue = "999999") Long productId,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "shopId", required = false) Long shopId,
            @RequestParam(value = "shopName", required = false) String shopName,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "description", required = false) String description) {
        
        UserDocumentRequest request = new UserDocumentRequest();
        request.setUserId(userId);
        request.setProductId(productId);
        request.setProductName(productName);
        request.setShopId(shopId);
        request.setShopName(shopName);
        request.setDocumentName(documentName);
        request.setDescription(description);
        
        Long documentId = userDocumentService.uploadDocument(file, request);
        return ApiResponse.success(documentId);
    }

    /**
     * 更新文档信息
     *
     * @param id 文档ID
     * @param request 文档请求对象
     * @return 是否成功
     */
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody UserDocumentRequest request) {
        
        boolean success = userDocumentService.updateDocument(id, request);
        return ApiResponse.success(success);
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteDocument(@PathVariable Long id) {
        boolean success = userDocumentService.deleteDocument(id);
        return ApiResponse.success(success);
    }

    /**
     * 获取文档详情
     *
     * @param id 文档ID
     * @return 文档响应对象
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDocumentResponse> getDocumentById(@PathVariable Long id) {
        UserDocumentResponse response = userDocumentService.getDocumentById(id);
        return ApiResponse.success(response);
    }

    /**
     * 获取用户的文档列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 文档分页列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<Page<UserDocumentResponse>> getUserDocuments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<UserDocumentResponse> responses = userDocumentService.getUserDocuments(userId, page, size);
        return ApiResponse.success(responses);
    }

    /**
     * 获取产品的文档列表
     *
     * @param productId 产品ID
     * @param page 页码
     * @param size 每页大小
     * @return 文档分页列表
     */
    @GetMapping("/product/{productId}")
    public ApiResponse<Page<UserDocumentResponse>> getProductDocuments(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<UserDocumentResponse> responses = userDocumentService.getProductDocuments(productId, page, size);
        return ApiResponse.success(responses);
    }

    /**
     * 下载文档
     *
     * @param id 文档ID
     * @param response HTTP响应对象
     */
    @GetMapping("/download/{id}")
    public void downloadDocument(
            @PathVariable Long id,
            HttpServletResponse response) {
        
        userDocumentService.downloadDocument(id, response);
    }
} 