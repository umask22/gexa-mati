package ar.gexa.app.eecc.utils;


import android.text.format.DateFormat;
import android.widget.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static class Pattern {

        public static final String DEFAULT = "yyyy-MM-dd'T'HH:mm:ss";
        public static final String CODE = "yyyyMMddHHmmss";

        public static final String DD_MM_YY = "dd/MM/yy";
        public static final String DD_MM_HH_MM = "dd/MM HH:mm";

        public static final String DD_MM_YY_HH_MM = "dd/MM/yy HH:mm";
        public static final String HH_MM = "HH:mm";
        public static final String DD_MM = "dd/MM";
    }

    public static String toString(Date date, String pattern) {
        if(date == null)
            return "";
        return (String) DateFormat.format(pattern, date);
    }

    public static String parse(String date, String patternFrom, String patternUntil) {
        final SimpleDateFormat formatter = new SimpleDateFormat(patternFrom);
        try {
            return toString(formatter.parse(date), patternUntil);
        } catch (ParseException e) {}
        return null;
    }

    public static Date parse(String date, String pattern) {
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parse(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendario = Calendar.getInstance();
        calendario.set(year, month, day);
        return calendario.getTime();
    }

    public static String toStringFrom(Date date, String pattern) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        return toString(calendar.getTime(), pattern);
    }

    public static String toStringUntil(Date date, String pattern) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return toString(calendar.getTime(), pattern);
    }
}
