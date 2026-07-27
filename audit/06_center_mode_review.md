# Center 模式（`-CM`）专项审查

- 日期：2026-07-24
- 范围：CLI 参数、Java coverage scaling、染色体边界、JSON/R 绘图契约、现有测试
- 结论：`-CM true` 当前存在确定性科学逻辑错误，不应将现有 center-mode 结果用于正式分析或投稿图。

## 已确认问题

### [P1] center scaling 结果被普通 gene-body scaling 无条件覆盖

证据链：

1. `src/main/java/com/NGSViz/coverageCalculator/DataScaler.java:29-35` 在 `CenterMode == true` 时计算并赋值 `centerScaleByGeneCenter(...)`。
2. 方法没有立即返回，也没有用 `else` 隔离后续路径；`DataScaler.java:39-68` 随后无条件执行普通 broad-interval 的 head/mid/tail 分箱并重新写入同一个 `coverage_scaled`。
3. `src/main/java/com/NGSViz/configSet/DataPointNum.java:16-18` 在 center 模式下设置 `middle_points = 1`、`flank_points = num_datapoints / 2`。因此被误执行的普通拼接路径把整个 gene body 压成一个中间统计值。
4. `DataScaler.java:64-65` 连续写入同一个中心索引；第二次写入又覆盖第一次写入。

最小复现：

```bash
mvn -q -DskipTests compile
javac -cp target/classes -d /tmp audit/scripts/01_center_mode_probe.java
java -cp target/classes:/tmp CenterModeProbe
```

输入为 200 bp 左侧值 `1`、600 bp gene body 值 `7`、200 bp 右侧值 `2`，输出为：

```text
public_center_bin=4.5
public_gene_bins=0
private_center_gene_bins=60
public_matches_private_center=false
Exception in thread "main" java.lang.AssertionError:
Center-mode result is overwritten by broad-interval scaling
```

影响：

- `-CM true` 的最终 coverage matrix 不是 `centerScaleByGeneCenter()` 的结果。
- gene-body 信号被压缩并与右侧首个 flank bin 求平均；宽 gene-body 信号可从矩阵主体中消失。
- `Samples/Result/K36me3_center/` 中现有示例由该路径生成，不能作为 center 模式正确性的验证。

### [P1] Java 计算坐标与 R 轴上的 `-flank / Center / +flank` 含义不一致

证据链：

1. `src/main/java/com/NGSViz/sqldbOperate/ProcessEachQueryCoorRecord.java:89-92` 在 center 模式下仍按 broad interval 查询 `start - flank` 到 `end + flank`，并未改为以 region center 为基准的固定宽度窗口。
2. `DataScaler.java:82-103` 把包含完整 region 和两侧 flank 的整个数组均匀映射到全部数据点。公式可化简为：

   ```text
   bin = floor(i / (L - 1) * (num_datapoints - 1))
   ```

   `gene_center` 和 `flank_size` 在映射公式中完全抵消。
3. `src/main/java/com/NGSViz/configSet/GenerateJsonConfig.java:101-103` 却把该结果声明为 `point_interval`，并将标签设为 `Center`。
4. `lib/coverageHeatmap.R:124-135` 使用 `flank_size` 把矩阵两端标为 `-flank_size` 和 `+flank_size`。

影响：

- 对长度为 `W` 的 region，Java 矩阵两端实际对应中心两侧约 `W/2 + flank_size`，而图上标为 `±flank_size`。
- 不同长度 region 的每个数据点代表不同 bp 宽度，当前图轴却表达为固定距离，造成可重复的坐标误释。
- 即使只修复第一个覆盖问题，center 模式的图轴语义仍不正确；需先明确 center 模式究竟是“固定中心窗口”还是“保留完整 region 后按比例缩放”。

### [P1] 染色体左边界会进一步移动 center

证据链：

1. `src/main/java/com/NGSViz/ReadBam/QueryGenomeRange.java:58-64` 将越过染色体左端的 `query_start` 截断为 `1`。
2. `src/main/java/com/NGSViz/coverageCalculator/TrimBuffer.java:11-14` 不知道发生过截断，仍从左右各删除完整 `buf_size`。
3. `DataScaler.java:83-86` 仍假定删除 buffer 后存在完整左 flank，并以 `gene_start = flank_size` 推导 center。

例如 region `[1,100]`、`flank=50`、`buffer=10` 时，查询区间被截为 `[1,160]`；再固定删除两端各 10 bp 后只剩 140 bp，但 center 逻辑仍把索引 50 当作 gene start。边界附近 region 的中心因此发生系统偏移。

影响：

- 靠近染色体起点的 region 与其他 region 不能正确按中心对齐。
- 此问题同时影响普通 broad mode，但 center 模式的核心目的正是位置对齐，因此属于阻断性边界条件。

### [P2] `-CM` 非法值会静默变成 `false`

- `src/main/java/com/NGSViz/configSet/GetInputParameterValue.java:72-75` 使用 `Boolean.parseBoolean()`；除大小写形式的 `"true"` 外，任意字符串都返回 `false`。
- `src/main/java/com/NGSViz/configSet/CheckLegal2InputParameter.java:16-42` 没有检查 `-CM` 的原始值。

例如 `-CM ture` 不报错，程序会静默运行普通模式，降低命令行分析的可复现性。

## 测试状态

- `mvn test`：失败；共 15 个测试，14 个通过，1 个失败。
- 失败项：`src/test/java/com/NGSViz/ReadBam/QueryGenomeRangeTest.java:46`，期望 `85`，实际 `80`。
- 该失败来自 `QueryGenomeRange` 对配置值的静态快照，与本次 center-mode 主问题不同，但说明当前完整测试基线不是绿色。
- `src/test/` 中没有 `DataScaler` 或 `CenterMode` 回归测试，因此上述错误未被现有测试覆盖。

## 修复前置条件

在修改科学行为前，需要先确认 center 模式的唯一目标定义：

1. 固定窗口模式：以每个 region 的 center 为零点，仅统计 `center ± flank_size`。
2. 完整 region 模式：保留整个 region 及 flank，再进行比例缩放；此时横轴不能标成固定的 `±flank_size bp`。

确认定义后，应先添加失败测试，再分别修复计算分支、边界坐标和 JSON/R 轴契约；每项独立提交。
