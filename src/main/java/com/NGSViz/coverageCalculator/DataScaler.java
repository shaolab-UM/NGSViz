package com.NGSViz.coverageCalculator;

import com.NGSViz.configSet.InputParameterAttributes;
import java.util.stream.DoubleStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2025-02--14:25
 */
public class DataScaler {
    private static  int num_datapoints = InputParameterAttributes.num_datapoints+1;
    private static int flanking_region_datapoints = InputParameterAttributes.flank_points;
    private static int middle_datapoints = InputParameterAttributes.middle_points;

    // Method: switch scale
    // Input:
    //   method: String, method name to scale coverage
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] getCoverageScaled2(int[] physical_coverage, String method, int flank_size) {
        double[] coverage_scaled = new double[num_datapoints];
        double[] head_cov_scaled;
        double[] tail_cov_scaled;
        double[] mid_cov_scaled;
        //System.out.println("--- flank size is :" + flank_size);
        //System.out.println("--- physical coverage size is " + physical_coverage.length);

        int length = physical_coverage.length;
        int[] head_cov = Arrays.copyOfRange(physical_coverage, 0, flank_size);
        int[] tail_cov = Arrays.copyOfRange(physical_coverage, length - flank_size, length);
        int[] mid_cov = Arrays.copyOfRange(physical_coverage, flank_size, length - flank_size);

        switch (method){
            case "bin":
                head_cov_scaled = bin(head_cov, flanking_region_datapoints);
                mid_cov_scaled = bin(mid_cov, middle_datapoints);
                tail_cov_scaled = bin(tail_cov, flanking_region_datapoints);
                //System.out.println("--- Normalize data with bin");
                // copy the result into the coverage_scaled
                System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
                System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
                System.arraycopy(tail_cov_scaled, 0, coverage_scaled,
                        head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
                break;
            case "spline":
                head_cov_scaled = spline(head_cov, flanking_region_datapoints);
                tail_cov_scaled = spline(tail_cov, flanking_region_datapoints);
                mid_cov_scaled = spline(mid_cov, middle_datapoints);
                //System.out.println("--- Normalize data with spline");
                // copy the result into the coverage_scaled
                System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
                System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
                System.arraycopy(tail_cov_scaled, 0, coverage_scaled,
                        head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
                break;
        }
        return coverage_scaled;
    }
    public static double[] getCoverageScaled2(double[] physical_coverage, String method, int flank_size) {
        double[] coverage_scaled = new double[num_datapoints];
        double[] head_cov_scaled;
        double[] tail_cov_scaled;
        double[] mid_cov_scaled;
        //System.out.println("--- flank size is :" + flank_size);
        //System.out.println("--- physical coverage size is " + physical_coverage.length);

        int length = physical_coverage.length;
        double[] head_cov = Arrays.copyOfRange(physical_coverage, 0, flank_size);
        double[] tail_cov = Arrays.copyOfRange(physical_coverage, length - flank_size, length);
        double[] mid_cov = Arrays.copyOfRange(physical_coverage, flank_size, length - flank_size);

        switch (method){
            case "bin":
                head_cov_scaled = bin(head_cov, flanking_region_datapoints);
                mid_cov_scaled = bin(mid_cov, middle_datapoints);
                tail_cov_scaled = bin(tail_cov, flanking_region_datapoints);
                //System.out.println("--- Normalize data with bin");
                // copy the result into the coverage_scaled
                System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
                System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
                System.arraycopy(tail_cov_scaled, 0, coverage_scaled,
                        head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
                break;
            case "spline":
                head_cov_scaled = spline(head_cov, flanking_region_datapoints);
                tail_cov_scaled = spline(tail_cov, flanking_region_datapoints);
                mid_cov_scaled = spline(mid_cov, middle_datapoints);
                //System.out.println("--- Normalize data with spline");
                // copy the result into the coverage_scaled
                System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
                System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
                System.arraycopy(tail_cov_scaled, 0, coverage_scaled,
                        head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
                break;
        }
        return coverage_scaled;
    }

    public static double[] getCoverageScaled(ArrayList<Integer> physical_coverage, String method, int flank_size) {
        double[] coverage_scaled = new double[num_datapoints];
        double[] head_cov_scaled;
        double[] tail_cov_scaled;
        double[] mid_cov_scaled;
        //System.out.println("--- flank size is :" + flank_size);
        //System.out.println("--- physical coverage size is " + physical_coverage.size());

        ArrayList<Integer> head_cov = new ArrayList<>(physical_coverage.subList(0, flank_size));
        ArrayList<Integer> tail_cov = new ArrayList<>(physical_coverage.subList(physical_coverage.size() - flank_size, physical_coverage.size()));
        ArrayList<Integer> mid_cov = new ArrayList<>(physical_coverage.subList(flank_size, physical_coverage.size() - flank_size));

        switch (method){
            case "bin":
                head_cov_scaled = bin(head_cov, flanking_region_datapoints);
                mid_cov_scaled = bin(mid_cov, middle_datapoints);
                tail_cov_scaled = bin(tail_cov, flanking_region_datapoints);
                //System.out.println("--- Normalize data with bin");
                // copy the result into the coverage_scaled
                System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
                System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
                System.arraycopy(tail_cov_scaled, 0, coverage_scaled,
                        head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
                break;
            case "spline":
                head_cov_scaled = spline(head_cov, flanking_region_datapoints);
                tail_cov_scaled = spline(tail_cov, flanking_region_datapoints);
                mid_cov_scaled = spline(mid_cov, middle_datapoints);
                //System.out.println("--- Normalize data with spline");
                // copy the result into the coverage_scaled
                System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
                System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
                System.arraycopy(tail_cov_scaled, 0, coverage_scaled,
                        head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
                break;
        }
        return coverage_scaled;
    }


    // Method: to bin the coverage
    // Input:
    //   physical_coverage: ArrayList<Integer> of physical coverage
    //   num_datapoints: int, number of data points to plot
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] bin(ArrayList<Integer> coverage_list, int num_points){
        int total_size = coverage_list.size();
        int chunk_size = total_size / num_points;
        double[] coverage_scaled = new double[num_points];
        for (int i = 0; i < num_points; i++) {
            int start = i * chunk_size;
            // could include more item in the last chunk
            int end = (i == num_points - 1) ? total_size : start + chunk_size;
            double sum = 0;
            int count = 0;
            for (int j = start; j < end; j++) {
                sum += coverage_list.get(j);
                count++;
            }
            coverage_scaled[i] = sum / count;
        }
        return coverage_scaled;
    }

    public static double[] bin(double[] coverage_list, int num_points){
        int total_size = coverage_list.length;
        int chunk_size = total_size / num_points;
        double[] coverage_scaled = new double[num_points];
        for (int i = 0; i < num_points; i++) {
            int start = i * chunk_size;
            // could include more item in the last chunk
            int end = (i == num_points - 1) ? total_size : start + chunk_size;
            double sum = 0;
            int count = 0;
            for (int j = start; j < end; j++) {
                sum += coverage_list[j];
                count++;
            }
            coverage_scaled[i] = sum / count;
        }
        return coverage_scaled;
    }
    public static double[] bin(int[] coverage_list, int num_points){
        int total_size = coverage_list.length;
        int chunk_size = total_size / num_points;
        
        // OPTIMIZED: Pre-compute boundaries
        int[] boundaries = new int[num_points + 1];
        for (int i = 0; i <= num_points; i++) {
            boundaries[i] = i * chunk_size;
        }
        boundaries[num_points] = total_size; // Ensure last boundary is correct
        
        double[] coverage_scaled = new double[num_points];
        for (int i = 0; i < num_points; i++) {
            int start = boundaries[i];
            int end = boundaries[i + 1];
            
            double sum = 0;
            int count = end - start;
            
            for (int j = start; j < end; j++) {
                sum += coverage_list[j];
            }
            
            coverage_scaled[i] = count > 0 ? sum / count : 0;
        }
        return coverage_scaled;
    }

    public static List<Double> bin2(ArrayList<Integer> coverage_list, int num_points){
        int total_size = coverage_list.size();
        int chunk_size = total_size / num_points;
        List<Double> coverage_scaled = new ArrayList<>();
        for (int i = 0; i < num_points; i++) {
            int start = i * chunk_size;
            // could include more item in the last chunk
            int end = (i == num_points - 1) ? total_size : start + chunk_size;
            double sum = 0;
            int count = 0;
            for (int j = start; j < end; j++) {
                sum += coverage_list.get(j);
                count++;
            }
            coverage_scaled.add(sum / count);
        }
        return coverage_scaled;
    }
    // Method: spline
    // Input:
    //   physical_coverage: ArrayList<Integer> of physical coverage
    //   num_datapoints: int, number of data points to plot
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] spline(double[] coverage_list , int num_points) {
        double[] coverage_scaled = new double[num_points];
        int totalSize = coverage_list.length;
        double step = (double) (totalSize - 1) / (num_points - 1);
        System.out.println("Num points is " + num_points);
        if (num_points == 1) {
            // For num_points == 1, take the first value or average if needed
            coverage_scaled[0] = coverage_list.length == 0 ? 0.0 : (double) coverage_list[0];
            return coverage_scaled;
        }
        for (int i = 0; i < num_points; i++) {
            double index = i * step;
            int lower = (int) Math.floor(index);
            int upper = (int) Math.ceil(index);
            if (upper >= totalSize) upper = totalSize - 1;
            double weight = index - lower;
            // Linear interpolation method is used to calculate the value after interpolation
            double interpolatedValue = (1 - weight) * coverage_list[lower] +
                    weight * coverage_list[upper];
            // Floor negative values which are caused by spline
            if (interpolatedValue < 0) {
                interpolatedValue = 0;
            }
            coverage_scaled[i] = interpolatedValue;
        }
        return coverage_scaled;
    }
    public static double[] spline(int[] coverage_list , int num_points) {
        double[] coverage_scaled = new double[num_points];
        int totalSize = coverage_list.length;
        double step = (double) (totalSize - 1) / (num_points - 1);
        System.out.println("Num points is " + num_points);
        if (num_points == 1) {
            // For num_points == 1, take the first value or average if needed
            coverage_scaled[0] = coverage_list.length == 0 ? 0.0 : (double) coverage_list[0];
            return coverage_scaled;
        }
        for (int i = 0; i < num_points; i++) {
            double index = i * step;
            int lower = (int) Math.floor(index);
            int upper = (int) Math.ceil(index);
            if (upper >= totalSize) upper = totalSize - 1;
            double weight = index - lower;
            // Linear interpolation method is used to calculate the value after interpolation
            double interpolatedValue = (1 - weight) * coverage_list[lower] +
                    weight * coverage_list[upper];
            // Floor negative values which are caused by spline
            if (interpolatedValue < 0) {
                interpolatedValue = 0;
            }
            coverage_scaled[i] = interpolatedValue;
        }
        return coverage_scaled;
    }

    public static double[] spline(ArrayList<Integer> coverage_list , int num_points) {
        double[] coverage_scaled = new double[num_points];
        int totalSize = coverage_list.size();
        double step = (double) (totalSize - 1) / (num_points - 1);
        System.out.println("Num points is " + num_points);
        if (num_points == 1) {
            // For num_points == 1, take the first value or average if needed
            coverage_scaled[0] = coverage_list.isEmpty() ? 0.0 : coverage_list.get(0).doubleValue();
            return coverage_scaled;
        }
        for (int i = 0; i < num_points; i++) {
            double index = i * step;
            int lower = (int) Math.floor(index);
            int upper = (int) Math.ceil(index);
            if (upper >= totalSize) upper = totalSize - 1;
            double weight = index - lower;
            // Linear interpolation method is used to calculate the value after interpolation
            double interpolatedValue = (1 - weight) * coverage_list.get(lower) +
                    weight * coverage_list.get(upper);
            // Floor negative values which are caused by spline
            if (interpolatedValue < 0) {
                interpolatedValue = 0;
            }
            coverage_scaled[i] = interpolatedValue;
        }
        return coverage_scaled;
    }

    public static List<Double> spline2(ArrayList<Integer> coverage_list , int num_points) {
        List<Double> coverage_scaled = new ArrayList<>();
        int totalSize = coverage_list.size();
        double step = (double) (totalSize - 1) / (num_points - 1);
        System.out.println("Num points is " + num_points);
        if(num_points==1){
            for (Integer value : coverage_list) {
                coverage_scaled.add(value.doubleValue());
            }
            System.out.println();
            return coverage_scaled;
        }
        for (int i = 0; i < num_points; i++) {
            double index = i * step;
            int lower = (int) Math.floor(index);
            int upper = (int) Math.ceil(index);
            if (upper >= totalSize) upper = totalSize - 1;
            double weight = index - lower;
            // Linear interpolation method is used to calculate the value after interpolation
            double interpolatedValue = (1 - weight) * coverage_list.get(lower) +
                    weight * coverage_list.get(upper);
            // Floor negative values which are caused by spline.
            if(interpolatedValue < 0){
                interpolatedValue = 0;
            }
            coverage_scaled.add(interpolatedValue);
        }
        return coverage_scaled;
    }

    // OPTIMIZED: Get coverage scaled using primitive arrays
    public static double[] getCoverageScaledOptimized(int[] physical_coverage, String method, int flank_size) {
        double[] coverage_scaled = new double[num_datapoints];
        double[] head_cov_scaled;
        double[] tail_cov_scaled;
        double[] mid_cov_scaled;
        
        // Convert to primitive arrays for better performance
        int[] head_cov = new int[flank_size];
        int[] tail_cov = new int[flank_size];
        int[] mid_cov = new int[physical_coverage.length - 2 * flank_size];
        
        // Copy head region
        System.arraycopy(physical_coverage, 0, head_cov, 0, flank_size);
        
        // Copy tail region
        System.arraycopy(physical_coverage, physical_coverage.length - flank_size, tail_cov, 0, flank_size);
        
        // Copy middle region
        System.arraycopy(physical_coverage, flank_size, mid_cov, 0, mid_cov.length);
        
        // Scale each region
        if (method.equals("bin")) {
            head_cov_scaled = bin(head_cov, num_datapoints / 4);
            mid_cov_scaled = bin(mid_cov, num_datapoints / 2);
            tail_cov_scaled = bin(tail_cov, num_datapoints / 4);
        } else if (method.equals("spline")) {
            head_cov_scaled = spline(head_cov, num_datapoints / 4);
            mid_cov_scaled = spline(mid_cov, num_datapoints / 2);
            tail_cov_scaled = spline(tail_cov, num_datapoints / 4);
        } else {
            throw new IllegalArgumentException("Unknown scaling method: " + method);
        }
        
        // Combine scaled regions
        System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
        System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
        System.arraycopy(tail_cov_scaled, 0, coverage_scaled, head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
        
        return coverage_scaled;
    }
    
    // OPTIMIZED: Convert ArrayList<Integer> to int[] for better performance
    public static int[] toPrimitiveArray(ArrayList<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
    
    // OPTIMIZED: Convert int[] to ArrayList<Integer> when needed
    public static ArrayList<Integer> toArrayList(int[] array) {
        ArrayList<Integer> list = new ArrayList<>(array.length);
        for (int value : array) {
            list.add(value);
        }
        return list;
    }
}
