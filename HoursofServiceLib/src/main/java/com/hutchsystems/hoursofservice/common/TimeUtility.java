package com.hutchsystems.hoursofservice.common;

import java.util.Calendar;
import java.util.Date;

public class TimeUtility {

    // Unit of Time
    public enum Unit{
        HOURS, MIN, SECONDS,DAY;

    }
    /// <summary>
    /// Created By: Pallavi Wattamwar
    /// Created Date: 22 September 2018
    /// Purpose: get difference basis on  day,Minutes, hours,Seconds
    /// </summary>
    /// <param name="startDate">start Date For calculating Difference </param>
    /// <param name="endDate">end Date For calculating Difference</param>
    ///<param name="unit">In which unit we want difference between this dates </param>
    public static float getDiff(Date startDate, Date endDate,Unit unit)
    {
        float diff = 0f;
        switch (unit)
        {
            case DAY:
                diff = differenceInDay(startDate,endDate);
                break;
            case MIN:
                diff =  differenceInMints(startDate,endDate);
                break;
            case HOURS:
                diff = differenceInHours(startDate,endDate);
                break;
            case SECONDS:
                diff = differenceInSeconds(startDate,endDate);
                break;
        }

        return diff;
    }

    /// <summary>
    /// Created By: Pallavi Wattamwar
    /// Created Date: 22 September 2018
    /// Purpose: get difference between two dates in Minutes
    /// </summary>
    /// <param name="startDate">start Date For calculating Difference </param>
    /// <param name="endDate">end Date For calculating Difference</param>

    public static float differenceInMints(Date startDate, Date endDate) {
        float mins = 0f;
        try {
            long millis = endDate.getTime() - startDate.getTime();
            mins = (1.0f * millis) / (1000f * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mins;
    }

    /// <summary>
    /// Created By: Pallavi Wattamwar
    /// Created Date: 22 September 2018
    /// Purpose: get difference between two dates in Hours
    /// </summary>
    /// <param name="startDate">start Date For calculating Difference </param>
    /// <param name="endDate">end Date For calculating Difference</param>

    public static float differenceInHours(Date startDate, Date endDate) {
        float hours = 0f;
        try {

            long millis = endDate.getTime() - startDate.getTime();
            hours = (1.0f * millis) / (1000f * 60 * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hours;
    }

    /// <summary>
    /// Created By: Pallavi Wattamwar
    /// Created Date: 22 September 2018
    /// Purpose: get difference between two dates in Day
    /// </summary>
    /// <param name="startDate">start Date For calculating Difference </param>
    /// <param name="endDate">end Date For calculating Difference</param>

    public static int differenceInDay(Date startDate, Date endDate) {

        float days = 0;
        try {

            long millis = endDate.getTime() - startDate.getTime();
            days = (1.0f * millis) / (1000f * 60 * 60 * 24);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) days;
    }
    /// <summary>
    /// Created By: Pallavi Wattamwar
    /// Created Date: 22 September 2018
    /// Purpose: get difference between two dates in Seconds
    /// </summary>
    /// <param name="startDate">start Date For calculating Difference </param>
    /// <param name="duration">end Date For calculating Difference</param>


    public static float differenceInSeconds(Date startDate, Date endDate) {
        float hours = 0f;
        try {

            long millis = endDate.getTime() - startDate.getTime();
            hours = (1.0f * millis) / (1000f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hours;
    }

    /// <summary>
    /// Created By: Pallavi Wattamwar
    /// Created Date: 22 September 2018
    /// Purpose: To add the days in given date
    /// </summary>
    /// <param name="date">date </param>
    /// <param name="timeFrequency">no of days</param>
    public static Date addDays(Date date, int timeFrequency) {

        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, timeFrequency);
            return c.getTime();
        } catch (Exception e) {
        }
        return null;
    }

    /// <summary>
    /// Created By: Pallavi Wattamwar
    /// Created Date: 22 September 2018
    /// Purpose: To add the seconds in given date
    /// </summary>
    /// <param name="date">date </param>
    /// <param name="timeFrequency">no of days</param>
    public static Date addSeconds(Date date, int timeFrequency) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.SECOND, timeFrequency);
            return c.getTime();
        } catch (Exception e) {
        }
        return null;
    }

}
