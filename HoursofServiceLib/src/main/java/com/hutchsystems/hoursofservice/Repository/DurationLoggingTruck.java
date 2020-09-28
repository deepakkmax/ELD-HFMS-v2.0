package com.hutchsystems.hoursofservice.Repository;

import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.DurationModel;
import com.hutchsystems.hoursofservice.model.DutyStatus;

/**
 * Created by Deepak Sharma on 5/14/2019.
 */

public class DurationLoggingTruck extends DurationModel {

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 14 May 2019
    /// Purpose: reset all work shift hours
    /// </summary>
    public void ResetWorkShift() {
        // Properties related to workshift duration reset
        workShiftDriving = 0d;
        workShiftOnDuty = 0d;
        totalWorkshift = 0d;
    }

    public void ResetDailyTime() {
        offDuty = 0d;
        driving = 0d;
        onDuty = 0d;
    }

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 14 May 2019
    /// Purpose: update duration of duty stauts as per respective rules
    /// </summary>
    /// <param name="item">This is Duty status item having all detail of duty status</param>
    /// <param name="duration">it holds duration of duty status</param>
    public void Update(DutyStatus item, double duration) {
        // Duty Status. 1-Off Duty, 2-Sleeper Berth, 3-Driving , 4-OnDuty
        int status = item.getStatus();

        // total workshift after reset add hours of all status
        totalWorkshift += duration;

        switch (status) {
            case 1: // Off Duty
            case 2: // Sleeper
                ResetTime += duration;
                break;
            case 3:
            case 4:
                onDuty += duration;
                driving += item.getStatus() == 3 ? duration : 0;

                // track onduty time for consecutive days
                OnDutyList.set((OnDutyList.size() - 1), onDuty);

                // track driving time for consective days
                DrivingList.set((DrivingList.size() - 1), driving);

                // driving under work shift rule
                workShiftDriving += status == 3 ? duration : 0;

                // on duty under work shift rule
                workShiftOnDuty += duration;

                // reset time is set to zero if duty status is driving or on duty
                ResetTime = 0;
                break;
            default:
                break;
        }

        if (ResetTime >= 9 * 60 * 60d) {
            ResetWorkShift();
        }

        // This condition is for section 25, 27A  of Canadian rule
        // This will not be aplicable for US rule
        if (ResetTime >= 24 * 60 * 60d) {
            // date when 24 hour break is taken
            LastBreak24Hours = TimeUtility.addSeconds(item.getEventDateTime(), (int) duration);
        }

    }

}
