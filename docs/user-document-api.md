# 用户文档管理API文档

本文档描述了用户文档管理相关的API接口，包括文档的上传、下载、查询、更新和删除功能。

## 接口概览

| 接口名称 | URL | 方法 | 描述 |
| --- | --- | --- | --- |
| 上传文档 | `/api/documents/upload` | POST | 上传文档并存储 |
| 更新文档信息 | `/api/documents/{id}` | PUT | 更新文档的名称和描述 |
| 删除文档 | `/api/documents/{id}` | DELETE | 逻辑删除文档 |
| 获取文档详情 | `/api/documents/{id}` | GET | 获取单个文档的详细信息 |
| 获取用户文档列表 | `/api/documents/user/{userId}` | GET | 获取指定用户的文档列表 |
| 获取产品文档列表 | `/api/documents/product/{productId}` | GET | 获取指定产品的文档列表 |
| 下载文档 | `/api/documents/download/{id}` | GET | 下载指定文档 |

## 详细接口说明

### 1. 上传文档

- **URL**: `/api/documents/upload`
- **方法**: `POST`
- **Content-Type**: `multipart/form-data`
- **描述**: 上传文档到MinIO存储，并在数据库中记录文档信息
- **请求参数**:

| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| file | File | 是 | 要上传的文件 |
| userId | Long | 是 | 用户ID |
| productId | Long | 是 | 产品ID |
| productName | String | 否 | 产品名称 |
| shopId | Long | 否 | 店铺ID |
| shopName | String | 否 | 店铺名称 |
| documentName | String | 否 | 文档名称，不提供则使用原始文件名 |
| description | String | 否 | 文档描述 |

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": 1  // 返回文档ID
}
```

- **前端调用示例**:

```jsx
// React示例
import { Upload, Button, message } from 'antd';
import { UploadOutlined } from '@ant-design/icons';

const props = {
  name: 'file',
  action: '/api/documents/upload',
  data: {
    userId: 1,
    productId: 2,
    productName: '智能手机X1',
    shopId: 3,
    shopName: '电子科技旗舰店',
    documentName: '自定义文档名称',
    description: '文档描述'
  },
  onChange(info) {
    if (info.file.status === 'done') {
      message.success(`${info.file.name} 上传成功`);
    } else if (info.file.status === 'error') {
      message.error(`${info.file.name} 上传失败`);
    }
  },
};

ReactDOM.render(
  <Upload {...props}>
    <Button icon={<UploadOutlined />}>点击上传</Button>
  </Upload>,
  mountNode,
);
```

### 2. 更新文档信息

- **URL**: `/api/documents/{id}`
- **方法**: `PUT`
- **Content-Type**: `application/json`
- **描述**: 更新指定ID的文档信息（仅支持更新文档名称和描述）
- **路径参数**:

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| id | Long | 文档ID |

- **请求体**:

```json
{
  "documentName": "更新后的文档名称",
  "description": "更新后的文档描述"
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": true
}
```

- **前端调用示例**:

```javascript
fetch('/api/documents/1', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    documentName: '更新后的产品说明书',
    description: '更新后的产品使用说明'
  }),
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### 3. 删除文档

- **URL**: `/api/documents/{id}`
- **方法**: `DELETE`
- **描述**: 逻辑删除指定ID的文档（仅在数据库中标记为已删除，不会物理删除文件）
- **路径参数**:

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| id | Long | 文档ID |

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": true
}
```

- **前端调用示例**:

```javascript
fetch('/api/documents/1', {
  method: 'DELETE',
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### 4. 获取文档详情

- **URL**: `/api/documents/{id}`
- **方法**: `GET`
- **描述**: 获取指定ID的文档详细信息
- **路径参数**:

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| id | Long | 文档ID |

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "productId": 2,
    "productName": "智能手机X1",
    "shopId": 3,
    "shopName": "电子科技旗舰店",
    "documentName": "产品说明书.pdf",
    "documentType": "pdf",
    "fileSize": 1024000,
    "fileUrl": "http://minio-server:9000/document-bucket/1/2023/12/25/abcdef1234567890.pdf?X-Amz-Algorithm=...",
    "description": "产品使用说明文档",
    "createTime": "2023-12-25T10:00:00",
    "updateTime": "2023-12-25T10:00:00"
  }
}
```

- **前端调用示例**:

```javascript
fetch('/api/documents/1')
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### 5. 获取用户的文档列表

