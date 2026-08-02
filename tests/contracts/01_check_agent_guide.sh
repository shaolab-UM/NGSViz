#!/usr/bin/env bash
set -euo pipefail

guide="agent/ngsViz_agent.md"
skill=".agents/skills/ngsviz-agent/SKILL.md"
schema="docs/ai-friendly/schemas/analysis_plan.schema.json"
example="docs/ai-friendly/examples/analysis_plan.multi_sample.json"

for file in "$guide" "$skill" "$schema" "$example"; do
  test -f "$file"
done

for command in \
  "java -jar NGSViz-1.3.jar describe" \
  "validate-compute" \
  "run-compute" \
  "Rscript lib/ngsVizPlotMain.R describe" \
  "validate-plot" \
  "run-plot"; do
  rg -q --fixed-strings "$command" "$guide"
done

if find agent -type f \( -name '*.py' -o -name '*.R' -o -name '*.java' -o -name '*.sh' \) | grep -q .; then
  echo "agent/ must contain Markdown guides only" >&2
  exit 1
fi
test -z "$(find agent -type f ! -name '*.md' -print -quit)"

for text in \
  "Java 不调用 R" \
  "R 不调用 Java" \
  "路径一：单样本只计算" \
  "路径二：多样本顺序计算" \
  "路径三：只可视化" \
  "路径四：完整流程" \
  "Java 只执行单样本计算" \
  "Codex 负责多样本顺序执行"; do
  rg -q --fixed-strings "$text" "$skill"
done

Rscript -e '
  x <- jsonlite::fromJSON("docs/ai-friendly/examples/analysis_plan.multi_sample.json")
  stopifnot(
    length(x$samples$sample_id) >= 2,
    !anyDuplicated(x$samples$sample_id),
    !anyDuplicated(x$samples$sample_name),
    !anyDuplicated(x$samples$group_name),
    !anyDuplicated(x$samples$signal_bam),
    !anyDuplicated(x$samples$output_dir)
  )
'

Rscript -e '
  x <- jsonlite::fromJSON("docs/ai-friendly/schemas/analysis_plan.schema.json", simplifyVector = FALSE)
  stopifnot(
    identical(x$properties$samples$minItems, 1L),
    identical(x$properties$execution[["$ref"]], "#/$defs/execution"),
    identical(x[["$defs"]]$execution$properties$mode$const, "sequential"),
    identical(x[["$defs"]]$execution$properties$failure_policy$enum, list("stop_on_error", "continue_on_error")),
    identical(x[["$defs"]]$execution$properties$visualization$enum, list("deferred", "after_compute"))
  )
'

echo "Agent guide contract checks passed."
