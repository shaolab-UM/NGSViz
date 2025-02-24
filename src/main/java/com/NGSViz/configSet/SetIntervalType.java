package com.NGSViz.configSet;

import java.util.Collections;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--20:07
 * @function set the value of interval type
 * @value: `point` for region_labels == 1; `large` for median(cd$end - cd$start + 1) > flanksize; `mid_interval`
 */
public class SetIntervalType{
    public static String setIntervalTypeValue(List<Double> width_list, List<String> region_labels, int flank_region){
        String interval_type = null;
        double width_median = calculateMedian(width_list);
        if(region_labels.size() == 1){
            interval_type = "point_interval";
        } else if(width_median > flank_region){
            interval_type = "broad_interval";
        } else {
            interval_type = "mid_interval";
        }
        System.out.println("interval type is " + interval_type);
        return interval_type;
    };
    public static double calculateMedian(List<Double> numbers) {
        // order
        Collections.sort(numbers);
        int size = numbers.size();
        if (size % 2 == 0) {
            // If it is even, return the average of the middle two numbers.
            return (numbers.get(size / 2 - 1) + numbers.get(size / 2)) / 2.0;
        } else {
            // If the number is odd, return the middle number
            return numbers.get(size / 2);
        }
    }
}
