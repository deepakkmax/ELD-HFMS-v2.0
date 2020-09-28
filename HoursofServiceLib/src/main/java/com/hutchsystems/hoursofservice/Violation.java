package com.hutchsystems.hoursofservice;

import com.hutchsystems.hoursofservice.Repository.DurationRepository;
import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.common.Utility;
import com.hutchsystems.hoursofservice.model.DutyStatus;
import com.hutchsystems.hoursofservice.model.ViolationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class Violation extends ViolationModel {

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
    DurationRepository objCanada, objUS;
    int currentRule = 1;
    //   #endregion

    //   #region Public constructor

    // Parameterized constructor
    public Violation(Date fromDate, Date toDate, ArrayList<DutyStatus> list, ArrayList<DutyStatus> ruleList, int futureTime, int currentRule, int newSplitSleep) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        //this.list = list;
        this.ruleList = ruleList;

        copyList(list);
        this.futureTime = futureTime;

        // initialize object to keep duration in canada
        objCanada = new DurationRepository();
        objCanada.canadaFg = true;
        objCanada.OnDutyList.add(0d);
        objCanada.newSplitSleep = newSplitSleep;
        this.currentRule = currentRule;

        // last date when 24 hour break was taken default value is from date
        objCanada.LastBreak24Hours = fromDate;

        // initialize object to keep duration in US
        objUS = new DurationRepository();
        objUS.newSplitSleep = newSplitSleep;
        objUS.OnDutyList.add(0d);

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
    public DurationRepository HoursGet(boolean canadaFg) {
        return canadaFg ? objCanada : objUS;
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
            futureEvent.setFutureFg(true);
            list.add(futureEvent);
        }

        Collections.sort(list, DutyStatus.dateAsc);

        // to keep track if 14B is calculated or not
        boolean rule14BFg = false;

        int canadianRule = 1;
        int usRule = 3;

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

            // Date of duty status
            Date eventDate = item.getEventDateTime();

            // pick next Duty Status if exists
            nextItem = i == list.size() - 1 ? null : list.get(i + 1);

            int ruleId = EventRuleGet(eventDate);

            // get rule of us and canada used by driver
            if (ruleId <= 2) {
                canadianRule = ruleId;
            } else {
                usRule = ruleId;
            }

            objCanada.RuleId = ruleId;
            objUS.RuleId = ruleId;

            // Date of next duty status if it exists else put to Date in next date
            Date nextDate = nextItem == null ? toDate : nextItem.getEventDateTime();
            int nextStatus = nextItem == null ? 0 : nextItem.getStatus();

            // to check if next date and event date fall in different day
            int diff = (int) TimeUtility.getDiff(Utility.dateOnly(eventDate), Utility.dateOnly(nextDate), TimeUtility.Unit.DAY);

            // Duration of duty status
            //  double duration = nextDate.Subtract(eventDate).TotalSeconds;
            double duration = TimeUtility.getDiff(eventDate, nextDate, TimeUtility.Unit.SECONDS);

            if (!rule14BFg && item.getDeferedDay() == 0) {

                Violation14B(list, i, duration);
                rule14BFg = true;
            }

            if (diff == 0) {

                item.setNextStatus(nextStatus);
                /*if (!rule14BFg) {

                    Violation14B(list, i, duration);
                    rule14BFg = true;
                }*/
                // Calculate violation
                ProcessViolations(item, duration);

                // calculate day violations
                DayViolationCalculate(item, duration);

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
                    newItem.setNextStatus(j == diff ? nextStatus : item.getStatus());
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

                    // calculate day violations
                    DayViolationCalculate(newItem, duration);

                    if (j < diff) {


                        // check if 14C rule is violated for canada rule
                        if (j > 0 && newItem.getStatus() >= 3 && item.getRule() <= 2 && item.getDeferedDay() != 1) {
                            double violationDuration = 2 * 60 * 60d;
                            Date violationStartTime = TimeUtility.addSeconds(newNextDate, (int) (-violationDuration));

                            // violation duration is when violation start untill user change non driving status
                            AddViolation("14C", violationStartTime, violationDuration);
                        }

                        // only reset our if it's not future event and next date is on or before current date
                        if (!item.isFutureFg() && !Utility.dateOnly(nextDate).after(Utility.dateOnly(futureEvent.getEventDateTime()))) {
                            if (objCanada.OnDutyList.size() == (canadianRule == 2 ? 14 : 7))
                                objCanada.OnDutyList.remove(0);

                            // add item in ondutylist
                            objCanada.OnDutyList.add(0d);

                            if (objUS.OnDutyList.size() == (usRule == 3 ? 8 : 7))
                                objUS.OnDutyList.remove(0);

                            // add item in ondutylist
                            objUS.OnDutyList.add(0d);

                            // off duty hours for canadian rule
                            double offDuty = objCanada.offDuty;

                            // driving for canadian rule
                            double driving = objCanada.driving;

                            // reset day's duty status time for canada rule
                            objCanada.ResetDailyTime();

                            // reset day's duty status time for us rule
                            objUS.ResetDailyTime();

                            // if driver is deferring off duty. then retain driving and off duty of day for 2 days.
                            if (item.getDeferedDay() > 0) {
                                objCanada.offDuty = offDuty;
                                objCanada.driving = driving;
                            }
                        }
                    }
                }
                rule14BFg = false;
            }
        }


        Collections.sort(_violationList, Violation.dateAsc);
        return _violationList;
    }

    private void Violation14B(List<DutyStatus> list, int row, double duration) {
        try {
            // add previous offduty/sleeper berth time to newReset to check if its greater than 8 hours or not
            double resetTime = objCanada.ResetTime;

            // get end of day time
            //Date dateEnd = list[row].EventDateTime.AddDays(1);
            Date dateEnd = TimeUtility.addDays(Utility.dateOnly(list.get(row).getEventDateTime()), 1);

            while (row < list.size()) {
                DutyStatus item = list.get(row);
                // Date of duty status
                Date eventDate = item.getEventDateTime();

                // if next duty status is same then increase value of i till we get differnt next duty status.
                while (item.getStatus() <= 2 && row < list.size() - 1 && list.get(row + 1).getStatus() <= 2) {
                    // double newDuration = list[row + 1].EventDateTime.Subtract(eventDate).TotalSeconds;
                    double newDuration = TimeUtility.getDiff(eventDate, list.get(row + 1).getEventDateTime(), TimeUtility.Unit.SECONDS);
                    row++;
                    if (newDuration > 10 * 60 * 60d) break;
                }

                // pick next Duty Status if exists
                DutyStatus nextItem = row == list.size() - 1 ? null : list.get(row + 1);

                // Date of next duty status if it exists else put to Date in next date
                Date nextDate = nextItem == null ? toDate : nextItem.getEventDateTime();

                // to check if next date and event date fall in different day

                //  double diff = nextDate.Date.Subtract(eventDate.Date).TotalDay;
                double diff = TimeUtility.getDiff(Utility.dateOnly(eventDate), Utility.dateOnly(nextDate), TimeUtility.Unit.DAY);
                // Duration of duty status
                // duration = nextDate.Subtract(eventDate).TotalSeconds;
                duration = TimeUtility.getDiff(eventDate, nextDate, TimeUtility.Unit.SECONDS);

                if (item.getStatus() <= 2) {
                    resetTime += duration;
                }

                if (resetTime >= 8 * 60 * 60d) {
                    // off duty time excluding mandatory 8 hours off duty time
                    objCanada.OffDutyExcluding8Hours += resetTime - 8 * 60 * 60d;
                } else if (resetTime >= 30 * 60d) {
                    objCanada.OffDutyExcluding8Hours += resetTime;
                }

                if (resetTime > 2)
                    resetTime = 0;

                if (diff > 0 || objCanada.OffDutyExcluding8Hours >= 2 * 60 * 60d) {
                    break;
                }
                row++;
            }


            // Day off duty limit that is not part of 8 mandatory off duty and more then 30 minute. driver has to take minimum 2 hours of off duty in a day
            double DayOffDutyExcluding8HoursLimit = 2 * 60 * 60d;
            // add violation once in a day. If required off duty time is less then 2 hours then check for violation
            if (objCanada.OffDutyExcluding8Hours < DayOffDutyExcluding8HoursLimit) {
                // get remaing off duty time to reach minimum off duty limit
                double remainingOffDuty = DayOffDutyExcluding8HoursLimit - objCanada.OffDutyExcluding8Hours;

                // violation start time
                // Date violationStartTime = dateEnd.AddSeconds(-remainingOffDuty);
                Date violationStartTime = TimeUtility.addSeconds(dateEnd, (int) -remainingOffDuty);

                AddViolation("14C", violationStartTime, remainingOffDuty);
            }
        } catch (Exception exe) {
        }
    }


    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 21 August 2018
    /// Purpose: process and add day violation. This method is only applicable when driver is in Canada Rule
    /// </summary>
    /// <param name="eventDateTime">time of duty status</param>
    /// <param name="deferedDay">defer day is 0 if driver is deferring his off duyt</param>
    /// <param name="status">Duty status</param>
    /// <param name="duration">duration of duty status</param>
    private void DayViolationCalculate(DutyStatus item, double duration) {
        if (item.getRule() > 2) {
            return;
        }

        Date eventDateTime = item.getEventDateTime();
        int deferedDay = item.getDeferedDay();
        int status = item.getStatus();
        if (item.getStatus() == 3) {

            // check and add violation belongs to 12A or 16D section
            AddViolation(deferedDay == 0 ? "12A" : "16D", eventDateTime, duration, objCanada.driving);

            // check and add violation belongs to 12B section if defered then no violation of this rule
            AddViolation(deferedDay == 0 ? "12B" : "", eventDateTime, duration, objCanada.onDuty);
        }


        // Day off duty limit. driver has to take minimum 10 hours of off duty in a day
        // if driver is deferring his at most 2 hours of off duty then he has to take 20 hours of off duty in 2 consecutive days i.e day 1 and day 2
        double DayOffDutyLimit = (deferedDay == 0 ? 10 : 20) * 60 * 60d;

        // This violation would occurs once in a day. we set offduty -1 if it occurs
        // if reuqired off duty time is less then minimum off duty required as per rule then violation occurs
        if (objCanada.offDuty != -1 && objCanada.offDuty < DayOffDutyLimit) {
            // if its day 1 then dateEnd is end of next day else end of date when event is added
            //  Date dateEnd = eventDateTime.Date.AddDays(deferedDay == 1 ? 2 : 1);
            Date dateEnd = TimeUtility.addDays(Utility.dateOnly(eventDateTime), deferedDay == 1 ? 2 : 1);

            // get time remaining before day end
            //double remainingDayTime = dateEnd.Subtract(eventDateTime).TotalSeconds - (status >= 3 ? duration : 0);
            double remainingDayTime = TimeUtility.getDiff(eventDateTime, dateEnd, TimeUtility.Unit.SECONDS) - (status >= 3 ? duration : 0);

            // remaing off duty time to reach minimum off duty required as per rule
            double remainingOffDuty = DayOffDutyLimit - objCanada.offDuty;

            // if remaining Day time is less then remaining off duty required then violation occurs
            if (remainingDayTime < remainingOffDuty) {
                // violation start would be the time when driver should start taking off duty time to avoid violation
                // subtract remaing off duty time from end date time
                // Date violationStartTime = eventDateTime.Date.AddDays(1).AddSeconds(-remainingOffDuty);
                Date violationStartTime = TimeUtility.addSeconds(TimeUtility.addDays(Utility.dateOnly(eventDateTime), 1), (int) -remainingOffDuty);

                // if driver defers off duty then add 14A rule violation else 16B
                AddViolation((deferedDay == 0 ? "14A" : "16B"), violationStartTime, remainingOffDuty);

                // once violation occurs then set off duty to -1 so that we can avoid same violation to be added in same day
                objCanada.offDuty = -1;
            }
        }
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

        // udpate time of duty status as per rule of US
        objUS.Update(item, duration);

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
        AddViolation("13A", item.getEventDateTime(), duration, objCanada.workShiftDriving);

        // check and add violation belongs to 13B section
        AddViolation("13B", item.getEventDateTime(), duration, objCanada.workShiftOnDuty);

        // check and add violation belongs to 13C section
        AddViolation("13C", item.getEventDateTime(), duration, objCanada.totalWorkshift);

        // driver has to take consective 24 hour break in last 24 hours
        //if (item.eventDateTime.Subtract(objCanada.LastBreak24Hours).TotalDays > 14)
        if (TimeUtility.getDiff(objCanada.LastBreak24Hours, item.getEventDateTime(), TimeUtility.Unit.DAY) > 14) {

            // next event to calculate duration of violation
            Date nextEventDate = TimeUtility.addSeconds(item.getEventDateTime(), (int) duration);

            // violation duration is when violation start untill user change non driving status
            double violationDuration = TimeUtility.getDiff(item.getEventDateTime(), nextEventDate, TimeUtility.Unit.SECONDS);

            // check and add violation belongs to rule 25
            AddViolation("25", item.getEventDateTime(), violationDuration);
        }

        if (item.getRule() == 1) {
            // check and add violation belongs to 26 section
            AddViolation("26", item.getEventDateTime(), duration, objCanada.getRuleOnDuty());

        } else {
            // check and add violation belongs to 27A section
            AddViolation("27A", item.getEventDateTime(), duration, objCanada.onDutyWithoutBreak);

            // check and add violation belongs to 27B section
            AddViolation("27B", item.getEventDateTime(), duration, objCanada.getRuleOnDuty());

        }
       /* } else // These are the rule when driver drives in United States of America
        {*/

        // check and add violation belongs to Take 30 min Rest section
        AddViolation("Take 30 min Rest", item.getEventDateTime(), duration, objUS.drivingWithoutBreak);

        // check and add violation belongs to 11 Hrs 395.3(a)(3)(i) ***EGREGIOUS*** section
        // if egregious driving does not exists then we'll check for 11 Hrs 395.3(a)(3)(i) section
        Boolean violationFg = AddViolation("11 Hrs 395.3(a)(3)(i) ***EGREGIOUS***", item.getEventDateTime(), duration, objUS.workShiftDriving);

        // these are the hour left before violation occur when driver starts driving
        // there is 13 hours of limit untill egregious violation has occured
        double hourLeft = 13 * 60 * 60d - (objUS.workShiftDriving - duration);
        // check this violation if either Egregious violation has not occured or if Egregious violation has occurred But hour left are greater than or equal to 2
        if (!violationFg || (violationFg && hourLeft >= 2 * 20 * 60d)) {
            // check and add violation belongs to 11 Hrs 395.3(a)(3)(i) section
            AddViolation("11 Hrs 395.3(a)(3)(i)", item.getEventDateTime(), duration, objUS.workShiftDriving);
        }

        // check and add violation belongs to 14 Hrs 395.3(a)(2) section
        AddViolation("14 Hrs 395.3(a)(2)", item.getEventDateTime(), duration, objUS.totalWorkshift);

        // check and add violation belongs to 14 Hrs 395.3(a)(2) section
        //AddViolation(item.getRule() == 3 ? "70 Hrs 395.3(b)(2)" : "60 Hrs 395.3(b)(1)", item.getEventDateTime(), duration, objUS.getRuleOnDuty());
        AddViolation("70 Hrs 395.3(b)(2)", item.getEventDateTime(), duration, objUS.getRuleOnDuty());

        //  }
    }

    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 21 August 2018
    /// Purpose: create violation object
    /// </summary>
    /// <param name="rule">section of violation</param>
    /// <param name="violationDate">violation date</param>
    public void AddViolation(String rule, Date violationDate, double violationDuration) {
        // get violation info by rule e.g 12A,12B ... etc
        ViolationModel violationInfo = Utility.violationInfoGet(rule);

        // get violation start time
        violationInfo.setViolationDate(violationDate);

        boolean canadaFg = RuleGet(violationDate);

        // we show next day violation
        if (violationInfo.getCanadaFg() == canadaFg && !(violationInfo.getViolationDate().before(fromDate))) {
            // it's date when we added a virtual driving event to show purposed violation to driver
            // if he drives vehicle
            Date futureDate = TimeUtility.addSeconds(toDate, -futureTime * 60);
            violationInfo.setVirtualFg(futureDate.equals(violationInfo.getViolationDate()));

            violationInfo.setViolationDuration(violationDuration);

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
                boolean canadaFg = RuleGet(violationInfo.getViolationDate());
                if (violationInfo.getCanadaFg() == canadaFg) {

                    // it's date when we added a virtual driving event to show purposed violation to driver
                    // if he drives vehicle
                    Date futureDate = TimeUtility.addSeconds(toDate, -futureTime * 60);
                    violationInfo.setVirtualFg(futureDate.equals(violationInfo.getViolationDate()));

                    // next event to calculate duration of violation
                    Date nextEventDate = TimeUtility.addSeconds(eventDateTime, (int) duration);

                    // violation duration is when violation start untill user change non driving status
                    double violationDuration = TimeUtility.getDiff(violationInfo.getViolationDate(), nextEventDate, TimeUtility.Unit.SECONDS);

                    violationInfo.setViolationDuration(violationDuration);

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
    private boolean RuleGet(Date violationDate) {
        int ruleId = currentRule;

        for (int i = ruleList.size() - 1; i >= 0; i--) {
            DutyStatus rule = ruleList.get(i);
            if (rule.getEventDateTime().before(violationDate)) {
                ruleId = rule.getRule();
                break;
            }
        }

        return ruleId <= 2;
    }


    /// <summary>
    /// Created By: Deepak Sharma
    /// Created Date: 25 September 2019
    /// Purpose: get rule at the time of event
    /// </summary>
    /// <param name="eventdate"></param>
    /// <returns></returns>
    private int EventRuleGet(Date eventDate) {
        int ruleId = currentRule;
        for (int i = ruleList.size() - 1; i >= 0; i--) {
            DutyStatus rule = ruleList.get(i);
            if (rule.getEventDateTime().before(eventDate)) {
                ruleId = rule.getRule();
                break;
            }
        }

        return ruleId;
    }

}



