# 人工 ChIP-seq coverage 数值验证

原始数据来自 `/Users/benche/Desktop/myProj/ngsPlot/Test/synthetic_chipseq_validation`。本目录保存复制的 BAM、理论 coverage、独立 BAI、测试数据库、计算请求和 ngsViz 结果。原始数据及 ngsViz 正式参考数据库未修改。

运行参数：`fragment_length=20`、`scale_ratio=1`、`strand_specific=both`、`flank_region=0`、`num_datapoints=500`、无 control BAM。人工 reference 为 `chrSynthetic`，长度 500 bp。

验证结果（2026-10-04）：

- `validate-compute`：成功，警告 0，错误 0。
- `run-compute`：成功，`compute_manifest.json` 状态为 `completed`。
- ngsViz read count：18。
- 原始逐碱基 coverage：500/500 个位置与人工理论值一致；不匹配数 0，最大绝对误差 0，MAE 0。
- BEDTools v2.31.1：500/500 个位置与人工理论值一致。
- ngsViz 最终 coverage CSV：按源码的 buffer、分箱和边界规则变换理论值后，501/501 个输出点一致；不匹配数 0，最大绝对误差 0，MAE 0。
- `figure/Supplementary_Figure_3`：人工 reads、三方 coverage、Cluster B 局部结构及逐位置绝对误差，提供 PDF、PNG 和 600 dpi LZW TIFF。

原始 coverage 总和为 360；最终 CSV 的总和为 235，因为后者为分箱后数值，两者不应直接比较。

复核命令：

```bash
/usr/bin/java -cp NGSViz-1.3.jar validation/synthetic_chipseq/03_verify_raw.java \
  /Users/benche/Desktop/myProj/ngsPlot/NGSViz/validation/synthetic_chipseq/input/synthetic_chipseq.bam \
  /Users/benche/Desktop/myProj/ngsPlot/NGSViz/validation/synthetic_chipseq/input/expected_coverage.tsv
python3 validation/synthetic_chipseq/04_verify_matrix.py \
  validation/synthetic_chipseq/input/expected_coverage.tsv \
  validation/synthetic_chipseq/output/synthetic_chipseq_coverage_matrix_heatmap.csv
```

审稿回复建议：

> We thank the reviewer for this suggestion. We performed a numerical validation using a synthetic coordinate-sorted BAM containing 18 unspliced 20-bp reads aligned to a 500-bp reference with analytically defined overlap patterns. With fragment length set to 20 bp and raw-count scaling, ngsViz's per-base coverage matched the analytical expectation at all 500 positions (0 mismatches; maximum absolute error = 0; MAE = 0). BEDTools `genomecov` independently matched the same expected coverage at all 500 positions. We also compared the exported ngsViz coverage matrix with the analytical coverage after applying ngsViz's buffer and binning transformations; all 501 output values matched (maximum absolute error = 0; MAE = 0).

将验证方法、结果和数据集实际补入修订稿及补充材料后，再在回复中注明相应位置。
