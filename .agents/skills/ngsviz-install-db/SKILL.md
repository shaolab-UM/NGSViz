---
name: ngsviz-install-db
description: Discover installed ngsViz species and genome assemblies, parse the README pre-built database catalog, download a requested pre-built SQLite database, validate it, and import it with the ngsViz Java -DB method. Use when a requested genome is absent from the configured database or when ngsViz has no usable reference database.
---

# ngsViz 预构建参考数据库安装

只安装用户已经明确指定或确认的物种与 genome assembly。不得从 BAM 文件名、染色体前缀或常见实验习惯推断 assembly。

## 查询

1. 查询当前 DB：

   ```bash
   python3 scripts/manage_ngsviz_db.py inspect --db /absolute/path/to/current.db
   ```

2. 从项目当前 `README.md` 解析预构建目录，不复制硬编码的旧链接：

   ```bash
   python3 scripts/manage_ngsviz_db.py catalog --readme /absolute/path/to/README.md
   ```

   可增加 `--species Homo_sapiens` 或 `--genome hg38` 精确过滤。

## 版本决策

- 用户已明确 genome：若当前 DB 已安装该版本，立即返回主流程；否则在 README 目录中精确查找。
- 用户只明确物种：先查询当前 DB。只有一个 genome 时询问是否使用；多个时列出并询问选择。
- 用户拒绝唯一已安装版本：列出 README 中同物种的其他版本供选择。
- 当前 DB 没有该物种：查询 README；有则列出版本并询问，随后安装确认版本。
- README 没有该物种或 genome：停止下载流程，请用户从 UCSC 提供对应 assembly 的 GTF，并转交 `$ngsviz-build-db`。

物种匹配同时展示 README 的通用名与 scientific name，内部以 `defaultTbl.Species`/README scientific name 为准。

## 下载和验证

1. 使用目录解析出的精确 URL，以 `curl -L --fail` 下载到独立的临时文件。下载属于网络操作，遵循平台审批。
2. README 的预构建文件可能是 gzip 归档。使用以下命令按文件 magic bytes 判断是否需要解压，并将解压、验证后的 SQLite 文件原子写入目标路径：

   ```bash
   python3 scripts/manage_ngsviz_db.py prepare \
     --source /absolute/path/to/downloaded-file \
     --output <tool_path>/database/prebuilt/NGSViz_<genome>_RefSeq.db \
     --genome <assembly>
   ```

   保留下载归档；不得把 `.db.gz` 直接交给 SQLite，也不得覆盖同名目标文件。冲突时先比较内容或使用带时间戳的新路径。
3. `prepare` 已执行 SQLite 与目标 genome 验证。导入前可再次独立验证：

   ```bash
   python3 scripts/manage_ngsviz_db.py verify \
     --db /absolute/path/to/downloaded.db --genome <assembly>
   ```

   必须满足 SQLite `quick_check=ok`、存在 `defaultTbl`、包含目标 genome，且其 `CoordinateTblName` 表存在并非空。

## 安装

- 当前没有主 DB：不运行合并。将已验证的预构建 DB 作为主 DB，调用 `$ngsviz-setup --apply` 写入 `db_path`。
- 当前已有可写主 DB：先复制时间戳备份，再用配置中的绝对 Java、JAR 和配置执行 README 指定的方法：

  ```bash
  /absolute/path/to/java -jar /absolute/path/to/NGSViz.jar \
    -CP /absolute/path/to/NGSViz_setting.json \
    -DB /absolute/path/to/NGSViz_<genome>_RefSeq.db
  ```

- 主 DB 不可写：不要尝试原地合并；复制到用户可写的独立路径，更新配置后再合并。
- Java legacy 合并可能在打印 SQL 异常后仍返回成功退出码，因此不能只看退出码。合并后必须再次运行 `verify --db <main_db> --genome <assembly>`。
- 验证失败则保留备份和下载文件，报告错误，不进入 `validate-compute`。

安装成功后重新执行 `$ngsviz-setup`，再把已安装的 species/genome 列表交回 `$ngsviz-agent`。
