package com.example.kefu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.kefu.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    @Select("SELECT * FROM product WHERE category = #{category}")
    List<Product> findByCategory(@Param("category") String category);
    
    @Select("SELECT * FROM product WHERE price <= #{maxPrice}")
    List<Product> findByPriceLessThan(@Param("maxPrice") BigDecimal maxPrice);
} 