# 聊天问答API接口文档

本文档详细描述了客服系统中的聊天问答API接口，包括向量搜索、文本搜索和混合搜索三种方式的问答接口。这些接口用于根据用户的问题，从知识库中检索相关信息并生成回答。

## 目录

- [接口概述](#接口概述)
- [通用参数说明](#通用参数说明)
- [向量搜索问答接口](#向量搜索问答接口)
- [文本搜索问答接口](#文本搜索问答接口)
- [混合搜索问答接口](#混合搜索问答接口)
- [GET方式混合搜索问答接口](#get方式混合搜索问答接口)
- [错误码说明](#错误码说明)
- [常见问题](#常见问题)
- [文件上传接口](#文件上传接口)
- [指定向量字段搜索问答接口](#指定向量字段搜索问答接口)
- [指定向量字段混合搜索问答接口](#指定向量字段混合搜索问答接口)
- [聊天历史功能](#聊天历史功能)

## 接口概述

系统提供了多种不同的问答接口，分别基于不同的搜索策略：

1. **向量搜索问答接口**：将用户问题转换为向量，通过向量相似度搜索找到最相关的问答对，然后生成回答。
2. **文本搜索问答接口**：使用传统的全文检索方式，通过关键词匹配找到相关的问答对，然后生成回答。
3. **混合搜索问答接口**：结合向量搜索和文本搜索的优点，同时使用两种方式进行搜索，并按权重合并结果，然后生成回答。
4. **GET方式混合搜索问答接口**：功能与混合搜索问答接口相同，但使用GET请求方式，适用于简单场景和URL参数传递。
5. **指定向量字段搜索问答接口**：允许指定使用问题向量或答案向量进行搜索，增加搜索的灵活性。
6. **指定向量字段混合搜索问答接口**：在混合搜索的基础上，允许指定使用问题向量或答案向量进行向量部分的搜索。

所有接口都支持多轮对话功能，可以通过tableId参数来标识对话上下文，系统会自动记录对话历史并在生成回答时考虑历史上下文。

## 通用参数说明

所有问答接口都使用相同的请求参数结构：

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Integer | 是 | 用户ID，用于权限控制和数据隔离 |
| shopName | String | 否 | 店铺名称，用于筛选特定店铺的问答内容 |
| productName | String | 否 | 产品名称，用于筛选特定产品的问答内容 |
| question | String | 是 | 用户提出的问题 |
| tableId | String | 否 | 聊天窗口ID，用于标识多轮对话的上下文，如果不提供则使用userId作为tableId |

## 向量搜索问答接口

### 接口说明

该接口使用向量搜索技术，将用户问题转换为向量，然后在向量空间中查找最相似的问答对，生成回答。

### 请求方式

- **URL**: `/api/chat/answer`
- **Method**: POST
- **Content-Type**: application/json

### 请求参数

```json
{
  "userId": 123,
  "shopName": "某某店铺",
  "productName": "超级牛逼袜子",
  "question": "这款袜子的材质是什么？",
  "tableId": "chat_session_001"
}
```

### 返回结果

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "这款超级牛逼袜子采用纯棉材质，柔软舒适，透气性好，适合各种季节穿着。"
  }
}
```

### 处理流程

1. 接收用户请求，提取userId、shopName、productName和question参数
2. 将question转换为向量
3. 在ES中执行向量相似度搜索，找到相关的问答对
4. 提取相似度高于阈值的文档
5. 构建提示词，包含用户问题和相关的问答信息
6. 调用大模型生成最终答案
7. 返回生成的答案

### 示例代码

```javascript
// 前端请求示例（使用axios）
axios.post('/api/chat/answer', {
  userId: 123,
  shopName: '某某店铺',
  productName: '超级牛逼袜子',
  question: '这款袜子的材质是什么？',
  tableId: 'chat_session_001'
})
.then(response => {
  console.log('回答:', response.data.data.answer);
})
.catch(error => {
  console.error('请求失败:', error);
});
```

## 文本搜索问答接口

### 接口说明

该接口使用传统的全文检索技术，通过关键词匹配在问答库中查找相关内容，生成回答。

### 请求方式

- **URL**: `/api/chat/text-answer`
- **Method**: POST
- **Content-Type**: application/json

### 请求参数

```json
{
  "userId": 123,
  "shopName": "某某店铺",
  "productName": "超级牛逼袜子",
  "question": "这款袜子的材质是什么？",
  "tableId": "chat_session_001"
}
```

### 返回结果

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "这款超级牛逼袜子采用纯棉材质，柔软舒适，透气性好，适合各种季节穿着。"
  }
}
```

### 处理流程

1. 接收用户请求，提取userId、shopName、productName和question参数
2. 在ES中执行文本搜索，找到相关的问答对
3. 提取相似度高于阈值的文档
4. 构建提示词，包含用户问题和相关的问答信息
5. 调用大模型生成最终答案
6. 返回生成的答案

### 示例代码

```javascript
// 前端请求示例（使用axios）
axios.post('/api/chat/text-answer', {
  userId: 123,
  shopName: '某某店铺',
  productName: '超级牛逼袜子',
  question: '这款袜子的材质是什么？',
  tableId: 'chat_session_001'
})
.then(response => {
  console.log('回答:', response.data.data.answer);
})
.catch(error => {
  console.error('请求失败:', error);
});
```

## 混合搜索问答接口

### 接口说明

该接口结合了向量搜索和文本搜索的优点，同时使用两种方式进行搜索，并按权重合并结果，生成回答。这种方式通常能够获得更好的搜索效果。

### 请求方式

```
POST /api/chat/hybrid-answer
```

### 请求参数

请求体为JSON格式，包含以下字段：

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Integer | 是 | 用户ID，用于权限控制和数据隔离 |
| shopName | String | 否 | 店铺名称，用于筛选特定店铺的问答内容 |
| productName | String | 否 | 产品名称，用于筛选特定产品的问答内容 |
| question | String | 是 | 用户提出的问题 |
| tableId | String | 否 | 聊天窗口ID，用于标识多轮对话的上下文，如果不提供则使用userId作为tableId |

### 请求示例

```json
{
  "userId": 1,
  "shopName": "示例店铺",
  "productName": "示例产品",
  "question": "这个产品有什么特点？",
  "tableId": "chat_1"
}
```

### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Object | 响应数据 |
| data.answer | String | 生成的回答内容 |

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "answer": "这个产品的主要特点是：1. 高性能；2. 低功耗；3. 易于使用；4. 兼容性好。"
  }
}
```

## GET方式混合搜索问答接口

### 接口说明

该接口功能与混合搜索问答接口完全相同，但使用GET请求方式，通过URL参数传递请求参数。这种方式适用于简单场景和URL参数传递，便于测试和集成。

### 请求方式

```
GET /api/chat/hybrid-answer-get
```

### 请求参数

请求参数通过URL查询字符串传递：

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Integer | 是 | 用户ID，用于权限控制和数据隔离 |
| shopName | String | 否 | 店铺名称，用于筛选特定店铺的问答内容 |
| productName | String | 否 | 产品名称，用于筛选特定产品的问答内容 |
| question | String | 是 | 用户提出的问题 |
| tableId | String | 否 | 聊天窗口ID，用于标识多轮对话的上下文，如果不提供则使用userId作为tableId |

### 请求示例

```
GET /api/chat/hybrid-answer-get?userId=1&shopName=示例店铺&productName=示例产品&question=这个产品有什么特点？&tableId=chat_1
```

### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Object | 响应数据 |
| data.answer | String | 生成的回答内容 |

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "answer": "这个产品的主要特点是：1. 高性能；2. 低功耗；3. 易于使用；4. 兼容性好。"
  }
}
```

### 注意事项

1. 使用GET请求时，参数会显示在URL中，不适合传输敏感信息
2. URL长度有限制，如果问题内容较长，可能会超出URL长度限制
3. 中文和特殊字符需要进行URL编码

## 错误码说明

| 错误码 | 说明 | 处理建议 |
| ----- | ---- | ------- |
| 400 | 请求参数错误 | 检查请求参数是否完整，userId和question是必填项 |
| 401 | 未授权 | 检查用户是否已登录，token是否有效 |
| 403 | 权限不足 | 检查用户是否有权限访问该接口 |
| 500 | 服务器内部错误 | 联系后端开发人员排查问题 |

## 常见问题

### 1. 如何选择合适的问答接口？

- **向量搜索问答接口**：适合语义理解要求高的场景，能够理解问题的含义而不仅仅是关键词匹配。
- **文本搜索问答接口**：适合关键词匹配场景，计算资源消耗较少，响应速度较快。
- **混合搜索问答接口**：综合了两种方式的优点，适合大多数场景，但计算资源消耗较大。

### 2. 为什么相同的问题可能得到不同的回答？

系统使用大模型生成回答，会根据检索到的相关问答对动态生成回答。即使是相同的问题，如果检索到的相关问答对有所不同，或者大模型的生成结果有随机性，都可能导致回答不同。

### 3. 如何提高回答的准确性？

- 提供更精确的shopName和productName参数，缩小搜索范围
- 使用更清晰、具体的问题描述
- 优先使用混合搜索问答接口，结合向量搜索和文本搜索的优点
- 确保知识库中有足够多的高质量问答对

### 4. 接口响应时间过长怎么办？

- 检查网络连接是否稳定
- 考虑使用文本搜索问答接口，响应速度较快
- 联系后端开发人员，可能需要优化服务器配置或算法实现

### 5. 如何处理敏感问题？

系统会对用户问题进行过滤，如果检测到敏感内容，会拒绝回答或给出适当的提示。如果您需要处理特定领域的敏感问题，请联系后端开发人员进行定制化配置。

### 6. 如何选择合适的向量字段？

- **问题向量（question_vector）**：适合当用户提问的方式与知识库中的问题表述相似时，能够找到语义相似的问题。
- **答案向量（answer_vector）**：适合当用户直接询问某个答案相关的内容时，能够直接匹配到相关答案。
- 在实际应用中，可以尝试两种方式，选择效果更好的一种，或者使用混合搜索接口结合文本搜索获得更全面的结果。

## 文件上传接口

### 接口说明

该接口用于上传文件并提取文件中的文本内容，目前主要支持Excel文件的内容提取。

### 请求方式

- **URL**: `/api/chat/upload`
- **Method**: POST
- **Content-Type**: multipart/form-data

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| file | File | 是 | 要上传的文件，目前主要支持Excel格式 |

### 返回结果

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "文件名: 产品信息.xlsx\n\n提取的内容:\n产品名称: 超级牛逼袜子\n材质: 纯棉\n颜色: 黑色\n尺寸: 均码\n价格: 39.9元\n..."
  }
}
```

### 处理流程

1. 接收用户上传的文件
2. 根据文件类型进行相应处理（目前主要支持Excel文件）
3. 提取文件中的文本内容
4. 返回提取的文本内容

### 示例代码

```javascript
// 前端请求示例（使用FormData）
const formData = new FormData();
formData.append('file', fileInput.files[0]);

axios.post('/api/chat/upload', formData, {
  headers: {
    'Content-Type': 'multipart/form-data'
  }
})
.then(response => {
  console.log('提取的文本:', response.data.data.answer);
})
.catch(error => {
  console.error('文件上传失败:', error);
});
```

## 指定向量字段搜索问答接口

### 接口说明

该接口允许用户指定使用问题向量或答案向量进行搜索，提供更灵活的搜索方式。

### 请求方式

- **URL**: `/api/chat/vector-answer`
- **Method**: POST
- **Content-Type**: application/json

### 请求参数

```json
{
  "userId": 123,
  "shopName": "某某店铺",
  "productName": "超级牛逼袜子",
  "question": "这款袜子的材质是什么？",
  "vectorField": "question_vector",
  "tableId": "chat_session_001"
}
```

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Integer | 是 | 用户ID |
| shopName | String | 否 | 店铺名称 |
| productName | String | 否 | 产品名称 |
| question | String | 是 | 用户问题 |
| vectorField | String | 否 | 向量字段名称，可选值：question_vector（问题向量）或answer_vector（答案向量），默认为question_vector |
| tableId | String | 否 | 聊天窗口ID，用于标识多轮对话的上下文，如果不提供则使用userId作为tableId |

### 返回结果

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "这款超级牛逼袜子采用纯棉材质，柔软舒适，透气性好，适合各种季节穿着。"
  }
}
```

### 处理流程

1. 接收用户请求，提取userId、shopName、productName、question和vectorField参数
2. 将question转换为向量
3. 在ES中执行向量搜索，使用指定的向量字段（question_vector或answer_vector）
4. 提取相似度高于阈值的文档
5. 构建提示词，包含用户问题和相关的问答信息
6. 调用大模型生成最终答案
7. 返回生成的答案

### 示例代码

```javascript
// 前端请求示例（使用axios）
axios.post('/api/chat/vector-answer', {
  userId: 123,
  shopName: '某某店铺',
  productName: '超级牛逼袜子',
  question: '这款袜子的材质是什么？',
  vectorField: 'answer_vector',
  tableId: 'chat_session_001'
})
.then(response => {
  console.log('回答:', response.data.data.answer);
})
.catch(error => {
  console.error('请求失败:', error);
});
```

## 指定向量字段混合搜索问答接口

### 接口说明

该接口结合了向量搜索和文本搜索的优点，同时使用两种方式进行搜索，并按权重合并结果，生成更准确的回答。此接口允许用户指定使用问题向量或答案向量进行搜索。

### 请求方式

- **URL**: `/api/chat/hybrid-vector-answer`
- **Method**: POST
- **Content-Type**: application/json

### 请求参数

```json
{
  "userId": 123,
  "shopName": "某某店铺",
  "productName": "超级牛逼袜子",
  "question": "这款袜子的材质是什么？",
  "vectorField": "answer_vector",
  "tableId": "chat_session_001"
}
```

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Integer | 是 | 用户ID |
| shopName | String | 否 | 店铺名称 |
| productName | String | 否 | 产品名称 |
| question | String | 是 | 用户问题 |
| vectorField | String | 否 | 向量字段名称，可选值：question_vector（问题向量）或answer_vector（答案向量），默认为question_vector |
| tableId | String | 否 | 聊天窗口ID，用于标识多轮对话的上下文，如果不提供则使用userId作为tableId |

### 返回结果

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "这款超级牛逼袜子采用纯棉材质，柔软舒适，透气性好，适合各种季节穿着。"
  }
}
```

### 处理流程

1. 接收用户请求，提取userId、shopName、productName、question和vectorField参数
2. 将question转换为向量
3. 在ES中执行混合搜索（文本搜索权重0.3，向量搜索权重0.7），使用指定的向量字段（question_vector或answer_vector）
4. 提取相似度高于阈值的文档
5. 构建提示词，包含用户问题和相关的问答信息
6. 调用大模型生成最终答案
7. 返回生成的答案

### 示例代码

```javascript
// 前端请求示例（使用axios）
axios.post('/api/chat/hybrid-vector-answer', {
  userId: 123,
  shopName: '某某店铺',
  productName: '超级牛逼袜子',
  question: '这款袜子的材质是什么？',
  vectorField: 'answer_vector'  // 使用答案向量进行搜索
})
.then(response => {
  console.log('回答:', response.data.data.answer);
})
.catch(error => {
  console.error('请求失败:', error);
});
```

## 聊天历史功能

系统支持多轮对话功能，可以记住用户的历史问题和系统的回答，从而在后续对话中提供更加连贯和上下文相关的回答。

### 功能说明

1. **对话上下文管理**：系统通过tableId参数来标识不同的对话上下文，同一个tableId下的所有问答都被视为同一个对话的一部分。
2. **自动记录对话历史**：系统会自动记录每次问答的内容，包括用户的问题和系统的回答。
3. **历史信息融入回答**：在生成回答时，系统会考虑历史对话内容，使回答更加连贯和上下文相关。
4. **过期清理机制**：为了避免历史记录过多占用资源，系统会自动清理24小时未活动的对话历史。

### 使用方法

在请求中添加tableId参数即可启用多轮对话功能：

```json
{
  "userId": 123,
  "question": "这个产品有什么特点？",
  "shopName": "示例店铺",
  "productName": "示例产品",
  "tableId": "chat_session_001"
}
```

如果不提供tableId参数，系统会自动使用userId作为tableId。

### 注意事项

1. 同一个tableId下的所有问答都被视为同一个对话的一部分，请确保不同用户或不同对话使用不同的tableId。
2. 对话历史会在24小时后自动过期，如果需要继续之前的对话，请确保在24小时内发送新的问题。
3. 如果需要清除对话历史，可以使用新的tableId开始新的对话。

### 示例

**第一轮对话请求**：
```json
{
  "userId": 123,
  "question": "这个产品有什么特点？",
  "shopName": "示例店铺",
  "productName": "示例产品",
  "tableId": "chat_session_001"
}
```

**第一轮对话响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "这个产品的主要特点包括：高性能、低功耗、易操作、外观时尚。"
  }
}
```

**第二轮对话请求**（使用相同的tableId）：
```json
{
  "userId": 123,
  "question": "它的价格是多少？",
  "shopName": "示例店铺",
  "productName": "示例产品",
  "tableId": "chat_session_001"
}
```

**第二轮对话响应**（系统会考虑第一轮对话的上下文）：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "这个产品的价格是1999元，目前有优惠活动，购买即送价值200元的配件。"
  }
}
``` 