\# AI Agent 平台



\## 1. 项目简介



本项目是一个基于 Spring Boot 的 AI Agent 平台后端 MVP，目标不是只实现一个普通聊天接口，而是构建一个能够“理解用户任务、选择合适 Skill、调用工具或业务能力、返回结果并记录执行过程”的 Agent 运行时系统。



当前项目已经支持多模型路由、Skill 注册与调用、Memory 记忆、MCP 风格工具适配、Skill 热加载 MVP，以及一个复杂业务 Skill 示例：`resume\_optimize` 简历优化 Skill。



用户可以通过 `/api/chat` 发送自然语言请求，AgentRuntime 会结合模型配置、会话记忆、Skill 元数据和规则兜底机制，判断是否需要调用某个 Skill。执行完成后，接口会返回最终回答、使用的模型、调用过的 Skill，以及完整的 trace 执行链路，方便观察 Agent 的决策与执行过程。



本项目目前的核心定位是：



```text

AI Agent 平台后端 MVP

&#x20; + Skill 体系

&#x20; + 复杂业务 Skill 示例

&#x20; + Skill 热加载 MVP

```



其中，简历优化功能不是作为一个孤立接口实现，而是被包装成平台中的一个标准 Skill，使其既可以通过专用 REST API 调用，也可以被 AgentRuntime 在 Chat 对话中自动选择和调用。



\---



\## 2. 当前功能总览



\### 2.1 Agent Runtime



AgentRuntime 是整个 Agent 系统的调度中心，负责接收用户消息、读取记忆、判断是否需要调用 Skill、执行 Skill，并返回结构化结果。



当前 AgentRuntime 主要能力包括：



```text

接收用户消息

读取短期记忆和长期记忆

调用 LLM 进行 Skill Decision

在 LLM 决策失败时使用规则兜底

从 SkillRegistry 中选择 Skill

执行 Skill

根据 Skill 类型决定是否二次总结

保存本轮对话记忆

返回 answer、usedSkills、trace

```



典型调用链如下：



```text

POST /api/chat

&#x20; ↓

AgentRuntime

&#x20; ↓

MemoryService

&#x20; ↓

LlmSkillDecisionService

&#x20; ↓

SkillRegistry

&#x20; ↓

Skill.execute(...)

&#x20; ↓

ChatResponse(answer, usedModel, usedSkills, trace)

```



\### 2.2 多模型支持



项目支持通过 `modelId` 选择不同模型。目前配置了：



```text

mock-model

siliconflow-qwen

siliconflow-deepseek

```



多模型配置统一放在 `application.yml` 的 `agent-platform.models` 下。业务代码通过 `ModelService` 和 `ModelRouter` 调用具体模型，避免在业务模块中写死模型供应商。



当前支持 OpenAI-compatible API，因此可以接入 SiliconFlow 等兼容接口的模型服务。



相关接口：



```http

GET /api/models

POST /api/models/test-chat

```



\### 2.3 Skill 体系



项目中，Skill 是 Agent 可调用的标准能力单元。每个 Skill 都包含元数据和执行逻辑。



当前已经实现：



```text

Skill 接口

SkillMetadata

SkillContext

SkillResult

SkillRegistry

/api/skills

/api/skills/{skillName}/call

```



当前已有内置 Skill：



```text

calculator

weather

file\_search

resume\_optimize

```



同时，项目已经支持通过外部 Jar 热加载轻量 Skill，例如测试插件：



```text

text\_reverse

```



Skill 的意义在于：Agent 不需要把所有能力写死在主流程里，而是可以通过统一的 SkillRegistry 发现、选择和调用能力。



\### 2.4 Memory 记忆



当前项目中 Memory 分为短期记忆和长期记忆。



```text

短期记忆：

&#x20; 按 conversationId 保存最近几轮对话，用于当前会话上下文。



长期记忆：

&#x20; 使用 JSON 文件持久化保存，后续可替换为 Redis 或数据库。

```



