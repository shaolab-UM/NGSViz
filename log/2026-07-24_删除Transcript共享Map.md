# 删除 Transcript 共享 Map

- 删除 `Transcript` 中未使用的静态 `record` 字段。
- 删除未使用的 `getQueryCoord()` 方法及相关 import。
- 新增结构回归测试，防止共享字段和废弃方法被重新引入。

## 验证

- `mvn -q -Dtest=TranscriptApiTest test`：通过，1 项测试全部通过。
- `mvn test`：共 11 项测试，10 项通过，1 项失败。
- 失败项：`QueryGenomeRangeTest.testQueryStartAtGeneStart`，期望值为 85，实际值为 80；与本次 `Transcript` 死代码删除无关。
