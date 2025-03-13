# QA数据上传API文档

本文档描述了QA数据上传相关的API接口，用于直接将问答数据上传到Elasticsearch索引中，无需通过文件上传。

## 接口概览

| 接口名称 | URL | 方法 | 描述 |
| --- | --- | --- | --- |
| 上传QA数据 | `/api/qa-data/upload` | POST | 上传单个QA数据请求 |
| 批量上传QA数据 | `/api/qa-data/batch-upload` | POST | 批量上传多个QA数据请求 |

## 详细接口说明

### 1. 上传QA数据

- **URL**: `/api/qa-data/upload`
- **方法**: `POST`
- **Content-Type**: `application/json`
- **描述**: 直接上传QA数据到Elasticsearch索引，并自动生成向量
- **请求参数**:

```json
{
  "userId": 123,
  "shopId": 456,
  "shopName": "某某店铺",
  "productId": 789,
  "productName": "超级牛逼袜子",
  "qaList": [
    {
      "question": "这款袜子的材质是什么？",
      "answer": "这款袜子采用优质纯棉材质，柔软舒适，透气性好。"
    },
    {
      "question": "这款袜子有什么颜色可选？",
      "answer": "目前有黑色、白色、灰色和蓝色四种颜色可选。"
    }
  ]
}
```

| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| userId | Long | 是 | 用户ID |
| shopId | Long | 否 | 店铺ID |
| shopName | String | 否 | 店铺名称 |
| productId | Long | 否 | 产品ID |
| productName | String | 否 | 产品名称 |
| qaList | Array | 是 | QA列表，每个元素包含question和answer字段 |

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": 2  // 返回成功上传的QA数量
}
```

- **错误响应示例**:

```json
{
  "code": 400,
  "message": "用户ID不能为空",
  "data": null
}
```

```json
{
  "code": 400,
  "message": "QA列表不能为空",
  "data": null
}
```

```json
{
  "code": 500,
  "message": "所有QA对处理失败: 处理QA对失败: 向量生成失败",
  "data": null
}
```

- **前端调用示例**:

```javascript
// 使用axios发送请求
import axios from 'axios';

const qaData = {
  userId: 123,
  shopId: 456,
  shopName: "某某店铺",
  productId: 789,
  productName: "超级牛逼袜子",
  qaList: [
    {
      question: "这款袜子的材质是什么？",
      answer: "这款袜子采用优质纯棉材质，柔软舒适，透气性好。"
    },
    {
      question: "这款袜子有什么颜色可选？",
      answer: "目前有黑色、白色、灰色和蓝色四种颜色可选。"
    }
  ]
};

axios.post('/api/qa-data/upload', qaData)
  .then(response => {
    console.log('上传成功，成功数量:', response.data.data);
  })
  .catch(error => {
    console.error('上传失败:', error.response.data.message);
  });
```

### 2. 批量上传QA数据

- **URL**: `/api/qa-data/batch-upload`
- **方法**: `POST`
- **Content-Type**: `application/json`
- **描述**: 批量上传多个QA数据请求到Elasticsearch索引
- **请求参数**:

```json
[
  {
    "userId": 123,
    "shopId": 456,
    "shopName": "某某店铺",
    "productId": 789,
    "productName": "超级牛逼袜子",
    "qaList": [
      {
        "question": "这款袜子的材质是什么？",
        "answer": "这款袜子采用优质纯棉材质，柔软舒适，透气性好。"
      }
    ]
  },
  {
    "userId": 123,
    "shopId": 457,
    "shopName": "另一个店铺",
    "productId": 790,
    "productName": "高端T恤",
    "qaList": [
      {
        "question": "这款T恤的尺码如何选择？",
        "answer": "建议根据您的身高体重选择，S适合160cm以下，M适合160-170cm，L适合170-180cm，XL适合180cm以上。"
      }
    ]
  }
]
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": 2  // 返回成功上传的QA总数量
}
```

- **错误响应示例**:

```json
{
  "code": 400,
  "message": "请求列表不能为空",
  "data": null
}
```

```json
{
  "code": 500,
  "message": "所有QA数据处理失败: 处理QA数据请求失败: 用户ID不能为空",
  "data": null
}
```

- **前端调用示例**:

```javascript
// 使用axios发送请求
import axios from 'axios';

const batchQaData = [
  {
    userId: 123,
    shopId: 456,
    shopName: "某某店铺",
    productId: 789,
    productName: "超级牛逼袜子",
    qaList: [
      {
        question: "这款袜子的材质是什么？",
        answer: "这款袜子采用优质纯棉材质，柔软舒适，透气性好。"
      }
    ]
  },
  {
    userId: 123,
    shopId: 457,
    shopName: "另一个店铺",
    productId: 790,
    productName: "高端T恤",
    qaList: [
      {
        question: "这款T恤的尺码如何选择？",
        answer: "建议根据您的身高体重选择，S适合160cm以下，M适合160-170cm，L适合170-180cm，XL适合180cm以上。"
      }
    ]
  }
];

axios.post('/api/qa-data/batch-upload', batchQaData)
  .then(response => {
    console.log('批量上传成功，成功数量:', response.data.data);
  })
  .catch(error => {
    console.error('批量上传失败:', error.response.data.message);
  });
```

## 数据处理流程

1. 接收QA数据请求
2. 验证请求参数的有效性
3. 处理每个QA对：
   - 验证问题和答案不为空
   - 为问题生成向量表示（question_vector）
   - 为答案生成向量表示（answer_vector）
   - 构建完整的ES文档，包含用户信息、产品信息、问答内容和向量
   - 将文档保存到Elasticsearch索引（qa_vectors）
4. 统计成功处理的QA数量
5. 返回处理结果

## 注意事项

1. 上传的QA数据会直接存储到Elasticsearch索引中，无需经过文件上传和解析过程
2. 每个问题和答案都会自动生成对应的向量表示，用于后续的向量搜索
3. 上传成功后，可以立即通过聊天接口（如`/api/chat/vector-answer`）查询相关问题
4. 批量上传接口适合一次性上传大量QA数据，可以提高处理效率
5. 如果部分QA对处理失败，接口会返回成功处理的数量和失败原因
6. 建议每个QA对的问题和答案长度适中，过长的文本可能会影响向量生成的质量 