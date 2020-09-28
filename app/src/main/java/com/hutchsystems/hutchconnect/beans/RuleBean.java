package com.hutchsystems.hutchconnect.beans;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by Dev-1 on 5/26/2016.
 */
public class RuleBean {
    private int ruleId, driverId, dailyLogId;
    private Date ruleStartTime;
    private Date ruleEndTime;
    private String startDate, endDate;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getDailyLogId() {
        return dailyLogId;
    }

    public void setDailyLogId(int dailyLogId) {
        this.dailyLogId = dailyLogId;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public Date getRuleStartTime() {
        return ruleStartTime;
    }

    public void setRuleStartTime(Date ruleStartDate) {
        this.ruleStartTime = ruleStartDate;
    }

    public Date getRuleEndTime() {
        return ruleEndTime;
    }

    public void setRuleEndTime(Date ruleEndTime) {
        this.ruleEndTime = ruleEndTime;
    }

    /*Comparator for sorting the list by dateAsc*/
    public static Comparator<RuleBean> dateAsc = new Comparator<RuleBean>() {
        public int compare(RuleBean s1, RuleBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getRuleStartTime().getTime() / 1000 * 60);
                date2 = (int) (s2.getRuleStartTime().getTime() / 1000 * 60);

            } catch (Exception exe) {
            }

            return date1 - date2;
        }
    };

    /*Comparator for sorting the list by dateDesc*/
    public static Comparator<RuleBean> dateDesc = new Comparator<RuleBean>() {
        public int compare(RuleBean s1, RuleBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getRuleStartTime().getTime() / 1000 * 60);
                date2 = (int) (s2.getRuleStartTime().getTime() / 1000 * 60);

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };
}
