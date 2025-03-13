package com.example.kefu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.kefu.model.request.UserDocumentRequest;
import com.example.kefu.model.response.UserDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户文档服务接口
 */
public interface UserDocumentService {

    /**
     * 上传文档
     *
     * @param file 文件
     * @param request 文档请求对象
     * @return 文档ID
     */
    Long uploadDocument(MultipartFile file, UserDocumentRequest request);

    /**
     * 更新文档信息
     *
     * @param id 文档ID
     * @param request 文档请求对象
     * @return 是否成功
     */
    boolean updateDocument(Long id, UserDocumentRequest request);

    /**
     * 逻辑删除文档
     *
     * @param id 文档ID
     * @return 是否成功
     */
    boolean deleteDocument(Long id);

    /**
     * 获取文档详情
     *
     * @param id 文档ID
     * @return 文档响应对象
     */
    UserDocumentResponse getDocumentById(Long id);

    /**
     * 获取用户的文档列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 文档分页列表
     */
    Page<UserDocumentResponse> getUserDocuments(Long userId, int page, int size);

    /**
     * 获取产品的文档列表
     *
     * @param productId 产品ID
     * @param page 页码
     * @param size 每页大小
     * @return 文档分页列表
     */
    Page<UserDocumentResponse> getProductDocuments(Long productId, int page, int size);

    /**
     * 下载文档
     *
     * @param id 文档ID
     * @param response HTTP响应对象
     */
    void downloadDocument(Long id, HttpServletResponse response);
}