## Skill 市场模块

本项目设计了统一的 Skill 接口，用于抽象 Agent 可调用的外部能力。每个 Skill 需要提供元数据，包括名称、描述、版本、参数 schema 和依赖信息，并通过统一的 execute 方法完成调用。

当前版本实现了基于 Spring Bean 的内置 Skill 注册机制。系统启动时，SkillRegistry 会自动扫描所有 Skill Bean 并注册到技能市场中。用户可以通过 REST API 查看 Skill 列表、启用或禁用 Skill，并手动调用指定 Skill。

当前内置了三个示例 Skill：

- CalculatorSkill：用于数学表达式计算；
- WeatherSkill：用于天气查询，当前使用 mock 数据；
- FileSearchSkill：用于在 workspace 目录下搜索文件。

该模块后续可以扩展为 Jar 插件热加载机制。通过 PluginLoader 加载外部 Jar，发现其中实现 Skill 接口的类，并动态注册到 SkillRegistry，从而实现真正的插件化 Skill 市场。