AgentRuntime 在每次对话开始时读取记忆，在对话结束后保存本轮用户消息和模型回复。



当前 Memory 主要用于对话上下文保留，后续可以进一步用于：



```text

保存用户偏好

保存最近上传的文件

保存 pending skill call

支持更自然的上下文补参

```



\### 2.5 MCP 风格工具适配



当前项目实现了简化版 MCP Adapter，用于展示 Agent 如何通过 Skill 调用外部工具能力。



当前 `file\_search` Skill 可以通过 McpClient 调用文件系统相关能力，实现类似工具调用的效果。



当前 MCP 仍是简化实现，主要用于演示：



```text

Skill

&#x20; ↓

McpClient

&#x20; ↓

外部工具能力

```



后续可以进一步演进为更标准的 MCP JSON-RPC 风格实现，包括：



```text

tools/list

tools/call

stdio 或 HTTP transport

外部 MCP server 管理

```



\### 2.6 ResumeOptimizeSkill



`resume\_optimize` 是当前项目中最完整的复杂业务 Skill。它覆盖了老师临时任务中要求的核心内容：



```text

读取招聘网站

读取 PDF / DOCX 简历

使用数据库保存过程数据

调用 LLM 生成优化简历

输出面试准备建议

包装成 Skill 供 Agent 调用

```



完整流程如下：



```text

上传 PDF / DOCX 简历

&#x20; ↓

解析简历文本

&#x20; ↓

读取招聘网页正文

&#x20; ↓

MySQL 保存文件、岗位、任务和结果

&#x20; ↓

调用 LLM 生成简历优化报告

&#x20; ↓

输出岗位要求摘要、匹配分析、优化建议、优化后简历、面试建议

&#x20; ↓

包装成 resume\_optimize Skill

&#x20; ↓

支持 /api/chat 自动调用

```



该功能既可以通过专用 REST 接口调用，也可以作为 Skill 被 AgentRuntime 调用。



\### 2.7 Skill 热加载 MVP



项目已经实现 Skill 热加载 MVP，用于演示“上传外部 Skill Jar 并运行时注册”的能力。



当前热加载流程：



```text

上传外部 Skill Jar

&#x20; ↓

保存到 data/plugins

&#x20; ↓

URLClassLoader 加载 Jar

&#x20; ↓

扫描 Jar 中实现 Skill 接口的类

&#x20; ↓

反射实例化 Skill

&#x20; ↓

动态注册到 SkillRegistry

&#x20; ↓

GET /api/skills 可见

&#x20; ↓

POST /api/skills/{skillName}/call 可调用

```



当前热加载 MVP 面向轻量无状态 Skill。例如：



```text

text\_reverse：输入文本，返回反转后的文本

```



当前版本暂不支持插件 Spring Bean 注入、插件卸载、插件数据库 Entity、插件自己的 Flyway 迁移和完整依赖隔离。



\---



\## 3. 技术栈



| 技术                    | 用途                   |

| --------------------- | -------------------- |

| Java 17               | 项目主语言                |

| Spring Boot 3.x       | 后端应用框架               |

| Spring MVC            | REST API 接口          |

| Spring Data JPA       | 数据库访问层               |

| MySQL                 | 业务数据存储               |

| Flyway                | 数据库版本迁移              |

| Spring AI             | 模型调用基础能力             |

| OpenAI-compatible API | 接入兼容 OpenAI 接口的大模型服务 |

| PDFBox                | PDF 简历文本提取           |

| Apache POI            | DOCX 简历文本提取          |

| Jsoup                 | 招聘网页正文读取             |

| Maven                 | 项目构建和依赖管理            |

| Apifox                | 接口测试和演示              |

| Git / GitHub          | 版本控制                 |



\---



\## 4. 项目架构



\### 4.1 整体分层



当前项目采用较清晰的分层结构：



