package com.hutchsystems.hutchconnect.beans;

import java.util.Comparator;
import java.util.Date;

public class RecapBean {
    private Date date;
    private int driving,onDuty;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDriving() {
        return driving;
    }

    public void setDriving(int driving) {
        this.driving = driving;
    }

    public int getOnDuty() {
        return onDuty;
    }

    public void setOnDuty(int onDuty) {
        this.onDuty = onDuty;
    }


    /*Comparator for sorting the list by dateDesc*/
    public static Comparator<RecapBean> dateDesc = new Comparator<RecapBean>() {
        public int compare(RecapBean s1, RecapBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getDate().getTime() / (1000));
                date2 = (int) (s2.getDate().getTime() / (1000));

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };
}
