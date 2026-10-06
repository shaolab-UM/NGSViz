#!/usr/bin/env bash
set -euo pipefail

root="/Users/benche/Desktop/myProj/ngsPlot/NGSViz/Test/sgNSD1_GSE186239_profile_reproducibility_20261004"
java_bin="/usr/bin/java"
jar="/Users/benche/Desktop/myProj/ngsPlot/NGSViz/NGSViz-1.3.jar"
requests=(
  "$root/config/ngsviz/01_up_NSD1WT.json"
  "$root/config/ngsviz/02_up_NSD1KO.json"
  "$root/config/ngsviz/03_down_NSD1WT.json"
  "$root/config/ngsviz/04_down_NSD1KO.json"
  "$root/config/ngsviz/05_all_NSD1WT.json"
  "$root/config/ngsviz/06_all_NSD1KO.json"
)

mkdir -p "$root/results/ngsviz"

for request in "${requests[@]}"; do
  "$java_bin" -jar "$jar" validate-compute --request "$request"
done

for request in "${requests[@]}"; do
  "$java_bin" -jar "$jar" run-compute --request "$request"
done
