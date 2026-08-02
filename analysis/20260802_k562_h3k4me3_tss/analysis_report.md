# K562 H3K4me3 TSS 分析记录

- 输入：`Samples/K562_H3K4me3-ENCFF752MYF_5p.bam`
- 参考：hg19 RefSeq protein-coding transcript
- 区域：TSS 上下游各 2000 bp，101 个 coverage 位置
- 计算：MAPQ 20，single-end fragment extension 150 bp，CPM normalization
- Java 状态：`compute_manifest.json` 为 `completed`
- R 状态：`visualization_manifest.json` 为 `completed`

平均曲线和热图均显示 TSS 附近 H3K4me3 富集。平均 coverage 主峰位于 TSS 下游约 320 bp，峰值为 0.1346；TSS 位置为 0.1134。19,268 条记录中有 5,765 条全窗口零覆盖，占 29.92%。可复现统计见 `summary/tss_profile_summary.csv`。
