package com.hutchsystems.hoursofservice.Repository;

import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.DurationModel;
import com.hutchsystems.hoursofservice.model.DutyStatus;


public class DurationRepository extends DurationModel {


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
        if ((canadaFg || (!canadaFg && currentStatus == 2 && duration >= 8 * 60 * 60d && duration < 10 * 60 * 60d)) || newSplitSleep == 1)
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


    private void setSplitSleepHours(DutyStatus item, double duration) {

        // if Split fg is true means there was a sleeper berth that is qualified as Split sleep
        if (splitFg) {
            // Split logic for Canadian Law
            if (canadaFg) {

                // to find pair of previous split sleep
                if (item.getStatus() == 2 && splitSleeper + SleeperConsective >= (item.getCoDriverId() > 0 ? 8 * 60 * 60 : 10 * 60 * 60)) {
                    // carry forward driving,onduty and workshift hours before spleeper berth
                    CarryForwardHours(SleeperConsective);
                }
            } else // Split logic for US Law
            {

                // if sum of sleeper is greater than 10 and second segment is off duty then split sleeper duration should be greater than 8 hours
                // if previous segment is off duty between 2 and 8 hours then next segment should be sleeper betweewn 8 and 10 hours
                // if previous segment is sleeper between 8 and 10 hours then next segment should be off duty between 2 and 8 hours

                if (splitSleeper + ResetTime >= 10 * 60 * 60 && (splitSleeper >= 8 * 60 * 60d || (SleeperConsective >= 8 * 60 * 60d && SleeperConsective < 10 * 60 * 60d))) {

                    double newDuration = splitSleeper >= 8 * 60 * 60d ? duration : SleeperConsective;

                    // carry forward driving,onduty and workshift hours before spleeper berth
                    CarryForwardHours(newDuration);

                }
            }
        } else {

            // Split logic for Canadian Law
            if (canadaFg) {

                // if driver has selected qualified sleeper berth as split then set SplitFg true
                if (item.getStatus() == 2 && item.getSplitFg() && SleeperConsective >= (item.getCoDriverId() > 0 ? 4 * 60 * 60 : 2 * 60 * 60)) {
                    // helps to find next pair of sleeer berth which is qualified as split sleep
                    splitFg = true;
                    // carry forward driving,onduty and workshift hours before spleeper berth
                    CarryForwardHours(SleeperConsective);
                }
            } else // Split logic for US Law
            {
                if (item.getSplitFg()) {
                    markSplitFg = true;
                }

                // if driver has selected qualified sleeper berth as split then set SplitFg true
                // for US Law driver can split sleep in 8 and 2 hours segment in which sleeper should be of 8 hours
                // if previous segment is off duty between 2 and 8 hours then next segment should be sleeper betweewn 8 and 10 hours
                // if previous segment is sleeper between 8 and 10 hours then next segment should be off duty between 2 and 8 hours
                //if (markSplitFg && ((item.getStatus() == 2 && newDuration >= 8 * 60 * 60d && duration < 10 * 60 * 60d) || (item.getStatus() <= 2 && newDuration >= 2 * 60 * 60d && newDuration < 8 * 60 * 60d))) {
                if (markSplitFg && ((item.getStatus() == 2 && SleeperConsective >= 8 * 60 * 60d && SleeperConsective < 10 * 60 * 60d) || (item.getStatus() <= 2 && ResetTime >= 2 * 60 * 60d && ResetTime < 8 * 60 * 60d))) {

                    // if sleeper berth is between 8 and 10 hours consider this
                    // else reset time is current duration
                    double newDuration = (item.getStatus() == 2 && SleeperConsective >= 8 * 60 * 60d && SleeperConsective < 10 * 60 * 60d) ? SleeperConsective : ResetTime;
                    // helps to find next pair of sleeer berth which is qualified as split sleep
                    splitFg = true;
                    // carry forward driving,onduty and workshift hours before spleeper berth
                    CarryForwardHours(newDuration);
                }
            }
        }
    }

