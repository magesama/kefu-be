package com.example.kefu.service;

import com.example.kefu.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    
    Product getProductById(Long id);
    
    List<Product> getAllProducts();
    
    List<Product> getProductsByCategory(String category);
    
    List<Product> getProductsByMaxPrice(BigDecimal maxPrice);
    
    boolean saveProduct(Product product);
    
    boolean updateProduct(Product product);
    
    boolean deleteProduct(Long id);
} 