package de.uniks.stp.util;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    public static boolean isYesterday(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return isSameDay(date, cal.getTime());
    }

    public static String formatTime(long time, Locale locale) {
        Date date = new Date();
        date.setTime(time);

        if (DateUtil.isToday(date)) {
            return ViewLoader.loadLabel(Constants.LBL_TIME_FORMATTING_TODAY) + ", " + DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(date);
        } else if (DateUtil.isYesterday(date)) {
            return ViewLoader.loadLabel(Constants.LBL_TIME_FORMATTING_YESTERDAY) + ", " + DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(date);
        }

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale).format(date);
    }
}
