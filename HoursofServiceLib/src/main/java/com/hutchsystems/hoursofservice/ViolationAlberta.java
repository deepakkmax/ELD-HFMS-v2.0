package com.hutchsystems.hoursofservice;

import com.hutchsystems.hoursofservice.Repository.DurationRepositoryAlberta;
import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.common.Utility;
import com.hutchsystems.hoursofservice.model.DutyStatus;
import com.hutchsystems.hoursofservice.model.ViolationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


public class ViolationAlberta extends ViolationModel {

    //#region Class Variables

    // from date and to date between list
    Date fromDate, toDate;

    // this are the minutes added to current time to get future violations
    int futureTime = 0;

    // list of duty status between fromDate and to Date
    ArrayList<DutyStatus> list;

    // rule list
    ArrayList<DutyStatus> ruleList;

    // violation List
    ArrayList<ViolationModel> _violationList = new ArrayList<>();

    // object to hold duration of US and Canada respectively
    DurationRepositoryAlberta objCanada;
    int currentRule = 1;
    //   #endregion

    //   #region Public constructor

    // Parameterized constructor
    public ViolationAlberta(Date fromDate, Date toDate, ArrayList<DutyStatus> list, ArrayList<DutyStatus> ruleList, int futureTime, int currentRule) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        //this.list = list;
        this.ruleList = ruleList;

        copyList(list);
        this.futureTime = futureTime;

        // initialize object to keep duration in canada
        objCanada = new DurationRepositoryAlberta();
        objCanada.canadaFg = true;
        objCanada.OnDutyList.add(0d);
        this.currentRule = currentRule;

        // last date when 24 hour break was taken default value is from date
        objCanada.LastBreak24Hours = fromDate;

    }

    /// Created By: Deepak sharma
    // Created Date: 06 November 2018
    // Purpose: get object to get hours as per rule
    private void copyList(ArrayList<DutyStatus> list) {
        this.list = new ArrayList<>();
        this.list.addAll(list);
        //this.list.addAll(ruleList);

       /* for (DutyStatus item : list) {
            DutyStatus bean = new DutyStatus(item);
            this.list.add(bean);
        }*/
    }
    //  #endregion
