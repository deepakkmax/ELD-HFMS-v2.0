package com.hutchsystems.hutchconnect.beans;

import java.util.Date;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class LocationBean {
    double bearing,Latitude, Longitude;
    String locationDescription = "";
    String validLocationDate;
    float  accuracy;

    String address = "", state = "", city = "", country = "", fullAddress = "";
    int gpsStatus;
    Date locationDate;

    public int getGpsStatus() {
        return gpsStatus;
    }

    public void setGpsStatus(int gpsStatus) {
        this.gpsStatus = gpsStatus;
    }

    public Date getLocationDate() {
        return locationDate;
    }

    public void setLocationDate(Date locationDate) {
        this.locationDate = locationDate;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getValidLocationDate() {
        return validLocationDate;
    }

    public void setValidLocationDate(String validLocationDate) {
        this.validLocationDate = validLocationDate;
    }

    public String getEngineHoursSinceLastValidCoordinate() {
        return EngineHoursSinceLastValidCoordinate;
    }

    public void setEngineHoursSinceLastValidCoordinate(String engineHoursSinceLastValidCoordinate) {
        EngineHoursSinceLastValidCoordinate = engineHoursSinceLastValidCoordinate;
    }

    String EngineHoursSinceLastValidCoordinate;

    public String getOdometerReadingSinceLastValidCoordinate() {
        return OdometerReadingSinceLastValidCoordinate;
    }

    public void setOdometerReadingSinceLastValidCoordinate(String odometerReadingSinceLastValidCoordinate) {
        OdometerReadingSinceLastValidCoordinate = odometerReadingSinceLastValidCoordinate;
    }

    String OdometerReadingSinceLastValidCoordinate = "0";

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
}