```text

用户 / Apifox / 前端

&#x20;       ↓

Controller 层

&#x20;       ↓

Service 层

&#x20;       ↓

Repository / ModelService / Skill / MCP Client

&#x20;       ↓

MySQL / LLM / File System / External Tools

```



各层职责：



```text

Controller：

&#x20; 提供 REST API，接收前端或 Apifox 请求。



Service：

&#x20; 承载业务流程编排，是核心业务逻辑所在。



Repository：

&#x20; 负责数据库读写。



Skill：

&#x20; 面向 AgentRuntime 的能力包装入口。



ModelService / ModelRouter：

&#x20; 负责模型调用和模型选择。



MCP Client：

&#x20; 负责对接外部工具能力。

```



\### 4.2 Agent 调用链



```text

POST /api/chat

&#x20; ↓

AgentRuntime

&#x20; ↓

读取 Memory

&#x20; ↓

LLM Skill Decision

&#x20; ↓

规则兜底 Intent Detection

&#x20; ↓

SkillRegistry 查找 Skill

&#x20; ↓

Skill.execute(...)

&#x20; ↓

根据 Skill 类型生成最终回答

&#x20; ↓

保存本轮对话到 Memory

&#x20; ↓

返回 ChatResponse

```



`ChatResponse` 中包含：



```text

conversationId

answer

usedModel

usedSkills

trace

```



其中 trace 是观察 Agent 行为的重要依据。



\### 4.3 Resume 功能调用链



\#### 4.3.1 简历上传



```text

POST /api/resume/files

&#x20; ↓

ResumeFileController

&#x20; ↓

ResumeFileStorageService

&#x20; ↓

保存文件到 data/resume/files

&#x20; ↓

写入 resume\_file 表

```



\#### 4.3.2 简历解析



```text

POST /api/resume/files/{fileId}/parse

&#x20; ↓

ResumeFileParseController

&#x20; ↓

ResumeFileParseService

&#x20; ↓

PdfResumeFileParser / DocxResumeFileParser

&#x20; ↓

ResumeTextCleaner

&#x20; ↓

写回 resume\_file.parsed\_text

```



\#### 4.3.3 招聘网页读取



```text

POST /api/resume/job-postings/read

&#x20; ↓

JobPostingController

&#x20; ↓

JobPostingService

&#x20; ↓

JobPageReader 使用 Jsoup 读取网页

&#x20; ↓

写入 job\_posting.raw\_text

```



\#### 4.3.4 简历优化



```text

POST /api/resume/optimize

&#x20; ↓

ResumeOptimizeController

&#x20; ↓

ResumeOptimizeService

&#x20; ↓

读取 resume\_file.parsed\_text

&#x20; ↓

读取 job\_posting.raw\_text

&#x20; ↓

创建 resume\_analysis\_task

&#x20; ↓

ResumePromptBuilder 构造 Prompt

&#x20; ↓

ModelService 调用 LLM

&#x20; ↓

ResumeOptimizeResultParser 解析模型输出

&#x20; ↓

写入 resume\_optimization\_result

&#x20; ↓

更新 task 状态

```



\#### 4.3.5 Agent 调用 resume\_optimize Skill



```text

POST /api/chat

&#x20; ↓

AgentRuntime 判断需要调用 Skill

&#x20; ↓

SkillRegistry 找到 resume\_optimize

&#x20; ↓

ResumeOptimizeSkill.execute(...)

&#x20; ↓

ResumeOptimizeService.optimize(...)

&#x20; ↓

SkillResult 返回完整简历优化报告

&#x20; ↓

AgentRuntime direct return

```



\---



\## 5. 数据库设计



Resume 模块使用 MySQL 存储简历文件、岗位网页、分析任务和优化结果。数据库结构通过 Flyway 管理，避免依赖 Hibernate 自动建表。



\### 5.1 resume\_file



