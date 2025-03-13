package com.example.kefu.service.impl;

import com.example.kefu.service.MinioService;
import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MinIO服务实现类
 */
@Service
public class MinioServiceImpl implements MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.document-bucket}")
    private String documentBucket;

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 如果存在则返回true，否则返回false
     */
    private boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    private void createBucket(String bucketName) throws Exception {
        boolean isExist = bucketExists(bucketName);
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 上传文件到MinIO
     *
     * @param file 文件
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 文件URL
     */
    @Override
    public String uploadFile(MultipartFile file, String bucketName, String objectName) {
        try {
            // 检查存储桶是否存在
            boolean isExist = bucketExists(bucketName);
            if (!isExist) {
                createBucket(bucketName);
            }

            // 设置文件类型
            String contentType = file.getContentType();
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            // 返回文件访问路径
            return getFileUrl(bucketName, objectName, 3600);
        } catch (Exception e) {
            throw new RuntimeException("上传文件到MinIO失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从MinIO下载文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    @Override
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("从MinIO下载文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件URL
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @param expires 过期时间（秒）
     * @return 文件URL
     */
    @Override
    public String getFileUrl(String bucketName, String objectName, int expires) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(io.minio.http.Method.GET)
                            .expiry(expires)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("获取MinIO文件URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    @Override
    public boolean isFileExist(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从URL中提取桶名称和对象名称
     *
     * @param url MinIO URL
     * @return 包含桶名称和对象名称的数组，索引0为桶名称，索引1为对象名称
     */
    @Override
    public String[] extractBucketAndObjectFromUrl(String url) {
        try {
            // 假设URL格式为：http://minio-server/bucket-name/object-name
            String[] parts = url.split("/");
            if (parts.length >= 4) {
                String bucketName = parts[3];
                StringBuilder objectName = new StringBuilder();
                for (int i = 4; i < parts.length; i++) {
                    objectName.append(parts[i]);
                    if (i < parts.length - 1) {
                        objectName.append("/");
                    }
                }
                return new String[]{bucketName, objectName.toString()};
            }
            throw new RuntimeException("无效的MinIO URL格式");
        } catch (Exception e) {
            throw new RuntimeException("从URL提取桶名称和对象名称失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从MinIO删除文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
//            log.error("从MinIO删除文件失败", e);
            return false;
        }
    }



} 