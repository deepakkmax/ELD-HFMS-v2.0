package com.hutchsystems.hoursofservice.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DurationModel {

    public int newSplitSleep = 0;

    // to know which status is right now in loop
    public int currentStatus = 1;

    // is duration belongs to Canada or US
    // if value is true then duration object belong to canada
    public Boolean canadaFg = false;

    // its offduty time of each day. we add off duty and sleeper berth time in this variable
    public double offDuty = 0d;

    // This property holds Off duty time which does not form part of 8 mandatory hours of off duty
    // This is used for canadian rule 14B
    public double OffDutyExcluding8Hours = 0d;

    // Its driving time of each day
    public double driving = 0d;

    // It holds driving duration till driver takes 30 minute of Offduty or sleeper berth
    public double drivingWithoutBreak = 0d;

    // its On Duty time of respective day
    public double onDuty = 0d;

    // Its OnDuty time taken by driver without taking 24 hours of break
    // if cycle 2 get 24 hour of break after 70 hour of driving
    public double onDutyWithoutBreak = 0d;

    // Its Total On Duty time of respective day
    public double totalOnDuty = 0d;

    // This list contains On Duty time of each day
    // every item have Total OnDuty time of respective days
    // Total Number of item would be as per rule follow by driver
    public List<Double> OnDutyList = new ArrayList<>();


    // This list contains On Duty time of each day
    // every item have Total Driving time of respective days
    // Total Number of item would be as per rule follow by driver
    public List<Double> DrivingList = new ArrayList<>();

    // Its get sum of all items on OnDutyList
    public double getRuleOnDuty() {

        double sum = 0d;
        for (int i = 0; i < OnDutyList.size(); i++) {
            sum += OnDutyList.get(i);
        }
        return sum;
    }


    // Its get sum of all items on DrivingList
    public double getRuleDriving() {

        double sum = 0d;
        for (int i = 0; i < DrivingList.size(); i++) {
            sum += DrivingList.get(i);
        }
        return sum;
    }

    // driving time after workshift reset
    public double workShiftDriving;

    // OnDuty Time after workshift reset. We add both driving and on duty duration in this property
    public double workShiftOnDuty;

    // Its total elapsed time after WorkShift Reset. We add all statuses time
    // Work Shift Reset while driver take 8 Mandatory off duty hours in Canada and 10 Hours in US
    public double totalWorkshift;

    // properties to save split time
    // set it true once find first Split Sleep
    public Boolean splitFg = false;

    // once driver choose split we will set this true. as driver may take multiple slots of offduty and sleeper
    // to make hours eligible for split sleep
    public Boolean markSplitFg = false;

    // set it true if find first reduced event
    public Boolean reduceFg = false;

    public double ReducedTime = 0d;

    // Duration of Sleeper berth in Split Sleep
    public double splitSleeper = 0d;

    // Driving hours before split sleep
    public double splitDriving = 0d;

    // On Duty hours before Split Sleep
    public double splitOnDuty = 0d;

    // Work shift hours before Split Sleep
    public double splitWorkShift = 0d;

    // date when driver takes 24 hours of consective off duty
    public Date LastBreak24Hours;

    // Reset Time is the consective off duty
    public double ResetTime;

    // reset time for 30 min break for US
    public double Reset30MinBreak=0;

    // Consective sleeper berth time
    public double SleeperConsective;

    public double NonDrivingConsecutive;


    // Oil well service vehicle hours
    // to check if driver has 3 period of off duty time of at least 24 hours
    public double totalOffDuty24Days = 0;

    //  to save rule when event is created
    public int RuleId = 1;
}