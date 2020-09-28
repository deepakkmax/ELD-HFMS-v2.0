package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 5/9/2017.
 */

public class GeofenceBean {
    int portId, radius, vehicleId, enterByDriverId, exitByDriverId, regionInFg, statusId;
    String portRegionName, latitude, longitude, enterDate, exitDate;

    public int getPortId() {
        return portId;
    }

    public void setPortId(int portId) {
        this.portId = portId;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getEnterByDriverId() {
        return enterByDriverId;
    }

    public void setEnterByDriverId(int enterByDriverId) {
        this.enterByDriverId = enterByDriverId;
    }

    public int getExitByDriverId() {
        return exitByDriverId;
    }

    public void setExitByDriverId(int exitByDriverId) {
        this.exitByDriverId = exitByDriverId;
    }

    public int getRegionInFg() {
        return regionInFg;
    }

    public void setRegionInFg(int regionInFg) {
        this.regionInFg = regionInFg;
    }

    public String getPortRegionName() {
        return portRegionName;
    }

    public void setPortRegionName(String portRegionName) {
        this.portRegionName = portRegionName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getEnterDate() {
        return enterDate;
    }

    public void setEnterDate(String enterDate) {
        this.enterDate = enterDate;
    }

    public String getExitDate() {
        return exitDate;
    }

    public void setExitDate(String exitDate) {
        this.exitDate = exitDate;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }
}
