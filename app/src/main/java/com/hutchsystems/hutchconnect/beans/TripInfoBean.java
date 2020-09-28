package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 4/24/2018.
 */

public class TripInfoBean {
    private int vehicleId, startOdometerReading, endOdometerReading;
    private String unitNo, plateNo,VIN, odoUnit, startEngineHour, endEngineHour;

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getStartOdometerReading() {
        return startOdometerReading;
    }

    public void setStartOdometerReading(int startOdometerReading) {
        this.startOdometerReading = startOdometerReading;
    }

    public int getEndOdometerReading() {
        return endOdometerReading;
    }

    public void setEndOdometerReading(int endOdometerReading) {
        this.endOdometerReading = endOdometerReading;
    }

    public String getStartEngineHour() {
        return startEngineHour;
    }

    public void setStartEngineHour(String startEngineHour) {
        this.startEngineHour = startEngineHour;
    }

    public String getEndEngineHour() {
        return endEngineHour;
    }

    public void setEndEngineHour(String endEngineHour) {
        this.endEngineHour = endEngineHour;
    }

    public String getUnitNo() {
        return unitNo;
    }

    public void setUnitNo(String unitNo) {
        this.unitNo = unitNo;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getOdoUnit() {
        return odoUnit;
    }

    public void setOdoUnit(String odoUnit) {
        this.odoUnit = odoUnit;
    }

    public String getVIN() {
        return VIN;
    }

    public void setVIN(String VIN) {
        this.VIN = VIN;
    }
}
