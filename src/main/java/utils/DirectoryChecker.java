package utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Benchen Ye
 * @create 2024-11--21:24
 * @function check whether the directory exist, if not create it.
 */
public class DirectoryChecker {
    public static void checkDirectoryExist(String directory_path) {
        Path dirPath = Paths.get(directory_path);
        if (Files.exists(dirPath)) {
            System.out.println("The directory already exists.: " + directory_path);
        } else {
            try {
                Files.createDirectories(dirPath);
                System.out.println("The directory has benn created: " + directory_path);
            } catch (Exception e) {
                System.out.println("An error occurred while creating the directory: " + e.getMessage());
            }
        }
    }
    public static String removeTrailingSlash(String path) {
        // Check if the path ends with a slash and remove it.
        if (path.endsWith("/") || path.endsWith("\\")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
