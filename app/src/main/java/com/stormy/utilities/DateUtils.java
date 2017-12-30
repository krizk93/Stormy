package com.stormy.utilities;

import android.content.Context;

import com.stormy.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Sofia on 12/30/2017.
 */

public class DateUtils {

    public static String formatDate(Context context) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.timeFormat), Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    public static long getNextDaysDate(int daysToAdd) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, daysToAdd);
        return calendar.getTimeInMillis();
    }

    public static String getDayOfWeek(Context context, long timeInMillis){
        SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.dayOfWeek), Locale.getDefault());
        return sdf.format(timeInMillis);
    }
}
