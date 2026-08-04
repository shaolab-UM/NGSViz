# 背景归一化伪计数修复

## 问题

- 原实现固定使用 `1e-6`，与 CPM 中一个原始 read 对应的尺度不一致。
- 修改过程中产生的 `BackgroudNormalizer.java` 缺少类声明，且 `MainCalculator.java` 包含无效静态导入，导致 Java 9 编译失败。
- Signal 使用 spike-in `scale_ratio` 时，Signal 伪计数仍按 CPM 计算，与矩阵单位不一致。

## 修复

- 保留已有 `BackgrouNormalizer` 类名和 `backgroundNormalize` 方法名。
- CPM Signal 使用 `1e6 / signalLibrarySize` 作为伪计数。
- spike-in Signal 使用 `signalScaleRatio` 作为伪计数。
- Input 继续使用 CPM，伪计数为 `1e6 / inputLibrarySize`。
- 增加矩阵维度与文库大小校验。
- 新增 CPM 和 spike-in 两种路径的回归测试。

## 自查

- 定向测试：`mvn -Dtest=BackgrouNormalizerTest test`
- 全量测试：`mvn test`
