package com.example.kefu.controller;

import com.example.kefu.entity.Product;
import com.example.kefu.model.response.ApiResponse;
import com.example.kefu.service.ProductService;
import com.example.kefu.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 产品控制器
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ElasticsearchService elasticsearchService;
    
    /**
     * 创建产品
     *
     * @param product 产品信息
     * @return API响应
     */
    @PostMapping
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        boolean success = productService.createProduct(product);
        if (success) {
            return ApiResponse.success(product);
        } else {
            return ApiResponse.error(500, "创建产品失败");
        }
    }
    
    /**
     * 更新产品
     *
     * @param id 产品ID
     * @param product 产品信息
     * @return API响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        boolean success = productService.updateProduct(product);
        if (success) {
            return ApiResponse.success(true);
        } else {
            return ApiResponse.error(500, "更新产品失败");
        }
    }
    
    /**
     * 根据ID查询产品
     *
     * @param id 产品ID
     * @return API响应
     */
    @GetMapping("/{id}")
    public ApiResponse<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ApiResponse.success(product);
        } else {
            return ApiResponse.error(404, "产品不存在");
        }
    }
    
    /**
     * 根据ID删除产品
     *
     * @param id 产品ID
     * @return API响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteProduct(@PathVariable Long id) {
        boolean success = productService.deleteProduct(id);
        if (success) {
            return ApiResponse.success(true);
        } else {
            return ApiResponse.error(500, "删除产品失败");
        }
    }
    
    /**
     * 根据用户ID查询产品列表
     *
     * @param userId 用户ID
     * @return API响应
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Product>> getProductsByUserId(@PathVariable Long userId) {
        List<Product> products = productService.getProductsByUserId(userId);
        return ApiResponse.success(products);
    }
    
    /**
     * 根据分类查询产品列表
     *
     * @param category 产品分类
     * @return API响应
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ApiResponse.success(products);
    }
    
    /**
     * 查询所有产品
     *
     * @return API响应
     */
    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ApiResponse.success(products);
    }
    
    /**
     * 分页查询产品
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return API响应
     */
    @GetMapping("/page")
    public ApiResponse<List<Product>> getProductsByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Product> products = productService.getProductsByPage(pageNum, pageSize);
        return ApiResponse.success(products);
    }
    
    /**
     * 获取产品列表（仅包含ID和名称）
     * 
     * @return 产品列表，格式为 {label: "产品名称", value: "产品ID"}
     */
    @GetMapping("/options")
    public ApiResponse<List<Map<String, Object>>> getProductOptions() {
        try {
            List<Product> products = productService.getAllProducts();
            List<Map<String, Object>> options = new ArrayList<>();
            
            for (Product product : products) {
                Map<String, Object> option = new HashMap<>();
                option.put("label", product.getName());
                option.put("value", product.getId().toString());
                options.add(option);
            }
            
            return ApiResponse.success(options);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取产品选项列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取产品的问答对
     * 
     * @param productId 产品ID
     * @return 问答对列表，每个问答对包含question和answer字段
     */
    @GetMapping("/{productId}/qa")
    public ApiResponse<List<Map<String, String>>> getProductQA(@PathVariable Long productId) {
        try {
            // 直接通过产品ID查询ES中的问答对
            Map<String, Object> searchResult = elasticsearchService.searchByProductId(
                "qa_vectors",                          // 索引名
                productId,                             // 产品ID
                100,                                   // 返回结果数量
                Arrays.asList("question", "answer")    // 只包含问题和答案字段
            );
            
            // 提取搜索结果
            List<Map<String, String>> qaList = new ArrayList<>();
            
            if (searchResult.containsKey("body")) {
                Map<String, Object> body = (Map<String, Object>) searchResult.get("body");
                if (body.containsKey("hits")) {
                    Map<String, Object> hits = (Map<String, Object>) body.get("hits");
                    List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");
                    
                    for (Map<String, Object> hit : hitsList) {
                        Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                        Map<String, String> qa = new HashMap<>();
                        qa.put("question", (String) source.get("question"));
                        qa.put("answer", (String) source.get("answer"));
                        qaList.add(qa);
                    }
                }
            }
            
            return ApiResponse.success(qaList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "获取产品问答对失败: " + e.getMessage());
        }
    }
} 