用途：保存上传的简历文件信息、文件路径和解析后的文本。



核心字段：



```text

id

file\_id

original\_file\_name

file\_type

storage\_path

parsed\_text

created\_at

updated\_at

```



说明：



```text

file\_id 是对外返回的文件标识。

storage\_path 是文件在本地 data/resume/files 下的存储路径。

parsed\_text 是 PDF / DOCX 解析后的纯文本。

```



\### 5.2 job\_posting



用途：保存招聘网页链接、标题和网页正文。



核心字段：



```text

id

job\_url

page\_title

raw\_text

requirement\_summary

created\_at

updated\_at

```



说明：



```text

raw\_text 是 Jsoup 读取到的网页正文。

requirement\_summary 预留用于后续保存岗位要求摘要。

```



\### 5.3 resume\_analysis\_task



用途：记录一次简历分析任务。



核心字段：



```text

id

conversation\_id

model\_id

resume\_file\_id

job\_posting\_id

status

error\_message

created\_at

updated\_at

```



说明：



```text

status 用于记录任务状态，例如 RUNNING、SUCCESS、FAILED。

error\_message 用于记录失败原因。

```



\### 5.4 resume\_optimization\_result



用途：保存模型生成的简历优化结果。



核心字段：



```text

id

task\_id

job\_requirement\_summary

match\_analysis

optimization\_suggestions

optimized\_resume

interview\_suggestions

raw\_model\_response

created\_at

```



说明：



```text

raw\_model\_response 保存模型原始输出。

其他字段由 ResumeOptimizeResultParser 从模型输出中解析得到。

```



\---



\## 6. API 测试流程



以下流程适合用 Apifox 进行端到端演示。



\### 6.1 查看模型列表



```http

GET /api/models

```



预期可以看到：



```text

mock-model

siliconflow-qwen

siliconflow-deepseek

```



\### 6.2 测试模型调用



```http

POST /api/models/test-chat

Content-Type: application/json

```



请求体：



```json

{

&#x20; "modelId": "siliconflow-deepseek",

&#x20; "message": "请用一句话介绍你自己"

}

```



\### 6.3 上传简历



```http

POST /api/resume/files

Content-Type: multipart/form-data

```



Body 使用 form-data：



```text

key: file

类型: File

value: 选择 .docx 或 .pdf 简历文件

```



返回结果中会包含：



```text

fileId

originalFileName

fileType

storagePath

createdAt

```



\### 6.4 解析简历



```http

POST /api/resume/files/{fileId}/parse

```



不需要 Body。



成功后会将解析文本写回：



```text

resume\_file.parsed\_text

```



\### 6.5 读取招聘网页



```http

POST /api/resume/job-postings/read

Content-Type: application/json

```



请求体示例：



```json

{

&#x20; "jobUrl": "https://job-boards.greenhouse.io/eulerity/jobs/4666015006"

}

```



成功后会写入：



```text

job\_posting.raw\_text

```



并返回：



```text

jobPostingId

pageTitle

textLength

preview

```



\### 6.6 手动调用简历优化接口



```http

POST /api/resume/optimize

Content-Type: application/json

```



请求体：



```json

{

&#x20; "conversationId": "resume-direct-demo-001",

&#x20; "modelId": "siliconflow-deepseek",

&#x20; "resumeFileId": "替换成上传接口返回的 fileId",

&#x20; "jobPostingId": 1

}

```



返回内容包括：



```text

taskId

resultId

jobRequirementSummary

matchAnalysis

optimizationSuggestions

optimizedResume

interviewSuggestions

rawModelResponse

```



\### 6.7 查看 Skill 列表



```http

GET /api/skills

```



预期可以看到：



```text

calculator

weather

file\_search

resume\_optimize

```



如果已经上传热加载插件，还可以看到：



```text

text\_reverse

```



\### 6.8 手动调用 resume\_optimize Skill



