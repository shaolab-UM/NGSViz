# Java 原生 CLI Phase 2 实施日志

## 变更

- 新增 `CliCommand`、`CliExitCode`、`CliResponse`、`CliDispatcher` 和 `DescribeCommand`。
- 按首个参数区分原生子命令与旧短参数流程；未知非短参数命令返回退出码 2。
- `describe --format json` 输出版本、命令、旧 flags、退出码及统一 JSON response。
- `validate-compute` 和 `run-compute` 仅校验 `--request <json>` 参数形态，随后返回 `unsupported` 和退出码 2；未读取请求文件。
- `--request` 与旧计算 flag 混用返回 `CLI_INPUT_MODE_CONFLICT` 和退出码 2。
- Maven 最终产物固定为 `target/NGSViz-1.0.jar`，应用版本保持 1.3。

## 测试与自查

- `mvn -Dtest=CliDispatcherTest,CliResponseTest test`：通过，9 个测试。
- `mvn test`：通过，12 个测试。
- `mvn -DskipTests package`：通过。
- `java -jar target/NGSViz-1.0.jar describe --format json`：通过，退出码 0，输出合法 JSON。
- 新增原生 CLI service 未调用 `System.exit`；进程退出仅由 `NGSViz.main` 决定。
