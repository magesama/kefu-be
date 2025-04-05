package com.example.kefu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kefu.entity.Product;
import com.example.kefu.mapper.ProductMapper;
import com.example.kefu.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public boolean createProduct(Product product) {
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        product.setCreateTime(now);
        product.setUpdateTime(now);
        
        // 设置默认值
        if (product.getStatus() == null) {
            product.setStatus(1); // 默认上架
        }
        if (product.getIsDeleted() == null) {
            product.setIsDeleted(0); // 默认未删除
        }
        
        return productMapper.insert(product) > 0;
    }
    
    @Override
    public boolean updateProduct(Product product) {
        // 设置更新时间
        product.setUpdateTime(LocalDateTime.now());
        
        return productMapper.updateById(product) > 0;
    }
    
    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }
    
    @Override
    public boolean deleteProduct(Long id) {
        // 逻辑删除，将isDeleted设置为1
        Product product = new Product();
        product.setId(id);
        product.setIsDeleted(1);
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
    }
    
    @Override
    public List<Product> getProductsByUserId(Long userId) {
        return productMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Product> getProductsByCategory(String category) {
        return productMapper.selectByCategory(category);
    }
    
    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }
    
    @Override
    public List<Product> getProductsByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return productMapper.selectByPage(offset, pageSize);
    }
    

} 