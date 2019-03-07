package lib.misc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Time {

    public static String getCurrentTime() {
        LocalDateTime timePointNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        return timePointNow.format(formatter);
    }
}
