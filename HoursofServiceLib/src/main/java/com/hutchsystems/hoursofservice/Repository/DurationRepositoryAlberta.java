package com.hutchsystems.hoursofservice.Repository;

import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.DurationModel;
import com.hutchsystems.hoursofservice.model.DutyStatus;

/**
 * Created by Deepak Sharma on 3/22/2019.
 */

public class DurationRepositoryAlberta extends DurationModel {

    public double driving10 = 0d;

    // default rule we will check for 30 minute break.
    public boolean NonDriving30Fg = true;


    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 22 Aug 2018
    /// Purpose: reset all work shift hours
    /// </summary>
    public void ResetWorkShift() {
        // Properties related to workshift duration reset
        workShiftDriving = 0d;
        workShiftOnDuty = 0d;
        totalWorkshift = 0d;

        driving10 = 0d;

        // Properties related to Split sleep reset
        splitFg = false;
        splitDriving = 0d;
        splitOnDuty = 0d;
        splitWorkShift = 0d;

        // This properties hold driving duration without taking 30 minutes of break.
        // its used in US
        drivingWithoutBreak = 0d;

        // track status of driver if he has taken 30 minute break or 10 minute break
        NonDriving30Fg = true;

        reduceFg = false;
        ReducedTime = 0d;
    }

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 22 Aug 2018
    /// Purpose: adjust time before previous split
    /// </summary>
    /// <param name="duration">duration of split sleep</param>
    private void CarryForwardHours(double duration) {
        // remove split sleep from total workshift
        totalWorkshift -= duration;

        // save split sleep duration
        splitSleeper = duration;

        // subtract driving time before previous split
        workShiftDriving -= splitDriving;
        workShiftOnDuty -= splitOnDuty;
        totalWorkshift -= splitWorkShift;

        // sum of driving before and after should not exceed 13 hours. so we save driving before split sleep starts and subtract it from the total shift once we get next sleep
        splitDriving = workShiftDriving;
        splitOnDuty = workShiftOnDuty;
        splitWorkShift = totalWorkshift;
    }

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 21 August 2018
    /// Purpose: update duration of duty stauts as per respective rules
    /// </summary>
    /// <param name="item">This is Duty status item having all detail of duty status</param>
    /// <param name="duration">it holds duration of duty status</param>
    public void Update(DutyStatus item, double duration) {
        // Duty Status. 1-Off Duty, 2-Sleeper Berth, 3-Driving , 4-OnDuty
        int status = item.getStatus();

        // Rule when Duty Status was started. 1-Canada 70 Hours/7days, 2-Canada 120 Hours/14days, 3-US 70 Hours/8Days, 4- US 60 Hours/7 Days
        int rule = item.getRule();

        // calculate duration of status each day
        //AddDailyStatusTime(item, duration);

        // total workshift after reset add hours of all status
        totalWorkshift += duration;

        // to check rule 7(1)a, 7(1)b
        if (status == 3) {
            NonDrivingConsecutive = 0;
        } else
            NonDrivingConsecutive += duration;

        switch (status) {
            case 1: // Off Duty
            case 2: // Sleeper
                ResetTime += duration;
                offDuty += offDuty == -1 ? 0 : duration;

                // add consective sleeper duration in case driver add multiple consective event time span  between two days
                SleeperConsective += status == 1 ? 0 : duration;
                // if status is off duty then no need to check split sleep logic
                //if (status == 1) break;

                // if Split fg is true means there was a sleeper berth that is qualified as Split sleep
                if (splitFg) {
                    // Split logic for Canadian Law
                    if (canadaFg) {

                        // to find pair of previous split sleep
                        if (item.getStatus() == 2 && splitSleeper + SleeperConsective >= 8 * 60 * 60) {
                            // carry forward driving,onduty and workshift hours before spleeper berth
                            CarryForwardHours(SleeperConsective);
                        }
                    }
                } else {

                    // Split logic for Canadian Law
                    if (canadaFg) {

                        // if driver has selected qualified sleeper berth as split then set SplitFg true
                        if (item.getStatus() == 2 && item.getSplitFg() && SleeperConsective >= 2 * 60 * 60) {
                            // helps to find next pair of sleeer berth which is qualified as split sleep
                            splitFg = true;
                            // carry forward driving,onduty and workshift hours before spleeper berth
                            CarryForwardHours(SleeperConsective);
                        }
                    }
                }

                break;
            case 3:
            case 4:
                driving += item.getStatus() == 3 ? duration : 0;
                driving10 += item.getStatus() == 3 ? duration : 0;
                drivingWithoutBreak += item.getStatus() == 3 ? duration : 0;
                onDuty += duration;

                OnDutyList.set((OnDutyList.size() - 1), onDuty);

                if (canadaFg)
                    // on duty without 24 hours of break should not be more than 70 hours in cycle 2
                    onDutyWithoutBreak += duration;

                // driving under work shift rule
                workShiftDriving += status == 3 ? duration : 0;

                // on duty under work shift rule
                workShiftOnDuty += duration;

                // reset time is set to zero if duty status is driving or on duty
                ResetTime = 0;

                // reset this variable if non sleeper event occurs
                SleeperConsective = 0;
                break;
            default:
                break;
        }

        // if previous reset was reduced
        if (reduceFg) {
            // next reset should be 8 hours plus number of hours reduced
            if (ResetTime >= (8 * 60 * 60d) + ReducedTime) {

                ResetWorkShift();
            }
        } else {

            // if driver has reduced reset off duty time then save number of hours reduced
            // for calculating next reset
            if (item.isReducedFg()) {
                ResetWorkShift();
                ReducedTime = (8 * 60 * 60d) - ResetTime;
                reduceFg = true;
            } else {
                if (ResetTime >= 8 * 60 * 60d) {
                    ResetWorkShift();
                }
            }

        }

        // if consecutive non driving time is greater then or equal to 30 minutes then driver can driver 6 hour continuously
        if (NonDrivingConsecutive >= 30 * 60d) {


            // track status of driver if he has taken 30 minute break or 10 minute break
            NonDriving30Fg = true;

            drivingWithoutBreak = 0d;
            driving10 = 0d;
        } else if (NonDrivingConsecutive >= 10 * 60d) {

            driving10 = 0d;

            // track status of driver if he has taken 30 minute break or 10 minute break
            NonDriving30Fg = false;
        }

        // This condition is for section 25, 27A  of Canadian rule
        // This will not be aplicable for US rule
        if (ResetTime >= 24 * 60 * 60d) {
            // reset below variable if reset time is greater than 24
            onDutyWithoutBreak = 0d;

            // date when 24 hour break is taken
            LastBreak24Hours = TimeUtility.addSeconds(item.getEventDateTime(), (int) duration);
        }

    }

}
