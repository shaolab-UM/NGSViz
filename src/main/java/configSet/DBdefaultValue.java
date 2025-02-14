package configSet;

import sqldbOperate.QueryDefaultDB;

/**
 * @author Benchen Ye
 * @ create 2024-11--22:52
 */
public class DBdefaultValue extends InputParameterAttributes {
    public static void getPointLabFIValue(){
        String query_key = "PointLab";
        String pointLab_value = QueryDefaultDB.getDefaultDBValue(query_key, genome, DB_tpye, region_plot);
        String [] str_res = pointLab_value.split("-");
        for (String item : str_res) {
            region_labels.add(item);
        }
        System.out.println("Point lab is " +  region_labels);
    }

}
