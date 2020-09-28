package com.hutchsystems.hoursofservice.model;

import java.util.Comparator;
import java.util.Date;

public class ViolationModel {

    // date and time when violation occurred
     Date violationDate;

    // section of violated rule
    String rule ;

    // Title of the violation
    String title ;

    // Description of violation
    String description ;

    // Is violation belongs to US or Canada
     Boolean canadaFg;

    // Maximum limit of duration in respective rule
    double threshold, violationDuration;

     String driverName ;

     // True means, driver would be in violation if he starts driving.
     boolean virtualFg;

     int ruleId;

    public double getViolationDuration() {
        return violationDuration;
    }

    public void setViolationDuration(double violationDuration) {
        this.violationDuration = violationDuration;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public boolean isVirtualFg() {
        return virtualFg;
    }

    public void setVirtualFg(boolean virtualFg) {
        this.virtualFg = virtualFg;
    }

    public Date getViolationDate() {
        return violationDate;
    }

    public void setViolationDate(Date violationDate) {
        this.violationDate = violationDate;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCanadaFg() {
        return canadaFg;
    }

    public void setCanadaFg(Boolean canadaFg) {
        this.canadaFg = canadaFg;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }


    /*Comparator for sorting the list by dateAsc*/
    public static Comparator<ViolationModel> dateAsc = new Comparator<ViolationModel>() {
        public int compare(ViolationModel s1, ViolationModel s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getViolationDate().getTime() / 1000 * 60);
                date2 = (int) (s2.getViolationDate().getTime() / 1000 * 60);

            } catch (Exception exe) {
            }

            return date1 - date2;
        }
    };


    /*Comparator for sorting the list by dateAsc*/
    public static Comparator<ViolationModel> dateDesc = new Comparator<ViolationModel>() {
        public int compare(ViolationModel s1, ViolationModel s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getViolationDate().getTime() / 1000 * 60);
                date2 = (int) (s2.getViolationDate().getTime() / 1000 * 60);

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };
}