```http

POST /api/skills/resume\_optimize/call

Content-Type: application/json

```



请求体：



```json

{

&#x20; "params": {

&#x20;   "conversationId": "resume-skill-demo-001",

&#x20;   "modelId": "siliconflow-deepseek",

&#x20;   "resumeFileId": "替换成 fileId",

&#x20;   "jobPostingId": 1

&#x20; }

}

```



\### 6.9 Chat 调用 resume\_optimize Skill



```http

POST /api/chat

Content-Type: application/json

```



请求体：



```json

{

&#x20; "conversationId": "resume-agent-demo-001",

&#x20; "modelId": "siliconflow-deepseek",

&#x20; "message": "请根据 resumeFileId=替换成fileId 和 jobPostingId=1 帮我优化简历，并给出面试准备建议。"

}

```



预期：



```text

usedSkills 包含 resume\_optimize

trace 中出现 SELECT\_SKILL、CALL\_SKILL、GENERATE\_ANSWER

GENERATE\_ANSWER 中 directReturn = true

```



\### 6.10 追问式调用



第一轮请求：



```json

{

&#x20; "conversationId": "pending-resume-demo-001",

&#x20; "modelId": "siliconflow-deepseek",

&#x20; "message": "帮我优化简历并给出面试建议"

}

```



预期：Agent 识别为简历优化意图，但参数不足，追问用户提供 `resumeFileId` 和 `jobPostingId`。



第二轮请求：



```json

{

&#x20; "conversationId": "pending-resume-demo-001",

&#x20; "modelId": "siliconflow-deepseek",

&#x20; "message": "resumeFileId=替换成fileId，jobPostingId=1"

}

```



预期：Agent 从 pendingSkillCall 中恢复未完成任务，补齐参数后调用 `resume\_optimize`。



\### 6.11 插件热加载



上传插件 Jar：



```http

POST /api/plugins/skills/upload

Content-Type: multipart/form-data

```



Body：



```text

key: file

类型: File

value: plugin-demo-1.0.0.jar

```



查看 Skill：



```http

GET /api/skills

```



调用插件 Skill：



```http

POST /api/skills/text\_reverse/call

Content-Type: application/json

```



请求体：



```json

{

&#x20; "params": {

&#x20;   "text": "hello skill"

&#x20; }

}

```



预期返回：



```text

lliks olleh

```



\---



\## 7. ResumeOptimizeSkill 实现说明



\### 7.1 业务目标



ResumeOptimizeSkill 的目标是根据用户上传的简历和招聘网页内容，生成一份面向具体岗位的简历优化报告。



输出内容包括：



```text

岗位要求摘要

简历匹配分析

简历优化建议

优化后的简历

面试准备建议

```



该功能用于覆盖老师布置的 Skill 学习任务：



```text

用户发送学校招聘网站链接和准备的简历

Agent 解读网站内容

读取简历文件

输出根据岗位要求优化好的简历

要求带数据库、能读取 docx 和 pdf

```



\### 7.2 文件上传与解析



用户通过 `/api/resume/files` 上传 PDF 或 DOCX 文件。系统将文件保存到：



```text

data/resume/files

```



同时在 `resume\_file` 表中创建记录。



随后通过：



```http

POST /api/resume/files/{fileId}/parse

```



触发文本解析。



解析策略：



```text

PDF：使用 PDFBox

DOCX：使用 Apache POI

解析后：使用 ResumeTextCleaner 清洗文本

```



当前 DOCX 解析稳定性更高。PDF 由于格式本身更偏向页面排版，因此复杂排版、特殊字体、扫描版 PDF 仍可能出现解析不稳定。



\### 7.3 招聘网页读取



用户通过 `/api/resume/job-postings/read` 提供招聘网页 URL。



系统使用 Jsoup 读取网页：



```text

document.title() 获取标题

document.body().text() 获取正文

```



读取结果保存到 `job\_posting` 表。



