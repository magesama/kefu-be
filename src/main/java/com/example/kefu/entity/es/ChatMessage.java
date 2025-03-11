package com.example.kefu.entity.es;

import lombok.Data;
import org.elasticsearch.common.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

@Data
public class ChatMessage {

    @Nullable
    @JsonProperty("_id")
    private String id;

    private String content;

    private String userId;

    private String sessionId;

    private float[] vector;

    private Date createTime;

    private Integer messageType; // 0: 用户消息, 1: 系统回复
} 