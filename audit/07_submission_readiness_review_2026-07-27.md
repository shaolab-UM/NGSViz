# ngsViz 投稿前剩余问题复核

复核日期：2026-07-27

复核范围：当前 `HEAD`（`6561b9e`）中的 Java 计算后端、现有 Java 测试及相关参数路径。

复核方式：源码静态证据、最近提交差异、现有审计去重、Maven 测试和最小 Java 探针。

限制：本轮为只读初审，未修改源码、测试或科学行为。

## 结论

当前版本不建议直接投稿或发布。确认存在 11 个会影响覆盖度、归一化、多样本处理或 exon 模式结果的问题，并且基线测试未通过。

## 已确认问题

### [P1] 链特异性判断会覆盖 duplicate、MAPQ 和 unmapped 过滤结果

证据：

- `src/main/java/com/NGSViz/ReadBam/ReadRecordProcess.java:110-125` 先将 duplicate、低 MAPQ 或 unmapped read 的 `filter_res` 设为 `true`。
- `src/main/java/com/NGSViz/ReadBam/ReadRecordProcess.java:127-134` 在 `strand_spec != "both"` 时直接重新赋值 `filter_res`，而不是与已有结果做逻辑或。
- `src/main/java/com/NGSViz/coverageCalculator/PhysicalCoverageCalculator.java:152-155` 完全依赖该返回值决定是否计入覆盖度。

最小触发条件：

- `strand_spec=same`；
- read 与查询区域同链；
- read 同时为 duplicate 或 `MAPQ < min_mapq`。

实际行为：

- 最小 Java 探针得到：
  - `lowMapqSameStrandFiltered=false`
  - `duplicateSameStrandFiltered=false`
- 两类本应被过滤的 reads 均重新通过。

预期行为：

- 链过滤只能增加过滤条件，不能清除 duplicate、MAPQ 或 unmapped 的既有过滤结果。

影响：

- 所有启用 `-SS same` 或 `-SS opposite` 的分析都可能计入低质量或重复 reads，直接改变 coverage、read count 和归一化结果。

### [P1] 多样本列表中的 Input/IgG 模式会泄漏到后续样本

证据：

- `src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java:32` 将 `input_mode` 定义为静态状态，初始值为 `false`。
- `MainCalculator.java:46-56` 每轮只在检测到 `signal:input` 时将其设为 `true`，没有在新一轮开始时复位为 `false`，也没有清空 `bam_file_bkg`。
- `MainCalculator.java:93-105` 后续所有轮次只依据持久化的 `input_mode` 决定是否进行背景校正。

最小触发条件：

- `bam_list` 至少包含两个样本；
- 前一个元素为 `signal1.bam:input1.bam`；
- 后一个元素为不带背景的 `signal2.bam`。

实际行为：

- 第二个样本仍进入 Input 模式，并复用上一轮的 `input1.bam`。

预期行为：

- Input 模式和背景 BAM 应是每个样本元素的局部状态。

影响：

- 后续样本可能被错误执行 `log2(signal/input)`，产生无提示的错误结果。

### [P1] Center 模式计算结果被普通 gene-body scaling 覆盖

证据：

- `src/main/java/com/NGSViz/coverageCalculator/DataScaler.java:29-35` 在 `CenterMode=true` 时计算中心对齐结果。
- `DataScaler.java:39-68` 随后无条件继续执行普通 broad-interval 分段缩放，并写入同一个 `coverage_scaled`。
- `DataScaler.java:70-71` 返回的是被后续逻辑改写后的数组。

实际行为：

- `-CM true` 不会稳定返回 `centerScaleByGeneCenter()` 的结果。

预期行为：

- Center 分支完成后应返回中心对齐结果，或与普通 gene-body 分支互斥。

影响：

- README 中公开的 Center Mode 功能与实际计算不一致。

### [P1] 覆盖度跨度忽略 CIGAR

证据：

- `src/main/java/com/NGSViz/coverageCalculator/PhysicalCoverageCalculator.java:191-201`：
  - paired-end 直接使用 TLEN；
  - single-end 使用 `getReadLength()` 和固定 `frag_len`。
