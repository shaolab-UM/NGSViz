package configSet;

import sqldbOperate.QueryDefaultDB;

import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--22:52
 */
public class DBdefaultValue extends InputParameterAttributes {
    public static void main(String[] args){

    }
    public static void getDBdefaultFIValue(){
        String query_key;
        String query_res;
        query_key = "DefaultFI1";
        query_res = QueryDefaultDB.getDefaultDBValue(query_key, genome, region_plot);
        if(query_res != null){
            if(analysis_type.equals(query_res)){
                fi_subset.add(query_res);
            }
        }
        query_key = "DefaultFI2";
        query_res = QueryDefaultDB.getDefaultDBValue(query_key, genome, region_plot);
        if(query_res != null){
            fi_subset.add(query_res);
        }
        query_key = "DefaultFI3";
        query_res = QueryDefaultDB.getDefaultDBValue(query_key, genome, region_plot);
        if(query_res != null){
            fi_subset.add(query_res);
        }
        //System.out.println("the DefaultFI3 is : " + query_res);
        System.out.println("fi_subset is " + fi_subset);
    }

    public static void getPointLabFIValue(){
        String query_key = "PointLab";
        String pointLab_value = QueryDefaultDB.getDefaultDBValue(query_key, genome, region_plot);
        String [] str_res = pointLab_value.split("-");
        for (String item : str_res) {
            region_labels.add(item);
        }
        System.out.println("Point lab is " +  region_labels);
    }
    public static void getIntervalType(){
        if(region_labels.size() == 1){
            interval_type = "point_interval";
        }else {
            interval_type = "broad_interval";
        }
        System.out.println("interval type is " + interval_type);
    }
}
