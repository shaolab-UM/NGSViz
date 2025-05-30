package com.NGSViz.configSet;

import com.NGSViz.sqldbOperate.GetRefDB;
import com.NGSViz.sqldbOperate.QueryWholeRegionCoordinate;
import com.NGSViz.utils.JsonConfigPath;
import com.NGSViz.utils.mergeDB;

import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--20:05
 * @function get the value of each input parameter and put the value into vars
 * @input `args` is the parameters inputted by user
 * @process get each parameters' value and save in the var
 * @process set default value for those without an input value
 */
public class ProcessInputParameters extends InputParameterAttributes {
    //public static List<Map<String, Object>> region_coord;
    public static void processInputParameter(String[] args) {
        if (args.length % 2 ==1 || args.length == 0){
            System.err.println("Error: Unpaired argument and value.");
            System.exit(-1);
        }
        // Parse by traversing the command line argument array.
        GetInputParameterValue.getInputParametersValue(args);
        if(!input_db_path.isEmpty()){
            mergeDB.mergeDB();
            System.out.println("Finish merging DB. Quit the program!");
            System.exit(0);
        }
        // set and print the default value for option parameters
        SetDefaultOptionParameter.setDefaultOptionParameter();
        //
        JsonConfigPath.getJsonConfigPath();
        // check whether is legal value for input parameter
        CheckLegal2InputParameter.checkLegal2InputParameter();
        // get default point label value
        DBdefaultValue.getPointLabFIValue();
        // process the input text file
        ProcessInputFile.processInputFile();
        // get the coordinate database will be used
        refname = GetRefDB.getDBTableName(genome, DB_type);
        // get the species name
        species = GetRefDB.getSpeciesName(genome, DB_type);
        // get the table name of genome coordinate information
        tbl_name = GetRefDB.getTblName(genome, DB_type);

        // get the whole coordinate
        QueryWholeRegionCoordinate.queryGenomeCoorDatabaseRecord(tbl_name, biotype, analysis_type);
        List<Double> width_list = QueryWholeRegionCoordinate.width_list;
        // get the interval type
        interval_type = SetIntervalType.setIntervalTypeValue(region_labels);
        // get data points
        DataPointNum.getDataPointNum();

        // generate the plot parameters json file
        // GeneratePlotParasJson.generatePlotParasJson(output_path);
        GenerateJsonConfig.generateJsonConfig(output_path, config_name);
    }
}
