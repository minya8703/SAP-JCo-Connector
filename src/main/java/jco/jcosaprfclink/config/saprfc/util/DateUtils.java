package jco.jcosaprfclink.config.saprfc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final String SAP_DATE_FORMAT = "yyyyMMdd";
    
    public static Date parseSapDate(String sapDate) {
        if (sapDate == null || sapDate.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(SAP_DATE_FORMAT).parse(sapDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid SAP date format: " + sapDate);
        }
    }
    
    public static String formatSapDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(SAP_DATE_FORMAT).format(date);
    }
} 