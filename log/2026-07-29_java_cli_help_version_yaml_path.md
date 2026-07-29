# Java CLI 帮助、版本与 YAML 路径参数

- 新增 `-H`，打印 Java CLI 全部参数帮助并在计算前退出。
- 新增 `-V`，打印 `ngsViz 1.3` 并在计算前退出。
- 新增 `-J <yaml>`，保存 AI 工作流传入的 YAML 配置文件路径。
- Maven 项目版本及 Java 内部版本号同步更新为 `1.3`。
- README 中的 JAR 版本示例和参数说明同步更新。
- 新增 `CliOptionsTest`，覆盖帮助完整性、版本输出和 YAML 路径解析。

## 自查

- `mvn test`：3 个测试全部通过，0 失败、0 错误。
- `mvn package`：成功生成 `target/NGSViz-1.3.jar`。
- `java -jar target/NGSViz-1.3.jar -H`：退出码 0，打印全部参数。
- `java -jar target/NGSViz-1.3.jar -V`：退出码 0，打印 `ngsViz 1.3`。
