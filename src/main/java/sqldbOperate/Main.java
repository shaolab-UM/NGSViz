package sqldbOperate;

import configSet.CommonFinalParas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * @author Benchen Ye
 * @create 2024-10--20:47
 */

public class Main {
    private static int num_datapoints = CommonFinalParas.num_datapoints;
    private static int num_core = CommonFinalParas.cores_num;

    public static void main(String[] args) {
        String query_refname = "hg19.ensembl.genebody.protein_coding";
        List<Map<String, Object>> results = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);

        // initiation coverage_scaled_sum list
        List<Double> coverage_scaled_sum = new ArrayList<>();
        for (int i = 0; i < num_datapoints; i++) {
            coverage_scaled_sum.add(0.0);
        }

        // loop results list and add the coverage_scaled
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> record = results.get(i);
            List<Double> coverage_scaled = PintProcessRecordGenomeCoordinateDB.processRecord(record);
            // add the coverage_scaled to coverage_scaled_sum
            for (int j = 0; j < coverage_scaled.size(); j++) {
                coverage_scaled_sum.set(j, coverage_scaled_sum.get(j) + coverage_scaled.get(j));
            }
            //System.out.println("Coverage Scaled Sum: " + coverage_scaled_sum);
        }
        // output the result
        System.out.println("Coverage Scaled Sum: " + coverage_scaled_sum);
    }

}

/*
public class Main {
    private static int num_datapoints = CommonFinalParas.num_datapoints;
    private static int num_core = CommonFinalParas.cores_num;

    public static void main(String[] args) {
        String query_refname = "hg19.ensembl.genebody.protein_coding";
        List<Map<String, Object>> results = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);
        // list size
        int list_size = num_datapoints + 1;
        // AtomicReferenceArray<DoubleAdder> represents a thread-safe reference array where each element is of type DoubleAdder
        AtomicReferenceArray<DoubleAdder> coverage_scaled_sum = new AtomicReferenceArray<>(list_size);
        for (int i = 0; i < list_size; i++) {
            coverage_scaled_sum.set(i, new DoubleAdder());
        }
        // ForkJoinPool is a thread pool for parallel task execution in Java,
        // especially for partitioning algorithms and recursive tasks.
        ForkJoinPool customThreadPool = new ForkJoinPool(num_core);
        customThreadPool.submit(() ->
                results.parallelStream().forEach(record -> {
                    List<Double> coverage_scaled = PintProcessRecordGenomeCoordinateDB.processRecord(record);
                    for (int i = 0; i < list_size; i++) {
                        coverage_scaled_sum.get(i).add(coverage_scaled.get(i));
                    }
                })
        ).join();

        System.out.println("coverage scaled sum is : " + coverage_scaled_sum);

        // output the sum
        for (int i = 0; i < list_size; i++) {
            System.out.println("Index " + i + ": " + coverage_scaled_sum.get(i).sum());
        }
    }
}

 */

/*
    public static void main(String[] args) {
        String query_refname = "hg19.ensembl.genebody.protein_coding";
        List<Map<String, Object>> results = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);

        // set core num
        ForkJoinPool customThreadPool = new ForkJoinPool(4);
        customThreadPool.submit(() ->
                results.parallelStream().forEach(PintProcessRecordGenomeCoordinateDB::processRecord)
        ).join();
    }

     */