当前版本主要支持公开可访问的静态网页。对于登录限制、验证码、动态渲染页面，Jsoup 可能无法完整读取。



\### 7.4 Prompt 构造



Prompt 构造集中在 `ResumePromptBuilder` 中。



Prompt 输入包括：



```text

岗位网页正文 jobPosting.rawText

简历解析文本 resumeFile.parsedText

输出格式要求

质量约束

```



模型被要求输出以下固定结构：



```markdown

\## 岗位要求摘要



\## 简历匹配分析



\## 简历优化建议



\## 优化后的简历



\## 面试准备建议

```



Prompt 中也限制模型：



```text

不要编造简历中不存在的学校、公司、项目、年份

如果简历中没有体现某项岗位要求，需要明确写“简历中暂未体现”

保留合理技术英文名，例如 Spring Boot、MySQL、REST API、MCP、LLM

面试建议必须围绕岗位要求和简历中的真实项目展开

```



\### 7.5 模型输出解析



模型原始输出由 `ResumeOptimizeResultParser` 解析成结构化字段：



```text

jobRequirementSummary

matchAnalysis

optimizationSuggestions

optimizedResume

interviewSuggestions

rawModelResponse

```



如果模型没有按预期 Markdown 标题输出，Parser 会进行兜底，避免最终结果完全为空。



\### 7.6 数据库存储



每次简历优化都会创建一条任务记录：



```text

resume\_analysis\_task

```



任务成功后，会保存一条结果记录：



```text

resume\_optimization\_result

```



这样可以追踪：



```text

哪份简历

哪个岗位

哪个模型

什么时间

执行状态

模型原始输出

最终结构化结果

```



\### 7.7 Skill 包装



`ResumeOptimizeSkill` 是对 `ResumeOptimizeService` 的包装。



它主要负责：



```text

从 SkillContext 读取参数

校验 resumeFileId 和 jobPostingId

构造 ResumeOptimizeRequest

调用 ResumeOptimizeService

把 ResumeOptimizeResponse 格式化为可读文本

返回 SkillResult

```



它不直接承载复杂业务逻辑，复杂流程集中在 Service 中。



\### 7.8 Chat 调用与 direct return



普通短结果 Skill 可以让 AgentRuntime 再调用模型进行二次总结。



但 `resume\_optimize` 本身已经调用 LLM 生成完整报告，如果再次交给模型总结，会导致：



```text

请求耗时增加

上下文变长

可能破坏原始报告结构

```



因此项目对 `resume\_optimize` 使用 direct return 策略：



```text

SkillResult 成功后，直接将 result 作为 ChatResponse.answer 返回。

```



\---



\## 8. Skill 热加载 MVP 说明



当前项目支持最小可演示版 Skill 热加载。



\### 8.1 热加载流程



```text

POST /api/plugins/skills/upload

&#x20; ↓

PluginSkillController

&#x20; ↓

PluginSkillService

&#x20; ↓

保存 Jar 到 data/plugins

&#x20; ↓

PluginSkillLoader 使用 URLClassLoader 加载 Jar

&#x20; ↓

扫描 .class 文件

&#x20; ↓

判断是否实现 Skill 接口

&#x20; ↓

反射调用 public 无参构造器实例化

&#x20; ↓

注册到 SkillRegistry

```



\### 8.2 测试插件



当前使用 `plugin-demo` 工程生成测试 Jar：



```text

plugin-demo-1.0.0.jar

```



其中包含：



```text

text\_reverse Skill

```



功能：



```text

输入 text

返回反转后的字符串

```



\### 8.3 当前限制



当前热加载 MVP 仍有明显边界：



```text

不支持 Spring Bean 注入

不支持插件卸载

不支持插件启用 / 禁用

不支持插件数据库 Entity

不支持插件自己的 Flyway 迁移

不支持完整依赖隔离

外部 Skill 必须有 public 无参构造器

外部 Skill 更适合轻量无状态能力

```