- **URL**: `/api/documents/user/{userId}`
- **方法**: `GET`
- **描述**: 获取指定用户ID的文档列表
- **路径参数**:

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| userId | Long | 用户ID |

- **查询参数**:

| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页大小，默认为10 |

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "productId": 2,
        "productName": "智能手机X1",
        "shopId": 3,
        "shopName": "电子科技旗舰店",
        "documentName": "产品说明书.pdf",
        "documentType": "pdf",
        "fileSize": 1024000,
        "fileUrl": "http://minio-server:9000/document-bucket/1/2023/12/25/abcdef1234567890.pdf?X-Amz-Algorithm=...",
        "description": "产品使用说明文档",
        "createTime": "2023-12-25T10:00:00",
        "updateTime": "2023-12-25T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

- **前端调用示例**:

```javascript
fetch('/api/documents/user/1?page=1&size=10')
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### 6. 获取产品的文档列表

- **URL**: `/api/documents/product/{productId}`
- **方法**: `GET`
- **描述**: 获取指定产品ID的文档列表
- **路径参数**:

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| productId | Long | 产品ID |

- **查询参数**:

| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页大小，默认为10 |

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "productId": 2,
        "productName": "智能手机X1",
        "shopId": 3,
        "shopName": "电子科技旗舰店",
        "documentName": "产品说明书.pdf",
        "documentType": "pdf",
        "fileSize": 1024000,
        "fileUrl": "http://minio-server:9000/document-bucket/1/2023/12/25/abcdef1234567890.pdf?X-Amz-Algorithm=...",
        "description": "产品使用说明文档",
        "createTime": "2023-12-25T10:00:00",
        "updateTime": "2023-12-25T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

- **前端调用示例**:

```javascript
fetch('/api/documents/product/2?page=1&size=10')
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### 7. 下载文档

- **URL**: `/api/documents/download/{id}`
- **方法**: `GET`
- **描述**: 下载指定ID的文档
- **路径参数**:

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| id | Long | 文档ID |

- **响应**: 文件流，直接下载文件

- **前端调用示例**:

```javascript
// 方法1：直接在浏览器中打开下载链接
window.location.href = '/api/documents/download/1';

// 方法2：使用HTML链接
// <a href="/api/documents/download/1" target="_blank">下载文档</a>

// 方法3：使用fetch API并处理二进制流
fetch('/api/documents/download/1')
.then(response => response.blob())
.then(blob => {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '文档名称.pdf'; // 可以从之前的API获取文档名称
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
})
.catch(error => console.error('Error:', error));
```

## 技术实现说明

### 数据库设计

用户文档表（user_document）结构：

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| id | bigint | 主键ID |
| user_id | bigint | 用户ID |
| product_id | bigint | 产品ID |
| product_name | varchar(255) | 产品名称 |
| shop_id | bigint | 店铺ID |
| shop_name | varchar(255) | 店铺名称 |
| document_name | varchar(255) | 文档名称 |
| document_type | varchar(50) | 文档类型（扩展名） |
| file_size | bigint | 文件大小（字节） |
| file_url | varchar(1000) | 文件URL |
| description | varchar(500) | 文档描述 |
| is_deleted | tinyint | 是否删除（0-未删除，1-已删除） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 文件存储流程

1. 用户上传文件时，系统会先将文件上传到MinIO存储服务。
2. 文件在MinIO中的存储路径格式为：`userId/yyyy/MM/dd/UUID.extension`，例如：`1/2023/12/25/abcdef1234567890.pdf`。
3. 上传成功后，系统会在数据库中记录文件的元数据信息，包括文件名、类型、大小、URL等。
4. 文件的URL是带有时效性的预签名URL，默认有效期为1小时（3600秒）。

### 文件下载流程

1. 用户请求下载文件时，系统会根据文件ID从数据库中获取文件信息。
2. 系统从文件URL中提取MinIO的桶名称和对象名称。
3. 系统从MinIO中获取文件流，并通过HTTP响应返回给用户。
4. 文件会以附件形式下载，文件名为数据库中记录的文档名称。