- `PhysicalCoverageCalculator.java:208` 将上述值作为连续参考区间。
- 当前覆盖度路径未使用 CIGAR alignment blocks。

实际行为：

- soft clipping、deletion、reference skip (`N`) 等均被当作连续覆盖区间。

预期行为：

- 覆盖度应按 CIGAR 中真正消耗参考序列且代表比对碱基的 blocks 计算；若不支持剪接 BAM，应在文档和输入校验中明确拒绝。

影响：

- soft-clipped reads 的覆盖跨度不准确；
- exon 模式处理含 `N` 的 RNA-seq BAM 时可能把内含子错误计为有覆盖。

### [P1] secondary 和 supplementary alignments 仍被计入

证据：

- `src/main/java/com/NGSViz/ReadBam/ReadRecordProcess.java:102-136` 过滤 duplicate、MAPQ、unmapped 和 strand，但未过滤 secondary/supplementary flags。
- `src/main/java/com/NGSViz/ReadBam/BAMAttribute.java:134-139` 文库大小统计同样未排除 secondary/supplementary alignments。
- `src/main/java/com/NGSViz/coverageCalculator/PhysicalCoverageCalculator.java:124-165` 的批量覆盖路径没有独立排除这两类记录。

实际行为：

- 多重比对和嵌合/补充比对可能同时进入 coverage、read count 和 library size。

预期行为：

- 默认排除 secondary 与 supplementary alignments，或提供明确、可复现的参数控制。

影响：

- 可能系统性夸大局部覆盖度；在重复序列、结构变异或嵌合 reads 较多的数据中影响更明显。

### [P1] 染色体右边界未裁剪，端粒附近会引入人工零覆盖

证据：

- `src/main/java/com/NGSViz/ReadBam/QueryGenomeRange.java:58-62` 只将左边界裁剪到 1。
- `QueryGenomeRange.java:63-64` 直接计算 `end_pos + flank_size + buf_size`，未与 BAM header 中的 contig length 比较。
- 覆盖数组长度由完整查询区间决定，超出染色体的部分仍会保留为零。

实际行为：

- 位于 contig 末端附近的区域会包含不存在的参考坐标，并把对应零值带入后续分箱。

预期行为：

- 查询右边界应裁剪到对应 contig length，同时保证后续 scaling 正确处理不对称 flank。

影响：

- 端粒附近基因或自定义区域的尾部 profile 被人为压低。

### [P2] CPM 分母与覆盖度分子的过滤集合不一致

证据：

- `src/main/java/com/NGSViz/ReadBam/BAMAttribute.java:134-139` 的 library size 接受所有 `MAPQ >= 0`、非 duplicate、mapped reads。
- `src/main/java/com/NGSViz/ReadBam/ReadRecordProcess.java:112-121` 的 coverage 使用用户配置的 `min_mapq`，默认值为 20。
- `src/main/java/com/NGSViz/coverageCalculator/EachCoverageResult.java:35-36` 使用上述 library size 作为 CPM 分母。

实际行为：

- `MAPQ 0..19` 的 reads 进入分母但不进入 coverage 分子；paired-end、strand、secondary/supplementary 的统计口径也不完全一致。

预期行为：

- CPM 分子和分母使用同一套明确的 read/filter 单位。

影响：

- CPM 值被系统性压低，不同 BAM 中低 MAPQ 比例差异会转化为额外批次偏差。

### [P0] exon 模型在不同 transcript 之间共享并覆盖坐标列表

证据：

- `src/main/java/com/NGSViz/sqldbOperate/exonMode/ExonModelData.java:31-33` 只创建一次 `start_list`、`end_list` 和 `width_list`。
- `ExonModelData.java:49-52` 把这些列表传给 `OneEnstAllExonCoorClass`。
- `ExonModelData.java:55-57` 随后直接 `clear()` 同一批列表，并继续为下一个 transcript 复用。
- `src/main/java/com/NGSViz/sqldbOperate/exonMode/OneEnstAllExonCoorClass.java:35-37` 直接保存传入引用，没有进行 defensive copy。

最小验证：

