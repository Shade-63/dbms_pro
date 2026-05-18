package com.hospital.bedalloc.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(Date date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    public static String formatTimestamp(Date date) {
        return date == null ? "" : TIMESTAMP_FORMAT.format(date);
    }
    
    public static java.sql.Date toSqlDate(Date date) {
        return date == null ? null : new java.sql.Date(date.getTime());
    }
}
