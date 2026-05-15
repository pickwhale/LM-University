# AI 大模型配置页面教程：以 DeepSeek 为例

本文档面向开发者和系统管理员，说明如何在后台“AI配置”页面接入大模型。示例使用 DeepSeek，系统本身采用“通用 HTTP API 模板”方式，不和某一家模型厂商强绑定。

## 1. 配置前准备

请先确认以下内容已经完成：

- 已执行 `docs/ai-chat-tables.sql`，数据库中已经存在 `ai_model_config`、`ai_chat_conversation`、`ai_chat_message` 三张表。
- 后端服务、后台管理端、学生前台可以正常启动。
- 后台管理员账号可以登录后台。
- 已在 DeepSeek 开放平台创建 API Key。
- 已确认要使用的 DeepSeek 模型名称，例如 `deepseek-v4-flash`。模型名称以后可能会调整，请以 DeepSeek 控制台或官方文档显示的名称为准。

注意：API Key 只应填写在后台配置页面中，不要写入前端代码、公开文档、日志或浏览器本地存储。

## 2. DeepSeek 接口信息

DeepSeek 的常用流式对话接口信息如下：

| 配置项 | 填写内容 |
| --- | --- |
| 接口地址 | `https://api.deepseek.com/chat/completions` |
| 请求方法 | `POST` |
| 鉴权方式 | 请求头 `Authorization: Bearer <API Key>` |
| 流式协议 | `SSE` |
| 文本提取路径 | `choices.0.delta.content` |
| 结束标记 | `[DONE]` |
| 模型示例 | `deepseek-v4-flash` |

虽然 DeepSeek 的接口格式类似 Chat Completions，但本系统不会把配置固定成某个厂商格式，而是通过“请求头模板”“请求体模板”“响应文本路径”来适配。

## 3. 后台配置页面填写示例

进入后台管理端，打开左侧菜单中的“AI配置”，按下面方式填写。

### 基础配置

| 页面字段 | 示例值 | 说明 |
| --- | --- | --- |
| 是否启用 | 建议测试通过后再启用 | 未启用时，学生端会提示 AI 功能未开启 |
| 服务名称 | `DeepSeek` | 仅用于后台识别当前配置 |
| 接口地址 | `https://api.deepseek.com/chat/completions` | DeepSeek 对话接口地址 |
| 请求方法 | `POST` | 当前系统第一版支持 `POST` |
| API Key | 粘贴你的 DeepSeek API Key | 保存后后台不会回显明文，只显示是否已配置 |
| 模型 | `deepseek-v4-flash` | 也可以填写 DeepSeek 后台提供的其他可用模型 |
| Temperature | `0.7` | 值越高回答越发散，推荐先使用 `0.3` 到 `0.8` |
| Max Tokens | `2048` | 控制单次回答最大长度 |
| 流式协议 | `SSE` | DeepSeek 流式输出推荐使用 SSE |
| 文本路径 | `choices.0.delta.content` | 从每段 SSE JSON 中提取增量文本 |
| 结束标记 | `[DONE]` | 收到该标记后结束本次回答 |
| 超时秒数 | `60` | 网络慢或回答较长时可设为 `120` |

### 请求头模板

在“请求头模板”中填写：

```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer {{apiKey}}"
}
```

说明：

- `{{apiKey}}` 会在后端请求大模型前替换成数据库中保存的密钥。
- 不要把真实 API Key 直接写死在模板里。
- 如果其他模型厂商使用自定义请求头，例如 `X-API-Key`，也可以在这里自行调整。

### 请求体模板

在“请求体模板”中填写：

```json
{
  "model": "{{model}}",
  "stream": true,
  "messages": [
    {
      "role": "system",
      "content": "{{systemPrompt}}\n\n数据库上下文：\n{{context}}"
    },
    {
      "role": "user",
      "content": "{{message}}"
    }
  ],
  "temperature": {{temperature}},
  "max_tokens": {{maxTokens}}
}
```

模板变量说明：

| 变量 | 含义 |
| --- | --- |
| `{{message}}` | 学生本次发送的问题 |
| `{{systemPrompt}}` | 后台配置的系统提示词 |
| `{{context}}` | 后端按权限查询出的数据库上下文 |
| `{{model}}` | 后台填写的模型名称 |
| `{{temperature}}` | 后台填写的温度参数 |
| `{{maxTokens}}` | 后台填写的最大输出长度 |
| `{{historyJson}}` | 历史消息 JSON，可用于适配支持自定义历史字段的厂商 |

当前 DeepSeek 示例中，重点使用 `messages`、`stream`、`model`、`temperature`、`max_tokens` 字段。

### 系统提示词示例

可以在“系统提示词”中填写：

