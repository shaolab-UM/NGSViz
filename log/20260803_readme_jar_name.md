# README JAR 文件名更新

- 将 `README.md` 中全部 `NGSViz-1.0.jar` 更新为 `ngsViz-1.3.jar`。
- 同步更新构建说明与配置示例中的 JAR 名称。
- 执行 `java -jar ngsViz-1.3.jar -V`，确认输出为 `ngsViz 1.3`。
- 执行旧名称残留检查与 `git diff --check`，均通过。
