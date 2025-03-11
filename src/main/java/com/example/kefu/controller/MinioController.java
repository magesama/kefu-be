package com.example.kefu.controller;

import com.example.kefu.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MinIO 文件存储控制器
 * 
 * <p>提供文件上传、下载、查询、删除等基本操作</p>
 * 
 * <h2>API 文档</h2>
 * 
 * <h3>1. 上传文件</h3>
 * <p>
 * <b>请求</b>: POST /api/minio/upload<br>
 * <b>参数</b>: 
 * <ul>
 *   <li>file: 文件 (必须)</li>
 *   <li>objectName: 对象名称 (可选，默认使用原始文件名)</li>
 * </ul>
 * <b>响应示例</b>:
 * <pre>
 * {
 *     "success": true,
 *     "message": "文件上传成功",
 *     "url": "http://39.102.210.49:9000/dev/test/example.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
 *     "objectName": "test/example.jpg"
 * }
 * </pre>
 * </p>
 * 
 * <h3>2. 上传文本内容</h3>
 * <p>
 * <b>请求</b>: POST /api/minio/upload-text<br>
 * <b>Content-Type</b>: application/json<br>
 * <b>请求体</b>:
 * <pre>
 * {
 *     "content": "这是一个测试文本内容",
 *     "objectName": "test/example.txt",
 *     "contentType": "text/plain"
 * }
 * </pre>
 * <b>响应示例</b>:
 * <pre>
 * {
 *     "success": true,
 *     "message": "文本上传成功",
 *     "url": "http://39.102.210.49:9000/dev/test/example.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
 *     "objectName": "test/example.txt"
 * }
 * </pre>
 * </p>
 * 
 * <h3>3. 获取文件信息</h3>
 * <p>
 * <b>请求</b>: GET /api/minio/info?objectName=test/example.jpg<br>
 * <b>响应示例</b>:
 * <pre>
 * {
 *     "success": true,
 *     "data": {
 *         "size": 12345,
 *         "contentType": "image/jpeg",
 *         "etag": "\"a1b2c3d4e5f6\"",
 *         "lastModified": "2023-06-15T10:30:00Z"
 *     }
 * }
 * </pre>
 * </p>
 * 
 * <h3>4. 下载文件</h3>
 * <p>
 * <b>请求</b>: GET /api/minio/download?objectName=test/example.jpg<br>
 * <b>响应</b>: 文件内容（浏览器会自动下载）
 * </p>
 * 
 * <h3>5. 删除文件</h3>
 * <p>
 * <b>请求</b>: DELETE /api/minio/delete?objectName=test/example.jpg<br>
 * <b>响应示例</b>:
 * <pre>
 * {
 *     "success": true,
 *     "message": "文件删除成功"
 * }
 * </pre>
 * </p>
 * 
 * <h3>6. 列出所有文件</h3>
 * <p>
 * <b>请求</b>: GET /api/minio/list<br>
 * <b>响应示例</b>:
 * <pre>
 * {
 *     "success": true,
 *     "data": [
 *         {
 *             "objectName": "test/example.jpg",
 *             "size": 12345,
 *             "lastModified": "2023-06-15T10:30:00Z",
 *             "etag": "\"a1b2c3d4e5f6\"",
 *             "isDir": false
 *         },
 *         {
 *             "objectName": "test/example.txt",
 *             "size": 25,
 *             "lastModified": "2023-06-15T10:35:00Z",
 *             "etag": "\"g7h8i9j0k1l2\"",
 *             "isDir": false
 *         }
 *     ]
 * }
 * </pre>
 * </p>
 * 
 * <h3>7. 列出指定前缀的文件</h3>
 * <p>
 * <b>请求</b>: GET /api/minio/list-by-prefix?prefix=test/<br>
 * <b>响应示例</b>:
 * <pre>
 * {
 *     "success": true,
 *     "data": [
 *         {
 *             "objectName": "test/example.jpg",
 *             "size": 12345,
 *             "lastModified": "2023-06-15T10:30:00Z",
 *             "etag": "\"a1b2c3d4e5f6\"",
 *             "isDir": false
 *         },
 *         {
 *             "objectName": "test/example.txt",
 *             "size": 25,
 *             "lastModified": "2023-06-15T10:35:00Z",
 *             "etag": "\"g7h8i9j0k1l2\"",
 *             "isDir": false
 *         }
 *     ]
 * }
 * </pre>
 * </p>
 */
@RestController
@RequestMapping("/api/minio")
public class MinioController {

