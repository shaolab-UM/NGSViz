#!/usr/bin/env bash
set -euo pipefail
tool=$1
cores=$2
[[ $cores == 1 || $cores == 4 ]]
root=$(cd "$(dirname "$0")" && pwd)
project=$(cd "$root/../../.." && pwd)
java=/usr/lib/jvm/java-17-openjdk-amd64/bin/java
jar="$project/Test/NGSViz-1.3.jar"
if [[ $tool == ngsViz ]]; then
  Rscript "$root/00_prepare_ngsViz.R" "$root" "$cores"
  for run in 1 2 3; do for fraction in 0.1 0.25 0.5 0.75 1.0; do
    id="p$cores"_run_"$run"_"$fraction"
    "$java" -jar "$jar" validate-compute --request "$root/requests/p$cores/$id.json" >/dev/null
  done; done
elif [[ $tool == deepTools ]]; then
  bed="$root/config/hg38_ngsViz.deeptools.bed"
  if [[ ! -e $bed ]]; then
    mkdir -p "$root/config"
    awk 'BEGIN{OFS="\t"} {$2=$2-1; print $1,$2,$3,$5":"$6,0,$4}' "$project/hg38_ngsViz.bed" > "$bed"
  fi
fi
for run in 1 2 3; do
  for fraction in 0.1 0.25 0.5 0.75 1.0; do
    bam="$project/Test/Data/GSE208913_H3K27me3_$fraction.sorted.bam"
    out="$root/results/p$cores/$tool/run_$run/$fraction"
    [[ ! -e $out ]] || { echo "Output exists: $out" >&2; exit 1; }
    mkdir -p "$out"
    id="p$cores"_run_"$run"_"$fraction"
    case $tool in
      ngsViz) cmd=("$java" -jar "$jar" run-compute --request
        "$root/requests/p$cores/$id.json");;
      deepTools) cmd=(bash "$root/08_deepTools_pipeline.sh" "$bam" "$out" "$bed" "$cores");;
      ngsplot) cmd=(/usr/bin/ngs.plot.r -C "$bam" -G hg38 -R genebody
        -O "$out/GSE208913_H3K27me3" -P "$cores");;
      *) echo "Unknown tool: $tool" >&2; exit 2;;
    esac
    printf '%q ' "${cmd[@]}" > "$out/command.txt"
    /usr/bin/time -o "$out/performance.tsv" -f '%e\t%U\t%S\t%P\t%M\t%I\t%O\t%x' "${cmd[@]}" > "$out/stdout.log" 2> "$out/stderr.log"
    echo "Completed $tool p$cores run=$run fraction=$fraction"
  done
done