- 创建 `tx1` 并传入 `[100, 200]`、`[150, 250]`；
- 清空原列表并写入下一 transcript 的 `[900]`、`[950]`；
- `tx1.getStartList()` 和 `tx1.getEndList()` 随即变成 `[900]`、`[950]`。

实际行为：

- 已存入 `exon_matrix` 的 transcript 会随着后续循环被改写；
- 多个 transcript 最终可能共享最后处理的一组 exon 坐标。

额外证据：

- `ExonModelData.java:59-64` 开始新 transcript 时没有设置 `end_enst = query_end`；
- `end_enst` 只在同一 transcript 的后续行中于 `ExonModelData.java:65-72` 更新；
- 单外显子 transcript 可能继承上一 transcript 的 `end_enst` 或保留初始值 0。

预期行为：

- 每个 transcript 必须保存独立的坐标列表副本；
- 新 transcript 第一条记录应同时初始化 start 和 end；
- SQL 查询应明确按 `tid` 和 exon 坐标排序，不能依赖未声明的返回顺序。

影响：

- 当前 exon 模式可能把其他 transcript 的 exon 坐标用于覆盖度提取，属于投稿阻断级科学正确性问题。

### [P1] exon coverage 提取存在闭区间错误和零填充污染

证据：

- `src/main/java/com/NGSViz/sqldbOperate/exonMode/CoverageExonSubset.java:24-25` 将数据库 exon 坐标映射为数组索引。
- `CoverageExonSubset.java:32-34` 使用 `j < exon_end`，对 1-based closed exon 坐标漏掉每个 exon 的最后一个碱基。
- `CoverageExonSubset.java:21` 按完整基因组查询范围分配输出数组，而不是按 exon 总宽分配。
- `CoverageExonSubset.java:22-35` 只从数组前端连续写入 exon coverage，剩余位置保持为零。
- `src/main/java/com/NGSViz/sqldbOperate/ProcessEachQueryCoorRecord.java:104-112` 随后直接对该零填充数组执行 buffer trimming 和 scaling。

实际行为：

- exon 末端碱基丢失；
- 输出包含与 exon 总宽无关的尾部零值；
- 这些零值进入后续分箱，压低 exon profile。

预期行为：

- 按内部 1-based closed 语义完整提取 `[exon_start, exon_end]`；
- 输出数组长度应等于所选 exon 的总参考宽度；
- buffer/flank 与 exon 拼接的顺序和坐标语义应明确。

### [P1] broad-interval 尾侧分箱重复首 bin 并丢失末 bin

证据：

- 默认配置下 `DataScaler.java:17-20` 得到内部 `num_datapoints=101`、`flank_points=21`、`middle_points=61`。
- `DataScaler.java:60-63` 把 `tail_cov_scaled[0..19]` 写入结果索引 `81..100`。
- `DataScaler.java:65` 又使用 `tail_cov_scaled[0]` 构建索引 80 的边界混合点。
- `tail_cov_scaled[20]` 在整个拼接过程中从未使用。

最小验证：

- 使用 21 个 head bins、61 个 middle bins 和 21 个 tail bins；
- 输出索引 80 为预期混合值 85；
- 输出索引 81 再次使用 `tail[0]=100`；
- 输出索引 100 为 `tail[19]=119`，最后的 `tail[20]=120` 丢失。

实际行为：

- 尾侧第一个 bin 被重复贡献；
- 最远端尾侧 bin 被丢弃；
- broad-interval profile 的左右侧翼不对称。

预期行为：

- 边界混合点使用 `tail[0]` 后，后续位置应使用 `tail[1..20]`。

### [P1] RShiny 默认 scale ratio 会绕过 CPM

证据：

- `lib/shiny/ui_builder.R:73` 将 `dashS` 默认值设为 1。
- `lib/shiny/server_builder.R:186` 每次运行都把该值作为 `-S` 传给 Java。
- `src/main/java/com/NGSViz/configSet/InputParameterAttributes.java:53` 的 CLI 默认值为 `null`。
- `src/main/java/com/NGSViz/coverageCalculator/EachCoverageResult.java:29-36` 在 `scale_ratio != null` 时只执行 `coverage * scale_ratio`，不执行 CPM。

实际行为：

