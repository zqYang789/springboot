package com.sunsan.framework.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TimeUtils {
    public static LocalDateTime getLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date getDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static long getSeconds(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    public static long getSeconds(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }


    public static Date parseDate(String dateStr) {
        Date date = null;
        String[] parsePatterns = new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yy-MM-dd", "yyyy/MM/dd"};
        try {
            date = DateUtils.parseDate(dateStr, parsePatterns);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
