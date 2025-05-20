#!/bin/bash
core_num=8
# 设置日志文件名
output_dir="/Users/bencheye/myProj/ngsPlot/Output/Test"
input_dir="/Users/bencheye/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam"
genome="hg19"
region="genebody"
LOG_FILE="${output_dir}/performance_log.txt"

# 运行单线程测试
# echo "Single-threaded Test:" >> $LOG_FILE
# /usr/bin/time -a -o $LOG_FILE java -jar NGSViz-1.0-SNAPSHOT.jar -G hg19 -R genebody -P 1 -I /Users/bencheye/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam -O ./Result/singleCore


/usr/bin/time -a -o "$LOG_FILE" -f "
=== Run Start: %Y-%m-%d %H:%M:%S ===
  Elapsed Real Time (s): %e
  User CPU Time (s):     %U
  System CPU Time (s):   %S
  CPU Utilization (%):   %P
  Max Resident Memory (KB): %M
  File System Inputs:    %I
  File System Outputs:   %O
=== Run End ===
" java -jar NGSViz-1.0-SNAPSHOT.jar -G $genome -R $region -P 1 -I $input_dir -O ${output_dir}
# 运行多线程测试（假设程序支持多线程）
# 需要根据程序的参数设置多线程
# echo "Multi-threaded Test:" >> $LOG_FILE
# /usr/bin/time -a -o $LOG_FILE java -jar NGSViz-1.0-SNAPSHOT.jar -G hg19 -R genebody -P 8 -I /Users/bencheye/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam -O ./Result/multiCore 
# echo "Performance testing completed. Check the output files for details."