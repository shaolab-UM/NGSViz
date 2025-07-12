package com.NGSViz.coverageCalculator;

import com.NGSViz.configSet.InputParameterAttributes;
import java.util.ArrayList;

/**
 * @author Benchen Ye
 * @create 2025-02--14:25
 */
public class DataScaler {
    private static  int num_datapoints = InputParameterAttributes.num_datapoints+1;
    private static String interval_type = InputParameterAttributes.interval_type;

    // Method: switch scale
    // Input:
    //   method: String, method name to scale coverage
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] getCoverageScaled(int[] physical_coverage, String method, int flank_size) {
        double[] coverage_scaled = new double[num_datapoints];
        double[] head_cov_scaled = new double[0];
        double[] tail_cov_scaled = new double[0];
        double[] mid_cov_scaled = new double[0];

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

        // Here, the points of bin need to match the division of datapoint.
        if (interval_type.equals("point_interval")) {
            if (method.equals("bin")) {
                head_cov_scaled = bin(head_cov, num_datapoints/2);
                mid_cov_scaled = bin(mid_cov, 1);
                tail_cov_scaled = bin(tail_cov, num_datapoints/2);
            } else if (method.equals("spline")) {
                head_cov_scaled = spline(head_cov, num_datapoints/2);
                mid_cov_scaled = spline(mid_cov, 1);
                tail_cov_scaled = spline(tail_cov, num_datapoints/2);
            } else {
                throw new IllegalArgumentException("Unknown scaling method: " + method);
            }
        } else if (interval_type.equals("broad_interval")) {
            if (method.equals("bin")) {
                head_cov_scaled = bin(head_cov, (num_datapoints/100)*30);
                mid_cov_scaled = bin(mid_cov, (num_datapoints/100)*40+1);
                tail_cov_scaled = bin(tail_cov, (num_datapoints/100)*30);
            } else if (method.equals("spline")) {
                head_cov_scaled = spline(head_cov, (num_datapoints/100)*30);
                mid_cov_scaled = spline(mid_cov, (num_datapoints/100)*40+1);
                tail_cov_scaled = spline(tail_cov, (num_datapoints/100)*30);
            } else {
                throw new IllegalArgumentException("Unknown scaling method: " + method);
            }
        }
        // Scale each region
        // Combine scaled regions
        System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
        System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
        System.arraycopy(tail_cov_scaled, 0, coverage_scaled, head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);

        return coverage_scaled;
    }


    // Method: to bin the coverage
    // Input:
    //   physical_coverage: ArrayList<Integer> of physical coverage
    //   num_datapoints: int, number of data points to plot
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] bin(int[] coverage_list, int num_points){
        int total_size = coverage_list.length;
        int chunk_size = total_size / num_points;
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

}
