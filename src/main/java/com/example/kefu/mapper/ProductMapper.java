package com.example.kefu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.kefu.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 插入产品
     *
     * @param product 产品信息
     * @return 影响行数
     */
    int insert(Product product);
    
    /**
     * 根据ID更新产品
     *
     * @param product 产品信息
     * @return 影响行数
     */
    int updateById(Product product);
    
    /**
     * 根据ID查询产品
     *
     * @param id 产品ID
     * @return 产品信息
     */
    Product selectById(Long id);
    
    /**
     * 根据用户ID查询产品列表
     *
     * @param userId 用户ID
     * @return 产品列表
     */
    List<Product> selectByUserId(Long userId);
    
    /**
     * 根据分类查询产品列表
     *
     * @param category 产品分类
     * @return 产品列表
     */
    List<Product> selectByCategory(String category);
    
    /**
     * 查询所有未删除的产品
     *
     * @return 产品列表
     */
    List<Product> selectAll();
    
    /**
     * 分页查询产品
     *
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 产品列表
     */
    List<Product> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT * FROM product WHERE category = #{category}")
    List<Product> findByCategory(@Param("category") String category);
    
    @Select("SELECT * FROM product WHERE price <= #{maxPrice}")
    List<Product> findByPriceLessThan(@Param("maxPrice") BigDecimal maxPrice);
} 