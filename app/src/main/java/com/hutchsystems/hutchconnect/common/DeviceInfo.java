package com.hutchsystems.hutchconnect.common;

/**
 * Created by SAMSUNG on 15-03-2017.
 */

public class DeviceInfo {
    private String simCardNo;
    private String telephoneNo;
    private String storage;
    private String batteryHealth;
    private String dataUsageApp;
    private String btbMac;
    private String wifiSSID;
    private String deviceUptime;
    private String totalDataUsage;
    private String deviceModel;
    private String OSVersion;
    private String serialNo;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String OSVersion) {
        this.OSVersion = OSVersion;
    }

    public String getEldDataUsage() {
        return eldDataUsage;
    }

    public void setEldDataUsage(String eldDataUsage) {
        this.eldDataUsage = eldDataUsage;
    }

    public String getTotalDataUsage() {
        return totalDataUsage;
    }

    public void setTotalDataUsage(String totalDataUsage) {
        this.totalDataUsage = totalDataUsage;
    }

    private String eldDataUsage;

    public String getSimCardNo() {
        return simCardNo;
    }

    public void setSimCardNo(String simCardNo) {
        this.simCardNo = simCardNo;
    }

    public String getTelephoneNo() {
        return telephoneNo;
    }

    public void setTelephoneNo(String telephoneNo) {
        this.telephoneNo = telephoneNo;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getBatteryHealth() {
        return batteryHealth;
    }

    public void setBatteryHealth(String batteryHealth) {
        this.batteryHealth = batteryHealth;
    }

    public String getDataUsageApp() {
        return dataUsageApp;
    }

    public void setDataUsageApp(String dataUsageApp) {
        this.dataUsageApp = dataUsageApp;
    }

    public String getBtbMac() {
        return btbMac;
    }

    public void setBtbMac(String btbMac) {
        this.btbMac = btbMac;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getDeviceUptime() {
        return deviceUptime;
    }

    public void setDeviceUptime(String deviceUptime) {
        this.deviceUptime = deviceUptime;
    }
}