    private void setSplitSleepHoursNew(DutyStatus item, double duration) {

        // if not sleeper then return
        // if next status is also sleeper berth then move cursor to next item for calculation
        if (item.getStatus() != 2 || item.getNextStatus() == 2)
            return;

        // if Split fg is true means there was a sleeper berth that is qualified as Split sleep
        if (splitFg) {

            double threshold = canadaFg && item.getCoDriverId() > 0 ? 8 * 60 * 60d : 10 * 60 * 60d;
            double totalSleep = splitSleeper + SleeperConsective;

            // if sum of both status not satisfying thrshold
            // and if rule is usa and both part of sleep is not between sub set (2,8), (3,7), (8,2) or (7,3)
            if (totalSleep < threshold || (!canadaFg && splitSleeper < 7 * 60 * 60d && SleeperConsective < 7 * 60 * 60d))
                return;

            CarryForwardHours(SleeperConsective);

        } else {

            // if break is consecutive in parts e.g 1 hours sleeper then 30 min sleeper then 30 min sleeper leads to 2 hours
            if (item.getSplitFg()) {
                markSplitFg = true;
            }

            double threshold = canadaFg && item.getCoDriverId() > 0 ? 4 * 60 * 60d : 2 * 60 * 60d;
            // status should be sleeper berth
            // if USA Rule driver can split in two part of (2 and 8) or (3 and 7) or vice verse
            // if Canada Rule driver can split like (2,8) or (3,7) or (4,6) or (5,5)
            if (markSplitFg && SleeperConsective >= threshold) {

                // helps to find next pair of sleeer berth which is qualified as split sleep
                splitFg = true;
                // carry forward driving,onduty and workshift hours before spleeper berth
                CarryForwardHours(SleeperConsective);
            }

        }
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
        int rule = RuleId;

        // calculate duration of status each day
        //AddDailyStatusTime(item, duration);

        // total workshift after reset add hours of all status
        totalWorkshift += duration;

        // if non driving event then add to 30min rest
        if (status == 3) {
            Reset30MinBreak = 0;
        } else {
            Reset30MinBreak += duration;
        }

        switch (status) {
            case 1: // Off Duty
            case 2: // Sleeper
                ResetTime += duration;
                offDuty += offDuty == -1 ? 0 : duration;

                // add consective sleeper duration in case driver add multiple consective event time span  between two days
                SleeperConsective += status == 1 ? 0 : duration;

                if (newSplitSleep == 1) {
                    setSplitSleepHoursNew(item, duration);
                } else {
                    setSplitSleepHours(item, duration);
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

        // for canada object we will consider only canadian rule and vice verse
        if (ResetTime >= (canadaFg ? (rule == 2 ? 72 : 36) : 34) * 60 * 60d) {
            Reset();

            // following property will only updated when its canadian rule
            //if (item.Rule <= 2)
            // off duty time excluding mandatory 8 hours off duty time
            //OffDutyExcluding8Hours += duration;
        }

        // Work shift reset limit is 8 hours for canadian rule and 10 hours for US Rule
        // Reset all elapased time related to workshift to 0
        else if (ResetTime >= (canadaFg ? 8 : 10) * 60 * 60d) {
            ResetWorkShift();

            // following property will only updated when its canadian rule
            //if (item.Rule <= 2)
            //    // off duty time excluding mandatory 8 hours off duty time
            //    OffDutyExcluding8Hours += ResetTime - 8 * 60 * 60d;
        } else if (ResetTime >= 30 * 60d || Reset30MinBreak >= 30 * 60d) // off duty more than 30 minute

        {
            // following property will only updated when its canadian rule
            if (canadaFg) {
                // only off duty more than or equal to 30 minute will be considered
                // if off duty or sleeper is between 2 to 8 hour then add to OffDutyExcluding8Hours
                //OffDutyExcluding8Hours += OffDutyExcluding8Hours == -1 ? 0 : ResetTime;
            } else
                // This is driving time without taking 30 minute of break only for US Rule
                drivingWithoutBreak = 0d;


        } else {
            drivingWithoutBreak += duration;
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
