#!/usr/bin/env bash
set -euo pipefail

java_cli_dirs=(
  "src/main/java/com/NGSViz/cli"
  "src/main/java/com/NGSViz/compute"
)
r_cli_dir="lib/cli"
compute_manifest="src/main/java/com/NGSViz/cli/ComputeManifest.java"
visualization_manifest="lib/cli/visualization_manifest.R"

for path in "${java_cli_dirs[@]}" "$r_cli_dir" "$compute_manifest" "$visualization_manifest"; do
  test -e "$path"
done

if rg -n 'Rscript|ngsVizPlotMain' "${java_cli_dirs[@]}"; then
  echo "Java CLI must not invoke the R runtime." >&2
  exit 1
fi

if rg -n 'java[[:space:]]+-jar|NGSViz-[^[:space:]]*\.jar|NGSViz JAR' "$r_cli_dir"; then
  echo "R CLI must not invoke the Java runtime." >&2
  exit 1
fi

if find agent -type f \( -name '*.py' -o -name '*.R' -o -name '*.java' -o -name '*.sh' \) | grep -q .; then
  echo "agent/ must not contain runtime scripts." >&2
  exit 1
fi

for field in \
  '"resolved_parameters"' \
  '"sample_id"' \
  '"sample_name"' \
  '"group_name"' \
  '"outputs"' \
  '"log_file"'; do
  rg -q --fixed-strings "$field" "$compute_manifest"
done

for field in \
  'schema_version = "1.0"' \
  'r_version = R.version.string' \
  'input_plot_settings' \
  'input_coverage_files' \
  'output_images' \
  'log_file'; do
  rg -q --fixed-strings "$field" "$visualization_manifest"
done

if rg -n 'visualization_manifest|visualization_status|output_images|r_version' "$compute_manifest"; then
  echo "Compute manifest contains visualization-only fields." >&2
  exit 1
fi

if rg -n 'compute_manifest|resolved_parameters|coverage_matrices|read_count_files|ngsviz_version|java_version' "$visualization_manifest"; then
  echo "Visualization manifest contains computation-only fields." >&2
  exit 1
fi

rg -q --fixed-strings 'compute_manifest.json' src/main/java/com/NGSViz/cli/RunComputeCommand.java
rg -q --fixed-strings 'visualization_manifest.json' lib/cli/run_plot.R

echo "Module independence contract checks passed."