- Shiny 默认运行传入 `-S 1`，得到原始 coverage，而 CLI 默认运行得到 CPM；
- 同一数据在 Shiny 与 CLI 中产生不同量纲。

预期行为：

- Shiny 默认不传 `-S`，或提供可表达 `null` 的开关；
- 只有明确启用 spike-in calibration 时才传入 scale ratio。

## 发布阻断项

### [P1] 当前 Maven 测试不通过

执行：

```bash
mvn test
```

结果：

- 共 15 个测试；
- 14 个通过；
- `src/test/java/com/NGSViz/ReadBam/QueryGenomeRangeTest.java:46` 失败；
- 期望查询起点为 85，实际为 80。

证据链：

- 测试在 `QueryGenomeRangeTest.java:42-43` 修改 `InputParameterAttributes.buf_size` 和 `flank_region`。
- `src/main/java/com/NGSViz/ReadBam/QueryGenomeRange.java:17-23` 在类加载时把这些参数复制到静态字段，之后测试修改不会刷新缓存。

判定：

- 这是确认的回归测试失败。
- 是否属于 CLI 运行时错误仍需结合“一个 JVM 是否支持多次配置/多任务调用”的接口约定决定；但投稿前构建不能保持红灯。

## 已排除或未重复列入

- BED 的 0-based half-open 到内部 1-based closed 转换已在 `BedDBProcess.java:54-57` 修复。
- BAM 索引缺失、索引损坏和非 coordinate-sorted BAM 已有测试覆盖并通过。
- `Transcript` 共享记录 Map 问题已由 context-based 路径替代，本轮未发现对应回归证据。
- POM 的重复依赖、动态 `RELEASE` 版本和缺失 compiler plugin 版本会产生构建警告，但本轮未将其计为科学结果 bug。
- `processJSON.R` 返回的 Java/JAR/数据库四元素向量由 Shiny 按索引拆解，并非直接作为 Java 命令执行；不列为启动参数 bug。
- `PhysicalCoverageCalculator.calculatePhysicalCoverage2()` 的 `queryContained` 只由仓库内未被主流程调用的旧 `processRecord2()` 使用；暂列旧 API 风险，不计入当前批量主路径 bug。
- `BAMAttribute.detectChromosomeFormat()` 接受所有以 `chr` 开头的 contig，因此 `chrUn_*` 不会被拒绝；不采用该项 Kimi 候选描述。

## Kimi Code 独立复核

- Kimi Code 会话恢复后独立支持了原报告中的 8 项判断，没有提供反证。
- Kimi 新增候选经主审查员复核后：
  - 接受：exon coverage 提取、Shiny scale ratio、broad-interval 尾侧分箱问题；
  - 纠正：broad-interval 问题不是索引 80 被覆盖，而是 `tail[0]` 重复、`tail[20]` 丢失；
  - 降级：`queryContained` 仅影响当前仓库未调用的旧 API；
  - 排除：`processJSON.R` 命令向量和 `chrUn_*` contig 两项描述。
- 主审查员额外发现并验证了 Kimi 未报告的 exon 坐标列表共享问题。

## 建议修复顺序

1. 先修复 exon 模型的共享列表、end 初始化和查询排序问题，并为多 transcript/单 exon 情形添加测试。
2. 修复 exon coverage 拼接的闭区间和输出长度问题。
3. 修复链特异性过滤覆盖问题，并补充 duplicate、低 MAPQ、unmapped 与 same/opposite 组合测试。
4. 将每个样本的 Input 模式改为循环局部状态，并补充混合 `signal:input` 与单 BAM 列表测试。
5. 修复 Center 模式与 broad-interval 尾侧分箱，并明确坐标语义。
6. 统一 coverage 与 library size 的过滤策略，同时排除 secondary/supplementary。
7. 明确 CIGAR/RNA-seq 支持边界，并实现 alignment-block coverage 或限制输入。
8. 修正 Shiny 默认归一化行为，加入 Shiny/CLI 等价性测试。
9. 加入 contig length 右边界裁剪和端粒附近回归测试。
10. 修复 `QueryGenomeRangeTest` 的配置缓存问题，恢复全套测试通过。
