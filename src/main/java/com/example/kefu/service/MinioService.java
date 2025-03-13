package com.example.kefu.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * MinIO服务接口
 */
public interface MinioService {

    /**
     * 上传文件到MinIO
     *
     * @param file 文件
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 文件URL
     */
    String uploadFile(MultipartFile file, String bucketName, String objectName);

    /**
     * 从MinIO下载文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String bucketName, String objectName);

    /**
     * 获取文件URL
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @param expires 过期时间（秒）
     * @return 文件URL
     */
    String getFileUrl(String bucketName, String objectName, int expires);

    /**
     * 检查文件是否存在
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean isFileExist(String bucketName, String objectName);

    /**
     * 从URL中提取桶名称和对象名称
     *
     * @param url MinIO URL
     * @return 包含桶名称和对象名称的数组，索引0为桶名称，索引1为对象名称
     */
    String[] extractBucketAndObjectFromUrl(String url);

    /**
     * 从MinIO删除文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    boolean deleteFile(String bucketName, String objectName);

} 