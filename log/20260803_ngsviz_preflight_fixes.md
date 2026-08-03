# ngsViz 前置流程问题修复

- 日期：2026-08-03
- 范围：修复预构建 DB、环境 DB 检查、GTF 染色体命名、`tool_path` 完整性和 macOS arm64 Java 自动安装风险。

## 修复

- 预构建 DB：`manage_ngsviz_db.py prepare` 按 magic bytes 识别 gzip，解压到临时文件，验证 SQLite 与目标 genome 后原子写入 `.db`，保留下载归档且拒绝覆盖目标。
- 空坐标表：环境检查对每个 `CoordinateTblName` 执行计数；空表返回 `COORDINATE_TABLE_EMPTY`，空 `defaultTbl` 返回 `DEFAULT_TABLE_EMPTY`。
- NCBI accession：GTF 中任一染色体名符合 NCBI accession 形式即返回 `NCBI_ACCESSION_CHROMOSOMES_UNSUPPORTED`，混合命名不再放行。
- `tool_path`：增加 `README.md` 可读性和 `lib/` 目录完整性检查，失败时返回 `TOOL_PATH_INCOMPLETE`。
- Java 安装：新增跨平台 `environment-java.yml`，仅解析 Java 17+；完整 `environment.yml` 明确限定为 Linux x86_64 锁定环境。setup skill 和 README 改用 Java-only 环境处理 macOS arm64 自动安装。

## 自查

- `tests/contracts/02_check_ngsviz_preflight_skills.py`：6 个回归测试全部通过。
- 4 个 ngsViz skill 均通过 `skill-creator/scripts/quick_validate.py`。
- `tests/contracts/01_check_agent_guide.sh` 通过。
- `conda env create --dry-run -f environment-java.yml` 在 `osx-arm64` 成功解析 `openjdk 25.0.2`，未创建环境。
- 当前真实环境审计为 `status=ready`：Java 22、AI CLI 兼容 JAR、hg19 数据库及完整 `tool_path` 均通过。
- 未修改用户当前的 `NGSViz_setting.json`、JAR 文件或参考数据库。
