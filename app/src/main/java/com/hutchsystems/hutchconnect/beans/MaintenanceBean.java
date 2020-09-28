package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 2/3/2017.
 */

public class MaintenanceBean {
    int alertId, scheduleId, repairFg, maintenanceId;
    String dueKM, dueMiles, dueEngineHour, dueDate, scheduleName;

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getRepairFg() {
        return repairFg;
    }

    public void setRepairFg(int repairFg) {
        this.repairFg = repairFg;
    }

    public int getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(int maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public String getDueKM() {
        return dueKM;
    }

    public void setDueKM(String dueKM) {
        this.dueKM = dueKM;
    }

    public String getDueMiles() {
        return dueMiles;
    }

    public void setDueMiles(String dueMiles) {
        this.dueMiles = dueMiles;
    }

    public String getDueEngineHour() {
        return dueEngineHour;
    }

    public void setDueEngineHour(String dueEngineHour) {
        this.dueEngineHour = dueEngineHour;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }
}
