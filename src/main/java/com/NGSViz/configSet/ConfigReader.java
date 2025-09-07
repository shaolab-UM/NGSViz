package com.NGSViz.configSet;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            // check whether exist for the db_path
            Path path1 = Paths.get(db_path);
            Path path2 = Paths.get(tool_path).resolve(db_path);
            if (Files.exists(path1)) {
                db_path = db_path.toString();
            } else if (Files.exists(path2)) {
                db_path = path2.toString();
            } else {
                // If both paths do not exist, throw an exception.
                String errorMessage = "Error: The database file or directory does not exist. Checked paths: "
                        + path1 + " And " + path2;
                System.err.println(errorMessage);
                throw new IOException(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db_path;
    }
}
