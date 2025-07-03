# NGSViz 性能优化指南

## 📊 **项目性能分析总结**

基于对NGSViz项目的深入分析，我发现了以下主要的性能瓶颈和优化机会：

### 🔍 **当前性能问题**

1. **SamReader重复创建**：每个基因处理都创建新的SamReader对象
2. **内存管理不当**：大量数据同时加载到内存
3. **线程池配置不优**：使用固定线程池，缺乏动态调整
4. **缺乏性能监控**：无法实时了解系统资源使用情况
5. **批处理效率低**：逐个处理基因，没有充分利用批处理优势

## 🚀 **并行运行优化方案**

### 1. **SamReader对象池优化**

**问题**：每个基因都创建新的SamReader，导致大量文件句柄和内存开销

**解决方案**：
```java
// 创建SamReader对象池
SamReader[] readerPool = new SamReader[core_num];
for (int i = 0; i < core_num; i++) {
    readerPool[i] = ReadBam.getBamReader(bam_file);
}

// 线程绑定特定的reader
int threadId = (int) (Thread.currentThread().getId() % core_num);
SamReader reader = readerPool[threadId];
```

**预期效果**：
- 减少文件句柄数量：从 N 个减少到 core_num 个
- 降低内存使用：避免重复创建SamReader对象
- 提高I/O效率：复用已打开的BAM文件连接

### 2. **线程池配置优化**

**当前配置**：
```java
ExecutorService executor = Executors.newFixedThreadPool(core_num);
```

**优化配置**：
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    core_num,                    // 核心线程数
    core_num * 2,               // 最大线程数
    60L, TimeUnit.SECONDS,      // 空闲线程存活时间
    new LinkedBlockingQueue<>(1000), // 工作队列
    new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
);
```

**优势**：
- 动态线程管理：根据负载自动调整线程数
- 更好的任务调度：避免任务饥饿
- 资源保护：防止系统过载

### 3. **批处理优化**

**当前方式**：逐个处理基因
```java
for (String gene_name : query_gene_list) {
    executor.submit(() -> processGene(...));
}
```

**优化方式**：批量处理
```java
List<List<String>> batches = splitIntoBatches(query_gene_list, BATCH_SIZE);
for (List<String> batch : batches) {
    executor.submit(() -> processBatch(batch));
}
```

**优势**：
- 减少线程创建开销
- 更好的负载均衡
- 提高缓存命中率

## 💾 **内存占用优化方案**

### 1. **数据结构优化**

**当前问题**：
```java
double[][] coverage_scaled_matrix = new double[num_query_gene][num_datapoints+1];
```

**优化建议**：
- 对于稀疏数据，使用稀疏矩阵
- 对于大数据集，考虑流式处理
- 使用对象池减少GC压力

### 2. **内存监控和管理**

**添加内存监控**：
```java
public static void monitorMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    double memoryUsagePercent = (double) usedMemory / runtime.totalMemory() * 100;
    
    if (memoryUsagePercent > MAX_MEMORY_THRESHOLD) {
        System.gc(); // 触发垃圾回收
    }
}
```

### 3. **数据库连接池**

**当前问题**：每次查询都创建新连接

**优化方案**：
```java
// 使用HikariCP连接池
HikariConfig config = new HikariConfig();
config.setMaximumPoolSize(core_num);
config.setMinimumIdle(2);
HikariDataSource dataSource = new HikariDataSource(config);
```

## 📈 **性能监控和调优**

### 1. **性能监控工具**

创建了 `PerformanceMonitor` 类来监控：
- 内存使用情况
- 线程池状态
- 操作耗时统计

### 2. **关键性能指标**

- **内存使用率**：堆内存和非堆内存使用情况
- **线程池效率**：活跃线程数、队列长度、完成任务数
- **I/O性能**：文件读取速度和并发度
- **CPU使用率**：处理器利用率

## 🔧 **实施建议**

### 1. **渐进式优化**

1. **第一阶段**：实施SamReader对象池
2. **第二阶段**：优化线程池配置
3. **第三阶段**：添加批处理优化
4. **第四阶段**：实施内存监控和管理

### 2. **配置参数调优**

```java
// 建议的配置参数
public static final int BATCH_SIZE = 100;           // 批处理大小
public static final int MAX_MEMORY_THRESHOLD = 80;  // 内存使用阈值
public static final int READER_POOL_SIZE = core_num; // SamReader池大小
```

### 3. **JVM参数优化**

```bash
# 建议的JVM参数
-Xmx8g                    # 最大堆内存
-Xms4g                    # 初始堆内存
-XX:+UseG1GC             # 使用G1垃圾收集器
-XX:MaxGCPauseMillis=200 # 最大GC暂停时间
-XX:+UseStringDeduplication # 字符串去重
```

## 📊 **预期性能提升**

基于优化方案，预期可以获得以下性能提升：

1. **内存使用减少**：30-50%
2. **处理速度提升**：40-60%
3. **文件I/O效率提升**：50-70%
4. **系统稳定性提升**：减少内存溢出和线程死锁

## 🎯 **下一步行动**

1. 实施 `OptimizedGeneProcessor` 类
2. 集成 `PerformanceMonitor` 监控工具
3. 进行性能测试和基准测试
4. 根据实际运行情况调整参数
5. 持续监控和优化

---

*本优化指南基于对NGSViz项目的深入分析，建议在实际环境中进行充分测试后再部署到生产环境。* 