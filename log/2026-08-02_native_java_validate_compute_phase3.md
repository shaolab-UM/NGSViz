# Java 原生计算校验 Phase 3 实施日志

## 实施范围

- 新增 `validate-compute --request <json>`，直接将 JSON 解析为单样本 `ComputeRequest`。
- 严格限制请求字段；未知字段、`samples`、字段类型错误和必填字段缺失返回 exit code 2。
- 保留 `run-compute` 未实现状态；新校验入口不调用 `MainCalculator` 或 R。

## 只读校验

- 解析 system config，并输出配置版本、数据库路径和当前 JAR 信息。
- 通过 SQLite `mode=ro` 连接执行 `PRAGMA integrity_check`，核验数据库组合、坐标表和染色体。
- 使用 HTSJDK 核验 signal/control BAM 的可读性、header、BAI、reference 命名风格及数据库染色体交集。
- 核验 BED 至少三列及 0-based half-open 坐标。
- 核验参数默认值、类型、范围、枚举和数据库参数组合。
- 检测已有 ngsViz 产物；校验过程不创建或修改输出目录。

## 测试与验收

- 先新增失败测试，再完成实现；覆盖合法请求、元数据、输入模式冲突、`samples`、缺失字段、参数范围、数据库、BAM/BAI、reference、BED 和已有产物。
- `mvn -Dtest=ComputeRequestReaderTest,ComputeValidatorTest,DatabaseInspectorTest,BamInspectorTest,BedInspectorTest test`：17 个测试通过。
- `mvn test`：30 个测试通过。
- `mvn -DskipTests package`：打包成功。
- 示例 JAR 命令返回 exit code 0 和单一 JSON response envelope；示例输出目录未创建。

## 已知构建提示

- Maven 仍报告既有的重复 `commons-math3` 依赖、动态 `annotations` 版本、compiler plugin 未固定版本及 shade 重叠资源提示；本阶段未修改这些非目标配置。
