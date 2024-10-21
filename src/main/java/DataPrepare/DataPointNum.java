package DataPrepare;

import configSet.CommonFinalParas;

import java.util.ArrayList;

/**
 * @author Benchen Ye
 * @create 2024-10--17:10
 */
public class DataPointNum {
    private static int num_datapoint = CommonFinalParas.num_datapoints;
    private static String interval_type = CommonFinalParas.interval_type;
    private static int num_middle_point;
    private static int num_flank_point;

    public static void main(String[] args) {
        ArrayList<Integer> data_point_list = DataPointNum.getDataPointNum();
    }
    public static ArrayList<Integer> getDataPointNum() {
        if (interval_type == "point_interval") {
            num_middle_point = 1;
            num_flank_point = num_datapoint/2;
        } else if (interval_type == "large_interval") {
            num_middle_point = num_datapoint/5*3+1;
            num_flank_point = num_datapoint/5;
        } else {
            num_middle_point = num_datapoint/5+1;
            num_flank_point = num_datapoint/5*2;
        }
        ArrayList<Integer> data_point_list = new ArrayList<>();
        data_point_list.add(num_middle_point);
        data_point_list.add(num_flank_point);
        System.out.println("The number of middle and flank point is " + data_point_list);
        return data_point_list;
    }
}
