package com.NGSViz.configSet;

import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Benchen Ye
 * @create 2025-02--17:47
 */
public class ConfigReader {
    public static String db_path = "";
    public static String ParseSystemJson(String config_path){
        try {
            // get JSON file
            String content = new String(Files.readAllBytes(Paths.get(config_path)));
            // parse JSON content
            JSONObject jsonObject = new JSONObject(content);
            // get versionNum obj
            JSONObject versionNum = jsonObject.getJSONObject("versionNum");
            String version = versionNum.getString("version");
            System.out.println("Version: " + version);
            // get toolParas obj
            JSONObject toolParas = jsonObject.getJSONObject("toolParas");
            String tool_path = toolParas.getString("tool_path");
            db_path = toolParas.getString("db_path");
            System.out.println("Tool Path: " + tool_path);
            System.out.println("DB Path: " + db_path);
            db_path = tool_path + db_path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db_path;
    }
}
