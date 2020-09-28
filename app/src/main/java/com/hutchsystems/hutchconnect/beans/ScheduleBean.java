package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 4/20/2017.
 */

public class ScheduleBean {
    int ScheduleId, Threshold, effectiveOn, dueOn, repairFG, StatusId, dueStatus, SyncFG;
    String Schedule, EffectiveDate, Unit;
    boolean checked;

    int _id;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getScheduleId() {
        return ScheduleId;
    }

    public void setScheduleId(int scheduleId) {
        ScheduleId = scheduleId;
    }

    public int getThreshold() {
        return Threshold;
    }

    public void setThreshold(int threshold) {
        Threshold = threshold;
    }

    public int getStatusId() {
        return StatusId;
    }

    public void setStatusId(int statusId) {
        StatusId = statusId;
    }

    public int getSyncFG() {
        return SyncFG;
    }

    public void setSyncFG(int syncFG) {
        SyncFG = syncFG;
    }

    public String getSchedule() {
        return Schedule;
    }

    public void setSchedule(String schedule) {
        Schedule = schedule;
    }

    public String getEffectiveDate() {
        return EffectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        EffectiveDate = effectiveDate;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public int getEffectiveOn() {
        return effectiveOn;
    }

    public void setEffectiveOn(int effectiveOn) {
        this.effectiveOn = effectiveOn;
    }

    public int getDueOn() {
        return dueOn;
    }

    public void setDueOn(int dueOn) {
        this.dueOn = dueOn;
    }

    public int getRepairFG() {
        return repairFG;
    }

    public void setRepairFG(int repairFG) {
        this.repairFG = repairFG;
    }

    public int getDueStatus() {
        return dueStatus;
    }

    public void setDueStatus(int dueStatus) {
        this.dueStatus = dueStatus;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
