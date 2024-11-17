package configSet;

import java.util.ArrayList;

/**
 * @author Benchen Ye
 * @create 2024-10--17:10
 */
public class DataPointNum extends InputParameterAttributes{
    public static void main(String[] args) {
        DataPointNum.getDataPointNum();
    }
    public static void getDataPointNum() {
        if (interval_type == "point_interval") {
            middle_points = 1;
            flank_points = num_datapoints/2;
        } else if (interval_type == "large_interval") {
            middle_points = num_datapoints/5*3+1;
            flank_points = num_datapoints/5;
        } else {
            middle_points = num_datapoints/5+1;
            flank_points = num_datapoints/5*2;
        }
        System.out.println("The number of middle point is " + middle_points);
        System.out.println("The number of flank points is " + flank_points);
    }
}
