# AI-friendly 原生架构 Phase 0 基线报告

## 基线范围

- 采集日期：2026-07-30（Asia/Shanghai）
- 采集方式：只读检查；未修改 Java/R 代码、原始数据或示例产物
- 校验算法：SHA-256
- 基线 Git commit：`bbd9fce6f9856968f4f48fa65eb4a596145dcc49`
- commit 摘要：`docs: 明确多样本顺序计算与延迟绘图`
- 采集时分支状态：`main...origin/main [ahead 7]`
- 采集前已有未提交修改：`docs/superpowers/plans/2026-07-30-ngsviz-native-ai-friendly-phased-prompts.md`；本 Phase 不触碰、不纳入提交

## 运行环境

### Java

```text
openjdk version "22.0.2" 2024-07-16
OpenJDK Runtime Environment Corretto-22.0.2.9.1 (build 22.0.2+9-FR)
OpenJDK 64-Bit Server VM Corretto-22.0.2.9.1 (build 22.0.2+9-FR, mixed mode, sharing)
```

### R

```text
Rscript (R) version 4.5.0 (2025-04-11)
```

## Maven 测试基线

命令：

```bash
mvn test
```

结果：`BUILD FAILURE`，退出码 `1`。

```text
Tests run: 3, Failures: 2, Errors: 0, Skipped: 0
CliOptionsTest.helpPrintsEveryCommandOptionAndStopsBeforeAnalysis
  expected: <0> but was: <255>
CliOptionsTest.versionPrintsCurrentVersionAndStopsBeforeAnalysis
  expected: <0> but was: <255>
```

已观察到的构建警告：

- `org.apache.commons:commons-math3:3.6.1` 重复声明。
- `org.jetbrains:annotations` 使用已弃用的 `RELEASE` 版本选择方式。
- `maven-compiler-plugin` 未显式声明版本。
- Java 9 编译目标未设置 system modules location。

以上均作为 Phase 0 既有基线记录，本 Phase 不修复。

## SQLite 基线

命令：

```bash
sqlite3 -readonly database/genomeCoordinate.db "PRAGMA integrity_check;"
```

结果：

```text
ok
```

现有表：

```text
defaultTbl
hg19_RefSeq
hg38_RefSeq
mm39_RefSeq
```

### 数据库支持的 genome/database/region

以下组合来自 `defaultTbl` 的去重语义；同一组合还分别记录了分析类型、biotype、标签和默认 flank size。

| genome | database | coordinate table | region |
|---|---|---|---|
| `hg19` | `RefSeq` | `hg19_RefSeq` | `exon` |
| `hg19` | `RefSeq` | `hg19_RefSeq` | `genebody` |
| `hg19` | `RefSeq` | `hg19_RefSeq` | `tes` |
| `hg19` | `RefSeq` | `hg19_RefSeq` | `tss` |
| `hg38` | `RefSeq` | `hg38_RefSeq` | `exon` |
| `hg38` | `RefSeq` | `hg38_RefSeq` | `genebody` |
| `hg38` | `RefSeq` | `hg38_RefSeq` | `tes` |
| `hg38` | `RefSeq` | `hg38_RefSeq` | `tss` |
| `mm39` | `RefSeq` | `mm39_RefSeq` | `exon` |
| `mm39` | `RefSeq` | `mm39_RefSeq` | `genebody` |
| `mm39` | `RefSeq` | `mm39_RefSeq` | `tes` |
| `mm39` | `RefSeq` | `mm39_RefSeq` | `tss` |

数据库中未记录 `mm10` coordinate table；`database/mm10_intergenic_regions_greater_1kb.bed` 是独立 BED 文件，不等同于 SQLite 内置组合。

## 示例 BAM/BAI 基线

| 文件 | 字节数 | SHA-256 |
|---|---:|---|
| `Samples/K562_H3K36me3-ENCFF975JFV_5P.bam` | 33116865 | `af2e88bebd3aa4653af806e1e74be6b7c7a05e4dac6ccbcb32a20b4bb3338aa7` |
| `Samples/K562_H3K36me3-ENCFF975JFV_5P.bam.bai` | 4970384 | `4637ab6eadcf2d21a471254674a6198418cf1db86410c1ebd730a9734c1462f8` |
| `Samples/K562_H3K4me3-ENCFF752MYF_5p.bam` | 52059606 | `37f3e47782f98b5a67a0ee593dc6035da1f2f3b8c339a195db31707842fe64f2` |
| `Samples/K562_H3K4me3-ENCFF752MYF_5p.bam.bai` | 5243112 | `9b79b46c6aa81491d72726f59dcb666ec2c1d196b31bc8c9ecf428416422f32e` |

## Samples/Result 现有产物基线

### H3K4me3_point