### 文件删除说明

为了保证数据安全和可恢复性，系统采用逻辑删除方式：
1. 删除操作只会在数据库中将文档的`is_deleted`字段标记为`1`。
2. 被标记为已删除的文档不会在查询结果中显示。
3. MinIO中的实际文件不会被物理删除，以便于后续可能的数据恢复。

## 前端集成指南

### 文件上传组件

建议使用支持进度显示的文件上传组件，例如：

```jsx
// React示例
import { Upload, Button, message } from 'antd';
import { UploadOutlined } from '@ant-design/icons';

const props = {
  name: 'file',
  action: '/api/documents/upload',
  data: {
    userId: 1,
    productId: 2,
    productName: '智能手机X1',
    shopId: 3,
    shopName: '电子科技旗舰店',
    documentName: '自定义文档名称',
    description: '文档描述'
  },
  onChange(info) {
    if (info.file.status === 'done') {
      message.success(`${info.file.name} 上传成功`);
    } else if (info.file.status === 'error') {
      message.error(`${info.file.name} 上传失败`);
    }
  },
};

ReactDOM.render(
  <Upload {...props}>
    <Button icon={<UploadOutlined />}>点击上传</Button>
  </Upload>,
  mountNode,
);
```

### 文件列表展示

根据文件类型显示不同的图标，并提供预览、下载、删除等操作按钮：

```jsx
// React示例
import { Table, Button, Space, Tooltip } from 'antd';
import { DownloadOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';

const columns = [
  {
    title: '文档名称',
    dataIndex: 'documentName',
    key: 'documentName',
  },
  {
    title: '文件类型',
    dataIndex: 'documentType',
    key: 'documentType',
    render: type => <Tag color="blue">{type.toUpperCase()}</Tag>,
  },
  {
    title: '文件大小',
    dataIndex: 'fileSize',
    key: 'fileSize',
    render: size => `${(size / 1024 / 1024).toFixed(2)} MB`,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
    render: (_, record) => (
      <Space size="middle">
        <Tooltip title="预览">
          <Button icon={<EyeOutlined />} onClick={() => window.open(record.fileUrl)} />
        </Tooltip>
        <Tooltip title="下载">
          <Button icon={<DownloadOutlined />} onClick={() => window.location.href = `/api/documents/download/${record.id}`} />
        </Tooltip>
        <Tooltip title="删除">
          <Button danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)} />
        </Tooltip>
      </Space>
    ),
  },
];

// 获取用户文档列表
const fetchUserDocuments = async (userId, page = 1, size = 10) => {
  try {
    const response = await fetch(`/api/documents/user/${userId}?page=${page}&size=${size}`);
    const data = await response.json();
    return data.data;
  } catch (error) {
    console.error('获取文档列表失败:', error);
    return { records: [], total: 0 };
  }
};

// 删除文档
const handleDelete = async (id) => {
  try {
    const response = await fetch(`/api/documents/${id}`, {
      method: 'DELETE',
    });
    const data = await response.json();
    if (data.code === 0) {
      message.success('文档删除成功');
      // 刷新列表
      fetchUserDocuments(userId);
    } else {
      message.error(`删除失败: ${data.message}`);
    }
  } catch (error) {
    message.error('删除失败，请稍后重试');
  }
};
```

### 文件类型限制

建议在前端对上传文件的类型和大小进行初步验证：

```javascript
// 文件类型和大小限制
const beforeUpload = (file) => {
  const isAllowedType = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'].includes(file.type);
  if (!isAllowedType) {
    message.error('只能上传PDF或Word文档!');
    return Upload.LIST_IGNORE;
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件必须小于10MB!');
    return Upload.LIST_IGNORE;
  }
  
  return true;
};
```

## 错误码说明

| 错误码 | 描述 |
| --- | --- |
| 0 | 成功 |
| 1001 | 参数错误 |
| 1002 | 文件不存在 |
| 1003 | 文件上传失败 |
| 1004 | 文件下载失败 |
| 1005 | 文件类型不支持 |
| 1006 | 文件大小超限 |
| 2001 | 用户不存在 |
| 2002 | 产品不存在 |
| 5000 | 服务器内部错误 | 