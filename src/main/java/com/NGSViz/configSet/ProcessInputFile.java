package com.NGSViz.configSet;

import com.NGSViz.utils.ReadTextFile;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @ create 2024-11--21:36
 * @ function process input file, bam file or txt/csv file
 */
public class ProcessInputFile extends InputParameterAttributes {
    public static void processInputFile(){
        file_type = getFileExtensionName(input_file);
        if(file_type.equals("bam")){
            bam_list.add(input_file);
            gene_list.add(genes);
            analysis_title.add(title);
            group_list.add("GroupName");
            sample_list.add("SmapleName");
        }else if(file_type.equals("txt")){
            Map<String, Object> res_list = ReadTextFile.readTxtConfigFile(input_file);
            if(res_list != null){
                bam_list = (List<String>) res_list.get("bam_list");
                gene_list = (List<String>) res_list.get("gene_list");
                analysis_title = (List<String>) res_list.get("title_list");
                group_list = (List<String>) res_list.get("group_list");
                sample_list = (List<String>) res_list.get("sample_list");
            }
        }else if(file_type.equals("csv")){
            Map<String, Object> res_list = ReadTextFile.readCsvConfigFile(input_file);
            if(res_list != null){
                bam_list = (List<String>) res_list.get("bam_list");
                gene_list = (List<String>) res_list.get("gene_list");
                analysis_title = (List<String>) res_list.get("title_list");
                group_list = (List<String>) res_list.get("group_list");
                sample_list = (List<String>) res_list.get("sample_list");
            }
        }else{
            System.out.println("Your input file `" + input_file +  "` is Unknown file type!");
        }
        System.out.println("bam list: " + bam_list);
        System.out.println("gene list: " + gene_list);
        System.out.println("analysis_title: " + analysis_title);
        System.out.println("Sample name is :" + sample_list);
        System.out.println("Group name is : " + group_list);
        System.out.println("---------");
    }
    public static String getFileExtensionName(String file_name){
        int dot_index = file_name.lastIndexOf('.');
        String extension = (dot_index == -1) ? "" : file_name.substring(dot_index + 1);
        System.out.println("file extension name is : " + extension);
        return extension;
    }
}
