#!/usr/bin/env bash
set -euo pipefail

host_root="/Users/benche/Desktop/myProj/ngsPlot/NGSViz/Test/sgNSD1_GSE186239_profile_reproducibility_20261004"
container_root="/home/rstudio/ngsPlot/NGSViz/Test/sgNSD1_GSE186239_profile_reproducibility_20261004"
mkdir -p "$host_root/results/ngsplot/compute" "$host_root/results/ngsplot/log"

run_panel() {
  local panel="$1"
  local title="$2"
  docker exec -w "$container_root" ngsplot-work ngs.plot.r \
    -G hg38 -R genebody -D refseq -F protein_coding \
    -C "config/ngsplot/${panel}.tsv" -O "results/ngsplot/compute/${panel}" \
    -T "$title" -L 2000 -MQ 20 -FL 150 -SS both -AL spline -RB 0 -S 1 -P 4 \
    >"$host_root/results/ngsplot/log/${panel}.stdout.log" \
    2>"$host_root/results/ngsplot/log/${panel}.stderr.log"
}

run_panel A "H3K36me2 - Up Regulated Genes"
run_panel B "H3K36me2 - Down Regulated Genes"
run_panel C "H3K36me2 - Genebody Region"