```text
你是高校招生咨询助手。你只能根据系统提供的数据库上下文回答问题。
你可以回答学校、专业、公告、资讯、报名、成绩、收藏、录取结果相关问题。
涉及学生个人数据时，只能使用当前登录学生自己的数据，不能猜测或输出其他学生的信息。
如果数据库上下文中没有答案，请明确说明当前系统没有查询到相关信息。
回答请简洁、准确，并优先使用用户当前选择的语言。
```

这段提示词的作用是约束 AI 不要编造数据库中不存在的信息，也不要输出其他学生的隐私数据。

## 4. 保存和测试连接

建议按以下顺序操作：

1. 先保持“是否启用”为关闭状态。
2. 填写接口地址、API Key、请求头模板、请求体模板、模型、流式协议等配置。
3. 点击“保存配置”。
4. 点击“测试连接”。
5. 测试成功后，再打开“是否启用”并保存。

测试消息可以使用：

```text
请用一句话回复：DeepSeek 连接测试成功。
```

如果配置正确，测试连接应返回一段正常文本。如果没有返回文本，但请求没有报错，通常是“文本路径”填写不正确，优先检查 `choices.0.delta.content` 是否适配当前模型接口返回格式。

## 5. 学生端验证方法

后台启用 AI 后，使用学生账号登录前台，在右下角点击 AI 悬浮按钮进行验证。

推荐测试以下问题：

```text
系统里目前有哪些学校？
```

```text
系统里有哪些专业可以选择？
```

```text
最近有什么公告或资讯？
```

```text
请根据我的个人信息和系统里的专业，推荐几个适合我的专业。
```

预期结果：

- AI 可以读取公开的学校、专业、公告、资讯数据。
- AI 可以读取当前登录学生自己的报名、收藏、成绩、录取结果等数据。
- AI 不应读取或输出其他学生的姓名、成绩、报名、录取结果、AI 对话历史。

## 6. 常见报错排查

### 后台保存时报 Unexpected server error

优先检查：

- 是否已经执行最新的 `docs/ai-chat-tables.sql`。
- 数据库中是否存在 `ai_model_config` 表。
- 后端连接的数据库是否和你执行 SQL 的数据库是同一个。
- `endpoint_url`、`headers_template`、`body_template` 等字段长度是否被旧表结构限制。

如果之前使用过旧版本配置，建议确认 `config` 表中是否还残留 `ai.*` 配置。新版 AI 配置不再使用 `config` 表。

### 测试连接返回 401 或鉴权失败

检查：

- API Key 是否复制完整。
- 请求头模板是否为 `Authorization: Bearer {{apiKey}}`。
- DeepSeek 账号余额、权限或 API Key 状态是否正常。

### 测试连接返回 404

检查：

- 接口地址是否填写为 `https://api.deepseek.com/chat/completions`。
- 模型名称是否存在、是否仍然可用。
- DeepSeek 官方文档是否调整了模型名称或接口路径。

### 学生端一直没有流式输出

检查：

- 请求体模板中是否包含 `"stream": true`。
- 流式协议是否选择 `SSE`。
- 文本路径是否为 `choices.0.delta.content`。
- 结束标记是否为 `[DONE]`。
- 浏览器开发者工具中，聊天接口响应类型是否为 `text/event-stream`。

### AI 回答说没有查询到数据

这不一定是模型配置问题。请继续检查：

- 数据库中是否确实存在学校、专业、公告、资讯数据。
- 当前登录学生账号是否已经绑定 `student` 表中的学生记录。
- 学生相关的报名、收藏、成绩、录取结果表中是否有该学生的数据。

## 7. 安全说明

系统的学生聊天接口不会接收前端传入的 `studentId`。后端会从当前登录 Token 中识别账号，再映射到当前学生记录。

学生 AI 上下文只允许包含：

- 当前学生自己的个人资料。
- 当前学生自己的报名信息。
- 当前学生自己的收藏信息。
- 当前学生自己的成绩信息。
- 当前学生自己的录取结果。
- 当前学生自己的 AI 会话摘要。
- 公开的学校、专业、公告、资讯数据。

以下数据不允许进入 AI 上下文：

- 其他学生的个人资料。
- 其他学生的报名、收藏、成绩、录取结果。
- 其他学生的咨询记录。
- 其他学生的 AI 对话历史。
- 后台管理员的敏感配置。
- AI API Key 明文。

后台查询 AI 配置时，接口只返回 `apiKeySet: true/false`，不会返回 API Key 明文。

## 8. 更换其他大模型的方法

如果以后要接入其他模型厂商，不需要修改前端聊天代码。通常只需要在后台“AI配置”页面调整：

- `endpointUrl`
- `headersTemplate`
- `bodyTemplate`
- `model`
- `streamProtocol`
- `responseTextPath`
- `doneMarker`

如果厂商返回的是纯文本流，可以把“流式协议”改为 `TEXT`，并清空或忽略“文本路径”。如果厂商返回的是 SSE JSON 流，则使用 `SSE`，再根据实际返回结构填写文本路径。

## 9. 参考资料

- [DeepSeek API 文档](https://api-docs.deepseek.com/)
