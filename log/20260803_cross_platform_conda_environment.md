# 跨平台 Conda 环境改造日志

日期：2026-08-03

## 改造内容

- 将原 Linux x86_64 完整导出保留为 `environment-linux-64.yml`。
- 将 `environment.yml` 改为 Linux x86_64、Intel macOS 和 Apple Silicon macOS 共用的直接依赖规范。
- 从 Shiny、R CLI、绘图脚本和 `buildRefDB.R` 提取 Java、R、CRAN 与 Bioconductor 直接依赖。
- 为 `linux-64`、`osx-64`、`osx-arm64` 生成 Conda explicit lock。
- 固定 R 4.4、Bioconductor 3.20 和 `EnrichedHeatmap 1.36.0`。
- 增加版本校验的 R 后安装脚本，解决 `EnrichedHeatmap` 缺少 macOS arm64 Conda 构建的问题。
- 将 `cxx-compiler` 和 `make` 纳入锁文件，支持三个平台从源码编译固定版本 R 包。

## 实测结果

- 三个平台均完成依赖求解并生成锁文件。
- macOS arm64 按 `osx-arm64.lock` 实际创建完整环境成功。
- Java 25.0.2 CLI contract smoke test 通过。
- R 4.4.3 CLI contract 与 R 回归测试通过。
- 使用 K562 H3K4me3 TSS 样例生成真实 average profile 和 coverage heatmap TIFF 成功。
- 使用最小 GTF 构建并校验 SQLite 参考 DB 成功。
- `EnrichedHeatmap 1.36.0` 从 Bioconductor 3.20 源码编译、加载和版本校验成功。

## 隔离与数据保护

- 完整环境和 smoke test 输出均写入 `/private/tmp`。
- 未覆盖原始样例、既有分析结果、数据库或用户配置。
