# 智能客服系统设计文档

## 项目概述
本项目是一个基于Spring Boot的智能客服系统，集成了文本大模型与向量大模型，通过ElasticSearch、MinIO和MySQL实现知识库管理、用户管理和智能问答功能。

## 目录结构
- database/
  - mysql/
    - init.sql      # MySQL建表脚本
  - elasticsearch/
    - indices.json  # ES索引配置
- api/
  - api.md         # API接口文档
- design/
  - design.md      # 系统设计文档
- deploy/
  - deploy.md      # 部署说明文档

## 技术栈
- Java 8
- Spring Boot 2.7.18
- MySQL 8.0
- ElasticSearch 7.10.0
- MinIO RELEASE.2022-01-04T07-41-07Z
- 阿里云DashScope大模型 