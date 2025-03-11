package com.example.kefu.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 如果存在则返回true，否则返回false
     */
    public boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    public void createBucket(String bucketName) throws Exception {
        boolean isExist = bucketExists(bucketName);
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String objectName) throws Exception {
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
        return getFileUrl(objectName);
    }

    /**
     * 上传文本内容为文件
     *
     * @param content 文本内容
     * @param objectName 对象名称
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    public String uploadText(String content, String objectName, String contentType) throws Exception {
        // 检查存储桶是否存在
        boolean isExist = bucketExists(bucketName);
        if (!isExist) {
            createBucket(bucketName);
        }

        byte[] bytes = content.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        
        // 上传文本内容
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(bais, bytes.length, -1)
                        .contentType(contentType)
                        .build());

        // 返回文件访问路径
        return getFileUrl(objectName);
    }

    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    public String getFileUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .method(io.minio.http.Method.GET)
                        .build());
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件流
     */
    public InputStream downloadFile(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }

    /**
     * 获取文件信息
     *
     * @param objectName 对象名称
     * @return 文件信息
     */
    public Map<String, Object> getFileInfo(String objectName) throws Exception {
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
        
        Map<String, Object> info = new HashMap<>();
        info.put("size", stat.size());
        info.put("contentType", stat.contentType());
        info.put("etag", stat.etag());
        info.put("lastModified", stat.lastModified());
        
        return info;
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }

    /**
     * 列出存储桶中的所有对象
     *
     * @return 对象列表
     */
    public List<Map<String, Object>> listObjects() throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .build());

        List<Map<String, Object>> objects = new ArrayList<>();
        for (Result<Item> result : results) {
            Item item = result.get();
            Map<String, Object> objectInfo = new HashMap<>();
            objectInfo.put("objectName", item.objectName());
            objectInfo.put("size", item.size());
            objectInfo.put("lastModified", item.lastModified());
            objectInfo.put("etag", item.etag());
            objectInfo.put("isDir", item.isDir());
            
            objects.add(objectInfo);
        }
        
        return objects;
    }

    /**
     * 列出指定前缀的对象
     *
     * @param prefix 前缀
     * @return 对象列表
     */
    public List<Map<String, Object>> listObjectsByPrefix(String prefix) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .build());

        List<Map<String, Object>> objects = new ArrayList<>();
        for (Result<Item> result : results) {
            Item item = result.get();
            Map<String, Object> objectInfo = new HashMap<>();
            objectInfo.put("objectName", item.objectName());
            objectInfo.put("size", item.size());
            objectInfo.put("lastModified", item.lastModified());
            objectInfo.put("etag", item.etag());
            objectInfo.put("isDir", item.isDir());
            
            objects.add(objectInfo);
        }
        
        return objects;
    }
} 