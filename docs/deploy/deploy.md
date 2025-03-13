# 智能客服系统部署说明

## 1. 环境要求

### 1.1 硬件要求

- **CPU**: 4核心及以上
- **内存**: 8GB及以上
- **存储**: 50GB及以上

### 1.2 软件要求

- **操作系统**: Linux/Windows/MacOS
- **JDK**: 1.8
- **MySQL**: 8.0
- **ElasticSearch**: 7.10.0
- **MinIO**: RELEASE.2022-01-04T07-41-07Z

## 2. 环境安装

### 2.1 JDK安装

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-8-jdk

# CentOS/RHEL
sudo yum install java-1.8.0-openjdk

# 验证安装
java -version
```

### 2.2 MySQL安装

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install mysql-server-8.0

# CentOS/RHEL
sudo yum install mysql-server

# 启动MySQL
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 安全配置
sudo mysql_secure_installation
```

### 2.3 ElasticSearch安装

```bash
# 下载ElasticSearch 7.10.0
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.10.0-linux-x86_64.tar.gz
tar -xzf elasticsearch-7.10.0-linux-x86_64.tar.gz
cd elasticsearch-7.10.0/

# 修改配置
vim config/elasticsearch.yml
# 添加以下配置
# network.host: 0.0.0.0
# discovery.type: single-node

# 启动ElasticSearch
./bin/elasticsearch -d

# 安装中文分词器
./bin/elasticsearch-plugin install analysis-smartcn
```

### 2.4 MinIO安装

```bash
# 下载MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/archive/minio.RELEASE.2022-01-04T07-41-07Z
chmod +x minio.RELEASE.2022-01-04T07-41-07Z

# 启动MinIO
mkdir -p /data/minio
./minio.RELEASE.2022-01-04T07-41-07Z server /data/minio --console-address ":9001"
```

## 3. 数据库初始化

### 3.1 创建数据库

```sql
CREATE DATABASE kefu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3.2 执行初始化脚本

```bash
# 使用MySQL客户端执行初始化脚本
mysql -u root -p kefu < docs/database/mysql/init.sql
```

## 4. ElasticSearch索引初始化

### 4.1 创建日志索引

```bash
# 创建日志索引
curl -X PUT "localhost:9200/log_index" -H "Content-Type: application/json" -d @docs/database/elasticsearch/indices.json
```

### 4.2 创建知识库索引

```bash
# 创建知识库索引
curl -X PUT "localhost:9200/knowledge_base_index" -H "Content-Type: application/json" -d @docs/database/elasticsearch/indices.json
```

## 5. MinIO配置

### 5.1 创建存储桶

1. 访问MinIO控制台: http://localhost:9001
2. 使用默认凭据登录: minioadmin/minioadmin
3. 创建名为"kefu"的存储桶

## 6. 应用部署

### 6.1 配置文件

创建 `application.yml` 文件:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kefu?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

elasticsearch:
  host: localhost
  port: 9200
  username: elastic
  password: elastic
  connectTimeout: 5000
  socketTimeout: 60000
  connectionRequestTimeout: 5000
  maxConnTotal: 100
  maxConnPerRoute: 100
  indices:
    log: log_index
    knowledge: knowledge_base_index

minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: kefu

dashscope:
  apiKey: your_api_key
  model:
    text: qwen-max
    embedding: text-embedding-v2
  vectorDimension: 768

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000  # 24小时
```

### 6.2 打包应用

```bash
# 使用Maven打包
mvn clean package -DskipTests
```

### 6.3 运行应用

```bash
# 运行应用
java -jar target/kefu-0.0.1-SNAPSHOT.jar
```

## 7. 验证部署

### 7.1 验证API

```bash
# 验证用户注册API
curl -X POST "http://localhost:8080/api/user/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"test_user","password":"password123","email":"test@example.com"}'
```

### 7.2 验证日志

```bash
# 查看应用日志
tail -f logs/kefu.log
```

## 8. 常见问题

### 8.1 端口占用

如果8080端口被占用，可以在 `application.yml` 中修改 `server.port` 配置。

### 8.2 内存不足

如果应用启动时出现内存不足的错误，可以调整JVM参数:

```bash
java -Xms512m -Xmx2g -jar target/kefu-0.0.1-SNAPSHOT.jar
```

### 8.3 ElasticSearch连接失败

检查ElasticSearch是否正常运行:

```bash
curl -X GET "localhost:9200/_cluster/health"
```

### 8.4 MinIO连接失败

检查MinIO是否正常运行:

```bash
curl -X GET "localhost:9000/minio/health/live"
```

## 9. 性能优化

### 9.1 JVM优化

```bash
java -server -Xms2g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar target/kefu-0.0.1-SNAPSHOT.jar
```

### 9.2 数据库优化

- 为频繁查询的字段添加索引
- 配置合适的连接池大小
- 定期执行OPTIMIZE TABLE操作

### 9.3 ElasticSearch优化

- 调整分片数量和副本数量
- 配置合适的堆内存大小
- 使用SSD存储