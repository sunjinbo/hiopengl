package com.hiopengl.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeUtil {
    public static String getCurrentTime(String format) {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    public static String getCurrentTime() {
        return getCurrentTime("yyyy-MM-dd HHmmss");
    }
}
