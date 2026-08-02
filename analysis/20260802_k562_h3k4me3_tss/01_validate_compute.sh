#!/bin/sh
set -eu

java -jar /Users/benche/Desktop/myProj/ngsPlot/NGSViz/target/NGSViz-1.0.jar \
  validate-compute \
  --request /Users/benche/Desktop/myProj/ngsPlot/NGSViz/analysis/20260802_k562_h3k4me3_tss/compute_request.json
