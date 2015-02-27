package com.satelinx.satelinx.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jlh on 2/26/15.
 */
public class DateHelper {

    public static String getFormattedString(Date date) {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_YEAR);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        int dateDay = dateCal.get(Calendar.DAY_OF_YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");

        if (day == dateDay) {
            sdf = new SimpleDateFormat("HH:mm");
        }
        return sdf.format(date);
    }

}
