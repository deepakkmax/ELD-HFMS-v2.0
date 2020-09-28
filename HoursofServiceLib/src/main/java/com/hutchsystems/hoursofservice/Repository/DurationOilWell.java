package com.hutchsystems.hoursofservice.Repository;

import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.DurationModel;
import com.hutchsystems.hoursofservice.model.DutyStatus;

/**
 * Created by Deepak Sharma on 5/14/2019.
 */

public class DurationOilWell extends DurationModel {

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 22 August 2018
    /// Purpose: reset all properties to 0 after driver reset rule
    /// after taking required consective off duty as per rule
    /// </summary>
    public void Reset() {
        // properties related to day duration of all duty status reset
        //OffDuty = 0d;
        //Driving = 0d;
        //OnDuty = 0d;

        // Properties related to workshift duration reset
        workShiftDriving = 0d;
        workShiftOnDuty = 0d;
        totalWorkshift = 0d;
        OnDutyList.clear();
        OnDutyList.add(0d);

        //ResetTime = 0d;

        // Properties related to Split sleep reset
        splitFg = false;
        markSplitFg = false;

        splitDriving = 0d;
        splitOnDuty = 0d;
        splitWorkShift = 0d;

        // This properties hold driving duration without taking 30 minutes of break.
        // its used in US
        drivingWithoutBreak = 0d;
    }


    public void ResetDailyTime() {
        offDuty = 0d;
        driving = 0d;
        onDuty = 0d;
        OffDutyExcluding8Hours = 0d;
    }

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

        // Properties related to Split sleep reset
        splitFg = false;
        markSplitFg = false;
        splitDriving = 0d;
        splitOnDuty = 0d;
        splitWorkShift = 0d;

        // This properties hold driving duration without taking 30 minutes of break.
        // its used in US
        drivingWithoutBreak = 0d;
    }

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 22 Aug 2018
    /// Purpose: adjust time before previous split
    /// </summary>
    /// <param name="duration">duration of split sleep</param>
    private void CarryForwardHours(double duration) {
        // remove split sleep from total workshift

        // subtract hours from workshift if in canadian rule or if in us then
        // sleeper between 8 and 10 hours would be deducted from workshift
        if (canadaFg || (!canadaFg && currentStatus == 2 && duration >= 8 * 60 * 60d && duration < 10 * 60 * 60d))
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

        currentStatus = status;

        // Rule when Duty Status was started. 1-Canada 70 Hours/7days, 2-Canada 120 Hours/14days, 3-US 70 Hours/8Days, 4- US 60 Hours/7 Days
        int rule = item.getRule();

        // calculate duration of status each day
        //AddDailyStatusTime(item, duration);

        // total workshift after reset add hours of all status
        totalWorkshift += duration;
        switch (status) {
            case 1: // Off Duty
            case 2: // Sleeper
                ResetTime += item.isWaitingFg() ? 0 : duration;
                offDuty += offDuty == -1 ? 0 : duration;

                // add consective sleeper duration in case driver add multiple consective event time span  between two days
                SleeperConsective += status == 1 ? 0 : duration;
                // if status is off duty then no need to check split sleep logic
                //if (status == 1) break;

                // if Split fg is true means there was a sleeper berth that is qualified as Split sleep
                if (splitFg) {

                    // to find pair of previous split sleep
                    if (item.getStatus() == 2 && splitSleeper + SleeperConsective >= (item.getCoDriverId() > 0 ? 8 * 60 * 60 : 10 * 60 * 60)) {
                        // carry forward driving,onduty and workshift hours before spleeper berth
                        CarryForwardHours(SleeperConsective);
                    }

                } else {
                    // if driver has selected qualified sleeper berth as split then set SplitFg true
                    if (item.getStatus() == 2 && item.getSplitFg() && SleeperConsective >= (item.getCoDriverId() > 0 ? 4 * 60 * 60 : 2 * 60 * 60)) {
                        // helps to find next pair of sleeer berth which is qualified as split sleep
                        splitFg = true;
                        // carry forward driving,onduty and workshift hours before spleeper berth
                        CarryForwardHours(SleeperConsective);
                    }
                }

                break;
            case 3:
            case 4:
                driving += item.getStatus() == 3 ? duration : 0;
                onDuty += duration;
                markSplitFg = false;

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

        // Reset Min limit is 36 hours for rule 1 , 72 hours for rule 2 and 34 hours for US Rule i.e 3 and 4
        // if Reset time is greater than minimum reset limit then reset all elapsed time to 0
        // It is rule reset calculation point

        if (ResetTime >= 8 * 60 * 60d)

        {
            ResetWorkShift();

            // following property will only updated when its canadian rule
            //if (item.Rule <= 2)
            //    // off duty time excluding mandatory 8 hours off duty time
            //    OffDutyExcluding8Hours += ResetTime - 8 * 60 * 60d;
        }

        // This condition is for section 25, 27A  of Canadian rule
        // This will not be aplicable for US rule
        if (ResetTime >= 24 * 60 * 60d) {

            // events are picked from last 24th day so add reset time
            if (ResetTime < 48 * 60 * 60d) // only consecutive period in multiple of 24 hours are added to get number of period of 24 hours
                totalOffDuty24Days += 24 * 60 * 60d;
            else if (ResetTime < 72 * 60 * 60d)
                totalOffDuty24Days += 48 * 60 * 60d;
            else
                totalOffDuty24Days += 72 * 60 * 60d;

            // reset below variable if reset time is greater than 24
            onDutyWithoutBreak = 0d;

            // date when 24 hour break is taken
            LastBreak24Hours = TimeUtility.addSeconds(item.getEventDateTime(), (int) duration);
        }

    }


}
