package com.example.myspot.util;

import android.content.Context;
import com.example.myspot.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd MMMM yyyy @HH:mm", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    /**
     * Formats a date to a readable string
     * @param date The date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }
    
    /**
     * Formats a date and time to a readable string
     * @param date The date to format
     * @return Formatted date and time string
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMAT.format(date);
    }
    
    /**
     * Gets a relative time string (e.g., "Updated today", "Updated yesterday")
     * @param context The application context
     * @param date The date to format
     * @return Relative time string
     */
    public static String getRelativeTimeString(Context context, Date date) {
        if (date == null) return "";
        
        Calendar calendar = Calendar.getInstance();
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        
        long diffInMillis = calendar.getTimeInMillis() - dateCalendar.getTimeInMillis();
        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
        
        if (diffInDays == 0) {
            return context.getString(R.string.updated_today);
        } else if (diffInDays == 1) {
            return context.getString(R.string.updated_yesterday);
        } else {
            return context.getString(R.string.updated_days_ago, (int) diffInDays);
        }
    }
    
    /**
     * Formats time only
     * @param date The date to format
     * @return Formatted time string
     */
    public static String formatTime(Date date) {
        if (date == null) return "";
        return TIME_FORMAT.format(date);
    }
}

