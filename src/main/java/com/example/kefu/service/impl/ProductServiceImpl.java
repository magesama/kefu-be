package com.example.kefu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kefu.entity.Product;
import com.example.kefu.mapper.ProductMapper;
import com.example.kefu.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }
    
    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectList(null);
    }
    
    @Override
    public List<Product> getProductsByCategory(String category) {
        return productMapper.findByCategory(category);
    }
    
    @Override
    public List<Product> getProductsByMaxPrice(BigDecimal maxPrice) {
        return productMapper.findByPriceLessThan(maxPrice);
    }
    
    @Override
    public boolean saveProduct(Product product) {
        return productMapper.insert(product) > 0;
    }
    
    @Override
    public boolean updateProduct(Product product) {
        return productMapper.updateById(product) > 0;
    }
    
    @Override
    public boolean deleteProduct(Long id) {
        return productMapper.deleteById(id) > 0;
    }
} 