# AGENTS.md

## Project Overview

This repository is a Spring Boot based AI Agent platform backend MVP.

The project is designed for an AI Agent platform assessment. It is not a single-purpose resume optimization tool. Resume optimization is implemented as a complex business Skill inside the Agent platform.

The project currently focuses on:

- AgentRuntime chat orchestration
- Multi-model routing
- Skill registration and execution
- Memory
- MCP-style tool integration
- ResumeOptimizeSkill as a complex business Skill
- Plugin Skill hot loading MVP
- Traceable Agent execution
- Async resume optimization task MVP

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring MVC
- Spring Data JPA
- MySQL
- Flyway
- Spring AI
- Maven
- PDFBox
- Apache POI
- Jsoup

## Common Commands

Compile:

```powershell
mvn compile

Run backend:

mvn spring-boot:run

Run tests if available:

mvn test

Check Git changes:

git status --short
git diff --stat
Environment Variables

The backend expects these environment variables when using real MySQL and LLM providers:

MYSQL_URL
MYSQL_USERNAME
MYSQL_PASSWORD
OPENAI_API_KEY

Do not hardcode secrets into source files.

Do not print API keys, database passwords, or private credentials in logs, README examples, or test output.

Architecture Rules

Keep these boundaries clear:

Controller: REST API entrypoint
Service: business orchestration
Repository: database access
Skill: Agent-callable capability wrapper
ModelService / ModelRouter: LLM routing and invocation
MCP client: external tool integration
DTO: request / response data shape

Do not put heavy business logic into controllers.

Do not duplicate service logic inside Skill classes.

Do not make AgentRuntime responsible for business-specific implementation details unless the task explicitly concerns Agent orchestration.

Prefer small focused classes over large mixed-responsibility classes.

Current Important Modules
Agent

Important package:

src/main/java/com/sharon/agentplatform/agent

AgentRuntime is the orchestration core. Be careful when modifying:

message receiving
memory loading
LLM Skill Decision
rule fallback
pending Skill call
Skill selection
Skill execution
direct return
answer generation
memory saving
trace generation

When adding new behavior, preserve trace visibility.

If modifying AgentRuntime, verify /api/chat and inspect:

usedSkills
trace
INTENT_DETECTION
SELECT_SKILL
CALL_SKILL
GENERATE_ANSWER
Model

Important package:

src/main/java/com/sharon/agentplatform/model

ModelService and ModelRouter handle model selection and invocation.

Do not hardcode model provider logic into business services.

Use modelId from requests when available.

Skill

Important package:

src/main/java/com/sharon/agentplatform/skill

A Skill should expose:

metadata()
execute(SkillContext context)

SkillMetadata should describe:

name
displayName
description
version
parameterSchema
dependencies

Use SkillResult.success(...) and SkillResult.fail(...) consistently.

Built-in complex Skills may use Spring Bean injection.

Hot-loaded plugin Skills currently must be lightweight and must not depend on Spring Bean injection.

Resume

Important package:

src/main/java/com/sharon/agentplatform/resume

Resume workflow:

Upload resume file
Parse PDF/DOCX into text
Read job posting web page
Create analysis task
Build prompt
Call LLM
Parse model output
Save result
Return response

Do not modify Flyway SQL unless explicitly requested.

Do not change existing REST API paths unless explicitly requested.

Keep synchronous and asynchronous resume optimize APIs both available:

POST /api/resume/optimize
POST /api/resume/optimize/async
GET /api/resume/tasks/{taskId}

ResumeOptimizeSkill should stay a thin wrapper around ResumeOptimizeService.

ResumeOptimizeService should remain the main business orchestration entrypoint.

ResumeOptimizeTaskRunner and ResumeOptimizeAsyncExecutor are used for async execution.

Plugin Loading

Important package:

src/main/java/com/sharon/agentplatform/plugin

Current plugin loading MVP supports:

Upload external Jar
Save Jar to data/plugins
Load via URLClassLoader
Scan classes implementing Skill
Instantiate via public no-args constructor
Register into SkillRegistry

Current limitations:

No Spring Bean injection for plugin Skills
No plugin unload
No plugin dependency isolation
No plugin database entities
No plugin Flyway migrations

Do not over-engineer plugin loading unless explicitly requested.

For plugin loading tests, use simple lightweight Skills such as text_reverse.

MCP

Important package:

src/main/java/com/sharon/agentplatform/mcp

Current MCP implementation is simplified and used to demonstrate MCP-style tool access.

Skill is the Agent-facing capability.

MCP is an optional lower-level tool connection mechanism.

Not every Skill must use MCP.

Examples:

calculator: pure Java Skill, no MCP required
resume_optimize: business Skill, no MCP required
file_search: tool-style Skill, suitable for MCP-style integration

Do not replace the current MCP implementation with a large protocol rewrite unless explicitly requested.

Async Task Rules

Async resume optimization is an MVP.

It uses Spring ThreadPoolTaskExecutor.

Current async endpoints:

POST /api/resume/optimize/async
GET /api/resume/tasks/{taskId}

Current limitations:

In-memory thread pool
No Redis
No message queue
No task cancellation
No progress percentage
RUNNING tasks are not automatically recovered after service restart

Avoid @Async self-invocation. Use a separate executor component or runner.

Do not introduce Redis, MQ, or extra infrastructure unless explicitly requested.

Prompt and LLM Output Rules

Prompt construction should stay separated from service orchestration.

For resume optimization, prompt-related logic belongs in:

ResumePromptBuilder

Model output parsing belongs in:

ResumeOptimizeResultParser

Do not silently discard raw model output.

Keep rawModelResponse when possible for debugging.

If model output does not follow the expected format, provide a safe fallback instead of returning completely empty sections.

Do not allow prompt changes to invent resume facts. Resume optimization must be based on the original resume content.

Direct Return Rule

For large report Skills such as resume_optimize, keep direct return behavior unless explicitly requested.

Reason:

resume_optimize already calls LLM internally
a second LLM summarization can be slow
a second LLM summarization may damage the report structure

Short-result Skills may still use normal answer generation.

Pending Skill Call Rule

The project supports a pending Skill call MVP.

When a user intent is clear but required parameters are missing, AgentRuntime may:

Save a pending Skill call
Ask the user for missing parameters
Merge parameters in the next turn
Call the original Skill after parameters are complete

Currently this is mainly used for resume optimization.

Do not generalize this mechanism heavily unless explicitly requested.

Database Rules

Database migrations are managed by Flyway.

Do not use Hibernate auto-create as the source of truth.

Do not change database tables without adding a Flyway migration.

Do not add foreign key constraints unless explicitly requested. The current design may intentionally avoid DB-level foreign keys for flexibility.

When adding new fields:

Add Flyway migration
Update entity
Update repository/service usage
Verify with mvn compile
API Response Rules

Use the existing ApiResponse wrapper consistently.

Business errors should use BusinessException where appropriate.

Do not expose stack traces to API users.

Error messages should be readable and useful for debugging.

Coding Style
Use plain getters/setters.
Do not introduce Lombok unless explicitly requested.
Prefer constructor injection where it is already used.
Keep method names descriptive.
Avoid large refactors unless explicitly requested.
Do not swallow exceptions silently.
Use logs for important lifecycle events.
Keep controller methods thin.
Keep service methods focused.
Keep DTOs simple.
Verification Requirements

After Java code changes, run:

mvn compile

If a change affects APIs, provide Apifox or curl test steps.

If a change affects Skill behavior, verify:

GET /api/skills
POST /api/skills/{skillName}/call

If a change affects AgentRuntime, verify:

POST /api/chat

and inspect:

usedSkills
trace
CALL_SKILL
GENERATE_ANSWER

If a change affects resume optimization, verify at least one of:

POST /api/resume/optimize
POST /api/resume/optimize/async
GET /api/resume/tasks/{taskId}

If a change affects plugin loading, verify:

POST /api/plugins/skills/upload
GET /api/skills
POST /api/skills/text_reverse/call
Demo Stability Rules

For demonstrations:

Prefer DOCX over PDF for resume parsing.
PDF parsing can be unstable for complex layout, scanned documents, or special fonts.
Use Greenhouse job pages for job posting read tests when possible.
Use Apifox or curl as the stable demo method.
Frontend work is optional and should not block backend MVP demonstration.
Files and Directories to Avoid Committing

Do not commit:

target/
data/
workspace/
.idea/
*.iml
local secret files
frontend node_modules/
plugin-demo target/
temporary upload files
generated logs
Git Rules

Do not create git commits unless explicitly asked.

Before suggesting a commit, ask the user to run:

git status --short
git diff --stat

Use clear commit messages, for example:

feat: add async resume optimization task
docs: update resume skill api guide
refactor: improve agent skill decision fallback
feat: add plugin skill loading
Preferred Workflow for Codex

Before coding:

Read this AGENTS.md.
Inspect relevant existing files.
Make the smallest safe change.
Preserve existing public APIs unless explicitly asked.
Avoid modifying unrelated modules.
Run the required verification command.
Report modified files and test steps.
Do not commit unless explicitly asked.
Current Project Priorities

The current priority is to complete backend assessment requirements before continuing frontend work.

Priority order:

Stabilize and document backend MVP
Keep resume optimization Skill demonstrable
Keep Skill hot loading demonstrable
Improve MCP structure gradually
Improve AgentRuntime carefully
Add frontend only after backend demo path is stable

Do not derail into frontend dependency issues unless explicitly requested.