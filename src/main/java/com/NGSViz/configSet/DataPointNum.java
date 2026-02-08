package com.NGSViz.configSet;

/**
 * @author Benchen Ye
 * @ create 2024-10--17:10
 */
public class DataPointNum extends InputParameterAttributes{
    public static void getDataPointNum() {
        if (interval_type.equals("point_interval")) {
            middle_points = 1;
            flank_points = num_datapoints/2;
        } else if (interval_type.equals("broad_interval")) {
            if(CenterMode){
                middle_points = 1; //40
                flank_points = num_datapoints/2; //30
            }else{
                middle_points = (num_datapoints/100)*60+1; //40
                flank_points = (num_datapoints/100)*20; //30
            }
        }
        System.out.println("The number of middle point is " + middle_points);
        System.out.println("The number of flank points is " + flank_points);
    }
}
