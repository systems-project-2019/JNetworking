package lib.misc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Josh Hilbert
 */
public class Time {

    /**
     * @return the system time when the method was called as a String
     */
    public static String getCurrentTime() {
        LocalDateTime timePointNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        return timePointNow.format(formatter);
    }
}
