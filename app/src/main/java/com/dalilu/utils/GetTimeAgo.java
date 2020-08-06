package com.dalilu.utils;

import com.dalilu.Dalilu;
import com.dalilu.R;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.Calendar;

public class GetTimeAgo {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {

            return Dalilu.getDaliluAppContext().getResources().getString(R.string.justNow);

        } else if (diff < 2 * MINUTE_MILLIS) {

            return Dalilu.getDaliluAppContext().getResources().getString(R.string.aMinuteAgo);

        } else if (diff < 50 * MINUTE_MILLIS) {

            return MessageFormat.format("{0} {1}", diff / MINUTE_MILLIS, Dalilu.getDaliluAppContext().getResources().getString(R.string.minuteAgo));

        } else if (diff < 90 * MINUTE_MILLIS) {

            return Dalilu.getDaliluAppContext().getResources().getString(R.string.anHourAgo);

        } else if (diff < 24 * HOUR_MILLIS) {

            return MessageFormat.format("{0} {1}", diff / HOUR_MILLIS, Dalilu.getDaliluAppContext().getResources().getString(R.string.hoursAgo));

        } else if (diff < 48 * HOUR_MILLIS) {
            return Dalilu.getDaliluAppContext().getResources().getString(R.string.yesterday);
        } else {
            return MessageFormat.format("{0} {1}", diff / DAY_MILLIS, Dalilu.getDaliluAppContext().getResources().getString(R.string.daysAgo));
        }
    }

    public static long getTimeInMillis() {
        long timeInMillis;
        Calendar calendar = Calendar.getInstance();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            timeInMillis = ZonedDateTime.now().toInstant().toEpochMilli();
        } else {

            timeInMillis = calendar.getTimeInMillis();
        }

        return timeInMillis;
    }

}
