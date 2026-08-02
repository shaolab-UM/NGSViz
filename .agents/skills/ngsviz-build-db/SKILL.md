---
name: ngsviz-build-db
description: Validate a user-provided UCSC GTF, build an ngsViz SQLite reference database with lib/buildRefDB.R, verify its schema and records, and import it into the configured ngsViz database. Use when the requested species or genome assembly is not available in the README pre-built database catalog.
---

# ngsViz GTF 参考数据库构建

仅在 README 预构建目录没有目标物种或 genome 时使用。要求用户提供与 BAM 比对 assembly 完全一致的 UCSC GTF。

## 收集参数

必须明确确认：

- GTF 绝对路径。
- 物种 scientific name，例如 `Homo_sapiens`。
- genome assembly，例如 `hg38`。
- `DB_name`，例如 `RefSeq`。
- `annotatedNCRNA`：`TRUE` 或 `FALSE`。

不得猜测物种或 assembly。文件名必须为 `<genome>.<annotation>.gtf`；第一个点前字段必须等于确认的 genome。

## 输入检查

运行：

```bash
python3 scripts/check_gtf_for_ngsviz.py \
  --gtf /absolute/path/to/<genome>.<annotation>.gtf \
  --genome <assembly>
```

必须确认 GTF 至少 9 列、有 transcript/exon 记录、包含 `gene_name` 和 `transcript_id`。当前 `buildRefDB.R` 会保留 `NM`/`NR` transcript，因此输入必须是兼容的 UCSC RefSeq GTF；若只有 Ensembl transcript ID，停止并要求兼容 GTF，不得构建空库。NCBI accession 风格染色体名也必须先转换为 UCSC 风格。

## 构建

1. 在 `analysis/reference_db_build/<date>_<species>_<genome>/` 创建独立工作区，不移动或改写原始 GTF。
2. 预检查 R 及 `RSQLite`、`dplyr`、`rtracklayer`、`biomaRt`。缺失包会触发下载，执行前遵循平台审批。
3. 从构建工作区运行项目脚本，使输出写入该工作区的 `DB/<species>/`：

   ```bash
   Rscript /absolute/path/to/lib/buildRefDB.R \
     /absolute/path/to/<genome>.<annotation>.gtf \
     <DB_name> <species> <TRUE-or-FALSE>
   ```

4. 预期产物为 `DB/<species>/NGSViz_<genome>_<DB_name>.db`。不得覆盖既有构建产物。
5. 使用 `$ngsviz-install-db` 的 `manage_ngsviz_db.py verify` 检查目标 genome、坐标表存在性和非空记录。
6. 通过 `$ngsviz-install-db` 的安装流程导入主 DB；无主 DB 时直接将构建产物设为 `db_path`。
7. 导入后再次验证主 DB，并重新执行 `$ngsviz-setup`。

## 失败边界

- GTF 与 BAM assembly 不一致、必要属性缺失或构建结果为空时停止。
- 不自动从非 UCSC 来源下载或改写 GTF。
- 不因染色体同时带或不带 `chr` 而推断 genome assembly。
- 原始 GTF、构建 DB、导入前备份和日志分别保留。
