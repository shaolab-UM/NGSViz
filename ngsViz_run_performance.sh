#!/bin/bash

#sample_ratio=0.1
# 设置日志文件名
proj_path="/Users/benche/myProj/ngsPlot"
genome="hg19"
region="tss"
run_num="p1_run_1"
core_num=1


echo "Processing value: "
output_dir="${proj_path}/Result/performanceCompare/ngsViz/${run_num}"
input_dir="${proj_path}/Data/hesc.H3k4me3.1M.bam"
LOG_FILE="${output_dir}/performance_log.txt"

if [ ! -d "${output_dir}" ]; then
mkdir -p ${output_dir}
fi

START_DATE=$(date +"%Y-%m-%d %H:%M:%S")
# 1.1 获取开始时间戳 (秒为单位)
START_TIMESTAMP=$(date +%s)
java -jar NGSViz-1.0-SNAPSHOT.jar -G $genome -R $region -P $core_num -I $input_dir -O ${output_dir} > ${output_dir}/$sample_ratio.log

# 2. 获取结束时间 (日期和时间)
END_DATE=$(date +"%Y-%m-%d %H:%M:%S")
# 2.1 获取结束时间戳 (秒为单位)
END_TIMESTAMP=$(date +%s)

# 3. 计算总运行时间
# 时间差（秒）
DURATION_SECONDS=$((END_TIMESTAMP - START_TIMESTAMP))

# 格式化显示总运行时间
# 可以转换成小时、分钟、秒
HOURS=$((DURATION_SECONDS / 3600))
MINUTES=$(( (DURATION_SECONDS % 3600) / 60 ))
SECONDS=$((DURATION_SECONDS % 60))

echo "----------------------------------------"
echo "总运行时间: ${HOURS}小时 ${MINUTES}分钟 ${SECONDS}秒"
echo "----------------------------------------"
echo "Loop finished."


# 运行多线程测试（假设程序支持多线程）
# 需要根据程序的参数设置多线程
# echo "Multi-threaded Test:" >> $LOG_FILE
# /usr/bin/time -a -o $LOG_FILE java -jar NGSViz-1.0-SNAPSHOT.jar -G hg19 -R genebody -P 8 -I /Users/bencheye/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam -O ./Result/multiCore 
# echo "Performance testing completed. Check the output files for details."