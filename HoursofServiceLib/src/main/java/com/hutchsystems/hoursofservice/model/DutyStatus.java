package com.hutchsystems.hoursofservice.model;

import java.util.Comparator;
import java.util.Date;

public class DutyStatus {
    public DutyStatus() {

    }

    public DutyStatus(DutyStatus item) {
        this.eventDateTime = item.eventDateTime;
        this.status = item.status;
        this.rule = item.rule;
        this.deferedDay = item.deferedDay;
        this.coDriverId = item.coDriverId;
        this.splitFg = item.splitFg;
        this.personalUseFg = item.personalUseFg;
        this.futureFg = item.futureFg;
    }

    Date eventDateTime;
    int status;
    int rule;
    int deferedDay;
    int coDriverId;
    Boolean splitFg;
    String driverName;
    int personalUseFg;
    boolean reducedFg, waitingFg;
    boolean futureFg;
    int eventId;
    int nextStatus;

    public int getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(int nextStatus) {
        this.nextStatus = nextStatus;
    }

    public boolean isWaitingFg() {
        return waitingFg;
    }

    public void setWaitingFg(boolean waitingFg) {
        this.waitingFg = waitingFg;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public boolean isReducedFg() {
        return reducedFg;
    }

    public void setReducedFg(boolean reducedFg) {
        this.reducedFg = reducedFg;
    }

    public boolean isFutureFg() {
        return futureFg;
    }

    public void setFutureFg(boolean futureFg) {
        this.futureFg = futureFg;
    }

    public Date getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    public int getDeferedDay() {
        return deferedDay;
    }

    public void setDeferedDay(int deferedDay) {
        this.deferedDay = deferedDay;
    }

    public int getCoDriverId() {
        return coDriverId;
    }

    public void setCoDriverId(int coDriverId) {
        this.coDriverId = coDriverId;
    }

    public Boolean getSplitFg() {
        return splitFg;
    }

    public void setSplitFg(Boolean splitFg) {
        this.splitFg = splitFg;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public int getPersonalUseFg() {
        return personalUseFg;
    }

    public void setPersonalUseFg(int personalUseFg) {
        this.personalUseFg = personalUseFg;
    }

    /*Comparator for sorting the list by dateAsc*/
    public static Comparator<DutyStatus> dateAsc = new Comparator<DutyStatus>() {
        public int compare(DutyStatus s1, DutyStatus s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getEventDateTime().getTime() / (1000));
                date2 = (int) (s2.getEventDateTime().getTime() / (1000));

            } catch (Exception exe) {
            }

            return date1 - date2;
        }
    };

    /*Comparator for sorting the list by dateDesc*/
    public static Comparator<DutyStatus> dateDesc = new Comparator<DutyStatus>() {
        public int compare(DutyStatus s1, DutyStatus s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getEventDateTime().getTime() / (1000));
                date2 = (int) (s2.getEventDateTime().getTime() / (1000));

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };
}