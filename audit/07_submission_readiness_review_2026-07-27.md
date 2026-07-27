# ngsViz 投稿前剩余问题复核

复核日期：2026-07-27

复核范围：当前 `HEAD`（`6561b9e`）中的 Java 计算后端、现有 Java 测试及相关参数路径。

复核方式：源码静态证据、最近提交差异、现有审计去重、Maven 测试和最小 Java 探针。

限制：本轮为只读初审，未修改源码、测试或科学行为。

## 结论

当前版本不建议直接投稿或发布。确认存在 6 个会影响覆盖度、归一化或多样本处理结果的问题，并且基线测试未通过。

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

## 建议修复顺序

1. 先修复链特异性过滤覆盖问题，并补充 duplicate、低 MAPQ、unmapped 与 same/opposite 组合测试。
2. 将每个样本的 Input 模式改为循环局部状态，并补充混合 `signal:input` 与单 BAM 列表测试。
3. 修复 Center 模式分支并明确其坐标语义。
4. 统一 coverage 与 library size 的过滤策略，同时排除 secondary/supplementary。
5. 明确 CIGAR/RNA-seq 支持边界，并实现 alignment-block coverage 或限制输入。
6. 加入 contig length 右边界裁剪和端粒附近回归测试。
7. 修复 `QueryGenomeRangeTest` 的配置缓存问题，恢复全套测试通过。
