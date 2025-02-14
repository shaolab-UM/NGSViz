package configSet;

import sqldbOperate.GetRefDB;
import sqldbOperate.QueryWholeRegionCoordinateCP;
import sqldbOperate.QueryWholeRegionCoordinate;

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
            System.out.println("Unpaired argument and value.");
            System.exit(-1);
        }
        // Parse by traversing the command line argument array.
        GetInputParameterValue.getInputParametersValue(args);
        // set and print the default value for option parameters
        SetDefaultOptionParameter.setDefaultOptionParameter();
        // check whether is legal value for input parameter
        CheckLegal2InputParameter.checkLegal2InputParameter();
        // get default point label value
        DBdefaultValue.getPointLabFIValue();
        // process the input text file
        ProcessInputFile.processInputFile();
        // get the coordinate database will be used
        refname = GetRefDB.getDBTableName(genome, DB_tpye);
        // get the species name
        species = GetRefDB.getSpeciesName(genome, DB_tpye);
        // get the table name of genome coordinate information
        tbl_name = GetRefDB.getTblName(genome, DB_tpye);

        // get the whole coordinate
        //QueryWholeRegionCoordinate.queryGenomeCoorDatabaseRecord(tbl_name, biotype, analysis_type);
        QueryWholeRegionCoordinate.queryGenomeCoorDatabaseRecord(tbl_name, biotype, analysis_type);

        //refname = GetRefDB.getDBFISubset(fi_subset, genome, region_plot, DB_tpye);
        //species = GetRefDB.getSpeciesName(genome, region_plot, DB_tpye);
        //QueryWholeRegionCoordinate.queryGenomeCoorDatabaseRecord(species, refname);
        // get the whole coordinate
        //QueryWholeRegionCoordinate.queryGenomeCoorDatabaseRecord(species, refname);

        List<Double> width_list = QueryWholeRegionCoordinate.width_list;
        // get the interval type
        interval_type = SetIntervalType.setIntervalTypeValue(width_list, region_labels, flank_region);

        // get data points
        DataPointNum.getDataPointNum();
        // exonModel for rnaseq
        /*if(analysis_type.equals("rnaseq")){
            //GetExonModelData.getExonModelData();
        }
        //*/
        GenerateJsonConfig.generateJsonConfig(output_path, config_name);
    }
}
