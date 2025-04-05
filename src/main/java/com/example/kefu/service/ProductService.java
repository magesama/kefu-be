package com.example.kefu.service;

import com.example.kefu.entity.Product;

import java.util.List;

/**
 * 产品服务接口
 */
public interface ProductService {
    
    /**
     * 创建产品
     *
     * @param product 产品信息
     * @return 创建成功返回true，否则返回false
     */
    boolean createProduct(Product product);
    
    /**
     * 更新产品
     *
     * @param product 产品信息
     * @return 更新成功返回true，否则返回false
     */
    boolean updateProduct(Product product);
    
    /**
     * 根据ID查询产品
     *
     * @param id 产品ID
     * @return 产品信息
     */
    Product getProductById(Long id);
    
    /**
     * 根据ID删除产品（逻辑删除）
     *
     * @param id 产品ID
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteProduct(Long id);
    
    /**
     * 根据用户ID查询产品列表
     *
     * @param userId 用户ID
     * @return 产品列表
     */
    List<Product> getProductsByUserId(Long userId);
    
    /**
     * 根据分类查询产品列表
     *
     * @param category 产品分类
     * @return 产品列表
     */
    List<Product> getProductsByCategory(String category);
    
    /**
     * 查询所有产品
     *
     * @return 产品列表
     */
    List<Product> getAllProducts();
    
    /**
     * 分页查询产品
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 产品列表
     */
    List<Product> getProductsByPage(int pageNum, int pageSize);
} 