package com.example.kefu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值记录实体类
 */
@Data
@TableName("recharge_record")
public class RechargeRecord {
    
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 充值金额
     */
    private BigDecimal amount;
    
    /**
     * 充值前余额
     */
    private BigDecimal beforeBalance;
    
    /**
     * 充值后余额
     */
    private BigDecimal afterBalance;
    
    /**
     * 充值状态：0-失败，1-成功
     */
    private Integer status;
    
    /**
     * 充值方式：1-支付宝，2-微信，3-银行卡
     */
    private Integer payType;
    
    /**
     * 交易流水号
     */
    private String tradeNo;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 