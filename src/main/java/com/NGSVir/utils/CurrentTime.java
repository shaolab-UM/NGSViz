package com.NGSVir.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Benchen Ye
 * @create 2024-10--21:14
 */
public class CurrentTime {
    public static String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        return LocalDateTime.now().format(formatter);
    }
}
