# 智能客服问答接口文档

## 问答接口（Answer API）

### 基本信息
- **接口URL**: `/api/chat/answer`
- **请求方式**: POST
- **接口描述**: 智能问答接口，支持闲聊和产品相关问题的回答
- **Content-Type**: application/json

### 请求参数

#### 请求体（Request Body）
```json
{
    "question": "string",  // 必填，用户的问题内容
    "userId": "number"     // 可选，用户ID，用于后续数据分析和个性化
}
```

#### 参数说明
| 参数名   | 类型   | 必填 | 说明                                                |
|----------|--------|------|---------------------------------------------------|
| question | string | 是   | 用户问题内容，不能为空                              |
| userId   | number | 否   | 用户ID，用于记录用户会话历史和个性化推荐，可以为空   |

### 响应结果

#### 成功响应
```json
{
    "answer": "string"  // 系统回答的内容
}
```

#### 响应参数说明
| 参数名 | 类型   | 说明                |
|--------|--------|-------------------|
| answer | string | AI助手的回答内容    |

### 调用示例

#### 请求示例
```javascript
// 使用 fetch 调用
fetch('/api/chat/answer', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        question: "这个产品怎么用？",
        userId: 123
    })
})
.then(response => response.json())
.then(data => console.log(data.answer));

// 使用 axios 调用
axios.post('/api/chat/answer', {
    question: "这个产品怎么用？",
    userId: 123
})
.then(response => {
    console.log(response.data.answer);
})
.catch(error => {
    console.error('Error:', error);
});
```

#### 响应示例
```json
{
    "answer": "根据产品说明书，使用步骤如下：\n1. 打开包装盒，取出产品\n2. 按住电源键3秒开机\n3. 根据屏幕提示完成初始设置\n4. 连接手机蓝牙即可使用"
}
```

### 错误码说明
| 错误码 | 说明                     | 处理建议                        |
|--------|------------------------|--------------------------------|
| 400    | 请求参数错误            | 检查请求参数格式是否正确         |
| 401    | 未授权                 | 检查用户是否登录                |
| 500    | 服务器内部错误          | 请稍后重试或联系技术支持         |

### 注意事项
1. 问题内容（question）不能为空，否则会返回400错误
2. 建议在发送请求时增加错误处理，处理可能的网络问题和服务器错误
3. 接口响应时间可能会因为问题复杂度不同而变化，建议在前端添加loading状态
4. 建议在UI上展示打字机效果，提升用户体验

### 前端实现建议
1. **Loading 状态处理**
```javascript
const [loading, setLoading] = useState(false);

const handleQuestion = async () => {
    setLoading(true);
    try {
        const response = await axios.post('/api/chat/answer', {
            question,
            userId
        });
        setAnswer(response.data.answer);
    } catch (error) {
        // 错误处理
        setError('抱歉，服务出现了点问题，请稍后重试');
    } finally {
        setLoading(false);
    }
};
```

2. **打字机效果实现**
```javascript
const TypeWriter = ({ text }) => {
    const [displayText, setDisplayText] = useState('');
    
    useEffect(() => {
        let index = 0;
        const timer = setInterval(() => {
            if (index < text.length) {
                setDisplayText((prev) => prev + text.charAt(index));
                index++;
            } else {
                clearInterval(timer);
            }
        }, 50); // 每50ms显示一个字
        
        return () => clearInterval(timer);
    }, [text]);
    
    return <div>{displayText}</div>;
};
```

3. **错误处理**
```javascript
const handleError = (error) => {
    if (error.response) {
        switch (error.response.status) {
            case 400:
                setError('请输入有效的问题内容');
                break;
            case 401:
                setError('请先登录后再提问');
                break;
            case 500:
                setError('服务器暂时无法处理您的请求，请稍后重试');
                break;
            default:
                setError('抱歉，服务出现了点问题，请稍后重试');
        }
    } else if (error.request) {
        setError('网络连接失败，请检查您的网络设置');
    } else {
        setError('发生未知错误，请稍后重试');
    }
};
```

4. **防抖处理**
```javascript
const debouncedAsk = _.debounce((question) => {
    handleQuestion(question);
}, 500); // 500ms防抖
```

### 更新历史
| 版本号 | 更新时间 | 更新内容 |
|--------|----------|----------|
| v1.0   | 2024-03-xx | 接口首次发布 | 