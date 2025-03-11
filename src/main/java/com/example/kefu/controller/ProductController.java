package com.example.kefu.controller;

import com.example.kefu.entity.Product;
import com.example.kefu.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
    
    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }
    
    @GetMapping("/price/{maxPrice}")
    public List<Product> getProductsByMaxPrice(@PathVariable BigDecimal maxPrice) {
        return productService.getProductsByMaxPrice(maxPrice);
    }
    
    @PostMapping
    public Map<String, Object> createProduct(@RequestBody Product product) {
        Map<String, Object> result = new HashMap<>();
        boolean success = productService.saveProduct(product);
        result.put("success", success);
        if (success) {
            result.put("message", "产品创建成功");
            result.put("product", product);
        } else {
            result.put("message", "产品创建失败");
        }
        return result;
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Map<String, Object> result = new HashMap<>();
        product.setId(id);
        boolean success = productService.updateProduct(product);
        result.put("success", success);
        if (success) {
            result.put("message", "产品更新成功");
        } else {
            result.put("message", "产品更新失败");
        }
        return result;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteProduct(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = productService.deleteProduct(id);
        result.put("success", success);
        if (success) {
            result.put("message", "产品删除成功");
        } else {
            result.put("message", "产品删除失败");
        }
        return result;
    }
    
    @GetMapping("/init-test-data")
    public Map<String, Object> initTestData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建测试产品
            Product product1 = new Product();
            product1.setName("测试产品A");
            product1.setPrice(new BigDecimal("88.88"));
            product1.setDescription("这是通过API创建的测试产品A");
            product1.setStock(200);
            product1.setCategory("测试类别");
            product1.setStatus(1);
            productService.saveProduct(product1);
            
            Product product2 = new Product();
            product2.setName("测试产品B");
            product2.setPrice(new BigDecimal("188.88"));
            product2.setDescription("这是通过API创建的测试产品B");
            product2.setStock(100);
            product2.setCategory("测试类别");
            product2.setStatus(1);
            productService.saveProduct(product2);
            
            result.put("success", true);
            result.put("message", "测试数据初始化成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试数据初始化失败: " + e.getMessage());
        }
        
        return result;
    }
} 