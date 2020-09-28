package com.hutchsystems.hutchconnect.beans;

import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Deepak.Sharma on 7/20/2015.
 */
public class DailyLogBean {
    int _id, OnlineDailyLogId, DriverId, CertifyFG, CreatedBy, ModifiedBy, StatusId, SyncFg;
    String LogDate;
    String ShippingId;
    String TrailerId;
    String Commodity;
    String StartTime;
    String StartOdometerReading;
    String EndOdometerReading;
    String CreatedDate;
    String ModifiedDate;

    int TotalDistance, PCDistance, DrivingTimeRemaining, WorkShiftTimeRemaining, TimeRemaining70, TimeRemaining120, TimeRemainingUS70, TimeRemainingReset;
    private int deferedDay=0; // default value is 0 For First Day 1 and For second Day 2


    public int getTotalDistance() {
        return TotalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        TotalDistance = totalDistance;
    }

    public int getPCDistance() {
        return PCDistance;
    }

    public void setPCDistance(int PCDistance) {
        this.PCDistance = PCDistance;
    }

    public int getDrivingTimeRemaining() {
        return DrivingTimeRemaining;
    }

    public void setDrivingTimeRemaining(int drivingTimeRemaining) {
        DrivingTimeRemaining = drivingTimeRemaining;
    }

    public int getWorkShiftTimeRemaining() {
        return WorkShiftTimeRemaining;
    }

    public void setWorkShiftTimeRemaining(int workShiftTimeRemaining) {
        WorkShiftTimeRemaining = workShiftTimeRemaining;
    }

    public int getTimeRemaining70() {
        return TimeRemaining70;
    }

    public void setTimeRemaining70(int timeRemaining70) {
        TimeRemaining70 = timeRemaining70;
    }

    public int getTimeRemaining120() {
        return TimeRemaining120;
    }

    public void setTimeRemaining120(int timeRemaining120) {
        TimeRemaining120 = timeRemaining120;
    }

    public int getTimeRemainingUS70() {
        return TimeRemainingUS70;
    }

    public void setTimeRemainingUS70(int timeRemainingUS70) {
        TimeRemainingUS70 = timeRemainingUS70;
    }

    public int getTimeRemainingReset() {
        return TimeRemainingReset;
    }

    public void setTimeRemainingReset(int timeRemainingReset) {
        TimeRemainingReset = timeRemainingReset;
    }

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    String Signature;


    public ArrayList<EventBean> getEventList() {
        return eventList;
    }

    public void setEventList(ArrayList<EventBean> eventList) {
        this.eventList = eventList;
    }

    ArrayList<EventBean> eventList;

    public int getCertifyCount() {
        return certifyCount;
    }

    public void setCertifyCount(int certifyCount) {
        this.certifyCount = certifyCount;
    }

    int certifyCount;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getOnlineDailyLogId() {
        return OnlineDailyLogId;
    }

    public void setOnlineDailyLogId(int onlineDailyLogId) {
        OnlineDailyLogId = onlineDailyLogId;
    }

    public int getDriverId() {
        return DriverId;
    }

    public void setDriverId(int driverId) {
        DriverId = driverId;
    }

    public int getCertifyFG() {
        return CertifyFG;
    }

    public void setCertifyFG(int certifyFG) {
        CertifyFG = certifyFG;
    }

    public int getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(int createdBy) {
        CreatedBy = createdBy;
    }

    public int getModifiedBy() {
        return ModifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        ModifiedBy = modifiedBy;
    }

    public int getStatusId() {
        return StatusId;
    }

    public void setStatusId(int statusId) {
        StatusId = statusId;
    }

    public int getSyncFg() {
        return SyncFg;
    }

    public void setSyncFg(int syncFg) {
        SyncFg = syncFg;
    }

    public String getLogDate() {
        return LogDate;
    }

    public void setLogDate(String logDate) {
        LogDate = logDate;
    }

    public String getShippingId() {
        return ShippingId;
    }

    public void setShippingId(String shippingId) {
        ShippingId = shippingId;
    }

    public String getTrailerId() {
        return TrailerId;
    }

    public void setTrailerId(String trailerId) {
        TrailerId = trailerId;
    }

    public String getCommodity() {
        return Commodity;
    }

    public void setCommodity(String commodity) {
        Commodity = commodity;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getStartOdometerReading() {
        return StartOdometerReading;
    }

    public void setStartOdometerReading(String startOdometerReading) {
        StartOdometerReading = startOdometerReading;
    }

    public String getEndOdometerReading() {
        return EndOdometerReading;
    }

    public void setEndOdometerReading(String endOdometerReading) {
        EndOdometerReading = endOdometerReading;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public String getModifiedDate() {
        return ModifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        ModifiedDate = modifiedDate;
    }

    public int getDeferedDay() {
        return deferedDay;
    }

    public void setDeferedDay(int deferedDay) {
        this.deferedDay = deferedDay;
    }

    /*Comparator for sorting the list by dateDesc*/
    public static Comparator<DailyLogBean> dateDesc = new Comparator<DailyLogBean>() {
        public int compare(DailyLogBean s1, DailyLogBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (Utility.parse(s1.getLogDate()).getTime() / (1000));
                date2 = (int) (Utility.parse(s2.getLogDate()).getTime() / (1000));

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };
}
