---
name: ngsviz-setup
description: Inspect, create, and repair NGSViz_setting.json and the local ngsViz Java runtime, runnable JAR, tool_path, and reference database path. Use before any ngsViz Java compute validation, when configuration is missing or invalid, or when Java, JAR, tool, or database paths may have moved.
---

# ngsViz 环境配置

在任何 Java `validate-compute` 之前完成环境检查。只可视化流程不需要此检查。

## 检查流程

1. 将包含 `README.md`、`lib/` 和 ngsViz JAR 的目录确定为 `tool_path`，不要根据当前工作目录盲猜。
2. 运行只读审计：

   ```bash
   python3 scripts/check_ngsviz_environment.py --tool-path /absolute/path/to/NGSViz
   ```

   已知用户要求的 genome 时增加 `--genome <assembly>`，让 DB 候选按该版本筛选。
3. 检查审计 JSON 中的 `errors`、`warnings`、`selected` 和 `proposed_config`。
4. 仅当 `can_apply=true` 时运行同一命令并增加 `--apply`。脚本以原子方式写配置；修改已有配置前会创建带时间戳的备份。
5. 再运行一次只读审计，必须达到 `status=ready`，然后才能进入 genome/数据库解析和计算参数收集。

## 修复规则

- `NGSViz_setting.json` 不存在：发现唯一且合格的 Java、JAR 和 DB 后创建。
- `java_path` 无效或 Java 主版本低于 17：先查找系统中其他 Java 17+；找到则修正路径。
- 系统没有 Java 17+：按 README 使用 `conda env create -f environment.yml` 安装。该命令会下载并安装软件，执行前遵循平台审批；完成后用 `conda run -n ngsViz which java` 取得绝对路径，再写配置。
- 配置指定的 `tool_name` 不存在：若工具根目录只有一个 `NGSViz-*.jar`，修正为该文件名；若没有 JAR，报错并停止；若有多个且无法唯一确定，列出候选并询问用户，不按文件名版本大小猜测。
- `tool_path` 无效：根据 JAR、README 和 `lib/` 的共同位置修正为绝对路径。
- `db_path` 无效：先检查配置相对路径解析错误，再检查 `<tool_path>/database/*.db`。只选择含 `defaultTbl` 且 SQLite 完整性检查通过的 DB。
- 没有可用 DB：不要创建指向不存在文件的配置；转交 `$ngsviz-install-db`。预构建目录没有目标物种或版本时转交 `$ngsviz-build-db`。
- 保留配置中的未知字段；禁止覆盖 JAR、DB 或其他原始文件。

## 就绪条件

- Java 可执行且主版本至少为 17。
- `tool_path/tool_name` 是存在且可运行的 JAR，并且 `describe --format json` 声明支持 `validate-compute` 和 `run-compute`。
- `db_path` 是可读 SQLite 文件，包含 `defaultTbl`，且坐标表元数据有效。
- 配置中的四个路径均为当前机器的真实路径。

环境未就绪时不得创建 compute request，也不得运行 `validate-compute`。