| 文件 | 类型 | 字节数 | SHA-256 |
|---|---|---:|---|
| `H3K4me3_NGSViz_plotSetting.json` | JSON | 1931 | `a3218f7c2fcd92539538bb171c7cc91ba5111b1e0b0d929572314fe8d758f8e8` |
| `K562_H3K4me3-ENCFF752MYF_5p_coverage_matrix_heatmap.csv` | CSV | 16935053 | `56ce8110d927e6d68f41862c08c678c4dd1bdf86c0d3fb86bb5ce6c4adb8367d` |
| `K562_H3K4me3-ENCFF752MYF_5p_gene_read_count.csv` | CSV | 399527 | `4c56a58bd7e1d2904ae4c1d7c1954966952591bed5778f1b0aad687758c6aa02` |
| `coverage_heatmap.tiff` | 图片 | 8031774 | `acc29da240a8aa500390127a1e06b7dd6b536779fd485632cc5d1b3d1043cdb9` |
| `merge_average_plot.tiff` | 图片 | 4217196 | `27d48eeb6b503090b0149b59b6677cfc9588b888130d40de80f1704c1695e442` |

### K36me3_broad

| 文件 | 类型 | 字节数 | SHA-256 |
|---|---|---:|---|
| `H3K36me3_NGSViz_plotSetting.json` | JSON | 1973 | `80da0ee8842fa12b6b37a38bb3d7ba0f59436708102d5da6fe2ec812a5a93e08` |
| `K562_H3K36me3-ENCFF975JFV_5P_coverage_matrix_heatmap.csv` | CSV | 9302637 | `e47a99a247cbbb4b2cc1480c72c4504aa7b5df54e4b68f9b939c750d68088f6d` |
| `K562_H3K36me3-ENCFF975JFV_5P_gene_read_count.csv` | CSV | 400312 | `4027e7147555f8c1b5c7925f0966cd7f73b65a44da7bfbdd5c49928343a09e07` |
| `coverage_heatmap.tiff` | 图片 | 8031774 | `4ac2adcb6b66e88c401b701239163ec2174db41845bc0e8f9397f6d86536d818` |
| `merge_average_plot.tiff` | 图片 | 4217196 | `2115f91b841866a5c5e8c28eac671350a53ec683c134ec30d3370f2d7f046827` |

### K36me3_center

| 文件 | 类型 | 字节数 | SHA-256 |
|---|---|---:|---|
| `H3K36me3_NGSViz_plotSetting.json` | JSON | 1934 | `2ae9be2b1200787d52b648484218e06fe404ea2cbe98aec183b11c26deacdd41` |
| `K562_H3K36me3-ENCFF975JFV_5P_coverage_matrix_heatmap.csv` | CSV | 9468437 | `d9bf86b3bbf5dd586a065ed1337843e1f0267b981ee124739ea7091a2fe70e23` |
| `K562_H3K36me3-ENCFF975JFV_5P_gene_read_count.csv` | CSV | 400312 | `4027e7147555f8c1b5c7925f0966cd7f73b65a44da7bfbdd5c49928343a09e07` |
| `coverage_heatmap.tiff` | 图片 | 8031774 | `be47d35194c127a9285c9d49b551db9a565cc64d272128718694ffddd506ebff` |
| `merge_average_plot.tiff` | 图片 | 4217196 | `f6953bcb4052ee6aa6f54c39a5c7558160ac8fd6d5c3bb5858682e6bf326afa5` |

### K36me3_intergenic

| 文件 | 类型 | 字节数 | SHA-256 |
|---|---|---:|---|
| `H3K36me3_NGSViz_plotSetting.json` | JSON | 1988 | `61a6a144435099f3f5cf587a687620980acb75deffe1e615b6387468da56a03a` |
| `K562_H3K36me3-ENCFF975JFV_5P_coverage_matrix_heatmap.csv` | CSV | 7783721 | `2908771c0280480a4a93d348d16fa83b40920d2defaff120f1490d0ac1b4f0e1` |
| `K562_H3K36me3-ENCFF975JFV_5P_gene_read_count.csv` | CSV | 780514 | `314634d4431ebc58e4461bb4cc86128d1e400ea7c36da87bc522126eb2e24e10` |
| `coverage_heatmap.tiff` | 图片 | 8031774 | `af7e14eb61526689d48852566994be17ce94b0b2ec8379e1b47c9f7e6c0bf7c7` |
| `merge_average_plot.tiff` | 图片 | 4217196 | `afc0a9069aa843112dd68bcda5d289d095f78d643a795de706dd0bfdbececee6` |

## 文件契约基线

- Java 计算端生成 coverage matrix CSV、read count CSV 和 `*_NGSViz_plotSetting.json`。
- R CLI 接收包含 JSON 的目录，先发现 JSON，再从 `OutputFile` 字段读取 CSV 路径。
- plot-setting JSON 当前版本字段为 `versionParameters.version = "1.0"`。
- Java/R 交接入口以现有 plot-setting JSON 为唯一文件契约；本 Phase 未新增其他接口。

## Phase 0 结论

- 架构边界已冻结到 `docs/ai-friendly/00_原生架构与协作边界.md`。
- SQLite 完整性检查通过。
- Maven 基线存在 2 个 CLI 退出码测试失败，后续 Phase 应先增加或调整回归测试，再经明确批准修复；不得在未授权情况下改变科学行为。
