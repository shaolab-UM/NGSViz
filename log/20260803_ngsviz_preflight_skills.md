# ngsViz 前置检查与参考数据库 skill

- 日期：2026-08-03
- 目标：在 Java `validate-compute` 前增加环境、配置、物种、genome 和参考数据库解析流程。

## 变更

- 新增 `ngsviz-setup`：检查、创建或修复 `NGSViz_setting.json`，验证 Java 17+、JAR、`tool_path` 和 SQLite DB。
- 新增 `ngsviz-install-db`：解析 README 预构建目录，检查已安装物种/genome，规范下载、校验、备份、Java `-DB` 导入和导入后复查。
- 新增 `ngsviz-build-db`：检查 UCSC RefSeq GTF，调用 `lib/buildRefDB.R` 构建独立 DB，并复用安装流程导入。
- 更新 `ngsviz-agent`、Agent Guide 和 Codex 编排契约：禁止在环境和参考数据库就绪前生成 compute request 或执行 validate；未提供 genome 时先确认物种。

## 自查

- 环境检查器识别当前 Java 22、可运行 JAR、配置路径及 DB 中 `hg19`、`hg38`、`mm39`。
- README 目录解析器正确返回 Human 的 `hg19`、`hg38` 及直接下载链接。
- DB 检查器通过 SQLite `quick_check`，确认三个坐标表存在且非空。
- GTF 检查器对缺失文件按预期返回非零状态。
- 未修改当前 `NGSViz_setting.json`、JAR 或参考数据库。
