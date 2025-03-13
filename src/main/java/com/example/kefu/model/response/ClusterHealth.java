package com.example.kefu.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 集群健康状态响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterHealth {
    
    /**
     * 集群名称
     */
    private String clusterName;
    
    /**
     * 集群状态：green, yellow, red
     */
    private String status;
    
    /**
     * 节点数量
     */
    private int numberOfNodes;
    
    /**
     * 数据节点数量
     */
    private int numberOfDataNodes;
    
    /**
     * 活跃的主分片数量
     */
    private int activePrimaryShards;
    
    /**
     * 活跃的分片数量
     */
    private int activeShards;
    
    /**
     * 正在迁移的分片数量
     */
    private int relocatingShards;
    
    /**
     * 正在初始化的分片数量
     */
    private int initializingShards;
    
    /**
     * 未分配的分片数量
     */
    private int unassignedShards;
} 