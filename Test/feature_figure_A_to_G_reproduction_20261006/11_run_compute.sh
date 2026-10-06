#!/usr/bin/env bash
set -euo pipefail
root=/Users/benche/Desktop/myProj/ngsPlot/NGSViz/Test/feature_figure_A_to_G_reproduction_20261006
jar=/Users/benche/Desktop/myProj/ngsPlot/NGSViz/NGSViz-1.3.jar
for request in "$root"/compute_requests/*.json; do
  /usr/bin/java -jar "$jar" validate-compute --request "$request" >/dev/null
done
for request in "$root"/compute_requests/*.json; do
  output=$(jq -r '.output_dir' "$request")
  test ! -e "$output"
  /usr/bin/java -jar "$jar" run-compute --request "$request"
  jq -e '.status == "completed"' "$output/compute_manifest.json" >/dev/null
done
