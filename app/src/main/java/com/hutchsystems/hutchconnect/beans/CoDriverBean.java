package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 4/9/2019.
 */

public class CoDriverBean {
    int driverId, driverId2;
    String StartDate, EndDate;

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getDriverId2() {
        return driverId2;
    }

    public void setDriverId2(int driverId2) {
        this.driverId2 = driverId2;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getEndDate() {
        return EndDate;
    }

    public void setEndDate(String endDate) {
        EndDate = endDate;
    }
}
