# README 用户帮助文档重写

## 任务范围

- 完整重写根目录 `README.md`，统一使用英文。
- 核验普通 Java CLI、R Shiny GUI、AI Agent 原生 CLI 与 Codex skill 工作流。
- 保留有效的数据库目录、BAM/BAI要求、参数语义、结果解释和自定义数据库说明。
- 删除过时命令、无效目录标记、混合语言内容和当前仓库无法验证的容器说明。

## 核验依据

- Java CLI：`src/main/java/com/NGSViz/CliOptions.java`、`src/main/java/com/NGSViz/cli/`、Java AI 契约。
- R CLI：`lib/ngsVizPlotMain.R`、`lib/cli/`、R AI 契约。
- R Shiny：`ngsViz-Shiny.R`、`lib/shiny/ui_builder.R`、`lib/shiny/server_builder.R`。
- AI 编排：`AGENTS.md`、`.agents/skills/ngsviz-*/SKILL.md`、`agent/ai-friendly/`。
- 版本信息：`pom.xml`、原生 `-V`/`describe` 输出和 Git 历史；仓库不存在 `CHANGELOG.md`。

## 主要变更

- 新增标准目录、安装、快速开始、三种使用方式、故障排查、版本说明、贡献与许可证章节。
- 普通 CLI 参数表补齐当前原生 flags、默认值和约束。
- GUI 启动方式明确为 `shiny::runApp('ngsViz-Shiny.R')`，并按源码列出当前面板。
- AI Agent 章节明确 JSON 输入、机器响应、manifest 分工、skill 形式、规则边界、单样本示例和两阶段完整流程示例。
- 示例绘图使用独立 workspace，避免覆盖仓库自带结果。
- 说明 Maven 历史 JAR 文件名与应用版本 1.3 的差异。

## 自查结果

- `ngsviz-setup` 只读环境审计：通过，状态为 `ready`。
- Java `validate-compute` 示例：通过。
- R `validate-plot` 示例：通过。
- `mvn test`：38 项通过。
- `Rscript tests/r/run_r_tests.R`：通过。
- `bash tests/contracts/02_check_module_independence.sh`：通过。
- `git diff --check`：通过。
- README 中文字符检查：未发现中文正文。

## 工作树说明

- `NGSViz_setting_cp.json`、`environment-java.yml`、`environment-linux-64.yml` 的删除为任务开始前已有用户修改，本次未处理、未纳入提交。