    @Autowired
    private MinioService minioService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param objectName 对象名称（可选，如果不提供则使用原始文件名）
     * @return 上传结果
     * 
     * 示例请求：
     * POST /api/minio/upload
     * Content-Type: multipart/form-data
     * 
     * 参数：
     * - file: [选择文件]
     * - objectName: test/example.jpg (可选)
     * 
     * 响应示例：
     * {
     *     "success": true,
     *     "message": "文件上传成功",
     *     "url": "http://39.102.210.49:9000/dev/test/example.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
     *     "objectName": "test/example.jpg"
     * }
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "objectName", required = false) String objectName) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 如果没有提供对象名称，则使用原始文件名
            if (objectName == null || objectName.isEmpty()) {
                objectName = file.getOriginalFilename();
            }
            
            // 直接使用 MultipartFile 进行上传
            String url = minioService.uploadFile(file, objectName);
            
            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("url", url);
            result.put("objectName", objectName);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 上传文本内容为文件
     *
     * @param request 包含文本内容和对象名称的请求
     * @return 上传结果
     * 
     * 示例请求：
     * POST /api/minio/upload-text
     * Content-Type: application/json
     * 
     * 请求体：
     * {
     *     "content": "这是一个测试文本内容",
     *     "objectName": "test/example.txt",
     *     "contentType": "text/plain"
     * }
     * 
     * 响应示例：
     * {
     *     "success": true,
     *     "message": "文本上传成功",
     *     "url": "http://39.102.210.49:9000/dev/test/example.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
     *     "objectName": "test/example.txt"
     * }
     */
    @PostMapping("/upload-text")
    public Map<String, Object> uploadText(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String content = request.get("content");
            String objectName = request.get("objectName");
            String contentType = request.getOrDefault("contentType", "text/plain");
            
            String url = minioService.uploadText(content, objectName, contentType);
            
            result.put("success", true);
            result.put("message", "文本上传成功");
            result.put("url", url);
            result.put("objectName", objectName);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文本上传失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取文件信息
     *
     * @param objectName 对象名称
     * @return 文件信息
     * 
     * 示例请求：
     * GET /api/minio/info?objectName=test/example.jpg
     * 
     * 响应示例：
     * {
     *     "success": true,
     *     "data": {
     *         "size": 12345,
     *         "contentType": "image/jpeg",
     *         "etag": "\"a1b2c3d4e5f6\"",
     *         "lastModified": "2023-06-15T10:30:00Z"
     *     }
     * }
     */
    @GetMapping("/info")
    public Map<String, Object> getFileInfo(@RequestParam("objectName") String objectName) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> fileInfo = minioService.getFileInfo(objectName);
            
            result.put("success", true);
            result.put("data", fileInfo);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取文件信息失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件内容
     * 
     * 示例请求：
     * GET /api/minio/download?objectName=test/example.jpg
     * 
     * 响应：
     * 文件内容（浏览器会自动下载）
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("objectName") String objectName) {
        try {
            InputStream inputStream = minioService.downloadFile(objectName);
            Map<String, Object> fileInfo = minioService.getFileInfo(objectName);
            
            // 读取文件内容
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] bytes = buffer.toByteArray();
            inputStream.close();
            buffer.close();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType((String) fileInfo.get("contentType")));
            headers.setContentLength((Long) fileInfo.get("size"));
            
            // 设置文件名
            String filename = URLEncoder.encode(objectName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     * @return 删除结果
     * 
     * 示例请求：
     * DELETE /api/minio/delete?objectName=test/example.jpg
     * 
     * 响应示例：
     * {
     *     "success": true,
     *     "message": "文件删除成功"
     * }
     */
    @DeleteMapping("/delete")
    public Map<String, Object> deleteFile(@RequestParam("objectName") String objectName) {
        Map<String, Object> result = new HashMap<>();
        try {
            minioService.deleteFile(objectName);
            
            result.put("success", true);
            result.put("message", "文件删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 列出所有文件
     *
     * @return 文件列表
     * 
     * 示例请求：
     * GET /api/minio/list
     * 
     * 响应示例：
     * {
     *     "success": true,
     *     "data": [
     *         {
     *             "objectName": "test/example.jpg",
     *             "size": 12345,
     *             "lastModified": "2023-06-15T10:30:00Z",
     *             "etag": "\"a1b2c3d4e5f6\"",
     *             "isDir": false
     *         },
     *         {
     *             "objectName": "test/example.txt",
     *             "size": 25,
     *             "lastModified": "2023-06-15T10:35:00Z",
     *             "etag": "\"g7h8i9j0k1l2\"",
     *             "isDir": false
     *         }
     *     ]
     * }
     */
    @GetMapping("/list")
    public Map<String, Object> listFiles() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> files = minioService.listObjects();
            
            result.put("success", true);
            result.put("data", files);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取文件列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 列出指定前缀的文件
     *
     * @param prefix 前缀
     * @return 文件列表
     * 
     * 示例请求：
     * GET /api/minio/list-by-prefix?prefix=test/
     * 
     * 响应示例：
     * {
     *     "success": true,
     *     "data": [
     *         {
     *             "objectName": "test/example.jpg",
     *             "size": 12345,
     *             "lastModified": "2023-06-15T10:30:00Z",
     *             "etag": "\"a1b2c3d4e5f6\"",
     *             "isDir": false
     *         },
     *         {
     *             "objectName": "test/example.txt",
     *             "size": 25,
     *             "lastModified": "2023-06-15T10:35:00Z",
     *             "etag": "\"g7h8i9j0k1l2\"",
     *             "isDir": false
     *         }
     *     ]
     * }
     */
    @GetMapping("/list-by-prefix")
    public Map<String, Object> listFilesByPrefix(@RequestParam("prefix") String prefix) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> files = minioService.listObjectsByPrefix(prefix);
            
            result.put("success", true);
            result.put("data", files);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取文件列表失败: " + e.getMessage());
        }
        return result;
    }
} 