/// Created By: Deepak sharma
    // Created Date: 06 November 2018
    // Purpose: get object to get hours as per rule
    public DurationRepositoryAlberta HoursGet() {
        return objCanada;
    }
    // Created By: Deepak Sharma
    // Created Date: 13 Nov 2018
    // Purpose: add driving duty status to perdict future violations if driver is not driving
    public static DutyStatus AddDriving(Date date, int rule, int coDriver) {
        DutyStatus bean = new DutyStatus();
        bean.setEventDateTime(date);
        bean.setRule(rule);
        bean.setCoDriverId(coDriver);
        bean.setSplitFg(false);
        bean.setDeferedDay(0);
        bean.setStatus(3);
        bean.setPersonalUseFg(0);
        return bean;
    }

    //   #region Methods
    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 20 August 2018
    /// Purpose: calculate violation of respective driver with in selected dates
    /// </summary>
    /// <param name="list"> list of duty status of driver between selected date</param>
    public ArrayList<ViolationModel> Calculate() {
        _violationList.clear();
        // if toDate is greater than current date then we will change value of toDate to current date
        //toDate = toDate.after(new Date()) ? new Date() : toDate;
        DutyStatus futureEvent = null;
        if (list.size() > 0) {

            DutyStatus currentStatus = list.get(list.size() - 1);
            futureEvent = new DutyStatus(currentStatus);
            futureEvent.setStatus(3);
            futureEvent.setEventDateTime(TimeUtility.addSeconds(toDate, -futureTime * 60));
            futureEvent.setRule(currentRule);
            list.add(futureEvent);
        }

        Collections.sort(list, DutyStatus.dateAsc);

        // loop through all of the dutystatus
        for (int i = 0; i < list.size(); i++) {
            // pick duty status at index i
            DutyStatus item = list.get(i);

            // this is static check. remove this line after testing is complete
            // item.setSplitFg(true);

            // declare variable to hold next duty status
            DutyStatus nextItem = null;

            // if next duty status is same then increase value of i till we get differnt next duty status.
            while (i < list.size() - 1 && list.get(i + 1).getStatus() == item.getStatus()) { //&& list.get(i + 1).getRule() == item.getRule()
                i++;
            }

            // pick next Duty Status if exists
            nextItem = i == list.size() - 1 ? null : list.get(i + 1);

            // Date of duty status
            Date eventDate = item.getEventDateTime();

            // Date of next duty status if it exists else put to Date in next date
            Date nextDate = nextItem == null ? toDate : nextItem.getEventDateTime();

            // to check if next date and event date fall in different day
            int diff = (int) TimeUtility.getDiff(Utility.dateOnly(eventDate), Utility.dateOnly(nextDate), TimeUtility.Unit.DAY);

            // Duration of duty status
            //  double duration = nextDate.Subtract(eventDate).TotalSeconds;
            double duration = TimeUtility.getDiff(eventDate, nextDate, TimeUtility.Unit.SECONDS);

            if (diff == 0) {
                // Calculate violation
                ProcessViolations(item, duration);

            } else {
                // in case next event fall in different day
                for (int j = 0; j <= diff; j++) {
                    // get new event date as per new date
                    // for first record event date remain same
                    eventDate = (j == 0 ? eventDate : TimeUtility.addDays(Utility.dateOnly(eventDate), 1));

                    // get new next date
                    // for last record next date remain same
                    Date newNextDate = (j == diff ? nextDate : TimeUtility.addDays(Utility.dateOnly(eventDate), 1));

                    // get new duration
                    //   duration = newNextDate.Subtract(eventDate).TotalSeconds;
                    duration = TimeUtility.getDiff(eventDate, newNextDate, TimeUtility.Unit.SECONDS);

                    DutyStatus newItem = new DutyStatus(item);
                    newItem.setEventDateTime(eventDate);

                    // update event date in item
                    // item.eventDateTime = eventDate;
                    //item.setEventDateTime(eventDate);


                    // calculate once at end of the day
                   /* if (!rule14BFg) {
                        Violation14B(list, i, duration);
                        rule14BFg = true;
                    }*/


                    // Calculate violation
                    ProcessViolations(newItem, duration);
                }
            }
        }


        Collections.sort(_violationList, ViolationAlberta.dateAsc);
        return _violationList;
    }

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 20 August 2018
    /// Purpose: calculate violation
    /// </summary>
    /// <param name="item">current duty status as per iteration</param>
    /// <param name="duration">duration of the current duty status as per iteration</param>
    private void ProcessViolations(DutyStatus item, double duration) {

        // update time of duty status as per rule of Canada
        objCanada.Update(item, duration);

        // only calculate violation of event between from date and to date for respective driver
        // if (item.eventDateTime < fromDate)
        if (item.getEventDateTime().before(fromDate))

            return;

        // do not find violation for non driving duty status
        // violation only occur while driver is in driving duty status except 14 rule
        if (item.getStatus() != 3) {
            return;
        }

        // Only find violation for canadian rule
       /* if (item.getRule() <= 2) // These are the rule when driver drives in Canada
        {*/

        // check and add violation belongs to 13A section
        AddViolation("6(2a)", item.getEventDateTime(), duration, objCanada.workShiftDriving);

        // check and add violation belongs to 13B section
        AddViolation("6(2b)", item.getEventDateTime(), duration, objCanada.workShiftOnDuty);

        if (objCanada.NonDriving30Fg) {

            AddViolation("7(1b)", item.getEventDateTime(), duration, objCanada.drivingWithoutBreak);
        } else {
            // check and add violation belongs to 13B section
            AddViolation("7(1a)", item.getEventDateTime(), duration, objCanada.driving10);
        }
    }

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 21 August 2018
    /// Purpose: create violation object
    /// </summary>
    /// <param name="rule">section of violation</param>
    /// <param name="violationDate">violation date</param>
    public void AddViolation(String rule, Date violationDate) {
        // get violation info by rule e.g 12A,12B ... etc
        ViolationModel violationInfo = Utility.violationInfoGet(rule);

        // get violation start time
        violationInfo.setViolationDate(violationDate);

        int ruleId = RuleGet(violationDate);

        // we show next day violation
        if (ruleId == 5 && !(violationInfo.getViolationDate().before(fromDate))) {
            // it's date when we added a virtual driving event to show purposed violation to driver
            // if he drives vehicle
            Date futureDate = TimeUtility.addSeconds(toDate, -futureTime * 60);

            violationInfo.setVirtualFg(futureDate.equals(violationInfo.getViolationDate()));

            // add violation to list
            _violationList.add(violationInfo);
        }

    }


    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 21 August 2018
    /// Purpose: create violation object
    /// </summary>
    /// <param name="rule">section of violation</param>
    /// <param name="eventDateTime">duty status datetime </param>
    /// <param name="duration">duration of duty status</param>
    /// <param name="totalElapsedTime">total onduty time elapsed</param>
    /// <returns>returns true if violation occurs</returns>
    public Boolean AddViolation(String rule, Date eventDateTime, double duration, double totalElapsedTime) {
        // if rule does not exists then don't add violation
        if (rule == null || rule.isEmpty()) return false;

        // get violation info by rule e.g 12A,12B ... etc
        ViolationModel violationInfo = Utility.violationInfoGet(rule);

        // check if violation occurs
        Boolean violationFg = totalElapsedTime > violationInfo.getThreshold();

        AddViolation(rule, eventDateTime, duration, totalElapsedTime, violationFg);

        return violationFg;
    }

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 21 August 2018
    /// Purpose: create violation object
    /// </summary>
    /// <param name="rule">section of violation</param>
    /// <param name="eventDateTime">duty status datetime </param>
    /// <param name="duration">duration of duty status</param>
    /// <param name="totalElapsedTime">total onduty time elapsed</param>
    /// <param name="violationFg">pass if true if want to add violation</param>
    /// <returns>returns true if violation occurs</returns>
    public Boolean AddViolation(String rule, Date eventDateTime, double duration, double totalElapsedTime, boolean violationFg) {
        // get violation info by rule e.g 12A,12B ... etc
        ViolationModel violationInfo = Utility.violationInfoGet(rule);

        // if work shift driving exceeds it's maximum allowed limit.
        if (violationFg) {
            // get violation start time
            violationInfo.setViolationDate(getViolationStartTime(eventDateTime, duration, totalElapsedTime, violationInfo.getThreshold()));
            if (!(violationInfo.getViolationDate().before(fromDate) && violationInfo.getViolationDate().after(toDate))) {

                // check which rule exists at the time of violation occurs
                int ruleId = RuleGet(violationInfo.getViolationDate());
                if (ruleId == 5) {

                    // it's date when we added a virtual driving event to show purposed violation to driver
                    // if he drives vehicle
                    Date futureDate = TimeUtility.addSeconds(toDate, -futureTime * 60);
                    violationInfo.setVirtualFg(futureDate.equals(violationInfo.getViolationDate()));

                    // add violation to list
                    _violationList.add(violationInfo);
                }
            }
            violationFg = true;
        }
        return violationFg;
    }


    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 20 August 2018
    /// Purpose: get violation start time
    /// </summary>
    /// <param name="eventDateTime">Time of event</param>
    /// <param name="duration">duration of event</param>
    /// <param name="totalElapsedTime">Total time elapsed after Reset</param>
    /// <param name="Threshold">Maximum limit allowed as per Rule</param>
    /// <returns>Violation start time</returns>
    public Date getViolationStartTime(Date eventDateTime, double duration, double totalElapsedTime, double Threshold) {

        // these are the hour left of driving when event added
        // if these hours have positive value it means driver is having few are left when he started driving
        // else he has not got any hour for driving
        double hourLeft = Threshold - (totalElapsedTime - duration);

        // get violation start time
        // hourLeft means driver has hour left for work shift driving before violation has occurred
        // if hourLeft is 0 it means driver is not allowed to driver any more
        // if driver driver inspite of it then every time he drivers will be violation at that time
        // Date violationStartTime = eventDateTime.AddSeconds(hourLeft > 0 ? hourLeft : 0);
        Date violationStartTime = TimeUtility.addSeconds(eventDateTime, (int) (hourLeft > 0 ? hourLeft : 0));
        return violationStartTime;
    }


    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 04 November 2018
    /// Purpose: get rule at the time of violation occurred
    /// </summary>
    /// <param name="violationDate"></param>
    /// <returns></returns>
    private int RuleGet(Date violationDate) {
        int ruleId = currentRule;
        for (int i = ruleList.size() - 1; i >= 0; i--) {
            DutyStatus rule = ruleList.get(i);
            if (rule.getEventDateTime().before(violationDate)) {
                ruleId = rule.getRule();
                break;
            }
        }

        return ruleId;
    }

}



