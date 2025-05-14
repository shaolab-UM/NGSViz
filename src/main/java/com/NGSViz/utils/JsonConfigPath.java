package com.NGSViz.utils;

import com.NGSViz.configSet.ConfigReader;
import com.NGSViz.configSet.InputParameterAttributes;

import java.io.File;
import java.io.IOException;

/**
 * @author Benchen Ye
 * @create 2025-02--17:37
 */
public class JsonConfigPath extends InputParameterAttributes {
    public static void main(String[] args){
        getJsonConfigPath();
    }
    private static String config_path;
    private static String sys_config = sys_config_path;
    public static void getJsonConfigPath(){
        if (sys_config.isEmpty()){
            String current_dir = System.getProperty("user.dir");
            System.out.println("Current directory: " + current_dir);
            // Construct configuration file path
            config_path = current_dir + '/' + sys_config_name;
        } else {
            config_path = sys_config;
        }
        //System.out.println("system config path is " + config_path);
        File config_file = new File(config_path);
        // check
        if (config_file.exists()) {
            System.out.println("Config file found at: " + config_file.getAbsolutePath());
            db_path = ConfigReader.ParseSystemJson(config_file.getAbsolutePath());
            System.out.println("Tool's DB path is: " + db_path);
        } else {
            //System.out.println("Config file not found: " + config_file.getAbsolutePath());
            System.err.println("Error: The system config file or directory '" + config_file + "' does not exist.");
            System.exit(1); //
        }
    }
}