复杂业务 Skill，例如 `resume\_optimize`，目前仍作为内置 Skill，因为它依赖数据库、ModelService、Repository 和多个 Spring Bean。



未来可以通过 `SkillHostContext` 暴露受控的平台能力，让外部 Skill 在不完全进入 Spring 容器的情况下访问部分平台服务。



\---



\## 9. Trace 设计



`/api/chat` 的响应中包含 trace，用于展示 Agent 的完整执行过程。



常见 trace step 包括：



```text

RECEIVE\_MESSAGE

LOAD\_MEMORY

LLM\_SKILL\_DECISION

INTENT\_DETECTION

SELECT\_SKILL

CALL\_SKILL

GENERATE\_ANSWER

SAVE\_MEMORY

FINISH

```



Trace 的作用：



```text

观察 Agent 为什么选择某个 Skill

排查 LLM Skill Decision 是否失败

验证规则兜底是否生效

观察 Skill 参数是否正确

确认是否执行 direct return

辅助调试和答辩展示

```



例如，当 Chat 成功调用 `resume\_optimize` 时，可以在 trace 中看到：



```text

SELECT\_SKILL：选择 resume\_optimize

CALL\_SKILL：调用 resume\_optimize 成功

GENERATE\_ANSWER：resume\_optimize Skill 结果已直接作为最终回答

directReturn=true

```



\---



\## 10. 当前问题与限制



当前项目已经完成后端核心 MVP，但仍有一些明确限制。



\### 10.1 PDF 解析限制



PDF 本质上更接近页面排版结果，不是结构化文档。当前使用 PDFBox 提取文本，并增加了文本清洗逻辑，但仍可能存在：



```text

多栏文本顺序错乱

特殊字体乱码

扫描版 PDF 无法提取文本

表格布局丢失

```



当前 Demo 更推荐使用 DOCX 文件。



\### 10.2 招聘网页读取限制



当前使用 Jsoup 读取网页正文，主要适合公开可访问的静态网页。



以下情况可能读取失败：



```text

需要登录

验证码

反爬限制

前端 JS 动态渲染

页面正文混入大量导航和页脚内容

```



\### 10.3 LLM 输出格式不稳定



当前模型输出主要通过 Markdown 标题解析。虽然已经做了 rawModelResponse 兜底，但模型仍可能出现：



```text

标题不完全匹配

字段缺失

内容重复

格式不稳定

```



后续可以升级为 JSON Schema 或更严格的结构化输出。



\### 10.4 同步接口耗时较长



简历优化需要调用大模型，可能耗时几十秒。当前接口是同步返回，后续更适合改为异步任务：



```text

创建任务 → 返回 taskId → 前端轮询任务状态 → 获取结果

```



\### 10.5 Chat 仍未完全自然语言化



当前 Chat 已经支持：



```text

显式参数调用 resume\_optimize

缺参数时追问

补齐参数后继续调用

显式调用热加载 Skill

```



但还不能完全做到：



```text

“我刚上传了简历，这是岗位链接，帮我优化”

```



这种自然任务需要：



```text

文件和 conversationId 绑定

自动识别最近上传的简历

自动识别 jobUrl

自动调用 job\_page\_read

自动调用 resume\_optimize

多 Skill Planner 编排

```



\### 10.6 Skill 热加载仍是 MVP



当前热加载主要面向轻量无状态 Skill。成熟插件系统还需要：



```text

插件卸载

插件启用 / 禁用

插件元数据持久化

ClassLoader 生命周期管理

权限控制

依赖隔离

受控平台能力注入

```



\### 10.7 MCP 仍是简化适配



当前 MCP 模块主要用于演示工具调用思想，还不是完整标准协议实现。



后续可升级为更标准的：



```text

JSON-RPC MCP Client

tools/list

tools/call

stdio / HTTP transport

外部 MCP Server 接入

```



