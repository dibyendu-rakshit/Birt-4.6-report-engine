package com.greytip.birtreport.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Dibyendu on 12/18/2017.
 */
public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public static final String DMY_FORMAT = "dd MMM yyyy";
    public static final String DMY_HM_FORMAT = "dd MMM yyyy HH:mm:ss";


    /**
     * Formats the given date into 'dd MMM yyyy' format. e.x.: 01 May 2003
     *
     * @param date The date to be formatted.
     * @return The formatted date as a string.
     */
    public static String formatDate(Date date) {
        return formatDate(date, DMY_FORMAT);
    }

    public static String formatDate(Date d, String format) {
        return format(d, format, null);
    }

    public static String format(Date d, String format, TimeZone zone) {
        SimpleDateFormat f = new SimpleDateFormat(format);
        if (zone != null) {
            f.setTimeZone(zone);
        }
        return d == null ? "" : f.format(d);
    }

    public static Date parseDate(String date) {
        return parseDate(date, DMY_FORMAT);
    }

    public static Date parseDate(String date, String format) {
        Date result = null;
        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            ParsePosition pp = new ParsePosition(0);
            result = StringUtils.isEmpty(date) ? null : df.parse(date, pp);
            if (date.length() != pp.getIndex()) {
                result = null;
            }
        } catch (Exception e) {
            logger.trace("Error while parsing date: {} with format: {} " , date, format);
            e.printStackTrace();
        }
        return result;
    }

    public static Date parseDateTime(String date) {
        return parseDateTime(date, DMY_HM_FORMAT);
    }

    public static Date parseDateTime(String date, String format) {
        Date result = null;
        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            ParsePosition pp = new ParsePosition(0);
            result = StringUtils.isEmpty(date) ? null : df.parse(date, pp);
            if (date.length() != pp.getIndex()) {
                result = null;
            }
        } catch (Exception e) {
            logger.trace("Error while parsing date: {} with format: " , date,format );
            e.printStackTrace();
        }
        return result;
    }
}
